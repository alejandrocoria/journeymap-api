package net.techbrew.mcjm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
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
import net.minecraft.src.IMob;
import net.minecraft.src.MathHelper;
import net.techbrew.mcjm.io.FileHandler;

public class EntityHelper {
	
	// TODO: make threadsafe
	static BufferedImage playerImg, otherImg;
	
	// TODO: make threadsafe
	static HashMap<String, BufferedImage> entityImageMap = new HashMap<String, BufferedImage>();

	private static int lateralDistance = 32;
	private static int verticalDistance = 8;
	
	public static List<IMob> getMobsNearby() {
		Minecraft mc = Minecraft.getMinecraft();
		
		return mc.theWorld.getEntitiesWithinAABB(IMob.class, getBB(mc.thePlayer));
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
		if(!mc.isSingleplayer()) {
			EntityPlayerSP player = mc.thePlayer;
			AxisAlignedBB bb = getBB(player);
			nearbyPlayers = mc.theWorld.getEntitiesWithinAABB(EntityPlayer.class, bb);
			nearbyPlayers.remove(player);
		}
		
		return nearbyPlayers;
	}
	
	/**
	 * Get a boundingbox to search nearby player.
	 * @param player
	 * @return
	 */
	private static AxisAlignedBB getBB(EntityPlayerSP player) {
		return AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX + 1.0D, player.posY + 1.0D, player.posZ + 1.0D).expand(lateralDistance, verticalDistance, lateralDistance);
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
	public static BufferedImage getEntityImage(String entityName) {
		BufferedImage img = entityImageMap.get(entityName);
		if(img==null) {
			img = getUnknownImage(); // backup plan
			try {
				String png = FileHandler.WEB_DIR + "/img/entity/" + entityName + ".png";	//$NON-NLS-1$ //$NON-NLS-2$
				InputStream is = EntityHelper.class.getResourceAsStream(png);
				img = ImageIO.read(is);
				is.close();		
			} catch (Exception e) {
				String error = Constants.getMessageJMERR17(entityName);
				JourneyMap.getLogger().severe(error);			
			}
			entityImageMap.put(entityName, img);
		}
		return img;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getPlayerImage() {
		if(playerImg==null) {
			 try {
				String png = FileHandler.WEB_DIR + "/img/locator-player.png";						 //$NON-NLS-1$
				InputStream is = EntityHelper.class.getResourceAsStream(png);
				playerImg = ImageIO.read(is);
				is.close();
			} catch (IOException e) {
				String error = Constants.getMessageJMERR17(e.getMessage());
				JourneyMap.getLogger().severe(error);
				return null;
			}
		}
		return playerImg;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getOtherImage() {
		if(otherImg==null) {
			 try {
				String png = FileHandler.WEB_DIR + "/img/entity/other.png";						 //$NON-NLS-1$
				InputStream is = EntityHelper.class.getResourceAsStream(png);
				otherImg = ImageIO.read(is);
				is.close();
			} catch (IOException e) {
				String error = Constants.getMessageJMERR17(e.getMessage());
				JourneyMap.getLogger().severe(error);
				return null;
			}
		}
		return otherImg;
	}
	
	/**
	 * TODO: Not threadsafe
	 * @return
	 */
	public static BufferedImage getUnknownImage() {
		BufferedImage img = null;
		try {
			String png = FileHandler.WEB_DIR + "/img/alert.png";						 //$NON-NLS-1$
			InputStream is = EntityHelper.class.getResourceAsStream(png);
			img = ImageIO.read(is);
			is.close();
		} catch (IOException e) {
			String error = Constants.getMessageJMERR17(e.getMessage());
			JourneyMap.getLogger().severe(error);
			return null;
		}
		return img;
	}
	
	
	/**
	 * Get the entity's heading in radians
	 * 
	 * @param player
	 * @return
	 */
	public static double getHeading(EntityLiving entity) {
		double degrees = Math.round(entity.rotationYaw % 360);
	    double radians = (degrees * Math.PI) / 180;
	    return radians;
	}
}
