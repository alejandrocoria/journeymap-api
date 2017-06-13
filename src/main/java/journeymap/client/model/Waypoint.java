/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.cartography.RGB;
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
import java.text.DateFormat;
import java.util.*;
import java.util.List;

/**
 * Generic waypoint data holder
 */
public class Waypoint implements Serializable
{
    public static final int VERSION = 3;
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).create();

    protected static final String ICON_NORMAL = "waypoint-normal.png";
    protected static final String ICON_DEATH = "waypoint-death.png";

    @Since(1)
    protected String id;

    @Since(1)
    protected String name;

    @Since(3)
    protected String groupName;

    @Since(2)
    protected String displayId;

    @Since(1)
    protected String icon;

    @Since(1)
    protected int x;

    @Since(1)
    protected int y;

    @Since(1)
    protected int z;

    @Since(1)
    protected int r;

    @Since(1)
    protected int g;

    @Since(1)
    protected int b;

    @Since(1)
    protected boolean enable;

    @Since(1)
    protected Type type;

    @Since(1)
    protected String origin;

    @Since(1)
    protected TreeSet<Integer> dimensions;

    @Since(2)
    protected boolean persistent;

    protected transient WaypointGroup group;
    protected transient boolean dirty;
    protected transient Minecraft mc = FMLClientHandler.instance().getClient();

    /**
     * Default constructor Required by GSON
     */
    public Waypoint()
    {
    }

    public Waypoint(Waypoint original)
    {
        this(original.name, original.x, original.y, original.z, original.enable, original.r, original.g, original.b, original.type, original.origin, original.dimensions.first(), original.dimensions);
        this.x = original.x;
        this.y = original.y;
        this.z = original.z;
    }

    public Waypoint(ModWaypoint modWaypoint)
    {
        this(modWaypoint.getWaypointName(), modWaypoint.getPoint(), new Color(modWaypoint.getColor()), Type.Normal, modWaypoint.getDimensions()[0]);

        int[] prim = modWaypoint.getDimensions();
        ArrayList<Integer> dims = new ArrayList<Integer>(prim.length);
        for (int aPrim : prim)
        {
            dims.add(aPrim);
        }
        this.setDimensions(dims);
        this.setOrigin(modWaypoint.getModId());
        this.displayId = modWaypoint.getDisplayId();
        this.setPersistent(modWaypoint.isPersistent());
        this.setGroupName(modWaypoint.getWaypointGroupName());
    }

    public Waypoint(String name, BlockPos pos, Color color, Type type, Integer currentDimension)
    {
        this(name, pos.getX(), pos.getY(), pos.getZ(), true, color.getRed(), color.getGreen(), color.getBlue(), type, Journeymap.MOD_ID, currentDimension, Arrays.asList(currentDimension));
    }

    /**
     * Main constructor.
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

    public static Waypoint of(EntityPlayer player)
    {
        BlockPos blockPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
        return at(blockPos, Type.Normal, FMLClientHandler.instance().getClient().player.world.provider.getDimension());
    }

    public static Waypoint at(BlockPos blockPos, Type type, int dimension)
    {
        String name;
        if (type == Type.Death)
        {
            Date now = new Date();
            name = String.format("%s %s %s", Constants.getString("jm.waypoint.deathpoint"),
                    DateFormat.getTimeInstance().format(now),
                    DateFormat.getDateInstance(DateFormat.SHORT).format(now));
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

    public static Waypoint fromString(String json)
    {
        return GSON.fromJson(json, Waypoint.class);
    }

    public Waypoint setLocation(int x, int y, int z, int currentDimension)
    {
        this.x = (currentDimension == -1) ? x * 8 : x;
        this.y = y;
        this.z = (currentDimension == -1) ? z * 8 : z;
        updateId();
        return setDirty();
    }

    public String updateId()
    {
        String oldId = this.id;
        this.id = String.format("%s_%s,%s,%s", this.name, this.x, this.y, this.z);
        return oldId;
    }

    public boolean isDeathPoint()
    {
        return this.type == Type.Death;
    }

    public TextureImpl getTexture()
    {
        return isDeathPoint() ? TextureCache.getTexture(TextureCache.Deathpoint) : TextureCache.getTexture(TextureCache.Waypoint);
    }

    public ChunkPos getChunkCoordIntPair()
    {
        return new ChunkPos(x >> 4, z >> 4);
    }

    public Waypoint setGroup(WaypointGroup group)
    {
        setOrigin(group.getOrigin());
        this.groupName = group.getName();
        this.group = group;
        return setDirty();
    }

    public Waypoint setGroupName(String groupName)
    {
        WaypointGroup group = WaypointGroupStore.INSTANCE.get(origin, groupName);
        setGroup(group);
        return this;
    }

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

    public Waypoint setRandomColor()
    {
        return setColor(RGB.randomColor());
    }

    public Integer getColor()
    {
        return RGB.toInteger(r, g, b);
    }

    public Waypoint setColor(Integer color)
    {
        int c[] = RGB.ints(color);
        this.r = c[0];
        this.g = c[1];
        this.b = c[2];
        return setDirty();
    }

    public Integer getSafeColor()
    {
        if (r + g + b >= 100)
        {
            return getColor();
        }
        return RGB.DARK_GRAY_RGB;
    }

    public Collection<Integer> getDimensions()
    {
        return this.dimensions;
    }

    public Waypoint setDimensions(Collection<Integer> dims)
    {
        this.dimensions = new TreeSet<Integer>(dims);
        return setDirty();
    }

    public boolean isTeleportReady()
    {
        return y >= 0 && this.isInPlayerDimension();
    }

    public boolean isInPlayerDimension()
    {
        return dimensions.contains(FMLClientHandler.instance().getClient().player.world.provider.getDimension());
    }

    public String getId()
    {
        return (displayId != null) ? getGuid() : id;
    }

    public String getGuid()
    {
        return origin + ":" + displayId;
    }

    public String getName()
    {
        return name;
    }

    public Waypoint setName(String name)
    {
        this.name = name;
        return setDirty();
    }

    public String getIcon()
    {
        return icon;
    }

    public Waypoint setIcon(String icon)
    {
        this.icon = icon;
        return setDirty();
    }

    public int getX()
    {
        if (mc != null && mc.player != null && mc.player.dimension == -1)
        {
            return x / 8;
        }
        return x;
    }

    public double getBlockCenteredX()
    {
        return getX() + .5d;
    }

    public int getY()
    {
        return y;
    }

    public double getBlockCenteredY()
    {
        return getY() + .5d;
    }

    public int getZ()
    {
        if (mc != null && mc.player != null && mc.player.dimension == -1)
        {
            return z / 8;
        }
        return z;
    }

    public double getBlockCenteredZ()
    {
        return getZ() + .5d;
    }

    public Vec3d getPosition()
    {
        return new Vec3d(getBlockCenteredX(), getBlockCenteredY(), getBlockCenteredZ());
    }

    public BlockPos getBlockPos()
    {
        return new BlockPos(getX(), getY(), getZ());
    }

    public int getR()
    {
        return r;
    }

    public Waypoint setR(int r)
    {
        this.r = r;
        return setDirty();
    }

    public int getG()
    {
        return g;
    }

    public Waypoint setG(int g)
    {
        this.g = g;
        return setDirty();
    }

    public int getB()
    {
        return b;
    }

    public Waypoint setB(int b)
    {
        this.b = b;
        return setDirty();
    }

    public boolean isEnable()
    {
        return enable;
    }

    public Waypoint setEnable(boolean enable)
    {
        if (enable != this.enable)
        {
            this.enable = enable;
            setDirty();
        }
        return this;
    }

    public Type getType()
    {
        return type;
    }

    public Waypoint setType(Type type)
    {
        this.type = type;
        return setDirty();
    }

    public String getOrigin()
    {
        return origin;
    }

    public Waypoint setOrigin(String origin)
    {
        this.origin = origin;
        return setDirty();
    }

    public String getFileName()
    {
        String fileName = id.replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
        if (fileName.equals(WaypointGroupStore.FILENAME))
        {
            fileName = "_" + fileName;
        }
        return fileName;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public Waypoint setDirty()
    {
        return setDirty(true);
    }

    public Waypoint setDirty(boolean dirty)
    {
        if (isPersistent())
        {
            this.dirty = dirty;
        }
        return this;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    public Waypoint setPersistent(boolean persistent)
    {
        this.persistent = persistent;
        this.dirty = persistent;
        return this;
    }

    public String toChatString()
    {
        boolean useName = !(getName().equals(String.format("%s, %s", getX(), getZ())));
        return toChatString(useName);
    }

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

    public enum Type
    {
        Normal,
        Death
    }
}
