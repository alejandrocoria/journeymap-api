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
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Delegates the handling of some mods' blocks to a special handler.  The handling may or may not be related
 * to how the blocks are colored on the map.  For example, a certain block might trigger creation of a waypoint.
 */
public class ModBlockDelegate
{
    private static final int ERROR_LIMIT = 25;
    private static Logger logger = Journeymap.getLogger();
    private final HashMap<GameRegistry.UniqueIdentifier, IModBlockHandler> Blocks = new HashMap<GameRegistry.UniqueIdentifier, IModBlockHandler>();
    private final HashMap<GameRegistry.UniqueIdentifier, AtomicInteger> Errors = new HashMap<GameRegistry.UniqueIdentifier, AtomicInteger>();
    private final List<IModBlockHandler> handlers;

    /**
     * Register special block handlers when this class is initialized.
     */
    public ModBlockDelegate()
    {
        handlers = Arrays.asList(
                new Vanilla.CommonBlockHandler(),
                new CarpentersBlocks.CommonHandler(),
                new TerraFirmaCraft.TfcBlockHandler(),
                new Miscellaneous.CommonHandler());
    }

    /**
     * Call handlers to initialize their blocks' flags with the cache.
     */
    public void initialize(BlockMDCache blockMDCache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
    {
        for (IModBlockHandler handler : handlers)
        {
            initialize(handler, blockMDCache, registeredBlockIds);
        }
    }

    /**
     * Initialize a IModBlockHandler and register blocks to be handled by it.
     */
    private void initialize(IModBlockHandler handler, BlockMDCache blockMDCache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
    {
        List<GameRegistry.UniqueIdentifier> specialHandlingIds;
        List<GameRegistry.UniqueIdentifier> success = new ArrayList<GameRegistry.UniqueIdentifier>();
        List<GameRegistry.UniqueIdentifier> failure = new ArrayList<GameRegistry.UniqueIdentifier>();

        try
        {
            specialHandlingIds = handler.initialize(blockMDCache, registeredBlockIds);
        }
        catch (Throwable t)
        {
            String message = String.format("Couldn't initialize IModBlockHandler '%s': %s",
                    handler.getClass(),
                    LogFormatter.toString(t));
            logger.error(message);
            return;
        }

        if(specialHandlingIds!=null)
        {
            for (GameRegistry.UniqueIdentifier uid : specialHandlingIds)
            {
                if (uid != null)
                {
                    if (register(handler, uid))
                    {
                        success.add(uid);
                    }
                    else
                    {
                       failure.add(uid);
                    }
                }
            }

            // Show results
            if (success.size() > 0)
            {
                logger.info(String.format("Successfully registered IModBlockHandler %s for: '%s'.",
                        handler.getClass().getName(),
                        Joiner.on(", ").join(success)));
            }

            if (failure.size() > 0)
            {
                logger.error(String.format("Failed to register IModBlockHandler %s for: '%s'.",
                        handler.getClass().getName(),
                        Joiner.on(", ").join(failure)));
            }
        }
    }

    /**
     * Register a special block handler and a block UID to be handled by it.
     */
    private boolean register(IModBlockHandler handler, GameRegistry.UniqueIdentifier uid)
    {
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
     */
    public boolean canHandle(BlockMD blockMD)
    {
        return Blocks.containsKey(blockMD.uid);
    }

    /**
     * Provide special handling of a block in-situ when encountered during a mapping task.
     * The block returned will be used to color that spot on the map.
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

    /**
     * Interface for a class that initializes block flags for a specific mod, and/or
     * does special block handling during mapping.
     */
    public interface IModBlockHandler
    {
        /**
         * Provide Block UIDs that will be registered with as needing a special handler.
         * Optionally return a list of blockIDs that should be registered for special handling
         */
        List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache cache, List<GameRegistry.UniqueIdentifier> registeredBlockIds);

        /**
         * Provide special handling of a block in-situ when encountered during a mapping task.
         * The block returned will be used to color that spot on the map.
         */
        BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ);
    }

    /**
     * Interface for a class that determines how block colors are derived during mapping.
     */
    public interface IModBlockColorHandler
    {
        public Integer getCustomBiomeColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z);

        public Integer getFoliageColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z);

        public Integer getGrassColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z);

        public Integer getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z);

        public Integer getTint(BlockMD blockMD, int x, int y, int z);
    }
}
