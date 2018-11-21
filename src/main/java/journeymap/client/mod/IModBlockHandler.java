/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod;

import journeymap.client.model.BlockMD;

/**
 * Interface for a class that initializes block flags for a specific mod, and/or
 * does special block handling during mapping.
 */
public interface IModBlockHandler {
    /**
     * Initialize handling for a block
     *
     * @param blockMD the block md
     * @return the boolean
     */
    void initialize(BlockMD blockMD);

}
