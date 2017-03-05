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
    /**
     * The constant DEFAULT.
     */
    public static final WaypointGroup DEFAULT = new WaypointGroup(Journeymap.MOD_ID, Constants.getString("jm.config.category.waypoint")).setEnable(true);
    /**
     * The constant VERSION.
     */
    public static final double VERSION = 5.2;
    /**
     * The constant GSON.
     */
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).create();

    /**
     * The Name.
     */
    @Since(5.2)
    protected String name;

    /**
     * The Origin.
     */
    @Since(5.2)
    protected String origin;

    /**
     * The Icon.
     */
    @Since(5.2)
    protected String icon;

    /**
     * The Color.
     */
    @Since(5.2)
    protected String color;

    /**
     * The Enable.
     */
    @Since(5.2)
    protected boolean enable;

    /**
     * The Order.
     */
    @Since(5.2)
    protected int order;

    /**
     * The Dirty.
     */
    protected transient boolean dirty;
    /**
     * The Color int.
     */
    protected transient Integer colorInt;

    /**
     * Instantiates a new Waypoint group.
     *
     * @param origin the origin
     * @param name   the name
     */
    public WaypointGroup(String origin, String name)
    {
        setOrigin(origin).setName(name);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     * @return the name
     */
    public WaypointGroup setName(String name)
    {
        this.name = name;
        return setDirty();
    }

    /**
     * Gets origin.
     *
     * @return the origin
     */
    public String getOrigin()
    {
        return origin;
    }

    /**
     * Sets origin.
     *
     * @param origin the origin
     * @return the origin
     */
    public WaypointGroup setOrigin(String origin)
    {
        this.origin = origin;
        return setDirty();
    }

    /**
     * Gets icon.
     *
     * @return the icon
     */
    public String getIcon()
    {
        return icon;
    }

    /**
     * Sets icon.
     *
     * @param icon the icon
     * @return the icon
     */
    public WaypointGroup setIcon(String icon)
    {
        this.icon = icon;
        return setDirty();
    }

    /**
     * Gets color.
     *
     * @return the color
     */
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

    /**
     * Sets color.
     *
     * @param color the color
     * @return the color
     */
    public WaypointGroup setColor(String color)
    {
        this.colorInt = RGB.hexToInt(color);
        this.color = RGB.toHexString(this.colorInt);
        return setDirty();
    }

    /**
     * Sets color.
     *
     * @param color the color
     * @return the color
     */
    public WaypointGroup setColor(int color)
    {
        this.color = RGB.toHexString(color);
        this.colorInt = color;
        return setDirty();
    }

    /**
     * Is enable boolean.
     *
     * @return the boolean
     */
    public boolean isEnable()
    {
        return enable;
    }

    /**
     * Sets enable.
     *
     * @param enable the enable
     * @return the enable
     */
    public WaypointGroup setEnable(boolean enable)
    {
        this.enable = enable;
        return setDirty();
    }

    /**
     * Is dirty boolean.
     *
     * @return the boolean
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets dirty.
     *
     * @return the dirty
     */
    public WaypointGroup setDirty()
    {
        return setDirty(true);
    }

    /**
     * Sets dirty.
     *
     * @param dirty the dirty
     * @return the dirty
     */
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

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey()
    {
        return String.format("%s:%s", this.origin, this.name);
    }

    /**
     * Gets named group.
     *
     * @param origin    the origin
     * @param groupName the group name
     * @return the named group
     */
    public static WaypointGroup getNamedGroup(final String origin, final String groupName)
    {
        return WaypointGroupStore.INSTANCE.get(origin, groupName);
    }


}
