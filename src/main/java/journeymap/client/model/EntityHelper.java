/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.collect.ImmutableSortedMap;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFacade;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.*;

public class EntityHelper
{
    public static EntityDistanceComparator entityDistanceComparator = new EntityDistanceComparator();
    public static EntityDTODistanceComparator entityDTODistanceComparator = new EntityDTODistanceComparator();
    public static EntityMapComparator entityMapComparator = new EntityMapComparator();
    private static final String[] HORSE_TEXTURES = new String[]{"textures/entity/horse/horse_white.png", "textures/entity/horse/horse_creamy.png", "textures/entity/horse/horse_chestnut.png", "textures/entity/horse/horse_brown.png", "textures/entity/horse/horse_black.png", "textures/entity/horse/horse_gray.png", "textures/entity/horse/horse_darkbrown.png"};

    public static List<EntityDTO> getEntitiesNearby(String timerName, int maxEntities, boolean hostile, Class... entityClasses)
    {
        StatTimer timer = StatTimer.get("EntityHelper." + timerName);
        timer.start();

        Minecraft mc = FMLClientHandler.instance().getClient();
        List<EntityDTO> list = new ArrayList();

        List<Entity> allEntities = new ArrayList<Entity>(mc.theWorld.loadedEntityList);
        AxisAlignedBB bb = getBB(mc.thePlayer);

        try
        {
            for (Entity entity : allEntities)
            {
                if (entity instanceof EntityLivingBase && !entity.isDead && entity.addedToChunk && bb.intersectsWith(ForgeHelper.INSTANCE.getEntityBoundingBox((EntityLivingBase) entity)))
                {
                    for (Class entityClass : entityClasses)
                    {
                        if (entityClass.isAssignableFrom(entity.getClass()))
                        {
                            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                            EntityDTO dto = DataCache.INSTANCE.getEntityDTO(entityLivingBase);
                            dto.update(entityLivingBase, hostile);
                            list.add(dto);
                            break;
                        }
                    }
                }
            }

            if (list.size() > maxEntities)
            {
                int before = list.size();
                entityDTODistanceComparator.player = mc.thePlayer;
                Collections.sort(list, entityDTODistanceComparator);
                list = list.subList(0, maxEntities);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().warn("Failed to " + timerName + ": " + LogFormatter.toString(t));
        }

        timer.stop();
        return list;
    }

    public static List<EntityDTO> getMobsNearby()
    {
        return getEntitiesNearby("getMobsNearby", Journeymap.getClient().getCoreProperties().maxMobsData.get(), true, IMob.class);
    }

    public static List<EntityDTO> getVillagersNearby()
    {
        return getEntitiesNearby("getVillagersNearby", Journeymap.getClient().getCoreProperties().maxVillagersData.get(), false, EntityVillager.class);
    }

    public static List<EntityDTO> getAnimalsNearby()
    {
        return getEntitiesNearby("getAnimalsNearby", Journeymap.getClient().getCoreProperties().maxAnimalsData.get(), false, EntityAnimal.class, EntityGolem.class, EntityWaterMob.class);
    }

    public static boolean isPassive(EntityLiving entityLiving)
    {
        if (entityLiving == null)
        {
            return false;
        }

        if (entityLiving instanceof IMob)
        {
            return false;
        }

        EntityLivingBase attackTarget = entityLiving.getAttackTarget();
        if (attackTarget != null)
        {
            if (attackTarget instanceof EntityPlayer || attackTarget instanceof IEntityOwnable)
            {
                return false;
            }
        }

        return true;
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
        List<EntityPlayer> allPlayers = new ArrayList<EntityPlayer>(mc.theWorld.playerEntities);
        allPlayers.remove(mc.thePlayer);

        int max = Journeymap.getClient().getCoreProperties().maxPlayersData.get();
        if (allPlayers.size() > max)
        {
            entityDistanceComparator.player = mc.thePlayer;
            Collections.sort(allPlayers, entityDistanceComparator);
            allPlayers = allPlayers.subList(0, max);
        }

        List<EntityDTO> playerDTOs = new ArrayList<EntityDTO>(allPlayers.size());
        for (EntityPlayer player : allPlayers)
        {
            EntityDTO dto = DataCache.INSTANCE.getEntityDTO(player);
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
        int lateralDistance = Journeymap.getClient().getCoreProperties().radarLateralDistance.get();
        int verticalDistance = Journeymap.getClient().getCoreProperties().radarVerticalDistance.get();
        return ForgeHelper.INSTANCE.getBoundingBox(player, lateralDistance, verticalDistance);
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
    public static ResourceLocation getIconTextureLocation(Entity entity)
    {
        try
        {
            Render entityRender = ForgeHelper.INSTANCE.getRenderManager().getEntityRenderObject(entity);

            ResourceLocation original = null;

            // Manually handle horses
            if (entityRender instanceof RenderHorse)
            {
                EntityHorse horse = ((EntityHorse) entity);
                original = new ResourceLocation("minecraft", horse.getVariantTexturePaths()[0]);
            }
            else
            {
                original = RenderFacade.getEntityTexture(entityRender, entity);
            }

            if (original == null)
            {
                JMLogger.logOnce("Can't get entityTexture for " + entity.getClass() + " via " + entityRender.getClass(), null);
                return null;
            }

            if (!(original.getResourcePath().contains("/entity/")))
            {
                //JMLogger.logOnce(original + " doesn't have /entity/ in path, so can't look for /entity_icon/", null);
                return null;
            }

            ResourceLocation entityIconLoc = new ResourceLocation(original.getResourceDomain(), original.getResourcePath().replace("/entity/", "/entity_icon/"));
            return entityIconLoc;
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Can't get entityTexture for " + entity.getName(), t);
            return null;
        }
    }

    private static class EntityMapComparator implements Comparator<EntityDTO>
    {

        @Override
        public int compare(EntityDTO o1, EntityDTO o2)
        {

            Integer o1rank = 0;
            Integer o2rank = 0;

            if (o1.customName != null)
            {
                o1rank++;
            }
            else
            {
                if (o1.username != null)
                {
                    o1rank += 2;
                }
            }

            if (o2.customName != null)
            {
                o2rank++;
            }
            else
            {
                if (o2.username != null)
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
            EntityLivingBase e1 = o1.entityLivingRef.get();
            EntityLivingBase e2 = o2.entityLivingRef.get();
            if (e1 == null || e2 == null)
            {
                return 0;
            }
            return Double.compare(e1.getDistanceSqToEntity(player), e2.getDistanceSqToEntity(player));
        }
    }

}
