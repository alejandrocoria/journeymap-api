package net.techbrew.mcjm.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.techbrew.mcjm.Constants;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class MessagesData implements IDataProvider {
	
	private static final String KEY_PREFIX = "WebMap."; //$NON-NLS-1$
	
	private static long TTL = TimeUnit.DAYS.toMillis(1);
	
	public static enum Key {
		messages;
	}

	/**
	 * Constructor.
	 */
	public MessagesData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	public Enum[] getKeys() {
		return Key.values();
	}
		
	/**
	 * Return map of web-UI messages.
	 */
	public Map getMap() {		
		
		LinkedHashMap props = new LinkedHashMap();
		
		props.put("locale", Constants.getLocale());
		ResourceBundle bundle = Constants.getResourceBundle();

		Set<String> allKeys = Constants.getBundleKeys();
		for(String key : allKeys) {
			if(key.startsWith(KEY_PREFIX)) {
				String name = key.split(KEY_PREFIX)[1];
				String value = Constants.getString(key, bundle);
				props.put(name, value);
			}
		}

		return props;
	}
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
	
}
