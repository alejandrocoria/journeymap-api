/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * JSON-safe attributes derived from an EntityLivingBase.
 */
public class EntityDTO implements Serializable
{
    public final String entityId;
    public transient WeakReference<EntityLivingBase> entityLivingRef;
    public transient ResourceLocation entityIconLocation;
    public String iconLocation;
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
    public boolean invisible;
    public boolean sneaking;
    public boolean passiveAnimal;
    public int color;

    private EntityDTO(EntityLivingBase entity)
    {
        this.entityLivingRef = new WeakReference<EntityLivingBase>(entity);
        this.entityId = entity.getUniqueID().toString();
    }

    public void update(EntityLivingBase entity, boolean hostile)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer currentPlayer = FMLClientHandler.instance().getClient().thePlayer;
        this.dimension = entity.dimension;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
        this.chunkCoordX = entity.chunkCoordX;
        this.chunkCoordY = entity.chunkCoordY;
        this.chunkCoordZ = entity.chunkCoordZ;
        this.heading = Math.round(entity.rotationYawHead % 360);
        if (currentPlayer != null)
        {
            this.invisible = entity.isInvisibleToPlayer(currentPlayer);
        }
        else
        {
            this.invisible = false;
        }
        this.sneaking = entity.isSneaking();

        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        ResourceLocation entityIcon = null;
        int playerColor = coreProperties.getColor(coreProperties.colorPlayer);

        // Player check
        if (entity instanceof EntityPlayer)
        {
            String name = StringUtils.stripControlCodes(entity.getName());
            this.username = name;
            try
            {
                ScorePlayerTeam team = mc.theWorld.getScoreboard().getPlayersTeam(this.username);
                if (team != null)
                {
                    playerColor = team.getChatFormat().getColorIndex();
                }
                else if (currentPlayer.equals(entity))
                {
                    playerColor = coreProperties.getColor(coreProperties.colorSelf);
                }
                else
                {
                    playerColor = coreProperties.getColor(coreProperties.colorPlayer);
                }
            }
            catch (Throwable t)
            {
            }

            entityIcon = DefaultPlayerSkin.getDefaultSkinLegacy();
            try
            {

                NetHandlerPlayClient client = Minecraft.getMinecraft().getConnection();
                NetworkPlayerInfo info = client.getPlayerInfo(entity.getUniqueID());
                if (info != null)
                {
                    entityIcon = info.getLocationSkin();
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Error looking up player skin: " + LogFormatter.toPartialString(t));
            }
        }
        else
        {
            this.username = null;
            entityIcon = EntityHelper.getIconTextureLocation(entity);
        }

        if (entityIcon != null)
        {
            this.entityIconLocation = entityIcon;
            this.iconLocation = entityIcon.toString();
        }

        // Owner
        String owner = null;
        if (entity instanceof EntityTameable)
        {
            Entity ownerEntity = ((EntityTameable) entity).getOwner();
            if (ownerEntity != null)
            {
                owner = ownerEntity.getName();
            }
        }
        else if (entity instanceof IEntityOwnable)
        {
            Entity ownerEntity = ((IEntityOwnable) entity).getOwner();
            if (ownerEntity != null)
            {
                owner = ownerEntity.getName();
            }
        }
        else if (entity instanceof EntityHorse)
        {
            // TODO: Test this with and without owners
            // 1.9
            UUID ownerUuid = ((EntityHorse) entity).getOwnerUniqueId();
            if (currentPlayer != null && ownerUuid != null)
            {
                try
                {
                    String playerUuid = currentPlayer.getUniqueID().toString();
                    if (playerUuid.equals(ownerUuid))
                    {
                        owner = currentPlayer.getName();
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

        // TODO: Recompare to branch to ensure it matches bugfixes
        boolean passive = false;
        if (entity instanceof EntityLiving)
        {
            EntityLiving entityLiving = (EntityLiving) entity;

            // CustomName
            if (entity.hasCustomName() && entityLiving.getAlwaysRenderNameTag())
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

            if (EntityHelper.isPassive((EntityLiving) entity))
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

        // Color
        if (entity instanceof EntityPlayer)
        {
            // Player
            this.color = playerColor;
        }
        else if (!Strings.isNullOrEmpty(owner))
        {
            // Pet
            color = coreProperties.getColor(coreProperties.colorPet);
        }
        else if (profession != null)
        {
            // Villager
            color = coreProperties.getColor(coreProperties.colorVillager);
        }
        else if (hostile)
        {
            // Mob
            color = coreProperties.getColor(coreProperties.colorHostile);
        }
        else
        {
            // Passive
            color = coreProperties.getColor(coreProperties.colorPassive);
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
