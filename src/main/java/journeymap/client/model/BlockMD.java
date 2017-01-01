/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ColorHelper;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.client.model.mod.vanilla.VanillaColorHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.GameData;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Block + meta = BlockMetaData.  Carries color, flags, and other
 * data points specific to a Block+meta combination.
 */
public class BlockMD implements Comparable<BlockMD>
{
    public static final EnumSet FlagsPlantAndCrop = EnumSet.of(Flag.Plant, Flag.Crop);
    public static final EnumSet FlagsBiomeColored = EnumSet.of(Flag.Grass, Flag.Foliage, Flag.Water, Flag.CustomBiomeColor);
    public static BlockMD AIRBLOCK;
    public static BlockMD VOIDBLOCK;
    private static Logger LOGGER = Journeymap.getLogger();
    private static ModBlockDelegate modBlockDelegate = new ModBlockDelegate();
    private final IBlockState blockState;
    private final String uid;
    private final String name;
    private EnumSet<Flag> flags;
    private int textureSide;
    private Integer color;
    private float alpha;
    private String iconName;
    private ModBlockDelegate.IModBlockColorHandler blockColorHandler;
    private ModBlockDelegate.IModBlockHandler modBlockHandler;
    private boolean useDefaultState;

    private boolean noShadow;
    private boolean isAir;
    private boolean isWater;
    private boolean isLava;
    private boolean isIce;
    private boolean isFoliage;
    private boolean isGrass;
    private boolean isTransparentRoof;
    private boolean isPlantOrCrop;
    private boolean isBiomeColored;

    public static BlockMD create(@Nonnull IBlockState blockState)
    {
        try
        {
            if (blockState.getBlock() == Blocks.AIR || blockState.getBlock() instanceof BlockAir || blockState.getRenderType()== EnumBlockRenderType.INVISIBLE)
            {
                return BlockMD.AIRBLOCK;
            }

            if (blockState.getBlock().getRegistryName() == null)
            {
                if(LOGGER.isDebugEnabled())
                {
                    Journeymap.getLogger().debug("Unregistered block will be treated like air: " + blockState);
                }
                return BlockMD.AIRBLOCK;
            }

            return new BlockMD(blockState);
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get BlockMD for %s : %s", blockState, LogFormatter.toString(e)));
            return BlockMD.AIRBLOCK;
        }
    }

    /**
     * Private constructor.
     */
    private BlockMD(@Nonnull IBlockState blockState)
    {
        this(blockState, blockState.getBlock().getRegistryName().toString(), BlockMD.getBlockName(blockState), 1F, 1, EnumSet.noneOf(BlockMD.Flag.class));
    }

    /**
     * Private constructor
     */
    private BlockMD(@Nonnull IBlockState blockState, String uid, String name, Float alpha, int textureSide, EnumSet<Flag> flags)
    {
        this.blockState = blockState;
        this.uid = uid;
        this.name = name;
        this.alpha = alpha;
        this.textureSide = textureSide;
        this.flags = flags;
        this.blockColorHandler = VanillaColorHandler.INSTANCE;
        if (blockState != null && blockState.getBlock() != null && uid != null)
        {
            modBlockDelegate.initialize(this);
        }
        updateProperties();
    }

    private void updateProperties()
    {

        isAir = (blockState == null || hasFlag(Flag.HasAir) || blockState.getBlock() instanceof BlockAir);

        if (blockState != null)
        {
            isLava = blockState.getBlock() == Blocks.LAVA || blockState.getBlock() == Blocks.FLOWING_LAVA;
            isIce = blockState.getBlock() == Blocks.ICE;
        }

        isWater = hasFlag(Flag.Water);
        isTransparentRoof = hasFlag(Flag.TransparentRoof);
        noShadow = hasFlag(Flag.NoShadow);
        isFoliage = hasFlag(Flag.Foliage);
        isGrass = hasFlag(Flag.Grass);
        isPlantOrCrop = hasAnyFlag(FlagsPlantAndCrop);
        isBiomeColored = hasAnyFlag(FlagsBiomeColored);
    }

    public boolean isUseDefaultState()
    {
        return useDefaultState;
    }

    public BlockMD setUseDefaultState(boolean useDefaultState)
    {
        this.useDefaultState = useDefaultState;
        return this;
    }

    public Block getBlock()
    {
        return blockState.getBlock();
    }

    public int getMeta()
    {
        return blockState.getBlock().getMetaFromState(blockState);
    }

