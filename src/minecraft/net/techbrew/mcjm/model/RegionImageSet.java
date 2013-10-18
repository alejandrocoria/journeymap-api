package net.techbrew.mcjm.model;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.log.LogFormatter;

/**
 * A RegionImageSet contains one or more Wrappers of image, file, and maptype.
 * @author mwoodman
 *
 */
public class RegionImageSet {
	
	private final Map<Constants.MapType, Wrapper> imageWrappers;
	
	private final RegionCoord rCoord;
	
	private final Object lock = new Object();

	public RegionImageSet(RegionCoord rCoord) {
		this.rCoord = rCoord;
		imageWrappers = Collections.synchronizedMap(new HashMap<Constants.MapType, Wrapper>(3));
	}
	
	private Wrapper getWrapper(Constants.MapType mapType) {
		
		synchronized(lock) {
			
			// Check wrappers
			Wrapper wrapper = imageWrappers.get(mapType);
			if(wrapper!=null) return wrapper;
			
			// Prepare to find image in file
			BufferedImage image = null;
			File imageFile = null;
			
			// Check for new region file 
			imageFile = RegionImageHandler.getRegionImageFile(rCoord, mapType, false);	
			boolean useLegacy = !imageFile.exists();
			image = RegionImageHandler.readRegionImage(imageFile, rCoord, 1, false);
			
			// Add wrapper
			wrapper = addWrapper(mapType, imageFile, image);
			if(!useLegacy) {
				return wrapper;
			}
			
			// Fallback check for legacy (nightAndDay) region file
			File legacyFile = RegionImageHandler.getRegionImageFileLegacy(rCoord);
			if(legacyFile.exists()) {
				
				System.out.println("******** Using legacy region for " + rCoord);
				
				// Get image for wrapper from legacy
				BufferedImage legacyImage = RegionImageHandler.readRegionImage(legacyFile, rCoord, 1, true);
				wrapper.setImage(getSubimage(rCoord, mapType, legacyImage));
				
				// Add legacy wrapper
				addWrapper(Constants.MapType.OBSOLETE, legacyFile, null);
				
				// Add other wrappers for day/night
				if(mapType==Constants.MapType.day) {
					addWrapperFromLegacy(Constants.MapType.night, legacyImage);
				} else if(mapType==Constants.MapType.night) {
					addWrapperFromLegacy( Constants.MapType.day, legacyImage);
				}			
				
			}			
			return wrapper;	
		}
	}
	
	public boolean hasLegacy() {
		synchronized(lock) {
			Wrapper wrapper = imageWrappers.get(MapType.OBSOLETE);
			return wrapper!=null;
		}
	}
	
	public BufferedImage getImage(Constants.MapType mapType) {		
		return getWrapper(mapType).getImage();		
	}
	
	public File getFile(Constants.MapType mapType) {		
		return getWrapper(mapType).getFile();		
	}
	
	public void insertChunk(ChunkCoord cCoord, BufferedImage chunkImage) {		
		synchronized(lock) {
			Graphics2D g2d;			
			int x,z;				
			if(!cCoord.isUnderground()) {			
				insertChunkDay(cCoord, chunkImage);
				insertChunkNight(cCoord, chunkImage);				
			} else {			
				insertChunkUnderground(cCoord, chunkImage);
			}		
		}
	}
	
	private void insertChunkDay(ChunkCoord cCoord, BufferedImage chunkImage) {
		final Wrapper wrapper = getWrapper(MapType.day);		
		final int x = cCoord.getXOffsetDay();
		final int z = cCoord.getZOffsetDay();
		final Graphics2D g2d = wrapper.getImage().createGraphics();	
		g2d.drawImage(chunkImage, x, z, x+16, z+16, 0,0,16,16, null);
		g2d.dispose();
		wrapper.setDirty();
	}
	
	private void insertChunkNight(ChunkCoord cCoord, BufferedImage chunkImage) {
		final Wrapper wrapper = getWrapper(MapType.night);		
		final int x = cCoord.getXOffsetDay();
		final int z = cCoord.getZOffsetDay();
		final Graphics2D g2d = wrapper.getImage().createGraphics();	
		g2d.drawImage(chunkImage, x, z, x+16, z+16, 16,0,32,16, null);
		g2d.dispose();
		wrapper.setDirty();
	}
	
	private void insertChunkUnderground(ChunkCoord cCoord, BufferedImage chunkImage) {
		final Wrapper wrapper = getWrapper(MapType.underground);		
		final int x = cCoord.getXOffsetUnderground();
		final int z = cCoord.getZOffsetUnderground();
		final Graphics2D g2d = wrapper.getImage().createGraphics();	
		g2d.drawImage(chunkImage, x, z, x+16, z+16, 0,0,16,16,  null);
		g2d.dispose();
		wrapper.setDirty();
	}
	
