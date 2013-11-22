package net.techbrew.mcjm.render.texture;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionImageHandler;

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

    public static TextureImpl newUnmanagedTexture(BufferedImage image) {
    	return new TextureImpl(image);
    }
    
    public static TextureImpl newUnmanagedTexture(BufferedImage image, boolean retain) {
    	return new TextureImpl(image, retain);
    }
    
    public static enum Name {
    	Waypoint, Deathpoint, WaypointOffscreen, Logo, LocatorHostile, LocatorNeutral, LocatorOther, LocatorPet, LocatorPlayer, UnknownEntity;
    }
    
    private final Map<Name, TextureImpl> namedTextures = Collections.synchronizedMap(new HashMap<Name, TextureImpl>(Name.values().length + (Name.values().length/2) + 1));
    
    private final Map<String, TextureImpl> skinImageMap = Collections.synchronizedMap(new HashMap<String, TextureImpl>());    
    
    private final Map<String, TextureImpl> entityImageMap = Collections.synchronizedMap(new HashMap<String, TextureImpl>());
    
    /*************************************************/
    
	private TextureImpl getNamedTexture(Name name, String filename, boolean retain) {
		synchronized(namedTextures) {
			TextureImpl tex = namedTextures.get(name);
			if(tex==null) {
				BufferedImage img = FileHandler.getWebImage(filename);
				tex = new TextureImpl(img, retain);
				namedTextures.put(name, tex);
			}
			return tex;			
		}
	}
	
	public TextureImpl getWaypoint() {
		return getNamedTexture(Name.Waypoint, "waypoint.png", false); //$NON-NLS-1$
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
	
	public TextureImpl getUnknownEntity() {
		return getNamedTexture(Name.UnknownEntity, "entity/unknown.png", true); //$NON-NLS-1$
	}
	
	/*****************************************************/
	
	public TextureImpl getEntityImage(String filename) {
		
		synchronized(entityImageMap) {
			TextureImpl tex = entityImageMap.get(filename);
			if(tex==null) {
				BufferedImage img = FileHandler.getCustomizableImage("entity/" + filename, getUnknownEntity().getImage()); //$NON-NLS-1$	
				tex = new TextureImpl(img);
				entityImageMap.put(filename, tex);
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
		// TODO
	}
}
