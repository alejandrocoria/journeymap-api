package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.src.Block;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;

public class MapBlocks extends HashMap {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);
	
	public final static HashMap<Integer, Float> alphas = new HashMap<Integer, Float>(5);
	
	ColorCache colorCache;
	
	/**
	 * Constructor
	 */
	public MapBlocks() {
		colorCache = new ColorCache();
		resetAlphas();
	}	
	
	/**
	 * Returns a simple wrapper object of the blockId and the block meta values.
	 * @param chunkStub
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	BlockInfo getBlockInfo(ChunkStub chunkStub, int x, int y, int z) {
		try {
			int blockId, meta;
			if(y>=0) {
				blockId = chunkStub.getBlockID(x, y, z);
				meta = (blockId==0) ? 0 : chunkStub.getBlockMetadata(x, y, z);
			} else {
				blockId = -1;
				meta = 0;
			}
			BlockInfo info = new BlockInfo(blockId, meta);
			if(blockId>0) {
				info.setColor(colorCache.getBlockColor(chunkStub, info, x, y, z));
				Float alpha = alphas.get(blockId);
				info.setAlpha(alpha==null ? 1F : alpha);
			} else {
				info.setColor(Color.black);
				info.setAlpha(0);
			}
			
			return info;
			
		} catch (Exception e) {
			JourneyMap.getLogger().severe("Can't get blockId/meta for chunk " + chunkStub.xPosition + "," + chunkStub.zPosition + " block " + x + "," + y + "," + z); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			return null;
		}
	}
	
	
	/**
	 * Attempt at faster way to figure out if there is sky above
	 * @param chunkStub
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static boolean skyAbove(ChunkStub chunkStub, final int x, final int y, final int maxY, final int z) {
		boolean seeSky = true;
		int blockId;
		
		int checkY = y;
		while(seeSky && checkY<=maxY) {
			blockId = chunkStub.getBlockID(x, checkY, z);
			if(sky.contains(blockId)) {
				checkY++;
			} else {
				seeSky = false;
				break;
			}
		}
		return seeSky;
	}
	
	/**
	 * Attempt at faster way to figure out if there is sky above
	 * @param chunkStub
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static int topNonSkyBlock(ChunkStub chunkStub, final int x, int maxY, final int z) {
		
		final int chunkHeight = chunkStub._getHeightValue(x, z);
		final int topY = Math.min(maxY, chunkHeight);
		
		int blockId;
		int y = topY;
		if(topY==chunkHeight) {
			// Error check, could be bad data in the heightmap
			while(y<maxY) {
				blockId = chunkStub.getBlockID(x, y, z);
				
				if(blockId==0 || sky.contains(blockId)) {
					y++;
				} else {
					break;
				}
			}
			if(y<maxY) {
				return y;
			} else {
				return topY;
			}
		}
		
		while(y>=0) {
			blockId = chunkStub.getBlockID(x, y, z);
			if(blockId==0 || sky.contains(blockId)) {
				y--;
			} else {
				break;
			}
		}
		return y;
	}
		
	/**
	 * Map of transparent block ids that don't block view of the sky
	 */
	public final static HashSet<Integer> sky = new HashSet<Integer>(7);
	{
		sky.add(0); // air 
		sky.add(8); // water 
		sky.add(9); // stationary water 
		sky.add(18); // leaves
		sky.add(30); // web
		sky.add(65); // ladder
		sky.add(78); // snow
		sky.add(106); // vines
	}
	
	/**
	 * Map of block ids that shouldn't be used as top blocks
	 */
	public final static HashSet<Integer> excludeHeight = new HashSet<Integer>(5);
	{
		excludeHeight.add(0); // air 
		excludeHeight.add(Block.tallGrass.blockID); // grass, fern 
		excludeHeight.add(Block.deadBush.blockID); // shrub 
		excludeHeight.add(Block.tripWire.blockID); // tripwire hook
		excludeHeight.add(Block.tripWireSource.blockID); // tripwire
		excludeHeight.add(Block.glass.blockID);
		excludeHeight.add(Block.thinGlass.blockID);
	}
	
	/**
	 * Map of block ids that shouldn't cast shadows
	 */
	public final static HashSet<BlockInfo> noShadows = new HashSet<BlockInfo>(5);
	{
		noShadows.add(new BlockInfo(Block.waterStill.blockID, 0));
		noShadows.add(new BlockInfo(Block.lavaStill.blockID, 0));
		noShadows.add(new BlockInfo(Block.fire.blockID, 0));
		noShadows.add(new BlockInfo(Block.glass.blockID, 0));
		noShadows.add(new BlockInfo(Block.vine.blockID, 0));
	}
	
	/**
	 * Alpha values for block ids.
	 */	
	static void resetAlphas() {
		alphas.put(Block.waterStill.blockID,.7F); 
		alphas.put(Block.waterMoving.blockID,.7F); 
		alphas.put(Block.ice.blockID,.8F); 
		alphas.put(Block.glass.blockID,.3F); 
		alphas.put(Block.thinGlass.blockID,.3F);
		alphas.put(Block.vine.blockID,.2F);
		alphas.put(Block.torchWood.blockID,.5F);
	}
	
}
