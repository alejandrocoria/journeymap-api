package net.techbrew.journeymap.data;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.StringUtils;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.EntityHelper;

import java.util.*;

/**
 * Provides nearby mobs in a Map.
 *
 * @author mwoodman
 */
public class AnimalsData implements IDataProvider
{

    private final boolean includeNonPets;
    private final boolean includePets;

    /**
     * Constructor.
     */
    public AnimalsData()
    {
        includeNonPets = true;
        includePets = true;
    }

    /**
     * Constructor with specific inclusions.
     *
     * @param includeNonPets
     * @param includePets
     */
    public AnimalsData(boolean includeNonPets, boolean includePets)
    {
        super();
        this.includeNonPets = includeNonPets;
        this.includePets = includePets;
    }

    /**
     * Provides all possible keys.
     */
    @Override
    public Enum[] getKeys()
    {
        return EntityKey.values();
    }

    /**
     * Return map of nearby animals data.
     */
    @Override
    public Map getMap(Map optionalParams)
    {

        // TODO: setFrom includeNonPets, includePets?

        if (!FeatureManager.isAllowed(Feature.RadarAnimals))
        {
            return Collections.emptyMap();
        }

        List<IAnimals> animals = EntityHelper.getAnimalsNearby();
        List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(animals.size());
        String owner;
        EntityLiving entity;
        for (IAnimals animal : animals)
        {
            entity = (EntityLiving) animal;

            // Exclude animals being ridden, since their positions lag behind the players on the map
            if (entity.riddenByEntity != null)
            {
                continue;
            }

            LinkedHashMap eProps = new LinkedHashMap();
            eProps.put(EntityKey.entityId, entity.getUniqueID());
            eProps.put(EntityKey.entityLiving, entity);
            eProps.put(EntityKey.filename, EntityHelper.getFileName(entity));
            eProps.put(EntityKey.hostile, false);
            eProps.put(EntityKey.posX, entity.posX);
            eProps.put(EntityKey.posZ, entity.posZ);
            eProps.put(EntityKey.chunkCoordX, entity.chunkCoordX);
            eProps.put(EntityKey.chunkCoordZ, entity.chunkCoordZ);
            eProps.put(EntityKey.heading, EntityHelper.getHeading(entity));
            if (entity instanceof EntityTameable)
            {
                owner = ((EntityTameable) entity).getOwnerName();
                if (owner != null)
                {
                    eProps.put(EntityKey.owner, owner);
                }
            }
            else
            {
                if (entity instanceof EntityHorse)
                {
                    owner = entity.getDataWatcher().getWatchableObjectString(21);
                    eProps.put(EntityKey.owner, owner);
                }
            }

            // CustomName
            if (entity.hasCustomNameTag())
            {
                eProps.put(EntityKey.customName, StringUtils.stripControlCodes(entity.getCustomNameTag()));
            }

            list.add(eProps);
        }

        LinkedHashMap props = new LinkedHashMap();
        props.put(EntityKey.root, EntityHelper.buildEntityIdMap(list, true));

        return props;
    }


    /**
     * Return length of time in millis data should be kept.
     */
    @Override
    public long getTTL()
    {
        return JourneyMap.getInstance().coreProperties.cacheAnimalsData.get();
    }

    /**
     * Return false by default. Let cache expired based on TTL.
     */
    @Override
    public boolean dataExpired()
    {
        return false;
    }
}
