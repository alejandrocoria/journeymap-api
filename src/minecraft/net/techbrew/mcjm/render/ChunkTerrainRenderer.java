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

public class ChunkTerrainRenderer  {
	
	/**
	 * http://content.gpwiki.org/index.php/Faster_Ray_Traced_Terrain_Shadow_Maps#Optimized_Source_Code
	 */
	void shadeChunk(BufferedImage chunkImage, ChunkStub chunkStub, Map<Integer, ChunkStub> neighbors, boolean paintNight)
	{
		
//		float h, hN, hW;
//		float s=1f;
//		int shaded;
//		float slope;
//		float intensity = 1f;
//
//		if(chunkStub.slopes==null) {
//			chunkStub.slopes = new float[16][16];
//			float minNorm = chunkStub.worldHeight;
//			float maxNorm = 0;
//			for(int y=0; y<16; y++)
//			{
//				for(int x=0; x<16; x++)
//				{				
//					h = chunkStub.getSafeHeightValue(x, y);
//					hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x, y-1);							
//					hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x-1, y);
//					slope = ((h/hN)+(h/hW))/2f;
//					chunkStub.slopes[x][y] = slope;						
//				}
//			}
//		}
//		
//		BlockInfo blockInfo;
//		float sN, sNW, sW, sAvg;		
//		for(int z=0; z<16; z++)
//		{
//			for(int x=0; x<16; x++)
//			{				
//				slope = chunkStub.slopes[x][z];
//				
//				sN = getBlockSlope(x, z, 0, -1, chunkStub, neighbors, slope);
//				sNW = getBlockSlope(x, z, -1, -1, chunkStub, neighbors, slope);
//				sW = getBlockSlope(x, z, -1, 0, chunkStub, neighbors, slope);
//				sAvg = (sN+sNW+sW)/3f;
//				
//				if(slope<1) {
//					
//					if(slope<=sAvg) {
//						slope = slope*.6f;
//					} else if(slope>sAvg) {
//						slope = (slope+sAvg)/2f;
//					}
//					s = Math.max(slope * .8f, .1f);
//					
//					shaded = shade(chunkImage.getRGB(x, z), s);
//					chunkImage.setRGB(x,z,shaded);
//					
//					if(paintNight) {
//						shaded = shade(chunkImage.getRGB(x+16, z), s);
//						chunkImage.setRGB(x+16,z,shaded);
//					}
//					
//				} else if(slope>1) {
//					
//					if(sAvg>1) {
//						if(slope>=sAvg) {
//							slope = slope*1.2f;
//						}
//					}
//					s = (float) slope * 1.2f;
//					s = Math.min(s, 1.4f);
//					
//					shaded = shade(chunkImage.getRGB(x, z), s);
//					chunkImage.setRGB(x,z,shaded);
//					
//					if(paintNight) {
//						shaded = shade(chunkImage.getRGB(x+16, z), s);
//						chunkImage.setRGB(x+16,z,shaded);
//					}
//				} 
//				 				
//			}
//		}
	 
	}
	
	public int shade(int original, float factor) {
		
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
	
	
	


	

}
