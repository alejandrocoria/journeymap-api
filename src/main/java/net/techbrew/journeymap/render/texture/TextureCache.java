package net.techbrew.journeymap.render.texture;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.thread.JMThreadFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TODO:  Make this actually act like a cache.  
 * For now it's just a dumping ground for all textures that need to be retained
 *
 * @author mwoodman
 *
 */
public class TextureCache {

	private static class Holder {
        private static final TextureCache INSTANCE = new TextureCache();
    }

    public static TextureCache instance() {
        return Holder.INSTANCE;
    }

    private TextureCache()
    {
    }

    public static DynamicTexture newTexture(String path) {
        ResourceLocation loc = new ResourceLocation(path);
        DynamicTexture texture = null;
        InputStream is = null;
        try {
            is = JourneyMap.class.getResourceAsStream(path);
            texture = new DynamicTexture(ImageIO.read(is));
        } catch(Exception e) {
            JourneyMap.getLogger().severe("Can't get icon for " + loc + ": " + LogFormatter.toString(e));
            if(is!=null) {
                try {
                    is.close();
                } catch (IOException e1) {
                }
            }
        }
        return texture;
    }
    
    public static enum Name {
        MinimapSmallSquare, MinimapMediumSquare, MinimapLargeSquare, MinimapCustomSquare, MinimapSmallCircle, MinimapLargeCircle, Waypoint, Deathpoint, WaypointOffscreen, WaypointEdit, Logo, LocatorHostile, LocatorNeutral, LocatorOther, LocatorPet, LocatorPlayer, LocatorPlayerSmall, ColorPicker, UnknownEntity;
    }
    
    private final Map<Name, TextureImpl> namedTextures = Collections.synchronizedMap(new HashMap<Name, TextureImpl>(Name.values().length + (Name.values().length/2) + 1));

    private final Map<String, TextureImpl> customTextures = Collections.synchronizedMap(new HashMap<String, TextureImpl>(3));

    private final Map<String, TextureImpl> skinImageMap = Collections.synchronizedMap(new HashMap<String, TextureImpl>());    
    
    private final Map<String, TextureImpl> entityImageMap = Collections.synchronizedMap(new HashMap<String, TextureImpl>());

    private ThreadPoolExecutor texExec = (ThreadPoolExecutor) Executors.newFixedThreadPool(3, new JMThreadFactory("texture"));

    /*************************************************/

    public Future<DelayedTexture> prepareImage(final Integer glId, final BufferedImage image, final File worldDir, final ChunkCoordIntPair startCoord, final ChunkCoordIntPair endCoord, final Constants.MapType mapType,
                                               final Integer vSlice, final int dimension, final Boolean useCache, final Integer imageWidth, final Integer imageHeight, final boolean showGrid, final float alpha)
    {
        Future<DelayedTexture> future = texExec.submit(new Callable<DelayedTexture>()
        {
            @Override
            public DelayedTexture call() throws Exception
            {
                BufferedImage chunksImage = RegionImageHandler.getMergedChunks(worldDir, startCoord, endCoord, mapType, vSlice, dimension, useCache, image, imageWidth, imageHeight, true, showGrid);
                if(chunksImage==null){
                    chunksImage = RegionImageHandler.createBlankImage(imageWidth, imageHeight);
                }
                else if(alpha<1f)
                {
                    BufferedImage temp = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = (Graphics2D) temp.getGraphics();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g.drawImage(chunksImage, 0, 0, imageWidth, imageHeight, null);
                    chunksImage = temp;
                    g.dispose();
                }
                return new DelayedTexture(glId, chunksImage, null);
            }
        });
        return future;
    }
    
    /*************************************************/

//    public TextureImpl getCustomTexture(String filename, boolean retain) {
//        synchronized(customTextures)
//        {
//            TextureImpl tex = customTextures.get(filename);
//            if(tex==null || (!tex.hasImage() && retain)) {
//                BufferedImage img = FileHandler.getImage(filename);
//                if(img==null){
//                    img = getUnknownEntity().getImage();
//                }
//                if(img!=null){
//                    if(tex!=null){
//                        tex.deleteTexture();
//                    }
//                    tex = new TextureImpl(img, retain);
//                    customTextures.put(filename, tex);
//                }
//            }
//            return tex;
//        }
//    }

	private TextureImpl getNamedTexture(Name name, String filename, boolean retain) {
		synchronized(namedTextures) {
			TextureImpl tex = namedTextures.get(name);
			if(tex==null || (!tex.hasImage() && retain)) {
				BufferedImage img = FileHandler.getWebImage(filename);
                if(img==null){
                    img = getUnknownEntity().getImage();
                }
                if(img!=null){
                    if(tex!=null){
                        tex.deleteTexture();
                    }
                    tex = new TextureImpl(img, retain);
                    namedTextures.put(name, tex);
                }
			}
			return tex;			
		}
	}

    public TextureImpl getMinimapSmallSquare() {
        return getNamedTexture(Name.MinimapSmallSquare, "minimap/minimap-square-128.png", false); //$NON-NLS-1$
    }

    public TextureImpl getMinimapSquare(int size)
    {
        final BufferedImage original = FileHandler.getWebImage("minimap/minimap-square-512.png");
        BufferedImage custom = new BufferedImage(size, size, original.getType());

        Graphics2D g = custom.createGraphics();
        //RegionImageHandler.initRenderingHints(g);

        g.drawImage(custom, 0, 0, null);
        g.dispose();

        TextureImpl tex = namedTextures.get(Name.MinimapCustomSquare);
        if(tex!=null){
            tex.deleteTexture();
        }
        tex = new TextureImpl(original, true);
        namedTextures.put(Name.MinimapCustomSquare, tex);
        return tex;
    }

