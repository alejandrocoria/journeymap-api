/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.api.display.Context;
import journeymap.client.data.DataCache;

/**
 * Encapsulate irreducible complexity of map type-related properties.
 */
public class MapType
{
    public final Integer vSlice;
    public final Name name;
    public final int dimension;
    public final Context.MapType apiMapType;
    private final int theHashCode;
    private final String theCacheKey;


    public MapType(Name name, Integer vSlice, int dimension)
    {
        // Guarantee surface types don't use a slice
        vSlice = (name != Name.underground) ? null : vSlice;
        this.name = name;
        this.vSlice = vSlice;
        this.dimension = dimension;
        this.apiMapType = toApiContextMapType(name);
        this.theCacheKey = toCacheKey(name, vSlice, dimension);
        this.theHashCode = theCacheKey.hashCode();
    }

    public static MapType from(Name name, Integer vSlice, int dimension)
    {
        return DataCache.instance().getMapType(name, vSlice, dimension);
    }

    public static MapType from(Integer vSlice, int dimension)
    {
        return from(vSlice == null ? Name.surface : Name.underground, vSlice, dimension);
    }

    public static MapType from(Name name, EntityDTO player)
    {
        return from(name, player.chunkCoordY, player.dimension);
    }

    public static MapType day(int dimension)
    {
        return from(Name.day, null, dimension);
    }

    public static MapType day(EntityDTO player)
    {
        return from(Name.day, null, player.dimension);
    }

    public static MapType night(int dimension)
    {
        return from(Name.night, null, dimension);
    }

    public static MapType night(EntityDTO player)
    {
        return from(Name.night, null, player.dimension);
    }

    public static MapType underground(EntityDTO player)
    {
        return from(Name.underground, player.chunkCoordY, player.dimension);
    }

    public static MapType underground(Integer vSlice, int dimension)
    {
        return from(Name.underground, vSlice, dimension);
    }

    public static String toCacheKey(Name name, Integer vSlice, int dimension)
    {
        return "" + dimension + name + vSlice;
    }

    private Context.MapType toApiContextMapType(Name name)
    {
        switch (name)
        {
            case day:
                return Context.MapType.Day;
            case night:
                return Context.MapType.Night;
            case underground:
                return Context.MapType.Underground;
            default:
                return Context.MapType.Any;
        }
    }

    public String toCacheKey()
    {
        return theCacheKey;
    }

    public String toString()
    {
        return name.name();
    }

    public String name()
    {
        return name.name();
    }

    public boolean isUnderground()
    {
        return name == Name.underground;
    }

    public boolean isSurface()
    {
        return name == Name.surface;
    }

    public boolean isDay()
    {
        return name == Name.day;
    }

    public boolean isNight()
    {
        return name == Name.night;
    }

    @Override
    public int hashCode()
    {
        return theHashCode;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MapType mapType = (MapType) o;

        if (dimension != mapType.dimension)
        {
            return false;
        }
        if (name != mapType.name)
        {
            return false;
        }
        if (vSlice != null ? !vSlice.equals(mapType.vSlice) : mapType.vSlice != null)
        {
            return false;
        }

        return true;
    }

    public enum Name
    {
        day, night, underground, surface
    }

}