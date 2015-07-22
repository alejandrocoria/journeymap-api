/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import com.google.common.base.Joiner;
import journeymap.client.log.LogFormatter;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Delegates the handling of some mods' blocks to a special handler.  The handling may or may not be related
 * to how the blocks are colored on the map.  For example, a certain block might trigger creation of a waypoint.
 */
public class ModBlockDelegate
{
    // TODO: Move special block handling (torches, biomes) in BlockRegistry here

    //public static final GameRegistry.UniqueIdentifier MobSpawner = new GameRegistry.UniqueIdentifier("minecraft:mob_spawner");

    public static final HashMap<GameRegistry.UniqueIdentifier, IModBlockHandler> Blocks = new HashMap<GameRegistry.UniqueIdentifier, IModBlockHandler>();

    private static final HashMap<GameRegistry.UniqueIdentifier, AtomicInteger> Errors = new HashMap<GameRegistry.UniqueIdentifier, AtomicInteger>();
    private static final int ERROR_LIMIT = 25;
    private static Logger logger = Journeymap.getLogger();

    /**
     * Register special block handlers when this class is first loaded.
     */
    static
    {
        // register(new OpenBlocks.GraveHandler());
        register(new CarpentersBlocks.CommonHandler());
    }

    /**
     * Register a special block handler.
     *
     * @param handler
     */
    private static void register(IModBlockHandler handler)
    {
        try
        {
            List<GameRegistry.UniqueIdentifier> success = new ArrayList<GameRegistry.UniqueIdentifier>();
            for (GameRegistry.UniqueIdentifier uid : handler.getBlockUids())
            {
                if (uid != null)
                {
                    if (register(handler, uid))
                    {
                        success.add(uid);
                    }
                }
            }
            if (success.size() > 0)
            {
                logger.info(String.format("Registered IModBlockHandler for: '%s'.", Joiner.on(", ").join(success)));
            }
        }
        catch (Throwable t)
        {
            String message = String.format("Couldn't register IModBlockHandler '%s': %s",
                    Joiner.on(", ").join(handler.getBlockUids()),
                    LogFormatter.toString(t));
            logger.error(message);
        }
    }

    /**
     * Register a special block handler and a block UID to be handled by it.
     *
     * @param handler
     * @param uid
     * @return
     */
    private static boolean register(IModBlockHandler handler, GameRegistry.UniqueIdentifier uid)
    {
        if (uid == null)
        {
            throw new IllegalStateException("UID cannot be null");
        }
        if (Blocks.containsKey(uid))
        {
            throw new IllegalStateException("UID already registered to " + Blocks.get(uid));
        }
        Blocks.put(uid, handler);
        Errors.put(uid, new AtomicInteger(0));
        return true;
    }

    /**
     * Whether a block can get special handling.
     *
     * @param blockMD
     * @return
     */
    public boolean canHandle(BlockMD blockMD)
    {
        return Blocks.containsKey(blockMD.uid);
    }

//    /**
//     * Provide special handling of getting a block icon.  Return nulls if not necessary,
//     *
//     * @param blockMD
//     * @return
//     */
//    public IIcon getIcon(BlockMD blockMD)
//    {
//        try
//        {
//            IModBlockHandler handler = Blocks.get(blockMD.uid);
//            if (handler != null)
//            {
//                return handler.getIcon(blockMD);
//            }
//        }
//        catch (Throwable t)
//        {
//            int count = Errors.get(blockMD.uid).incrementAndGet();
//            String message = String.format("Error (%s) from getIcon() on block '%s': %s", count, blockMD.uid, LogFormatter.toString(t));
//            logger.error(message);
//            if (count >= ERROR_LIMIT)
//            {
//                logger.warn(String.format("Deregistering problematic IModBlockHandler for '%s'.", blockMD.uid));
//                Blocks.remove(blockMD.uid);
//            }
//        }
//
//        return null;
//    }

    /**
     * Provide special handling of a block in-situ when encountered during a mapping task.
     * The block returned will be used to color that spot on the map.
     *
     * @param chunkMD
     * @param blockMD
     * @param localX
     * @param y
     * @param localZ
     * @return
     */
    public BlockMD handleBlock(ChunkMD chunkMD, final BlockMD blockMD, int localX, int y, int localZ)
    {
        BlockMD delegatedBlockMD = null;
        try
        {
            IModBlockHandler handler = Blocks.get(blockMD.uid);
            if (handler != null)
            {
                delegatedBlockMD = handler.handleBlock(chunkMD, blockMD, localX, y, localZ);
            }
        }
        catch (Throwable t)
        {
            int count = Errors.get(blockMD.uid).incrementAndGet();
            String message = String.format("Error (%s) handling block '%s': %s", count, blockMD.uid, LogFormatter.toString(t));
            logger.error(message);
            if (count >= ERROR_LIMIT)
            {
                logger.warn(String.format("Deregistering problematic IModBlockHandler for '%s'.", blockMD.uid));
                Blocks.remove(blockMD.uid);
            }
        }
        if (delegatedBlockMD == null)
        {
            delegatedBlockMD = blockMD;
        }
        return delegatedBlockMD;
    }

    public static interface IModBlockHandler
    {
        /**
         * Provide Block UIDs that will be registered with as needing a special handler.
         *
         * @return
         */
        public Collection<GameRegistry.UniqueIdentifier> getBlockUids();

        /**
         * Provide special handling of getting a block icon.  Return null if not necessary,
         *
         * @param blockMD
         * @return
         */
//        public IIcon getIcon(BlockMD blockMD);

        /**
         * Provide special handling of a block in-situ when encountered during a mapping task.
         * The block returned will be used to color that spot on the map.
         *
         * @param chunkMD
         * @param blockMD
         * @param localX
         * @param y
         * @param localZ
         * @return
         */
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ);
    }
}
