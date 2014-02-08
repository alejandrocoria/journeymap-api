package net.techbrew.journeymap.data;

import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.EntityHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
			eProps.put(EntityKey.filename, "/skin/" + entity.getDisplayName());
			eProps.put(EntityKey.username, entity.getDisplayName());
            eProps.put(EntityKey.posX, entity.posX);
            eProps.put(EntityKey.posY, entity.posY);
            eProps.put(EntityKey.posZ, entity.posZ);
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
