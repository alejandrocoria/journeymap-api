package net.techbrew.mcjm.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Resource;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.ResourceManager;
import net.minecraft.src.ResourcePack;
import net.minecraft.src.ResourcePackRepository;
import net.minecraft.src.ResourcePackRepositoryEntry;
import net.minecraft.src.TextureAtlasSprite;
import net.minecraft.src.TextureMap;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.BlockInfo;
import net.techbrew.mcjm.render.MapBlocks;

public class IconLoader {
	
	Logger logger = JourneyMap.getLogger();
	BufferedImage blocksTexture;
	
	/**
	 * Must be instantiated on main minecraft thread where GL context is viable.
	 */
	public IconLoader() {
		initBlocksTexture();
	}
	
	public void initBlocksTexture() {
		
		try {
			int glid = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
			GL11.glBindTexture(3553, glid);
		    int width = GL11.glGetTexLevelParameteri(3553, 0, 4096);
		    int height = GL11.glGetTexLevelParameteri(3553, 0, 4097);
		    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		
		    GL11.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
		    BufferedImage image = new BufferedImage(width, height, 6);
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
		    this.blocksTexture = image;
		} catch(Throwable t) {
			logger.severe("Could not load blocksTexture: " + LogFormatter.toString(t));
		}
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
		
		try {
						
			logger.fine("Loading color for " + blockInfo.debugString());
			
	        Block block = Block.blocksList[blockInfo.id];

            if (block == null) {
            	logger.warning("No block type found");
            	
            	ItemBlock item = (ItemBlock) Item.itemsList[blockInfo.id];
            	if(item!=null) {
            		logger.warning("ItemBlocks not supported: " + item.itemID + ":" + item.getMetadata(0) + " " + item.getUnlocalizedName());
            	}

            } else {
            	int side = MapBlocks.side2Textures.contains(block.blockID) ? 2 : 1;
	            TextureAtlasSprite blockIcon = (TextureAtlasSprite) block.getIcon(side, blockInfo.meta);
	        	if(blockIcon==null) {
	        		logger.warning("No TextureAtlasSprite for " + blockInfo.debugString());
	        	} else {
	        		color = getColorForIcon(blockInfo, blockIcon);  
	        	}
            }
			
			return color;                           

		} catch (Throwable t) {
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
	        			logger.severe("Couldn't get RGB from BlocksTexture at " + x + "," + y + " for " + blockInfo.debugString());
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
	        	logger.warning("Unusable texture for " + blockInfo.debugString());
	        	r = g = b = 0;
	        }
			
	        // Set color
	        color = new Color(r,g,b);
			
	        // Determine alpha
	        float blockAlpha = 0f;		
	        if(MapBlocks.alphas.containsKey(blockInfo.id)) {
	        	blockAlpha = MapBlocks.alphas.get(blockInfo.id);
	        	//logger.info("Using transparency for " + blockInfo.debugString() + ": " + blockAlpha);
			} else if(blockInfo.getBlock().getRenderBlockPass()>0) {
				blockAlpha = a * 1.0f/255;
				MapBlocks.alphas.put(blockInfo.id, blockAlpha);
				// logger.info("Setting transparency for " + blockInfo.debugString() + ": " + blockAlpha);					
			}
	        blockInfo.setAlpha(blockAlpha);	        
							
		} catch (Throwable e1) {				
			logger.warning("Error deriving color for " + blockInfo.debugString());
			logger.severe(LogFormatter.toString(e1));
		} 
        
        if(color!=null) {
        	if(logger.isLoggable(Level.FINE)){
        		logger.fine("Derived color for " + blockInfo.debugString() + ": " + Integer.toHexString(color.getRGB()));
        	}
        }
        
        return color;
	}


}
