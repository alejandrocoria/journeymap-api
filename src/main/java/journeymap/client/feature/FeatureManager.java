/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.feature;

import journeymap.common.Journeymap;
import journeymap.server.properties.PermissionProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Governs what features are available at runtime.
 */
public enum FeatureManager
{
    INSTANCE;

    private final HashMap<Feature, Policy> policyMap = new HashMap<Feature, Policy>();

    /**
     * Private constructure.  Use instance()
     */
    private FeatureManager()
    {
        reset();
    }

    /**
     * Gets a detailed description of all policies.
     *
     * @return the policy details
     */
    public static String getPolicyDetails()
    {
        StringBuilder sb = new StringBuilder("Features: ");
        for (Feature feature : Feature.values())
        {
            boolean single = false;
            boolean multi = false;
            if (INSTANCE.policyMap.containsKey(feature))
            {
                single = INSTANCE.policyMap.get(feature).allowInSingleplayer;
                multi = INSTANCE.policyMap.get(feature).allowInMultiplayer;
            }

            sb.append(String.format("\n\t%s : singleplayer = %s , multiplayer = %s", feature.name(), single, multi));
        }
        return sb.toString();
    }

    /**
     * Whether the specified feature is currently permitted.
     *
     * @param feature the feature to check
     * @return true if permitted
     */
    public static boolean isAllowed(Feature feature)
    {
        Policy policy = INSTANCE.policyMap.get(feature);
        return (policy != null) && policy.isCurrentlyAllowed();
    }

    /**
     * Returns a map of all features and whether they are currently permitted.
     *
     * @return allowed features
     */
    public static Map<Feature, Boolean> getAllowedFeatures()
    {
        Map<Feature, Boolean> map = new HashMap<Feature, Boolean>(Feature.values().length * 2);
        for (Feature feature : Feature.values())
        {
            map.put(feature, isAllowed(feature));
        }
        return map;
    }

    /**
     * Disable dimension feature.
     *
     * @param properties the properties
     */
    public void updateDimensionFeatures(PermissionProperties properties)
    {
        reset();

        if (!properties.caveMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.MapCaves);
            policyMap.put(Feature.MapCaves, new Policy(Feature.MapCaves, true, false));
        }

        if (properties.radarEnabled.get())
        {
            setMultiplayerFeature(Feature.RadarAnimals, properties.animalRadarEnabled.get());
            setMultiplayerFeature(Feature.RadarMobs, properties.mobRadarEnabled.get());
            setMultiplayerFeature(Feature.RadarPlayers, properties.playerRadarEnabled.get());
            setMultiplayerFeature(Feature.RadarVillagers, properties.villagerRadarEnabled.get());
        }
        else
        {
            setMultiplayerFeature(Feature.RadarAnimals, false);
            setMultiplayerFeature(Feature.RadarMobs, false);
            setMultiplayerFeature(Feature.RadarPlayers, false);
            setMultiplayerFeature(Feature.RadarVillagers, false);
        }
    }

    private void setMultiplayerFeature(Feature feature, boolean enable)
    {
        if (!enable)
        {
            Journeymap.getLogger().info("Feature disabled in multiplayer: " + feature);
        }
        policyMap.put(feature, new Policy(feature, true, enable));
    }

    /**
     * Restores FeatureSet if a control code has altered the policy map
     */
    public void reset()
    {
        for (Policy policy : Policy.bulkCreate(true, true))
        {
            policyMap.put(policy.feature, policy);
        }
    }
}
