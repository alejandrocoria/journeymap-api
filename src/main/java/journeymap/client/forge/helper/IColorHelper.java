/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import journeymap.client.model.BlockMD;
import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * Interface used to encapsulate compile-time differences between Minecraft/Forge versions
 * with respect to deriving block colors.
 */
public interface IColorHelper
{
    boolean failedFor(BlockMD blockMD);

    Integer loadBlockColor(BlockMD blockMD);

    int getColorMultiplier(World world, Block block, int x, int y, int z);

    @Deprecated
    public int getRenderColor(BlockMD blockMD);

    public int getMapColor(BlockMD blockMD);
}
