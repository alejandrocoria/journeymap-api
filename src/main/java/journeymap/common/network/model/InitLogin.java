package journeymap.common.network.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Mysticdrew on 11/19/2016.
 */
public class InitLogin
{
    public static final Gson GSON = new GsonBuilder().create();
    private boolean teleportEnabled;

    public InitLogin()
    {
    }

    public boolean isTeleportEnabled()
    {
        return teleportEnabled;
    }

    public void setTeleportEnabled(boolean teleportEnabled)
    {
        this.teleportEnabled = teleportEnabled;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("teleportEnabled", teleportEnabled)
                .toString();
    }
}
