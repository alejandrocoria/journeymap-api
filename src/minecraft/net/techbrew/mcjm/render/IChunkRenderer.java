package net.techbrew.mcjm.render;

import java.awt.Graphics2D;
import java.util.Map;

import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.model.ChunkStub;

public interface IChunkRenderer {
	
	public boolean render(final Graphics2D g2D, final ChunkStub chunkStub, final boolean underground, 
			final Integer vSlice, final Map<ChunkCoordIntPair, ChunkStub> neighbors);

}