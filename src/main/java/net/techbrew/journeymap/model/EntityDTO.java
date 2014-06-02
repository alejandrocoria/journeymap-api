package net.techbrew.journeymap.model;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;

import java.io.Serializable;

/**
 * JSON-safe attributes derived from an EntityLivingBase.
 */
public final class EntityDTO implements Serializable
{
    public final transient EntityLivingBase entityLiving;
    public final String entityId;
    public final String filename;
    public final Boolean hostile;
    public final double posX;
    public final double posY;
    public final double posZ;
    public final int chunkCoordX;
    public final int chunkCoordY;
    public final int chunkCoordZ;
    public final double heading;
    public final String customName;
    public final String owner;
    public final Integer profession;
    public final String username;
    public String biome;
    public final int dimension;
    public Boolean underground;
    public final boolean invisible;
    public final boolean sneaking;
    
    public EntityDTO(EntityLivingBase entity, boolean hostile)
    {
        EntityPlayer currentPlayer = FMLClientHandler.instance().getClient().thePlayer;

        this.entityId= entity.getUniqueID().toString();
        this.entityLiving = entity;
        this.dimension = entity.dimension;
        this.posX= entity.posX;
        this.posY= entity.posY;
        this.posZ= entity.posZ;
        this.chunkCoordX= entity.chunkCoordX;
        this.chunkCoordY= entity.chunkCoordY;
        this.chunkCoordZ= entity.chunkCoordZ;
        this.heading = EntityHelper.getHeading(entity);
        if(currentPlayer!=null)
        {
            this.invisible = entity.isInvisibleToPlayer(currentPlayer);
        }
        else
        {
            this.invisible = false;
        }
        this.sneaking = entity.isSneaking();
        
        // Player check
        if(entity instanceof EntityPlayer)
        {
            this.filename = "/skin/" + ((EntityPlayer)entity).getDisplayName();
            this.username = ((EntityPlayer)entity).getDisplayName();
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
        else if (entity instanceof EntityHorse)
        {
            owner = ((EntityHorse) entity).getOwnerName();
        }
        this.owner = owner;

        String customName = null;
        if(entity instanceof EntityLiving)
        {
            // CustomName
            if (((EntityLiving)entity).hasCustomNameTag())
            {
                customName = StringUtils.stripControlCodes(((EntityLiving)entity).getCustomNameTag());
            }

            // Hostile check
            if(!hostile && currentPlayer!=null)
            {
                EntityLivingBase attackTarget = ((EntityLiving) entity).getAttackTarget();
                if(attackTarget!=null && attackTarget.getUniqueID().equals(currentPlayer.getUniqueID()))
                {
                    hostile = true;
                }
            }
        }
        this.customName = customName;
        this.hostile = hostile;

        // Profession
        if(entity instanceof EntityVillager)
        {
            this.profession = ((EntityVillager) entity).getProfession();
        }
        else
        {
            this.profession = null;
        }
    }
}
