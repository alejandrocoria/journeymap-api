package net.techbrew.mcjm.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.thread.JMThreadFactory;

public class RegionImageCache  {
	
	private static final int SIZE = 16;
	private static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
	private static volatile RegionImageCache instance;
	private volatile Map<RegionCoord, RegionImageSet> imageSets;
	private volatile long lastFlush;
	//private volatile Set<RegionCoord> dirty;
	private volatile Object lock = new Object();
	
	public synchronized static RegionImageCache getInstance() {
		if(instance==null) {
			instance = new RegionImageCache();
		}
		return instance;
	}
	
	private RegionImageCache() {
		imageSets = Collections.synchronizedMap(new CacheMap(SIZE));
		
		//dirty = Collections.synchronizedSet(new HashSet<RegionCoord>(SIZE));
		lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
		
		// Init thread factory
		JMThreadFactory tf = new JMThreadFactory("rcache");
		
		// Add shutdown hook to flush cache to disk
		Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable() {
			@Override
			public void run() {				
				flushToDisk();
				if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
					JourneyMap.getLogger().fine("RegionImageCache flushing to disk on shutdown"); //$NON-NLS-1$
				}
			}
		}));
	}
	
	private RegionImageSet getRegionImageSet(RegionCoord rCoord) {
		synchronized(lock) {
			RegionImageSet ris = imageSets.get(rCoord);
			if(ris==null) {
				ris = new RegionImageSet(rCoord);
				imageSets.put(rCoord, ris);			
			}
			return ris;
		}
	}
	
	public boolean contains(RegionCoord rCoord) {
		synchronized(lock) {
			return imageSets.containsKey(rCoord);
		}
	}
	
	public List<RegionCoord> getRegions() {
		return new ArrayList<RegionCoord>(imageSets.keySet());
	}
	
	public BufferedImage getGuaranteedImage(RegionCoord rCoord, Constants.MapType mapType) {
		RegionImageSet ris = getRegionImageSet(rCoord);
		return ris.getImage(mapType);
	}
	
	public void putAll(final Set<Map.Entry<ChunkCoord, BufferedImage>> chunkImageEntries, boolean forceFlush) {
		final RegionImageHandler rfh = RegionImageHandler.getInstance();
		synchronized(lock) {
			for(Map.Entry<ChunkCoord, BufferedImage> entry : chunkImageEntries) {
				final ChunkCoord cCoord = entry.getKey();
				final RegionCoord rCoord = cCoord.getRegionCoord();
				final BufferedImage chunkImage = entry.getValue();
				final RegionImageSet ris = getRegionImageSet(rCoord);
				if(ris.hasLegacy()) ris.writeToDisk(true);
				ris.insertChunk(cCoord, chunkImage);
			}
			if(forceFlush) {
				flushToDisk();
			}
		}
		if(!forceFlush) autoFlush();
	}
	
	private void autoFlush() {
		synchronized(lock) {
			if(lastFlush+flushInterval<System.currentTimeMillis()) {
				if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
					JourneyMap.getLogger().fine("RegionImageCache auto-flushing"); //$NON-NLS-1$
				}
				flushToDisk();
			}
		}
	}
	
	public void flushToDisk() {
		RegionImageHandler rfh = RegionImageHandler.getInstance();
		synchronized(lock) {		
			for(RegionImageSet ris : imageSets.values()) {
				ris.writeToDisk(false);
			}
			lastFlush = System.currentTimeMillis();
		}		
	}
	
	public void clear() {
		synchronized(lock) {
			imageSets.clear();
		}
	}
	
	/**
	 * LinkedHashMap serves as a LRU cache that can write a RIS to disk
	 * when it's removed.
	 * 
	 * @author mwoodman
	 *
	 */
	class CacheMap extends LinkedHashMap<RegionCoord, RegionImageSet> {
		
		private final int capacity;
		
		CacheMap(int capacity) {
			super(capacity + 1, 1.1f, true);
		    this.capacity = capacity;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<RegionCoord, RegionImageSet> entry)
	    {
			Boolean remove = size() > capacity;
			if(remove) {
				if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
					JourneyMap.getLogger().fine("RegionImageCache purging " + entry.getKey()); //$NON-NLS-1$
				}				
				entry.getValue().writeToDisk(false);
			}
			if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine("RegionImageCache size: " + (this.size()-1)); //$NON-NLS-1$
			}
			return remove;
	    }
	}
	
}
