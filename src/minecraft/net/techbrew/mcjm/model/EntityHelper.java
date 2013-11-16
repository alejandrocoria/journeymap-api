package net.techbrew.mcjm.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityGolem;
import net.minecraft.src.EntityHorse;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.EntityWaterMob;
import net.minecraft.src.IAnimals;
import net.minecraft.src.IBossDisplayData;
import net.minecraft.src.IMob;
import net.minecraft.src.IRangedAttackMob;
import net.minecraft.src.Minecraft;
import net.minecraft.src.RenderFacade;
import net.minecraft.src.RenderHorse;
import net.minecraft.src.RenderLiving;
import net.minecraft.src.RenderManager;
import net.minecraft.src.ResourceLocation;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.render.overlay.MapTexture;

public class EntityHelper {
	
	public static final String PLAYER_FILENAME = "steve.png";
	
	private static final double PI2 = 2*Math.PI;
	
	// TODO: make threadsafe
	static MapTexture locatorHostile, locatorNeutral, locatorOther, locatorPet, locatorPlayer, unknownImage;
	
	// TODO: make threadsafe
	static volatile HashMap<String, MapTexture> entityImageMap = new HashMap<String, MapTexture>();
	
	static volatile Map<String, MapTexture> skinImageMap = Collections.synchronizedMap(new HashMap<String, MapTexture>());
	
	static Method renderGetEntityTextureMethod;

	private static int lateralDistance = PropertyManager.getInstance().getInteger(PropertyManager.Key.CHUNK_OFFSET) * 8;
	private static int verticalDistance = lateralDistance/2;
	
	public static List getMobsNearby() {
		Minecraft mc = Minecraft.getMinecraft();
		
		AxisAlignedBB bb = getBB(mc.thePlayer);
		List list = mc.theWorld.getEntitiesWithinAABB(IMob.class, bb);
		list.addAll(mc.theWorld.getEntitiesWithinAABB(IBossDisplayData.class, bb));
		list.addAll(mc.theWorld.getEntitiesWithinAABB(IRangedAttackMob.class, bb));		
		return list;
	}
	
