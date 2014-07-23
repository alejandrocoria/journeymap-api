/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityOwnable;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * JSON-safe attributes derived from an EntityLivingBase.
 */
public class EntityDTO implements Serializable
{
    public final String entityId;
    public transient EntityLivingBase entityLiving;
    public String filename;
    public Boolean hostile;
    public double posX;
    public double posY;
    public double posZ;
    public int chunkCoordX;
    public int chunkCoordY;
    public int chunkCoordZ;
    public double heading;
    public String customName;
    public String owner;
    public Integer profession;
    public String username;
    public String biome;
    public int dimension;
    public Boolean underground;
<<<<<<< HEAD
    public boolean invisible;
    public boolean sneaking;

    private EntityDTO(EntityLivingBase entity)
    {
        this.entityId = entity.getUniqueID().toString();
        this.entityLiving = entity;
    }

    public void update(EntityLivingBase entity, boolean hostile)
=======
    public final boolean invisible;
    public final boolean sneaking;
    public final boolean passiveAnimal;
    
    public EntityDTO(EntityLivingBase entity, boolean hostile)
>>>>>>> c8373fc... Bugfixed passive/pet issues for the webmap
    {
        EntityPlayer currentPlayer = FMLClientHandler.instance().getClient().thePlayer;

        this.dimension = entity.dimension;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
        this.chunkCoordX = entity.chunkCoordX;
        this.chunkCoordY = entity.chunkCoordY;
        this.chunkCoordZ = entity.chunkCoordZ;
        this.heading = EntityHelper.getHeading(entity);
        if (currentPlayer != null)
        {
            this.invisible = entity.isInvisibleToPlayer(currentPlayer);
        }
        else
        {
            this.invisible = false;
        }
        this.sneaking = entity.isSneaking();

        // Player check
        if (entity instanceof EntityPlayer)
        {
            this.filename = "/skin/" + ((EntityPlayer) entity).getDisplayName();
            this.username = ((EntityPlayer) entity).getDisplayName();
        }
        else
        {
            this.filename = EntityHelper.getFileName(entity);
            this.username = null;
        }

        // Owner
        String owner = null;
        if (entity instanceof EntityTameable)
        {
            owner = ((EntityTameable) entity).getOwnerName();
        }
        else if(entity instanceof EntityOwnable)
        {
            owner = ((EntityOwnable) entity).getOwnerName();
        }
        else if (entity instanceof EntityHorse)
        {
            // TODO: Test this with and without owners
            String ownerUuidString = ((EntityHorse) entity).func_152119_ch();
            if (ownerUuidString != null)
            {
                try
                {
                    if (currentPlayer.getUniqueID().equals(UUID.fromString(ownerUuidString)))
                    {
                        owner = currentPlayer.getCommandSenderName();
                    }
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }

        this.owner = owner;

        String customName = null;
<<<<<<< HEAD
        if (entity instanceof EntityLiving)
=======
        boolean passive = false;
        if(entity instanceof EntityLiving)
>>>>>>> c8373fc... Bugfixed passive/pet issues for the webmap
        {
            // CustomName
            if (((EntityLiving) entity).hasCustomNameTag())
            {
                customName = StringUtils.stripControlCodes(((EntityLiving) entity).getCustomNameTag());
            }

            // Hostile check
            if (!hostile && currentPlayer != null)
            {
                EntityLivingBase attackTarget = ((EntityLiving) entity).getAttackTarget();
                if (attackTarget != null && attackTarget.getUniqueID().equals(currentPlayer.getUniqueID()))
                {
                    hostile = true;
                }
            }

            // Passive check
            if(EntityHelper.isPassiveAnimal((EntityLiving)entity))
            {
                passive = true;
            }
        }
        this.customName = customName;
        this.hostile = hostile;
        this.passiveAnimal = passive;

        // Profession
        if (entity instanceof EntityVillager)
        {
            this.profession = ((EntityVillager) entity).getProfession();
        }
        else
        {
            this.profession = null;
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<EntityLivingBase, EntityDTO>
    {
        @Override
        public EntityDTO load(EntityLivingBase entity) throws Exception
        {
            return new EntityDTO(entity);
        }
    }
}
