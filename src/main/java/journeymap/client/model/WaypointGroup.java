/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.cartography.RGB;
import journeymap.client.waypoint.WaypointGroupStore;
import journeymap.common.Journeymap;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Group of waypoints
 * TODO: If a waypoint is created with a group that doesn't exist, create it
 */
@ParametersAreNonnullByDefault
public class WaypointGroup implements Comparable<WaypointGroup>
{
    public static final WaypointGroup DEFAULT = new WaypointGroup(Journeymap.MOD_ID, Constants.getString("jm.config.category.waypoint")).setEnable(true);
    public static final double VERSION = 5.2;
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).create();

    @Since(5.2)
    protected String name;

    @Since(5.2)
    protected String origin;

    @Since(5.2)
    protected String icon;

    @Since(5.2)
    protected String color;

    @Since(5.2)
    protected boolean enable;

    @Since(5.2)
    protected int order;

    protected transient boolean dirty;
    protected transient Integer colorInt;

    public WaypointGroup(String origin, String name)
    {
        setOrigin(origin).setName(name);
    }

    public String getName()
    {
        return name;
    }

    public WaypointGroup setName(String name)
    {
        this.name = name;
        return setDirty();
    }

    public String getOrigin()
    {
        return origin;
    }

    public WaypointGroup setOrigin(String origin)
    {
        this.origin = origin;
        return setDirty();
    }

    public String getIcon()
    {
        return icon;
    }

    public WaypointGroup setIcon(String icon)
    {
        this.icon = icon;
        return setDirty();
    }

    public int getColor()
    {
        if (colorInt == null)
        {
            if (color == null)
            {
                color = RGB.toHexString(RGB.randomColor());
            }
            colorInt = RGB.hexToInt(color);
        }
        return colorInt;
    }

    public WaypointGroup setColor(String color)
    {
        this.colorInt = RGB.hexToInt(color);
        this.color = RGB.toHexString(this.colorInt);
        return setDirty();
    }

    public WaypointGroup setColor(int color)
    {
        this.color = RGB.toHexString(color);
        this.colorInt = color;
        return setDirty();
    }

    public boolean isEnable()
    {
        return enable;
    }

    public WaypointGroup setEnable(boolean enable)
    {
        this.enable = enable;
        return setDirty();
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public WaypointGroup setDirty()
    {
        return setDirty(true);
    }

    public WaypointGroup setDirty(boolean dirty)
    {
        this.dirty = dirty;
        return this;
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

        WaypointGroup group = (WaypointGroup) o;

        if (!name.equals(group.name))
        {
            return false;
        }
        return origin.equals(group.origin);

    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + origin.hashCode();
        return result;
    }

    @Override
    public int compareTo(WaypointGroup o)
    {
        int result = Integer.compare(order, o.order);
        if (result == 0)
        {
            result = name.compareTo(o.name);
        }
        return result;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("origin", origin)
                .toString();
    }

    public String getKey()
    {
        return String.format("%s:%s", this.origin, this.name);
    }

    public static WaypointGroup getNamedGroup(final String origin, final String groupName)
    {
        return WaypointGroupStore.INSTANCE.get(origin, groupName);
    }


}
