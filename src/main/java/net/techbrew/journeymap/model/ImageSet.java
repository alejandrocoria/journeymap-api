package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * An ImageSet contains one or more Wrappers of image, file, and maptype.
 * @author mwoodman
 *
 */
public abstract class ImageSet {
	
	protected final Map<Constants.MapType, Wrapper> imageWrappers;
	
	protected final Object lock = new Object();

	public ImageSet() {
		imageWrappers = Collections.synchronizedMap(new HashMap<Constants.MapType, Wrapper>(3));
	}
	
	protected abstract Wrapper getWrapper(Constants.MapType mapType);
	
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
			List<Wrapper> list = new ArrayList<Wrapper>(imageWrappers.values());
			Collections.sort(list, new WrapperComparator());
			for(Wrapper wrapper: list) {
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
	
	public boolean updatedSince(MapType mapType, long time) {
		for(Wrapper wrapper: imageWrappers.values()) {
			if(mapType!=null) {
				if(wrapper.getMapType()==mapType && wrapper.getTimestamp()>time) {
					return true;
				}
			} else if(wrapper.getTimestamp()>time) {
				return true;
			}
		}
		return false;
	}
	
	protected BufferedImage copyImage(BufferedImage image) {
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
	
	public void clear() {
		imageWrappers.clear();
	}
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj) ;

	/****************************/
	
	protected abstract Wrapper addWrapper(Constants.MapType mapType, BufferedImage image);
	
	protected Wrapper addWrapper(Constants.MapType mapType, File imageFile, BufferedImage image) {
		return addWrapper(new Wrapper(mapType, imageFile, image));
	}

	protected Wrapper addWrapper(Wrapper wrapper) {
		imageWrappers.put(wrapper.mapType, wrapper);
		return wrapper;
	}
	
	class Wrapper {
		final static String delim = " : ";
		final Constants.MapType mapType;
		final File imageFile;
		
		BufferedImage _image = null;
		boolean _dirty = true;
		long timestamp = System.currentTimeMillis();
		
		Wrapper(Constants.MapType mapType, File imageFile, BufferedImage image) {
			this.mapType = mapType;
			this.imageFile = imageFile;
			setImage(image);
			if(mapType==MapType.OBSOLETE) {
				_dirty=false;
			}
		}
		
		void setImage(BufferedImage image) {
			if(image!=_image) {
				setDirty();
			}
			_image = image;
			
		}
		
		File getFile() {
			return imageFile;
		}
		
		Constants.MapType getMapType() {
			return mapType;
		}
		
		BufferedImage getImage() {
			return _image;
		}
		
		void setDirty() {
			_dirty = true;
			timestamp = new Date().getTime();
		}
		
		long getTimestamp() {
			return timestamp;
		}
		
		boolean isDirty() {
			return _dirty;
		}

		protected void writeToDisk() {
			try {
				if(mapType==Constants.MapType.OBSOLETE) {
					if(imageFile.exists()) {
						imageFile.delete();						
					}
					return;
				}			
				
				if(_image==null) {
					JourneyMap.getLogger().warning("Null image for " + this);
				} else {
					File dir = imageFile.getParentFile();
			    	if(!dir.exists()) {
			    		dir.mkdirs();
			    	}
					ImageIO.write(_image, "png", new FileOutputStream(imageFile));
					if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
						JourneyMap.getLogger().fine("Wrote to disk: " + imageFile); //$NON-NLS-1$
					}	
					_dirty = false;
				}
		    } catch (Throwable e) {
		    	String error = Constants.getMessageJMERR22(imageFile, LogFormatter.toString(e));
				JourneyMap.getLogger().severe(error);
				throw new RuntimeException(e);
		    } 
		}
		
		@Override
		public String toString() {
			return mapType.name() + delim + (imageFile!=null ? imageFile.getPath() : "") + delim + "image=" + (_image==null ? "null" : "ok") + ", dirty=" + _dirty;
		}

	}
	
	class WrapperComparator implements Comparator<Wrapper> {
		@Override
		public int compare(Wrapper o1, Wrapper o2) {
			return o1.mapType.compareTo(o2.mapType);
		}
	}

}
