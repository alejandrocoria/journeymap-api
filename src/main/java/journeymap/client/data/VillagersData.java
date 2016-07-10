/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.EntityHelper;
import journeymap.common.Journeymap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides nearby mobs in a Map.
 *
 * @author mwoodman
 */
public class VillagersData extends CacheLoader<Class, Map<String, EntityDTO>>
{
    @Override
    public Map<String, EntityDTO> load(Class aClass) throws Exception
    {
        if (!FeatureManager.isAllowed(Feature.RadarVillagers))
        {
            return new HashMap<String, EntityDTO>();
        }

        List<EntityDTO> list = EntityHelper.getVillagersNearby();
        return EntityHelper.buildEntityIdMap(list, true);
    }

    public long getTTL()
    {
        return Journeymap.getClient().getCoreProperties().cacheVillagersData.get();
    }
}
