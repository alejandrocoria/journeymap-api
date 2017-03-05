/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Provides a common cache for TileDrawSteps that can be reused as Tile positions shift
 */
public class TileDrawStepCache
{
    private final Logger logger = Journeymap.getLogger();
    private final Cache<String, TileDrawStep> drawStepCache;
    private File worldDir;
    private MapType mapType;

    private TileDrawStepCache()
    {
        this.drawStepCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, TileDrawStep>()
                {
                    @Override
                    public void onRemoval(RemovalNotification<String, TileDrawStep> notification)
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

    /**
     * Instance cache.
     *
     * @return the cache
     */
    public static Cache<String, TileDrawStep> instance()
    {
        return Holder.INSTANCE.drawStepCache;
    }

    /**
     * Gets or create.
     *
     * @param mapType     the map type
     * @param regionCoord the region coord
     * @param zoom        the zoom
     * @param highQuality the high quality
     * @param sx1         the sx 1
     * @param sy1         the sy 1
     * @param sx2         the sx 2
     * @param sy2         the sy 2
     * @return the or create
     */
    public static TileDrawStep getOrCreate(final MapType mapType, RegionCoord regionCoord, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        return Holder.INSTANCE._getOrCreate(mapType, regionCoord, zoom, highQuality, sx1, sy1, sx2, sy2);
    }

    /**
     * Clear.
     */
    public static void clear()
    {
        instance().invalidateAll();
    }

    /**
     * Sets context.
     *
     * @param worldDir the world dir
     * @param mapType  the map type
     */
    public static void setContext(File worldDir, MapType mapType)
    {
        if (!worldDir.equals(Holder.INSTANCE.worldDir))
        {
            instance().invalidateAll();
        }
        Holder.INSTANCE.worldDir = worldDir;
        Holder.INSTANCE.mapType = mapType;
    }

    /**
     * Size long.
     *
     * @return the long
     */
    public static long size()
    {
        return instance().size();
    }

    private TileDrawStep _getOrCreate(final MapType mapType, RegionCoord regionCoord, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        checkWorldChange(regionCoord);

        final String key = TileDrawStep.toCacheKey(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
        TileDrawStep tileDrawStep = drawStepCache.getIfPresent(key);
        if (tileDrawStep == null)
        {
            tileDrawStep = new TileDrawStep(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
            drawStepCache.put(key, tileDrawStep);
        }
        return tileDrawStep;
    }

    private void checkWorldChange(RegionCoord regionCoord)
    {
        if (!regionCoord.worldDir.equals(this.worldDir))
        {
            drawStepCache.invalidateAll();
            RegionImageCache.INSTANCE.clear();
        }
    }

    private static class Holder
    {
        private static final TileDrawStepCache INSTANCE = new TileDrawStepCache();
    }

}
