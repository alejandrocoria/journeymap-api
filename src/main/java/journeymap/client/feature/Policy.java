/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Couples a feature with the contexts in which it is permitted.
 */
public class Policy
{
    /**
     * Minecraft client
     */
    static Minecraft mc = FMLClientHandler.instance().getClient();
    /**
     * The Feature.
     */
    final Feature feature;
    /**
     * The Allow in singleplayer.
     */
    final boolean allowInSingleplayer;
    /**
     * The Allow in multiplayer.
     */
    final boolean allowInMultiplayer;

    /**
     * Constructor.
     *
     * @param feature             the feature
     * @param allowInSingleplayer the allow in singleplayer
     * @param allowInMultiplayer  the allow in multiplayer
     */
    public Policy(Feature feature, boolean allowInSingleplayer, boolean allowInMultiplayer)
    {
        this.feature = feature;
        this.allowInSingleplayer = allowInSingleplayer;
        this.allowInMultiplayer = allowInMultiplayer;
    }

    /**
     * Get a set of Policies based on categorical usage of all features.
     *
     * @param allowInSingleplayer the allow in singleplayer
     * @param allowInMultiplayer  the allow in multiplayer
     * @return set
     */
    public static Set<Policy> bulkCreate(boolean allowInSingleplayer, boolean allowInMultiplayer)
    {
        return bulkCreate(Feature.all(), allowInSingleplayer, allowInMultiplayer);
    }

    /**
     * Get a set of Policies based on categorical usage of a set of features.
     *
     * @param features            the features
     * @param allowInSingleplayer the allow in singleplayer
     * @param allowInMultiplayer  the allow in multiplayer
     * @return set
     */
    public static Set<Policy> bulkCreate(EnumSet<Feature> features, boolean allowInSingleplayer, boolean allowInMultiplayer)
    {
        Set<Policy> policies = new HashSet<Policy>();
        for (Feature feature : features)
        {
            policies.add(new Policy(feature, allowInSingleplayer, allowInMultiplayer));
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

        Policy policy = (Policy) o;

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
