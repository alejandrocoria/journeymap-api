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
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.GameData;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.TimeData;
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

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$	

	/**
	 * Serves chunk data and player info.
	 */
	public DataService() {
		super();
	}
	
	@Override
	public String path() {
		return "/data/game:/data/player:/data/time:/data/world:/data/animals:/data/mobs:/data/players"; //$NON-NLS-1$
	}
	
	@Override
	public void filter(Event event) throws Event, Exception {
		
		// Ensure JourneyMap and World is loaded
		if(!JourneyMap.isRunning()) {
			throwEventException(503, Constants.getMessageJMERR02(), event, false);
		} else if(Minecraft.getMinecraft().theWorld==null) {
			throwEventException(503, Constants.getMessageJMERR09(), event, false);
		}

		// Parse query for parameters
		Query query = event.query();
		String path = query.path();
		
		String jsonData = null;
		
		if(path.endsWith("game")) {
			jsonData = DataCache.instance().getJson(GameData.class);
		} else if(path.endsWith("player")) {
			jsonData = DataCache.instance().getJson(PlayerData.class);
		} else if(path.endsWith("time")) {
			jsonData = DataCache.instance().getJson(TimeData.class);
		} else if(path.endsWith("world")) {
			jsonData = DataCache.instance().getJson(WorldData.class);
		} else if(path.endsWith("mobs")) {
			jsonData = DataCache.instance().getJson(MobsData.class);
		} else if(path.endsWith("animals")) {
			jsonData = DataCache.instance().getJson(AnimalsData.class);
		} else if(path.endsWith("players")) {
			jsonData = DataCache.instance().getJson(PlayersData.class);
		} else {
			throw new IllegalArgumentException("Couldn't handle path: " + path);
		}
		
		byte[] jsonBytes = jsonData.getBytes();		
		
		// TODO: Use TTL for expiring data
		ResponseHeader.on(event).noCache().contentType(ContentType.json).contentLength(jsonBytes.length);		
		event.output().write(jsonBytes); 
	}
	
}
