package net.techbrew.mcjm.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityCreature;
import net.minecraft.src.EntityGolem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.EntityWaterMob;
import net.minecraft.src.IAnimals;
import net.minecraft.src.IBossDisplayData;
import net.minecraft.src.IMob;
import net.minecraft.src.IRangedAttackMob;
import net.minecraft.src.MathHelper;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;

public class EntityHelper {
	
	public static final String PLAYER_FILENAME = "char.png";
	
	// TODO: make threadsafe
	static BufferedImage locatorHostile, locatorNeutral, locatorOther, locatorPet, locatorPlayer;
	
	// TODO: make threadsafe
	static HashMap<String, BufferedImage> entityImageMap = new HashMap<String, BufferedImage>();

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
		return getAnimalsNearby(false);
	}
	
	public static List<IAnimals> getAnimalsNearby(boolean excludePets) {
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		AxisAlignedBB bb = getBB(player);
		
		List<EntityCreature> animals = mc.theWorld.getEntitiesWithinAABB(EntityAnimal.class, bb);
		animals.addAll(mc.theWorld.getEntitiesWithinAABB(EntityGolem.class, bb));
		animals.addAll(mc.theWorld.getEntitiesWithinAABB(EntityWaterMob.class, bb));
		
		List<IAnimals> keep = new ArrayList<IAnimals>(animals.size());
		for(EntityLiving animal : animals) {
			if(!excludePets) {
				keep.add((IAnimals) animal);
			} else if(!isPetOf((IAnimals) animal, player)) {
				keep.add((IAnimals) animal);
			}
		}

		return keep;
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
	 * Whether an animal is the tamed pet of player.
	 * @param animal
	 * @param player
	 * @return
	 */
	public static boolean isPetOf(IAnimals animal, EntityPlayer player) {
		if(animal instanceof EntityTameable) {
			return player.equals(((EntityTameable) animal).getOwner());
		} else {
			return false;
		}
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
		return FileHandler.getImage("unknown.png");
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
	 * Get the entity's heading in radians
	 * 
	 * @param player
	 * @return
	 */
	public static double getHeading(float rotationYaw) {
		double degrees = Math.round(rotationYaw % 360);
	    double radians = (degrees * Math.PI) / 180;
	    return radians;
	}
	
	
	/**
	 * Get the simple name of the entity (without Entity prefix)
	 * @param entity
	 * @return
	 */
	public static String getFileName(Entity entity) {
		String tex = entity.getTexture();
		int i = tex.lastIndexOf('/');
		return tex.substring(i+1);
	}
}
