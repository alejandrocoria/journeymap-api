/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import journeymap.client.cartography.color.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.mod.IBlockSpritesProxy;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Block + meta = BlockMetaData.  Carries color, flags, and other
 * data points specific to a Block+meta combination.
 */
public class BlockMD implements Comparable<BlockMD>
{
    /**
     * Flags for plants and crops.
     */
    public static final EnumSet<BlockFlag> FlagsPlantAndCrop = EnumSet.of(BlockFlag.Plant, BlockFlag.Crop);

    /**
     * Non-error, non-ignore Flags.
     */
    public static final EnumSet<BlockFlag> FlagsNormal = EnumSet.complementOf(EnumSet.of(BlockFlag.Error, BlockFlag.Ignore));

    /**
     * Air stand-in.
     */
    public static final BlockMD AIRBLOCK = new BlockMD(Blocks.AIR.getDefaultState(), "minecraft:air", "0", "Air", 0f, EnumSet.of(BlockFlag.Ignore), false);
    /**
     * Void stand-io.
     */
    public static final BlockMD VOIDBLOCK = new BlockMD(Blocks.AIR.getDefaultState(), "journeymap:void", "0", "Void", 0f, EnumSet.of(BlockFlag.Ignore), false);

    private static Logger LOGGER = Journeymap.getLogger();
    private final IBlockState blockState;
    private final String blockId;
    private final String blockStateId;
    private final String name;
    private EnumSet<BlockFlag> flags;
    private Integer color;
    private float alpha;

    private IBlockSpritesProxy blockSpritesProxy;
    private IBlockColorProxy blockColorProxy;

    private boolean noShadow;
    private boolean isIgnore;
    private boolean isWater;
    private boolean isLava;
    private boolean isFluid;
    private boolean isFire;
    private boolean isIce;
    private boolean isFoliage;
    private boolean isGrass;
    private boolean isPlantOrCrop;
    private boolean isError;

    /**
     * Private constructor.
     */
    private BlockMD(@Nonnull IBlockState blockState)
    {
        this(blockState, getBlockId(blockState), getBlockStateId(blockState), getBlockName(blockState));
    }

    /**
     * Private constructor
     */
    private BlockMD(@Nonnull IBlockState blockState, String blockId, String blockStateId, String name) {
        this(blockState, blockId, blockStateId, name, 1F, EnumSet.noneOf(BlockFlag.class), true);
    }

    /**
     * Private constructor
     */
    private BlockMD(@Nonnull IBlockState blockState, String blockId, String blockStateId, String name, Float alpha, EnumSet<BlockFlag> flags, boolean initDelegates)
    {
        this.blockState = blockState;
        this.blockId = blockId;
        this.blockStateId = blockStateId;
        this.name = name;
        this.alpha = alpha;
        this.flags = flags;
        if (initDelegates)
        {
            ModBlockDelegate.INSTANCE.initialize(this);
        }
        updateProperties();
    }

    public Set<BlockMD> getValidStateMDs() {
        return getBlock().getBlockState().getValidStates().stream()
                .map(BlockMD::get)
                .collect(Collectors.toSet());
    }

    private void updateProperties()
    {
        isIgnore = (blockState == null || hasFlag(BlockFlag.Ignore) || blockState.getBlock() instanceof BlockAir || blockState.getRenderType() == EnumBlockRenderType.INVISIBLE);
        if (isIgnore) {
            color = RGB.WHITE_ARGB;
            setAlpha(0f);
            flags.add(BlockFlag.Ignore);
            flags.add(BlockFlag.OpenToSky);
            flags.add(BlockFlag.NoShadow);
        }

        if (blockState != null)
        {
            Block block = blockState.getBlock();
            isLava = block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
            isIce = block == Blocks.ICE;
            isFire = block == Blocks.FIRE;
        }

        //useDefaultState = hasFlag(BlockFlag.DefaultState);
        isFluid = hasFlag(BlockFlag.Fluid);
        isWater = hasFlag(BlockFlag.Water);
        noShadow = hasFlag(BlockFlag.NoShadow);
        isFoliage = hasFlag(BlockFlag.Foliage);
        isGrass = hasFlag(BlockFlag.Grass);
        isPlantOrCrop = hasAnyFlag(FlagsPlantAndCrop);
        isError = hasFlag(BlockFlag.Error);
    }

