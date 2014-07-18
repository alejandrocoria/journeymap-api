/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.overlay;

public class TilePos implements Comparable<TilePos>
{

    public final int deltaX;
    public final int deltaZ;

    final double startX;
    final double startZ;
    final double endX;
    final double endZ;

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
