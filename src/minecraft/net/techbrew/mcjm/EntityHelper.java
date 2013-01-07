package net.techbrew.mcjm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityBlaze;
import net.minecraft.src.EntityCaveSpider;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityDragon;
import net.minecraft.src.EntityEnderman;
import net.minecraft.src.EntityGhast;
import net.minecraft.src.EntityList;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMagmaCube;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntitySilverfish;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EntitySlime;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.EntityZombie;
import net.minecraft.src.World;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.MapBlocks;

public class EntityHelper {
	
	static BufferedImage playerImg, otherImg;
	public static HashMap<Class, String> entityMap = new HashMap<Class, String>();
	static HashMap<Class, BufferedImage> entityImageMap = new HashMap<Class, BufferedImage>();

	@SuppressWarnings("unchecked")
	public
	static List<Entity> getEntitiesNearby(Minecraft mc) {
		EntityPlayerSP player = mc.thePlayer;
		List<Entity> list = new ArrayList<Entity>();
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX + 1.0D, player.posY + 1.0D, player.posZ + 1.0D).expand(32D, 8D, 32D);
		for(Class entityClass : entityMap.keySet()) {
			list.addAll(mc.theWorld.getEntitiesWithinAABB(entityClass, bb));
		}		
		list.remove(mc.thePlayer);
		return list;
	}
		
	static {
		entityMap.put(EntityCreeper.class, "Creeper"); //$NON-NLS-1$
		entityMap.put(EntityPigZombie.class, "PigZombie"); //$NON-NLS-1$
		entityMap.put(EntityZombie.class, "Zombie"); //$NON-NLS-1$
		entityMap.put(EntitySkeleton.class, "Skeleton"); //$NON-NLS-1$
		entityMap.put(EntitySpider.class, "Spider"); //$NON-NLS-1$
		entityMap.put(EntityCaveSpider.class, "Spider"); //$NON-NLS-1$
		entityMap.put(EntityEnderman.class, "Enderman"); //$NON-NLS-1$
		entityMap.put(EntitySilverfish.class, "Silverfish"); //$NON-NLS-1$
		entityMap.put(EntityDragon.class, "Dragon"); //$NON-NLS-1$
		entityMap.put(EntityGhast.class, "Ghast"); //$NON-NLS-1$
		entityMap.put(EntitySlime.class, "Slime"); //$NON-NLS-1$
		entityMap.put(EntityMagmaCube.class, "MagmaCube"); //$NON-NLS-1$
		entityMap.put(EntityBlaze.class, "Blaze"); //$NON-NLS-1$
		entityMap.put(EntityWolf.class, "Wolf"); //$NON-NLS-1$
	}
	
	public static BufferedImage getEntityImage(Class<? extends Entity> entityClass) {
		if(!entityMap.containsKey(entityClass)) {
			try {
				JourneyMap.getLogger().severe("Can't find entry for entity: " + EntityList.getEntityString(entityClass.newInstance())); //$NON-NLS-1$
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JourneyMap.getLogger().severe("Can't find entry for entity class: " + entityClass); //$NON-NLS-1$
			}
			return null;
		}
		BufferedImage img = entityImageMap.get(entityClass);
		if(img==null) {
			 try {
				String png = "/net/techbrew/mcjm/web/" + entityMap.get(entityClass) + ".png";	//$NON-NLS-1$ //$NON-NLS-2$
				InputStream is = EntityHelper.class.getResourceAsStream(png);
				img = ImageIO.read(is);
				is.close();
				entityImageMap.put(entityClass, img);
			} catch (IOException e) {
				String error = Constants.getMessageJMERR17(entityClass);
				JourneyMap.getLogger().severe(error);
				JourneyMap.getLogger().severe(LogFormatter.toString(e));
				return null;
			}
		}
		return img;
	}
	
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
	 * Check whether player isn't under sky
	 * @param player
	 * @return
	 */
	public static boolean playerIsUnderground(EntityPlayerSP player) {
		
		if(player.worldObj.provider.hasNoSky) {
			return true;
		}
		
		final int posX = (int) Math.floor(player.posX);
		final int posY = (int) Math.floor(player.posY)-1;
		final int posZ = (int) Math.floor(player.posZ);
		final int offset=1;		
		int x=0,y=0,z=0,blockId=0;
		boolean isUnderground = true;
		
		check : {
			for(x = (posX-offset);x<=(posX+offset);x++) {
				for(z=(posZ-offset);z<=(posZ+offset);z++) {					
					y = posY+1;
					if(canSeeSky(player.worldObj, x, y, z)) {
						isUnderground = false;
						break check;
					}
				}
				
			}
		}
		//System.out.println("underground: " + isUnderground);
		return isUnderground;
	}
	
	/**
	 * Potentially dangerous to use anywhere other than for player's current position
	 * - seems to cause crashes when used with ChunkRenderer.paintUnderground()
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static boolean canSeeSky(World world, final int x, final int y, final int z) {
		boolean seeSky = true;
		int blockId;
		
		int topY = 256; //world.worldMaxY;
		if(y>=topY) {
			return true;
		}
		int checkY = topY;
		while(seeSky && checkY>y) {
			blockId = world.getBlockId(x, checkY, z);
			if(MapBlocks.sky.contains(blockId)) {
				checkY--;
			} else {
				seeSky = false;
				break;
			}
		}
		return seeSky;
	}
}
