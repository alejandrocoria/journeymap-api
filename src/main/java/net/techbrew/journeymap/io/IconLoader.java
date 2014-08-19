package net.techbrew.journeymap.io;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IconLoader {

    final BufferedImage blocksTexture;
    Logger logger = JourneyMap.getLogger();
    DataCache dataCache = DataCache.instance();
    HashSet<BlockMD> failed = new HashSet<BlockMD>();

    /**
     * Must be instantiated on main minecraft thread where GL context is viable.
     */
    public IconLoader()
    {
        blocksTexture = initBlocksTexture();
    }

    public boolean failedFor(BlockMD blockMD)
    {
        return failed.contains(blockMD);
    }

    /**
     * Derive block color from the corresponding texture.
     *
     * @param blockMD
     * @return
     */
    public Color loadBlockColor(BlockMD blockMD)
    {

        Color color = null;

        if (blocksTexture == null)
        {
            logger.warning("BlocksTexture not yet loaded");
            return null;
        }

//        if (failed.contains(blockMD))
//        {
//            return null;
//        }

        try
        {

            IIcon blockIcon = getDirectIcon(blockMD);

            if (blockIcon == null)
            {
                if (blockMD.getBlock() instanceof ITileEntityProvider)
                {
                    logger.info("Ignoring TitleEntity without standard block texture: " + blockMD);
                    blockMD.addFlags(BlockUtils.Flag.TileEntity, BlockUtils.Flag.HasAir);
                    return null;
                }
            }

            if (blockIcon == null)
            {
                blockMD.addFlags(BlockUtils.Flag.Error);
                failed.add(blockMD);
                return null;
            }

            color = getColorForIcon(blockMD, blockIcon);
            if (color == null)
            {
                failed.add(blockMD);
                return null;
            }

            return color;
        }
        catch (Throwable t)
        {
            failed.add(blockMD);
            logger.severe("Error getting color: " + LogFormatter.toString(t));
            return null;
        }
    }

    private IIcon getDirectIcon(BlockMD blockMD)
    {
        IIcon blockIcon = null;

        if (blockMD.getBlock() instanceof BlockDoublePlant)
        {
            BlockDoublePlant blockDoublePlant = ((BlockDoublePlant) blockMD.getBlock());

            // Get the top icon
            blockIcon = blockDoublePlant.func_149888_a(true, blockMD.key.meta & BlockDoublePlant.field_149892_a.length);
            if (blockIcon.getIconName().contains("sunflower"))
            {
                // Sunflower front
                blockIcon = blockDoublePlant.sunflowerIcons[0];
            }
        }
        else
        {
            int side = blockMD.hasFlag(BlockUtils.Flag.Side2Texture) ? 2 : 1;
            while (blockIcon == null && side >= 0)
            {
                blockIcon = blockMD.getBlock().getIcon(side, blockMD.key.meta);
                side--;
            }
        }

        return blockIcon;
    }

    Color getColorForIcon(BlockMD blockMD, IIcon icon)
    {
        Color color = null;

        try
        {
            int count = 0;
            int argb, alpha;
            int a = 0, r = 0, g = 0, b = 0;
            int x = 0, y = 0;

            int xStart, yStart, xStop, yStop;
            BufferedImage textureImg;

            if(icon instanceof TextureAtlasSprite) {
                TextureAtlasSprite tas = (TextureAtlasSprite) icon;
                textureImg = blocksTexture;
                xStart = tas.getOriginX();
                yStart = tas.getOriginY();
                xStop = xStart + icon.getIconWidth();
                yStop = yStart + icon.getIconHeight();
            } else {
                textureImg = blocksTexture;

                xStart = (int) Math.round(((float) textureImg.getWidth()) * Math.min(icon.getMinU(), icon.getMaxU()));
                yStart = (int) Math.round(((float) textureImg.getHeight()) * Math.min(icon.getMinV(), icon.getMaxV()));
                int iconWidth = (int) Math.round(((float) textureImg.getWidth()) * Math.abs(icon.getMaxU() - icon.getMinU()));
                int iconHeight = (int) Math.round(((float) textureImg.getHeight()) * Math.abs(icon.getMaxV() - icon.getMinV()));

                xStop = xStart + iconWidth;
                yStop = yStart + iconHeight;
            }

            boolean unusable = true;
            if(textureImg!=null && textureImg.getWidth()>0)
            {
                outer: for(x=xStart; x<xStop; x++) {
                    inner: for(y=yStart; y<yStop; y++) {
                        try {
                            argb = textureImg.getRGB(x, y);
                        } catch(ArrayIndexOutOfBoundsException e) {
                            logger.warning("Bad index at " + x + "," + y + " for " + blockMD + ": " + e.getMessage());
                            continue; // Bugfix for some texturepacks that may not be reporting correct size?
                        } catch(Throwable e) {
                            logger.severe("Couldn't get RGB from BlocksTexture at " + x + "," + y + " for " + blockMD + ": " + e.getMessage());
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
                    unusable = false;
                    if(a>0) a = a/count;
                    if(r>0) r = r/count;
                    if(g>0) g = g/count;
                    if(b>0) b = b/count;
                }
            }
            else {
                logger.warning("Couldn't get texture for " + icon.getIconName() + " using blockid ");
            }

            if(unusable) {
                blockMD.addFlags(BlockUtils.Flag.Error);
                BlockUtils.setFlags(blockMD.getBlock(), BlockUtils.Flag.Error);
                String pattern = "Unusable texture for %s, icon=%s,texture coords %s,%s - %s,%s";
                logger.severe(String.format(pattern, blockMD, icon.getIconName(), xStart, yStart, xStop, yStop));
                r = g = b = 0;
            }

			
	        // Set color
	        color = new Color(r,g,b);
			
	        // Determine alpha
            Block block = blockMD.getBlock();
	        float blockAlpha = 1f;
            if(unusable) {
                blockAlpha = 0f;
            }
            else
            {
                if(BlockUtils.hasAlpha(block)) {
                    blockAlpha = BlockUtils.getAlpha(block);
                } else if(block.getRenderBlockPass()>0) {
                    blockAlpha = a * 1.0f/255; // try to use texture alpha
                    if(blockAlpha==1f) { // try to use light opacity
                        blockAlpha = block.getLightOpacity(FMLClientHandler.instance().getClient().theWorld, 0, 65, 0) / 255f;
                    }
                }
            }
            BlockUtils.setAlpha(block, blockAlpha);
	        blockMD.setAlpha(blockAlpha);
							
		} catch (Throwable e1) {				
			logger.warning("Error deriving color for " + blockMD + ": " + LogFormatter.toString(e1));
		} 
        
        if(color!=null) {
        	if(logger.isLoggable(Level.FINE)){
        		logger.fine("Derived color for " + blockMD + ": " + Integer.toHexString(color.getRGB()));
        	}
        } 
        
        return color;
	}

    private BufferedImage initBlocksTexture() {

        StatTimer timer = StatTimer.get("IconLoader.initBlocksTexture", 0);
        timer.start();

		BufferedImage image = null;

		try {
            int glid = FMLClientHandler.instance().getClient().getTextureManager().getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
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

            timer.stop();
		    
		} catch(Throwable t) {
			logger.severe("Could not load blocksTexture :" + t);
            timer.cancel();
		}

		return image;
	}
	
}
