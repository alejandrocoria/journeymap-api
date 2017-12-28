/*
 * JourneyMap API (http://journeymap.info)
 * http://bitbucket.org/TeamJM/journeymap-api
 *
 * Copyright (c) 2011-2016 Techbrew.  All Rights Reserved.
 * The following limited rights are granted to you:
 *
 * You MAY:
 *  + Write your own code that uses the API source code in journeymap.* packages as a dependency.
 *  + Write and distribute your own code that uses, modifies, or extends the example source code in example.* packages
 *  + Fork and modify any source code for the purpose of submitting Pull Requests to the TeamJM/journeymap-api repository.
 *    Submitting new or modified code to the repository means that you are granting Techbrew all rights to the submitted code.
 *
 * You MAY NOT:
 *  - Distribute source code or classes (whether modified or not) from journeymap.* packages.
 *  - Submit any code to the TeamJM/journeymap-api repository with a different license than this one.
 *  - Use code or artifacts from the repository in any way not explicitly granted by this license.
 *
 */

package journeymap.client.api.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gson.annotations.Since;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.display.IWaypointDisplay;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Internal use only.  Mods should not extend this class.
 */
public abstract class WaypointBase<T extends WaypointBase> extends Displayable implements IWaypointDisplay
{
    @Since(1.4)
    protected String name;

    @Since(1.4)
    protected MapImage icon;

    @Since(1.6)
    protected MapText label;

    @Since(1.4)
    protected HashSet<Integer> displayDims;

    @Since(1.4)
    protected transient boolean dirty;

    /**
     * Empty constructor for GSON
     */
    protected WaypointBase()
    {
    }

    /**
     * Constructor.
     *
     * @param modId Your mod id
     * @param name  Display name
     */
    protected WaypointBase(String modId, String name)
    {
        super(modId);
        setName(name);
    }

    /**
     * Constructor.
     *
     * @param modId Your mod id
     * @param id    Unique id scoped to mod
     * @param name  Display name
     */
    protected WaypointBase(String modId, String id, String name)
    {
        super(modId, id);
        setName(name);
    }

    /**
     * Gets a delegate for this object, if one exists.
     *
     * @return delegate or null
     */
    protected abstract IWaypointDisplay getDelegate();

    /**
     * Whether this has a delegate.
     *
     * @return true if delegate exists.
     */
    protected abstract boolean hasDelegate();

    /**
     * Waypoint name.
     * @return name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     * @return this
     */
    public final T setName(String name)
    {
        if (Strings.isNullOrEmpty(name))
        {
            throw new IllegalArgumentException("name may not be blank");
        }
        this.name = name;
        return setDirty();
    }

    /**
     * Gets the text specifications for the waypoint label.
     *
     * @return rgb int
     */
    public final MapText getLabel()
    {
        if (label == null)
        {
            if(hasDelegate())
            {
                return getDelegate().getLabel();
            }
        }
        return label;
    }

    /**
     * Gets the icon color or returns default
     * @param defaultRgb default color
     * @return icon color if icon specified, defaultRgb otherwise.
     */
    public final int getOrDefaultIconColor(int defaultRgb)
    {
        MapImage icon = getIcon();
        return icon==null ? defaultRgb : icon.getColor();
    }

    /**
     * Sets the waypoint icon color
     * @param rgb int
     * @return self
     */
    public final T setIconColor(int rgb)
    {
        MapImage mapImage = getIcon();
        if(mapImage==null)
        {
            mapImage = new MapImage();
            setIcon(mapImage);
        }
        mapImage.setColor(rgb);
        return setDirty();
    }

    /**
     * Gets the label color or returns default
     * @param defaultRgb default color
     * @return label color if label specified, defaultRgb otherwise.
     */
    public final int getOrDefaultLabelColor(int defaultRgb)
    {
        MapText label = getLabel();
        return label==null ? defaultRgb : label.getColor();
    }

    /**
     * Sets the waypoint label color
     * @param rgb int
     * @return self
     */
    public final T setLabelColor(int rgb)
    {
        MapText mapText = getLabel();
        if(mapText==null)
        {
            mapText = new MapText();
            setLabel(mapText);
        }
        mapText.setColor(rgb);
        return setDirty();
    }

    /**
     * Sets basic text specifications for the waypoint label.
     * @param color font color
     * @param opacity font opacity
     * @return self
     */
    public final T setLabel(int color, float opacity)
    {
        return setLabel(new MapText().setColor(color).setOpacity(opacity));
    }

    /**
     * Text specifications for the waypoint label.
     * @param label the label
     * @return self
     */
    public final T setLabel(MapText label)
    {
        this.label = label;
        return setDirty();
    }

