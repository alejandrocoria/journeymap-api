/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography;

import journeymap.client.model.ChunkMD;
import journeymap.client.render.ComparableBufferedImage;

/**
 * The interface Chunk renderer.
 */
public interface IChunkRenderer
{
    /**
     * Render boolean.
     *
     * @param chunkImage the chunk image
     * @param chunkStub  the chunk stub
     * @param vSlice     the v slice
     * @return the boolean
     */
    public boolean render(final ComparableBufferedImage chunkImage, final ChunkMD chunkStub, final Integer vSlice);

    /**
     * Sets stratum colors.
     *
     * @param stratum          the stratum
     * @param lightAttenuation the light attenuation
     * @param waterColor       the water color
     * @param waterAbove       the water above
     * @param underground      the underground
     * @param mapCaveLighting  the map cave lighting
     */
    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting);

    /**
     * Get ambient color float [ ].
     *
     * @return the float [ ]
     */
    public float[] getAmbientColor();

}
