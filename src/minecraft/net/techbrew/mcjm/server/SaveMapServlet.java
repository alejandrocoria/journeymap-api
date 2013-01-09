package net.techbrew.mcjm.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import se.rupy.http.Event;
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
import net.techbrew.mcjm.render.ChunkRenderer;

public class SaveMapServlet extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String PNG_CONTENT_TYPE = "image/png"; //$NON-NLS-1$
	private static boolean debug = true;

	/**
	 * Serves chunk data and player info.
	 */
	public SaveMapServlet() {
		super();
	}

	@Override
	public String path() {
		return "/save"; //$NON-NLS-1$
	}
	
	@Override
	public void filter(Event event) throws Event, Exception {
		
		// Parse query for parameters
		Query query = event.query();
		query.parse();

		// Check world
		Minecraft minecraft = ModLoader.getMinecraftInstance();
		World theWorld = minecraft.theWorld; 
		if (theWorld == null) {
			throwEventException(503, Constants.getMessageJMERR09(), event, false);
		}
		
		// Ensure world is loaded
		if(!JourneyMap.isRunning()) {
			throwEventException(503, Constants.getMessageJMERR02(), event, false);
		}
		
		File worldDir = FileHandler.getWorldDir(minecraft);
		
		// Check for hardcore
		Boolean hardcore = !minecraft.isSingleplayer() && theWorld.getWorldInfo().isHardcoreModeEnabled();

		// Check world requested
		String worldName = getParameter(query, "worldName", null); //$NON-NLS-1$
		worldName = URLDecoder.decode(worldName, "UTF-8"); //$NON-NLS-1$
		
		// Check world type
		String worldTypeString = getParameter(query, "worldType", Constants.WorldType.sp.name()); //$NON-NLS-1$
		Constants.WorldType worldType;
		try {
			worldType = Constants.WorldType.valueOf(worldTypeString);
		} catch (Exception e) {
			String error = Constants.getMessageJMERR05("worldType=" + worldTypeString); //$NON-NLS-1$
			throwEventException(400, error, event, false);
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(query, "depth", 4); //$NON-NLS-1$
		
		if(!worldName.equals(URLDecoder.decode(FileHandler.getSafeName(ModLoader.getMinecraftInstance())))) {
			String error = Constants.getMessageJMERR10("worldType=" + worldTypeString); //$NON-NLS-1$
			throwEventException(503, error, event, true);

		}  else if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR10(worldDir.getAbsolutePath());
			
		}
		
		try {
			int worldProviderType = getParameter(query, "worldProviderType", 0);  //$NON-NLS-1$
			String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				throwEventException(400, error, event, true);
			}
			
			// Validate cave mapping allowed
			if(mapType.equals(Constants.MapType.underground) && hardcore) {
				String error = "Cave mapping on hardcore servers is not allowed"; //$NON-NLS-1$
				throwEventException(403, error, event, false);
			}			

			// Get image
			final Constants.CoordType cType = Constants.CoordType.convert(mapType, worldProviderType);
			BufferedImage mapImg = MapSaver.saveMap(worldDir, mapType, depth, cType);
			
			// Set response headers
			ResponseHeader.on(event).noCache().contentType(ContentType.png);
			
			// Write image to output
			ImageIO.write(mapImg, "png", event.output()); //$NON-NLS-1$
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} 
	}

}
