/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.mod.vanilla.VanillaBlockHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Delegates the handling of some mods' blocks to a special handler.  The handling may or may not be related
 * to how the blocks are colored on the map.  For example, a certain block might trigger creation of a waypoint.
 */
public class ModBlockDelegate
{
    private static Logger logger = Journeymap.getLogger();
    private final List<IModBlockHandler> handlers;

    /**
     * Register special block handlers when this class is initialized.
     */
    public ModBlockDelegate()
    {
        handlers = Arrays.asList(
                new VanillaBlockHandler(),
                new BiomesOPlenty.BopBlockHandler(),
                new CarpentersBlocks.CommonHandler(),
                new TerraFirmaCraft.TfcBlockHandler(),
                new Miscellaneous.CommonHandler());
    }

    /**
     * Provide special handling of a block in-situ when encountered during a mapping task.
     * The block returned will be used to color that spot on the map.
     */
    public static BlockMD handleBlock(ChunkMD chunkMD, final BlockMD blockMD, BlockPos blockPos)
    {
        BlockMD delegatedBlockMD = null;
        try
        {
            IModBlockHandler handler = blockMD.getModBlockHandler();
            if (handler != null)
            {
                delegatedBlockMD = handler.handleBlock(chunkMD, blockMD, blockPos);
            }
            else
            {
                blockMD.setModBlockHandler(null);
            }
        }
        catch (Throwable t)
        {
            String message = String.format("Error handling block '%s': %s", blockMD, LogFormatter.toString(t));
            logger.error(message);
        }
        if (delegatedBlockMD == null)
        {
            delegatedBlockMD = blockMD;
        }
        return delegatedBlockMD;
    }

    /**
     * Call handlers to initialize their blocks' flags with the cache.
     */
    public void initialize(BlockMD blockMD)
    {
        for (IModBlockHandler handler : handlers)
        {
            initialize(handler, blockMD);
        }
    }

    /**
     * Initialize a IModBlockHandler and register blocks to be handled by it.
     */
    private void initialize(IModBlockHandler handler, BlockMD blockMD)
    {
        try
        {
            boolean specialHandling = handler.initialize(blockMD);
            if (specialHandling)
            {
                logger.info(String.format("Registered IModBlockHandler %s for: '%s'.",
                        handler.getClass().getName(),
                        blockMD));
            }
        }
        catch (Throwable t)
        {
            String message = String.format("Couldn't initialize IModBlockHandler '%s': %s",
                    handler.getClass(),
                    LogFormatter.toString(t));
            logger.error(message);
            return;
        }


    }

    /**
     * Interface for a class that initializes block flags for a specific mod, and/or
     * does special block handling during mapping.
     */
    public interface IModBlockHandler
    {
        /**
         * Provide Block UIDs that will be registered with as needing a special handler.
         * If return is true, it should be registered for handleBlock
         */
        boolean initialize(BlockMD blockMD);

        /**
         * Provide special handling of a block in-situ when encountered during a mapping task.
         * The block returned will be used to color that spot on the map.
         */
        BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos);
    }

    /**
     * Interface for a class that determines how block colors are derived during mapping.
     */
    public interface IModBlockColorHandler
    {
        public Integer getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos);

        public Integer getTextureColor(BlockMD blockMD);
    }
}
