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


    private int x;

    private int y;

    private int z;

    private int dim;

    public Location()
    {
    }

    public Location(int x, int y, int z, int dim)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
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
