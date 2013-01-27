package net.techbrew.mcjm.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.ChunkRenderer;

/**
 * Provides a map of the entire world
 * @author mwoodman
 *
 */
public class SaveMapService extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String PNG_CONTENT_TYPE = "image/png"; //$NON-NLS-1$
	private static boolean debug = true;

	/**
	 * Serves chunk data and player info.
	 */
	public SaveMapService() {
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
		
		// Ensure world dir available
		File worldDir = FileHandler.getWorldDir(minecraft);
		if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR10(worldDir.getAbsolutePath());			
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(query, "depth", 4); //$NON-NLS-1$
		
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
			// Check for hardcore
			Boolean hardcore = !minecraft.isSingleplayer() && theWorld.getWorldInfo().isHardcoreModeEnabled();
			if(mapType.equals(Constants.MapType.underground) && hardcore) {
				String error = "Cave mapping on hardcore servers is not allowed"; //$NON-NLS-1$
				throwEventException(403, error, event, false);
			}			

			// Get image
			final Constants.CoordType cType = Constants.CoordType.convert(mapType, worldProviderType);
			BufferedImage mapImg = MapSaver.saveMap(worldDir, mapType, depth, cType);
			
			// Get save-as name
			StringBuffer sb = new StringBuffer(WorldData.getWorldName(minecraft));
			sb.append("_").append(mapType).append("_"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(cType).append(ContentType.png);
			String saveName = URLEncoder.encode(sb.toString(), CHARACTER_ENCODING);
					
			// Set response headers
			ResponseHeader.on(event).noCache().inlineFilename(saveName).contentType(ContentType.png);
			
			// Write image to output
			ImageIO.write(mapImg, "png", event.output()); //$NON-NLS-1$
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} catch (Event eventEx ) {
			throw eventEx;
		} catch(Throwable t) {				
			JourneyMap.getLogger().severe(LogFormatter.toString(t));			
			throwEventException(500, Constants.getMessageJMERR19(path), event, true);
		}
	}

}
