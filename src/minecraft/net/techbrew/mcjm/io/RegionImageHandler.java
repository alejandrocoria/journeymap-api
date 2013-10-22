package net.techbrew.mcjm.io;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.MapBlocks;
import net.techbrew.mcjm.ui.ZoomLevel;

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
	
	public BufferedImage getChunkImages(File worldDir, int rx1, int rz1, Integer vSlice, int rx2, int rz2, Constants.MapType mapType, int dimension, Boolean useCache, int sampling) {

		final RegionCoord regionCoord = RegionCoord.fromChunkPos(worldDir, rx1, vSlice, rz1, dimension);
		RegionCoord r2 = RegionCoord.fromChunkPos(worldDir, rx2, vSlice, rz2, dimension);
		
		if(!regionCoord.equals(r2)) {
			throw new IllegalArgumentException("Chunks not from the same region: " + regionCoord + " / " + r2); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
//		int ix1, ix2, iz1, iz2, width, height;
//		ix1 = regionCoord.getXOffsetDay(rx1);
//		iz1 = regionCoord.getZOffsetDay(rz1);
//		ix2 = regionCoord.getXOffsetDay(rx2);
//		iz2 = regionCoord.getZOffsetDay(rz2);
//		if(rx1==rx2) {
//			width=1;
//		} else {
//			width = rx2-rx1+1;
//		}
//		if(rz1==rz2) {
//			height=1;
//		} else {
//			height = rz2-rz1+1;
//		}
		
		BufferedImage regionImage = null;
		if(useCache) {
			regionImage = getCachedRegionImage(regionCoord, mapType);
		} else {
			regionImage = readRegionImage(getRegionImageFile(regionCoord, mapType, true), regionCoord, sampling, true); // TODO allow legacy?
		}
		//BufferedImage chunksImage = regionImage.getSubimage(ix1,iz1,width*16,height*16);
		
		return regionImage;
		
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
	
	static BufferedImage createBlankImage(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = img.createGraphics();
		return img;
	}
	
	public static BufferedImage readRegionImage(File regionFile, RegionCoord rCoord, int sampling, boolean legacy) {
		
		FileInputStream fis = null;
		BufferedImage image = null;

		if(regionFile.exists() && regionFile.canRead()) {
			
//				final int maxTries = 5;
//				int tries = 1;
//				while(tries<=maxTries) {
					try {
//						fis = new FileInputStream(regionFile);
//						FileChannel fc = fis.getChannel();
//						ImageIO.setUseCache(false);
//						ImageReader reader = ImageIO.getImageReadersBySuffix("png").next(); //$NON-NLS-1$
//						ImageReadParam param = new ImageReadParam();
//						param.setSourceSubsampling(sampling, sampling, 0, 0);
//						image = ImageIO.read(fis);				
						
						image = ImageIO.read(new BufferedInputStream(new FileInputStream(regionFile)));
						
						//break;
					} catch (Exception e) {
//						tries++;
						String error = Constants.getMessageJMERR21(regionFile, LogFormatter.toString(e));
						JourneyMap.getLogger().warning(error);
					} finally {
//				    	if(fis!=null) {
//							try {								
//								fis.close();
//							} catch (IOException e) {
//								JourneyMap.getLogger().severe(LogFormatter.toString(e));
//							}
//				    	}
					}
//				}
//				if(tries==maxTries) {
//					JourneyMap.getLogger().severe("Deleting unusable region file: " + regionFile);
//					regionFile.delete();
//				}
		}
			
		if(image==null) {
			if(legacy) {
				image = createBlankImage(1024, 512);
			} else {
				image = createBlankImage(512, 512);
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
	 * Used by MapSaver, MapService to get a merged image for what the display needs
	 * @param worldDir
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param mapType
	 * @param depth
	 * @throws IOException
	 */
	public static synchronized BufferedImage getMergedChunks(File worldDir, int x1, int z1,
			int x2, int z2, Constants.MapType mapType, Integer vSlice, int dimension, Boolean useCache, ZoomLevel zoomLevel)
			throws IOException {

		int imageWidth = Math.max(16, (x2 - x1) * 16);
		int imageHeight = Math.max(16, (z2 - z1) * 16);
		
		return getMergedChunks(worldDir, x1, z1, x2, z2, mapType, vSlice, dimension, useCache, zoomLevel, imageWidth, imageHeight);

	}
	
	/**
	 * Used by MapOverlay to let the image dimensions be directly specified (as a power of 2)
	 * @param worldDir
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param mapType
	 * @param vSlice
	 * @throws IOException
	 */
	public static synchronized BufferedImage getMergedChunks(File worldDir, int x1, int z1,
			int x2, int z2, Constants.MapType mapType, Integer vSlice, int dimension, Boolean useCache, ZoomLevel zoomLevel, int imageWidth, int imageHeight)
			throws IOException {

		long start = 0, stop = 0;		
		start = System.currentTimeMillis();

		boolean isUnderground = mapType.equals(Constants.MapType.underground);
		if(!isUnderground) {
			vSlice = null;
		}
		
		BufferedImage mergedImg = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = mergedImg.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, zoomLevel.antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, zoomLevel.interpolation);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// Merge chunk images
		RegionImageHandler rfh = RegionImageHandler.getInstance();
		
		// Get region coords
		final int rx1=RegionCoord.getRegionPos(x1);
		final int rz1=RegionCoord.getRegionPos(z1);
		final int rx2=RegionCoord.getRegionPos(x2);
		final int rz2=RegionCoord.getRegionPos(z2);
		
		// Get merged chunks by region
		for(int rx=rx1;rx<=rx2;rx++) {
			for(int rz=rz1;rz<=rz2;rz++) {
				
				// Get merged chunks from region
				RegionCoord rCoord = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
				int cx1 = Math.max(x1, rCoord.getMinChunkX());
				int cz1 = Math.max(z1, rCoord.getMinChunkZ());
				int cx2 = Math.min(x2, rCoord.getMaxChunkX());
				int cz2 = Math.min(z2, rCoord.getMaxChunkZ());
				
				BufferedImage chunkImg = rfh.getChunkImages(worldDir, cx1, cz1, vSlice, cx2, cz2, mapType, dimension, useCache, zoomLevel.sampling);				
				int imageX = ((cx1-x1) * 16)-1;
				int imageZ = ((cz1-z1) * 16)-1;
				g2D.drawImage(chunkImg, imageX, imageZ, null);
			}
		}
		
		// Show chunk grid
		if(PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID)) {
			g2D.setColor(new Color(130,130,130));
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1F));
					
			for(int x = -1; x<imageWidth; x+=16) {
				for(int z = -1; z<imageHeight; z+=16) {
					g2D.drawRect(x, z, 16, 16);
				}
			}
			g2D.setComposite(MapBlocks.OPAQUE);
		}
				
		if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
			stop = System.currentTimeMillis();
			JourneyMap.getLogger().fine("getMergedChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		g2D.dispose();
		
		return mergedImg;

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
	
}
