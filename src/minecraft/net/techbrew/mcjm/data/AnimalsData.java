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
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.IAnimals;
import net.minecraft.src.World;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.EntityHelper;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.MobsData.Key;
import net.techbrew.mcjm.render.MapBlocks;

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
	
	public static enum Key {
		animals,
		type,
		posX,
		posZ,
		chunkCoordX,
		chunkCoordZ,
		heading,
		owner;
	}

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
		return Key.values();
	}
	
	/**
	 * Return map of nearby animals data.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;			
	   
		List<IAnimals> animals = EntityHelper.getAnimalsNearby();
		List<Map> list = new ArrayList<Map>(animals.size());
		
		for(IAnimals animal : animals) {
			EntityLiving entity = (EntityLiving) animal;
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(Key.type, entity.getEntityName()); 
			eProps.put(Key.posX, (int) entity.posX); 
			eProps.put(Key.posZ, (int) entity.posZ); 
			eProps.put(Key.chunkCoordX, entity.chunkCoordX); 
			eProps.put(Key.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(Key.heading, EntityHelper.getHeading(entity));
			if(entity instanceof EntityTameable) {
				String owner = ((EntityTameable) entity).getOwnerName();
				if(owner!=null) {
					eProps.put(Key.owner, owner);
				}
			}
			list.add(eProps);
		}
					
		LinkedHashMap props = new LinkedHashMap();
		props.put(Key.animals, list);
		
		return props;		
	}	
	
	/**
	 * Get the biome name where the player is standing.
	 * 
	 * @param player
	 * @return
	 */
	private String getPlayerBiome() {
		
		Minecraft mc = Minecraft.getMinecraft();
		
		EntityPlayerSP player = mc.thePlayer;
		int x = ((int) Math.floor(player.posX) % 16) & 15;
		int z = ((int) Math.floor(player.posZ) % 16) & 15;
		
		ChunkStub playerChunk = JourneyMap.getLastPlayerChunk();
		if(playerChunk!=null) {
			return playerChunk.getBiomeGenForWorldCoords(x,z, mc.theWorld.getWorldChunkManager()).biomeName;
		} else {
			return "?"; //$NON-NLS-1$
		}
	}
	
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
}
