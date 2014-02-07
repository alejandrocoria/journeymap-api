package net.techbrew.mcjm.data;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.model.BlockUtils;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.model.EntityHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
	@Override
	public Enum[] getKeys() {
		return EntityKey.values();
	}
	
	/**
	 * Return map of world-related properties.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityClientPlayerMP player = mc.thePlayer;
	   
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.entityId, player.getUniqueID());
		props.put(EntityKey.username, player.getDisplayName());
		props.put(EntityKey.heading, EntityHelper.getHeading(player));
		props.put(EntityKey.chunkCoordX, player.chunkCoordX); 
		props.put(EntityKey.chunkCoordY, player.chunkCoordY); 
		props.put(EntityKey.chunkCoordZ, player.chunkCoordZ); 
		props.put(EntityKey.posX, player.posX);
		props.put(EntityKey.posY, player.posY);
		props.put(EntityKey.posZ, player.posZ);
		
		props.put(EntityKey.dimension, mc.theWorld.provider.dimensionId); 
		props.put(EntityKey.biome, getPlayerBiome()); 
		props.put(EntityKey.underground, playerIsUnderground(player));		

		return props;	
	}	
	
	/**
	 * Get the biome name where the player is standing.
	 */
	private String getPlayerBiome() {
		
		Minecraft mc = Minecraft.getMinecraft();
		
		EntityClientPlayerMP player = mc.thePlayer;
		int x = ((int) Math.floor(player.posX) % 16) & 15;
		int z = ((int) Math.floor(player.posZ) % 16) & 15;

		ChunkMD playerChunk = ChunkLoader.getChunkStubFromMemory(player.chunkCoordX, player.chunkCoordZ, mc);
		if(playerChunk!=null) {
			return playerChunk.stub.getBiomeGenForWorldCoords(x,z, mc.theWorld.getWorldChunkManager()).biomeName;
		} else {
			return "?"; //$NON-NLS-1$
		}
	}
	
	/**
	 * Check whether player isn't under sky
	 * @param player
	 * @return
	 */
	public static boolean playerIsUnderground(EntityPlayer player) {
		
		Minecraft mc = Minecraft.getMinecraft();		

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
		Block block;

        int topY = world.getTopSolidOrLiquidBlock(x,z);
        if(y>=topY) {
            return true;
        }

        Chunk chunk = world.getChunkFromBlockCoords(x, z);
		int checkY = topY;
		while(seeSky && checkY>y) {
            try {
                block = chunk.getBlock(x & 15, checkY, z & 15);
                if(BlockUtils.hasFlag(block, BlockUtils.Flag.NotHideSky)) {
                    checkY--;
                } else {
                    seeSky = false;
                    break;
                }
            } catch (Exception e) {
                checkY--;
                JourneyMap.getLogger().warning(e + " at " + (x & 15) + "," + checkY + "," + (z & 15));
                continue;
            }

		}
		return seeSky;
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
