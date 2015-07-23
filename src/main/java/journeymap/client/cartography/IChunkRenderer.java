/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import journeymap.client.model.ChunkMD;

import java.awt.*;

public interface IChunkRenderer
{
    public boolean render(final Graphics2D g2D, final ChunkMD chunkStub, final Integer vSlice);

    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting);

    public float[] getAmbientColor();

}