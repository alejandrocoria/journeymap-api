package net.techbrew.mcjm.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

public class PropertyManager {

	private static PropertyManager instance;
	
	public static final String FILE_NAME = "journeyMap.properties"; //$NON-NLS-1$
	
	public enum Key {
		MAPGUI_ENABLED("mapgui_enabled", true), //$NON-NLS-1$
		WEBSERVER_ENABLED("webserver_enabled", true), //$NON-NLS-1$
		WEBSERVER_PORT("webserver_port", 8080), //$NON-NLS-1$
		CHUNK_OFFSET("chunk_offset", 5), //$NON-NLS-1$
		BROWSER_POLL("browser_poll", 1900), //$NON-NLS-1$
		UPDATETIMER_PLAYER("update_timer_entities", 1000), //$NON-NLS-1$
		UPDATETIMER_CHUNKS("update_timer_chunks",2000), //$NON-NLS-1$
		MAPGUI_KEYCODE("mapgui_keycode",36), //$NON-NLS-1$
		LOGGING_LEVEL("logging_level", "INFO"), //$NON-NLS-1$  //$NON-NLS-2$
		CAVE_LIGHTING("render_cavelighting_enabled",true), //$NON-NLS-1$
		ANNOUNCE_MODLOADED("announce_modloaded", true), //$NON-NLS-1$
		UPDATE_CHECK_ENABLED("update_check_enabled", true), //$NON-NLS-1$
		
		PREF_SHOW_CAVES("preference_show_caves", true), //$NON-NLS-1$
		PREF_SHOW_MOBS("preference_show_mobs", true), //$NON-NLS-1$
		PREF_SHOW_ANIMALS("preference_show_animals", true), //$NON-NLS-1$
		PREF_SHOW_VILLAGERS("preference_show_villagers", true), //$NON-NLS-1$
		PREF_SHOW_PETS("preference_show_pets", true), //$NON-NLS-1$
		PREF_SHOW_PLAYERS("preference_show_players", true); //$NON-NLS-1$
		
		private final String property;
		private final String defaultValue;
		private final boolean isBoolean;
		private final boolean isNumeric;
		
		private Key(String property, Object defaultValue) {
			this.property = property;
			isNumeric = (defaultValue instanceof Number);
			isBoolean = (defaultValue instanceof Boolean);
			this.defaultValue = defaultValue.toString();
		}
		
		public String getProperty() {
			return property;
		}
		
		String getDefault() {
			return defaultValue;
		}
		
		public boolean isBoolean() {
			return isBoolean;
		}
		
		public boolean isNumeric() {
			return isNumeric;
		}
		
		public static Key lookup(String propName) {
			for(Key key : Key.values()) {
				if(key.getProperty().equals(propName)) {
					return key;
				}
			}
			return null;
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
	
	public void setProperty(Key key, Object value) {
		properties.setProperty(key.getProperty(), value.toString());
		writeToFile();
	}
	
	/**
	 * Get a normalized, type-safe view of the properties.
	 * @return
	 */
	public Map<String, Object> getProperties() {
		HashMap<String, Object> map = new HashMap<String,Object>(properties.size());
		for(Key key : Key.values()) {
			if(key.isBoolean) {
				map.put(key.getProperty(), getBoolean(key));
			} else if(key.isNumeric) {
				map.put(key.getProperty(), getInteger(key));
			} else {
				map.put(key.getProperty(), getString(key));
			}
		}
		return map;
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
		
		// Convert older files if needed
		HashMap<String, String> temp = new HashMap(properties);
		for(Map.Entry<String, String> entry: temp.entrySet()) {
			if(entry.getKey().contains(".")) {
				writeNeeded = true;
				properties.put(entry.getKey().replaceAll("\\.", "_"), entry.getValue());	
				properties.remove(entry.getKey());
			}
		}
		
		if(writeNeeded) {
			JourneyMap.getLogger().info("Updated property names to use _ instead of .");
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
