/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

/**
 * Model describing Grid appearance for day/night/caves
 */
public class GridSpecs
{
    /**
     * The constant DEFAULT_DAY.
     */
    public static final GridSpec DEFAULT_DAY = new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .5f);
    /**
     * The constant DEFAULT_NIGHT.
     */
    public static final GridSpec DEFAULT_NIGHT = new GridSpec(GridSpec.Style.Squares, .5f, .5f, 1f, .3f);
    /**
     * The constant DEFAULT_UNDERGROUND.
     */
    public static final GridSpec DEFAULT_UNDERGROUND = new GridSpec(GridSpec.Style.Squares, .5f, .5f, .5f, .3f);

    private GridSpec day;
    private GridSpec night;
    private GridSpec underground;

    /**
     * Instantiates a new Grid specs.
     */
    public GridSpecs()
    {
        this(DEFAULT_DAY.clone(), DEFAULT_NIGHT.clone(), DEFAULT_UNDERGROUND.clone()); // underground
    }

    /**
     * Instantiates a new Grid specs.
     *
     * @param day         the day
     * @param night       the night
     * @param underground the underground
     */
    public GridSpecs(GridSpec day, GridSpec night, GridSpec underground)
    {
        this.day = day;
        this.night = night;
        this.underground = underground;
    }

    /**
     * Gets spec.
     *
     * @param mapType the map type
     * @return the spec
     */
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

    /**
     * Sets spec.
     *
     * @param mapType the map type
     * @param newSpec the new spec
     */
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

    /**
     * Update from.
     *
     * @param other the other
     */
    public void updateFrom(GridSpecs other)
    {
        day = other.day.clone();
        night = other.night.clone();
        underground = other.underground.clone();
    }
}