    /**
     * Clears label on this to ensure
     * delegate provides it on subsequent calls.
     *
     * @return this
     */
    public final T clearLabel()
    {
        this.label = null;
        return setDirty();
    }

    /**
     * Dimensions where this should be displayed. Altering the result
     * will have no affect on the waypoint.
     * @return a set, possibly empty.
     */
    public Set<Integer> getDisplayDimensions()
    {
        if (displayDims == null)
        {
            if(hasDelegate())
            {
                return getDelegate().getDisplayDimensions();
            }
            else
            {
                return Collections.emptySet();
            }
        }
        return new HashSet<>(displayDims);
    }

    /**
     * Sets the dimensions in which this waypoint should be displayed.
     *
     * @param dimensions the dimensions
     * @return this
     */
    public final T setDisplayDimensions(Integer... dimensions)
    {
        return setDisplayDimensions(Arrays.asList(dimensions));
    }

    /**
     * Sets the dimensions in which this waypoint should be displayed.
     *
     * @param dimensions the dimensions
     * @return this
     */
    public final T setDisplayDimensions(Collection<Integer> dimensions)
    {
        HashSet<Integer> temp = null;
        if(dimensions!=null && dimensions.size()>0)
        {
            temp = new HashSet<>(dimensions.size());
            dimensions.stream().filter(java.util.Objects::nonNull).forEach(temp::add);
            if(temp.size()==0)
            {
                temp = null;
            }

        }
        this.displayDims = temp;
        return setDirty();
    }

    /**
     * Clears displayDimensions on this to ensure
     * delegate provides them on subsequent calls.
     *
     * @return this
     */
    public final T clearDisplayDimensions()
    {
        this.displayDims = null;
        return setDirty();
    }

    /**
     * Sets whether to display in a given dimension.
     *
     * @param dimension dim id
     * @param displayed true to display
     */
    public void setDisplayed(int dimension, boolean displayed)
    {
        Set<Integer> dimSet = getDisplayDimensions();
        if (displayed && dimSet.add(dimension))
        {
            setDisplayDimensions(dimSet);
        }
        else if (!displayed && dimSet.remove(dimension))
        {
            setDisplayDimensions(dimSet);
        }
    }

    /**
     * Whether the waypoint is shown in the dimension.
     *
     * @param dimension dim id
     * @return true if dim id is in getDisplayDimensions()
     */
    public final boolean isDisplayed(int dimension)
    {
        return getDisplayDimensions().contains(dimension);
    }

    /**
     * Icon specification for waypoint.
     *
     * @return spec
     */
    public MapImage getIcon()
    {
        if (icon == null && hasDelegate())
        {
            return getDelegate().getIcon();
        }
        return icon;
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon
     * @return this
     */
    public final T setIcon(@Nullable MapImage icon)
    {
        this.icon = icon;
        return setDirty();
    }

    /**
     * Clears icon on this to ensure
     * delegate provides it on subsequent calls.
     *
     * @return this
     */
    public final T clearIcon()
    {
        this.icon = null;
        return setDirty();
    }

    /**
     * Whether needs to be saved.
     *
     * @return is dirty
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets dirty.
     *
     * @param dirty the dirty
     * @return the dirty
     */
    public T setDirty(boolean dirty)
    {
        this.dirty = dirty;
        return (T) this;
    }

    /**
     * Set state as needing to be saved.
     *
     * @return the dirty
     */
    public T setDirty()
    {
        return setDirty(true);
    }

    /**
     * Whether this has an icon set.  Returns
     * false if not, even if delegate exists
     * and would provide an icon.
     *
     * @return true if set
     */
    public boolean hasIcon()
    {
        return icon != null;
    }

    /**
     * Whether this has a color set.  Returns
     * false if not, even if delegate exists
     * and would provide a color.
     *
     * @return true if set
     */
    public boolean hasColor()
    {
        return getLabel() != null;
    }

    /**
     * Whether this has a background color set.  Returns
     * false if not, even if delegate exists
     * and would provide a background color.
     *
     * @return true if set
     */
    public boolean hasBackgroundColor()
    {
        return getLabel() != null;
    }

    /**
     * Whether this has display dimensions set.  Returns
     * false if not, even if delegate exists
     * and would provide a background color.
     *
     * @return true if set
     */
    public boolean hasDisplayDimensions()
    {
        return displayDims != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof WaypointBase))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        WaypointBase<?> that = (WaypointBase<?>) o;
        return Objects.equal(getGuid(), that.getGuid()) &&
                Objects.equal(getIcon(), that.getIcon()) &&
                Objects.equal(getLabel(), that.getLabel()) &&
                Arrays.equals(getDisplayDimensions().toArray(), that.getDisplayDimensions().toArray());
    }
}
