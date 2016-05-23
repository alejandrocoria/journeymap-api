/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.BlockPos;

/**
 * Interface used to encapsulate compile-time differences between Minecraft/Forge versions
 * with respect to deriving block colors.
 */
public interface IColorHelper
{
    boolean clearBlocksTexture();

    boolean hasBlocksTexture();

    boolean initBlocksTexture();

    boolean failedFor(BlockMD blockMD);

    Integer getTextureColor(BlockMD blockMD);

    int getColorMultiplier(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos);

    int getRenderColor(BlockMD blockMD);

    int getMapColor(BlockMD blockMD);
}
