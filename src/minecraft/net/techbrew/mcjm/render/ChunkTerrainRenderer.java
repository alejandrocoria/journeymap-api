package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.Vec3;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;

public class ChunkTerrainRenderer implements IChunkRenderer {
	
	private IChunkRenderer flatRenderer = new ChunkFlatRenderer();
	private IChunkRenderer heightRenderer = new ChunkHeightMapRenderer();


	@Override
	public BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, int vSlice,
			Map<Integer, ChunkStub> neighbors) {
		
		BufferedImage chunkImage = null;
		
		try {
		
			BufferedImage colorImage = flatRenderer.getChunkImage(chunkStub, underground, vSlice, neighbors);
			chunkImage = new BufferedImage(underground ? 16 : 32, 16, BufferedImage.TYPE_INT_ARGB);
			chunkImage.setData(colorImage.getData());
			
			int arrSize = 256;
	
			float[] lightDir = new float[]{-100,1,-100};
			intersectMapOptimized(chunkStub, 16, chunkImage, neighbors); 
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return chunkImage;
	}
	
	

	/**
	 * http://content.gpwiki.org/index.php/Faster_Ray_Traced_Terrain_Shadow_Maps#Optimized_Source_Code
	 */
	void intersectMapOptimized(ChunkStub chunkStub, int size, BufferedImage chunkImage, Map<Integer, ChunkStub> neighbors)
	{
		// create flag buffer to indicate where we've been
		float[] flagMap = new float[size*size];
		
		float h, hN, hNW, hW;
		float s=1f, sN, sNW, sW;
		int shaded;
		float slope;
		float intensity = 1f;
		
		if(chunkStub.slopes==null) {
			chunkStub.slopes = new float[size][size];
			for(int x=0; x<size; x++)
			{
				Arrays.fill(chunkStub.slopes[x], 1f);
			}
		}
		
		for(int y=0; y<size; y++)
		{
			for(int x=0; x<size; x++)
			{				
				h = chunkStub.getSafeHeightValue(x, y);
				hN = (y==0) ? getBlockHeight(x, y, 0, -1, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x, y-1);
				hNW = (x==0 || y==0) ? getBlockHeight(x, y, -1, -1, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x-1, y-1);
				hW = (x==0) ? getBlockHeight(x, y, -1, 0, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x-1, y);
				float avgH = (hN + hNW + hW) / 3.0f;
				
				s = h/avgH;				
				
				if(s!=1f) {
					
					sN = (y==0) ? getBlockSlope(x, y, 0, -1, chunkStub, neighbors, s) : chunkStub.slopes[x][y-1];
					sNW = (x==0 || y==0) ? getBlockSlope(x, y, -1, -1, chunkStub, neighbors, s) : chunkStub.slopes[x-1][y-1];
					sW = (x==0) ? getBlockSlope(x, y, -1, 0, chunkStub, neighbors, s) : chunkStub.slopes[x-1][y];
					float avgS = (sN + sNW + sW) / 3.0f;
					
					if(h<avgH) {
						
						s = Math.min(s, h/hN);
						s = Math.min(s, h/hNW);
						s = Math.min(s, h/hW);
						
						s = Math.min(s, sN);
						s = Math.min(s, sNW);
						s = Math.min(s, sW);
						s = Math.max(s*.9f, .4f);						
						
					} else if(h>avgH) {
						
						s = Math.min(s, Math.max(sN, 1.0f));
						s = Math.min(s, Math.max(sNW, 1.0f));
						s = Math.min(s, Math.max(sW, 1.0f));
						s = s*1.05f;
					}				
					
				}
				
				chunkStub.slopes[x][y] = s;
				
				shaded = darken(chunkImage.getRGB(x, y), s);
				chunkImage.setRGB(x,y,shaded);
				 				
			}
		}
	 
	}
	
	public int darken(int original, float factor) {
		
		if(factor<0) {
			throw new IllegalArgumentException("factor can't be negative");
		}
		int r = Math.min(255, (int) (((original >> 16) & 0xFF) * factor));
		int g = Math.min(255, (int) (((original >> 8) & 0xFF) * factor));
		int b = Math.min(255, (int) (((original >> 0) & 0xFF) * factor));

		try {
			return new Color(r,g,b).getRGB();
		} catch(IllegalArgumentException e) {
			throw e;
		}
	}
	
	
	public Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors, float defaultVal) {
		int newX = x+offsetX;
		int newZ = z+offsetz;
		
		int chunkX = currentChunk.xPosition;
		int chunkZ = currentChunk.zPosition;
		boolean search = false;
		
		if(newX==-1) {
			chunkX--;
			newX = 15;
			search = true;
		} else if(newX==16) {
			chunkX++;
			newX = 0;
			search = true;
		}
		if(newZ==-1) {
			chunkZ--;
			newZ = 15;
			search = true;
		} else if(newZ==16) {
			chunkZ++;
			newZ = 0;
			search = true;
		}
		
		ChunkStub chunk = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);
		
		if(chunk!=null) {
			return (float) chunk.getSafeHeightValue(newX, newZ);
		} else {
			return defaultVal;
		}
	}
	
	public Float getBlockSlope(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors, float defaultVal) {
		int newX = x+offsetX;
		int newZ = z+offsetz;
		
		int chunkX = currentChunk.xPosition;
		int chunkZ = currentChunk.zPosition;
		boolean search = false;
		
		if(newX==-1) {
			chunkX--;
			newX = 15;
			search = true;
		} else if(newX==16) {
			chunkX++;
			newX = 0;
			search = true;
		}
		if(newZ==-1) {
			chunkZ--;
			newZ = 15;
			search = true;
		} else if(newZ==16) {
			chunkZ++;
			newZ = 0;
			search = true;
		}
		
		ChunkStub chunk = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);
		
		if(chunk!=null) {
			if(chunk.slopes==null) {
				return defaultVal;
			} else {
				return chunk.slopes[newX][newZ];
			}
		} else {
			return defaultVal;
		}
	}
	
	
	ChunkStub getChunk(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors) {
		int newX = x+offsetX;
		int newZ = z+offsetz;
		
		int chunkX = currentChunk.xPosition;
		int chunkZ = currentChunk.zPosition;
		boolean search = false;
		
		if(newX==-1) {
			chunkX--;
			newX = 15;
			search = true;
		} else if(newX==16) {
			chunkX++;
			newX = 0;
			search = true;
		}
		if(newZ==-1) {
			chunkZ--;
			newZ = 15;
			search = true;
		} else if(newZ==16) {
			chunkZ++;
			newZ = 0;
			search = true;
		}
		
		ChunkStub chunk = null;
		if(search) {
			Integer stubHash = ChunkStub.toHashCode(chunkX, chunkZ);
			chunk = neighbors.get(stubHash);
		} else {
			chunk = currentChunk;
		}
		
		return chunk;
	}
	

}
