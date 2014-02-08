package net.techbrew.journeymap.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderFacade;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.StatTimer;

import java.util.*;

;

public class EntityHelper {
	
    private static int MAX_ENTITIES = 16;
	private static int lateralDistance = PropertyManager.getInstance().getInteger(PropertyManager.Key.CHUNK_OFFSET) * 8;
	private static int verticalDistance = lateralDistance/2;

    public static List getEntitiesNearby(String timerName, Class... entityClasses) {
        StatTimer timer = StatTimer.get("EntityHelper." + timerName);
        timer.start();

        Minecraft mc = Minecraft.getMinecraft();
        AxisAlignedBB bb = getBB(mc.thePlayer);
        List list = new ArrayList();
        for(Class entityClass : entityClasses) {
            list.addAll(mc.theWorld.getEntitiesWithinAABB(entityClass, bb));
        }

        if(list.size()>MAX_ENTITIES)
        {
            Collections.sort(list, new EntityDistanceComparator(mc.thePlayer));
            list = list.subList(0, MAX_ENTITIES);
        }

        timer.stop();
        return list;
    }

	public static List getMobsNearby() {
		return getEntitiesNearby("getMobsNearby", IMob.class, IBossDisplayData.class, IRangedAttackMob.class);
	}
	
	public static List<EntityVillager> getVillagersNearby() {
        return getEntitiesNearby("getVillagersNearby", EntityVillager.class);
	}
	
	public static List<IAnimals> getAnimalsNearby() {
        return getEntitiesNearby("getAnimalsNearby", EntityAnimal.class, EntityGolem.class, EntityWaterMob.class);
	}
	
	/**
	 * Get nearby non-player entities
	 * @return
	 */
	public static List<EntityPlayer> getPlayersNearby() {
        Minecraft mc = Minecraft.getMinecraft();
        if(!mc.isSingleplayer()) {
            List<EntityPlayer> nearbyPlayers = getEntitiesNearby("getPlayersNearby", EntityPlayer.class);
            nearbyPlayers.remove(Minecraft.getMinecraft().thePlayer);
            return nearbyPlayers;
        } else {
            return Collections.EMPTY_LIST;
        }
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
	 * Get the entity's heading in degrees
	 * 
	 * @param entity
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
	 * Put entities into map, preserving the order, using entityId as key
	 * @param list
	 * @return
	 */
	public static Map<Object,Map> buildEntityIdMap(List<LinkedHashMap> list, boolean sort) {
		
		if(list==null || list.isEmpty()) return Collections.emptyMap();
		
		// Sort to keep named entities last.  (Why? display on top of others?)
		if(sort) {
			Collections.sort(list, new EntityHelper.EntityMapComparator());
		}

		LinkedHashMap<Object,Map> idMap = new LinkedHashMap<Object,Map>(list.size());
		for(Map entityMap : list) {
			idMap.put("id"+entityMap.get(EntityKey.entityId), entityMap);
		}
		return idMap;
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

    public static class EntityDistanceComparator implements Comparator<Entity> {

        final EntityPlayer player;

        EntityDistanceComparator(EntityPlayer player)
        {
            this.player = player;
        }

        @Override
        public int compare(Entity o1, Entity o2) {
            return Double.compare(o1.getDistanceSqToEntity(player), o2.getDistanceSqToEntity(player));
        }
    }
	
}