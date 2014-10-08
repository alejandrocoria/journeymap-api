/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;

import static net.techbrew.journeymap.model.BlockMD.Flag.*;

/**
 * Created by Mark on 7/14/2014.
 */
public class BlockMDCache extends CacheLoader<Block, HashMap<Integer, BlockMD>>
{
    public final BlockMD AIRBLOCK;
    public final BlockMD VOIDBLOCK;
    private final HashMap<Block, EnumSet<BlockMD.Flag>> blockFlags;
    private final HashMap<Block, Float> blockAlphas;

    public BlockMDCache()
    {
        GameRegistry.UniqueIdentifier airUid = new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(Blocks.air));
        AIRBLOCK = new BlockMD("Air", airUid, Blocks.air, 0, 0f, EnumSet.of(BlockMD.Flag.HasAir));

        GameRegistry.UniqueIdentifier voidUid = new GameRegistry.UniqueIdentifier("journeymap:void");
        VOIDBLOCK = new BlockMD("Void", voidUid, null, 0, 1f, null);

        blockFlags = new HashMap<Block, EnumSet<BlockMD.Flag>>(64);
        blockAlphas = new HashMap<Block, Float>(8);
    }

    /**
     * Constructor
     */
    public void initialize()
    {
        StatTimer timer = StatTimer.get("BlockMDCache.ensureCurrent").start();

        blockAlphas.clear();
        blockFlags.clear();

        // Set alphas
        setAlpha(Blocks.air, 0F);
        setAlpha(Blocks.fence, .4F);
        setAlpha(Blocks.fence_gate, .4F);
        setAlpha(Blocks.flowing_water, .3F);
        setAlpha(Blocks.glass, .3F);
        setAlpha(Blocks.glass_pane, .3F);
        setAlpha(Blocks.ice, .8F);
        setAlpha(Blocks.iron_bars, .4F);
        setAlpha(Blocks.nether_brick_fence, .4F);
        setAlpha(Blocks.stained_glass, .5F);
        setAlpha(Blocks.stained_glass_pane, .5F);
        setAlpha(Blocks.torch, .5F);
        setAlpha(Blocks.vine, .2F);
        setAlpha(Blocks.water, .3F);

        // Set optional flags
        if (JourneyMap.getCoreProperties().caveIgnoreGlass.get())
        {
            setFlags(Blocks.glass, OpenToSky);
            setFlags(Blocks.glass_pane, OpenToSky);
            setFlags(Blocks.stained_glass, OpenToSky);
            setFlags(Blocks.stained_glass, OpenToSky);
        }

        // Set manual flags
        setFlags(Blocks.air, HasAir, OpenToSky, NoShadow, OpenToSky);
        setFlags(Blocks.double_plant, BiomeColor);
        setFlags(Blocks.fence, TransparentRoof);
        setFlags(Blocks.fire, NoShadow, Side2Texture);
        setFlags(Blocks.flowing_water, BiomeColor);
        setFlags(Blocks.glass, TransparentRoof);
        setFlags(Blocks.glass_pane, TransparentRoof);
        setFlags(Blocks.grass, BiomeColor);
        setFlags(Blocks.iron_bars, TransparentRoof);
        setFlags(Blocks.ladder, OpenToSky);
        setFlags(Blocks.lava, NoShadow);
        setFlags(Blocks.leaves, OpenToSky, BiomeColor);
        setFlags(Blocks.leaves2, OpenToSky, BiomeColor);
        setFlags(Blocks.redstone_torch, HasAir);
        setFlags(Blocks.stained_glass, TransparentRoof, Transparency);
        setFlags(Blocks.stained_glass_pane, TransparentRoof, Transparency);
        setFlags(Blocks.tallgrass, BiomeColor);
        setFlags(Blocks.torch, HasAir, NoShadow);
        setFlags(Blocks.tripwire, NoShadow);
        setFlags(Blocks.tripwire_hook, NoShadow);
        setFlags(Blocks.unlit_redstone_torch, HasAir, NoShadow);
        setFlags(Blocks.vine, OpenToSky, NoShadow, BiomeColor);
        setFlags(Blocks.water, NoShadow, BiomeColor);
        setFlags(Blocks.web, OpenToSky, Side2Texture);

        // Set flags based on inheritance
        for (Block block : GameData.getBlockRegistry().typeSafeIterable())
        {
            //blockUids.put(block, new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(block)));

            if (block.getMaterial() == Material.air)
            {
                setFlags(block, HasAir, OpenToSky, NoShadow);
                continue;
            }

            if (block instanceof BlockLeavesBase || block instanceof BlockGrass || block instanceof BlockVine || block instanceof BlockLilyPad)
            {
                setFlags(block, BiomeColor);
            }

            if (block instanceof BlockCrops)
            {
                setFlags(block, Side2Texture, Crop);
                if (!JourneyMap.getCoreProperties().mapPlantShadows.get())
                {
                    setFlags(block, NoShadow);
                }
            }
            else if (block instanceof BlockBush)
            {
                setFlags(block, Side2Texture, Plant);
                if (!JourneyMap.getCoreProperties().mapPlantShadows.get())
                {
                    setFlags(block, NoShadow);
                }
            }
            else if (block instanceof BlockCactus)
            {
                setFlags(block, Side2Texture, Plant);
                if (!JourneyMap.getCoreProperties().mapPlantShadows.get())
                {
                    setFlags(block, NoShadow);
                }
            }

            if (block instanceof BlockRailBase)
            {
                setFlags(block, NoShadow);
            }
        }

        timer.stop();
    }

    @Override
    public HashMap<Integer, BlockMD> load(Block key) throws Exception
    {
        return new HashMap<Integer, BlockMD>(16);
    }


    public GameRegistry.UniqueIdentifier findUniqueIdentifierFor(Block block)
    {
//        GameRegistry.UniqueIdentifier uid = blockUids.get(block);
//        if (uid == null)
//        {
//            uid = new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(block));
//            blockUids.put(block, uid);
//        }
//        return uid;
        return new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(block));
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     */
    public BlockMD getBlockMD(LoadingCache<Block, HashMap<Integer, BlockMD>> cache, ChunkMD chunkMd, int x, int y, int z)
    {
        try
        {
            if (y >= 0)
            {
                Block block = chunkMd.getBlock(x, y, z);

                if (block instanceof BlockAir)
                {
                    return AIRBLOCK;
                }
                else
                {
                    int meta = chunkMd.getChunk().getBlockMetadata(x, y, z);
                    return getBlockMD(cache, block, meta);
                }
            }
            else
            {
                return VOIDBLOCK;
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().error(String.format("Can't get blockId/meta for chunk %s,%s block %s,%s,%s : %s", chunkMd.getChunk().xPosition, chunkMd.getChunk().zPosition, x, y, z, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     */
    public BlockMD getBlockMD(LoadingCache<Block, HashMap<Integer, BlockMD>> cache, Block block, int meta)
    {
        try
        {
            if (block instanceof BlockAir)
            {
                return AIRBLOCK;
            }
            else
            {
                HashMap<Integer, BlockMD> map = cache.get(block);
                BlockMD blockMD = map.get(meta);

                synchronized (this)
                {
                    if (blockMD == null)
                    {
                        String displayName = BlockMD.getBlockName(block, meta);
                        blockMD = new BlockMD(displayName, block, meta, getAlpha(block), getFlags(block));
                        map.put(meta, blockMD);
                    }
                }

                return blockMD;
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().error(String.format("Can't get blockId/meta for block %s meta %s : %s", block, meta, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    public EnumSet<BlockMD.Flag> getFlags(Block block)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(block);
        return flags == null ? EnumSet.noneOf(BlockMD.Flag.class) : flags;
    }

    public void setFlags(Block block, BlockMD.Flag... flags)
    {
        EnumSet<BlockMD.Flag> eset = getFlags(block);
        eset.addAll(Arrays.asList(flags));
        blockFlags.put(block, eset);
        JourneyMap.getLogger().debug(block.getUnlocalizedName() + " flags set: " + eset);
    }

    public boolean hasFlag(Block block, BlockMD.Flag flag)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(block);
        return flags != null && flags.contains(flag);
    }

    public boolean hasAnyFlags(Block block, BlockMD.Flag... flags)
    {
        EnumSet<BlockMD.Flag> flagSet = blockFlags.get(block);
        if (flagSet == null)
        {
            return false;
        }
        for (BlockMD.Flag flag : flags)
        {
            if (flagSet.contains(flag))
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(block);
    }

    public float getAlpha(Block block)
    {
        Float alpha = blockAlphas.get(block);
        return alpha == null ? 1F : alpha;
    }

    public void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(block, alpha);
    }

    public HashMap getFlagsMap()
    {
        return blockFlags;
    }

    public HashMap getAlphaMap()
    {
        return blockAlphas;
    }
}