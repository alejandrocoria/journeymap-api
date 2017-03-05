/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.data;

import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides data of what's changed in RegionImageCache in a Map.
 * This provider requires parameters for a valid response.
 *
 * @author techbrew
 */
public class ImagesData
{
    /**
     * The constant PARAM_SINCE.
     */
    public static final String PARAM_SINCE = "images.since";

    /**
     * The Since.
     */
// last query time
    final long since;

    /**
     * The Regions.
     */
// list of region coords changed {[x][z]}
    final List<Object[]> regions;

    /**
     * The Query time.
     */
// Last time this was queried
    final long queryTime;

    /**
     * Constructor.
     *
     * @param since the since
     */
    public ImagesData(Long since)
    {
        final long now = new Date().getTime();
        this.queryTime = now;
        this.since = (since == null) ? now : since;

        List<RegionCoord> dirtyRegions = RegionImageCache.INSTANCE.getChangedSince(null, this.since);
        if (dirtyRegions.isEmpty())
        {
            this.regions = Collections.EMPTY_LIST;
        }
        else
        {
            this.regions = new ArrayList<Object[]>(dirtyRegions.size());
            for (RegionCoord rc : dirtyRegions)
            {
                this.regions.add(new Integer[]{rc.regionX, rc.regionZ});
            }
        }
    }
}
