package net.techbrew.journeymap.cartography;

import net.minecraft.block.Block;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;

public interface IChunkRenderer {
	
	public boolean render(final Graphics2D g2D, final ChunkMD chunkStub, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors);


    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting);

    public float[] getFogColor();

}
