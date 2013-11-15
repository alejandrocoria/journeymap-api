package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.Minecraft;
import net.minecraft.src.StringUtils;
import net.techbrew.mcjm.model.EntityHelper;

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
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityClientPlayerMP player = mc.thePlayer;			
	   
		List<EntityVillager> villagers = EntityHelper.getVillagersNearby();
		ArrayList<LinkedHashMap> list = new ArrayList<LinkedHashMap>(villagers.size());
		
		for(EntityVillager entity : villagers) {
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(EntityKey.entityId, entity.entityId); 
			eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
			eProps.put(EntityKey.hostile, false);
			eProps.put(EntityKey.posX, (int) Math.floor(entity.posX)); 
			eProps.put(EntityKey.posZ, (int) Math.floor(entity.posZ)); 
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
		
		// Put into map, preserving the order, using entityId as key
		LinkedHashMap<Object,Map> idMap = new LinkedHashMap<Object,Map>(list.size());
		for(Map entityMap : list) {
			idMap.put("id"+entityMap.get(EntityKey.entityId), entityMap);
		}
		
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, idMap);
		
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
