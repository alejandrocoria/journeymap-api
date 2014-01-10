package net.techbrew.mcjm.model;

import net.techbrew.mcjm.Constants.MapType;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ChunkImageCache extends HashMap<ChunkCoord, ChunkImageSet> {
	
	public ChunkImageCache() {
		super(768);
	}
	
	public void put(ChunkCoord cCoord, MapType mapType, BufferedImage chunkImage) {
		ChunkImageSet cis = get(cCoord);
		if(cis==null) {
			cis = new ChunkImageSet(cCoord);
			put(cCoord, cis);
		}
		cis.getWrapper(mapType).setImage(chunkImage);
	}
}
