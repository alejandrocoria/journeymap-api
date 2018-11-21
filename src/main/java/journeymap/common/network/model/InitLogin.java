/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network.model;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Mysticdrew on 11/19/2018.
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