	public static List<EntityVillager> getVillagersNearby() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.theWorld.getEntitiesWithinAABB(EntityVillager.class, getBB(mc.thePlayer));
	}
	
	public static List<IAnimals> getAnimalsNearby() {
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		AxisAlignedBB bb = getBB(player);
		
		List<IAnimals> animals = mc.theWorld.getEntitiesWithinAABB(EntityAnimal.class, bb);
		animals.addAll(mc.theWorld.getEntitiesWithinAABB(EntityGolem.class, bb));
		animals.addAll(mc.theWorld.getEntitiesWithinAABB(EntityWaterMob.class, bb));
		
		return animals;
	}
	
	/**
	 * Get nearby non-player entities
	 * @return
	 */
	public static List<EntityPlayer> getPlayersNearby() {
		
		List<EntityPlayer> nearbyPlayers = new ArrayList<EntityPlayer>();
		
		Minecraft mc = Minecraft.getMinecraft();
		//if(!mc.isSingleplayer()) {
			EntityPlayerSP player = mc.thePlayer;
			AxisAlignedBB bb = getBB(player);
			nearbyPlayers = mc.theWorld.getEntitiesWithinAABB(EntityPlayer.class, bb);
			nearbyPlayers.remove(player);
		//}
		
		return nearbyPlayers;
	}
	
	/**
	 * Get a boundingbox to search nearby player.
	 * @param player
	 * @return
	 */
	private static AxisAlignedBB getBB(EntityPlayerSP player) {
		return AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);
	}
			
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static MapTexture getEntityImage(String filename) {
		MapTexture tex = entityImageMap.get(filename);
		if(tex==null) {
			BufferedImage img = FileHandler.getWebImage("entity/" + filename);	//$NON-NLS-1$ //$NON-NLS-2$
			if(img==null) {				
				tex = getUnknownImage();
			} else {	
				tex = new MapTexture(img);
			}
			entityImageMap.put(filename, tex);
		}
		return tex;
	}
	
	public static MapTexture getPlayerSkin(String username) {
		
		synchronized(skinImageMap) {
			MapTexture tex = skinImageMap.get(username);
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
					tex = new MapTexture(scaledImage, true);
				} else {
					tex = getUnknownImage();
				}
				skinImageMap.put(username, tex);
			}
			return tex;
		}			
	}

	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static MapTexture getHostileLocator() {
		if(locatorHostile==null) {
			BufferedImage img =  FileHandler.getWebImage("locator-hostile.png"); //$NON-NLS-1$	
			locatorHostile = new MapTexture(img);	
		}
		return locatorHostile;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static MapTexture getNeutralLocator() {
		if(locatorNeutral==null) {
			BufferedImage img =  FileHandler.getWebImage("locator-neutral.png"); //$NON-NLS-1$	
			locatorNeutral = new MapTexture(img);			
		}
		return locatorNeutral;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static MapTexture getOtherLocator() {
		if(locatorOther==null) {
			BufferedImage img =  FileHandler.getWebImage("locator-other.png"); //$NON-NLS-1$	
			locatorOther = new MapTexture(img);	
		}
		return locatorOther;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static MapTexture getPetLocator() {
		if(locatorPet==null) {
			BufferedImage img =  FileHandler.getWebImage("locator-pet.png"); //$NON-NLS-1$	
			locatorPet = new MapTexture(img);			
		}
		return locatorPet;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static MapTexture getPlayerImage() {
		if(locatorPlayer==null) {
			BufferedImage img =  FileHandler.getWebImage("locator-player.png"); //$NON-NLS-1$	
			locatorPlayer = new MapTexture(img);
		}
		return locatorPlayer;
	}
	
	/**
	 * @return
	 */
	public static synchronized MapTexture getUnknownImage() {
		if(unknownImage==null) {
			unknownImage = new MapTexture(FileHandler.getWebImage("entity/unknown.png"), true);
		}
		return unknownImage;
	}
	
	
	/**
	 * Get the entity's heading in degrees
	 * 
	 * @param player
	 * @return
	 */
	public static double getHeading(Entity entity) {
		if(entity instanceof EntityLiving) {
			return getHeading(((EntityLiving) entity).rotationYawHead);
		} else {
			return getHeading(entity.rotationYaw);
		}
	}
	
	/**
	 * Get the entity's heading in degrees,
	 * normalized to be between 0 and 360.
	 * 
	 * @param rotationYaw
	 * @return
	 */
	public static double getHeading(float rotationYaw) {
		double degrees = Math.round(rotationYaw % 360);
	    return degrees;
	}
	
	
	/**
	 * Get the simple name of the entity (without Entity prefix)
	 * @param entity
	 * @return
	 */
	public static String getFileName(Entity entity) {
		
		RenderLiving render = (RenderLiving) RenderManager.instance.getEntityRenderObject(entity);
		
		// Manually handle horses
		if(render instanceof RenderHorse) {			
			switch (((EntityHorse) entity).getHorseType())
	        {
		        case 0:
	            default:
	                return "horse/horse.png";
	
	            case 1:
	                return "horse/donkey.png";
	
	            case 2:
	                return "horse/mule.png";
	
	            case 3:
	                return "horse/zombiehorse.png";
	
	            case 4:
	                return "horse/skeletonhorse.png";
	        }
		}
		
		// Non-horse mobs
		ResourceLocation loc = RenderFacade.getEntityTexture(render, entity);
		String tex = loc.getResourcePath();
		String search = "/entity/";
		int i = tex.lastIndexOf(search);
		if(i>=0) {
			tex = tex.substring(i+search.length());
		} 
		return tex;
	}
	
	public static class EntityMapComparator implements Comparator<Map> {

		@Override
		public int compare(Map o1, Map o2) {
			
			Integer o1rank = 0;
			Integer o2rank = 0;
			if(o1.containsKey(EntityKey.customName)) {
				o1rank++;
			} else if(o1.containsKey(EntityKey.username)) {
				o1rank+=2;
			}
			if(o2.containsKey(EntityKey.customName)) {
				o2rank++;
			} else if(o2.containsKey(EntityKey.username)) {
				o2rank+=2;
			}
			
			return o1rank.compareTo(o2rank);
		}
		
	}
	
	public static void clearCaches() {
		entityImageMap.clear();
		skinImageMap.clear();
	}
}
