package net.techbrew.mcjm.io;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Iterator;
import java.util.logging.Level;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.plaf.FontUIResource;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Chunk;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.ui.ZoomLevel;

public class RegionFileHandler {
	
	private final Object lock;
	
	// On-demand-holder for instance
	private static class Holder {
        private static final RegionFileHandler INSTANCE = new RegionFileHandler();
    }

	// Get singleton instance.  Concurrency-safe.
    public static RegionFileHandler getInstance() {
        return Holder.INSTANCE;
    }

	private RegionFileHandler() {
		lock = new Object();
	}
	
	public File getRegionFile(RegionCoord rCoord) {
		StringBuffer sb = new StringBuffer();
		sb.append(rCoord.regionX).append(",").append(rCoord.regionZ); //$NON-NLS-1$
		if(!rCoord.cType.equals(Constants.CoordType.Normal)) {
			sb.append(",").append(rCoord.getVerticalSlice()); //$NON-NLS-1$
		}
		sb.append(getRegionFileSuffix(rCoord.cType));
		File regionFile = new File(rCoord.worldDir, sb.toString());
		//System.out.println("RegionFile: " + regionFile + " exists: " + regionFile.exists();
		return regionFile;
	}
	
	static String getRegionFileSuffix(final Constants.CoordType cType) {
		StringBuffer sb = new StringBuffer("_"); //$NON-NLS-1$
		sb.append(cType.name());
		sb.append(".region.png"); //$NON-NLS-1$
		return sb.toString();
	}
	

	
//	public synchronized void putChunkImage(BufferedImage chunkImage, ChunkCoord cCoord) {
//
//		RegionCoord rCoord = cCoord.getRegionCoord();
//		//BufferedImage regionImage = getCachedRegionImage(worldDir, rCoord);
//		BufferedImage regionImage = readRegionFile(getRegionFile(rCoord));
//		if(regionImage!=null) {
//			regionImage = insertChunk(cCoord, chunkImage, regionImage);
//			synchronized(lock) {
//				//cache.put(rCoord, regionImage);
//				writeRegionFile(regionImage, getRegionFile(rCoord));
//				//System.out.println("Inserted chunk " + chunk.xPosition + "," + chunk.zPosition + " into region " + rCoord.regionX + "," + rCoord.regionZ);
//			}
//		}
//	}
		
	public BufferedImage getChunkImage(ChunkCoord cCoord) {
		
		BufferedImage chunkImage = new BufferedImage(32,16, BufferedImage.TYPE_INT_ARGB);
		BufferedImage regionImage = RegionImageCache.getInstance().getGuaranteed(cCoord.getRegionCoord());
		
		Graphics2D g2d = chunkImage.createGraphics();		
		int x,z;

		// Get day image
		x = cCoord.getXOffsetDay();
		z = cCoord.getZOffsetDay();
		BufferedImage chunkDay = regionImage.getSubimage(x, z, 16, 16);
		g2d.drawImage(chunkDay, 0, 0, null);
		
		// Get night image
		x = cCoord.getXOffsetNight();
		z = cCoord.getZOffsetNight();
		BufferedImage chunkNight = regionImage.getSubimage(x, z, 16, 16);
		g2d.drawImage(chunkNight, 16, 0, null);
		
		return chunkImage;
	}
	
