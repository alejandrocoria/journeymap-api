package net.techbrew.mcjm.io;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.ChunkImageCache.CacheMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.thread.JMThreadFactory;

public class RegionImageCache  {
	
	private static final int SIZE = 16;
	private static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
	private static volatile RegionImageCache instance;
	private volatile Map<RegionCoord, BufferedImage> imageMap;
	private volatile long lastFlush;
	private volatile Set<RegionCoord> dirty;
	private volatile Object lock = new Object();
	
	public synchronized static RegionImageCache getInstance() {
		if(instance==null) {
			instance = new RegionImageCache();
		}
		return instance;
	}
	
	private RegionImageCache() {
		imageMap = Collections.synchronizedMap(new CacheMap(SIZE));
		dirty = Collections.synchronizedSet(new HashSet<RegionCoord>(SIZE));
		lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
		
		// Init thread factory
		JMThreadFactory tf = new JMThreadFactory("RegionImageCache");
		
		// Add shutdown hook to flush cache to disk
		Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable() {
			public void run() {				
				flushToDisk();
				if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
					JourneyMap.getLogger().fine("RegionImageCache flushing to disk on shutdown"); //$NON-NLS-1$
				}
			}
		}));
	}
	
	public boolean contains(RegionCoord rCoord) {
		synchronized(lock) {
			return imageMap.containsKey(rCoord);
		}
	}
	
	public BufferedImage get(RegionCoord rCoord) {
		BufferedImage regionImage = null;
		synchronized(lock) {
			regionImage = imageMap.get(rCoord);
		}
		return regionImage;
	}
	
	public BufferedImage getGuaranteed(RegionCoord rCoord) {
		
		BufferedImage regionImage = null;
		
		synchronized(lock) {
			regionImage = imageMap.get(rCoord);
			if(regionImage==null) {
				RegionFileHandler rfh = RegionFileHandler.getInstance();
				 regionImage = rfh.readRegionFile(rfh.getRegionFile(rCoord), rCoord, 1);
				 imageMap.put(rCoord,  regionImage);
				 dirty.add(rCoord);
				 if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
					JourneyMap.getLogger().fine("RegionImageCache had to pull from disk: " + rCoord); //$NON-NLS-1$
				 }
			}
		}
		return regionImage;
	}
	
	public void putAll(final Set<Map.Entry<ChunkCoord, BufferedImage>> chunkImageEntries) {
		final RegionFileHandler rfh = RegionFileHandler.getInstance();
		synchronized(lock) {
			for(Map.Entry<ChunkCoord, BufferedImage> entry : chunkImageEntries) {
				final ChunkCoord cCoord = entry.getKey();
				final RegionCoord rCoord = cCoord.getRegionCoord();
				final BufferedImage chunkImage = entry.getValue();
				final BufferedImage regionImage = getGuaranteed(rCoord);
				insertChunk(cCoord, chunkImage, regionImage);
				dirty.add(rCoord);
			}
		}
		autoFlush();
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
	
	private Boolean insertChunk(ChunkCoord cCoord, BufferedImage chunkImage, BufferedImage regionImage) {
		Graphics2D g2d = regionImage.createGraphics();		
		Boolean regionAltered = true;
		
		int x,z;
				
		if(!cCoord.isUnderground()) {
			
			// Insert day image
			x = cCoord.getXOffsetDay();
			z = cCoord.getZOffsetDay();
			
			g2d.drawImage(chunkImage, x, z, x+16, z+16, 0,0,16,16, null);
	
			// Insert night image
			x = cCoord.getXOffsetNight();
			z = cCoord.getZOffsetNight();
			
			g2d.drawImage(chunkImage, x, z, x+16, z+16, 16,0,32,16, null);
				
		} else {
			
			// Insert underground image
			x = cCoord.getXOffsetUnderground();
			z = cCoord.getZOffsetUnderground();
			
			g2d.drawImage(chunkImage, x, z, x+16,z+16, 0,0,16,16,  null);
		}
		
		//regionAltered = !rastersEqual(originalData, newData);
		return regionAltered;
	}
	
	/**
	 * http://blog.varunin.com/2011/07/comparing-and-re-sizing-images-using.html
	 * @param ras1
	 * @param ras2
	 * @return
	 */
	private Boolean rastersEqual(Raster ras1, Raster ras2) {
		//Comparing the the two images for number of bands,width & height.
		if (ras1.getNumBands() != ras2.getNumBands()
				|| ras1.getWidth() != ras2.getWidth()
				|| ras1.getHeight() != ras2.getHeight()) {
			return false;
		}else{
			// Once the band ,width & height matches, comparing the images.
			for (int i = 0; i < ras1.getNumBands(); ++i) {
				for (int x = 0; x < ras1.getWidth(); ++x) {
					for (int y = 0; y < ras1.getHeight(); ++y) {
						if (ras1.getSample(x, y, i) != ras2.getSample(x, y, i)) {
							// If one of the result is false setting the result as false and breaking the loop.
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	public void flushToDisk() {
		RegionFileHandler rfh = RegionFileHandler.getInstance();
		synchronized(lock) {
			Set<RegionCoord> dirtyCopy = new HashSet<RegionCoord>(dirty);			
			for(RegionCoord dirtyRC : dirtyCopy) {
				rfh.writeRegionFile(dirtyRC, imageMap.get(dirtyRC));
				//JourneyMap.getLogger().info("Flushing to disk: " + dirtyRC);
			}
			dirty.clear();
			lastFlush = System.currentTimeMillis();
		}		
	}

	public void clear() {
		synchronized(lock) {
			imageMap.clear();
		}
	}
	
	public Set<Map.Entry<RegionCoord, BufferedImage>> getEntries() {
		return imageMap.entrySet();
	}
	
	class CacheMap extends LinkedHashMap<RegionCoord, BufferedImage> {
		
		private final int capacity;
		
		CacheMap(int capacity) {
			super(capacity + 1, 1.1f, true);
		    this.capacity = capacity;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<RegionCoord, BufferedImage> entry)
	    {
			Boolean remove = size() > capacity;
			if(remove) {
				RegionCoord rc = entry.getKey();
				if(dirty.contains(rc)) {
					BufferedImage image = entry.getValue();
					if(image!=null && image.getWidth()>0) {
						if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
							JourneyMap.getLogger().fine("RegionImageCache purging " + rc); //$NON-NLS-1$
						}
						try {
							RegionFileHandler.getInstance().writeRegionFile(rc, image);
						} catch(Throwable t) {
							JourneyMap.getLogger().severe("RegionImageCache failed to flush purging entry: " + entry.getKey()); //$NON-NLS-1$
							JourneyMap.getLogger().severe(LogFormatter.toString(t));
						}
					}
				}
				dirty.remove(rc);
			}
			if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine("RegionImageCache size: " + (this.size()-1)); //$NON-NLS-1$
			}
			return remove;
	    }
	}
	
}