    /**
     * Preloads the cache with all registered blocks and their subblocks.
     */
    public static void reset()
    {
        DataCache.INSTANCE.resetBlockMetadata();
        ColorHelper.INSTANCE.resetIconColorCache();

        // Create new delegate
        modBlockDelegate = new ModBlockDelegate();

        // Dummy blocks
        AIRBLOCK = new BlockMD(Blocks.AIR.getDefaultState(), "minecraft:air", "Air", 0f, 1, EnumSet.of(BlockMD.Flag.HasAir));
        VOIDBLOCK = new BlockMD(null, "journeymap:void", "Void", 0f, 1, EnumSet.noneOf(BlockMD.Flag.class));

        // Load all registered block+metas
        Collection<BlockMD> all = getAll();

        // Final color updates
        VanillaColorHandler.INSTANCE.setExplicitColors();
    }

    /**
     * Get all BlockMDs.
     *
     * @return
     */
    public static Collection<BlockMD> getAll()
    {
        List<BlockMD> allBlockMDs = new ArrayList<BlockMD>(512);
        Block.REGISTRY.forEach(block -> {
            allBlockMDs.addAll(BlockMD.getAllBlockMDs(block));
        });
        Collections.sort(allBlockMDs);
        return allBlockMDs;
    }

    /**
     * Retrieves a BlockMD instance corresponding to chunk-local coords.
     */
    public static BlockMD getBlockMDFromChunkLocal(ChunkMD chunkMd, int localX, int y, int localZ)
    {
        return getBlockMD(chunkMd, chunkMd.getBlockPos(localX, y, localZ));
    }

    /**
     * Retrieves a BlockMD instance
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
                    blockState = ForgeHelper.INSTANCE.getIBlockAccess().getBlockState(blockPos);
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
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for chunk %s,%s at %s : %s", chunkMd.getChunk().xPosition, chunkMd.getChunk().zPosition, blockPos, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    /**
     * Finds BlockMD by block uid + meta
     */
    public static BlockMD get(String uid, int meta)
    {
        Block block = GameData.getBlockRegistry().getObject(new ResourceLocation(uid));
        if (block == null)
        {
            return null;
        }
        return BlockMD.get(block.getStateFromMeta(meta));
    }

    /**
     * Retrieves/lazy-creates the corresponding BlockMD instance.
     */
    public static BlockMD get(IBlockState blockState)
    {
        return DataCache.INSTANCE.getBlockMD(blockState);
    }

    public static void debug()
    {
        for (BlockMD blockMD : getAll())
        {
            Journeymap.getLogger().info(blockMD);
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
            Journeymap.getLogger().warn(String.format("Couldn't get display name for %s: %s ", blockState, e));
        }

        if (Strings.isNullOrEmpty(displayName) || displayName.contains("tile"))
        {
            displayName = blockState.getBlock().getClass().getSimpleName().replaceAll("Block", "");
        }

        return displayName;
    }

    /**
     * Get all BlockMD variations for a given block.
     */
    public static Collection<BlockMD> getAllBlockMDs(Block block)
    {
        List<IBlockState> states = new ArrayList<IBlockState>(block.getBlockState().getValidStates());
        Collections.sort(states, (o1, o2) -> Integer.compare(o1.getBlock().getMetaFromState(o1), o2.getBlock().getMetaFromState(o2)));

        List<BlockMD> blockMDs = new ArrayList<BlockMD>(states.size());
        for (IBlockState state : states)
        {
            blockMDs.add(BlockMD.get(state));
        }
        return blockMDs;
    }

