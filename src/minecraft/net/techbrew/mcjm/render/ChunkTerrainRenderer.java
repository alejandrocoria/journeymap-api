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
			shadeChunk(chunkStub, chunkImage, neighbors); 
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return chunkImage;
	}
	
	

	/**
	 * http://content.gpwiki.org/index.php/Faster_Ray_Traced_Terrain_Shadow_Maps#Optimized_Source_Code
	 */
	void shadeChunk(ChunkStub chunkStub, BufferedImage chunkImage, Map<Integer, ChunkStub> neighbors)
	{
		
		float h, hN, hW;
		float s=1f;
		int shaded;
		float slope;
		float intensity = 1f;
		boolean paintNight = chunkImage.getWidth()>16;
		
		if(chunkStub.slopes==null) {
			chunkStub.slopes = new float[16][16];
			float minNorm = chunkStub.worldHeight;
			float maxNorm = 0;
			for(int y=0; y<16; y++)
			{
				for(int x=0; x<16; x++)
				{				
					h = chunkStub.getSafeHeightValue(x, y);
					hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x, y-1);							
					hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x-1, y);
					slope = ((h/hN)+(h/hW))/2f;
					chunkStub.slopes[x][y] = slope;						
				}
			}
		}
		
		float sN, sNW, sW, sAvg;		
		for(int z=0; z<16; z++)
		{
			for(int x=0; x<16; x++)
			{				
				slope = chunkStub.slopes[x][z];
				
				sN = getBlockSlope(x, z, 0, -1, chunkStub, neighbors, slope);
				sNW = getBlockSlope(x, z, -1, -1, chunkStub, neighbors, slope);
				sW = getBlockSlope(x, z, -1, 0, chunkStub, neighbors, slope);
				sAvg = (sN+sNW+sW)/3f;
				
				if(slope<1) {
					
					if(slope<=sAvg) {
						slope = slope*.6f;
					} else if(slope>sAvg) {
						slope = (slope+sAvg)/2f;
					}
					s = Math.max(slope * .8f, .1f);
					
					shaded = darken(chunkImage.getRGB(x, z), s);
					chunkImage.setRGB(x,z,shaded);
					
					if(paintNight) {
						shaded = darken(chunkImage.getRGB(x+16, z), s);
						chunkImage.setRGB(x+16,z,shaded);
					}
					
				} else if(slope>1) {
					if(sAvg>1) {
						if(slope>=sAvg) {
							slope = slope*1.15f;
						}
					}
					s = (float) slope * 1.15f;
					s = Math.min(s, 1.4f);
					
					shaded = darken(chunkImage.getRGB(x, z), s);
					chunkImage.setRGB(x,z,shaded);
					
					if(paintNight) {
						shaded = darken(chunkImage.getRGB(x+16, z), s);
						chunkImage.setRGB(x+16,z,shaded);
					}
				} 
				 				
			}
		}
	 
	}
	
	public int darken(int original, float factor) {
		
		if(factor<0) {
			throw new IllegalArgumentException("factor can't be negative");
		}
		float bluer = (factor>=1) ? 1f : .9f;
		
		int r = Math.max(0, Math.min(255, (int) (((original >> 16) & 0xFF) * (factor*bluer))));
		int g = Math.max(0, Math.min(255, (int) (((original >> 8) & 0xFF) * (factor*bluer))));
		int b = Math.max(0, Math.min(255, (int) (((original >> 0) & 0xFF) * (factor))));

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
