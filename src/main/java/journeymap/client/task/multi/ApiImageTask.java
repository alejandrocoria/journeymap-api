/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.multi;

import journeymap.client.api.display.Context;
import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.model.MapType;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

/**
 * Fetches an image for the API
 */
public class ApiImageTask implements Runnable
{
    /**
     * The Mod id.
     */
    final String modId;
    /**
     * The Dimension.
     */
    final int dimension;
    /**
     * The Map type.
     */
    final MapType mapType;
    /**
     * The Start chunk.
     */
    final ChunkPos startChunk;
    /**
     * The End chunk.
     */
    final ChunkPos endChunk;
    /**
     * The V slice.
     */
    final Integer vSlice;
    /**
     * The Zoom.
     */
    final int zoom;
    /**
     * The Show grid.
     */
    final boolean showGrid;
    /**
     * The Jm world dir.
     */
    final File jmWorldDir;
    /**
     * The Callback.
     */
    final Consumer<BufferedImage> callback;

    /**
     * Instantiates a new Api image task.
     *
     * @param modId      the mod id
     * @param dimension  the dimension
     * @param apiMapType the api map type
     * @param startChunk the start chunk
     * @param endChunk   the end chunk
     * @param vSlice     the v slice
     * @param zoom       the zoom
     * @param showGrid   the show grid
     * @param callback   the callback
     */
    public ApiImageTask(final String modId, final int dimension, final Context.MapType apiMapType,
                        final ChunkPos startChunk, final ChunkPos endChunk, Integer vSlice, final int zoom, final boolean showGrid,
                        final Consumer<BufferedImage> callback)
    {
        this.modId = modId;
        this.dimension = dimension;
        this.startChunk = startChunk;
        this.endChunk = endChunk;
        this.zoom = zoom;
        this.showGrid = showGrid;
        this.callback = callback;
        this.vSlice = vSlice;
        this.mapType = MapType.fromApiContextMapType(apiMapType, vSlice, dimension);
        this.jmWorldDir = FileHandler.getJMWorldDir(FMLClientHandler.instance().getClient());
    }

    @Override
    public void run()
    {
        BufferedImage image = null;

        try
        {
            final int scale = (int) Math.pow(2, zoom);
            image = RegionImageHandler.getMergedChunks(jmWorldDir, startChunk, endChunk, mapType, scale, showGrid);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error in ApiImageTask: " + t, LogFormatter.toString(t));
        }

        // Callback on main thread
        final BufferedImage finalImage = image;
        Minecraft.getMinecraft().addScheduledTask(() -> callback.accept(finalImage));
    }

}