	public BufferedImage getChunkImages(File worldDir, int rx1, int rz1, Integer chunkY, int rx2, int rz2, Constants.MapType mapType, Constants.CoordType cType, Boolean useCache, int sampling) {

		final RegionCoord regionCoord = RegionCoord.fromChunkPos(worldDir, rx1, chunkY, rz1, cType);
		RegionCoord r2 = RegionCoord.fromChunkPos(worldDir, rx2, chunkY, rz2, cType);
		
		if(!regionCoord.equals(r2)) {
			throw new IllegalArgumentException("Chunks not from the same region: " + regionCoord + " / " + r2); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		int ix1, ix2, iz1, iz2, width, height;
		if(mapType.equals(Constants.MapType.day) || mapType.equals(Constants.MapType.underground)) {
			ix1 = regionCoord.getXOffsetDay(rx1);
			iz1 = regionCoord.getZOffsetDay(rz1);
			ix2 = regionCoord.getXOffsetDay(rx2);
			iz2 = regionCoord.getZOffsetDay(rz2);
		} else {
			ix1 = regionCoord.getXOffsetNight(rx1);
			iz1 = regionCoord.getZOffsetNight(rz1);
			ix2 = regionCoord.getXOffsetNight(rx2);
			iz2 = regionCoord.getZOffsetNight(rz2);
		}
		if(rx1==rx2) {
			width=1;
		} else {
			width = rx2-rx1+1;
		}
		if(rz1==rz2) {
			height=1;
		} else {
			height = rz2-rz1+1;
		}
		
		BufferedImage regionImage = null;
		if(useCache) {
			regionImage = getCachedRegionImage(regionCoord);
//			if(flushCacheToDisk) {
//				RegionImageCache.getInstance().flushToDisk();
//			} 
		} else {
			regionImage = readRegionFile(getRegionFile(regionCoord), regionCoord, sampling);
		}
		BufferedImage chunksImage = regionImage.getSubimage(ix1,iz1,width*16,height*16);
		
		return chunksImage;
		
	}
	
	public BufferedImage getCachedRegionImage(RegionCoord rCoord) {
		return RegionImageCache.getInstance().getGuaranteed(rCoord);
	}	
	
	private BufferedImage createBlankImage() {
		int height = (int) Math.pow(2, RegionCoord.SIZE)*16;
		int width = 2*height;
		return createBlankImage(width, height);
	}
	
	private BufferedImage createUndergroundBlankImage() {
		int height = (int) Math.pow(2, RegionCoord.SIZE)*16;
		int width = height;
		return createBlankImage(width, height);
	}
	
	private BufferedImage createBlankImage(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = img.createGraphics();
		return img;
	}
	
	public BufferedImage readRegionFile(File regionFile, RegionCoord rCoord, int sampling) {
		
		FileInputStream fis = null;
		BufferedImage image = null;

		if(!regionFile.exists()) {
			if(rCoord.isUnderground()) {
				image = createUndergroundBlankImage();
			} else {
				image = createBlankImage();
			}
		} else if (regionFile != null && regionFile.canRead()) {
			
				final int maxTries = 5;
				int tries = 1;
				while(tries<=maxTries) {
					try {
						fis = new FileInputStream(regionFile);
						FileChannel fc = fis.getChannel();
						ImageIO.setUseCache(false);
						ImageReader reader = ImageIO.getImageReadersBySuffix("png").next(); //$NON-NLS-1$
						ImageReadParam param = new ImageReadParam();
						param.setSourceSubsampling(sampling, sampling, 0, 0);
						image = ImageIO.read(fis);					
						break;
					} catch (Exception e) {
						tries++;
						String error = Constants.getMessageJMERR21(regionFile, LogFormatter.toString(e));
						JourneyMap.getLogger().warning(error);
					} finally {
				    	if(fis!=null) {
							try {								
								fis.close();
							} catch (IOException e) {
								JourneyMap.getLogger().severe(LogFormatter.toString(e));
							}
				    	}
					}
				}
				if(tries==maxTries) {
					JourneyMap.getLogger().severe("Deleting unusable region file: " + regionFile);
					regionFile.delete();
				}
			}
			
		if(image==null) {
			image = createBlankImage();
		}
		return image;
	}
	
	/**
	 * Write region image to file.
	 * 
	 * @param rCoord
	 * @param regionImage
	 */
	public void writeRegionFile(RegionCoord rCoord, BufferedImage regionImage) {

		FileOutputStream fos = null;
		FileLock fileLock = null;		
		FileOutputStream outputFile = null;
		
		if(regionImage==null) {
			JourneyMap.getLogger().warning("Null regionImage?");
			return;
		}
		
		ImageOutputStream imageOutputStream;	
		ImageWriter imageWriter;
		ImageWriteParam pngparams;
		
		
		File regionFile = getRegionFile(rCoord);
	    try {
//	    	if(!regionFile.exists()) {
//	    		regionFile.createNewFile();	
//	    	}
//	    	fos = new FileOutputStream(regionFile);
//			FileChannel fc = fos.getChannel();
//			if(fc.isOpen()) {
////				JourneyMap.getLogger().warning("Region file already open:" + regionFile.getPath());
////				return;
//			}
//			fileLock = fc.lock();
//			
//
//			imageWriter = (ImageWriter) ImageIO.getImageWritersByFormatName( "png" ).next();
//			pngparams = imageWriter.getDefaultWriteParam();
//			//pngparams.setCompressionQuality(1F);
//			pngparams.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
//			pngparams.setDestinationType(new ImageTypeSpecifier(regionImage.getColorModel(), regionImage.getSampleModel() ) );
//			 
//			imageOutputStream = ImageIO.createImageOutputStream(fos);
//			imageWriter.setOutput( imageOutputStream );
//
//			// Write the changed Image
//			imageWriter.write(null, new IIOImage(regionImage, null, null ), pngparams );
//
//			// Close the streams
//			imageOutputStream.close();
//			imageWriter.dispose();
			
			ImageIO.write(regionImage, "png", regionFile);
    		
	    } catch (Throwable e) {
	    	String error = Constants.getMessageJMERR22(regionFile, LogFormatter.toString(e));
			JourneyMap.getLogger().severe(error);
	    } finally {
	    	if(fileLock!=null) {
				try {
					fileLock.release();
				} catch (IOException e) {
					JourneyMap.getLogger().severe("Error releasing file lock: " + LogFormatter.toString(e));
				}
	    	}
	    	if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					JourneyMap.getLogger().severe("Error closing file lock: " + LogFormatter.toString(e));
				}
	    	}
	    }
		
	}
	
	/**
	 * Used by ChunkServlet and MapOverlay to get a merged image for what the display needs
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
			int x2, int z2, Constants.MapType mapType, int depth, final Constants.CoordType cType, Boolean useCache, ZoomLevel zoomLevel)
			throws IOException {

		long start = 0, stop = 0;
		
		start = System.currentTimeMillis();

		int width = Math.max(16, (x2 - x1) * 16);
		int height = Math.max(16, (z2 - z1) * 16);
		boolean isUnderground = mapType.equals(Constants.MapType.underground);
			
		BufferedImage mergedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = mergedImg.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, zoomLevel.antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, zoomLevel.interpolation);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// Merge chunk images
		RegionFileHandler rfh = RegionFileHandler.getInstance();
		
		// Get region coords
		final int rx1=RegionCoord.getRegionPos(x1);
		final int rz1=RegionCoord.getRegionPos(z1);
		final int rx2=RegionCoord.getRegionPos(x2);
		final int rz2=RegionCoord.getRegionPos(z2);
		
		// Get merged chunks by region
		for(int rx=rx1;rx<=rx2;rx++) {
			for(int rz=rz1;rz<=rz2;rz++) {
				
				// Get merged chunks from region
				RegionCoord rCoord = new RegionCoord(worldDir, rx, depth, rz, cType);
				int cx1 = Math.max(x1, rCoord.getMinChunkX());
				int cz1 = Math.max(z1, rCoord.getMinChunkZ());
				int cx2 = Math.min(x2, rCoord.getMaxChunkX());
				int cz2 = Math.min(z2, rCoord.getMaxChunkZ());
				
				BufferedImage chunks = rfh.getChunkImages(worldDir, cx1, cz1, depth, cx2, cz2, mapType, rCoord.cType, useCache, zoomLevel.sampling);				
				int imageX = ((cx1-x1) * 16)-1;
				int imageZ = ((cz1-z1) * 16)-1;
				g2D.drawImage(chunks, imageX, imageZ, null);
				
			}
		}
				
		if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
			stop = System.currentTimeMillis();
			JourneyMap.getLogger().fine("getMergedChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return mergedImg;

	}
	
	public static class RegionFileFilter implements FilenameFilter {
		
		final String regionName;
		
		public RegionFileFilter(final Constants.CoordType cType) {
			regionName = RegionFileHandler.getRegionFileSuffix(cType);
		}
		
		@Override
		public boolean accept(File arg0, String arg1) {
			return arg1.endsWith(regionName);
		}	
	}
	
}
