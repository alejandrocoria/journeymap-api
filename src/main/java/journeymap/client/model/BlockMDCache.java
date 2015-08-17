/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;

import static journeymap.client.model.BlockMD.Flag.SpecialHandling;

/**
 * Block MetaData Cache
 */
public class BlockMDCache extends CacheLoader<Block, HashMap<Integer, BlockMD>>
{
    public final BlockMD AIRBLOCK;
    public final BlockMD VOIDBLOCK;
    private final HashMap<Block, EnumSet<BlockMD.Flag>> blockFlags;
    private final HashMap<Block, Float> blockAlphas;
    private ModBlockDelegate modBlockDelegate;

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
        StatTimer timer = StatTimer.get("BlockMDCache.initialize", 0, 2000).start();

        blockAlphas.clear();
        blockFlags.clear();
        modBlockDelegate = new ModBlockDelegate();

        // Get list of all registered block ids
        List<GameRegistry.UniqueIdentifier> registeredBlockIds = new ArrayList<GameRegistry.UniqueIdentifier>(256);
        for (Block block : GameData.getBlockRegistry().typeSafeIterable())
        {
            registeredBlockIds.add(findUniqueIdentifierFor(block));
        }

        // Delegate initialization of block flags, alphas, etc.
        modBlockDelegate.initialize(this, registeredBlockIds);

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
                    int meta = chunkMd.getBlockMeta(x, y, z);
                    return getBlockMD(cache, chunkMd, block, meta, x, y, z);
                }
            }
            else
            {
                return VOIDBLOCK;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for chunk %s,%s block %s,%s,%s : %s", chunkMd.getChunk().xPosition, chunkMd.getChunk().zPosition, x, y, z, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     * Handles blocks with SpecialBlockHandler as needed.
     */
    public BlockMD getBlockMD(LoadingCache<Block, HashMap<Integer, BlockMD>> cache, ChunkMD chunkMD, Block block, int meta, int x, int y, int z)
    {
        BlockMD blockMD = getBlockMD(cache, block, meta);
        if (blockMD.hasFlag(SpecialHandling))
        {
            blockMD = modBlockDelegate.handleBlock(chunkMD, blockMD, x, y, z);
        }
        return blockMD;
    }

    /**
     * Produces a BlockMD instance from Block and Meta
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
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for block %s meta %s : %s", block, meta, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    public EnumSet<BlockMD.Flag> getFlags(Block block)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(block);
        return flags == null ? EnumSet.noneOf(BlockMD.Flag.class) : flags;
    }

    /**
     * Set flags by block UID.
     * @param blockid
     * @param flags
     */
    public void setFlags(GameRegistry.UniqueIdentifier blockid, BlockMD.Flag... flags)
    {
        Block block = GameData.getBlockRegistry().getObject(blockid);
        if(block!=null)
        {
            setFlags(block, Arrays.asList(flags));
        }
        else
        {
            Journeymap.getLogger().error(String.format("Can't find block with id %s; unable to set flags", blockid));
        }
    }

    public void setFlags(Block block, BlockMD.Flag... flags)
    {
        setFlags(block, Arrays.asList(flags));
    }

    public void setFlags(Block block, Collection<BlockMD.Flag> flags)
    {
        EnumSet<BlockMD.Flag> eset = getFlags(block);
        eset.addAll(flags);
        blockFlags.put(block, eset);
        Journeymap.getLogger().debug(block.getUnlocalizedName() + " flags set: " + eset);
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

    public void removeFlag(Block block, BlockMD.Flag flag)
    {
        EnumSet<BlockMD.Flag> flagSet = blockFlags.get(block);
        if (flagSet != null)
        {
            flagSet.remove(flag);
        }
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
        // safety check on value
        alpha = Math.min(Math.max(0f, alpha), 1f);
        blockAlphas.put(block, alpha);
    }

    public void setAlpha(GameRegistry.UniqueIdentifier blockid, Float alpha)
    {
        Block block = GameData.getBlockRegistry().getObject(blockid);
        if(block!=null)
        {
            setAlpha(block, alpha);
        }
        else
        {
            Journeymap.getLogger().error(String.format("Can't find block with id %s; unable to set alpha", blockid));
        }
    }

    public HashMap getFlagsMap()
    {
        return blockFlags;
    }

    public HashMap getAlphaMap()
    {
        return blockAlphas;
    }

    public ModBlockDelegate getModBlockDelegate()
    {
        return modBlockDelegate;
    }

}