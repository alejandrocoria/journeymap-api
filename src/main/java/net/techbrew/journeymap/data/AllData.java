package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.Waypoint;

import java.util.*;

/**
 * Provides game-related properties in a Map.
 *
 * @author mwoodman
 */
public class AllData extends CacheLoader<Long, Map>
{
    public static enum Key
    {
        animals,
        images,
        mobs,
        player,
        players,
        villagers,
        waypoints,
        world
    }

    /**
     * Constructor.
     */
    public AllData()
    {
    }

    @Override
    public Map load(Long since) throws Exception
    {
        DataCache cache = DataCache.instance();
        LinkedHashMap<Key, Object> props = new LinkedHashMap<Key, Object>();

        props.put(Key.world, cache.getWorld(false));
        props.put(Key.player, cache.getPlayer(false));
        props.put(Key.images, new ImagesData(since));

        if (JourneyMap.getInstance().webMapProperties.showWaypoints.get())
        {
            Collection<Waypoint> waypoints = cache.getWaypoints(false);
            Map<String,Waypoint> wpMap = new HashMap<String, Waypoint>();
            for(Waypoint waypoint : waypoints)
            {
                wpMap.put(waypoint.getId(), waypoint);
            }
            props.put(Key.waypoints, wpMap);
        }
        else
        {
            props.put(Key.waypoints, Collections.emptyMap());
        }

        if (!WorldData.isHardcoreAndMultiplayer())
        {
            if (JourneyMap.getInstance().webMapProperties.showAnimals.get())
            {
                props.put(Key.animals, cache.getAnimals(false));
            }
            else
            {
                props.put(Key.animals, Collections.emptyMap());
            }

            if (JourneyMap.getInstance().webMapProperties.showMobs.get())
            {
                props.put(Key.mobs, cache.getMobs(false));
            }
            else
            {
                props.put(Key.mobs, Collections.emptyMap());
            }

            if (JourneyMap.getInstance().webMapProperties.showPlayers.get())
            {
                props.put(Key.players, cache.getPlayers(false));
            }
            else
            {
                props.put(Key.players, Collections.emptyMap());
            }

            if (JourneyMap.getInstance().webMapProperties.showVillagers.get())
            {
                props.put(Key.villagers, cache.getVillagers(false));
            }
            else
            {
                props.put(Key.villagers, Collections.emptyMap());
            }
        }

        return ImmutableMap.copyOf(props);
    }

    /**
     * Return length of time in millis data should be kept.
     */
    public long getTTL()
    {
        return JourneyMap.getInstance().coreProperties.chunkPoll.get();
    }
}
