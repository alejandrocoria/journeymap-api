/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.model.EntityHelper;

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
        for (EntityDTO entity : list)
        {
            // Exclude animals being ridden, since their positions lag behind the players on the map
            if (entity.entityLiving.riddenByEntity != null)
            {
                finalList.remove(entity);
            }
        }

        return EntityHelper.buildEntityIdMap(finalList, true);
    }

    public long getTTL()
    {
        return Math.max(1000, JourneyMap.getInstance().coreProperties.cacheAnimalsData.get());
    }
}
