/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import journeymap.client.model.ChunkMD;
import journeymap.client.render.MonitoredBufferedImage;

public interface IChunkRenderer
{
    public boolean render(final MonitoredBufferedImage chunkImage, final ChunkMD chunkStub, final Integer vSlice);

    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting);

    public float[] getAmbientColor();

}
