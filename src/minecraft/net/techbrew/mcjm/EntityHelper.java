package net.techbrew.mcjm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityBlaze;
import net.minecraft.src.EntityCaveSpider;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityDragon;
import net.minecraft.src.EntityEnderman;
import net.minecraft.src.EntityGhast;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityList;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMagmaCube;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntitySilverfish;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EntitySlime;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.EntityZombie;
import net.minecraft.src.MathHelper;
import net.minecraft.src.World;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.MapBlocks;

public class EntityHelper {
	
	// TODO: make threadsafe
	static BufferedImage playerImg, otherImg;
	
	// TODO: make threadsafe
	static HashMap<String, BufferedImage> entityImageMap = new HashMap<String, BufferedImage>();

	private static int lateralDistance = 32;
	private static int verticalDistance = 16;
	
	public static List<EntityMob> getMobsNearby() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.theWorld.getEntitiesWithinAABB(EntityMob.class, getBB(mc.thePlayer));
	}
	
	public static List<EntityAnimal> getAnimalsNearby(boolean includeUntamed, boolean includeTamed) {
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		AxisAlignedBB bb = getBB(player);
		
		List<EntityAnimal> animals = null;
		
		if(!includeUntamed && !includeTamed) {
			animals = new ArrayList<EntityAnimal>(0);
		} else if(includeUntamed && includeTamed) {		
			animals = mc.theWorld.getEntitiesWithinAABB(EntityAnimal.class, bb);
		} else if(includeTamed) {			
			List<EntityTameable> tameable = mc.theWorld.getEntitiesWithinAABB(EntityTameable.class, bb);
			animals = new ArrayList<EntityAnimal>(tameable.size());
			for(EntityTameable animal : tameable) {
				if(isPetOf(animal, player)) {
					animals.add(animal);
				}
			}
		} else {
			animals = mc.theWorld.getEntitiesWithinAABB(EntityAnimal.class, bb);
			List<EntityAnimal> pets = new ArrayList<EntityAnimal>(animals.size());
			for(EntityAnimal animal : animals) {
				if(isPetOf(animal, player)) {
					pets.add(animal);
				}
			}
			animals.removeAll(pets);
		}
		
		return animals;
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
	public static boolean isPetOf(EntityAnimal animal, EntityPlayer player) {
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
				String png = "/net/techbrew/mcjm/web/" + entityName + ".png";	//$NON-NLS-1$ //$NON-NLS-2$
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
				String png = "/net/techbrew/mcjm/web/arrow.png";						 //$NON-NLS-1$
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
				String png = "/net/techbrew/mcjm/web/other.png";						 //$NON-NLS-1$
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
			String png = "/net/techbrew/mcjm/web/alert.png";						 //$NON-NLS-1$
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
	 * Get the entity's heading in degrees
	 * @param player
	 * @return
	 */
	public static double getHeading(EntityLiving entity) {
		double xHeading = -MathHelper.sin((entity.rotationYaw * 3.141593F) / 180F);
	    double zHeading = MathHelper.cos((entity.rotationYaw * 3.141593F) / 180F);
		double degrees = Math.atan2(xHeading, zHeading) * (180 / Math.PI);
	    if(degrees > 0 || degrees < 180) degrees = 180 - degrees;
	    return degrees;
	}
}
