/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.waypoint;

import com.google.common.cache.*;
import com.google.common.io.Files;
import journeymap.client.io.FileHandler;
import journeymap.client.model.Waypoint;
import journeymap.client.model.WaypointGroup;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Disk-backed cache of WaypointGroups.
 */
@ParametersAreNonnullByDefault
public enum WaypointGroupStore
{
    INSTANCE;

    public final static String KEY_PATTERN = "%s:%s";
    public final static String FILENAME = "waypoint_groups.json";
    public final LoadingCache<String, WaypointGroup> cache = createCache();

    public WaypointGroup get(String name)
    {
        return get(Journeymap.MOD_ID, name);
    }

    public WaypointGroup get(String origin, String name)
    {
        ensureLoaded();
        return cache.getUnchecked(String.format(KEY_PATTERN, origin, name));
    }

    public boolean exists(WaypointGroup waypointGroup)
    {
        ensureLoaded();
        return cache.getIfPresent(waypointGroup.getKey()) != null;
    }

    public void put(WaypointGroup waypointGroup)
    {
        ensureLoaded();
        cache.put(waypointGroup.getKey(), waypointGroup);
        save(true);
    }

    public boolean putIfNew(WaypointGroup waypointGroup)
    {
        if (exists(waypointGroup))
        {
            return false;
        }
        put(waypointGroup);
        return true;
    }

    public void remove(WaypointGroup waypointGroup)
    {
        ensureLoaded();
        cache.invalidate(waypointGroup.getKey());
        waypointGroup.setDirty(false);
        save();
    }

    private void ensureLoaded()
    {
        if (cache.size() == 0)
        {
            load();
        }
    }

    /**
     * Load cache from disk.
     */
    private void load()
    {
        File groupFile = new File(FileHandler.getWaypointDir(), FILENAME);
        if (groupFile.exists())
        {
            HashMap<String, WaypointGroup> map = new HashMap<String, WaypointGroup>(0);

            try
            {
                String groupsString = Files.toString(groupFile, Charset.forName("UTF-8"));
                map = WaypointGroup.GSON.fromJson(groupsString, map.getClass());
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(String.format("Error reading WaypointGroups file %s: %s", groupFile, LogFormatter.toPartialString(e)));
                try
                {
                    groupFile.renameTo(new File(groupFile.getParentFile(), groupFile.getName() + ".bad"));
                }
                catch (Exception e2)
                {
                    Journeymap.getLogger().error(String.format("Error renaming bad WaypointGroups file %s: %s", groupFile, LogFormatter.toPartialString(e)));
                }
            }

            if (!map.isEmpty())
            {
                cache.invalidateAll();
                cache.putAll(map);
                Journeymap.getLogger().info(String.format("Loaded WaypointGroups file %s", groupFile));

                // Ensure default is there.
                cache.put(WaypointGroup.DEFAULT.getKey(), WaypointGroup.DEFAULT);
                return;
            }
        }

        // Add default and save file
        cache.put(WaypointGroup.DEFAULT.getKey(), WaypointGroup.DEFAULT);
        save(true);
    }

    public void save()
    {
        save(true);
    }

    public void save(boolean force)
    {
        boolean doWrite = force;
        if (!force)
        {
            for (WaypointGroup group : cache.asMap().values())
            {
                if (group.isDirty())
                {
                    doWrite = true;
                    break;
                }
            }
        }

        if (doWrite)
        {
            TreeMap<String, WaypointGroup> map = null;
            try
            {
                map = new TreeMap<String, WaypointGroup>(new Comparator<String>()
                {
                    final String defaultKey = WaypointGroup.DEFAULT.getKey();

                    @Override
                    public int compare(String o1, String o2)
                    {
                        if (o1.equals(defaultKey))
                        {
                            return -1;
                        }
                        if (o2.equals(defaultKey))
                        {
                            return 1;
                        }
                        return o1.compareTo(o2);
                    }
                });

                map.putAll(cache.asMap());

            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(String.format("Error preparing WaypointGroups: %s", LogFormatter.toPartialString(e)));
                return;
            }

            File groupFile = null;
            try
            {
                File waypointDir = FileHandler.getWaypointDir();
                if (!waypointDir.exists())
                {
                    waypointDir.mkdirs();
                }
                groupFile = new File(waypointDir, FILENAME);

                boolean isNew = groupFile.exists();
                Files.write(WaypointGroup.GSON.toJson(map), groupFile, Charset.forName("UTF-8"));

                for (WaypointGroup group : cache.asMap().values())
                {
                    group.setDirty(false);
                }

                if (isNew)
                {
                    Journeymap.getLogger().info("Created WaypointGroups file: " + groupFile);
                }
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(String.format("Error writing WaypointGroups file %s: %s", groupFile, LogFormatter.toPartialString(e)));
            }
        }
    }

    /**
     * Create the cache.
     */
    private LoadingCache<String, WaypointGroup> createCache()
    {
        LoadingCache<String, WaypointGroup> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .removalListener(new RemovalListener<String, WaypointGroup>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onRemoval(RemovalNotification<String, WaypointGroup> notification)
                    {
                        for (Waypoint orphan : WaypointStore.INSTANCE.getAll(notification.getValue()))
                        {
                            orphan.setGroupName(WaypointGroup.DEFAULT.getName());
                            orphan.setGroup(WaypointGroup.DEFAULT.DEFAULT);
                        }
                        save();
                    }
                }).build(new CacheLoader<String, WaypointGroup>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public WaypointGroup load(String key) throws Exception
                    {
                        String name, origin;
                        int index = key.indexOf(":");
                        if (index < 1)
                        {
                            origin = "Unknown";
                            name = key;
                            Journeymap.getLogger().warn("Problematic waypoint group key: " + key);
                        }
                        else
                        {
                            origin = key.substring(0, index);
                            name = key.substring(index, key.length());
                        }

                        return new WaypointGroup(origin, name);
                    }
                });
        return cache;
    }
}
