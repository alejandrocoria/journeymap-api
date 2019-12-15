/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.waypoint.WaypointGroupStore;
import journeymap.client.waypoint.WaypointParser;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.util.Strings;

import java.awt.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Generic waypoint data holder
 */
public class Waypoint implements Serializable
{
    /**
     * The constant VERSION.
     */
    public static final int VERSION = 3;
    /**
     * The constant GSON.
     */
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).create();

    /**
     * The constant ICON_NORMAL.
     */
    protected static final String ICON_NORMAL = "waypoint-normal.png";
    /**
     * The constant ICON_DEATH.
     */
    protected static final String ICON_DEATH = "waypoint-death.png";

    /**
     * The Id.
     */
    @Since(1)
    protected String id;

    /**
     * The Name.
     */
    @Since(1)
    protected String name;

    /**
     * The Group name.
     */
    @Since(3)
    protected String groupName;

    /**
     * The Display id.
     */
    @Since(2)
    protected String displayId;

    /**
     * The Icon.
     */
    @Since(1)
    protected String icon;

    /**
     * The X.
     */
    @Since(1)
    protected int x;

    /**
     * The Y.
     */
    @Since(1)
    protected int y;

    /**
     * The Z.
     */
    @Since(1)
    protected int z;

    /**
     * The R.
     */
    @Since(1)
    protected int r;

    /**
     * The G.
     */
    @Since(1)
    protected int g;

    /**
     * The B.
     */
    @Since(1)
    protected int b;

    /**
     * The Enable.
     */
    @Since(1)
    protected boolean enable;

    /**
     * The Type.
     */
    @Since(1)
    protected Type type;

    /**
     * The Origin.
     */
    @Since(1)
    protected String origin;

    /**
     * The Dimensions.
     */
    @Since(1)
    protected TreeSet<Integer> dimensions;

    /**
     * The Persistent.
     */
    @Since(2)
    protected boolean persistent;

    /**
     * The Group.
     */
    protected transient WaypointGroup group;
    /**
     * The Dirty.
     */
    protected transient boolean dirty;
    /**
     * Minecraft client
     */
    protected transient Minecraft mc = FMLClientHandler.instance().getClient();

    /**
     * Default constructor Required by GSON
     */
    public Waypoint()
    {
    }

    /**
     * Instantiates a new Waypoint.
     *
     * @param original the original
     */
    public Waypoint(Waypoint original)
    {
        this(original.name, original.x, original.y, original.z, original.enable, original.r, original.g, original.b, original.type, original.origin, original.dimensions.first(), original.dimensions);
        this.x = original.x;
        this.y = original.y;
        this.z = original.z;
    }

    /**
     * Instantiates a new Waypoint.
     *
     * @param modWaypoint the mod waypoint
     */
    public Waypoint(journeymap.client.api.display.Waypoint modWaypoint)
    {
        this(modWaypoint.getName(), modWaypoint.getPosition(), modWaypoint.getColor() == null ? Color.WHITE : new Color(modWaypoint.getColor()), Type.Normal, modWaypoint.getDimension());

        int[] prim = modWaypoint.getDisplayDimensions();
        ArrayList<Integer> dims = new ArrayList<Integer>(prim.length);
        for (int aPrim : prim)
        {
            dims.add(aPrim);
        }
        this.setDimensions(dims);
        this.setOrigin(modWaypoint.getModId());
        this.displayId = modWaypoint.getId();
        this.setPersistent(modWaypoint.isPersistent());
        if (modWaypoint.getGroup() != null)
        {
            this.setGroupName(modWaypoint.getGroup().getName());
        }
    }

    /**
     * Instantiates a new Waypoint.
     *
     * @param name             the name
     * @param pos              the pos
     * @param color            the color
     * @param type             the type
     * @param currentDimension the current dimension
     */
    public Waypoint(String name, BlockPos pos, Color color, Type type, Integer currentDimension)
    {
        this(name, pos.getX(), pos.getY(), pos.getZ(), true, color.getRed(), color.getGreen(), color.getBlue(), type, Journeymap.MOD_ID, currentDimension, Arrays.asList(currentDimension));
    }

    /**
     * Main constructor.
     *
     * @param name             the name
     * @param x                the x
     * @param y                the y
     * @param z                the z
     * @param enable           the enable
     * @param red              the red
     * @param green            the green
     * @param blue             the blue
     * @param type             the type
     * @param origin           the origin
     * @param currentDimension the current dimension
     * @param dimensions       the dimensions
     */
    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, Type type, String origin, Integer currentDimension, Collection<Integer> dimensions)
    {
        if (name == null)
        {
            name = createName(x, z);
        }
        if (dimensions == null || dimensions.size() == 0)
        {
            dimensions = new TreeSet<Integer>();
            dimensions.add(FMLClientHandler.instance().getClient().player.world.provider.getDimension());
        }
        this.dimensions = new TreeSet<Integer>(dimensions);
        this.dimensions.add(currentDimension);

        this.name = name;
        setLocation(x, y, z, currentDimension);

        this.r = red;
        this.g = green;
        this.b = blue;
        this.enable = enable;
        this.type = type;
        this.origin = origin;
        this.persistent = true;

        switch (type)
        {
            case Normal:
            {
                icon = ICON_NORMAL;
                break;
            }
            case Death:
            {
                icon = ICON_DEATH;
                break;
            }
        }
    }

    /**
     * Of waypoint.
     *
     * @param player the player
     * @return the waypoint
     */
    public static Waypoint of(EntityPlayer player)
    {
        BlockPos blockPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
        return at(blockPos, Type.Normal, FMLClientHandler.instance().getClient().player.world.provider.getDimension());
    }

    /**
     * At waypoint.
     *
     * @param blockPos  the block pos
     * @param type      the type
     * @param dimension the dimension
     * @return the waypoint
     */
    public static Waypoint at(BlockPos blockPos, Type type, int dimension)
    {
        String name;
        if (type == Type.Death)
        {
            Date now = new Date();
            WaypointProperties properties = Journeymap.getClient().getWaypointProperties();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(properties.timeFormat.get() + " " + properties.dateFormat.get());
            String timeDate = simpleDateFormat.format(now);
            name = String.format("%s %s", Constants.getString("jm.waypoint.deathpoint"), timeDate);
        }
        else
        {
            name = createName(blockPos.getX(), blockPos.getZ());
        }
        Waypoint waypoint = new Waypoint(name, blockPos, Color.white, type, dimension);
        waypoint.setRandomColor();
        return waypoint;
    }

    private static String createName(int x, int z)
    {
        return String.format("%s, %s", x, z);
    }

    /**
     * From string waypoint.
     *
     * @param json the json
     * @return the waypoint
     */
    public static Waypoint fromString(String json)
    {
        return GSON.fromJson(json, Waypoint.class);
    }

    /**
     * Sets location.
     *
     * @param x                the x
     * @param y                the y
     * @param z                the z
     * @param currentDimension the current dimension
     * @return the location
     */
    public Waypoint setLocation(int x, int y, int z, int currentDimension)
    {
        this.x = (currentDimension == -1) ? x * 8 : x;
        this.y = y;
        this.z = (currentDimension == -1) ? z * 8 : z;
        updateId();
        return setDirty();
    }

    /**
     * Update id string.
     *
     * @return the string
     */
    public String updateId()
    {
        String oldId = this.id;
        this.id = String.format("%s_%s,%s,%s", this.name, this.x, this.y, this.z);
        return oldId;
    }

    /**
     * Is death point boolean.
     *
     * @return the boolean
     */
    public boolean isDeathPoint()
    {
        return this.type == Type.Death;
    }

    /**
     * Gets texture.
     *
     * @return the texture
     */
    public TextureImpl getTexture()
    {
        return isDeathPoint() ? TextureCache.getTexture(TextureCache.Deathpoint) : TextureCache.getTexture(TextureCache.Waypoint);
    }

    /**
     * Gets chunk coord int pair.
     *
     * @return the chunk coord int pair
     */
    public ChunkPos getChunkCoordIntPair()
    {
        return new ChunkPos(x >> 4, z >> 4);
    }

    /**
     * Sets group.
     *
     * @param group the group
     * @return the group
     */
    public Waypoint setGroup(WaypointGroup group)
    {
        setOrigin(group.getOrigin());
        this.groupName = group.getName();
        this.group = group;
        return setDirty();
    }

    /**
     * Sets group name.
     *
     * @param groupName the group name
     * @return the group name
     */
    public Waypoint setGroupName(String groupName)
    {
        WaypointGroup group = WaypointGroupStore.INSTANCE.get(origin, groupName);
        setGroup(group);
        return this;
    }

    /**
     * Gets group.
     *
     * @return the group
     */
    public WaypointGroup getGroup()
    {
        if (group == null)
        {
            if (Strings.isEmpty(origin) || Strings.isEmpty(groupName))
            {
                setGroup(WaypointGroup.DEFAULT);
            }
            else
            {
                setGroup(WaypointGroupStore.INSTANCE.get(origin, groupName));
            }
        }

        return this.group;
    }

    /**
     * Sets random color.
     *
     * @return the random color
     */
    public Waypoint setRandomColor()
    {
        return setColor(RGB.randomColor());
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public Integer getColor()
    {
        return RGB.toInteger(r, g, b);
    }

    /**
     * Sets color.
     *
     * @param color the color
     * @return the color
     */
    public Waypoint setColor(Integer color)
    {
        int c[] = RGB.ints(color);
        this.r = c[0];
        this.g = c[1];
        this.b = c[2];
        return setDirty();
    }

    /**
     * Gets safe color.
     *
     * @return the safe color
     */
    public Integer getSafeColor()
    {
        if (r + g + b >= 100)
        {
            return getColor();
        }
        return RGB.DARK_GRAY_RGB;
    }

    /**
     * Gets dimensions.
     *
     * @return the dimensions
     */
    public Collection<Integer> getDimensions()
    {
        return this.dimensions;
    }

    /**
     * Sets dimensions.
     *
     * @param dims the dims
     * @return the dimensions
     */
    public Waypoint setDimensions(Collection<Integer> dims)
    {
        this.dimensions = new TreeSet<Integer>(dims);
        return setDirty();
    }

    /**
     * Is teleport ready boolean.
     *
     * @return the boolean
     */
    public boolean isTeleportReady()
    {
        return y >= 0 && this.isInPlayerDimension();
    }

    /**
     * Is in player dimension boolean.
     *
     * @return the boolean
     */
    public boolean isInPlayerDimension()
    {
        return dimensions.contains(FMLClientHandler.instance().getClient().player.world.provider.getDimension());
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId()
    {
        return (displayId != null) ? getGuid() : id;
    }

    /**
     * Gets guid.
     *
     * @return the guid
     */
    public String getGuid()
    {
        return origin + ":" + displayId;
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
    public Waypoint setName(String name)
    {
        this.name = name;
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
    public Waypoint setIcon(String icon)
    {
        this.icon = icon;
        return setDirty();
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public int getX()
    {
        if (mc != null && mc.player != null && mc.player.dimension == -1)
        {
            return x / 8;
        }
        return x;
    }

    /**
     * Gets block centered x.
     *
     * @return the block centered x
     */
    public double getBlockCenteredX()
    {
        return getX() + .5d;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public int getY()
    {
        return y;
    }

    /**
     * Gets block centered y.
     *
     * @return the block centered y
     */
    public double getBlockCenteredY()
    {
        return getY() + .5d;
    }

    /**
     * Gets z.
     *
     * @return the z
     */
    public int getZ()
    {
        if (mc != null && mc.player != null && mc.player.dimension == -1)
        {
            return z / 8;
        }
        return z;
    }

    /**
     * Gets block centered z.
     *
     * @return the block centered z
     */
    public double getBlockCenteredZ()
    {
        return getZ() + .5d;
    }

    /**
     * Gets position.
     *
     * @return the position
     */
    public Vec3d getPosition()
    {
        return new Vec3d(getBlockCenteredX(), getBlockCenteredY(), getBlockCenteredZ());
    }

    /**
     * Gets block pos.
     *
     * @return the block pos
     */
    public BlockPos getBlockPos()
    {
        return new BlockPos(getX(), getY(), getZ());
    }

    /**
     * Gets r.
     *
     * @return the r
     */
    public int getR()
    {
        return r;
    }

    /**
     * Sets r.
     *
     * @param r the r
     * @return the r
     */
    public Waypoint setR(int r)
    {
        this.r = r;
        return setDirty();
    }

    /**
     * Gets g.
     *
     * @return the g
     */
    public int getG()
    {
        return g;
    }

    /**
     * Sets g.
     *
     * @param g the g
     * @return the g
     */
    public Waypoint setG(int g)
    {
        this.g = g;
        return setDirty();
    }

    /**
     * Gets b.
     *
     * @return the b
     */
    public int getB()
    {
        return b;
    }

    /**
     * Sets b.
     *
     * @param b the b
     * @return the b
     */
    public Waypoint setB(int b)
    {
        this.b = b;
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
    public Waypoint setEnable(boolean enable)
    {
        if (enable != this.enable)
        {
            this.enable = enable;
            setDirty();
        }
        return this;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     * @return the type
     */
    public Waypoint setType(Type type)
    {
        this.type = type;
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
    public Waypoint setOrigin(String origin)
    {
        this.origin = origin;
        return setDirty();
    }

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName()
    {
        String fileName = id.replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
        if (fileName.equals(WaypointGroupStore.FILENAME))
        {
            fileName = "_" + fileName;
        }
        return fileName;
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
    public Waypoint setDirty()
    {
        return setDirty(true);
    }

    /**
     * Sets dirty.
     *
     * @param dirty the dirty
     * @return the dirty
     */
    public Waypoint setDirty(boolean dirty)
    {
        if (isPersistent())
        {
            this.dirty = dirty;
        }
        return this;
    }

    /**
     * Is persistent boolean.
     *
     * @return the boolean
     */
    public boolean isPersistent()
    {
        return persistent;
    }

    /**
     * Sets persistent.
     *
     * @param persistent the persistent
     * @return the persistent
     */
    public Waypoint setPersistent(boolean persistent)
    {
        this.persistent = persistent;
        this.dirty = persistent;
        return this;
    }

    /**
     * To chat string string.
     *
     * @return the string
     */
    public String toChatString()
    {
        boolean useName = !(getName().equals(String.format("%s, %s", getX(), getZ())));
        return toChatString(useName);
    }

    /**
     * To chat string string.
     *
     * @param useName the use name
     * @return the string
     */
    public String toChatString(boolean useName)
    {
        boolean useDim = dimensions.first() != 0;

        List<String> parts = new ArrayList<String>();
        List<Object> args = new ArrayList<Object>();
        if (useName)
        {
            parts.add("name:\"%s\"");
            args.add(getName().replaceAll("\"", " "));
        }

        parts.add("x:%s, y:%s, z:%s");
        args.add(getX());
        args.add(getY());
        args.add(getZ());

        if (useDim)
        {
            parts.add("dim:%s");
            args.add(dimensions.first());
        }

        String format = "[" + Joiner.on(", ").join(parts) + "]";
        String result = String.format(format, args.toArray());
        if (WaypointParser.parse(result) == null)
        {
            Journeymap.getLogger().warn("Couldn't produce parsable chat string from Waypoint: " + this);
            if (useName)
            {
                return toChatString(false);
            }
        }
        return result;
    }

    @Override
    public String toString()
    {
        return GSON.toJson(this);
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

        Waypoint waypoint = (Waypoint) o;

        if (b != waypoint.b)
        {
            return false;
        }
        if (enable != waypoint.enable)
        {
            return false;
        }
        if (g != waypoint.g)
        {
            return false;
        }
        if (r != waypoint.r)
        {
            return false;
        }
        if (x != waypoint.x)
        {
            return false;
        }
        if (y != waypoint.y)
        {
            return false;
        }
        if (z != waypoint.z)
        {
            return false;
        }
        if (!dimensions.equals(waypoint.dimensions))
        {
            return false;
        }
        if (!icon.equals(waypoint.icon))
        {
            return false;
        }
        if (!id.equals(waypoint.id))
        {
            return false;
        }
        if (!name.equals(waypoint.name))
        {
            return false;
        }
        if (origin != waypoint.origin)
        {
            return false;
        }
        if (type != waypoint.type)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * The enum Type.
     */
    public enum Type
    {
        /**
         * Normal type.
         */
        Normal,
        /**
         * Death type.
         */
        Death
    }
}
