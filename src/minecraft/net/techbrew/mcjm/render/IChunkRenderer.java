package net.techbrew.mcjm.render;

import java.awt.Graphics2D;

import net.techbrew.mcjm.model.ChunkMD;

public interface IChunkRenderer {
	
	public boolean render(final Graphics2D g2D, final ChunkMD chunkStub, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors);

}