package net.techbrew.mcjm.render;

import java.awt.Graphics2D;
import java.util.Map;

import net.techbrew.mcjm.model.ChunkStub;

public interface IChunkRenderer {
	
	public void render(final Graphics2D g2D, final ChunkStub chunkStub, final boolean underground, 
			final int vSlice, final Map<Integer, ChunkStub> neighbors);

}