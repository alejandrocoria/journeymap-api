package journeymap.client.model;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.cache.*;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.cartography.RGB;
import journeymap.client.io.FileHandler;
import journeymap.client.waypoint.WaypointGroupStore;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Group of waypoints
 * TODO: If a waypoint is created with a group that doesn't exist, create it
 */
@ParametersAreNonnullByDefault
public class WaypointGroup implements Comparable<WaypointGroup>
{
    public static final WaypointGroup DEFAULT = new WaypointGroup(Journeymap.MOD_ID, Constants.getString("jm.config.category.waypoint"));
    public static final int VERSION = 3;
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).create();

    @Since(1)
    protected String name;

    @Since(1)
    protected String origin;

    @Since(1)
    protected String icon;

    @Since(1)
    protected String color;

    @Since(1)
    protected boolean enable;

    @Since(1)
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WaypointGroup group = (WaypointGroup) o;

        if (!name.equals(group.name)) return false;
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
