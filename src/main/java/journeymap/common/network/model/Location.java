/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network.model;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * Created by Mysticdrew on 9/16/2016.
 */
public class Location implements Serializable
{

    /**
     * The constant GSON.
     */
    public static final Gson GSON = new GsonBuilder().create();


    private double x;

    private double y;

    private double z;

    private int dim;

    /**
     * Instantiates a new Location.
     */
    public Location()
    {
    }

    /**
     * Instantiates a new Location.
     *
     * @param x   the x
     * @param y   the y
     * @param z   the z
     * @param dim the dim
     */
    public Location(double x, double y, double z, int dim)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public double getX()
    {
        return x;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public double getY()
    {
        return y;
    }

    /**
     * Gets z.
     *
     * @return the z
     */
    public double getZ()
    {
        return z;
    }

    /**
     * Gets dim.
     *
     * @return the dim
     */
    public int getDim()
    {
        return dim;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .add("z", z)
                .add("dim", dim)
                .toString();
    }
}
