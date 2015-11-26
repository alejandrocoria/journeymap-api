/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.waypoint;

import journeymap.client.Constants;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.ChatLog;
import journeymap.client.log.LogFormatter;
import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatAllowedCharacters;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read Rei's waypoint files and return a collection of the results.
 */
public class ReiReader
{
    public static final String[] classNames = {"reifnsk.minimap.ReiMinimap", "reifnsk.minimap.Waypoint"};
    public static Boolean modLoaded;

    int pointErrors = 0;
    int fileErrors = 0;

    /**
     * Get waypoints from ReiMinimap instance.
     *
     * @return
     */
    public static java.util.List<Waypoint> loadWaypoints()
    {
        ArrayList<Waypoint> converted = new ArrayList<Waypoint>();
        try
        {
            Class.forName("reifnsk.minimap.ReiMinimap").getDeclaredField("instance");
            reifnsk.minimap.ReiMinimap reiMinimap = reifnsk.minimap.ReiMinimap.instance;
            java.util.List<reifnsk.minimap.Waypoint> reiWaypoints = reiMinimap.getWaypoints();
            if (reiWaypoints == null || reiWaypoints.isEmpty())
            {
                return Collections.EMPTY_LIST;
            }

            EntityPlayer player = ForgeHelper.INSTANCE.getClient().thePlayer;
            int dimension = player != null ? player.dimension : 0;
            for (reifnsk.minimap.Waypoint reiWp : reiWaypoints)
            {
                Waypoint jmWp = new Waypoint(reiWp.name, reiWp.x, reiWp.y, reiWp.z, reiWp.enable,
                        (int) (reiWp.red * 255.0F) & 255,
                        (int) (reiWp.green * 255.0F) & 255,
                        (int) (reiWp.blue * 255.0F) & 255,
                        reiWp.type == 1 ? Waypoint.Type.Death : Waypoint.Type.Normal,
                        Waypoint.Origin.ReiMinimap,
                        dimension,
                        Arrays.asList(dimension));
                jmWp.setReadOnly(true);
                converted.add(jmWp);
            }
            return converted;
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().warn("Incompatible version of Reijm.minimap. Tried reifnsk.jm.minimap.Reijm.minimap.instance.getWaypoints(): " + e);
            if (!(e instanceof ClassNotFoundException))
            {
                ChatLog.announceI18N("jm.waypoint.import_rei_version");
            }
            modLoaded = false;
            return Collections.EMPTY_LIST;
        }
    }

    public static String getPointsFilename()
    {
        String worldName = null;
        try
        {
            NetHandlerPlayClient sendQueue = ForgeHelper.INSTANCE.getClient().getNetHandler();
            SocketAddress addr = sendQueue.getNetworkManager().getRemoteAddress();
            String addrStr = addr.toString().replaceAll("[\r\n]", "");
            Matcher matcher = Pattern.compile("(.*)/(.*):([0-9]+)").matcher(addrStr);
            if (matcher.matches())
            {
                worldName = matcher.group(1);
                if (worldName.isEmpty())
                {
                    worldName = matcher.group(2);
                }
                if (!matcher.group(3).equals("25565"))
                {
                    worldName = (new StringBuilder()).append(worldName).append("[").append(matcher.group(3)).append("]").toString();
                }

                char arr$[] = ChatAllowedCharacters.allowedCharactersArray;
                int len$ = arr$.length;
                for (int i$ = 0; i$ < len$; i$++)
                {
                    char c = arr$[i$];
                    worldName = worldName.replace(c, '_');
                }
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Could not derive Rei filename: " + e.getMessage());
        }

        if (worldName == null)
        {
            worldName = "WorldName";
        }

        return String.format("mods\\rei_minimap\\%s.DIM*.points", worldName);
    }

    /**
     * Get waypoints from ReiMinimap .points files
     *
     * @param waypointDir
     * @param deleteOnSuccess
     * @return
     */
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

        if (files == null || files.length == 0)
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

                if (deleteOnSuccess && pointErrorCount == pointErrors)
                {
                    pointsFile.deleteOnExit();
                    pointsFile.delete();
                }
            }
            catch (Exception e)
            {
                ChatLog.announceError(Constants.getString("jm.waypoint.import_rei_file_error", pointsFile.getName()));
                Journeymap.getLogger().error(LogFormatter.toString(e));
                fileErrors++;
            }
        }

        if (waypoints.isEmpty())
        {
            ChatLog.announceI18N("jm.waypoint.import_rei_failure");
        }
        else if (fileErrors == 0 && pointErrors == 0)
        {
            ChatLog.announceI18N("jm.waypoint.import_rei_success", waypoints.size());
        }
        else
        {
            ChatLog.announceI18N("jm.waypoint.import_rei_errors", waypoints.size(), pointErrors);
        }

        return waypoints;
    }

    /**
     * Load waypoints for the given dimension from the ReiMinimap pointsFile into the provided arraylist.
     *
     * @param dimension
     * @param pointsFile
     * @param waypoints
     * @throws Exception
     */
    private void loadWaypoints(int dimension, File pointsFile, ArrayList<Waypoint> waypoints) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(pointsFile));
        String line;
        while ((line = br.readLine()) != null)
        {
            Waypoint waypoint = loadWaypoint(dimension, line);
            if (waypoint != null)
            {
                waypoints.add(waypoint);
            }
        }
        br.close();
    }

    /**
     * Create a waypoint from a ReiMinimap line in a .points file.
     *
     * @param dimension
     * @param line
     * @return
     */
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

            Waypoint waypoint = new Waypoint(name, x, y, z, color, Waypoint.Type.Normal, dimension);
            waypoint.setEnable(enable);
            waypoint.setOrigin(Waypoint.Origin.ReiMinimap);
            waypoint.setDirty(true);
            return waypoint;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Couldn't parse " + v[i] + " as " + parts[i] + " in \"" + line + "\" because: " + e.getMessage());
            pointErrors++;
            return null;
        }
    }
}
