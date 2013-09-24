package net.techbrew.mcjm.model;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Utilities;

import net.minecraft.src.Minecraft;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.Render;
import net.minecraft.src.RenderFacade;
import net.minecraft.src.RenderHorse;
import net.minecraft.src.RenderLiving;
import net.minecraft.src.RenderManager;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.IRangedAttackMob;
import net.minecraft.src.IBossDisplayData;
import net.minecraft.src.EntityGolem;
import net.minecraft.src.IMob;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityHorse;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.EntityWaterMob;
import net.minecraft.src.IAnimals;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.mod_JourneyMap;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;

public class EntityHelper {
	
	public static final String PLAYER_FILENAME = "steve.png";
	
	private static final double PI2 = 2*Math.PI;
	
	// TODO: make threadsafe
	static BufferedImage locatorHostile, locatorNeutral, locatorOther, locatorPet, locatorPlayer;
	
	// TODO: make threadsafe
	static HashMap<String, BufferedImage> entityImageMap = new HashMap<String, BufferedImage>();
	
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
	public static BufferedImage getEntityImage(String filename) {
		BufferedImage img = entityImageMap.get(filename);
		if(img==null) {
			img = FileHandler.getImage("entity/" + filename);	//$NON-NLS-1$ //$NON-NLS-2$
			if(img==null) {				
				img = getUnknownImage(); // fall back to unknown image
			}			
			entityImageMap.put(filename, img);
		}
		return img;
	}

	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getHostileLocator() {
		if(locatorHostile==null) {
			locatorHostile = FileHandler.getImage("locator-hostile.png"); //$NON-NLS-1$			
		}
		return locatorHostile;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getNeutralLocator() {
		if(locatorNeutral==null) {
			locatorNeutral = FileHandler.getImage("locator-neutral.png"); //$NON-NLS-1$			
		}
		return locatorNeutral;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getOtherLocator() {
		if(locatorOther==null) {
			locatorOther = FileHandler.getImage("locator-other.png"); //$NON-NLS-1$			
		}
		return locatorOther;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getPetLocator() {
		if(locatorPet==null) {
			locatorPet = FileHandler.getImage("locator-pet.png"); //$NON-NLS-1$			
		}
		return locatorPet;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getPlayerImage() {
		if(locatorPlayer==null) {
			locatorPlayer = FileHandler.getImage("locator-player.png"); //$NON-NLS-1$	
		}
		return locatorPlayer;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getUnknownImage() {
		return FileHandler.getImage("entity/unknown.png");
	}
	
	
	/**
	 * Get the entity's heading in radians
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
	 * Get the entity's heading in radians,
	 * normalized to be between 0 and 2*Pi.
	 * 
	 * @param rotationYaw
	 * @return
	 */
	public static double getHeading(float rotationYaw) {
		double degrees = Math.round(rotationYaw % 360);
	    double radians = (degrees * Math.PI) / 180;
	    
	    // Clamp between 0 and 2PI
		if(radians<PI2 || radians > PI2) {
			radians = radians % PI2;
		}
		if(radians<0) radians = PI2+radians;
	    
	    return radians;
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
}
