package net.techbrew.mcjm.data;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.JsonHelper;

/**
 * Singleton cache of data produced by IDataProviders.
 * 
 * Uses on-demand-holder pattern for singleton management.
 * 
 * @author mwoodman
 *
 */
public class DataCache {
	
	private final ConcurrentHashMap<Class<? extends IDataProvider>, DataHolder> cache;
	
	// Private constructor
	private DataCache() {
		 cache = new ConcurrentHashMap<Class<? extends IDataProvider>, DataHolder>(7); // adjust size for # of IDataProvider impl classes
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
    
    /**
     * Put the dataprovider's map into the cache, wrapped in a DataHolder 
     * and keyed by provider class.
     * @param dp
     */
    public void put(IDataProvider dp) {
    	putInternal(dp);
    }
    
    /**
     * Put an instance of the dataholder class into the cache, wrapped in a DataHolder 
     * and keyed by provider class.
     * @param dp
     */
    private DataHolder putInternal(Class<? extends IDataProvider> dpClass) {
    	try {
			return putInternal(dpClass.newInstance());
		} catch (Exception e) {
			// Shouldn't happen
			JourneyMap.getLogger().severe("Can't instantiate dataprovider for cache: " + e);
			throw new RuntimeException(e);
		}
    }
    
    /**
     * Put the dataprovider's map into the cache, wrapped in a DataHolder 
     * and keyed by provider class.
     * @param dp
     */
    private DataHolder putInternal(IDataProvider dp) {
    	if(JourneyMap.getLogger().isLoggable(Level.FINER)) {
    		JourneyMap.getLogger().finer("Caching " + dp.getClass().getName());
    	}
    	DataHolder dh = new DataHolder(dp);
    	cache.put(dp.getClass(), dh);
    	return dh;
    }
    
    /**
     * Get the cached dataholder, keyed by provider class.
     * @param dpClass
     * @return
     */
    private DataHolder internalGet(Class<? extends IDataProvider> dpClass) {    	
    	synchronized(cache) {
    		DataHolder dh = cache.get(dpClass);
    		if(dh==null || dh.hasExpired()) {
    			dh = putInternal(dpClass); // Get fresh instance and cache it
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
    	return internalGet(dpClass).getData();
    }
    
    /**
     * Get the JSON data string, keyed by provider class.
     * @param dpClass
     * @return
     */
    public String getJson(Class<? extends IDataProvider> dpClass) {    	
    	return internalGet(dpClass).getJsonData();
    }
    
    /**
     * Appends the JSON data string to the provided StringBuffer,
     * returns the expiration timestamp in millis.
     * @param dpClass
     * @param sb
     * @return
     */
    public long appendJson(Class<? extends IDataProvider> dpClass, StringBuffer sb) {    	
    	DataHolder dh = internalGet(dpClass);
    	sb.append(dh.getJsonData());
    	return dh.getExpires();
    }

    /**
     * Holds the map of values and an expiration timestamp.
     * @author mwoodman
     *
     */
    private class DataHolder {

    	private final long expires;
    	private final Map data;
    	private final String jsonData;
    	
    	DataHolder(IDataProvider dp) {
        	data = Collections.unmodifiableMap(dp.getMap());
        	jsonData = JsonHelper.toJson(data);
        	expires = System.currentTimeMillis() + dp.getTTL(); 		
    	}

    	public long getExpires() {
    		return expires;
    	}
    	
    	public boolean hasExpired() {
    		return expires<=System.currentTimeMillis();
    	}

    	public Map getData() {
    		return data;
    	}
    	
    	public String getJsonData() {
    		return jsonData;
    	}

    }
}
