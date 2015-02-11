package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;

/**
 * Created by Mark on 1/23/2015.
 */
public class GridSpecs
{
    private GridSpec dayGridSpec;
    private GridSpec nightGridSpec;
    private GridSpec undergroundGridSpec;

    public GridSpecs()
    {
        this(new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .5f), // day
                new GridSpec(GridSpec.Style.Squares, 0f, 0f, 1f, .3f), // night
                new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .3f)); // underground
    }

    public GridSpecs(GridSpec dayGridSpec, GridSpec nightGridSpec, GridSpec undergroundGridSpec)
    {
        this.dayGridSpec = dayGridSpec;
        this.nightGridSpec = nightGridSpec;
        this.undergroundGridSpec = undergroundGridSpec;
    }

    public GridSpec getSpec(Constants.MapType mapType)
    {
        switch (mapType)
        {
            case day:
                return dayGridSpec;
            case night:
                return nightGridSpec;
            case underground:
                return undergroundGridSpec;
            default:
                return dayGridSpec;
        }
    }
}
