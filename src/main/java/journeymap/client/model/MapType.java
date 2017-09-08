/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.api.display.Context;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;

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
        return DataCache.INSTANCE.getMapType(name, vSlice, dimension);
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

    public static MapType topo(int dimension)
    {
        return from(Name.topo, null, dimension);
    }

    public static MapType topo(EntityDTO player)
    {
        return from(Name.topo, null, player.dimension);
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
        return String.format("%s|%s|%s", dimension, name, vSlice == null ? "_" : vSlice);
    }

    private Context.MapType toApiContextMapType(Name name)
    {
        switch (name)
        {
            case day:
                return Context.MapType.Day;
            case topo:
                return Context.MapType.Topo;
            case night:
                return Context.MapType.Night;
            case underground:
                return Context.MapType.Underground;
            default:
                return Context.MapType.Any;
        }
    }

    public static MapType fromApiContextMapType(final Context.MapType apiMapType, Integer vSlice, int dimension)
    {
        switch (apiMapType)
        {
            case Day:
            {
                return new MapType(Name.day, vSlice, dimension);
            }
            case Night:
            {
                return new MapType(Name.night, vSlice, dimension);
            }
            case Underground:
            {
                return new MapType(Name.underground, vSlice, dimension);
            }
            case Topo:
            {
                return new MapType(Name.topo, vSlice, dimension);
            }
            default:
            {
                return new MapType(Name.day, vSlice, dimension);
            }
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

    public boolean isTopo()
    {
        return name == Name.topo;
    }

    /**
     * Whether the feature of the maptype is allowed is allowed.
     *
     * @return
     */
    public boolean isAllowed()
    {
        if (isUnderground())
        {
            return FeatureManager.isAllowed(Feature.MapCaves);
        }

        return FeatureManager.isAllowed(Feature.MapSurface);
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
        day, night, underground, surface, topo
    }

}
