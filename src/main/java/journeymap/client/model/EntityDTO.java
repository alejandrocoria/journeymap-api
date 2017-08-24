/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
import net.minecraft.entity.*;
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
    /**
     * The Entity id.
     */
    public final String entityId;
    /**
     * The Entity living ref.
     */
    public transient WeakReference<EntityLivingBase> entityLivingRef;
    /**
     * The Entity icon location.
     */
    public transient ResourceLocation entityIconLocation;
    /**
     * The Icon location.
     */
    public String iconLocation;
    /**
     * The Hostile.
     */
    public Boolean hostile;
    /**
     * The Pos x.
     */
    public double posX;
    /**
     * The Pos y.
     */
    public double posY;
    /**
     * The Pos z.
     */
    public double posZ;
    /**
     * The Chunk coord x.
     */
    public int chunkCoordX;
    /**
     * The Chunk coord y.
     */
    public int chunkCoordY;
    /**
     * The Chunk coord z.
     */
    public int chunkCoordZ;
    /**
     * The Heading.
     */
    public double heading;
    /**
     * The Custom name.
     */
    public String customName;
    /**
     * The Owner.
     */
    public String owner;
    /**
     * Villager Profession.
     */
    public String profession;
    /**
     * The Username.
     */
    public String username;
    /**
     * The Biome.
     */
    public String biome;
    /**
     * The Dimension.
     */
    public int dimension;
    /**
     * The Underground.
     */
    public Boolean underground;
    /**
     * The Invisible.
     */
    public boolean invisible;
    /**
     * The Sneaking.
     */
    public boolean sneaking;
    /**
     * The Passive animal.
     */
    public boolean passiveAnimal;

    /**
     * Whether the entity is an INpc
     */
    public boolean npc;
    /**
     * The Color.
     */
    public int color;

    private EntityDTO(EntityLivingBase entity)
    {
        this.entityLivingRef = new WeakReference<EntityLivingBase>(entity);
        this.entityId = entity.getUniqueID().toString();
    }

    /**
     * Update.
     *
     * @param entity  the entity
     * @param hostile the hostile
     */
    public void update(EntityLivingBase entity, boolean hostile) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer currentPlayer = FMLClientHandler.instance().getClient().player;
        this.dimension = entity.dimension;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
        this.chunkCoordX = entity.chunkCoordX;
        this.chunkCoordY = entity.chunkCoordY;
        this.chunkCoordZ = entity.chunkCoordZ;
        this.heading = Math.round(entity.rotationYawHead % 360);
        if (currentPlayer != null) {
            this.invisible = entity.isInvisibleToPlayer(currentPlayer);
        } else {
            this.invisible = false;
        }
        this.sneaking = entity.isSneaking();

        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        ResourceLocation entityIcon = null;
        int playerColor = coreProperties.getColor(coreProperties.colorPlayer);

        ScorePlayerTeam team = null;
        try
        {
            team = mc.world.getScoreboard().getPlayersTeam(entity.getCachedUniqueIdString());
        }
        catch (Throwable t)
        {
        }

        // Player check
        if (entity instanceof EntityPlayer) {
            String name = StringUtils.stripControlCodes(entity.getName());
            this.username = name;
            try {
                if (team != null) {
                    playerColor = team.getColor().getColorIndex();
                } else if (currentPlayer.equals(entity)) {
                    playerColor = coreProperties.getColor(coreProperties.colorSelf);
                } else {
                    playerColor = coreProperties.getColor(coreProperties.colorPlayer);
                }
            } catch (Throwable t) {
            }

            entityIcon = DefaultPlayerSkin.getDefaultSkinLegacy();
            try {

                NetHandlerPlayClient client = Minecraft.getMinecraft().getConnection();
                NetworkPlayerInfo info = client.getPlayerInfo(entity.getUniqueID());
                if (info != null) {
                    entityIcon = info.getLocationSkin();
                }
            } catch (Throwable t) {
                Journeymap.getLogger().error("Error looking up player skin: " + LogFormatter.toPartialString(t));
            }
        } else {
            this.username = null;
            entityIcon = EntityHelper.getIconTextureLocation(entity);
        }

        if (entityIcon != null) {
            this.entityIconLocation = entityIcon;
            this.iconLocation = entityIcon.toString();
        }

        // Owner
        String owner = null;
        if (entity instanceof EntityTameable) {
            Entity ownerEntity = ((EntityTameable) entity).getOwner();
            if (ownerEntity != null) {
                owner = ownerEntity.getName();
            }
        } else if (entity instanceof IEntityOwnable) {
            Entity ownerEntity = ((IEntityOwnable) entity).getOwner();
            if (ownerEntity != null) {
                owner = ownerEntity.getName();
            }
        } else if (entity instanceof EntityHorse) {
            // TODO: Test this with and without owners
            // 1.9
            UUID ownerUuid = ((EntityHorse) entity).getOwnerUniqueId();
            if (currentPlayer != null && ownerUuid != null) {
                try {
                    String playerUuid = currentPlayer.getUniqueID().toString();
                    if (playerUuid.equals(ownerUuid)) {
                        owner = currentPlayer.getName();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        this.owner = owner;
        String customName = null;

        // TODO: Recompare to branch to ensure it matches bugfixes
        boolean passive = false;
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            // CustomName
            if (entity.hasCustomName() && entityLiving.getAlwaysRenderNameTag()) {
                customName = StringUtils.stripControlCodes(((EntityLiving) entity).getCustomNameTag());
            }

            // Hostile check
            if (!hostile && currentPlayer != null) {
                EntityLivingBase attackTarget = ((EntityLiving) entity).getAttackTarget();
                if (attackTarget != null && attackTarget.getUniqueID().equals(currentPlayer.getUniqueID())) {
                    hostile = true;
                }
            }

            // Passive check

            if (EntityHelper.isPassive((EntityLiving) entity)) {
                passive = true;
            }
        }

        // Profession and NPC
        if (entity instanceof EntityVillager) {
            EntityVillager villager = ((EntityVillager) entity);
            this.profession = villager.getProfessionForge().getCareer(villager.careerId).getName();
        } else if (entity instanceof INpc) {
            this.npc = true;
            this.profession = null;
            this.passiveAnimal = false;
        } else {
            this.profession = null;
            this.passiveAnimal = passive;
        }

        this.customName = customName;
        this.hostile = hostile;

        // Color
        if (entity instanceof EntityPlayer) {
            color = playerColor;
        }
        else if (team != null)
        {
            color = team.getColor().getColorIndex();
        } else if (!Strings.isNullOrEmpty(owner)) {
            // Pet
            color = coreProperties.getColor(coreProperties.colorPet);
        } else if (profession != null || npc) {
            // Villager
            color = coreProperties.getColor(coreProperties.colorVillager);
        } else if (hostile) {
            // Mob
            color = coreProperties.getColor(coreProperties.colorHostile);
        } else {
            // Passive
            color = coreProperties.getColor(coreProperties.colorPassive);
        }
    }

    /**
     * The type Simple cache loader.
     */
    public static class SimpleCacheLoader extends CacheLoader<EntityLivingBase, EntityDTO> {
        @Override
        public EntityDTO load(EntityLivingBase entity) throws Exception {
            return new EntityDTO(entity);
        }
    }
}
