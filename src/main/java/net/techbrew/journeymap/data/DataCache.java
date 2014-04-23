package net.techbrew.journeymap.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Singleton cache of data produced by IDataProviders.
 * 
 * Uses on-demand-holder pattern for singleton management.
 * 
 * @author mwoodman
 *
 */
public class DataCache {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final ConcurrentHashMap<Class<? extends IDataProvider>, DataHolder> cache;
	
	// Private constructor
	private DataCache() {
		 cache = new ConcurrentHashMap<Class<? extends IDataProvider>, DataHolder>(8); // adjust size for # of IDataProvider impl classes
	}
	
	// On-demand-holder for instance
	private static class Holder {
        private static final DataCache INSTANCE = new DataCache();
    }

	// Get singleton instance.  Concurrency-safe.
    public static DataCache instance() {
        return Holder.INSTANCE;
    }
    
    /**
     * Empties the cache
     */
    public void purge() {
    	synchronized(cache) {
    		cache.clear();
    	}
    }

    public void forceRefresh(Class<? extends IDataProvider> dpClass) {
        synchronized(cache) {
            putInternal(dpClass, null);
        }
    }

    /**
     * Put the dataprovider's map into the cache, wrapped in a DataHolder 
     * and keyed by provider class.
     * @param dp
     */
    public void put(IDataProvider dp, Map optionalParams) {
    	putInternal(dp, optionalParams);
    }
    
    /**
     * Put an instance of the dataholder class into the cache, wrapped in a DataHolder 
     * and keyed by provider class.
     * @param dpClass
     */
    private DataHolder putInternal(Class<? extends IDataProvider> dpClass, Map optionalParams) {
    	try {
			return putInternal(dpClass.newInstance(), optionalParams);
    	} catch (Exception e) {
			// Shouldn't happen
			JourneyMap.getLogger().severe("Can't instantiate dataprovider " + dpClass + " for cache: " + LogFormatter.toString(e));
			throw new RuntimeException(e);
		}
    }
    
    /**
     * Put the dataprovider's map into the cache, wrapped in a DataHolder 
     * and keyed by provider class.
     * @param dp
     */
    private DataHolder putInternal(IDataProvider dp, Map optionalParams) {
    	synchronized(cache) {
	    	if(JourneyMap.getLogger().isLoggable(Level.FINER)) {
	    		JourneyMap.getLogger().finer("Caching " + dp.getClass().getName());
	    	}
	    	DataHolder dh = new DataHolder(dp, optionalParams);
	    	cache.put(dp.getClass(), dh);
	    	return dh;
    	}
    }
    
    /**
     * Get the cached dataholder, keyed by provider class.
     * @param dpClass
     * @return
     */
    private DataHolder internalGet(Class<? extends IDataProvider> dpClass, Map optionalParams) {    	
    	synchronized(cache) {
    		DataHolder dh = cache.get(dpClass);
    		if(dh==null || dh.hasExpired()) {
    			dh = putInternal(dpClass, optionalParams); // Get fresh instance and cache it
    		}
    		return dh;
    	}
    }
    
    /**
     * Get the cached data map, keyed by provider class.
     * @param dpClass
     * @return
     */
    public Map get(Class<? extends IDataProvider> dpClass) {    	
    	return internalGet(dpClass, null).getData();
    }
    
    /**
     * Get the cached data map, keyed by provider class.
     * @param dpClass
     * @return
     */
    public Map get(Class<? extends IDataProvider> dpClass, Map optionalParams) {    	
    	return internalGet(dpClass, optionalParams).getData();
    }
    
    /**
     * Get the JSON data string, keyed by provider class.
     * @param dpClass
     * @return
     */
    public String getJson(Class<? extends IDataProvider> dpClass, Map optionalParams) {    	
    	return internalGet(dpClass, optionalParams).getJsonData();
    }
    
    /**
     * Appends the JSON data string to the provided StringBuffer,
     * returns the expiration timestamp in millis.
     * @param dpClass
     * @param sb
     * @return
     */
    public long appendJson(Class<? extends IDataProvider> dpClass, Map optionalParams, StringBuffer sb) {    	
    	DataHolder dh = internalGet(dpClass, optionalParams);
    	sb.append(dh.getJsonData());
    	return dh.getExpires();
    }
    
    /**
     * Convenience method
     * @param key
     * @return
     */
    public static Object playerDataValue(EntityKey key) {
    	return instance().get(PlayerData.class, null).get(key);
    }

    /**
     * Holds the map of values and an expiration timestamp.
     * @author mwoodman
     *
     */
    private class DataHolder {

    	private final IDataProvider dp;
    	private final long expires;
    	private final Map data;
    	private final String jsonData;
    	
    	DataHolder(IDataProvider dp, Map optionalParams) {
    		this.dp = dp;
        	data = Collections.unmodifiableMap(dp.getMap(optionalParams));
        	jsonData = GSON.toJson(data);
        	expires = System.currentTimeMillis() + dp.getTTL(); 		
    	}

    	public long getExpires() {
    		return expires;
    	}
    	
    	public boolean hasExpired() {
    		return dp.dataExpired() || expires<=System.currentTimeMillis();
    	}

    	public Map getData() {
    		return data;
    	}
    	
    	public String getJsonData() {
    		return jsonData;
    	}

    }
}
