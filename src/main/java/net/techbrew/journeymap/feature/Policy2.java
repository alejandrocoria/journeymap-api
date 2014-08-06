/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.feature;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Couples a feature with the contexts in which it is permitted.
 */
public class Policy2
{
    static Minecraft mc = FMLClientHandler.instance().getClient();
    final Feature feature;
    final boolean allowInSingleplayer;
    final boolean allowInMultiplayer;

    /**
     * Constructor.
     */
    public Policy2(Feature feature, boolean allowInSingleplayer, boolean allowInMultiplayer)
    {
        this.feature = feature;
        this.allowInSingleplayer = allowInSingleplayer;
        this.allowInMultiplayer = allowInMultiplayer;
    }

    /**
     * Get a set of Policies based on categorical usage of all features.
     *
     * @param allowInSingleplayer
     * @param allowInMultiplayer
     * @return
     */
    public static Set<Policy2> bulkCreate(boolean allowInSingleplayer, boolean allowInMultiplayer)
    {
        return bulkCreate(Feature.all(), allowInSingleplayer, allowInMultiplayer);
    }

    /**
     * Get a set of Policies based on categorical usage of a set of features.
     *
     * @param allowInSingleplayer
     * @param allowInMultiplayer
     * @return
     */
    public static Set<Policy2> bulkCreate(EnumSet<Feature> features, boolean allowInSingleplayer, boolean allowInMultiplayer)
    {
        Set<Policy2> policies = new HashSet<Policy2>();
        for (Feature feature : features)
        {
            policies.add(new Policy2(feature, allowInSingleplayer, allowInMultiplayer));
        }
        return policies;
    }

    /**
     * Checks whether the feature is allowed based on current game context of single/multiplayer.
     *
     * @return true if allowed
     */
    public boolean isCurrentlyAllowed()
    {
        if (allowInSingleplayer == allowInMultiplayer)
        {
            return allowInSingleplayer;
        }
        else
        {
            IntegratedServer server = mc.getIntegratedServer();
            boolean isSinglePlayer = (server != null) && !server.getPublic();

            if (allowInSingleplayer && isSinglePlayer)
            {
                return true;
            }
            if (allowInMultiplayer && !isSinglePlayer)
            {
                return true;
            }
        }
        return false;
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

        Policy2 policy = (Policy2) o;

        if (allowInMultiplayer != policy.allowInMultiplayer)
        {
            return false;
        }
        if (allowInSingleplayer != policy.allowInSingleplayer)
        {
            return false;
        }
        if (feature != policy.feature)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = feature.hashCode();
        result = 31 * result + (allowInSingleplayer ? 1 : 0);
        result = 31 * result + (allowInMultiplayer ? 1 : 0);
        return result;
    }
}
