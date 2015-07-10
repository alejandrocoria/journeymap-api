/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.data;

import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides data of what's changed in RegionImageCache in a Map.
 * This provider requires parameters for a valid response.
 *
 * @author mwoodman
 */
public class ImagesData
{
    public static final String PARAM_SINCE = "images.since";

    final long since;
    final List<Object[]> regions;
    final long queryTime;

    /**
     * Constructor.
     */
    public ImagesData(Long since)
    {
        final long now = new Date().getTime();
        this.since = (since == null) ? now : since;
        this.queryTime = now;

        List<Object[]> coords = null;
        List<RegionCoord> regions = RegionImageCache.instance().getDirtySince(null, this.since);
        if (regions.isEmpty())
        {
            this.regions = Collections.EMPTY_LIST;
        }
        else
        {
            this.regions = new ArrayList<Object[]>(regions.size());
            for (RegionCoord rc : regions)
            {
                this.regions.add(new Integer[]{rc.regionX, rc.regionZ});
            }
        }
    }
}
