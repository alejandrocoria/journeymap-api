package net.techbrew.journeymap.model;

import com.google.common.collect.ImmutableSortedMap;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFacade;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;

import java.util.*;

public class EntityHelper
{
    private static int lateralDistance = JourneyMap.getInstance().coreProperties.chunkOffset.get() * 8;
    private static int verticalDistance = lateralDistance / 2;

    public static EntityDistanceComparator entityDistanceComparator = new EntityDistanceComparator();
    public static EntityDTODistanceComparator entityDTODistanceComparator = new EntityDTODistanceComparator();
    public static EntityMapComparator entityMapComparator = new EntityMapComparator();

    public static List<EntityDTO> getEntitiesNearby(String timerName, int maxEntities, boolean hostile, Class... entityClasses)
    {
        StatTimer timer = StatTimer.get("EntityHelper." + timerName);
        timer.start();

        Minecraft mc = FMLClientHandler.instance().getClient();
        List<EntityDTO> list = new ArrayList();

        List<Entity> allEntities = new ArrayList<Entity>(mc.theWorld.loadedEntityList);
        AxisAlignedBB bb = getBB(mc.thePlayer);

        lateralDistance = JourneyMap.getInstance().coreProperties.chunkOffset.get() * 8;
        verticalDistance = lateralDistance / 2;

        try
        {
            for(Entity entity : allEntities)
            {
                if(entity instanceof EntityLivingBase && !entity.isDead && entity.addedToChunk && bb.intersectsWith(entity.boundingBox))
                {
                    for (Class entityClass : entityClasses)
                    {
                        if(entityClass.isAssignableFrom(entity.getClass()))
                        {
                            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                            EntityDTO dto = DataCache.instance().getEntityDTO(entityLivingBase);
                            dto.update(entityLivingBase, hostile);
                            list.add(dto);
                            break;
                        }
                    }
                }
            }

            if(list.size()>maxEntities)
            {
                int before = list.size();
                entityDTODistanceComparator.player = mc.thePlayer;
                Collections.sort(list, entityDTODistanceComparator);
                list = list.subList(0, maxEntities);
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().warning("Failed to " + timerName + ": " + LogFormatter.toString(t));
        }

        timer.stop();
        return list;
    }

    public static List<EntityDTO> getMobsNearby()
    {
        return getEntitiesNearby("getMobsNearby", JourneyMap.getInstance().coreProperties.maxMobsData.get(), true, IMob.class);
    }

    public static List<EntityDTO> getVillagersNearby()
    {
        return getEntitiesNearby("getVillagersNearby", JourneyMap.getInstance().coreProperties.maxVillagersData.get(), false, EntityVillager.class);
    }

    public static List<EntityDTO> getAnimalsNearby()
    {
        return getEntitiesNearby("getAnimalsNearby", JourneyMap.getInstance().coreProperties.maxAnimalsData.get(), false, EntityAnimal.class, EntityGolem.class, EntityWaterMob.class);
    }

    /**
     * Get nearby non-player entities
     *
     * @return
     */
    public static List<EntityDTO> getPlayersNearby()
    {
        StatTimer timer = StatTimer.get("EntityHelper.getPlayersNearby");
        timer.start();

        Minecraft mc = FMLClientHandler.instance().getClient();

        lateralDistance = JourneyMap.getInstance().coreProperties.chunkOffset.get() * 8;
        verticalDistance = lateralDistance / 2;

        List<EntityPlayer> allPlayers = new ArrayList<EntityPlayer>(mc.theWorld.playerEntities);
        allPlayers.remove(mc.thePlayer);

        int max = JourneyMap.getInstance().coreProperties.maxPlayersData.get();
        if(allPlayers.size()>max)
        {
            entityDistanceComparator.player = mc.thePlayer;
            Collections.sort(allPlayers, entityDistanceComparator);
            allPlayers = allPlayers.subList(0, max);
        }

        List<EntityDTO> playerDTOs = new ArrayList<EntityDTO>(allPlayers.size());
        for(EntityPlayer player : allPlayers)
        {
            EntityDTO dto = DataCache.instance().getEntityDTO(player);
            dto.update(player, false);
            playerDTOs.add(dto);
        }

        timer.stop();
        return playerDTOs;
    }

    /**
     * Get a boundingbox to search nearby player.
     *
     * @param player
     * @return
     */
    private static AxisAlignedBB getBB(EntityPlayerSP player)
    {
        return AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);
    }

