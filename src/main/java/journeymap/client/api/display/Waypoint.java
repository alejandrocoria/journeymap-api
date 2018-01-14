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

package journeymap.client.api.display;

import com.google.common.base.Objects;
import com.google.gson.annotations.Since;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.MapText;
import journeymap.client.api.model.WaypointBase;
import journeymap.common.api.util.CachedDimPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition for a waypoint that is offered to a player.
 * <p>
 * Setters use the Builder pattern so they can be chained.
 * <p>
 * Note that like all Displayables, simply changing this object doesn't guarantee the player will get the changes.
 * You must call {@link journeymap.client.api.IClientAPI#show(Displayable)} in order for the changes to take effect
 * in JourneyMap.
 */
@ParametersAreNonnullByDefault
public class Waypoint extends WaypointBase<Waypoint>
{
    public static final double VERSION = 1.6;

    @Since(1.4)
    protected final double version = VERSION;

    @Since(1.4)
    protected int dim;

    @Since(1.4)
    protected BlockPos pos;

    @Since(1.4)
    protected WaypointGroup group;

    @Since(1.4)
    protected boolean editable = true;

    private transient boolean persistent = true;

    private transient final CachedDimPosition dimPositions;

    private Waypoint()
    {
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
    }

    /**
     * Constructor.
     *
     * @param modId Your mod id
     * @param name  Waypoint name
     * @param dimension the dimension of the waypoint
     * @param position the position in the dimension
     */
    public Waypoint(String modId, String name, int dimension, BlockPos position)
    {
        super(modId, name);
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
        setPosition(dimension, position);
    }

    /**
     * Constructor.
     *
     * @param modId Your mod id
     * @param id    Unique id scoped to mod
     * @param name  Waypoint name
     * @param dimension the dimension of the waypoint
     * @param position the position in the dimension
     */
    public Waypoint(String modId, String id, String name, int dimension, BlockPos position)
    {
        super(modId, id, name);
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
        setPosition(dimension, position);
    }

    /**
     * Constructor to copy another Waypoint.
     * Does not copy the id.
     * @param other waypoint
     */
    public Waypoint(Waypoint other)
    {
        super(other.getModId(), other.getName());
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
        updateFrom(other);
    }

    /**
     * Copies values from other waypoint to this one, except for modid and id.
     * @param other waypoint
     * @return self
     */
    public Waypoint updateFrom(Waypoint other)
    {
        this.setName(other.getName());
        this.dim = other.dim;
        this.pos = other.pos;
        this.group = other.group;
        this.editable = other.editable;
        this.persistent = other.persistent;
        this.displayDims = new HashSet<>(other.getDisplayDimensions());
        MapImage icon = other.getIcon();
        if(icon!=null)
        {
            this.icon = new MapImage(icon);
        }
        MapText label = other.getLabel();
        if(label!=null)
        {
            this.label = new MapText(label);
        }
        return this;
    }

    /**
     * (Optional) Group or category name for the waypoint.
     * @return the group
     */
    public final WaypointGroup getGroup()
    {
        return group;
    }

    /**
     * Sets the waypoint group.
     *
     * @param group the group
     * @return this
     */
    public Waypoint setGroup(@Nullable WaypointGroup group)
    {
        this.group = group;
        return setDirty();
    }

    public final int getDimension()
    {
        return dim;
    }

    /**
     * Waypoint location.
     * @return the position
     */
    public final BlockPos getPosition()
    {
        return pos;
    }

    /**
     * Gets block position within the specified dimension
     *
     * @param targetDimension dimension
     * @return the block pos
     */
    public BlockPos getPosition(int targetDimension)
    {
        return dimPositions.getPosition(targetDimension);
    }

    /**
     * Gets block position within the specified dimension (not cached)
     *
     * @return the block pos
     */
    private BlockPos getInternalPosition(int targetDimension)
    {
        if (this.dim != targetDimension)
        {
            if (this.dim == -1)
            {
                // Convert coords to 8x horizontal scale outside of the Nether
                pos = new BlockPos(pos.getX() * 8, pos.getY(), pos.getZ() * 8);
            }
            else if (targetDimension == -1)
            {
                // Convert coords to 1/8 horizontal scale for display in the Nether
                pos = new BlockPos(pos.getX() / 8.0, pos.getY(), pos.getZ() / 8.0);
            }
        }
        return pos;
    }

    /**
     * Sets the waypoint location.
     *
     * @param dimension the dimension
     * @param position the BlockPos
     * @return this
     */
    public Waypoint setPosition(int dimension, BlockPos position)
    {
        if (position == null)
        {
            throw new IllegalArgumentException("position may not be null");
        }
        this.dim = dimension;
        this.pos = position;
        this.dimPositions.reset();
        return setDirty();
    }

    /**
     * Gets Vec3D position relative to dimension. Caches the result.
     * @param dimension the dimension
     * @return the position
     */
    public Vec3d getVec(int dimension)
    {
        return this.dimPositions.getVec(dimension);
    }

    /**
     * Gets block-centered position as a Vec3D
     * @param dimension the dimension
     * @return the position
     */
    public Vec3d getCenteredVec(int dimension)
    {
        return this.dimPositions.getCenteredVec(dimension);
    }

    /**
     * Whether or not the waypoint should be persisted (saved to file)
     * after the player disconnects from the world or changes displayDims.
     *
     * @return true if persistent
     */
    public final boolean isPersistent()
    {
        return this.persistent;
    }

    /**
     * Sets whether or not the waypoint should be persisted (saved to file)
     * after the player disconnects from the world or changes displayDims.
     *
     * @param persistent true if save to file
     * @return this
     */
    public final Waypoint setPersistent(boolean persistent)
    {
        this.persistent = persistent;
        if (!persistent)
        {
            dirty = false;
        }
        return setDirty();
    }

    /**
     * Whether the player can edit the waypoint.
     *
     * @return true if editable
     */
    public final boolean isEditable()
    {
        return editable;
    }

    /**
     * Sets whether the player can edit the waypoint.
     *
     * @param editable is editable
     * @return this
     */
    public final Waypoint setEditable(boolean editable)
    {
        this.editable = editable;
        return setDirty();
    }

    @Override
    protected WaypointGroup getDelegate()
    {
        return getGroup();
    }

    @Override
    protected boolean hasDelegate()
    {
        return group != null;
    }

    /**
     * Dimensions where this should be displayed.
     * Auto-sets to the waypoint dimension if not
     * specified.
     */
    @Override
    public Set<Integer> getDisplayDimensions()
    {
        if (displayDims == null && !hasDelegate())
        {
            setDisplayDimensions(dim);
        }
        return super.getDisplayDimensions();
    }

    @Override
    public int getDisplayOrder()
    {
        return (group != null) ? group.getDisplayOrder() : 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Waypoint))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        Waypoint that = (Waypoint) o;
        return isPersistent() == that.isPersistent() &&
                isEditable() == that.isEditable() &&
                Objects.equal(getDimension(), that.getDimension()) &&
                Objects.equal(getLabel(), that.getLabel()) &&
                Objects.equal(getName(), that.getName()) &&
                Objects.equal(getPosition(), that.getPosition()) &&
                Objects.equal(getIcon(), that.getIcon()) &&
                Arrays.equals(getDisplayDimensions().toArray(), that.getDisplayDimensions().toArray());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(super.hashCode(), getName());
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("dim", dim)
                .add("pos", pos)
                .add("group", group)
                .add("icon", icon)
                .add("label", label)
                .add("displayDims", displayDims)
                .add("editable", editable)
                .add("persistent", persistent)
                .add("dirty", dirty)
                .toString();
    }

}
