/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.feature;

import com.google.common.base.MoreObjects;

/**
 * Created by Mysticdrew on 9/16/2018.
 */
public class Location
{

    private double x;

    private double y;

    private double z;

    private int dim;

    public Location()
    {
    }

    public Location(double x, double y, double z, int dim)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public int getDim()
    {
        return dim;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .add("z", z)
                .add("dim", dim)
                .toString();
    }
}
