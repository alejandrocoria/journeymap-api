/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.RegionCoord;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Provides a common cache for TileDrawSteps that can be reused as Tile positions shift
 */
public class TileDrawStepCache
{
    private final Logger logger = JourneyMap.getLogger();
    private final Cache<Integer, TileDrawStep> drawStepCache;
    private Path lastDimDir;

    private TileDrawStepCache()
    {
        this.drawStepCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<Integer, TileDrawStep>()
                {
                    @Override
                    public void onRemoval(RemovalNotification<Integer, TileDrawStep> notification)
                    {
                        TileDrawStep oldDrawStep = notification.getValue();
                        if (oldDrawStep != null)
                        {
                            oldDrawStep.clearTexture();
                        }
                    }
                })
                .build();
    }

    public static Cache<Integer, TileDrawStep> instance()
    {
        return Holder.INSTANCE.drawStepCache;
    }

    public static TileDrawStep getOrCreate(final Constants.MapType mapType, RegionCoord regionCoord, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        return Holder.INSTANCE._getOrCreate(mapType, regionCoord, zoom, highQuality, sx1, sy1, sx2, sy2);
    }

    public static void clear()
    {
        instance().invalidateAll();
    }

    private TileDrawStep _getOrCreate(final Constants.MapType mapType, RegionCoord regionCoord, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        checkWorldChange(regionCoord);

        final int hash = TileDrawStep.toHashCode(regionCoord, mapType, zoom, sx1, sy1, sx2, sy2);
        TileDrawStep tileDrawStep = drawStepCache.getIfPresent(hash);
        if (tileDrawStep == null)
        {
            tileDrawStep = new TileDrawStep(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
            drawStepCache.put(hash, tileDrawStep);
        }
        return tileDrawStep;
    }

    private void checkWorldChange(RegionCoord regionCoord)
    {
        if (!regionCoord.dimDir.equals(lastDimDir))
        {
            lastDimDir = regionCoord.dimDir;
            drawStepCache.invalidateAll();
        }
    }

    private static class Holder
    {
        private static final TileDrawStepCache INSTANCE = new TileDrawStepCache();
    }

}
