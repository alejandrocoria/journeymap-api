package net.techbrew.mcjm.io;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.ChunkImageCache.CacheMap;
import net.techbrew.mcjm.log.LogFormatter;

public class RegionImageCache  {
	
	private static final int SIZE = 4;
	private static RegionImageCache instance;
	private CacheMap imageMap;
	private Set<RegionCoord> dirty;
	
	public synchronized static RegionImageCache getInstance() {
		if(instance==null) {
			instance = new RegionImageCache();
		}
		return instance;
	}
	
	private RegionImageCache() {
		imageMap = new CacheMap(SIZE);
		dirty = new HashSet<RegionCoord>(SIZE);
	}
	
	public boolean contains(RegionCoord rCoord) {
		synchronized(imageMap) {
			return imageMap.containsKey(rCoord);
		}
	}
	
	public BufferedImage get(RegionCoord rCoord) {
		BufferedImage regionImage = null;
		synchronized(imageMap) {
			regionImage = imageMap.get(rCoord);
		}
		return regionImage;
	}
	
	public BufferedImage getGuaranteed(RegionCoord rCoord) {
		
		BufferedImage regionImage = null;
		
		synchronized(imageMap) {
			regionImage = imageMap.get(rCoord);
			if(regionImage==null) {
				RegionFileHandler rfh = RegionFileHandler.getInstance();
				 regionImage = rfh.readRegionFile(rfh.getRegionFile(rCoord), rCoord, 1);
				 imageMap.put(rCoord,  regionImage);
				 dirty.add(rCoord);
			}
		}
		return regionImage;
	}
	
	public void putAll(final Set<Map.Entry<ChunkCoord, BufferedImage>> chunkImageEntries) {
		final RegionFileHandler rfh = RegionFileHandler.getInstance();
		for(Map.Entry<ChunkCoord, BufferedImage> entry : chunkImageEntries) {
			final ChunkCoord cCoord = entry.getKey();
			final RegionCoord rCoord = cCoord.getRegionCoord();
			final BufferedImage chunkImage = entry.getValue();
			final BufferedImage regionImage = getGuaranteed(rCoord);
			insertChunk(cCoord, chunkImage, regionImage);
			synchronized(dirty) {
				dirty.add(rCoord);
			}
		}
	}
	
	private Boolean insertChunk(ChunkCoord cCoord, BufferedImage chunkImage, BufferedImage regionImage) {
		Graphics2D g2d = regionImage.createGraphics();
		Boolean regionAltered = false;
		
		int x,z;
				
		if(!cCoord.isUnderground()) {
			
			// Insert day image
			x = cCoord.getXOffsetDay();
			z = cCoord.getZOffsetDay();
			
			g2d.drawImage(chunkImage, x, z, x+16, z+16, 0,0,15,15, null);
	
			// Insert night image
			x = cCoord.getXOffsetNight();
			z = cCoord.getZOffsetNight();
			
			g2d.drawImage(chunkImage, x, z, x+16, z+16, 16,0,32,15, null);
				
		} else {
			
			// Insert underground image
			x = cCoord.getXOffsetUnderground();
			z = cCoord.getZOffsetUnderground();
			
			g2d.drawImage(chunkImage, x, z, x+16,z+16, 0,0,15,15,  null);
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
		Set<RegionCoord> dirtyCopy = null;
		synchronized(dirty) {
			dirtyCopy = new HashSet<RegionCoord>(dirty);
			dirty.clear();
		}
		synchronized(imageMap) {
			for(RegionCoord dirtyRC : dirtyCopy) {
				rfh.writeRegionFile(dirtyRC, imageMap.get(dirtyRC));
				//JourneyMap.getLogger().info("Flushing to disk: " + dirtyRC);
			}
		}
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
	
	public Set<Map.Entry<RegionCoord, BufferedImage>> getEntries() {
		return imageMap.entrySet();
	}
	
	class CacheMap extends LinkedHashMap<RegionCoord, BufferedImage> {
		
		private final int capacity;
		private boolean purge = true;
		
		CacheMap(int capacity) {
			super(capacity + 1, 1.1f, true);
		    this.capacity = capacity;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<RegionCoord, BufferedImage> entry)
	    {
			if(!purge) return false;
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
