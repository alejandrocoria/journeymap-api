package net.techbrew.mcjm.model;

import net.techbrew.mcjm.Constants;

import javax.naming.OperationNotSupportedException;
import java.awt.image.BufferedImage;

/**
 * A ChunkImageSet contains one or more Wrappers of image, file, and maptype.
 * @author mwoodman
 *
 */
public class ChunkImageSet extends ImageSet {

	protected final ChunkCoord cCoord;
	
	public ChunkImageSet(ChunkCoord cCoord) {
		super();
		this.cCoord = cCoord;
	}
	
	public ChunkCoord getCCoord() {
		return cCoord;
	}

	@Override
	protected Wrapper getWrapper(Constants.MapType mapType) {
		synchronized(lock) {
			Wrapper wrapper = imageWrappers.get(mapType);
			if(wrapper==null) {
				wrapper = addWrapper(mapType, null, null);
			}
			return wrapper;			
		}
	}
	
	@Override
	public int hashCode() {
		return 31 * cCoord.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return cCoord.equals(((ChunkImageSet)obj).cCoord);
	}

	
	@Override
	protected Wrapper addWrapper(Constants.MapType mapType, BufferedImage image) {
		return addWrapper(new Wrapper(mapType, null, image));
	}

	@Override
	public void writeToDisk(boolean force) {
		throw new RuntimeException(new OperationNotSupportedException());
	}
	
	
}
