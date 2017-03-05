/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography;

import journeymap.client.model.ChunkMD;
import journeymap.client.render.ComparableBufferedImage;

public interface IChunkRenderer
{
    public boolean render(final ComparableBufferedImage chunkImage, final ChunkMD chunkStub, final Integer vSlice);

    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting);

    public float[] getAmbientColor();

}
