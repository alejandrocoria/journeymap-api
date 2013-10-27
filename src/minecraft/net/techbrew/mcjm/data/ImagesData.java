package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;

/**
 * Provides data of what's changed in RegionImageCache in a Map.
 * This provider requires parameters for a valid response.
 * 
 * @author mwoodman
 *
 */
public class ImagesData implements IDataProvider {
	
	public static final String PARAM_SINCE = "images.since";
	
	private static long TTL = TimeUnit.SECONDS.toMillis(1);
	
	public static enum Key {
		since,
		regions,
		queryTime,
	}

	/**
	 * Constructor.
	 */
	public ImagesData() {
	}
	
	private Double[] toZoomedBounds(final int zoom, final int rX, final int rZ) {
		double scale = Math.pow(2, zoom);
		double sX = rX*scale;
		double sZ = rZ*scale;
		double sX2 = (rX+1)*scale;
		double sZ2 = (rZ+1)*scale;
		return new Double[]{sX,sZ,sX2,sZ2};
	}
	
	@Override
	public Enum[] getKeys() {
		return Key.values();
	}
	
	/**
	 * Return a map of game data.
	 */
	@Override
	public Map getMap(Map optionalParams) {	
		
		final LinkedHashMap props = new LinkedHashMap();	
		final long now = new Date().getTime();

		Long since = null;
		
		Object sinceStr = optionalParams.get(PARAM_SINCE);
		if(sinceStr!=null) since = Long.parseLong((String) sinceStr);
		if(since==null) {
			since = now;
		}
		
		List<Object[]> coords = null;
		List<RegionCoord> regions = RegionImageCache.getInstance().getDirtySince(since);
		if(regions.isEmpty()) {
			coords = Collections.EMPTY_LIST;
		} else {
			coords = new ArrayList<Object[]>(regions.size());		
			for(RegionCoord rc : regions) {
				coords.add(new Integer[]{rc.regionX, rc.regionZ});
			}
		}
		
		props.put(Key.queryTime, now);
		props.put(Key.since, since); 
		props.put(Key.regions, coords);

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
	 * Return false by default. Let cache expired based on TTL.
	 */
	@Override
	public boolean dataExpired() {
		return false;
	}

}
