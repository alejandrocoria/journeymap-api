package net.techbrew.mcjm.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import net.techbrew.mcjm.ChunkStub;

public interface IChunkRenderer {

	public abstract BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, int vSlice, Map<Integer, ChunkStub> neighbors);

}