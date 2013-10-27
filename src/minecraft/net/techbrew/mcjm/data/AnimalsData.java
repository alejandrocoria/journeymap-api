package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.src.EntityHorse;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.IAnimals;
import net.minecraft.src.Minecraft;
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
	@Override
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of nearby animals data.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		// TODO: override includeNonPets, includePets?
		
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
			eProps.put(EntityKey.entityId, entity.entityId); 
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
					
		// Sort to keep named entities last.  (Why? display on top of others?)
		Collections.sort(list, new EntityHelper.EntityMapComparator());
		
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
