package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.src.Minecraft;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityHorse;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.IAnimals;
import net.techbrew.mcjm.model.EntityHelper;

/**
 * Provides nearby mobs in a Map.
 * 
 * @author mwoodman
 *
 */
public class AnimalsData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(3);
	
	private final boolean includeNonPets;
	private final boolean includePets;

	/**
	 * Constructor.
	 */
	public AnimalsData() {
		includeNonPets = true;
		includePets = true;
	}
	
	/**
	 * Constructor with specific inclusions.
	 * @param includeNonPets
	 * @param includePets
	 */
	public AnimalsData(boolean includeNonPets, boolean includePets) {
		super();
		this.includeNonPets = includeNonPets;
		this.includePets = includePets;
	}

	/**
	 * Provides all possible keys.
	 */
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of nearby animals data.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;			
	   
		List<IAnimals> animals = EntityHelper.getAnimalsNearby();
		ArrayList<LinkedHashMap> list = new ArrayList<LinkedHashMap>(animals.size());
		String owner;
		EntityLiving entity;
		for(IAnimals animal : animals) {
			entity = (EntityLiving) animal;
			
			// Exclude animals being ridden, since their positions lag behind the players on the map
			if(entity.riddenByEntity!=null) {
				continue;
			}
			
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
			eProps.put(EntityKey.hostile, false);
			eProps.put(EntityKey.posX, (int) Math.floor(entity.posX)); 
			eProps.put(EntityKey.posZ, (int) Math.floor(entity.posZ)); 
			eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX); 
			eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(EntityKey.heading, EntityHelper.getHeading(entity));
			if(entity instanceof EntityTameable) {
				owner = ((EntityTameable) entity).getOwnerName();
				if(owner!=null) {
					eProps.put(EntityKey.owner, owner);
				}
			} else if(entity instanceof EntityHorse) {
				owner = entity.getDataWatcher().getWatchableObjectString(21);
				eProps.put(EntityKey.owner, owner);
			}
			
			// CustomName
			if(entity.hasCustomNameTag()) {
				eProps.put(EntityKey.customName, entity.getCustomNameTag()); 
			}
						
			list.add(eProps);
		}
					
		// Sort to keep named entities last
		Collections.sort(list, new EntityHelper.EntityMapComparator());
		
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
