package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.src.EntityPlayer;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager;
import net.techbrew.mcjm.model.EntityHelper;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class PlayersData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(3);

	/**
	 * Constructor.
	 */
	public PlayersData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	@Override
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of world-related properties.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		if(!FeatureManager.isAllowed(Feature.RadarPlayers)) {
			return Collections.emptyMap();
		}
		
		List<EntityPlayer> others = EntityHelper.getPlayersNearby();
		List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(others.size());
		for(EntityPlayer entity : others) {
			LinkedHashMap eProps = new LinkedHashMap();
			// eProps.put(EntityKey.entityId, entity.entityId); 
			eProps.put(EntityKey.filename, "/skin/" + entity.getEntityName()); 
			eProps.put(EntityKey.username, entity.getEntityName());
			eProps.put(EntityKey.posX, (int) entity.posX); 
			eProps.put(EntityKey.posY, (int) entity.posY); 
			eProps.put(EntityKey.posZ, (int) entity.posZ);
			eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX); 
			eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(EntityKey.heading, EntityHelper.getHeading(entity));
			list.add(eProps);
		}
	
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, EntityHelper.buildEntityIdMap(list, false));
		
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