    /**
     * Gets block.
     *
     * @return the block
     */
    public Block getBlock()
    {
        return blockState.getBlock();
    }

    /**
     * Preloads the cache with all registered blocks and their subblocks.
     */
    public static void reset()
    {
        DataCache.INSTANCE.resetBlockMetadata();
    }

    /**
     * Get all BlockMDs.
     *
     * @return all
     */
    public static Set<BlockMD> getAll()
    {
        return StreamSupport.stream(GameData.getBlockStateIDMap().spliterator(), false)
                .map(BlockMD::get)
                .collect(Collectors.toSet());
    }

    /**
     * Get all BlockMDs that should have colors.
     *
     * @return all
     */
    public static Set<BlockMD> getAllValid() {
        return getAll().stream()
                .filter(blockMD -> !blockMD.isIgnore() && !blockMD.hasFlag(BlockFlag.Error))
                .collect(Collectors.toSet());
    }

    /**
     * Get all BlockMDs for vanilla blocks
     *
     * @return all
     */
    public static Set<BlockMD> getAllMinecraft() {
        return StreamSupport.stream(GameData.getBlockStateIDMap().spliterator(), false)
                .filter(blockState1 -> blockState1.getBlock().getRegistryName().getResourceDomain().equals("minecraft"))
                .map(BlockMD::get)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves a BlockMD instance corresponding to chunk-local coords.
     *
     * @param chunkMd the chunk md
     * @param localX  the local x
     * @param y       the y
     * @param localZ  the local z
     * @return the block md from chunk local
     */
    public static BlockMD getBlockMDFromChunkLocal(ChunkMD chunkMd, int localX, int y, int localZ)
    {
        return getBlockMD(chunkMd, chunkMd.getBlockPos(localX, y, localZ));
    }

    /**
     * Retrieves a BlockMD instance
     *
     * @param chunkMd  the chunk md
     * @param blockPos the block pos
     * @return the block md
     */
    public static BlockMD getBlockMD(ChunkMD chunkMd, BlockPos blockPos)
    {
        try
        {
            if (blockPos.getY() >= 0)
            {
                IBlockState blockState;
                if (chunkMd != null && chunkMd.hasChunk())
                {
                    blockState = chunkMd.getChunk().getBlockState(blockPos);
                }
                else
                {
                    blockState = JmBlockAccess.INSTANCE.getBlockState(blockPos);
                }

                return get(blockState);
            }
            else
            {
                return VOIDBLOCK;
            }
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("Can't get blockId/meta for chunk %s,%s at %s : %s", chunkMd.getChunk().x, chunkMd.getChunk().z, blockPos, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    /**
     * Retrieves/lazy-creates the corresponding BlockMD instance.
     *
     * @param blockState the block state
     * @return the block md
     */
    public static BlockMD get(IBlockState blockState)
    {
        return DataCache.INSTANCE.getBlockMD(blockState);
    }

    /**
     * Get a Block ID from the state.
     */
    public static String getBlockId(BlockMD blockMD)
    {
        return getBlockId(blockMD.getBlockState());
    }

    /**
     * Get a Block ID from the state.
     */
    public static String getBlockId(IBlockState blockState) {
        return Block.REGISTRY.getNameForObject(blockState.getBlock()).toString();
    }

    /**
     * Get a BlockState ID from the state.
     */
    public static String getBlockStateId(BlockMD blockMD) {
        return getBlockStateId(blockMD.getBlockState());
    }

    /**
     * Get a BlockState ID from the state.
     */
    public static String getBlockStateId(IBlockState blockState) {
        Collection properties = blockState.getProperties().values();
        if (properties.isEmpty())
        {
            return Integer.toString(blockState.getBlock().getMetaFromState(blockState));
        } else {
            return Joiner.on(",").join(properties);
        }
    }

    /**
     * Get a displayname for the block.
     */
    private static String getBlockName(IBlockState blockState)
    {
        String displayName = null;
        try
        {
            Block block = blockState.getBlock();
            Item item = Item.getItemFromBlock(block);
            if (item != null)
            {
                ItemStack idPicked = new ItemStack(item, 1, block.getMetaFromState(blockState));
                displayName = I18n.translateToLocal(item.getUnlocalizedName(idPicked) + ".name");
            }

            if (Strings.isNullOrEmpty(displayName))
            {
                displayName = block.getLocalizedName();
            }

        }
        catch (Exception e)
        {
            LOGGER.debug(String.format("Couldn't get display name for %s: %s ", blockState, e));
        }

        if (Strings.isNullOrEmpty(displayName) || displayName.contains("tile"))
        {
            displayName = blockState.getBlock().getClass().getSimpleName().replaceAll("Block", "");
        }

        return displayName;
    }

    /**
     * Set flags on all BlockMD variations of Block.
     *
     * @param block the block
     * @param flags the flags
     */
    public static void setAllFlags(Block block, BlockFlag... flags)
    {
        BlockMD defaultBlockMD = BlockMD.get(block.getDefaultState());
        for (BlockMD blockMD : defaultBlockMD.getValidStateMDs())
        {
            blockMD.addFlags(flags);
        }
        LOGGER.debug(block.getUnlocalizedName() + " flags set: " + flags);
    }

    /**
     * Whether BlockMD has the flag.
     *
     * @param checkFlag the flag to check for
     * @return true if found
     */
    public boolean hasFlag(BlockFlag checkFlag)
    {
        return flags.contains(checkFlag);
    }

    /**
     * Whether BlockMD has any flag.
     *
     * @param checkFlags the flags to check for
     * @return true if found
     */
    public boolean hasAnyFlag(EnumSet<BlockFlag> checkFlags)
    {
        for (BlockFlag flag : checkFlags)
        {
            if (flags.contains(flag))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Add flags.
     *
     * @param addFlags flags to add
     */
    public void addFlags(BlockFlag... addFlags)
    {
        Collections.addAll(this.flags, addFlags);
        updateProperties();
    }

    /**
     * Remove flags.
     *
     * @param removeFlags flags to remove
     */
    public void removeFlags(BlockFlag... removeFlags) {
        for (BlockFlag flag : removeFlags) {
            this.flags.remove(flag);
        }
        updateProperties();
    }

    /**
     * Remove flags.
     *
     * @param removeFlags flags to remove
     */
    public void removeFlags(Collection<BlockFlag> removeFlags) {
        this.flags.removeAll(removeFlags);
        updateProperties();
    }

    /**
     * Add flags.
     *
     * @param addFlags the add flags
     */
    public void addFlags(Collection<BlockFlag> addFlags)
    {
        this.flags.addAll(addFlags);
        updateProperties();
    }

    /**
     * Gets block color using world coordinates.
     *
     * @param chunkMD  the chunk md
     * @param blockPos the block pos
     * @return the color
     */
    public int getBlockColor(ChunkMD chunkMD, BlockPos blockPos)
    {
        return blockColorProxy.getBlockColor(chunkMD, this, blockPos);
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public int getTextureColor()
    {
        if (color == null && !isError && blockColorProxy != null) {
            this.color = blockColorProxy.deriveBlockColor(this);
        }
        if (color == null) {
            this.color = RGB.BLACK_RGB;
        }
        return this.color;
    }

    /**
     * Clears the color.
     */
    public void clearColor() {
        this.color = null;
    }

    /**
     * Sets color.
     *
     * @param baseColor the base color
     */
    public int setColor(int baseColor)
    {
        this.color = baseColor;
        return baseColor;
    }

    /**
     * Whether a color is set.
     * @return
     */
    public boolean hasColor() {
        return this.color != null;
    }

    /**
     * Sets blockSprites proxy.
     *
     * @param blockSpritesProxy the blockSprites proxy
     */
    public void setBlockSpritesProxy(IBlockSpritesProxy blockSpritesProxy) {
        this.blockSpritesProxy = blockSpritesProxy;
    }

    /**
     * Returns blockSprites proxy.
     */
    public IBlockSpritesProxy getBlockSpritesProxy() {
        return blockSpritesProxy;
    }

    /**
     * Sets block color proxy.
     *
     * @param blockColorProxy the block color handler
     */
    public void setBlockColorProxy(IBlockColorProxy blockColorProxy) {
        this.blockColorProxy = blockColorProxy;
    }

    /**
     * Returns block color proxy.
     */
    public IBlockColorProxy getBlockColorProxy() {
        return blockColorProxy;
    }

    /**
     * Gets alpha.
     *
     * @return the alpha
     */
    public float getAlpha()
    {
        return alpha;
    }

    /**
     * Sets alpha.
     *
     * @param alpha the alpha
     */
    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
        if (alpha < 1f) {
            this.flags.add(BlockFlag.Transparency);
        } else {
            this.flags.remove(BlockFlag.Transparency);
        }
    }

    /**
     * Whether it should be used for beveled slope coloration.
     *
     * @return boolean
     */
    public boolean hasNoShadow()
    {
        if (noShadow)
        {
            return true;
        }

        return (isPlantOrCrop && !Journeymap.getClient().getCoreProperties().mapPlantShadows.get());
    }

    /**
     * Gets the blockstate
     *
     * @return the blockstate
     */
    public IBlockState getBlockState()
    {
        return blockState;
    }

    /**
     * Has tranparency.
     *
     * @return the boolean
     */
    public boolean hasTransparency()
    {
        return alpha < 1f;
    }

    /**
     * Is air.
     *
     * @return the boolean
     */
    public boolean isIgnore() {
        return isIgnore;
    }

    /**
     * Is ice.
     *
     * @return the boolean
     */
    public boolean isIce()
    {
        return isIce;
    }

    /**
     * Is water.
     *
     * @return the boolean
     */
    public boolean isWater()
    {
        return isWater;
    }

    /**
     * Is fluid.
     *
     * @return the boolean
     */
    public boolean isFluid() {
        return isFluid;
    }

    /**
     * Is lava.
     *
     * @return the boolean
     */
    public boolean isLava() {
        return isLava;
    }


    /**
     * Is lava.
     *
     * @return the boolean
     */
    public boolean isFire() {
        return isFire;
    }


    /**
     * Is foliage.
     *
     * @return the boolean
     */
    public boolean isFoliage()
    {
        return isFoliage;
    }

    /**
     * Is grass.
     *
     * @return the boolean
     */
    public boolean isGrass()
    {
        return isGrass;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets block state ID
     *
     * @return uid
     */
    public String getBlockId() {
        return blockId;
    }

    public String getBlockStateId() {
        return blockStateId;
    }

    public String getBlockDomain() {
        return getBlock().getRegistryName().getResourceDomain();
    }

    /**
     * Gets flags
     *
     * @return flags
     */
    public EnumSet<BlockFlag> getFlags()
    {
        return flags;
    }

    public boolean isVanillaBlock() {
        return getBlockDomain().equals("minecraft");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BlockMD))
        {
            return false;
        }
        BlockMD blockMD = (BlockMD) o;
        return Objects.equal(getBlockId(), blockMD.getBlockId()) &&
                Objects.equal(getBlockStateId(), blockMD.getBlockStateId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBlockId(), getBlockStateId());
    }

    @Override
    public String toString() {
        return String.format("BlockMD [%s] (%s)", blockState, Joiner.on(",").join(flags));
    }

    @Override
    public int compareTo(BlockMD that)
    {
        Ordering ordering = Ordering.natural().nullsLast();
        return ComparisonChain.start()
                .compare(this.blockId, that.blockId, ordering)
                .compare(this.blockStateId, that.blockStateId, ordering)
                .result();
    }

    /**
     * The type Simple cache loader.
     */
    public static class CacheLoader extends com.google.common.cache.CacheLoader<IBlockState, BlockMD> {
        @Override
        public BlockMD load(@Nonnull IBlockState blockState) throws Exception {

            try {
                if (blockState instanceof IExtendedBlockState) {
                    IBlockState clean = ((IExtendedBlockState) blockState).getClean();
                    if (clean != null) {
                        blockState = clean;
                    }
                }

                if (blockState == null || blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
                    return BlockMD.AIRBLOCK;
                }

                if (blockState.getBlock().getRegistryName() == null) {
                    LOGGER.warn("Unregistered block will be treated like air: " + blockState);
                    return BlockMD.AIRBLOCK;
                }

                return new BlockMD(blockState);
            } catch (Exception e) {
                LOGGER.error(String.format("Can't get BlockMD for %s : %s", blockState, LogFormatter.toPartialString(e)));
                return BlockMD.AIRBLOCK;
            }
        }
    }

}
