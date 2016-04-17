/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

/**
 * Model describing Grid appearance for day/night/caves
 */
public class GridSpecs
{
    public static final GridSpec DEFAULT_DAY = new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .5f);
    public static final GridSpec DEFAULT_NIGHT = new GridSpec(GridSpec.Style.Squares, .5f, .5f, 1f, .3f);
    public static final GridSpec DEFAULT_UNDERGROUND = new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .3f);

    private GridSpec day;
    private GridSpec night;
    private GridSpec underground;

    public GridSpecs()
    {
        this(DEFAULT_DAY.clone(), DEFAULT_NIGHT.clone(), DEFAULT_UNDERGROUND.clone()); // underground
    }

    public GridSpecs(GridSpec day, GridSpec night, GridSpec underground)
    {
        this.day = day;
        this.night = night;
        this.underground = underground;
    }

    public GridSpec getSpec(MapType mapType)
    {
        switch (mapType.name)
        {
            case day:
                return day;
            case night:
                return night;
            case underground:
                return underground;
            default:
                return day;
        }
    }

    public void setSpec(MapType mapType, GridSpec newSpec)
    {
        switch (mapType.name)
        {
            case day:
            {
                day = newSpec.clone();
                return;
            }
            case night:
            {
                night = newSpec.clone();
                return;
            }
            case underground:
            {
                underground = newSpec.clone();
                return;
            }
            default:
            {
                day = newSpec.clone();
            }
        }
    }

    public GridSpecs clone()
    {
        return new GridSpecs(day.clone(), night.clone(), underground.clone());
    }

    public void updateFrom(GridSpecs other)
    {
        day = other.day.clone();
        night = other.night.clone();
        underground = other.underground.clone();
    }
}
