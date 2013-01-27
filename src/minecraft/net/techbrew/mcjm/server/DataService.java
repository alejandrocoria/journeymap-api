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
import java.net.URLEncoder;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
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
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.GameData;
import net.techbrew.mcjm.data.IDataProvider;
import net.techbrew.mcjm.data.MessagesData;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.TimeData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.ChunkRenderer;
import net.techbrew.mcjm.server.BaseService.ContentType;
import net.techbrew.mcjm.server.BaseService.ResponseHeader;
import net.techbrew.mcjm.ui.ZoomLevel;

/**
 * Provide player data
 * 
 * @author mwoodman
 *
 */
public class DataService extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CALLBACK_PARAM = "callback";  //$NON-NLS-1$	
	
	public static final String combinedPath;
	
	public static final HashMap<String,Class<? extends IDataProvider>> providerMap;
	static {
		providerMap = new HashMap<String,Class<? extends IDataProvider>>(8);
		providerMap.put("/data/animals", AnimalsData.class);  //$NON-NLS-1$
		providerMap.put("/data/mobs", MobsData.class);  //$NON-NLS-1$
		providerMap.put("/data/game", GameData.class);  //$NON-NLS-1$
		providerMap.put("/data/messages", MessagesData.class);  //$NON-NLS-1$
		providerMap.put("/data/player", PlayerData.class);  //$NON-NLS-1$
		providerMap.put("/data/players", PlayersData.class);  //$NON-NLS-1$
		providerMap.put("/data/time", TimeData.class);  //$NON-NLS-1$
		providerMap.put("/data/world", WorldData.class);  //$NON-NLS-1$
		providerMap.put("/data/villagers", VillagersData.class);  //$NON-NLS-1$
		
		// Compose path string used by RupyService
		StringBuffer sb = new StringBuffer();
		for(String key : providerMap.keySet()) {
			sb.append(key).append(":");
		}
		combinedPath = sb.toString();
	}
	
	
	
	/**
	 * Serves chunk data and player info.
	 */
	public DataService() {
		super();
	}
	
	@Override
	public String path() {
		return combinedPath;
	}
	
	@Override
	public void filter(Event event) throws Event, Exception {

		// Parse query for parameters
		Query query = event.query();
		query.parse();
		String path = query.path();
		
		// If not a request for L10N, check world/minecraft status
		if(!path.equals("/data/messages")) {		
			// Ensure JourneyMap and World is loaded
			if(!JourneyMap.isRunning()) {
				throwEventException(503, Constants.getMessageJMERR02(), event, false);
			} else if(Minecraft.getMinecraft().theWorld==null) {
				throwEventException(503, Constants.getMessageJMERR09(), event, false);
			}
		}
		
		// Get cached data provider keyed by the path
		Class<? extends IDataProvider> dpClass = providerMap.get(path);		
		
		// Build the response string
		StringBuffer jsonData = new StringBuffer();
				
		// Check for callback to determine Json or JsonP
		boolean useJsonP = query.containsKey(CALLBACK_PARAM);
		if(useJsonP) {
			jsonData.append(URLEncoder.encode(query.get(CALLBACK_PARAM).toString(), UTF8.name()));
			jsonData.append("("); //$NON-NLS-1$	
		} else {
			jsonData.append("data="); //$NON-NLS-1$	
		}	
		
		// Append the data
		DataCache.instance().appendJson(dpClass, jsonData);	
		
		// Finish function call for JsonP if needed
		if(useJsonP) {
			jsonData.append(")"); //$NON-NLS-1$
		}
		
		// Optimize headers for JSONP
		ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
				
		// Gzip response
		gzipResponse(event, jsonData.toString());
	}
	
}
