package net.techbrew.mcjm.server;

import java.io.File;
import java.io.FileInputStream;

import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.log.LogFormatter;
import se.rupy.http.Deploy;
import se.rupy.http.Event;
import se.rupy.http.Query;
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
		Minecraft minecraft = Minecraft.getMinecraft();
		World theWorld = minecraft.theWorld; 
		if (theWorld == null) {
			throwEventException(503, Constants.getMessageJMERR09(), event, false);
		}
		
		// Ensure world is loaded
		if(!JourneyMap.getInstance().isMapping()) {
			throwEventException(503, Constants.getMessageJMERR02(), event, false);
		}
		
		// Ensure world dir available
		File worldDir = FileHandler.getJMWorldDir(minecraft);
		if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR10(worldDir.getAbsolutePath());			
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(query, "depth", 4); //$NON-NLS-1$
		
		try {
			int dimension = getParameter(query, "worldProviderType", 0);  //$NON-NLS-1$
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
			File mapFile = MapSaver.lightWeightSaveMap(worldDir, mapType, depth, dimension);	
					
			// Set response headers
			ResponseHeader.on(event).noCache().content(mapFile);			
			FileInputStream fis = new FileInputStream(mapFile);
			
			// Write image to output
			try {
				Deploy.pipe(fis, event.reply().output(mapFile.length()));
			} finally {
				fis.close();
			}
			
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
