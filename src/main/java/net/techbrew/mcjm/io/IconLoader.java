package net.techbrew.mcjm.io;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.BlockMD;
import net.techbrew.mcjm.model.BlockUtils;
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
	HashSet<BlockMD> failed = new HashSet<BlockMD>();
	
	/**
	 * Must be instantiated on main minecraft thread where GL context is viable.
	 */
	public IconLoader() {
		blocksTexture = initBlocksTexture();
	}	
	
	public boolean failedFor(BlockMD blockMD) {
		return failed.contains(blockMD);
	}
	
	/**
	 * Derive block color from the corresponding texture.
	 * @param blockMD
	 * @return
	 */
	public Color loadBlockColor(BlockMD blockMD) {
		
		Color color = null;
		
		if(blocksTexture==null) {
			logger.warning("BlocksTexture not yet loaded");
			return null;					
		}
		
		if(failed.contains(blockMD)){
			return null;
		}
		
		try {
				
			if(logger.isLoggable(Level.FINE)){
				logger.fine("Loading color for " + blockMD);
			}

            int side = blockMD.hasFlag(BlockUtils.Flag.Side2Texture) ? 2 : 1;
            TextureAtlasSprite blockIcon = null;
            while(blockIcon==null && side>=0) {
                blockIcon = (TextureAtlasSprite) blockMD.getBlock().getIcon(side, blockMD.key.meta);
                side--;
            }
            if(blockIcon==null) {
                logger.warning("Could not get Icon for " + blockMD);
            } else {
                color = getColorForIcon(blockMD, blockIcon);
            }
			
            if(color==null) {
            	failed.add(blockMD);
            }
			return color;                           

		} catch (Throwable t) {
			failed.add(blockMD);
			logger.severe("Error getting color: " + LogFormatter.toString(t));
			return null;
		}
	}
	

	
	Color getColorForIcon(BlockMD blockMD, TextureAtlasSprite icon) {
		
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
	        			logger.severe("Couldn't get RGB from BlocksTexture at " + x + "," + y + " for " + blockMD);
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
	        	logger.warning("Unusable texture for " + blockMD);
	        	r = g = b = 0;
	        }
			
	        // Set color
	        color = new Color(r,g,b);
			
	        // Determine alpha
	        float blockAlpha = 1f;
            Block block = blockMD.getBlock();
	        if(BlockUtils.hasAlpha(block)) {
	        	blockAlpha = BlockUtils.getAlpha(block);
			} else if(blockMD.getBlock().getRenderBlockPass()>0) {
				blockAlpha = a * 1.0f/255;
			}
	        blockMD.setAlpha(blockAlpha);
							
		} catch (Throwable e1) {				
			logger.warning("Error deriving color for " + blockMD);
			logger.severe(LogFormatter.toString(e1));
		} 
        
        if(color!=null) {
        	if(logger.isLoggable(Level.FINE)){
        		logger.fine("Derived color for " + blockMD + ": " + Integer.toHexString(color.getRGB()));
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
