package net.techbrew.journeymap.waypoint;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Disk-backed cache for Waypoints.
 */
public class WaypointStore
{
    private static class Holder
    {
        private static final WaypointStore INSTANCE = new WaypointStore();
    }

    public static WaypointStore instance()
    {
        return Holder.INSTANCE;
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private boolean loaded = false;
    private final Cache<String, Waypoint> cache = CacheBuilder.newBuilder().build();

    private WaypointStore()
    {
    }

    private void writeToFile(Waypoint waypoint)
    {
        File waypointFile = null;
        try
        {
            // Write to file
            waypointFile = new File(FileHandler.getWaypointDir(), waypoint.getFileName());
            FileWriter fw = new FileWriter(waypointFile);
            fw.write(gson.toJson(waypoint));
            fw.flush();
            fw.close();
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't save waypoint file %s: %s", waypointFile, LogFormatter.toString(e)));
        }
    }

    public Collection<Waypoint> getAll()
    {
        return cache.asMap().values();
    }

    public void save(Waypoint waypoint)
    {
        cache.put(waypoint.getId(), waypoint);
        writeToFile(waypoint);
    }

    public void remove(Waypoint waypoint)
    {
        cache.invalidate(waypoint.getId());
        File waypointFile = null;
        waypointFile = new File(FileHandler.getWaypointDir(), waypoint.getFileName());
        remove(waypointFile);
    }

    public void remove(File waypointFile)
    {
        try
        {
            Files.deleteIfExists(waypointFile.toPath());
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning(String.format("Can't delete waypoint file %s: %s", waypointFile, e.getMessage()));
            waypointFile.deleteOnExit();
        }
    }

    public void clear()
    {
        cache.invalidateAll();
        loaded = false;
    }

    public void load()
    {
        synchronized(cache)
        {
            cache.invalidateAll();

            try
            {
                File waypointDir = FileHandler.getWaypointDir();
                File[] files = waypointDir.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".json");
                    }
                });

                ArrayList<File> obsoleteFiles = new ArrayList<File>();

                for (File waypointFile : files)
                {
                    Waypoint wp = load(waypointFile);
                    if(wp!=null)
                    {
                        // Check for obsolete filename
                        if(!wp.getFileName().endsWith(waypointFile.getName()))
                        {
                            save(wp);
                            obsoleteFiles.add(waypointFile);
                        }
                    }
                }

                while(!obsoleteFiles.isEmpty())
                {
                    remove(obsoleteFiles.remove(0));
                }

                loaded = true;

                JourneyMap.getLogger().info(String.format("Loaded %s waypoints for world", cache.size()));
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().severe(String.format("Error loading waypoints: %s", LogFormatter.toString(e)));
            }
        }
    }

    public boolean hasLoaded()
    {
        return loaded;
    }

    private Waypoint load(File waypointFile)
    {
        Waypoint waypoint = null;
        FileReader reader = null;
        try
        {
            reader = new FileReader(waypointFile);
            waypoint = gson.fromJson(reader, Waypoint.class);
            cache.put(waypoint.getId(), waypoint);
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't load waypoint file %s: %s", waypointFile, e.getMessage()));
        }
        finally
        {
            if(reader!=null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception e)
                {
                    JourneyMap.getLogger().severe(String.format("Can't close waypoint file %s: %s", waypointFile, e.getMessage()));
                }
            }
        }
        return waypoint;
    }
}
