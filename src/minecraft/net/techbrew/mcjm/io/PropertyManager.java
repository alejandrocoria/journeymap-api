package net.techbrew.mcjm.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

public class PropertyManager {

	private static PropertyManager instance;
	
	public static final String FILE_NAME = "journeyMap.properties"; //$NON-NLS-1$
	
	public enum Key {
		MAPGUI_ENABLED("mapgui.enabled", true), //$NON-NLS-1$
		WEBSERVER_ENABLED("webserver.enabled", true), //$NON-NLS-1$
		WEBSERVER_PORT("webserver.port", 8080), //$NON-NLS-1$
		CHUNK_OFFSET("chunk_offset", 5), //$NON-NLS-1$
		BROWSER_POLL("browser.poll", 1900), //$NON-NLS-1$
		UPDATETIMER_PLAYER("update_timer.entities", 1000), //$NON-NLS-1$
		UPDATETIMER_CHUNKS("update_timer.chunks",2000), //$NON-NLS-1$
		MAPGUI_KEYCODE("mapgui.keycode",36), //$NON-NLS-1$
		LOGGING_LEVEL("logging.level", "INFO"), //$NON-NLS-1$  //$NON-NLS-2$
		CAVE_LIGHTING("render.cavelighting.enabled",true), //$NON-NLS-1$
		ANNOUNCE_MODLOADED("announce.modloaded", true), //$NON-NLS-1$
		UPDATE_CHECK_ENABLED("update_check.enabled", true); //$NON-NLS-1$
		
		private final String property;
		private final String defaultValue;
		private Key(String property, Object defaultValue) {
			this.property = property;
			this.defaultValue = defaultValue.toString();
		}
		
		String getProperty() {
			return property;
		}
		
		String getDefault() {
			return defaultValue;
		}
	}
	
	private final Properties properties;
	private Boolean writeNeeded = false;
	
	public synchronized static PropertyManager getInstance() {
		if(instance==null) {
			instance = new PropertyManager();
		}
		return instance;
	}
	
	public String getString(Key key) {
		return properties.getProperty(key.getProperty());
	}
	
	public Integer getInteger(Key key) {
		return Integer.parseInt(properties.getProperty(key.getProperty()));
	}
	
	public Boolean getBoolean(Key key) {
		return Boolean.parseBoolean(properties.getProperty(key.getProperty()));
	}
	
	private Properties getDefaultProperties() {
		Properties defaults  = new Properties();
		for(Key key : Key.values()) {
			defaults.put(key.getProperty(), key.getDefault());
		}
		return defaults;
	}
	
	private PropertyManager() {
		properties = new Properties();
		readFromFile();
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
