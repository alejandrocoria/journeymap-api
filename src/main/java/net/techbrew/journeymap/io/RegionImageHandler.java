package net.techbrew.journeymap.io;


import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

public class RegionImageHandler {
	
	private final Object lock;
	
	// On-demand-holder for instance
	private static class Holder {
        private static final RegionImageHandler INSTANCE = new RegionImageHandler();
    }

	// Get singleton instance.  Concurrency-safe.
    public static RegionImageHandler getInstance() {
        return Holder.INSTANCE;
    }

	private RegionImageHandler() {
		lock = new Object();
	}
	
	public static File getImageDir(RegionCoord rCoord, MapType mapType) {
		File dimDir = new File(rCoord.worldDir, "DIM"+rCoord.dimension); //$NON-NLS-1$
		File subDir = null;
		if(rCoord.isUnderground()) {
			subDir = new File(dimDir, Integer.toString(rCoord.getVerticalSlice()));
		} else {
			subDir = new File(dimDir, mapType.name());
		}
		if(!subDir.exists()) {
			subDir.mkdirs();
		}
		return subDir;
	}
	
	@Deprecated
	public static File getDimensionDir(File worldDir, int dimension) {
		File dimDir = new File(worldDir, "DIM"+dimension); //$NON-NLS-1$
		if(!dimDir.exists()) {
			dimDir.mkdirs();
		}
		return dimDir;
	}
	
	public static File getRegionImageFile(RegionCoord rCoord, Constants.MapType mapType, boolean allowLegacy) {
		StringBuffer sb = new StringBuffer();
		sb.append(rCoord.regionX).append(",").append(rCoord.regionZ).append(".png"); //$NON-NLS-1$ //$NON-NLS-2$
		File regionFile = new File(getImageDir(rCoord, mapType), sb.toString());
		
		if(allowLegacy && !regionFile.exists()) {
			File oldRegionFile = getRegionImageFileLegacy(rCoord);
			if(oldRegionFile.exists()) {
				regionFile = oldRegionFile;
			}
		}
		return regionFile;
	}
	
	@Deprecated
	public static File getRegionImageFileLegacy(RegionCoord rCoord) {
		StringBuffer sb = new StringBuffer();
		sb.append(rCoord.regionX).append(",").append(rCoord.regionZ); //$NON-NLS-1$
		Constants.CoordType cType = Constants.CoordType.convert(rCoord.dimension);
		if(cType!=Constants.CoordType.Normal) {
			sb.append(",").append(rCoord.getVerticalSlice()); //$NON-NLS-1$
		}
		sb.append(getRegionFileSuffix(cType));
		File regionFile = new File(rCoord.worldDir, sb.toString());
		return regionFile;
	}
	
	@Deprecated
	static String getRegionFileSuffix(final Constants.CoordType cType) {
		StringBuffer sb = new StringBuffer("_"); //$NON-NLS-1$
		sb.append(cType.name());
		sb.append(".region.png"); //$NON-NLS-1$
		return sb.toString();
	}
	
	public BufferedImage getCachedRegionImage(RegionCoord rCoord, MapType mapType) {
		return RegionImageCache.getInstance().getGuaranteedImage(rCoord, mapType);
	}	
	
	public static boolean isBlank(BufferedImage img) {		
		int[] pixels = img.getRaster().getPixels(0,0,img.getWidth()-1,img.getHeight()-1, (int[]) null);
		boolean isBlank = true;
		for(int pixel: pixels) {
			if(pixel!=0) {
				isBlank = false;
				break;
			}
		}
		return isBlank;
	}
	
	public static BufferedImage createBlankImage(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = img.createGraphics();
		return img;
	}
	
