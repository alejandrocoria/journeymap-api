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
import net.techbrew.mcjm.data.AllData;
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.GameData;
import net.techbrew.mcjm.data.IDataProvider;
import net.techbrew.mcjm.data.MessagesData;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.JsonHelper;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.server.BaseService.ContentType;
import net.techbrew.mcjm.server.BaseService.ResponseHeader;
import net.techbrew.mcjm.ui.ZoomLevel;

/**
 * Provide player data
 * 
 * @author mwoodman
 *
 */
public class PropertyService extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CALLBACK_PARAM = "callback";  //$NON-NLS-1$	
	
	public static final String BASEPATH = "/property"; //$NON-NLS-1$	
	
	public static final String combinedPath;
	
	static {
		// Compose path string used by RupyService
		StringBuffer sb = new StringBuffer();
		sb.append(BASEPATH).append(":"); //$NON-NLS-1$	
		
		String keyProp;
		for(PropertyManager.Key key : PropertyManager.Key.values()) {
			keyProp = key.getProperty();			
			sb.append(BASEPATH).append("/").append(keyProp).append(":"); //$NON-NLS-1$	//$NON-NLS-2$	
		}
		combinedPath = sb.toString();
	}
	
	/**
	 * Serves / saves property info
	 */
	public PropertyService() {
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
		
		if(query.method()==Query.PUT) {
			post(event);
			return;
		} else if(query.method()==Query.PUT) {
			throw new Exception("HTTP method not allowed");
		}
		
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
		
		if(path.equals(BASEPATH)) {		
			jsonData.append(JsonHelper.toJson(PropertyManager.getInstance().getProperties()));
		} else {
			String key = path.split(BASEPATH)[1].split("/")[1];
			String value = JsonHelper.toJson(PropertyManager.getInstance().getProperties().get(key));
			jsonData.append("{").append(key).append(":").append(value).append("}");
		}		
		
		// Finish function call for JsonP if needed
		if(useJsonP) {
			jsonData.append(")"); //$NON-NLS-1$
		}
		
		// Optimize headers for JSONP
		ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
				
		// Gzip response
		gzipResponse(event, jsonData.toString());
	}
	
	public void post(Event event) throws Event, Exception {
		
		
	}
	
}
