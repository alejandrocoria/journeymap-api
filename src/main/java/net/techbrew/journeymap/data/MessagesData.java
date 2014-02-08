package net.techbrew.journeymap.data;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.io.FileHandler;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

;

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
	
	private final String lang;

	/**
	 * Constructor.
	 */
	public MessagesData() {
		lang = Minecraft.getMinecraft().gameSettings.language;
	}
	
	/**
	 * Provides all possible keys.
	 */
	@Override
	public Enum[] getKeys() {
		return Key.values();
	}
		
	/**
	 * Return map of web-UI messages.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		LinkedHashMap props = new LinkedHashMap();
		
		props.put("locale", Constants.getLocale());
		Properties properties = FileHandler.getLangFile("en_US.lang");

		Enumeration<Object> allKeys = properties.keys();
        while(allKeys.hasMoreElements()){
            String key = (String) allKeys.nextElement();
			if(key.startsWith(KEY_PREFIX)) {
				String name = key.split(KEY_PREFIX)[1];
				String value = Constants.getString(key);
				props.put(name, value);
			}
		}

		return props;
	}
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	@Override
	public long getTTL() {
		return TTL;
	}
	
	/**
	 * Return true if language has changed.
	 */
	@Override
	public boolean dataExpired() {
		return !Minecraft.getMinecraft().gameSettings.language.equals(lang);
	}
}
