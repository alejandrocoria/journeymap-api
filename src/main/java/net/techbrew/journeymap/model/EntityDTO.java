package net.techbrew.journeymap.model;

import com.google.common.cache.CacheLoader;
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
public class EntityDTO implements Serializable
{
    public transient EntityLivingBase entityLiving;
    public final String entityId;
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
    public boolean invisible;
    public boolean sneaking;
    
    private EntityDTO(EntityLivingBase entity)
    {
        this.entityId= entity.getUniqueID().toString();
        this.entityLiving = entity;
    }
    
    public void update(EntityLivingBase entity, boolean hostile)
    {
        EntityPlayer currentPlayer = FMLClientHandler.instance().getClient().thePlayer;

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

    public static class SimpleCacheLoader extends CacheLoader<EntityLivingBase, EntityDTO>
    {
        @Override
        public EntityDTO load(EntityLivingBase entity) throws Exception
        {
            return new EntityDTO(entity);
        }
    }
}
