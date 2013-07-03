package net.techbrew.mcjm;

import net.minecraft.src.Minecraft;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EmptyChunk;
import net.minecraft.src.World;

public class Utils {

	/**
	 * Tries to safely get a Chunk.
	 * @param world
	 * @param chunkX
	 * @param chunkZ
	 * @return
	 */
	public static Chunk getChunkIfAvailable(World world, int chunkX, int chunkZ) {		
		Chunk result  = null;
		if(world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
			Chunk theChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
			if(!(theChunk instanceof EmptyChunk)) {
				if(theChunk.isChunkLoaded) {
					result = theChunk;
				}
			}			
		}
		return result;
	}
	
	/**
	 * For int v, return the closest larger power of 2, not to exceed clamp.
	 * @param v
	 * @param clamp
	 * @return
	 */
	public static int upperPowerOfTwo(int v, int clamp)
	{
	    v--;
	    v |= v >> 1;
	    v |= v >> 2;
	    v |= v >> 4;
	    v |= v >> 8;
	    v |= v >> 16;
	    v++;
	    return Math.min(v, clamp);
	}
	
	/**
	 * Returns the larger distance of blocks (x axis or z axis) represented by the coordinate pair.
	 * @param pair1
	 * @param pair2
	 * @return
	 */
	public static int upperDistanceInBlocks(ChunkCoordIntPair pair1, ChunkCoordIntPair pair2) {
		if(pair1.equals(pair2)) {
			return 16;
		} else {
			int dx = pair2.chunkXPos - pair1.chunkXPos;
			int dz = pair2.chunkZPos - pair1.chunkZPos;
			return 16 * Math.max(dx,dz);
		}
	}
	
	/**
	 * Workaround now that in 1.2.1 you can't get the world seed.
	 * 
	 * @param world
	 * @return
	 */
	public static long getWorldHash(Minecraft minecraft) {
		
		return minecraft.theWorld.getSeed();
		
		// TODO World Hash Fix
//		World world = minecraft.theWorld;
//		if(world.getSeed()==0) {
//			
//			ChunkCoordinates cc = world.getSpawnPoint();
//			Chunk spawnChunk = Utils.getChunkIfAvailable(world, cc.posX>>4, cc.posZ>>4);
//			if((spawnChunk==null || spawnChunk instanceof EmptyChunk) && !Minecraft.getMinecraft().isSingleplayer()) {
//				spawnChunk = world.getChunkFromBlockCoords(cc.posX, cc.posZ);
//				//spawnChunk = world.getChunkProvider().loadChunk(cc.posX, cc.posZ);
//				if(spawnChunk.isTerrainPopulated) {
//					return 0;
//				}
//			}
//			
//			long hash = 0;
//			if(spawnChunk != null && !(spawnChunk instanceof EmptyChunk)) {
//				hash = 31;
//				hash = hash + (31 * spawnChunk.xPosition);
//				hash = hash + (31 * spawnChunk.zPosition);
//			}
//			
////			long hash = 31;
////			int bid = 0;
////			int x,y,z;
////			for(x=0;x<16;x++) {
////				for(z=0;z<16;z++) {
////					bid = spawnChunk.getBlockMetadata(x, 2, z);
////					if(bid!=0) System.out.println(bid);
////					hash = (31 * hash) + (bid==7 ? 7 : 0);
////				}
////			}
////			
////			if(hash==436481077226119199L) { // all zeros
////				hash=0;
////			}
//			
//			return hash;
//		} else {
//			return world.getSeed();
//		}
	}
}
