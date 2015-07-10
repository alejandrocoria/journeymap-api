/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.server;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.model.MapType;
import net.techbrew.journeymap.render.map.Tile;
import org.apache.logging.log4j.Level;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Provides a map image by combining region files.
 *
 * @author mwoodman
 */
public class TileService extends FileService
{

    public static final String CALLBACK_PARAM = "callback";  //$NON-NLS-1$
    public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final long serialVersionUID = 4412225358529161454L;
    private byte[] blankImage;

    /**
     * Serves chunk data and player info.
     */
    public TileService()
    {
        super();
    }

    @Override
    public String path()
    {
        return "/tile"; //$NON-NLS-1$
    }

    @Override
    public void filter(Event event) throws Event, Exception
    {

        long start = System.currentTimeMillis();

        // Parse query for parameters
        Query query = event.query();
        query.parse();

        Minecraft minecraft = ForgeHelper.INSTANCE.getClient();
        World theWorld = minecraft.theWorld;
        if (theWorld == null)
        {
            throwEventException(503, "World not connected", event, false);
        }

        // Ensure world is loaded
        if (!JourneyMap.getInstance().isMapping())
        {
            throwEventException(503, "JourneyMap not started", event, false);
        }

        // Ensure world dir is found
        File worldDir = FileHandler.getJMWorldDir(minecraft);
        if (!worldDir.exists() || !worldDir.isDirectory())
        {
            throwEventException(400, "World not found", event, true);
        }

        try
        {
            int zoom = getParameter(query, "zoom", 0); //$NON-NLS-1$

            // Region coords
            final int x = getParameter(query, "x", 0); //$NON-NLS-1$
            Integer vSlice = getParameter(query, "depth", (Integer) null); //$NON-NLS-1$
            final int z = getParameter(query, "z", 0); //$NON-NLS-1$
            final int dimension = getParameter(query, "dim", 0);  //$NON-NLS-1$

            // Map type
            final String mapTypeString = getParameter(query, "mapType", MapType.Name.day.name()); //$NON-NLS-1$
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

            if (mapTypeName == MapType.Name.underground && WorldData.isHardcoreAndMultiplayer())
            {
                ResponseHeader.on(event).contentType(ContentType.png).noCache();
                serveFile(RegionImageHandler.getBlank512x512ImageFile(), event);
            }
            else
            {

                // Determine chunks for coordinates at zoom level
                final int scale = (int) Math.pow(2, zoom);
                final int distance = 32 / scale;
                final int minChunkX = x * distance;
                final int minChunkZ = z * distance;
                final int maxChunkX = minChunkX + distance - 1;
                final int maxChunkZ = minChunkZ + distance - 1;

                //System.out.println("zoom " + zoom + ", scale=" + scale + ", distance=" + distance + ": " + minChunkX + "," + minChunkZ + " - " + maxChunkX + "," + maxChunkZ);

                final ChunkCoordIntPair startCoord = new ChunkCoordIntPair(minChunkX, minChunkZ);
                final ChunkCoordIntPair endCoord = new ChunkCoordIntPair(maxChunkX, maxChunkZ);

                boolean showGrid = JourneyMap.getFullMapProperties().showGrid.get();
                MapType mapType = new MapType(mapTypeName, vSlice, dimension);
                final BufferedImage img = RegionImageHandler.getMergedChunks(worldDir, startCoord, endCoord, mapType, true, null, Tile.TILESIZE, Tile.TILESIZE, false, showGrid);

                ResponseHeader.on(event).contentType(ContentType.png).noCache();
                serveImage(event, img);
            }

            final long stop = System.currentTimeMillis();
            if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
            {
                JourneyMap.getLogger().debug((stop - start) + "ms to serve tile");
            }

        }
        catch (NumberFormatException e)
        {
            reportMalformedRequest(event);
        }

    }

}
