package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.FileHandler;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.BlockFluid;
import net.minecraft.src.ITexturePack;
import net.minecraft.src.Icon;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Rect2i;
import net.minecraft.src.Texture;
import net.minecraft.src.TextureStitched;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.ExtendedBlockStorageStub;
import net.techbrew.mcjm.model.TextureStitchedStub;

public class MapBlocks extends HashMap {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);
	
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
			int blockId = chunkStub.getBlockID(x, y, z);
			int meta = (blockId==0) ? 0 : chunkStub.getBlockMetadata(x, y, z);
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
			JourneyMap.getLogger().severe("Can't get blockId/meta for chunk " + chunkStub.xPosition + "," + chunkStub.zPosition + " block " + x + "," + y + "," + z + ": " + LogFormatter.toString(e)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$			
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
	 * Map of block ids that shouldn't cast shadows
	 */
	public final static HashSet<Integer> excludeHeight = new HashSet<Integer>(5);
	{
		excludeHeight.add(0); // air 
		excludeHeight.add(31); // grass, fern 
		excludeHeight.add(32); // shrub 
		excludeHeight.add(106); // vines
	}
	
	/**
	 * Alpha values for block ids.
	 */
	public final static HashMap<Integer, Float> alphas = new HashMap<Integer, Float>(5);
	
	static void resetAlphas() {
		alphas.put(8,.55F); // water
		alphas.put(9,.55F); // stationary water 
		alphas.put(20,.3F); // glass
		alphas.put(79,.8F); // ice
		alphas.put(102,.3F); // glass		
		alphas.put(131,0F); // tripwire hook
		alphas.put(132,0F); // tripwire
	}
	
}
