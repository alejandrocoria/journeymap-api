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
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static journeymap.client.model.BlockMD.Flag.SpecialHandling;

// 1.8
//import net.minecraftforge.fml.common.registry.GameData;
//import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Block MetaData Cache
 */
public class BlockMDCache extends CacheLoader<Block, HashMap<Integer, BlockMD>>
{
    public final BlockMD AIRBLOCK;
    public final BlockMD VOIDBLOCK;
    private ModBlockDelegate modBlockDelegate;

    public BlockMDCache()
    {
        GameRegistry.UniqueIdentifier airUid = new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(Blocks.air));
        AIRBLOCK = new BlockMD("Air", airUid, Blocks.air, 0, 0f, EnumSet.of(BlockMD.Flag.HasAir));

        GameRegistry.UniqueIdentifier voidUid = new GameRegistry.UniqueIdentifier("journeymap:void");
        VOIDBLOCK = new BlockMD("Void", voidUid, null, 0, 1f, null);
    }

    /**
     * Constructor
     */
    public void initialize()
    {
        StatTimer timer = StatTimer.get("BlockMDCache.initialize", 0, 2000).start();
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
                        blockMD = new BlockMD(displayName, block, meta, 1f);
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

    /**
     * Set flags by block UID.
     * @param blockid
     * @param flags
     */
    public void setFlags(GameRegistry.UniqueIdentifier blockid, BlockMD.Flag... flags)
    {
        Block block = (Block) GameData.getBlockRegistry().getObject(blockid);
        if(block!=null)
        {
            setFlags(block, flags);
        }
        else
        {
            Journeymap.getLogger().error(String.format("Can't find block with id %s; unable to set flags", blockid));
        }
    }

    public void setFlags(Block block, BlockMD.Flag... flags)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.addFlags(flags);
        }
        Journeymap.getLogger().debug(block.getUnlocalizedName() + " flags set: " + flags);
    }

    public void setAlpha(Block block, Float alpha)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.setAlpha(alpha);
        }
        Journeymap.getLogger().debug(block.getUnlocalizedName() + " alpha set: " + alpha);
    }

    public void setTextureSide(GameRegistry.UniqueIdentifier blockid, int side)
    {
        Block block = (Block) GameData.getBlockRegistry().getObject(blockid);
        if(block!=null)
        {
            setTextureSide(block, side);
        }
        else
        {
            Journeymap.getLogger().error(String.format("Can't find block with id %s; unable to set texture side", blockid));
        }
    }

    public void setTextureSide(Block block, int side)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.setTextureSide(side);
        }
    }

    public void setOverrideMeta(Block block, int overrideMeta)
    {
        for (BlockMD blockMD : BlockMD.getAllBlockMDs(block))
        {
            blockMD.setOverrideMeta(overrideMeta);
        }
    }


    public void setAlpha(GameRegistry.UniqueIdentifier blockid, Float alpha)
    {
        Block block = (Block) GameData.getBlockRegistry().getObject(blockid);
        if(block!=null)
        {
            setAlpha(block, alpha);
        }
        else
        {
            Journeymap.getLogger().error(String.format("Can't find block with id %s; unable to set alpha", blockid));
        }
    }

    public ModBlockDelegate getModBlockDelegate()
    {
        return modBlockDelegate;
    }

    public void preloadBlock(Block block, BlockMD.Flag... flags)
    {
        preloadBlock(block, null, null, flags);
    }

    public void preloadBlock(Block block, Integer color, Float alpha, BlockMD.Flag... flags)
    {
        for (int meta : BlockMD.getMetaValuesForBlock(block))
        {
            BlockMD blockMD = DataCache.instance().getBlockMD(block, meta);
            if (flags.length > 0)
            {
                blockMD.addFlags(flags);
            }
            if (alpha != null)
            {
                blockMD.setAlpha(alpha);
            }
            if (color == null)
            {
                color = ForgeHelper.INSTANCE.getColorHelper().loadBlockColor(blockMD);
            }
            if (color != null)
            {
                Journeymap.getLogger().info("Preloaded " + blockMD + " color: " + RGB.toString(color));
            }
            else
            {
                Journeymap.getLogger().info("Couldn't preload " + blockMD + " color.");
            }
        }
    }

}