package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityGolem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityWaterMob;
import net.minecraft.src.IBossDisplayData;
import net.minecraft.src.IMob;
import net.minecraft.src.IRangedAttackMob;
import net.minecraft.src.World;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.EntityHelper;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.VillagersData.Key;
import net.techbrew.mcjm.render.MapBlocks;

/**
 * Provides nearby mobs in a Map.
 * 
 * @author mwoodman
 *
 */
public class MobsData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(3);
	
	public static enum Key {		
		type,
		hostile,
		posX,
		posZ,
		chunkCoordX,
		chunkCoordZ,
		heading,
		root;
	}

	/**
	 * Constructor.
	 */
	public MobsData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	public Enum[] getKeys() {
		return Key.values();
	}
	
	/**
	 * Return map of nearby mobs data.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;			
	   
		List<IMob> mobs = EntityHelper.getMobsNearby();
		List<Map> list = new ArrayList<Map>(mobs.size());
		for(IMob mob : mobs) {
			EntityLiving entity = (EntityLiving) mob;
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(Key.type, entity.getEntityName()); 
			if(mob instanceof EntityMob || mob instanceof IBossDisplayData || mob instanceof IRangedAttackMob) {
				eProps.put(Key.hostile, true); 
			} else {
				eProps.put(Key.hostile, false); 
			}
			
			eProps.put(Key.posX, (int) entity.posX); 
			eProps.put(Key.posZ, (int) entity.posZ); 
			eProps.put(Key.chunkCoordX, entity.chunkCoordX); 
			eProps.put(Key.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(Key.heading, EntityHelper.getHeading(entity));
			list.add(eProps);
		}
					
		LinkedHashMap props = new LinkedHashMap();
		props.put(Key.root, list);
		
		return props;		
	}	
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
}
