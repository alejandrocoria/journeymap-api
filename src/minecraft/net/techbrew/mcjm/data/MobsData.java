package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.IMob;
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
		mobs,
		type,
		posX,
		posZ,
		chunkCoordX,
		chunkCoordZ,
		heading;
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
			eProps.put(Key.posX, (int) entity.posX); 
			eProps.put(Key.posZ, (int) entity.posZ); 
			eProps.put(Key.chunkCoordX, entity.chunkCoordX); 
			eProps.put(Key.chunkCoordZ, entity.chunkCoordZ); 
			eProps.put(Key.heading, EntityHelper.getHeading(entity));
			list.add(eProps);
		}
					
		LinkedHashMap props = new LinkedHashMap();
		props.put(Key.mobs, list);
		
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
