package net.techbrew.journeymap.waypoint;

import net.minecraft.util.ChunkCoordinates;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Read Rei's waypoint files and return a collection of the results.
 */
public class ReiReader
{
    int pointErrors = 0;
    int fileErrors = 0;

    public Collection<Waypoint> loadWaypoints(File waypointDir, boolean deleteOnSuccess)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

        File[] files = waypointDir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.contains(".DIM") && name.endsWith(".points");
            }
        });

        if(files.length==0)
        {
            return waypoints;
        }

        for (File pointsFile : files)
        {
            try
            {
                int pointErrorCount = pointErrors;

                String name = pointsFile.getName();
                int start = name.lastIndexOf("DIM") + 3;
                int end = name.lastIndexOf(".points");
                String dimName = name.substring(start, end);
                int dimension = Integer.parseInt(dimName);

                loadWaypoints(dimension, pointsFile, waypoints);

                if(deleteOnSuccess && pointErrorCount==pointErrors)
                {
                    pointsFile.deleteOnExit();
                    pointsFile.delete();
                }
            }
            catch (Exception e)
            {
                ChatLog.announceError(Constants.getString("Waypoint.import_rei_file_error", pointsFile.getName()));
                JourneyMap.getLogger().severe(LogFormatter.toString(e));
                fileErrors++;
            }
        }

        if(waypoints.isEmpty())
        {
            ChatLog.announceI18N("Waypoint.import_rei_failure");
        }
        else if(fileErrors == 0 && pointErrors==0)
        {
            ChatLog.announceI18N("Waypoint.import_rei_success", waypoints.size());
        }
        else
        {
            ChatLog.announceI18N("Waypoint.import_rei_errors", waypoints.size(), pointErrors);
        }

        return waypoints;
    }

    private void loadWaypoints(int dimension, File pointsFile, ArrayList<Waypoint> waypoints) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(pointsFile));
        String line;
        while ((line = br.readLine()) != null)
        {
            Waypoint waypoint = loadWaypoint(dimension, line);
            if(waypoint!=null)
            {
                waypoints.add(waypoint);
            }
        }
        br.close();
    }

    private Waypoint loadWaypoint(int dimension, String line)
    {
        // Example: Cool DesertVillage By Temple:-503:76:-322:true:16CC24
        String[] parts = {"name", "x", "y", "z", "enable", "color"};
        String[] v = line.split(":");
        int i = 0;
        try
        {
            String name = v[i];
            int x = Integer.parseInt(v[++i]);
            int y = Integer.parseInt(v[++i]);
            int z = Integer.parseInt(v[++i]);
            boolean enable = Boolean.parseBoolean(v[++i]);
            Color color = new Color(Integer.parseInt(v[++i], 16));

            Waypoint waypoint = new Waypoint(name, new ChunkCoordinates(x, y, z), color, Waypoint.Type.Normal, dimension);
            waypoint.setEnable(enable);
            waypoint.setOrigin("rei");
            waypoint.setDirty(true);
            return waypoint;
        }
        catch(Exception e)
        {
            JourneyMap.getLogger().warning("Couldn't parse " + v[i]  + " as " + parts[i] + " in \"" + line + "\" because: " + e.getMessage());
            pointErrors++;
            return null;
        }
    }
}
