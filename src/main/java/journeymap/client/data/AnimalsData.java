/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.JourneymapClient;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.EntityHelper;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CacheLoader for passive mob entities.
 *
 * @author mwoodman
 */
public class AnimalsData extends CacheLoader<Class, Map<String, EntityDTO>>
{
    @Override
    public Map<String, EntityDTO> load(Class aClass) throws Exception
    {
        if (!FeatureManager.isAllowed(Feature.RadarAnimals))
        {
            return new HashMap<String, EntityDTO>();
        }

        List<EntityDTO> list = EntityHelper.getAnimalsNearby();
        List<EntityDTO> finalList = new ArrayList<EntityDTO>(list);
        for (EntityDTO entityDTO : list)
        {
            EntityLivingBase entityLiving = entityDTO.entityLivingRef.get();
            if (entityLiving == null)
            {
                finalList.remove(entityDTO);
                continue;
            }

            // Exclude animals being ridden, since their positions lag behind the players on the map
            if (entityLiving.isBeingRidden())
            {
                finalList.remove(entityDTO);
            }
        }

        return EntityHelper.buildEntityIdMap(finalList, true);
    }

    public long getTTL()
    {
        return Math.max(1000, JourneymapClient.getCoreProperties().cacheAnimalsData.get());
    }
}
