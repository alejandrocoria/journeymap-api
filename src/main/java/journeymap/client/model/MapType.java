/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
    /**
     * The V slice.
     */
    public final Integer vSlice;
    /**
     * The Name.
     */
    public final Name name;
    /**
     * The Dimension.
     */
    public final int dimension;
    /**
     * The Api map type.
     */
    public final Context.MapType apiMapType;
    private final int theHashCode;
    private final String theCacheKey;


    /**
     * Instantiates a new Map type.
     *
     * @param name      the name
     * @param vSlice    the v slice
     * @param dimension the dimension
     */
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

    /**
     * From map type.
     *
     * @param name      the name
     * @param vSlice    the v slice
     * @param dimension the dimension
     * @return the map type
     */
    public static MapType from(Name name, Integer vSlice, int dimension)
    {
        return DataCache.INSTANCE.getMapType(name, vSlice, dimension);
    }

    /**
     * From map type.
     *
     * @param vSlice    the v slice
     * @param dimension the dimension
     * @return the map type
     */
    public static MapType from(Integer vSlice, int dimension)
    {
        return from(vSlice == null ? Name.surface : Name.underground, vSlice, dimension);
    }

    /**
     * From map type.
     *
     * @param name   the name
     * @param player the player
     * @return the map type
     */
    public static MapType from(Name name, EntityDTO player)
    {
        return from(name, player.chunkCoordY, player.dimension);
    }

    /**
     * Day map type.
     *
     * @param dimension the dimension
     * @return the map type
     */
    public static MapType day(int dimension)
    {
        return from(Name.day, null, dimension);
    }

    /**
     * Day map type.
     *
     * @param player the player
     * @return the map type
     */
    public static MapType day(EntityDTO player)
    {
        return from(Name.day, null, player.dimension);
    }

    /**
     * Night map type.
     *
     * @param dimension the dimension
     * @return the map type
     */
    public static MapType night(int dimension)
    {
        return from(Name.night, null, dimension);
    }

    /**
     * Night map type.
     *
     * @param player the player
     * @return the map type
     */
    public static MapType night(EntityDTO player)
    {
        return from(Name.night, null, player.dimension);
    }

    /**
     * Topo map type.
     *
     * @param dimension the dimension
     * @return the map type
     */
    public static MapType topo(int dimension)
    {
        return from(Name.topo, null, dimension);
    }

    /**
     * Topo map type.
     *
     * @param player the player
     * @return the map type
     */
    public static MapType topo(EntityDTO player)
    {
        return from(Name.topo, null, player.dimension);
    }

    /**
     * Underground map type.
     *
     * @param player the player
     * @return the map type
     */
    public static MapType underground(EntityDTO player)
    {
        return from(Name.underground, player.chunkCoordY, player.dimension);
    }

    /**
     * Underground map type.
     *
     * @param vSlice    the v slice
     * @param dimension the dimension
     * @return the map type
     */
    public static MapType underground(Integer vSlice, int dimension)
    {
        return from(Name.underground, vSlice, dimension);
    }

    public static MapType none()
    {
        return from(Name.none, 0, 0);
    }


    /**
     * To cache key string.
     *
     * @param name      the name
     * @param vSlice    the v slice
     * @param dimension the dimension
     * @return the string
     */
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

    /**
     * From api context map type map type.
     *
     * @param apiMapType the api map type
     * @param vSlice     the v slice
     * @param dimension  the dimension
     * @return the map type
     */
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

    /**
     * To cache key string.
     *
     * @return the string
     */
    public String toCacheKey()
    {
        return theCacheKey;
    }

    @Override
    public String toString()
    {
        return theCacheKey;
    }

    /**
     * Name string.
     *
     * @return the string
     */
    public String name()
    {
        return name.name();
    }

    /**
     * Is underground boolean.
     *
     * @return the boolean
     */
    public boolean isUnderground()
    {
        return name == Name.underground;
    }

    /**
     * Is surface boolean.
     *
     * @return the boolean
     */
    public boolean isSurface()
    {
        return name == Name.surface;
    }

    /**
     * Is day boolean.
     *
     * @return the boolean
     */
    public boolean isDay()
    {
        return name == Name.day;
    }

    /**
     * Is night boolean.
     *
     * @return the boolean
     */
    public boolean isNight()
    {
        return name == Name.night;
    }

    /**
     * Is topo boolean.
     *
     * @return the boolean
     */
    public boolean isTopo()
    {
        return name == Name.topo;
    }

    /**
     * Whether the maptype is day or night.
     * @return
     */
    public boolean isDayOrNight()
    {
        return name == Name.day || name == Name.night;
    }

    /**
     * Whether the feature of the maptype is allowed is allowed.
     * @return
     */
    public boolean isAllowed()
    {
        if(isUnderground())
        {
            return FeatureManager.isAllowed(Feature.MapCaves);
        }
        else if (isTopo())
        {
            return FeatureManager.isAllowed(Feature.MapTopo);
        }
        else if (isDayOrNight() || isSurface())
        {
            return FeatureManager.isAllowed(Feature.MapSurface);
        }
        else if (name == Name.none)
        {
            return true;
        }
        else
        {
            return false;
        }
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

    /**
     * The enum Name.
     */
    public enum Name
    {
        day, night, underground, surface, topo, none
    }

}
