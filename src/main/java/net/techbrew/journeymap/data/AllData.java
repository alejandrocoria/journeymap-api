/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

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

        if (JourneyMap.getWebMapProperties().showWaypoints.get())
        {
            int currentDimension = cache.getPlayer(false).dimension;
            Collection<Waypoint> waypoints = cache.getWaypoints(false);
            Map<String, Waypoint> wpMap = new HashMap<String, Waypoint>();
            for (Waypoint waypoint : waypoints)
            {
                if (waypoint.getDimensions().contains(currentDimension))
                {
                    wpMap.put(waypoint.getId(), waypoint);
                }
            }
            props.put(Key.waypoints, wpMap);
        }
        else
        {
            props.put(Key.waypoints, Collections.emptyMap());
        }

        if (!WorldData.isHardcoreAndMultiplayer())
        {
            if (JourneyMap.getWebMapProperties().showAnimals.get() || JourneyMap.getWebMapProperties().showPets.get())
            {
                props.put(Key.animals, cache.getAnimals(false));
            }
            else
            {
                props.put(Key.animals, Collections.emptyMap());
            }

            if (JourneyMap.getWebMapProperties().showMobs.get())
            {
                props.put(Key.mobs, cache.getMobs(false));
            }
            else
            {
                props.put(Key.mobs, Collections.emptyMap());
            }

            if (JourneyMap.getWebMapProperties().showPlayers.get())
            {
                props.put(Key.players, cache.getPlayers(false));
            }
            else
            {
                props.put(Key.players, Collections.emptyMap());
            }

            if (JourneyMap.getWebMapProperties().showVillagers.get())
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
        return JourneyMap.getCoreProperties().renderFrequency.get() * 2000;
    }

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
}
