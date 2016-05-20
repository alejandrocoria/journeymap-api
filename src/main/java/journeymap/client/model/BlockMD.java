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
    private static final Map<Block, Map<Integer, BlockMD>> cache = new HashMap<Block, Map<Integer, BlockMD>>();
    public static BlockMD AIRBLOCK;
    public static BlockMD VOIDBLOCK;
    private static ModBlockDelegate modBlockDelegate = new ModBlockDelegate();
    private final Block block;
    private final int meta;
    private final String uid;
    private final String name;
    private EnumSet<Flag> flags;
    private int textureSide;
    private Integer overrideMeta;
    private Integer color;
    private float alpha;
    private String iconName;
    private ModBlockDelegate.IModBlockColorHandler blockColorHandler;

    private ModBlockDelegate.IModBlockHandler modBlockHandler;

    /**
     * Private constructor.
     */
    private BlockMD(Block block, int meta)
    {
        this(block, meta, Block.REGISTRY.getNameForObject(block).toString(), BlockMD.getBlockName(block, meta), 1F, 1, EnumSet.noneOf(BlockMD.Flag.class));
    }

    /**
     * Private constructor
     */
    private BlockMD(Block block, int meta, String uid, String name, Float alpha, int textureSide, EnumSet<Flag> flags)
    {
        this.block = block;
        this.meta = meta;
        this.uid = uid;
        this.name = name;
        this.alpha = alpha;
        this.textureSide = textureSide;
        this.flags = flags;
        this.blockColorHandler = VanillaColorHandler.INSTANCE;
        if (block != null)
        {
            modBlockDelegate.initialize(this);
        }
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
        AIRBLOCK = new BlockMD(Blocks.AIR, 0, "minecraft:air", "Air", 0f, 1, EnumSet.of(BlockMD.Flag.HasAir));
        VOIDBLOCK = new BlockMD(null, 0, "journeymap:void", "Void", 0f, 1, EnumSet.noneOf(BlockMD.Flag.class));

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
            Collection<Integer> metas = BlockMD.getMetaValuesForBlock(block);
            for (int meta : metas)
            {
                allBlockMDs.add(get(block, meta, metas.size()));
            }
        }
        return allBlockMDs;
    }

    /**
     * Retrieves a BlockMD instance corresponding to chunk-local coords.
     */
    public static BlockMD getBlockMD(ChunkMD chunkMd, int localX, int y, int localZ)
    {
        try
        {
            if (y >= 0)
            {
                Block block = chunkMd.getBlock(localX, y, localZ);

                if (block instanceof BlockAir)
                {
                    return AIRBLOCK;
                }
                else
                {
                    int meta = chunkMd.getBlockMeta(localX, y, localZ);
                    BlockMD blockMD = get(block, meta);
                    if (blockMD.hasFlag(Flag.SpecialHandling))
                    {
                        BlockMD delegated = ModBlockDelegate.handleBlock(chunkMd, blockMD, localX, y, localZ);
                        if (delegated != null)
                        {
                            blockMD = delegated;
                        }
                    }
                    return blockMD;
                }
            }
            else
            {
                return VOIDBLOCK;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for chunk %s,%s block %s,%s,%s : %s", chunkMd.getChunk().xPosition, chunkMd.getChunk().zPosition, localX, y, localZ, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    /**
     * Retrieves/lazy-creates the corresponding BlockMD instance.
     */
    public static BlockMD get(Block block, int meta)
    {
        return get(block, meta, null);
    }

    /**
     * Retrieves/lazy-creates the corresponding BlockMD instance,
     * preinitializing the inner map to subBlocks size if needed.
     */
    private static BlockMD get(Block block, int meta, Integer subBlocks)
    {
        try
        {
            if (block == null)
            {
                return AIRBLOCK;
            }

            Map<Integer, BlockMD> map = cache.get(block);
            if (map == null)
            {
                if (subBlocks == null)
                {
                    subBlocks = BlockMD.getMetaValuesForBlock(block).size();
                }
                int size = (int) Math.ceil(Math.max(1, subBlocks) * 1.25);
                map = new HashMap<Integer, BlockMD>(size + (size / 2));
                cache.put(block, map);
            }

            BlockMD blockMD = map.get(meta);
            if (blockMD == null)
            {
                if (Block.REGISTRY.getNameForObject(block) == null)
                {
                    Journeymap.getLogger().warn(String.format("Can't find UID for block %s", block));
                    return AIRBLOCK;
                }
                blockMD = new BlockMD(block, meta);
                map.put(meta, blockMD);
            }

            return blockMD;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for block %s meta %s : %s", block, meta, LogFormatter.toString(e)));
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
    private static String getBlockName(Block block, int meta)
    {
        String name = null;
        try
        {
            name = ForgeHelper.INSTANCE.getBlockName(block, meta);
            if (name == null)
            {
                name = block.getUnlocalizedName().replaceAll("tile.", "").replaceAll("Block", "");
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().debug("Displayname not available for " + name);
        }

        if (Strings.isNullOrEmpty(name))
        {
            name = block.getClass().getSimpleName().replaceAll("Block", "");
        }
        return name;
    }

    /**
     * Gets the meta variants possible for a given Block.
     */
    public static Collection<Integer> getMetaValuesForBlock(Block block)
    {
        ArrayList<Integer> metas = new ArrayList<Integer>();

        for (IBlockState state : block.getBlockState().getValidStates())
        {
            int meta = state.getBlock().getMetaFromState(state);
            metas.add(meta);
        }

        return metas;
    }

    /**
     * Get all BlockMD variations for a given block.
     */
    public static Collection<BlockMD> getAllBlockMDs(Block block)
    {
        if (cache.isEmpty())
        {
            reset();
        }

        Collection<Integer> metas = BlockMD.getMetaValuesForBlock(block);
        List<BlockMD> list = new ArrayList<BlockMD>(metas.size());
        for (int meta : metas)
        {
            list.add(BlockMD.get(block, meta));
        }
        return list;
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
    public int getColor(ChunkMD chunkMD, int globalX, int y, int globalZ)
    {
        return blockColorHandler.getBlockColor(chunkMD, this, globalX, y, globalZ);
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
    public Block getBlock()
    {
        return block;
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
        return block instanceof BlockAir || hasFlag(Flag.HasAir);
    }

    /**
     * Is ice.
     *
     * @return the boolean
     */
    public boolean isIce()
    {
        return block == Blocks.ICE;
    }

    /**
     * Is torch.
     *
     * @return the boolean
     */
    public boolean isTorch()
    {
        return block == Blocks.TORCH || block == Blocks.REDSTONE_TORCH || block == Blocks.UNLIT_REDSTONE_TORCH;
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
        return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
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
     * Gets meta
     *
     * @return
     */
    public int getMeta()
    {
        return meta;
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
     * Has an override meta to use
     *
     * @return
     */
    public boolean hasOverrideMeta()
    {
        return overrideMeta != null;
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
     * Returns the override meta to use when deriving color, or null if no override specified.
     *
     * @return
     */
    public Integer getOverrideMeta()
    {
        return overrideMeta;
    }

    /**
     * Sets override meta.
     *
     * @param overrideMeta
     */
    public void setOverrideMeta(Integer overrideMeta)
    {
        this.overrideMeta = overrideMeta;
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

        BlockMD blockMD = (BlockMD) o;

        if (meta != blockMD.meta)
        {
            return false;
        }
        if (!uid.equals(blockMD.uid))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = uid.hashCode();
        result = 31 * result + meta;
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("BlockMD [%s:%s] (%s)", uid, meta, Joiner.on(",").join(flags));
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