	public void setImage(Constants.MapType mapType, BufferedImage image) {
		synchronized(lock) {
			Wrapper wrapper = imageWrappers.get(mapType);
			if(wrapper!=null) {
				wrapper.setImage(image);			
			} else {
				addWrapper(mapType, image);
			}
		}
	}
	
	public void setDirtyFor(Constants.MapType mapType) {
		Wrapper wrapper = imageWrappers.get(mapType);
		if(wrapper!=null) {
			wrapper.setDirty();	
		}
	}
	
	public void writeToDisk(boolean force) {
		synchronized(lock) {
			for(Wrapper wrapper: imageWrappers.values()) {
				if(force || wrapper.isDirty()) {
					wrapper.writeToDisk();					
				}
			}
			imageWrappers.remove(MapType.OBSOLETE);
		}
	}
	
	public boolean isDirty() {
		for(Wrapper wrapper: imageWrappers.values()) {
			if(wrapper.isDirty()) {
				return true;
			}
		}
		return false;
	}
	
	private BufferedImage getSubimage(RegionCoord rCoord, MapType mapType, BufferedImage image) {
		if(image==null) return null;
		switch(mapType) {
			case night: {
				return copyImage(image.getSubimage(512, 0, 512, 512));
			}
			default: {
				return copyImage(image.getSubimage(0, 0, 512, 512));
			}
		}
	}
	
	private BufferedImage copyImage(BufferedImage image) {
		BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
	    Graphics g = copy.getGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    return copy;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("[ ");
		Iterator<Wrapper> iter = imageWrappers.values().iterator();
		while(iter.hasNext()) {
			sb.append(iter.next().toString());
			if(iter.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.append(" ]").toString();
	}
	
	@Override
	public int hashCode() {
		return 31 * rCoord.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return rCoord.equals(((RegionImageSet)obj).rCoord);
	}

	/****************************/
	
	private Wrapper addWrapperFromLegacy(Constants.MapType mapType, BufferedImage legacyImage) {
		BufferedImage image = getSubimage(rCoord, mapType, legacyImage);
		return addWrapper(mapType, image);
	}
	
	private Wrapper addWrapper(Constants.MapType mapType, BufferedImage image) {
		return addWrapper(new Wrapper(mapType, RegionImageHandler.getRegionImageFile(rCoord, mapType, false), image));
	}
	
	private Wrapper addWrapper(Constants.MapType mapType, File imageFile, BufferedImage image) {
		return addWrapper(new Wrapper(mapType, imageFile, image));
	}

	private Wrapper addWrapper(Wrapper wrapper) {
		imageWrappers.put(wrapper.mapType, wrapper);
		return wrapper;
	}
	
	class Wrapper {
		final static String delim = " : ";
		final Constants.MapType mapType;
		final File imageFile;
		
		BufferedImage _image = null;
		boolean _dirty = true;
		
		Wrapper(Constants.MapType mapType, File imageFile, BufferedImage image) {
			this.mapType = mapType;
			this.imageFile = imageFile;
			_image = image;
			if(mapType!=MapType.OBSOLETE && image==null) {
				_dirty=false;
			}
		}
		
		void setImage(BufferedImage image) {
			_image = image;
			_dirty = true;
		}
		
		File getFile() {
			return imageFile;
		}
		
		BufferedImage getImage() {
			return _image;
		}
		
		void setDirty() {
			_dirty = true;
		}
		
		boolean isDirty() {
			return _dirty;
		}
		
		private void writeToDisk() {
			try {
				if(mapType==Constants.MapType.OBSOLETE) {
					if(imageFile.exists()) {
						imageFile.delete();
						System.out.println("PURGED legacy region for " + rCoord);						
					}
					return;
				}			
				
				if(_image==null) {
					JourneyMap.getLogger().warning("Null image for " + this);
				} else {
			    	if(!imageFile.canRead()) {
			    		imageFile.mkdirs();
			    	}
					ImageIO.write(_image, "png", imageFile);
					_dirty = false;
				}
		    } catch (Throwable e) {
		    	String error = Constants.getMessageJMERR22(imageFile, LogFormatter.toString(e));
				JourneyMap.getLogger().severe(error);
		    } 
		}
		
		@Override
		public String toString() {
			return mapType.name() + delim + imageFile.getPath() + delim + "image=" + (_image==null ? "null" : "ok") + "dirty=" + _dirty;
		}

	}

}
