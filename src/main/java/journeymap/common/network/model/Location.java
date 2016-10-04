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

    public static final Gson GSON = new GsonBuilder().create();


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
        return Objects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .add("z", z)
                .add("dim", dim)
                .toString();
    }
}
