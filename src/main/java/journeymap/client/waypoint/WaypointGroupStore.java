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
import java.util.ArrayList;
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
        try
        {
            File groupFile = new File(FileHandler.getWaypointDir(), FILENAME);
            if (groupFile.exists())
            {
                HashMap<String, WaypointGroup> map = new HashMap<String, WaypointGroup>(0);
                String groupsString = Files.toString(groupFile, Charset.forName("UTF-8"));
                map = WaypointGroup.GSON.fromJson(groupsString, map.getClass());
                if (!map.isEmpty())
                {
                    cache.invalidateAll();
                    cache.putAll(map);
                    Journeymap.getLogger().info("Loaded WaypointGroups from file");
                } else
                {
                    Journeymap.getLogger().info("WaypointGroups file was empty");
                }
            } else
            {
                Journeymap.getLogger().info("WaypointGroups file doesn't exist");
            }
        } catch (Exception e)
        {
            Journeymap.getLogger().error("Error loading WaypointGroups from file: " + LogFormatter.toPartialString(e));
        }

        /**
         * Ensure default group
         */
        putIfNew(WaypointGroup.DEFAULT);
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
            try
            {
                TreeMap<String, WaypointGroup> map = new TreeMap<String, WaypointGroup>(new Comparator<String>()
                {
                    final String defaultKey = WaypointGroup.DEFAULT.getKey();

                    @Override
                    public int compare(String o1, String o2)
                    {
                        if (o1.equals(defaultKey)) return -1;
                        if (o2.equals(defaultKey)) return 1;
                        return o1.compareTo(o2);
                    }
                });

                map.putAll(cache.asMap());

                File waypointDir = FileHandler.getWaypointDir();
                if (!waypointDir.exists())
                {
                    waypointDir.mkdirs();
                }
                File groupFile = new File(waypointDir, FILENAME);
                Files.write(WaypointGroup.GSON.toJson(map), groupFile, Charset.forName("UTF-8"));

                for (WaypointGroup group : cache.asMap().values())
                {
                    group.setDirty(false);
                }
                Journeymap.getLogger().info("Wrote WaypointGroups to file");
            } catch (Exception e)
            {
                Journeymap.getLogger().error("Error writing WaypointGroups to file: " + LogFormatter.toPartialString(e));
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
                        } else
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
