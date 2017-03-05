/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

/**
 * The type Block coord int pair.
 */
public class BlockCoordIntPair
{

    /**
     * The X.
     */
    public int x;
    /**
     * The Z.
     */
    public int z;

    /**
     * Instantiates a new Block coord int pair.
     */
    public BlockCoordIntPair()
    {
        setLocation(0, 0);
    }

    /**
     * Instantiates a new Block coord int pair.
     *
     * @param x the x
     * @param z the z
     */
    public BlockCoordIntPair(int x, int z)
    {
        setLocation(x, z);
    }

    /**
     * Sets location.
     *
     * @param x the x
     * @param z the z
     */
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
