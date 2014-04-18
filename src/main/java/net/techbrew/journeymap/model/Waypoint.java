package net.techbrew.journeymap.model;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Generic waypoint data holder
 */
public class Waypoint implements Serializable
{
    protected static final String ICON_NORMAL = "waypoint-normal.png";
    protected static final String ICON_DEATH = "waypoint-death.png";

    public enum Type
    {
        Normal,
        Death
    }

    protected String id;
    protected String name;
    protected String icon;
    protected int x;
    protected int y;
    protected int z;
    protected int r;
    protected int g;
    protected int b;
    protected boolean enable;
    protected Type type;
    protected String origin;
    protected String texture;
    protected Integer[] dimensions;
    transient protected ChunkCoordinates location;

    public static Waypoint deathOf(Entity player)
    {
        ChunkCoordinates cc = new ChunkCoordinates((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        return at(cc, Type.Death, player.dimension);
    }

    public static Waypoint of(EntityPlayer player)
    {
        ChunkCoordinates cc = new ChunkCoordinates((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        return at(cc, Type.Normal, player.dimension);
    }

    public static Waypoint at(ChunkCoordinates cc, Type type, int dimension)
    {
        String name;
        if(type == Type.Death)
        {
            name = Constants.getString("Waypoint.deathpoint");
        }
        else
        {
            name = createName(cc.posX, cc.posZ);
        }
        Waypoint waypoint = new Waypoint(name, cc, Color.white, type, dimension);
        waypoint.setRandomColor();
        return waypoint;
    }

    private static String createName(int x, int z)
    {
        return String.format("%s, %s", x, z);
    }

    public Waypoint(Waypoint original)
    {
        this(original.name, original.x, original.y, original.z, original.enable, original.r, original.g, original.b, original.type, original.origin, original.dimensions);
    }

    public Waypoint(String name, ChunkCoordinates location, Color color, Type type, int dimension) {
        this(name, location.posX, location.posY, location.posZ, true, color.getRed(), color.getGreen(), color.getBlue(), type, "journeymap", dimension);
    }

    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, Type type, String origin, Integer... dimensions)
    {
        if(name==null) name = createName(x, z);
        this.name = name;
        setLocation(x, y, z);

        this.r = red;
        this.g = green;
        this.b = blue;
        this.enable = enable;
        this.type = type;
        this.origin = origin;
        this.dimensions = dimensions;

        switch(type)
        {
            case Normal:
            {
                icon = ICON_NORMAL;
            }
            case Death:
            {
                icon = ICON_DEATH;
            }
        }
    }

    public void setLocation(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        updateId();
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
        return isDeathPoint() ? TextureCache.instance().getDeathpoint() : TextureCache.instance().getWaypoint();
    }

    public ChunkCoordIntPair getChunkCoordIntPair()
    {
        return new ChunkCoordIntPair(x >> 4, z >> 4);
    }

    public void setColor(Color color)
    {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
    }

    public void setRandomColor()
    {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);

        int min = 100;
        int max = Math.max(r, Math.max(g, b));
        if(max < min)
        {
            if(r == max)
            {
                r = min;
            }
            else if(g == max)
            {
                g = min;
            }
            else
            {
                b = min;
            }
        }

        setColor(new Color(r,g,b));
    }

    public Color getColor()
    {
        return new Color(r,g,b);
    }

    public Collection<Integer> getDimensions()
    {
        return Arrays.asList(this.dimensions);
    }

    public void setDimensions(Collection<Integer> dims)
    {
        this.dimensions = dims.toArray(new Integer[dims.size()]);
    }

    public boolean isTeleportReady()
    {
        return y>=0 && this.isInPlayerDimension();
    }

    public boolean isInPlayerDimension()
    {
        Minecraft mc = Minecraft.getMinecraft();
        return getDimensions().contains(mc.thePlayer.dimension);
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public int getR()
    {
        return r;
    }

    public void setR(int r)
    {
        this.r = r;
    }

    public int getG()
    {
        return g;
    }

    public void setG(int g)
    {
        this.g = g;
    }

    public int getB()
    {
        return b;
    }

    public void setB(int b)
    {
        this.b = b;
    }

    public boolean isEnable()
    {
        return enable;
    }

    public void setEnable(boolean enable)
    {
        this.enable = enable;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public String getOrigin()
    {
        return origin;
    }

    public ChunkCoordinates getLocation()
    {
        if(location==null)
        {
            location = new ChunkCoordinates(x, y, z);
        }
        return location;
    }

    public String getFileName()
    {
        return id.replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
    }

    @Override
    public String toString()
    {
        return name;
    }
}
