package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.World;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.render.MapBlocks;

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
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of nearby animals data.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;			
	   
		List<EntityVillager> villagers = EntityHelper.getVillagersNearby();
		List<Map> list = new ArrayList<Map>(villagers.size());
		
		for(EntityVillager entity : villagers) {
			LinkedHashMap eProps = new LinkedHashMap();
			eProps.put(EntityKey.filename, EntityHelper.getFileName(entity)); 
			eProps.put(EntityKey.hostile, false);
			eProps.put(EntityKey.posX, (int) entity.posX); 
			eProps.put(EntityKey.posZ, (int) entity.posZ); 
			eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX); 
			eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ); 	
			eProps.put(EntityKey.heading, EntityHelper.getHeading(entity)); 
			eProps.put(EntityKey.profession, entity.getProfession()); 
			list.add(eProps);
		}
					
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, list);
		
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
	 * Check whether player isn't under sky
	 * @param player
	 * @return
	 */
	private boolean playerIsUnderground() {
		
		Minecraft mc = Minecraft.getMinecraft();		
		EntityPlayerSP player = mc.thePlayer;
		
		if(player.worldObj.provider.hasNoSky) {
			return true;
		}
		
		final int posX = (int) Math.floor(player.posX);
		final int posY = (int) Math.floor(player.posY)-1;
		final int posZ = (int) Math.floor(player.posZ);
		final int offset=1;		
		int x=0,y=0,z=0,blockId=0;
		boolean isUnderground = true;
		
		check : {
			for(x = (posX-offset);x<=(posX+offset);x++) {
				for(z=(posZ-offset);z<=(posZ+offset);z++) {					
					y = posY+1;
					if(canSeeSky(player.worldObj, x, y, z)) {
						isUnderground = false;
						break check;
					}
				}
				
			}
		}
		//System.out.println("underground: " + isUnderground);
		return isUnderground;
	}
	
	/**
	 * Potentially dangerous to use anywhere other than for player's current position
	 * - seems to cause crashes when used with ChunkRenderer.paintUnderground()
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static boolean canSeeSky(World world, final int x, final int y, final int z) {
		boolean seeSky = true;
		int blockId;
		
		int topY = 256; //world.worldMaxY;
		if(y>=topY) {
			return true;
		}
		int checkY = topY;
		while(seeSky && checkY>y) {
			blockId = world.getBlockId(x, checkY, z);
			if(MapBlocks.sky.contains(blockId)) {
				checkY--;
			} else {
				seeSky = false;
				break;
			}
		}
		return seeSky;
	}
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
}
