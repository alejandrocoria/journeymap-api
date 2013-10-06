package net.techbrew.mcjm.model;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.techbrew.mcjm.JourneyMap;

public class ChunkImageCache {
	
	private static ChunkImageCache instance;
	private final CacheMap imageMap;

	public synchronized static ChunkImageCache getInstance() {
		if(instance==null) {
			instance = new ChunkImageCache();
		}
		return instance;
	}
	
	public ChunkImageCache() {
		imageMap = new CacheMap(1024);
	}
	
	public void put(ChunkCoord cCoord, BufferedImage chunkImage) {
		synchronized(imageMap) {
			imageMap.put(cCoord, chunkImage);
		}
	}
	
	public BufferedImage get(ChunkCoord cCoord) {
		BufferedImage chunkImage = null;
		synchronized(imageMap) {
			chunkImage = imageMap.get(cCoord);
		}
		return chunkImage;
	}
	
	public void clear() {
		synchronized(imageMap) {
			imageMap.clear();
		}
	}
	
	public void setLRU(boolean purge) {
		synchronized(imageMap) {
			imageMap.purge = purge;
		}
	}
	
	public boolean isEmpty() {
		synchronized(imageMap) {
			return imageMap.isEmpty();
		}
	}
	
	public Set<Map.Entry<ChunkCoord, BufferedImage>> getEntries() {
		return new HashSet<Map.Entry<ChunkCoord, BufferedImage>>(imageMap.entrySet());
	}
	
	class CacheMap extends LinkedHashMap<ChunkCoord, BufferedImage> {
		
		private final int capacity;
		private boolean purge = false;
		
		CacheMap(int capacity) {
			super(capacity + 1, 1.1f, true);
		    this.capacity = capacity;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry entry)
	    {
			if(!purge) return false;
			Boolean remove = size() > capacity;
			if(remove && JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine("ChunkImageCache purging " + entry.getKey()); //$NON-NLS-1$
			}
			return remove;
	    }
	}
}
