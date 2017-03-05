/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.map;

/**
 * The type Tile pos.
 */
public class TilePos implements Comparable<TilePos>
{

    /**
     * The Delta x.
     */
    public final int deltaX;
    /**
     * The Delta z.
     */
    public final int deltaZ;

    /**
     * The Start x.
     */
    final double startX;
    /**
     * The Start z.
     */
    final double startZ;
    /**
     * The End x.
     */
    final double endX;
    /**
     * The End z.
     */
    final double endZ;

    /**
     * Instantiates a new Tile pos.
     *
     * @param deltaX the delta x
     * @param deltaZ the delta z
     */
    TilePos(int deltaX, int deltaZ)
    {
        this.deltaX = deltaX;
        this.deltaZ = deltaZ;

        this.startX = deltaX * Tile.TILESIZE;
        this.startZ = deltaZ * Tile.TILESIZE;
        this.endX = startX + Tile.TILESIZE;
        this.endZ = startZ + Tile.TILESIZE;
    }

    @Override
    public int hashCode()
    {
        final int prime = 37;
        int result = 1;
        result = prime * result + deltaX;
        result = prime * result + deltaZ;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        TilePos other = (TilePos) obj;
        if (deltaX != other.deltaX)
        {
            return false;
        }
        if (deltaZ != other.deltaZ)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TilePos [" + deltaX + "," + deltaZ + "]";
    }

    @Override
    public int compareTo(TilePos o)
    {
        int result = new Integer(deltaZ).compareTo(o.deltaZ);
        if (result == 0)
        {
            result = new Integer(deltaX).compareTo(o.deltaX);
        }
        return result;
    }

}
