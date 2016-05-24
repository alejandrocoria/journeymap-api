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
import journeymap.client.JourneymapClient;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.StatTimer;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.*;

/**
 * Block + meta = BlockMetaData.  Carries color, flags, and other
 * data points specific to a Block+meta combination.
 */
public class BlockMD
{
    public static final EnumSet FlagsPlantAndCrop = EnumSet.of(Flag.Plant, Flag.Crop);
    public static final EnumSet FlagsBiomeColored = EnumSet.of(Flag.Grass, Flag.Foliage, Flag.Water, Flag.CustomBiomeColor);
    private static final Map<IBlockState, BlockMD> cache = new HashMap<IBlockState, BlockMD>();
    public static BlockMD AIRBLOCK;
    public static BlockMD VOIDBLOCK;
    private static ModBlockDelegate modBlockDelegate = new ModBlockDelegate();
    private IBlockState blockState;
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

    /**
     * Private constructor.
     */
    private BlockMD(IBlockState blockState)
    {
        this(blockState, blockState.getBlock().getRegistryName(), BlockMD.getBlockName(blockState), 1F, 1, EnumSet.noneOf(BlockMD.Flag.class));
    }

    /**
     * Private constructor
     */
    private BlockMD(IBlockState blockState, String uid, String name, Float alpha, int textureSide, EnumSet<Flag> flags)
    {
        this.blockState = blockState;
        this.uid = uid;
        this.name = name;
        this.alpha = alpha;
        this.textureSide = textureSide;
        this.flags = flags;
        this.blockColorHandler = VanillaColorHandler.INSTANCE;
        if (blockState != null && blockState.getBlock() != null)
        {
            modBlockDelegate.initialize(this);
        }
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

    /**
     * Preloads the cache with all registered blocks and their subblocks.
     */
    public static void reset()
    {
        StatTimer timer = StatTimer.get("BlockMD.reset", 0, 2000);
        timer.start();
        cache.clear();

        // Create new delegate
        modBlockDelegate = new ModBlockDelegate();

        // Dummy blocks
        AIRBLOCK = new BlockMD(Blocks.air.getDefaultState(), "minecraft:air", "Air", 0f, 1, EnumSet.of(BlockMD.Flag.HasAir));
        VOIDBLOCK = new BlockMD(null, "journeymap:void", "Void", 0f, 1, EnumSet.noneOf(BlockMD.Flag.class));

        // Load all registered block+metas
        Collection<BlockMD> all = getAll();

        // Final color updates
        VanillaColorHandler.INSTANCE.setExplicitColors();

        timer.stop();
        Journeymap.getLogger().info(String.format("Built BlockMD cache (%s) : %s", all.size(), timer.getLogReportString()));
    }

    /**
     * Get all BlockMDs.
     *
     * @return
     */
    public static Collection<BlockMD> getAll()
    {
        List<BlockMD> allBlockMDs = new ArrayList<BlockMD>(512);
        for (Block block : GameData.getBlockRegistry().typeSafeIterable())
        {
            allBlockMDs.addAll(BlockMD.getAllBlockMDs(block));
        }
        return allBlockMDs;
    }

    /**
     * Retrieves a BlockMD instance corresponding to chunk-local coords.
     */
    public static BlockMD getBlockMD(ChunkMD chunkMd, int localX, int y, int localZ)
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
                IBlockState blockState = ForgeHelper.INSTANCE.getIBlockAccess().getBlockState(blockPos);
                BlockMD blockMD = get(blockState);
                if (blockMD.isAir())
                {
                    return blockMD;
                }
                else if (blockMD.hasFlag(Flag.SpecialHandling))
                {
                    BlockMD delegated = ModBlockDelegate.handleBlock(chunkMd, blockMD, blockPos);
                    if (delegated != null)
                    {
                        blockMD = delegated;
                    }
                }
                return blockMD;
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
     * Retrieves/lazy-creates the corresponding BlockMD instance.
     */
    public static BlockMD get(IBlockState blockState)
    {
        try
        {
            if (blockState == null)
            {
                return AIRBLOCK;
            }

            BlockMD blockMD = cache.get(blockState);
            if (blockMD == null)
            {
                blockMD = new BlockMD(blockState);
                cache.put(blockState, blockMD);
            }

            return blockMD;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get BlockMD for %s : %s", blockState, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
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
                displayName = StatCollector.translateToLocal(item.getUnlocalizedName(idPicked) + ".name");
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
        List<IBlockState> states = block.getBlockState().getValidStates();
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
     * Set alpha on all BlockMD variations of Block.
     */
    public static void setAllAlpha(Block block, Float alpha)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.setAlpha(alpha);
        }
        Journeymap.getLogger().debug(block.getUnlocalizedName() + " alpha set: " + alpha);
    }

    /**
     * Set texture side on all BlockMD variations of Block.
     */
    public static void setTextureSide(Block block, int side)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.setTextureSide(side);
        }
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
    }

    /**
     * Add flags.
     */
    public void addFlags(Collection<Flag> addFlags)
    {
        this.flags.addAll(addFlags);
    }

    /**
     * Clear flags
     */
    public void clearFlags()
    {
        this.flags.clear();
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
            if (this.hasFlag(Flag.Transparency))
            {
                this.flags.remove(Flag.Transparency);
            }
        }
    }

    /**
     * Whether it should be used for beveled slope coloration.
     *
     * @return
     */
    public boolean hasNoShadow()
    {
        if (hasFlag(Flag.NoShadow))
        {
            return true;
        }

        return (hasAnyFlag(FlagsPlantAndCrop) && !JourneymapClient.getCoreProperties().mapPlantShadows.get());
    }

    /**
     * Gets block.
     *
     * @return the block
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
        return hasFlag(Flag.Transparency);
    }

    /**
     * Is air.
     *
     * @return the boolean
     */
    public boolean isAir()
    {
        return blockState.getBlock() instanceof BlockAir || hasFlag(Flag.HasAir);
    }

    /**
     * Is ice.
     *
     * @return the boolean
     */
    public boolean isIce()
    {
        return blockState.getBlock() == Blocks.ice;
    }


    /**
     * Is water.
     *
     * @return the boolean
     */
    public boolean isWater()
    {
        return hasFlag(Flag.Water);
    }

    /**
     * Is transparent roof.
     *
     * @return the boolean
     */
    public boolean isTransparentRoof()
    {
        return hasFlag(Flag.TransparentRoof);
    }

    /**
     * Is lava.
     *
     * @return the boolean
     */
    public boolean isLava()
    {
        return blockState.getBlock() == Blocks.lava || blockState.getBlock() == Blocks.flowing_lava;
    }

    /**
     * Is foliage.
     *
     * @return the boolean
     */
    public boolean isFoliage()
    {
        return hasFlag(Flag.Foliage);
    }

    /**
     * Is grass.
     *
     * @return the boolean
     */
    public boolean isGrass()
    {
        return hasFlag(Flag.Grass);
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
        return hasAnyFlag(FlagsBiomeColored);
    }

    /**
     * Gets texture side
     *
     * @return
     */
    public int getTextureSide()
    {
        return textureSide;
    }

    /**
     * Sets texture side
     *
     * @param textureSide
     */
    public void setTextureSide(int textureSide)
    {
        this.textureSide = textureSide;
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

        //BlockMD blockMD = (BlockMD) o;

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