	public static BufferedImage readRegionImage(File regionFile, RegionCoord rCoord, int sampling, boolean legacy, boolean returnNull) {
		
		FileInputStream fis = null;
		BufferedImage image = null;

		if(regionFile.exists() && regionFile.canRead()) {			
			try {					
				image = ImageIO.read(new BufferedInputStream(new FileInputStream(regionFile)));
			} catch (Exception e) {
				String error = Constants.getMessageJMERR21(regionFile, LogFormatter.toString(e));
				JourneyMap.getLogger().severe(error);
			} 
		}
			
		if(image==null) {
			if(!returnNull) {
				if(legacy) {
					image = createBlankImage(1024, 512);
				} else {
					image = createBlankImage(512, 512);
				}
			}
		}
		return image;
	}
	
	public static BufferedImage getImage(File file) {
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			String error = Constants.getMessageJMERR17(e.getMessage());
			JourneyMap.getLogger().severe(error);
			return null;
		}
	}


    /**
     * Used by MapOverlay to let the image dimensions be directly specified (as a power of 2)
     * @param worldDir
     * @param startCoord
     * @param endCoord
     * @param mapType
     * @param vSlice
     * @param dimension
     * @param useCache
     * @param imageWidth
     * @param imageHeight
     * @param allowNullImage
     * @return
     */
	public static synchronized BufferedImage getMergedChunks(final File worldDir, final ChunkCoordIntPair startCoord, final ChunkCoordIntPair endCoord, final Constants.MapType mapType, Integer vSlice, final int dimension, final Boolean useCache, BufferedImage image, final Integer imageWidth, final Integer imageHeight, final boolean allowNullImage, boolean showGrid) {
		
		long start = 0, stop = 0;		
		start = System.currentTimeMillis();

		boolean isUnderground = mapType.equals(Constants.MapType.underground);
		if(!isUnderground) {
			vSlice = null;
		}
		
		final int initialWidth = (endCoord.chunkXPos-startCoord.chunkXPos+1) * 16;
		final int initialHeight = (endCoord.chunkZPos-startCoord.chunkZPos+1) * 16;		

        if(image==null || image.getWidth()!=initialWidth || imageHeight!=initialHeight) {
		    image = new BufferedImage(initialWidth, initialHeight, BufferedImage.TYPE_INT_ARGB);
        }
		final Graphics2D g2D = initRenderingHints(image.createGraphics());
        g2D.clearRect(0, 0, imageWidth, imageHeight);

		final RegionImageCache cache = RegionImageCache.getInstance();

		RegionCoord rc = null;
		BufferedImage regionImage = null;
		
		final int rx1 = RegionCoord.getRegionPos(startCoord.chunkXPos);
		final int rx2 = RegionCoord.getRegionPos(endCoord.chunkXPos);
		final int rz1 = RegionCoord.getRegionPos(startCoord.chunkZPos);
		final int rz2 = RegionCoord.getRegionPos(endCoord.chunkZPos);
		
		int rminCx, rminCz, rmaxCx, rmaxCz, sx1, sy1, sx2, sy2, dx1, dx2, dy1, dy2;
		
		boolean imageDrawn = false;
		for(int rx=rx1;rx<=rx2;rx++) {
			for(int rz=rz1;rz<=rz2;rz++) {
				rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
				if(cache.contains(rc)) {
					regionImage = cache.getGuaranteedImage(rc, mapType);
				} else {
					regionImage = RegionImageHandler.readRegionImage(RegionImageHandler.getRegionImageFile(rc, mapType, false), rc, 1, false, true);
				}

                if(regionImage==null)
                {
                    continue;
                }

				rminCx = Math.max(rc.getMinChunkX(), startCoord.chunkXPos);
				rminCz = Math.max(rc.getMinChunkZ(), startCoord.chunkZPos);
				rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.chunkXPos);
				rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.chunkZPos);
						
				int xoffset = rc.getMinChunkX()*16;
				int yoffset = rc.getMinChunkZ()*16;
				sx1 = (rminCx * 16) - xoffset;
				sy1 = (rminCz * 16) - yoffset;
				sx2 = sx1 + ((rmaxCx-rminCx + 1)* 16);
				sy2 = sy1 + ((rmaxCz-rminCz + 1)* 16);
				
				xoffset = startCoord.chunkXPos*16;
				yoffset = startCoord.chunkZPos*16;
				dx1 = (startCoord.chunkXPos*16) - xoffset;
				dy1 = (startCoord.chunkZPos*16) - yoffset;
				dx2 = dx1 +((endCoord.chunkXPos-startCoord.chunkXPos + 1) * 16);
				dy2 = dy1 +((endCoord.chunkZPos-startCoord.chunkZPos + 1) * 16);
				
				g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);		
				imageDrawn = true;
			}
		}

		// Show chunk grid
		if(imageDrawn) {
			if(showGrid) {

				if(mapType==MapType.day) {
                    g2D.setColor(Color.black);
					g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.25F));
				} else {
                    g2D.setColor(Color.gray);
					g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.1F));
				}
	
				for(int x = 0; x<=initialWidth; x+=16) {
					g2D.drawLine(x, 0, x, initialHeight);
				}
				
				for(int z = 0; z<=initialHeight; z+=16) {
					g2D.drawLine(0, z, initialWidth, z);
				}
			}
		}
		
		g2D.dispose();
				
		if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
			stop = System.currentTimeMillis();
			JourneyMap.getLogger().fine("getMergedChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}

        if(allowNullImage && !imageDrawn){
            return null;
        }
		
		// Scale if needed
		if(imageHeight!=null && imageWidth!=null && (initialHeight!=imageHeight || initialWidth!=imageWidth)) {
			final BufferedImage scaledImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = initRenderingHints(scaledImage.createGraphics());
			g.drawImage(image, 0, 0, imageWidth, imageHeight, null);
			g.dispose();
			return scaledImage;
		} else {
			return image;
		}		

	}

    /**
     *  Used by MapOverlay to let the image dimensions be directly specified (as a power of 2)
     * @param worldDir
     * @param startCoord
     * @param endCoord
     * @param mapType
     * @param vSlice
     * @param dimension
     * @param since
     * @return
     */
	public static synchronized boolean hasImageChanged(final File worldDir, final ChunkCoordIntPair startCoord, final ChunkCoordIntPair endCoord, final Constants.MapType mapType, Integer vSlice, final int dimension, final long since) {
		
		boolean isUnderground = mapType.equals(Constants.MapType.underground);
		if(!isUnderground) {
			vSlice = null;
		}
				
		final RegionImageCache cache = RegionImageCache.getInstance();

		RegionCoord rc = null;
	
		final int rx1 = RegionCoord.getRegionPos(startCoord.chunkXPos);
		final int rx2 = RegionCoord.getRegionPos(endCoord.chunkXPos);
		final int rz1 = RegionCoord.getRegionPos(startCoord.chunkZPos);
		final int rz2 = RegionCoord.getRegionPos(endCoord.chunkZPos);
		
		int rminCx, rminCz, rmaxCx, rmaxCz, sx1, sy1, sx2, sy2, dx1, dx2, dy1, dy2;
		
		for(int rx=rx1;rx<=rx2;rx++) {
			for(int rz=rz1;rz<=rz2;rz++) {
				rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
				if(cache.contains(rc)) {
					if(cache.isDirtySince(rc, mapType, since)) {
						return true;
					}
				} else {
					File file = RegionImageHandler.getRegionImageFile(rc, mapType, false);
					if(file.canRead() && file.lastModified()>since) {
						return true;
					}
				}							
			}
		}

		return false;		
	}
	
	public static File getBlank512x512ImageFile() {
		final File dataDir = new File(Minecraft.getMinecraft().mcDataDir, Constants.DATA_DIR);
		final File tmpFile = new File(dataDir, "blank512x512.png");
		if(!tmpFile.canRead()) {
			BufferedImage image;
			image = createBlankImage(512,512);				
			try {
				dataDir.mkdirs();
				ImageIO.write(image, "png", tmpFile);
				tmpFile.setReadOnly();
				tmpFile.deleteOnExit();
			} catch(IOException e) {
				JourneyMap.getLogger().severe(Constants.getMessageJMERR22(tmpFile, LogFormatter.toString(e)));
			}
		}
		return tmpFile;
	}
	
	public static Graphics2D initRenderingHints(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return g;
	}
	
}
