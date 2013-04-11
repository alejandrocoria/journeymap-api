package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayerSP;
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
		
		for(IAnimals animal : animals) {
			EntityLiving entity = (EntityLiving) animal;
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
			eProps.put(EntityKey.hostile, false);
			eProps.put(EntityKey.posX, (int) entity.posX); 
			eProps.put(EntityKey.posZ, (int) entity.posZ); 
			eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX); 
			eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(EntityKey.heading, EntityHelper.getHeading(entity));
			if(entity instanceof EntityTameable) {
				String owner = ((EntityTameable) entity).getOwnerName();
				if(owner!=null) {
					eProps.put(EntityKey.owner, owner);
				}
			}
			
			// CustomName
			if(entity.func_94056_bM()) {
				eProps.put(EntityKey.customName, entity.func_94057_bL()); 
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
