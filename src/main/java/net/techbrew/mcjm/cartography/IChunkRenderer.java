package net.techbrew.mcjm.cartography;

import net.techbrew.mcjm.model.ChunkMD;

import java.awt.*;

public interface IChunkRenderer {
	
	public boolean render(final Graphics2D g2D, final ChunkMD chunkStub, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors);

}
