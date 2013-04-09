package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.IBossDisplayData;
import net.minecraft.src.IMob;
import net.minecraft.src.IRangedAttackMob;
import net.techbrew.mcjm.model.EntityHelper;

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
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of nearby mobs data.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;			
	   
		List mobs = EntityHelper.getMobsNearby();
		List<Map> list = new ArrayList<Map>(mobs.size());
		for(Object mob : mobs) {
			EntityLiving entity = (EntityLiving) mob;
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
			if(mob instanceof EntityMob || mob instanceof IBossDisplayData || mob instanceof IRangedAttackMob) {
				eProps.put(EntityKey.hostile, true); 
			} else {
				eProps.put(EntityKey.hostile, false); 
			}
			
			eProps.put(EntityKey.posX, (int) entity.posX); 
			eProps.put(EntityKey.posZ, (int) entity.posZ); 
			eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX); 
			eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(EntityKey.heading, EntityHelper.getHeading(entity));
			list.add(eProps);
		}
					
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, list);
		
		return props;		
	}	
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
	
	/**
	 * Return false by default. Let cache expired based on TTL.
	 */
	public boolean dataExpired() {
		return false;
	}
}
