package net.techbrew.mcjm.model;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
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
			switch (((EntityHorse) entity).func_110265_bP())
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
		
		// All other mobs	
		try {
			
			//Method m = Render.class.getMethod("func_110775_a", Entity.class);
			if(renderGetEntityTextureMethod==null) {
				for(Method m : Render.class.getMethods()){
					Class[] pTypes = m.getParameterTypes();
					if(pTypes.length==1) {
						if(Entity.class.equals(pTypes[0])){
							renderGetEntityTextureMethod = m;
							m.setAccessible(true);
							break;
						}
					}
				}
			}
			if(renderGetEntityTextureMethod!=null) {
				ResourceLocation loc = (ResourceLocation) renderGetEntityTextureMethod.invoke(render, entity);
				
				String tex = loc.func_110623_a();
				String search = "/entity/";
				int i = tex.lastIndexOf(search);
				if(i>=0) {
					tex = tex.substring(i+search.length());
				} 
				return tex;
			} else {
				return null;
			}
			
		} catch (Exception e) {
			JourneyMap.getLogger().warning("Can't get mob resource from Render.class aka " + render.getClass().getSimpleName() + " for " + entity.getEntityName() + " because " + e);
			return null;
		} 
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
