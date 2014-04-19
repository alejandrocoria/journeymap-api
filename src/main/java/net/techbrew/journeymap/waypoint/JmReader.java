package net.techbrew.journeymap.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.Waypoint;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Read JM waypoint files and return a collection of the results.
 */
public class JmReader
{
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Collection<Waypoint> loadWaypoints(File waypointDir)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

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
                    wp.setDirty(true);
                    obsoleteFiles.add(waypointFile);
                }
                waypoints.add(wp);
            }
        }

        while(!obsoleteFiles.isEmpty())
        {
            remove(obsoleteFiles.remove(0));
        }

        return waypoints;
    }

    private void remove(File waypointFile)
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

    private Waypoint load(File waypointFile)
    {
        Waypoint waypoint = null;
        FileReader reader = null;
        try
        {
            reader = new FileReader(waypointFile);
            waypoint = gson.fromJson(reader, Waypoint.class);
            return waypoint;
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
