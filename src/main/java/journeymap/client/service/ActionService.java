/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.service;

import journeymap.client.JourneymapClient;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.io.FileHandler;
import journeymap.client.io.MapSaver;
import journeymap.client.log.LogFormatter;
import journeymap.client.model.MapType;
import journeymap.client.task.multi.MapRegionTask;
import journeymap.client.task.multi.SaveMapTask;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

/**
 * Service delegate for special actions.
 */
public class ActionService extends BaseService
{

    public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final long serialVersionUID = 4412225358529161454L;
    private static boolean debug = true;

    /**
     * Serves chunk data and player info.
     */
    public ActionService()
    {
        super();
    }

    @Override
    public String path()
    {
        return "/action"; //$NON-NLS-1$
    }

    @Override
    public void filter(Event event) throws Event, Exception
    {

        // Parse query for parameters
        Query query = event.query();
        query.parse();

        // Check world
        Minecraft minecraft = ForgeHelper.INSTANCE.getClient();
        World theWorld = minecraft.theWorld;
        if (theWorld == null)
        {
            throwEventException(503, "World not connected", event, false);
        }

        // Ensure world is loaded
        if (!JourneymapClient.getInstance().isMapping())
        {
            throwEventException(503, "JourneyMap not mapping", event, false);
        }

        // Use type param to delegate
        String type = getParameter(query, "type", (String) null); //$NON-NLS-1$
        if ("savemap".equals(type))
        {
            saveMap(event);
        }
        else if ("automap".equals(type))
        {
            autoMap(event);
        }
        else
        {
            String error = "Bad request: type=" + type; //$NON-NLS-1$
            throwEventException(400, error, event, true);
        }

    }

    /**
     * Save a map of the world at the current dimension, vSlice and map type.
     *
     * @param event
     * @throws Event
     * @throws Exception
     */
    private void saveMap(Event event) throws Event, Exception
    {

        Query query = event.query();

        Minecraft minecraft = ForgeHelper.INSTANCE.getClient();
        World theWorld = minecraft.theWorld;


        try
        {

            // Ensure world dir available
            File worldDir = FileHandler.getJMWorldDir(minecraft);
            if (!worldDir.exists() || !worldDir.isDirectory())
            {
                String error = "World unknown: " + (worldDir.getAbsolutePath());
            }

            Integer vSlice = getParameter(query, "depth", (Integer) null); //$NON-NLS-1$
            final int dimension = getParameter(query, "dim", 0);  //$NON-NLS-1$
            final String mapTypeString = getParameter(query, "mapType", MapType.Name.day.name()); //$NON-NLS-1$
            MapType mapType = null;
            MapType.Name mapTypeName = null;
            try
            {
                mapTypeName = MapType.Name.valueOf(mapTypeString);
            }
            catch (Exception e)
            {
                String error = "Bad request: mapType=" + mapTypeString; //$NON-NLS-1$
                throwEventException(400, error, event, true);
            }
            if (mapTypeName != MapType.Name.underground)
            {
                vSlice = null;
            }

            // Validate cave mapping allowed
            // Check for hardcore
            Boolean hardcore = theWorld.getWorldInfo().isHardcoreModeEnabled();
            if (mapType.isUnderground() && hardcore)
            {
                String error = "Cave mapping on hardcore servers is not allowed"; //$NON-NLS-1$
                throwEventException(403, error, event, true);
            }

            // Check estimated file size
            MapSaver mapSaver = new MapSaver(worldDir, mapType);
            if (!mapSaver.isValid())
            {
                throwEventException(403, "No image files to save.", event, true);
            }
            JourneymapClient.getInstance().toggleTask(SaveMapTask.Manager.class, true, mapSaver);

            Properties response = new Properties();
            response.put("filename", mapSaver.getSaveFileName());
            respondJson(event, response);

        }
        catch (NumberFormatException e)
        {
            reportMalformedRequest(event);
        }
        catch (Event eventEx)
        {
            throw eventEx;
        }
        catch (Throwable t)
        {
            JourneymapClient.getLogger().error(LogFormatter.toString(t));
            throwEventException(500, "Unexpected error handling path: " + (path), event, true);
        }
    }

    /**
     * Automap the world at the current dimension and vSlice.
     *
     * @param event
     * @throws Event
     * @throws Exception
     */
    private void autoMap(Event event) throws Event, Exception
    {

        boolean enabled = JourneymapClient.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class);
        String scope = getParameter(event.query(), "scope", "stop");

        HashMap responseObj = new HashMap();

        if ("stop".equals(scope))
        {
            if (enabled)
            {
                JourneymapClient.getInstance().toggleTask(MapRegionTask.Manager.class, false, Boolean.FALSE);
                responseObj.put("message", "automap_complete");
            }
        }
        else if (!enabled)
        {
            boolean doAll = "all".equals(scope);
            JourneymapClient.getInstance().toggleTask(MapRegionTask.Manager.class, true, doAll);
            responseObj.put("message", "automap_started");
        }
        else
        {
            responseObj.put("message", "automap_already_started");
        }

        respondJson(event, responseObj);
    }

}
