package net.techbrew.mcjm.model;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

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
		
		Constants.MapType getMapType() {
			return mapType;
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
			return mapType.name() + delim + imageFile.getPath() + delim + "image=" + (_image==null ? "null" : "ok") + "dirty=" + _dirty;
		}

	}
	
	class WrapperComparator implements Comparator<Wrapper> {
		@Override
		public int compare(Wrapper o1, Wrapper o2) {
			return o1.mapType.compareTo(o2.mapType);
		}
	}

}
