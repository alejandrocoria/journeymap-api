package net.techbrew.mcjm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;

public class PropertyManager {

	private static PropertyManager instance;
	
	public static final String FILE_NAME = "journeyMap.properties"; //$NON-NLS-1$
	public static final String OLD_FILE_NAME = "webserver.properties"; //$NON-NLS-1$

	public static final String MAPGUI_ENABLED_PROP = "mapgui.enabled"; //$NON-NLS-1$
	private static final String MAPGUI_ENABLED_VALUE = "true"; //$NON-NLS-1$
	
	public static final String WEBSERVER_ENABLED_PROP = "webserver.enabled"; //$NON-NLS-1$
	private static final String WEBSERVER_ENABLED_VALUE = "true"; //$NON-NLS-1$
	
	private static final String WEBSERVER_PORT_LEGACY_PROP = "port"; //$NON-NLS-1$
	public static final String WEBSERVER_PORT_PROP = "webserver.port"; //$NON-NLS-1$
	private static final String WEBSERVER_PORT_VALUE = "8080"; //$NON-NLS-1$
	
	public static final String CHUNK_OFFSET_PROP = "chunk_offset"; //$NON-NLS-1$
	private static final String CHUNK_OFFSET_VALUE = "5"; //$NON-NLS-1$
	
	public static final String UPDATETIMER_PLAYER_PROP = "update_timer.entities"; //$NON-NLS-1$
	private static final String UPDATETIMER_PLAYER_VALUE = "1000"; //$NON-NLS-1$
	
	public static final String UPDATETIMER_CHUNKS_PROP = "update_timer.chunks"; //$NON-NLS-1$
	private static final String UPDATETIMER_CHUNKS_VALUE = "2000"; //$NON-NLS-1$
	
	public static final String BROWSER_POLL_PROP = "browser.poll"; //$NON-NLS-1$
	private static final String BROWSER_POLL_VALUE = "1900"; //$NON-NLS-1$
	
	public static final String MAPGUI_KEYCODE_PROP = "mapgui.keycode"; //$NON-NLS-1$
	private static final String MAPGUI_KEYCODE_VALUE = "36"; //$NON-NLS-1$
	
	public static final String LOGGING_LEVEL_PROP = "logging.level"; //$NON-NLS-1$
	private static final String LOGGING_LEVEL_VALUE = "INFO"; //$NON-NLS-1$
	
	public static final String CAVE_LIGHTING_PROP = "render.cavelighting.enabled"; //$NON-NLS-1$
	private static final String CAVE_LIGHTING_VALUE = "true"; //$NON-NLS-1$
	
	public static final String ANNOUNCE_MODLOADED_PROP = "announce.modloaded"; //$NON-NLS-1$
	private static final String ANNOUNCE_MODLOADED_VALUE = "true"; //$NON-NLS-1$
	

	
	private final Properties properties;
	private Boolean writeNeeded = false;
	
	public synchronized static PropertyManager getInstance() {
		if(instance==null) {
			instance = new PropertyManager();
		}
		return instance;
	}
	
	public String getString(String key) {
		return properties.getProperty(key);
	}
	
	public Integer getInteger(String key) {
		return Integer.parseInt(properties.getProperty(key));
	}
	
	public Boolean getBoolean(String key) {
		return Boolean.parseBoolean(properties.getProperty(key));
	}
	
	private Properties getDefaultProperties() {
		Properties defaults  = new Properties();
		defaults.put(MAPGUI_ENABLED_PROP, MAPGUI_ENABLED_VALUE);
		defaults.put(WEBSERVER_ENABLED_PROP, WEBSERVER_ENABLED_VALUE);
		defaults.put(WEBSERVER_PORT_PROP, WEBSERVER_PORT_VALUE);
		defaults.put(CHUNK_OFFSET_PROP, CHUNK_OFFSET_VALUE);
		defaults.put(UPDATETIMER_PLAYER_PROP, UPDATETIMER_PLAYER_VALUE);
		defaults.put(UPDATETIMER_CHUNKS_PROP, UPDATETIMER_CHUNKS_VALUE);		
		defaults.put(BROWSER_POLL_PROP, BROWSER_POLL_VALUE);		
		defaults.put(MAPGUI_KEYCODE_PROP, MAPGUI_KEYCODE_VALUE);
		defaults.put(LOGGING_LEVEL_PROP, LOGGING_LEVEL_VALUE);
		defaults.put(CAVE_LIGHTING_PROP, CAVE_LIGHTING_VALUE);
		defaults.put(ANNOUNCE_MODLOADED_PROP, ANNOUNCE_MODLOADED_VALUE);
		return defaults;
	}
	
	private PropertyManager() {
		properties = new Properties();
		readFromFile();
		addFromLegacyFile();
		Properties defaults = getDefaultProperties();
		for(Object key : defaults.keySet()) {
			if(!properties.containsKey(key)) {
				properties.put(key, defaults.get(key));
				writeNeeded = true;
			}
		}
		if(writeNeeded) {
			writeToFile();
		}
		
	}
	
	private void addFromLegacyFile() {
		Properties legacy = new Properties();
		File propFile = new File(FileHandler.getJourneyMapDir(), OLD_FILE_NAME);
		try {
			if(propFile.exists()) {
				// Prop file exists
				FileReader in = new FileReader(propFile);
				legacy.load(in);
				in.close();
				propFile.deleteOnExit();
				JourneyMap.getLogger().log(Level.INFO, "Removing obsolete property file: " + OLD_FILE_NAME); //$NON-NLS-1$
			}
		} catch(IOException e) {
			JourneyMap.getLogger().log(Level.SEVERE, "Can't use file: " + propFile.getAbsolutePath()); //$NON-NLS-1$
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(e));
		}
		if(legacy.containsKey(WEBSERVER_PORT_LEGACY_PROP)){
			properties.put(WEBSERVER_PORT_PROP, legacy.get(WEBSERVER_PORT_LEGACY_PROP));
			writeNeeded = true;
		}
	}
	
	private File getFile() {
		File propFile = new File(FileHandler.getJourneyMapDir(), FILE_NAME);
		return propFile;
	}
	
	private void readFromFile() {
		File propFile = getFile();
		if(!propFile.exists()) {
			JourneyMap.getLogger().log(Level.INFO, "Property file doesn't exist: " + propFile.getAbsolutePath()); //$NON-NLS-1$
			return;
		}
		try {
			FileReader in = new FileReader(propFile);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			String error = Constants.getMessageJMERR19(propFile.getAbsolutePath());
			JourneyMap.getLogger().severe(error);
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			throw new RuntimeException(error);
		}
	}
	
	private void writeToFile() {
		File propFile = getFile();
		try {
			FileHandler.getJourneyMapDir().mkdirs();
			FileWriter out = new FileWriter(propFile);
			properties.store(out, "Properties for JourneyMap " + JourneyMap.JM_VERSION); //$NON-NLS-1$
			out.close();
		} catch(IOException e) {
			String error = Constants.getMessageJMERR20(propFile.getAbsolutePath());
			JourneyMap.getLogger().severe(error);
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			throw new RuntimeException(error);
		}
	}
	
	public String toString() {
		return properties.toString();
	}

	
}
