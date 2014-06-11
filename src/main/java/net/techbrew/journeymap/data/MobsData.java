package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.model.EntityHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides nearby mobs in a Map.
 *
 * @author mwoodman
 */
public class MobsData extends CacheLoader<Class, Map<String,EntityDTO>>
{
    @Override
    public Map<String, EntityDTO> load(Class aClass) throws Exception
    {
        if (!FeatureManager.instance().isAllowed(Feature.RadarMobs))
        {
            return new HashMap<String,EntityDTO>();
        }

        List<EntityDTO> list = EntityHelper.getMobsNearby();
        return EntityHelper.buildEntityIdMap(list, true);
    }

    /**
     * Return length of time in millis data should be kept.
     */
    public long getTTL()
    {
        return JourneyMap.getInstance().coreProperties.cacheMobsData.get();
    }
}
