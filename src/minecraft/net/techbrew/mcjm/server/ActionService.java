package net.techbrew.mcjm.server;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.task.MapRegionTask;
import net.techbrew.mcjm.task.SaveMapTask;
import se.rupy.http.Event;
import se.rupy.http.Query;
/**
 * Service delegate for special actions.
 *
 */
public class ActionService extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
	private static boolean debug = true;

	/**
	 * Serves chunk data and player info.
	 */
	public ActionService() {
		super();
	}

	@Override
	public String path() {
		return "/action"; //$NON-NLS-1$
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
		
		// Use type param to delegate
		String type = getParameter(query, "type", (String) null); //$NON-NLS-1$
		if("savemap".equals(type)) {
			saveMap(event);
		} else if("automap".equals(type)) {
			autoMap(event);
		} else {
			String error = Constants.getMessageJMERR05("type=" + type); //$NON-NLS-1$
			throwEventException(400, error, event, true);
		}		
		
	}
	
	/**
	 * Save a map of the world at the current dimension, vSlice and map type.
	 * 
	 * @param event
	 * @throws Event
	 * @throws Exception
	 */
	private void saveMap(Event event) throws Event, Exception {
		
		Query query = event.query();
		
		Minecraft minecraft = Minecraft.getMinecraft();
		World theWorld = minecraft.theWorld; 

		
		try {
			
			// Ensure world dir available
			File worldDir = FileHandler.getJMWorldDir(minecraft);
			if (!worldDir.exists() || !worldDir.isDirectory()) {
				String error = Constants.getMessageJMERR10(worldDir.getAbsolutePath());			
			}
			
			Integer vSlice = getParameter(query, "depth", (Integer) null); //$NON-NLS-1$		
			final int dimension = getParameter(query, "dim", 0);  //$NON-NLS-1$
			final String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				throwEventException(400, error, event, true);
			}
			if(mapType!=MapType.underground) {
				vSlice = null;
			}
			
			// Validate cave mapping allowed
			// Check for hardcore			
			Boolean hardcore = !minecraft.isSingleplayer() && theWorld.getWorldInfo().isHardcoreModeEnabled();
			if(mapType.equals(Constants.MapType.underground) && hardcore) {
				String error = "Cave mapping on hardcore servers is not allowed"; //$NON-NLS-1$
				throwEventException(403, error, event, true);
			}			

			// Check estimated file size
			MapSaver mapSaver = new MapSaver(worldDir, mapType, vSlice, dimension);			
			if(!mapSaver.isValid()) {
				throwEventException(403, "No image files to save.", event, true);
			} 
			JourneyMap.getInstance().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
			
			Properties response = new Properties();
			response.put("filename", mapSaver.getSaveFileName());
			respondJson(event, response);
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} catch (Event eventEx ) {
			throw eventEx;
		} catch(Throwable t) {				
			JourneyMap.getLogger().severe(LogFormatter.toString(t));			
			throwEventException(500, Constants.getMessageJMERR19(path), event, true);
		}
	}
	
	/**
	 * Automap the world at the current dimension and vSlice.
	 * 
	 * @param event
	 * @throws Event
	 * @throws Exception
	 */
	private void autoMap(Event event) throws Event, Exception {
		
		boolean enabled = PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED);
		String scope = getParameter(event.query(), "scope", "stop");
		
		HashMap responseObj = new HashMap();
		
		if("stop".equals(scope)) {
			PropertyManager.getInstance().setProperty(PropertyManager.Key.AUTOMAP_ENABLED, false);
			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, false, Boolean.FALSE);			
			responseObj.put("message","automap_complete");			
		} else {
			boolean doAll = "all".equals(scope);
			PropertyManager.getInstance().setProperty(PropertyManager.Key.AUTOMAP_ENABLED, true);
			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, doAll);
			responseObj.put("message","automap_started");			
		} 
		
		respondJson(event, responseObj);
	}

}
