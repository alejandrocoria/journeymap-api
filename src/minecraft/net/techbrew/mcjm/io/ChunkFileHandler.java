package net.techbrew.mcjm.io;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketImpl;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Chunk;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
//import net.minecraft.src.NetworkManager;
import net.minecraft.src.World;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.WorldProviderHell;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.ReflectionHelper;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.Constants.WorldType;
import net.techbrew.mcjm.log.LogFormatter;

public class ChunkFileHandler {
	
	public ChunkFileHandler() {
		
	}
	
	public synchronized static void writeFile(File chunkFile, BufferedImage chunkImg) {
		
		FileOutputStream fos = null;
		FileLock fileLock = null;
		
		try {
			if (!chunkFile.exists()) {
				chunkFile.getParentFile().mkdirs();
				chunkFile.createNewFile();
			}
			if (chunkFile.exists() && chunkFile.canWrite()) {
				fos = new FileOutputStream(chunkFile);
				FileChannel fc = fos.getChannel();
				fileLock = fc.lock();
				ImageIO.write(chunkImg, "png", fos); //$NON-NLS-1$
			}
		} catch (Exception e) {
			String message = Constants.getMessageJMERR08(e.getMessage());
			JourneyMap.getLogger().severe(message);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(e));
			
		} catch (Throwable e) {
			String message = Constants.getMessageJMERR08(e.getMessage());
			JourneyMap.getLogger().severe(message);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(e));
		} finally {
			if(fileLock!=null) {
				try {
					fileLock.release();
				} catch (IOException e) {
					JourneyMap.getLogger().severe(LogFormatter.toString(e));
				}
	    	}
	    	if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					JourneyMap.getLogger().severe(LogFormatter.toString(e));
				}
	    	}
		}
	}
	
	
	
	private static File getChunkFile(Minecraft minecraft, Chunk chunk, boolean underground, int chunkY) {
		return getChunkFile(FileHandler.getWorldDir(minecraft), chunk.xPosition, underground ? chunkY : null,  chunk.zPosition, minecraft.theWorld.provider.dimensionId);	
	}
	
	public static File getChunkFile(File worldDir, int x, Integer y, int z, int worldProviderType) {
		final Boolean underground = (y!=null);
		final Constants.CoordType cType = Constants.CoordType.convert(underground, worldProviderType); 
		return new File(worldDir, getChunkFileName(x,y,z, cType));
	}
	
	public static File getChunkFile(ChunkCoord cCoord) {
		return new File(cCoord.worldDir, getChunkFileName(cCoord.chunkX,cCoord.vSlice,cCoord.chunkZ, cCoord.cType));
	}
	
	private static String getChunkFileName(Chunk chunk, boolean underground, int chunkY) {
		final int worldProviderType = chunk.worldObj.provider.dimensionId;
		final Constants.CoordType cType = Constants.CoordType.convert(underground, worldProviderType);
		return getChunkFileName(chunk.xPosition, underground ? chunkY : null, chunk.zPosition, cType);
	}
	
	private static String getChunkFileName(int x, Integer y, int z, Constants.CoordType cType) {
		StringBuffer sb = new StringBuffer();
		sb.append(x).append(",").append(z); //$NON-NLS-1$
		sb.append(getChunkFileSuffix(cType));
		return sb.toString();
	}
	
	static String getChunkFileSuffix(Constants.CoordType cType) {
		StringBuffer sb = new StringBuffer();
		switch(cType) {
			case Nether : {
				sb.append("_nether"); //$NON-NLS-1$
				break;
			}
			case Cave : {
				sb.append("_cave"); //$NON-NLS-1$
				break;
			}
			case End : {
				sb.append("_end"); //$NON-NLS-1$
				break;
			}
			case Other : {
				sb.append("_other"); //$NON-NLS-1$
				break;
			}
			case OtherCave : {
				sb.append("_othercave"); //$NON-NLS-1$
				break;
			}
			default : {
				// Normal
				break;
			}
		}
		sb.append(".chunk.png"); //$NON-NLS-1$
		return sb.toString();
	}
	
	/**
	 * Used by ChunkServlet to put chunk images together into what the browser needs.
	 * @param worldDir
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param mapType
	 * @param depth
	 * @throws IOException
	 */
	static synchronized BufferedImage getChunkImage(ChunkCoord cCoord, File chunkFile)
			throws IOException {

		long start = 0, stop = 0;

		boolean isUnderground = cCoord.vSlice!=null;
			
		BufferedImage chunkImage = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = chunkImage.createGraphics();

		int tries = 0;
		final int maxTries = 4;
		
		readimg : while(tries<maxTries) {
			tries++;

				try {
					FileInputStream fis = new FileInputStream(chunkFile);
					FileChannel fc = fis.getChannel();
					chunkImage = ImageIO.read(fis);
					fis.close();
					break readimg;
				} catch (FileNotFoundException e) {
					JourneyMap.getLogger().log(Level.WARNING, "Missing image file (FileNotFoundException):" + chunkFile, LogFormatter.toString(e)); //$NON-NLS-1$
					break readimg;
				} catch (javax.imageio.IIOException e) {
					// Bad image file
					if(tries>=maxTries) {
						JourneyMap.getLogger().log(Level.WARNING, "Bad image file (IIOException):" + chunkFile, LogFormatter.toString(e)); //$NON-NLS-1$
						chunkFile.delete();
					}
				} catch (java.lang.IndexOutOfBoundsException e) {
					// Badly sized image file
					if(tries>=maxTries) {
						JourneyMap.getLogger().log(Level.WARNING, "Can't write to file (IndexOutOfBounds):" + chunkFile); //$NON-NLS-1$
						chunkFile.delete();
					}
				} catch (Throwable t) {
					JourneyMap.getLogger().throwing("ImageHelper", "mergeImageChunks: " + chunkFile, t); //$NON-NLS-1$ //$NON-NLS-2$
					JourneyMap.getLogger().log(Level.SEVERE, t.getMessage(), t);
				}

		}

		return chunkImage;

	}
	
	public static Boolean absorbChunks(RegionCoord rCoord, BufferedImage regionImage) {
		
		// Check for chunks
		long start = System.currentTimeMillis();
		
		BufferedImage chunkImage, chunkDay, chunkNight, chunkUnder;
		ChunkCoord cCoord;
		File chunkFile;
		Graphics2D g2D = regionImage.createGraphics();
		g2D.setComposite(AlphaComposite.SrcOver);
		
		Boolean gotChunks = false;
		for(int x=rCoord.getMinChunkX();x<=rCoord.getMaxChunkX();x++) {
			for(int z=rCoord.getMinChunkZ();z<=rCoord.getMaxChunkZ();z++) {
				cCoord = ChunkCoord.fromChunkPos(rCoord.worldDir, x, rCoord.vSlice, z, rCoord.cType);
				synchronized(ChunkFileHandler.class) {
					try {
						chunkFile = ChunkFileHandler.getChunkFile(cCoord);
						if(chunkFile.exists()) {						
							chunkImage = ChunkFileHandler.getChunkImage(cCoord, chunkFile);
							if(!cCoord.isUnderground()) {
								chunkDay = chunkImage.getSubimage(0, 0, 16, 16);
								chunkNight = chunkImage.getSubimage(16, 0, 16, 16);								
								g2D.drawImage(chunkDay, cCoord.getXOffsetDay(), cCoord.getZOffsetDay(), 16,16,null);
								g2D.drawImage(chunkNight, cCoord.getXOffsetNight(), cCoord.getZOffsetNight(), 16,16,null);
							} else {
								int xoffset = cCoord.getVerticalSlice() * 16;
								chunkUnder = chunkImage.getSubimage(xoffset, 0, 16, 16);
								g2D.drawImage(chunkUnder, cCoord.getXOffsetUnderground(), cCoord.getZOffsetUnderground(), 16,16,null);
							}
							if(JourneyMap.getLogger().isLoggable(Level.FINEST)) {
								JourneyMap.getLogger().finest("Absorbed chunkfile: " + chunkFile); //$NON-NLS-1$
							}
							gotChunks = true;
							chunkFile.delete();
						}
					} catch(Throwable t) {
						JourneyMap.getLogger().severe("Error reading chunk file for " + cCoord + ": " + t); //$NON-NLS-1$ //$NON-NLS-2$
						JourneyMap.getLogger().severe(LogFormatter.toString(t));
					}
				}
			}
		}
		return gotChunks;
	}
	
	/**
	 * Used by ChunkServlet to put chunk images together into what the browser needs.
	 * @param worldDir
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param mapType
	 * @param depth
	 * @throws IOException
	 */
	static synchronized BufferedImage OLD_getMergedChunks(File worldDir, int x1, int z1,
			int x2, int z2, Constants.MapType mapType, int depth, int worldProviderType)
			throws IOException {

		long start = 0, stop = 0;
		

		int width = Math.max(16, (x2 - x1) * 16);
		int height = Math.max(16, (z2 - z1) * 16);
		boolean isUnderground = mapType.equals(Constants.MapType.underground);
			
		BufferedImage mergedImg = new BufferedImage(width, height, 0);
		Graphics2D g2D = mergedImg.createGraphics();

		// Determine offset
		int offset = isUnderground ? depth * 16 : mapType.offset();

		// Merge chunk images
		File chunkFile = null;
		Image image;
		for (int x = x1; x <= x2; x++) {
			if(x==x2 && (x1!=x2)) break;
			for (int z = z1; z <= z2; z++) {
				chunkFile = getChunkFile(worldDir, x, (isUnderground? depth : null), z, worldProviderType);
				int imageX = (x - x1) * 16;
				int imageZ = (z - z1) * 16;
				int tries = 0;
				final int maxTries = 4;
				
				readimg : while(tries<maxTries) {
					tries++;
					//if(tries>1) System.out.println("Trying on " + chunkFile + ": " + tries);
					if (chunkFile != null && chunkFile.exists()
							&& chunkFile.canRead()) {
						try {
							FileInputStream fis = new FileInputStream(chunkFile);
							FileChannel fc = fis.getChannel();
							image = ImageIO.read(fis);
							g2D.drawImage(image, imageX, imageZ, imageX + 16, imageZ + 16, offset, 0, offset + 16, 16, null);
							fis.close();
							
							// Success!
							//lastRequest = requestStr;
							break readimg;
						} catch (FileNotFoundException e) {
							JourneyMap.getLogger().log(Level.WARNING, "Missing image file (FileNotFoundException):" + chunkFile, LogFormatter.toString(e)); //$NON-NLS-1$
							break readimg;
						} catch (javax.imageio.IIOException e) {
							// Bad image file
							if(tries>=maxTries) {
								JourneyMap.getLogger().log(Level.WARNING, "Bad image file (IIOException):" + chunkFile, LogFormatter.toString(e)); //$NON-NLS-1$
								chunkFile.delete();
							}
						} catch (java.lang.IndexOutOfBoundsException e) {
							// Badly sized image file
							if(tries>=maxTries) {
								JourneyMap.getLogger().log(Level.WARNING, "Can't write to file (IndexOutOfBounds):" + chunkFile); //$NON-NLS-1$
								chunkFile.delete();
							}
						} catch (Throwable t) {
							JourneyMap.getLogger().throwing("ImageHelper", "mergeImageChunks: " + chunkFile, t); //$NON-NLS-1$ //$NON-NLS-2$
							JourneyMap.getLogger().log(Level.SEVERE, t.getMessage(), t);
						}
					} else { 
						image = null; // getPlaceholderChunk();
						g2D.drawImage(image, imageX, imageZ, imageX + 16, imageZ + 16, offset, 0, offset + 16, 16, null);
						break readimg;
					}
				}
			}
		}

		return mergedImg;

	}
	
	public static class ChunkFileFilter implements FilenameFilter {
		
		final String chunkName;
		
		public ChunkFileFilter(final Constants.CoordType cType) {
			chunkName = ChunkFileHandler.getChunkFileSuffix(cType);
		}
		
		@Override
		public boolean accept(File arg0, String arg1) {
			return arg1.endsWith(chunkName);
		}	
	}

}