    /**
     * Set flags on all BlockMD variations of Block.
     */
    public static void setAllFlags(Block block, BlockMD.Flag... flags)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.addFlags(flags);
        }
        Journeymap.getLogger().debug(block.getUnlocalizedName() + " flags set: " + flags);
    }

    /**
     * Whether BlockMD has the flag.
     *
     * @param checkFlag the flag to check for
     * @return true if found
     */
    public boolean hasFlag(Flag checkFlag)
    {
        return flags.contains(checkFlag);
    }

    /**
     * Whether BlockMD has any flag.
     *
     * @param checkFlags the flags to check for
     * @return true if found
     */
    public boolean hasAnyFlag(EnumSet<Flag> checkFlags)
    {
        for (Flag flag : checkFlags)
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
     */
    public void addFlags(Flag... addFlags)
    {
        Collections.addAll(this.flags, addFlags);
        updateProperties();
    }

    /**
     * Add flags.
     */
    public void addFlags(Collection<Flag> addFlags)
    {
        this.flags.addAll(addFlags);
        updateProperties();
    }

    /**
     * Gets block color using world coordinates.
     */
    public int getColor(ChunkMD chunkMD, BlockPos blockPos)
    {
        return blockColorHandler.getBlockColor(chunkMD, this, blockPos);
    }

    public Integer getColor()
    {
        ensureColor();
        return this.color;
    }

    public void setColor(Integer baseColor)
    {
        this.color = baseColor;
    }

    public boolean ensureColor()
    {
        if (this.color == null)
        {
            this.color = this.blockColorHandler.getTextureColor(this);
            return true;
        }
        return false;
    }

    public void setBlockColorHandler(ModBlockDelegate.IModBlockColorHandler blockColorHandler)
    {
        this.blockColorHandler = blockColorHandler;
    }

    public String getIconName()
    {
        return iconName == null ? "" : iconName;
    }

    public void setIconName(String iconName)
    {
        this.iconName = iconName;
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
        if (alpha < 1f)
        {
            this.flags.add(Flag.Transparency);
        }
        else
        {
            this.flags.remove(Flag.Transparency);
        }
    }

    /**
     * Whether it should be used for beveled slope coloration.
     *
     * @return
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
        return useDefaultState ? blockState.getBlock().getDefaultState() : blockState;
    }

    /**
     * Has tranparency.
     *
     * @return the boolean
     */
    public boolean hasTranparency()
    {
        return alpha < 1f;
    }

    /**
     * Is air.
     *
     * @return the boolean
     */
    public boolean isAir()
    {
        return isAir;
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
     * Is transparent roof.
     *
     * @return the boolean
     */
    public boolean isTransparentRoof()
    {
        return isTransparentRoof;
    }

    /**
     * Is lava.
     *
     * @return the boolean
     */
    public boolean isLava()
    {
        return isLava;
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
     * Gets UID
     *
     * @return
     */
    public String getUid()
    {
        return uid;
    }


    /**
     * Gets flags
     *
     * @return
     */
    public EnumSet<Flag> getFlags()
    {
        return flags;
    }

    /**
     * Is biome colored.
     *
     * @return the boolean
     */
    public boolean isBiomeColored()
    {
        return isBiomeColored;
    }

    /**
     * Gets handler for special mod blocks.
     */
    public ModBlockDelegate.IModBlockHandler getModBlockHandler()
    {
        return modBlockHandler;
    }

    /**
     * Sets handler for special mod blocks.
     *
     * @param modBlockHandler
     */
    public void setModBlockHandler(ModBlockDelegate.IModBlockHandler modBlockHandler)
    {
        this.modBlockHandler = modBlockHandler;
        if (modBlockHandler == null)
        {
            flags.remove(Flag.SpecialHandling);
        }
        else
        {
            flags.add(Flag.SpecialHandling);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        return blockState.equals(((BlockMD) o).blockState);
    }

    @Override
    public int hashCode()
    {
        return uid.hashCode();
    }

    @Override
    public String toString()
    {
        return String.format("BlockMD [%s:%s] (%s)", uid, blockState, Joiner.on(",").join(flags));
    }

    @Override
    public int compareTo(BlockMD that)
    {
        Ordering ordering = Ordering.natural().nullsLast();
        return ComparisonChain.start()
                .compare(this.uid, that.uid, ordering)
                .compare(this.getMeta(), that.getMeta(), ordering)
                .compare(this.name, that.name, ordering)
                .compare(this.color, that.color, ordering)
                .compare(this.alpha, that.alpha, ordering)
                .result();
    }

    /**
     * Flags indicating special behaviors of Blocks.
     */
    public enum Flag
    {
        /**
         * Block should be treated like air.
         */
        HasAir,

        /**
         * Block color is custom + determined by biome.
         */
        CustomBiomeColor,

        /**
         * Block color is determined by biome foliage multiplier
         */
        Foliage,

        /**
         * Block color is determined by biome grass multiplier
         */
        Grass,

        /**
         * Block color is determined by biome water multiplier
         */
        Water,

        /**
         * Block doesn't count as overhead cover.
         */
        OpenToSky,

        /**
         * Block shouldn't cast a shadow.
         */
        NoShadow,

        /**
         * Block isn't opaque.
         */
        Transparency,

        /**
         * Block was processed with errors.
         */
        Error,

        /**
         * Block is transparent and is ignored by Minecraft's chunk heights.
         */
        TransparentRoof,

        /**
         * Block is a non-crop plant
         */
        Plant,

        /**
         * Block is a crop
         */
        Crop,

        /**
         * Block is a tile entity
         */
        TileEntity,

        /**
         * Block has special handling considerations
         */
        SpecialHandling,

        /**
         * Block should be ignored in topological maps
         */
        NoTopo,

        /**
         * Block produces error when calling getColorMultiplier();
         */
        TintError;
    }
}
