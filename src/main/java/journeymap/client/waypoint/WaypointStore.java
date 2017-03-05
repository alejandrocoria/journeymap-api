/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.waypoint;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.io.FileHandler;
import journeymap.client.model.Waypoint;
import journeymap.client.model.WaypointGroup;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Enum singleton. Manages disk-backed caches for Waypoints and WaypointGroups
 */
@ParametersAreNonnullByDefault
public enum WaypointStore
{
    /**
     * Instance waypoint store.
     */
    INSTANCE;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Cache<String, Waypoint> cache = CacheBuilder.newBuilder().build();
    private final Cache<Long, Waypoint> groupCache = CacheBuilder.newBuilder().build();
    private final Set<Integer> dimensions = new HashSet<Integer>();
    private boolean loaded = false;

    private boolean writeToFile(Waypoint waypoint)
    {
        if (waypoint.isPersistent())
        {
            File waypointFile = null;
            try
            {
                // Write to file
                waypointFile = new File(FileHandler.getWaypointDir(), waypoint.getFileName());
                Files.write(gson.toJson(waypoint), waypointFile, Charset.forName("UTF-8"));
                return true;
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(String.format("Can't save waypoint file %s: %s", waypointFile, LogFormatter.toString(e)));
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public Collection<Waypoint> getAll()
    {
        return cache.asMap().values();
    }

    /**
     * Gets all.
     *
     * @param group the group
     * @return the all
     */
    public Collection<Waypoint> getAll(final WaypointGroup group)
    {
        return Maps.filterEntries(cache.asMap(), new Predicate<Map.Entry<String, Waypoint>>()
        {
            @Override
            public boolean apply(@Nullable Map.Entry<String, Waypoint> input)
            {
                return input != null && Objects.equals(group, input.getValue().getGroup());
            }
        }).values();
    }

    /**
     * Add.
     *
     * @param waypoint the waypoint
     */
    public void add(Waypoint waypoint)
    {
        if (cache.getIfPresent(waypoint.getId()) == null)
        {
            cache.put(waypoint.getId(), waypoint);
        }
    }

    /**
     * Save.
     *
     * @param waypoint the waypoint
     */
    public void save(Waypoint waypoint)
    {
        cache.put(waypoint.getId(), waypoint);
        boolean saved = writeToFile(waypoint);
        if (saved)
        {
            waypoint.setDirty(false);
        }
    }

    /**
     * Bulk save.
     */
    public void bulkSave()
    {
        for (Waypoint waypoint : cache.asMap().values())
        {
            if (waypoint.isDirty())
            {
                boolean saved = writeToFile(waypoint);
                if (saved)
                {
                    waypoint.setDirty(false);
                }
            }
        }
    }

    /**
     * Remove.
     *
     * @param waypoint the waypoint
     */
    public void remove(Waypoint waypoint)
    {
        cache.invalidate(waypoint.getId());
        File waypointFile = null;
        waypointFile = new File(FileHandler.getWaypointDir(), waypoint.getFileName());
        remove(waypointFile);
    }

    private void remove(File waypointFile)
    {
        try
        {
            waypointFile.delete();
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn(String.format("Can't delete waypoint file %s: %s", waypointFile, e.getMessage()));
            waypointFile.deleteOnExit();
        }
    }

    /**
     * Reset.
     */
    public void reset()
    {
        cache.invalidateAll();
        dimensions.clear();
        loaded = false;
        if (Journeymap.getClient().getWaypointProperties().managerEnabled.get())
        {
            load();
        }
    }

    private void load()
    {
        synchronized (cache)
        {
            ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
            File waypointDir = null;
            try
            {
                cache.invalidateAll();

                waypointDir = FileHandler.getWaypointDir();
                waypoints.addAll(new JmReader().loadWaypoints(waypointDir));

                load(waypoints, false);

                Journeymap.getLogger().info(String.format("Loaded %s waypoints from %s", cache.size(), waypointDir));
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(String.format("Error loading waypoints from %s: %s", waypointDir, LogFormatter.toString(e)));
            }
        }
    }

    /**
     * Load.
     *
     * @param waypoints the waypoints
     * @param forceSave the force save
     */
    public void load(Collection<Waypoint> waypoints, boolean forceSave)
    {
        for (Waypoint waypoint : waypoints)
        {
            if (waypoint.isPersistent() && (forceSave || waypoint.isDirty()))
            {
                save(waypoint);
            }
            else
            {
                cache.put(waypoint.getId(), waypoint);
            }

            dimensions.addAll(waypoint.getDimensions());
        }
        loaded = true;
    }

    /**
     * Has loaded boolean.
     *
     * @return the boolean
     */
    public boolean hasLoaded()
    {
        return loaded;
    }

    /**
     * Gets loaded dimensions.
     *
     * @return the loaded dimensions
     */
    public List<Integer> getLoadedDimensions()
    {
        return new ArrayList<Integer>(dimensions);
    }


}
