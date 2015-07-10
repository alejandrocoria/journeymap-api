/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.model;

public class BlockCoordIntPair
{

    public int x;
    public int z;

    public BlockCoordIntPair()
    {
        setLocation(0, 0);
    }

    public BlockCoordIntPair(int x, int z)
    {
        setLocation(x, z);
    }

    public void setLocation(int x, int z)
    {
        this.x = x;
        this.z = z;
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

        BlockCoordIntPair that = (BlockCoordIntPair) o;

        if (x != that.x)
        {
            return false;
        }
        if (z != that.z)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + z;
        return result;
    }
}
