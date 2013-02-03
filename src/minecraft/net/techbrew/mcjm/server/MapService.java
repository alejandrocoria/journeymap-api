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
import java.util.Arrays;
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
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.OldChunkRenderer;
import net.techbrew.mcjm.ui.ZoomLevel;

/**
 * Provides a map image by combining region files.
 * @author mwoodman
 *
 */
public class MapService extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$	

	/**
	 * Serves chunk data and player info.
	 */
	public MapService() {
		super();
	}
	
	@Override
	public String path() {
		return "/map.png"; //$NON-NLS-1$
	}
		
	@Override
	public void filter(Event event) throws Event, Exception {
		
		long start=System.currentTimeMillis();

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
		
		// Ensure world dir is found
		File worldDir = FileHandler.getWorldDir(minecraft);
		if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR06("worldDir=" + worldDir.getAbsolutePath()); //$NON-NLS-1$
			throwEventException(400, error, event, true);
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(event.query(), "depth", 4); //$NON-NLS-1$
		
		// Read coordinate pairs
		try {
			int x1 = getParameter(query, "x1", 0); //$NON-NLS-1$
			int z1 = getParameter(query, "z1", 0); //$NON-NLS-1$
			int x2 = getParameter(query, "x2", x1); //$NON-NLS-1$
			int z2 = getParameter(query, "z2", z1); //$NON-NLS-1$
			
			if (x1 >= x2 || z1 >= z2) {
				String error = Constants.getMessageJMERR05("coordinates=" + x1 + "," + z1 + "," + x2 + "," + z2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				throwEventException(400, error, event, true);
			}
			
			int width = getParameter(query, "width", 100); //$NON-NLS-1$
			int height = getParameter(query, "height", 100); //$NON-NLS-1$
			int worldProviderType = getParameter(query, "worldProviderType", 0);  //$NON-NLS-1$
			
			// Map type
			String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				throwEventException(400, error, event, true);
			}
			
			// Return the images
			ResponseHeader.on(event).noCache().contentType(ContentType.png);
			mergeImageChunks(event, worldDir, x1, z1, x2, z2, mapType, depth, worldProviderType, width, height);
			
			long stop=System.currentTimeMillis();
			if(JourneyMap.getLogger().isLoggable(Level.INFO)) {
				JourneyMap.getLogger().info((stop-start) + "ms to serve map.png");
			}
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
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
		
		start = System.currentTimeMillis();
		
		// Headers
		event.reply().header("Content-Type:","image/png"); //$NON-NLS-1$
		event.reply().header("Content-Disposition", "inline; filename=\"jm.png\"");	 //$NON-NLS-1$ //$NON-NLS-2$

		ImageOutputStream ios = ImageIO.createImageOutputStream(event.output());
		BufferedImage mergedImg = RegionFileHandler.getMergedChunks(worldDir, x1, z1, x2, z2, mapType, depth, cType, true, 
				new ZoomLevel(1F, 1, false, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));

		ImageIO.write(mergedImg, "png", ios); //$NON-NLS-1$
		
		ios.flush();
		ios.close();
		
		if(JourneyMap.getLogger().isLoggable(Level.INFO)) {
			stop = System.currentTimeMillis();
			//JourneyMap.getLogger().info("mergeImageChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
