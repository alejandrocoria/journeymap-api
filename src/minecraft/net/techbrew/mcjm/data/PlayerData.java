package net.techbrew.mcjm.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.src.Minecraft;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.World;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.render.MapBlocks;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class PlayerData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(1);

	/**
	 * Constructor.
	 */
	public PlayerData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of world-related properties.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;			
	   
		LinkedHashMap props = new LinkedHashMap();

		props.put(EntityKey.username, player.getEntityName());
		props.put(EntityKey.heading, EntityHelper.getHeading(player));
		props.put(EntityKey.chunkCoordX, player.chunkCoordX); 
		props.put(EntityKey.chunkCoordY, player.chunkCoordY); 
		props.put(EntityKey.chunkCoordZ, player.chunkCoordZ); 
		props.put(EntityKey.posX, (int) Math.floor(player.posX)); 
		props.put(EntityKey.posY, (int) Math.floor(player.posY));
		props.put(EntityKey.posZ, (int) Math.floor(player.posZ));
		
		props.put(EntityKey.dimension, mc.theWorld.provider.dimensionId); 
		props.put(EntityKey.biome, getPlayerBiome()); 
		props.put(EntityKey.underground, playerIsUnderground());		

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
		
		int topY = world.getHeight();
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
	
	/**
	 * Return false by default. Let cache expired based on TTL.
	 */
	public boolean dataExpired() {
		return false;
	}
}
