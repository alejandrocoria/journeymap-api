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
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides a common cache for TileDrawSteps that can be reused as Tile positions shift
 */
public class TileDrawStepCache implements RemovalListener<Integer, TileDrawStep>
{
    private final Logger logger = JourneyMap.getLogger();
    private final Cache<Integer, TileDrawStep> drawStepCache;

    private final List<TileDrawStep> expired = Collections.synchronizedList(new ArrayList<TileDrawStep>());

    private File lastWorldDir;

    private TileDrawStepCache()
    {
        this.drawStepCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .removalListener(this)
                .build();
    }

    public static Cache<Integer, TileDrawStep> instance()
    {
        return Holder.INSTANCE.drawStepCache;
    }

    public static synchronized TileDrawStep getOrCreate(final Constants.MapType mapType, RegionCoord regionCoord, Integer zoom, int sx1, int sy1, int sx2, int sy2)
    {
        Holder.INSTANCE.checkWorldChange(regionCoord.worldDir);

        final int hash = TileDrawStep.toHashCode(regionCoord, mapType, zoom, sx1, sy1, sx2, sy2);
        TileDrawStepCache tdsc = Holder.INSTANCE;
        synchronized (tdsc.drawStepCache)
        {
            TileDrawStep tileDrawStep = tdsc.drawStepCache.getIfPresent(hash);
            if (tileDrawStep == null)
            {
                tileDrawStep = new TileDrawStep(regionCoord, mapType, zoom, sx1, sy1, sx2, sy2);
                tdsc.drawStepCache.put(hash, tileDrawStep);
                //tdsc.logger.info("Cached new TileDrawStep: " + tileDrawStep);
            }
            return tileDrawStep;
        }
    }

    public static void cleanUp()
    {
        Holder.INSTANCE.removeExpired();
    }

    private void checkWorldChange(File worldDir)
    {
        if (!(worldDir.equals(lastWorldDir)))
        {
            lastWorldDir = worldDir;
            drawStepCache.invalidateAll();
        }
    }

    @Override
    public void onRemoval(RemovalNotification<Integer, TileDrawStep> notification)
    {
        TileDrawStep oldDrawStep = notification.getValue();
        if (oldDrawStep != null)
        {
            expired.add(oldDrawStep);
        }

        if (expired.size() > 5)
        {
            removeExpired();
        }
    }

    public void removeExpired()
    {
        try
        {
            if (Display.isCurrent())
            {
                synchronized (expired)
                {
                    while (!expired.isEmpty())
                    {
                        TileDrawStep tileDrawStep = expired.remove(0);
                        tileDrawStep.clearTexture();
                        //logger.info("TileDrawStepCache.removeExpired(): " + tileDrawStep);
                    }
                }
            }
            else
            {
                logger.info("TileDrawStepCache.removeExpired() invalid on this thread");
            }
        }
        catch (Throwable e)
        {
            logger.error("TileDrawStepCache.removeExpired() encountered an unexpected error: " + e);
        }
    }

    private static class Holder
    {
        private static final TileDrawStepCache INSTANCE = new TileDrawStepCache();
    }

}