    /**
     * Get the entity's heading in degrees
     *
     * @param entity
     * @return
     */
    public static double getHeading(Entity entity)
    {
        if (entity instanceof EntityLiving)
        {
            return getHeading(((EntityLiving) entity).rotationYawHead);
        }
        else
        {
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
    public static double getHeading(float rotationYaw)
    {
        double degrees = Math.round(rotationYaw % 360);
        return degrees;
    }

    /**
     * Put entities into map, preserving the order, using entityId as key
     *
     * @param list
     * @return
     */
    public static Map<String, EntityDTO> buildEntityIdMap(List<? extends EntityDTO> list, boolean sort)
    {

        if (list == null || list.isEmpty())
        {
            return Collections.emptyMap();
        }

        // Sort to keep named entities last.  (Why? display on top of others?)
        if (sort)
        {
            Collections.sort(list, new EntityHelper.EntityMapComparator());
        }

        LinkedHashMap<String, EntityDTO> idMap = new LinkedHashMap<String, EntityDTO>(list.size());
        for (EntityDTO entityMap : list)
        {
            idMap.put("id" + entityMap.entityId, entityMap);
        }
        return ImmutableSortedMap.copyOf(idMap);
    }


    /**
     * Get the simple name of the entity (without Entity prefix)
     *
     * @param entity
     * @return
     */
    public static String getFileName(Entity entity)
    {

        Render entityRender = RenderManager.instance.getEntityRenderObject(entity);

        // Manually handle horses
        if (entityRender instanceof RenderHorse)
        {
            switch (((EntityHorse) entity).getHorseType())
            {
                case 1:
                    return "horse/donkey.png";

                case 2:
                    return "horse/mule.png";

                case 3:
                    return "horse/zombiehorse.png";

                case 4:
                    return "horse/skeletonhorse.png";
                case 0:
                default:
                    return "horse/horse.png";
            }
        }

        // Non-horse mobs
        ResourceLocation loc = RenderFacade.getEntityTexture(entityRender, entity);
        if (loc.getResourceDomain().equals("minecraft"))
        {
            String tex = loc.getResourcePath();
            String search = "/entity/";
            int i = tex.lastIndexOf(search);
            if (i >= 0)
            {
                tex = tex.substring(i + search.length());
            }
            return tex;
        }
        else
        {
            return loc.getResourceDomain() + "/" + loc.getResourcePath();
        }
    }

    private static class EntityMapComparator implements Comparator<EntityDTO>
    {

        @Override
        public int compare(EntityDTO o1, EntityDTO o2)
        {

            Integer o1rank = 0;
            Integer o2rank = 0;

            if (o1.customName!=null)
            {
                o1rank++;
            }
            else
            {
                if (o1.username!=null)
                {
                    o1rank += 2;
                }
            }

            if (o2.customName!=null)
            {
                o2rank++;
            }
            else
            {
                if (o2.username!=null)
                {
                    o2rank += 2;
                }
            }

            return o1rank.compareTo(o2rank);
        }

    }

    private static class EntityDistanceComparator implements Comparator<Entity>
    {

        EntityPlayer player;

        @Override
        public int compare(Entity o1, Entity o2)
        {
            return Double.compare(o1.getDistanceSqToEntity(player), o2.getDistanceSqToEntity(player));
        }
    }

    private static class EntityDTODistanceComparator implements Comparator<EntityDTO>
    {
        EntityPlayer player;

        @Override
        public int compare(EntityDTO o1, EntityDTO o2)
        {
            return Double.compare(o1.entityLiving.getDistanceSqToEntity(player), o2.entityLiving.getDistanceSqToEntity(player));
        }
    }

}
