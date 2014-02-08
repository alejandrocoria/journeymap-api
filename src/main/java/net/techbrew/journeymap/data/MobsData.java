package net.techbrew.journeymap.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
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
public class MobsData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(3);

	/**
	 * Constructor.
	 */
	public MobsData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	@Override
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of nearby mobs data.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		if(!FeatureManager.isAllowed(Feature.RadarMobs)) {
			return Collections.emptyMap();
		}
		
		List mobs = EntityHelper.getMobsNearby();
		ArrayList<LinkedHashMap> list = new ArrayList<LinkedHashMap>(mobs.size());
		for(Object mob : mobs) {
			if(mob instanceof Entity) {
				Entity entity = (Entity) mob;
				LinkedHashMap eProps = new LinkedHashMap();
				eProps.put(EntityKey.entityId, entity.getUniqueID());
				eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
				if(mob instanceof EntityMob || mob instanceof IBossDisplayData || mob instanceof IRangedAttackMob || mob instanceof EntityGhast) {
					eProps.put(EntityKey.hostile, true); 
				} else {
					eProps.put(EntityKey.hostile, false); 
				}

                eProps.put(EntityKey.posX, entity.posX);
                eProps.put(EntityKey.posZ, entity.posZ);
                eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX);
				eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 
				eProps.put(EntityKey.heading, EntityHelper.getHeading(entity));
				
				// CustomName
				if(entity instanceof EntityLiving) {
					if(((EntityLiving)entity).hasCustomNameTag()) {
						eProps.put(EntityKey.customName, StringUtils.stripControlCodes(((EntityLiving) entity).getCustomNameTag()));
					}
				}
				
				list.add(eProps);
			}
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