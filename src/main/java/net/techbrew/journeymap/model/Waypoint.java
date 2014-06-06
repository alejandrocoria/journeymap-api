package net.techbrew.journeymap.model;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

/**
 * Generic waypoint data holder
 */
public class Waypoint implements Serializable
{
    protected static final String ICON_NORMAL = "waypoint-normal.png";
    protected static final String ICON_DEATH = "waypoint-death.png";

    public enum Origin
    {
        JourneyMap,
        ReiMinimap,
        VoxelMap
    }

    public enum Type
    {
        Normal,
        Death
    }

    protected int version = 1;
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
    protected Origin origin;
    protected String texture;

    protected TreeSet<Integer> dimensions;
    transient protected boolean readOnly;
    transient protected boolean dirty;

    public static Waypoint deathOf(Entity player)
    {
        ChunkCoordinates cc = new ChunkCoordinates((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        return at(cc, Type.Death, player.worldObj.provider.dimensionId);
    }

    public static Waypoint of(EntityPlayer player)
    {
        ChunkCoordinates cc = new ChunkCoordinates((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        return at(cc, Type.Normal, player.worldObj.provider.dimensionId);
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
        this.x = original.x;
        this.y = original.y;
        this.z = original.z;
    }

    public Waypoint(String name, ChunkCoordinates location, Color color, Type type, Integer... dimension) {
        this(name, location.posX, location.posY, location.posZ, true, color.getRed(), color.getGreen(), color.getBlue(), type, Origin.JourneyMap, dimension);
    }

    /**
     * Main constructor.
     */
    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, Type type, Origin origin, Integer... dimensions)
    {
        this(name, x, y, z, enable, red, green, blue, type, origin, new TreeSet<Integer>(Arrays.asList(dimensions)));
    }

    /**
     * Main constructor.
     */
    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, Type type, Origin origin, Collection<Integer> dimensions)
    {
        if(name==null) name = createName(x, z);
        if(dimensions==null || dimensions.size()==0)
        {
            dimensions = new TreeSet<Integer>();
            Minecraft mc = FMLClientHandler.instance().getClient();
            dimensions.add(mc.thePlayer.worldObj.provider.dimensionId);
        }
        this.dimensions = new TreeSet<Integer>(dimensions);

        JourneyMap.getLogger().fine("Waypoint created with dimensions " + new ArrayList<Integer>(getDimensions()));

        this.name = name;
        setLocation(x, y, z, this.dimensions.first());

        this.r = red;
        this.g = green;
        this.b = blue;
        this.enable = enable;
        this.type = type;
        this.origin = origin;

        switch(type)
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

    public void setLocation(int x, int y, int z)
    {
        setLocation(x, y, z, 0);
    }

    public void setLocation(int x, int y, int z, int currentDimension)
    {
        this.x = dimensionalValue(x, currentDimension, 0);
        this.y = dimensionalValue(y, currentDimension, 0);
        this.z = dimensionalValue(z, currentDimension, 0);
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

    public Color getSafeColor()
    {
        if(r+g+b>=100) return getColor();
        return Color.darkGray;
    }

    public Collection<Integer> getDimensions()
    {
        return this.dimensions;
    }

    public void setDimensions(Collection<Integer> dims)
    {
        this.dimensions = new TreeSet<Integer>(dims);
    }

    public boolean isTeleportReady()
    {
        return y>=0 && this.isInPlayerDimension();
    }

    public boolean isInPlayerDimension()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        return dimensions.contains(mc.thePlayer.worldObj.provider.dimensionId);
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

    private int getX()
    {
        return x;
    }

    private int getY()
    {
        return y;
    }

    private int getZ()
    {
        return z;
    }

    public int getX(int dimension)
    {
        return dimensionalValue(x, 0, dimension);
    }

    public int getY(int dimension)
    {
        return dimensionalValue(y, 0, dimension);
    }

    public int getZ(int dimension)
    {
        return dimensionalValue(z, 0, dimension);
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
        if(enable!=this.enable)
        {
            this.enable = enable;
            this.dirty = true;
        }
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Origin getOrigin()
    {
        return origin;
    }

    public String getFileName()
    {
        return id.replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
    }

    public int getVersion()
    {
        return version;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    public void setOrigin(Origin origin)
    {
        this.origin = origin;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    @Override
    public String toString()
    {
        return name;
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
        if (!(dimensions.equals(waypoint.dimensions)))
        {
            return false;
        }
        if (!icon.equals(waypoint.icon))
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

    private static int dimensionalValue(int original, int currentDimension, int targetDimension)
    {
        if(targetDimension==currentDimension)
        {
            return original;
        }
        else if(targetDimension==-1)
        {
            return original/8;
        }
        else if(currentDimension==-1)
        {
            return original*8;
        }
        else
        {
            return original;
        }
    }
}
