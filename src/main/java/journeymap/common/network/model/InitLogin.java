/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network.model;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Mysticdrew on 11/19/2016.
 */
public class InitLogin
{
    /**
     * The constant GSON.
     */
    public static final Gson GSON = new GsonBuilder().create();
    private boolean teleportEnabled;

    /**
     * Instantiates a new Init login.
     */
    public InitLogin()
    {
    }

    /**
     * Is teleport enabled boolean.
     *
     * @return the boolean
     */
    public boolean isTeleportEnabled()
    {
        return teleportEnabled;
    }

    /**
     * Sets teleport enabled.
     *
     * @param teleportEnabled the teleport enabled
     */
    public void setTeleportEnabled(boolean teleportEnabled)
    {
        this.teleportEnabled = teleportEnabled;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("teleportEnabled", teleportEnabled)
                .toString();
    }
}
