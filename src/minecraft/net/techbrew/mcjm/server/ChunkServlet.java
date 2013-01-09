package net.techbrew.mcjm.server;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import se.rupy.http.Event;
import se.rupy.http.Output;
import se.rupy.http.Query;
import se.rupy.http.Service;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.Constants.WorldType;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PlayerDataFileHandler;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.ChunkRenderer;
import net.techbrew.mcjm.ui.ZoomLevel;


public class ChunkServlet extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$	

	/**
	 * Serves chunk data and player info.
	 */
	public ChunkServlet() {
		super();
	}
	
	@Override
	public String path() {
		return "/jm"; //$NON-NLS-1$
	}
	
	
	
	@Override
	public void filter(Event event) throws Event, Exception {

		// Parse query for parameters
		Query query = event.query();
		query.parse();

		Minecraft minecraft = Minecraft.getMinecraft();
		World theWorld = minecraft.theWorld;
		if (theWorld == null) {
			throwEventException(503, Constants.getMessageJMERR09(), event, false);
		}
		
		// Ensure world is loaded
		if(!JourneyMap.isRunning()) {
			throwEventException(503, Constants.getMessageJMERR02(), event, false);
		}
		
		File worldDir = FileHandler.getWorldDir(minecraft);

		// Check world requested
		String worldName = getParameter(query, "worldName", null); //$NON-NLS-1$
		if (worldName == null) {
			servePlayerData(event, worldDir);
			if(JourneyMap.getLogger().isLoggable(Level.FINER)) {
				JourneyMap.getLogger().finer("Request: " + event.query().path()); //$NON-NLS-1$
			}
			return;
		}
		worldName = URLDecoder.decode(worldName, "UTF-8"); //$NON-NLS-1$
		
		// Check world type
		String worldTypeString = getParameter(query, "worldType", Constants.WorldType.sp.name()); //$NON-NLS-1$
		Constants.WorldType worldType = null;
		try {
			worldType = Constants.WorldType.valueOf(worldTypeString);
		} catch (Exception e) {			
			String error = Constants.getMessageJMERR05("worldType=" + worldTypeString); //$NON-NLS-1$
			throwEventException(400, Constants.getMessageJMERR09(), event, true);
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(event.query(), "depth", 4); //$NON-NLS-1$
		
		if(!worldName.equals(URLDecoder.decode(FileHandler.getSafeName(ModLoader.getMinecraftInstance())))) {
			String error = Constants.getMessageJMERR10("worldType=" + worldType); //$NON-NLS-1$
			throwEventException(503, Constants.getMessageJMERR09(), event, true);

		}  else if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR06("worldDir=" + worldDir.getAbsolutePath()); //$NON-NLS-1$
			throwEventException(400, Constants.getMessageJMERR09(), event, true);
		}
		
		// Read coordinate pairs
		try {
			int x1 = getParameter(query, "x1", 0); //$NON-NLS-1$
			int z1 = getParameter(query, "z1", 0); //$NON-NLS-1$
			int x2 = getParameter(query, "x2", x1); //$NON-NLS-1$
			int z2 = getParameter(query, "z2", z1); //$NON-NLS-1$
			int width = getParameter(query, "width", 100); //$NON-NLS-1$
			int height = getParameter(query, "height", 100); //$NON-NLS-1$
			int worldProviderType = getParameter(query, "worldProviderType", 0);  //$NON-NLS-1$
			String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				throwEventException(400, Constants.getMessageJMERR09(), event, true);
			}
			
			if (x1 >= x2 || z1 >= z2) {
				String error = Constants.getMessageJMERR05("coordinates=" + x1 + "," + z1 + "," + x2 + "," + z2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				throwEventException(400, Constants.getMessageJMERR09(), event, true);
			} else {
				ResponseHeader.on(event).noCache().contentType(ContentType.png);
				mergeImageChunks(event, worldDir, x1, z1, x2, z2, mapType, depth, worldProviderType, width, height);
			}
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} finally {

		}

	}

	/**
	 * Respond with the player data file.
	 * @param event
	 * @param worldDir
	 * @throws Event
	 * @throws IOException
	 */
	private void servePlayerData(Event event, File worldDir) throws Event, IOException {
		File playerFile = PlayerDataFileHandler.getPlayerFile(ModLoader.getMinecraftInstance());
		if (playerFile!=null && playerFile.exists() && playerFile.canRead()) {			
			ResponseHeader.on(event).noCache();
			FileServlet.serveFile(playerFile, event);
		} else {
			throwEventException(400, Constants.getMessageJMERR11(playerFile), event, false);
		}
	}
	

	/**
	 * Used by ChunkServlet to put chunk images together into what the browser needs.
	 * @param request
	 * @param response
	 * @param worldDir
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param mapType
	 * @param depth
	 * @throws IOException
	 */
	private static synchronized void mergeImageChunks(Event event, File worldDir, int x1, int z1,
			int x2, int z2, Constants.MapType mapType, int depth, int worldProviderType, int canvasWidth, int canvasHeight)
			throws IOException {
		
		long start = 0, stop = 0;
		final Constants.CoordType cType = Constants.CoordType.convert(mapType, worldProviderType);
		
		if(JourneyMap.getLogger().isLoggable(Level.FINEST)) {
			start = System.currentTimeMillis();
		}
		
		// Headers
		event.reply().header("Content-Type:","image/png"); //$NON-NLS-1$
		event.reply().header("Content-Disposition", "inline; filename=\"jm.png\"");	 //$NON-NLS-1$ //$NON-NLS-2$

		ImageOutputStream ios = ImageIO.createImageOutputStream(event.output());
		BufferedImage mergedImg = RegionFileHandler.getMergedChunks(worldDir, x1, z1, x2, z2, mapType, depth, cType, false, 
				new ZoomLevel(1F, 1, false, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		
//		if(mergedImg.getWidth()!=canvasWidth) {
//			Image scaled = mergedImg.getScaledInstance(canvasWidth, -1, Image.SCALE_SMOOTH);					
//			BufferedImage temp = new BufferedImage(mergedImg.getWidth(null), mergedImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//			Graphics2D g = temp.createGraphics();
//			g.drawImage(scaled, 0, 0, null);
//			mergedImg = temp;
//		} 
		
		ImageIO.write(mergedImg, "png", ios); //$NON-NLS-1$
		
		ios.flush();
		ios.close();
		
		if(JourneyMap.getLogger().isLoggable(Level.FINEST)) {
			stop = System.currentTimeMillis();
			JourneyMap.getLogger().finest("mergeImageChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