    public TextureImpl getMinimapMediumSquare() {
        return getNamedTexture(Name.MinimapMediumSquare, "minimap/minimap-square-256.png", false); //$NON-NLS-1$
    }

    public TextureImpl getMinimapLargeSquare() {
        return getNamedTexture(Name.MinimapLargeSquare, "minimap/minimap-square-512.png", false); //$NON-NLS-1$
    }

    public TextureImpl getMinimapSmallCircle() {
        return getNamedTexture(Name.MinimapSmallCircle, "minimap/minimap-circle-256.png", false); //$NON-NLS-1$
    }

    public TextureImpl getMinimapSmallCircleMask() {
        return getNamedTexture(Name.MinimapSmallCircle, "minimap/minimap-circle-mask-256.png", false); //$NON-NLS-1$
    }

    public TextureImpl getMinimapLargeCircle() {
        return getNamedTexture(Name.MinimapLargeCircle, "minimap/minimap-circle-512.png", false); //$NON-NLS-1$
    }

    public TextureImpl getMinimapLargeCircleMask() {
        return getNamedTexture(Name.MinimapLargeCircle, "minimap/minimap-circle-mask-512.png", false); //$NON-NLS-1$
    }

	public TextureImpl getWaypoint() {
		return getNamedTexture(Name.Waypoint, "waypoint.png", false); //$NON-NLS-1$
	}
	
	public TextureImpl getWaypointEdit() {
		return getNamedTexture(Name.WaypointEdit, "waypoint-edit.png", false); //$NON-NLS-1$
	}

    public TextureImpl getWaypointOffscreen() {
        return getNamedTexture(Name.WaypointOffscreen, "waypoint-offscreen.png", false); //$NON-NLS-1$
    }
	
	public TextureImpl getDeathpoint() {
		return getNamedTexture(Name.Deathpoint, "waypoint-death.png", false); //$NON-NLS-1$
	}
	
	public TextureImpl getLogo() {
		return getNamedTexture(Name.Logo, "ico/journeymap40.png", false); //$NON-NLS-1$
	}

	public TextureImpl getHostileLocator() {
		return getNamedTexture(Name.LocatorHostile, "locator-hostile.png", false); //$NON-NLS-1$
	}
	
	public TextureImpl getNeutralLocator() {
		return getNamedTexture(Name.LocatorNeutral, "locator-neutral.png", false); //$NON-NLS-1$
	}
	
	public TextureImpl getOtherLocator() {
		return getNamedTexture(Name.LocatorOther, "locator-other.png", false); //$NON-NLS-1$
	}
	
	public TextureImpl getPetLocator() {
		return getNamedTexture(Name.LocatorPet, "locator-pet.png", false); //$NON-NLS-1$
	}
	
	public TextureImpl getPlayerLocator() {
		return getNamedTexture(Name.LocatorPlayer, "locator-player.png", false); //$NON-NLS-1$
	}

    public TextureImpl getPlayerLocatorSmall() {
        return getNamedTexture(Name.LocatorPlayerSmall, "locator-player-sm.png", false); //$NON-NLS-1$
    }

    public TextureImpl getColorPicker() {
        return getNamedTexture(Name.ColorPicker, "colorpick.png", true); //$NON-NLS-1$
    }
	
	public TextureImpl getUnknownEntity() {
		return getNamedTexture(Name.UnknownEntity, "unknown.png", true); //$NON-NLS-1$
	}
	
	/*****************************************************/
	
	public TextureImpl getEntityIconTexture(String setName, String iconPath) {

        String texName = String.format("%s/%s", setName, iconPath);
		synchronized(entityImageMap) {
			TextureImpl tex = entityImageMap.get(texName);
            if(tex==null || (!tex.hasImage() && tex.retainImage)) {
				BufferedImage img = FileHandler.getEntityIconFromFile(setName, iconPath, getUnknownEntity().getImage()); //$NON-NLS-1$
                if(img!=null){
                    if(tex!=null) {
                        tex.deleteTexture();
                    }
                    tex = new TextureImpl(img);
                    entityImageMap.put(texName, tex);
                }
			}
			return tex;
		}		
	}
	
	/*****************************************************/
	
	public TextureImpl getPlayerSkin(String username) {
		
		synchronized(skinImageMap) {
			TextureImpl tex = skinImageMap.get(username);
			if(tex==null) {				
				BufferedImage img = null;
				try {
					URL url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + username + ".png");
					img = ImageIO.read(url).getSubimage(8, 8, 8, 8);
					
				} catch (Throwable e) {
					try {
						URL url = new URL("http://s3.amazonaws.com/MinecraftSkins/char.png");
						img = ImageIO.read(url).getSubimage(8, 8, 8, 8);
					} catch (Throwable e2) {
						JourneyMap.getLogger().warning("Can't get skin image for " + username + ": " + e2.getMessage());
					}
				}
				
				if(img!=null) {			
					final BufferedImage scaledImage = new BufferedImage(24, 24, img.getType());
					final Graphics2D g = RegionImageHandler.initRenderingHints(scaledImage.createGraphics());
					g.drawImage(img, 0, 0, 24, 24, null);
					g.dispose();
					tex = new TextureImpl(scaledImage, true);
				} else {
					tex = getUnknownEntity();
				}
				skinImageMap.put(username, tex);
			}
			return tex;
		}			
	}

	public void purge() {
		
	}
}
