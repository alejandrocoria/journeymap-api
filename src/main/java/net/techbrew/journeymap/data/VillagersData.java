package net.techbrew.journeymap.data;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.StringUtils;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.EntityHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Provides nearby mobs in a Map.
 * 
 * @author mwoodman
 *
 */
public class VillagersData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(5);
	

	/**
	 * Constructor.
	 */
	public VillagersData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	@Override
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of nearby animals data.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		if(!FeatureManager.isAllowed(Feature.RadarVillagers)) {
			return Collections.emptyMap();
		}
		
		List<EntityVillager> villagers = EntityHelper.getVillagersNearby();
		ArrayList<LinkedHashMap> list = new ArrayList<LinkedHashMap>(villagers.size());
		
		for(EntityVillager entity : villagers) {
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(EntityKey.entityId, entity.getUniqueID());
			eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
			eProps.put(EntityKey.hostile, false);
            eProps.put(EntityKey.posX, entity.posX);
            eProps.put(EntityKey.posZ, entity.posZ);
			eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX); 
			eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 	
			eProps.put(EntityKey.heading, EntityHelper.getHeading(entity)); 
			eProps.put(EntityKey.profession, entity.getProfession()); 
			
			// CustomName
			if(entity.hasCustomNameTag()) {
				eProps.put(EntityKey.customName, StringUtils.stripControlCodes(entity.getCustomNameTag()));
			}
						
			list.add(eProps);
		}
		
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, EntityHelper.buildEntityIdMap(list, true));
		
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
