/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.waypoint;

import com.google.common.io.Files;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.Waypoint;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Read JM waypoint files and return a collection of the results.
 */
public class JmReader
{
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
            if (wp != null)
            {
                // Check for obsolete filename
                if (!wp.getFileName().endsWith(waypointFile.getName()))
                {
                    wp.setDirty(true);
                    obsoleteFiles.add(waypointFile);
                }
                waypoints.add(wp);
            }
        }

        while (!obsoleteFiles.isEmpty())
        {
            remove(obsoleteFiles.remove(0));
        }

        return waypoints;
    }

    private void remove(File waypointFile)
    {
        try
        {
            waypointFile.delete();
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn(String.format("Can't delete waypoint file %s: %s", waypointFile, e.getMessage()));
            waypointFile.deleteOnExit();
        }
    }

    private Waypoint load(File waypointFile)
    {
        String waypointString = null;
        Waypoint waypoint = null;
        try
        {
            waypointString = Files.toString(waypointFile, Charset.forName("UTF-8"));
            waypoint = Waypoint.fromString(waypointString);
            return waypoint;
        }
        catch (Throwable e)
        {
            JourneyMap.getLogger().error(String.format("Can't load waypoint file %s with contents: %s because %s", waypointFile, waypointString, e.getMessage()));
        }
        return waypoint;
    }
}
