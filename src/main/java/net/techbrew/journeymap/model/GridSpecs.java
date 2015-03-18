package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;

/**
 * Created by Mark on 1/23/2015.
 */
public class GridSpecs
{
    private GridSpec day;
    private GridSpec night;
    private GridSpec underground;

    public GridSpecs()
    {
        this(new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .5f), // day
                new GridSpec(GridSpec.Style.Squares, 0f, 0f, 1f, .3f), // night
                new GridSpec(GridSpec.Style.Squares, 1f, 1f, 1f, .3f)); // underground
    }

    public GridSpecs(GridSpec day, GridSpec night, GridSpec underground)
    {
        this.day = day;
        this.night = night;
        this.underground = underground;
    }

    public GridSpec getSpec(Constants.MapType mapType)
    {
        switch (mapType)
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
}
