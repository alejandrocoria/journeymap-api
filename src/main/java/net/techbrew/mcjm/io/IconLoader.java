package net.techbrew.mcjm.io;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.cartography.BlockInfo;
import net.techbrew.mcjm.cartography.MapBlocks;
import net.techbrew.mcjm.log.LogFormatter;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IconLoader {
	
	Logger logger = JourneyMap.getLogger();
	final BufferedImage blocksTexture;
	HashSet<BlockInfo> failed = new HashSet<BlockInfo>();
	
	/**
	 * Must be instantiated on main minecraft thread where GL context is viable.
	 */
	public IconLoader() {
		blocksTexture = initBlocksTexture();
	}	
	
	public boolean failedFor(BlockInfo blockInfo) {
		return failed.contains(blockInfo);
	}
	
	/**
	 * Derive block color from the corresponding texture.
	 * @param blockInfo
	 * @return
	 */
	public Color loadBlockColor(BlockInfo blockInfo) {
		
		Color color = null;
		
		if(blocksTexture==null) {
			logger.warning("BlocksTexture not yet loaded");
			return null;					
		}
		
		if(failed.contains(blockInfo)){
			return null;
		}
		
		try {
				
			if(logger.isLoggable(Level.FINE)){
				logger.fine("Loading color for " + blockInfo);
			}

            int side = blockInfo.hasFlag(MapBlocks.Flag.Side2Texture) ? 2 : 1;
            TextureAtlasSprite blockIcon = null;
            while(blockIcon==null && side>=0) {
                blockIcon = (TextureAtlasSprite) blockInfo.getBlock().func_149691_a(side, blockInfo.key.meta);
                side--;
            }
            if(blockIcon==null) {
                logger.warning("Could not get Icon for " + blockInfo);
            } else {
                color = getColorForIcon(blockInfo, blockIcon);
            }
			
            if(color==null) {
            	failed.add(blockInfo);
            }
			return color;                           

		} catch (Throwable t) {
			failed.add(blockInfo);
			logger.severe("Error getting color: " + LogFormatter.toString(t));
			return null;
		}
	}
	

	
	Color getColorForIcon(BlockInfo blockInfo, TextureAtlasSprite icon) {
		
		Color color = null;		

        try {	    	
	        int count = 0;
	        int argb, alpha;
	    	int a=0, r=0, g=0, b=0;
	    	int x=0, y=0;
	    	int xStart = icon.getOriginX();
	    	int xStop = xStart + icon.getIconWidth();
	    	int yStart = icon.getOriginY();
	    	int yStop = yStart + icon.getIconHeight();
	    			    	
	        outer: for(x=xStart; x<xStop; x++) {
	        	for(y=yStart; y<yStop; y++) {
	        		try {
	        			argb = blocksTexture.getRGB(x, y);
	        		} catch(Throwable e) {
	        			logger.severe("Couldn't get RGB from BlocksTexture at " + x + "," + y + " for " + blockInfo);
	        			logger.severe(LogFormatter.toString(e));
	        			break outer;
	        		}
	        		alpha = (argb >> 24) & 0xFF; 
	        		if(alpha>0) {
	        			count++;
	        			a+= alpha;
	        			r+= (argb >> 16) & 0xFF;
	        			g+= (argb >> 8) & 0xFF;
	        			b+= (argb >> 0) & 0xFF;
	        		}
	        	}
	        }
	        
	        if(count>0) {    
	        	if(a>0) a = a/count;
	        	if(r>0) r = r/count;
	        	if(g>0) g = g/count;
	        	if(b>0) b = b/count;
	        } else {
	        	logger.warning("Unusable texture for " + blockInfo);
	        	r = g = b = 0;
	        }
			
	        // Set color
	        color = new Color(r,g,b);
			
	        // Determine alpha
	        float blockAlpha = 0f;
            Block block = blockInfo.getBlock();
	        if(MapBlocks.hasAlpha(block)) {
	        	blockAlpha = MapBlocks.getAlpha(block);
			} else if(blockInfo.getBlock().func_149701_w()>0) { // TODO FORGE:  should be getRenderBlockPass()
				blockAlpha = a * 1.0f/255;
				MapBlocks.setAlpha(block, blockAlpha);
			}
	        blockInfo.setAlpha(blockAlpha);	        
							
		} catch (Throwable e1) {				
			logger.warning("Error deriving color for " + blockInfo);
			logger.severe(LogFormatter.toString(e1));
		} 
        
        if(color!=null) {
        	if(logger.isLoggable(Level.FINE)){
        		logger.fine("Derived color for " + blockInfo + ": " + Integer.toHexString(color.getRGB()));
        	}
        } 
        
        return color;
	}

	private BufferedImage initBlocksTexture() {
		
		BufferedImage image = null;
		long start = System.currentTimeMillis();
		
		try {
			int glid = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
			GL11.glBindTexture(3553, glid);
		    int width = GL11.glGetTexLevelParameteri(3553, 0, 4096);
		    int height = GL11.glGetTexLevelParameteri(3553, 0, 4097);
		    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		
		    GL11.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
		    image = new BufferedImage(width, height, 6);
		    byteBuffer.position(0);
		    byte[] var4 = new byte[byteBuffer.remaining()];
		    byteBuffer.get(var4);
		
		    for (int var5 = 0; var5 < width; var5++) {
		      for (int var6 = 0; var6 < height; var6++)
		      {
		        int var7 = var6 * width * 4 + var5 * 4;
		        byte var8 = 0;
		        int var10 = var8 | (var4[(var7 + 2)] & 0xFF) << 0;
		        var10 |= (var4[(var7 + 1)] & 0xFF) << 8;
		        var10 |= (var4[(var7 + 0)] & 0xFF) << 16;
		        var10 |= (var4[(var7 + 3)] & 0xFF) << 24;
		        image.setRGB(var5, var6, var10);
		      }
		    }

		    long stop = System.currentTimeMillis();
		    logger.info("Got blockTexture image in " + (stop-start) + "ms");		    
		    
		} catch(Throwable t) {
			logger.severe("Could not load blocksTexture: " + LogFormatter.toString(t));
		}
		return image;
	}
	
}
