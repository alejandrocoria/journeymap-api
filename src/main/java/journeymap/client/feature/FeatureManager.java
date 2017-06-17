/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.feature;

import journeymap.client.feature.impl.Unlimited;
import journeymap.common.Journeymap;
import journeymap.server.properties.PermissionProperties;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Governs what features are available at runtime.
 */
public class FeatureManager
{
    private final PolicySet policySet;
    private final HashMap<Feature, Policy> policyMap = new HashMap<Feature, Policy>();
    private final HashMap<String, EnumSet<Feature>> disableControlCodes = new HashMap<String, EnumSet<Feature>>();
    private Boolean controlCodeAltered = null;

    /**
     * Private constructure.  Use instance()
     */
    private FeatureManager()
    {
        disableControlCodes.put("\u00a73 \u00a76 \u00a73 \u00a76 \u00a73 \u00a76 \u00a7e", Feature.radar());
        disableControlCodes.put("\u00a73\u00a76\u00a73\u00a76\u00a73\u00a76\u00a7e", Feature.radar());
        disableControlCodes.put("\u00a73 \u00a76 \u00a73 \u00a76 \u00a73 \u00a76 \u00a7d", EnumSet.of(Feature.MapCaves));
        disableControlCodes.put("\u00a73\u00a76\u00a73\u00a76\u00a73\u00a76\u00a7d", EnumSet.of(Feature.MapCaves));
        policySet = new Unlimited();
        reset();
    }

    /**
     * Gets a detailed description of all policies.
     */
    public static String getPolicyDetails()
    {
        StringBuilder sb = new StringBuilder(String.format("%s Features: ", getPolicySetName()));
        for (Feature feature : Feature.values())
        {
            boolean single = false;
            boolean multi = false;
            if (Holder.INSTANCE.policyMap.containsKey(feature))
            {
                single = Holder.INSTANCE.policyMap.get(feature).allowInSingleplayer;
                multi = Holder.INSTANCE.policyMap.get(feature).allowInMultiplayer;
            }

            sb.append(String.format("\n\t%s : singleplayer = %s , multiplayer = %s", feature.name(), single, multi));
        }
        return sb.toString();
    }

    /**
     * Gets the singleton.
     */
    public static FeatureManager instance()
    {
        return Holder.INSTANCE;
    }

    /**
     * Whether the specified feature is currently permitted.
     *
     * @param feature the feature to check
     * @return true if permitted
     */
    public static boolean isAllowed(Feature feature)
    {
        Policy policy = Holder.INSTANCE.policyMap.get(feature);
        return (policy != null) && policy.isCurrentlyAllowed();
    }

    /**
     * Returns a map of all features and whether they are currently permitted.
     *
     * @return
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
     * Gets the name of the PolicySet.
     *
     * @return
     */
    public static String getPolicySetName()
    {
        return instance().policySet.getName();
    }

    public Set<String> getControlCodes()
    {
        return disableControlCodes.keySet();
    }

    public void handleControlCode(String controlCode)
    {
        if (disableControlCodes.containsKey(controlCode))
        {
            controlCodeAltered = true;
            for (Feature feature : disableControlCodes.get(controlCode))
            {
                Journeymap.getLogger().info("Feature disabled in multiplayer via control code: " + feature);
                Holder.INSTANCE.policyMap.put(feature, new Policy(feature, true, false));
            }

        }
    }

    public void disableDimensionFeature(PermissionProperties properties)
    {
        FeatureManager.instance().reset();

        Holder.INSTANCE.policyMap.put(Feature.RadarAnimals, new Policy(Feature.RadarAnimals, true, true));
        Holder.INSTANCE.policyMap.put(Feature.RadarMobs, new Policy(Feature.RadarMobs, true, true));
        Holder.INSTANCE.policyMap.put(Feature.RadarVillagers, new Policy(Feature.RadarVillagers, true, true));
        Holder.INSTANCE.policyMap.put(Feature.RadarPlayers, new Policy(Feature.RadarPlayers, true, true));
        Holder.INSTANCE.policyMap.put(Feature.MapCaves, new Policy(Feature.MapCaves, true, true));

        if (!properties.caveMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.MapCaves);
            Holder.INSTANCE.policyMap.put(Feature.MapCaves, new Policy(Feature.MapCaves, true, false));
        }

        if (properties.radarEnabled.get())
        {
            if (!properties.animalRadarEnabled.get())
            {
                Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.RadarAnimals);
                Holder.INSTANCE.policyMap.put(Feature.RadarAnimals, new Policy(Feature.RadarAnimals, true, false));
            }
            if (!properties.mobRadarEnabled.get())
            {
                Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.RadarMobs);
                Holder.INSTANCE.policyMap.put(Feature.RadarMobs, new Policy(Feature.RadarMobs, true, false));
            }
            if (!properties.playerRadarEnabled.get())
            {
                Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.RadarPlayers);
                Holder.INSTANCE.policyMap.put(Feature.RadarPlayers, new Policy(Feature.RadarPlayers, true, false));
            }
            if (!properties.villagerRadarEnabled.get())
            {
                Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.RadarVillagers);
                Holder.INSTANCE.policyMap.put(Feature.RadarVillagers, new Policy(Feature.RadarVillagers, true, false));
            }
        }
        else
        {
            Journeymap.getLogger().info("Feature disabled in multiplayer via control code: AllRadar");
            Holder.INSTANCE.policyMap.put(Feature.RadarAnimals, new Policy(Feature.RadarAnimals, true, false));
            Holder.INSTANCE.policyMap.put(Feature.RadarMobs, new Policy(Feature.RadarMobs, true, false));
            Holder.INSTANCE.policyMap.put(Feature.RadarVillagers, new Policy(Feature.RadarVillagers, true, false));
            Holder.INSTANCE.policyMap.put(Feature.RadarPlayers, new Policy(Feature.RadarPlayers, true, false));
        }
    }

    /**
     * Restores FeatureSet if a control code has altered the policy map
     */
    public void reset()
    {
        synchronized (policySet)
        {
            if (controlCodeAltered == null || controlCodeAltered)
            {
                for (Policy policy : policySet.getPolicies())
                {
                    policyMap.put(policy.feature, policy);
                }
                if (controlCodeAltered != null)
                {
                    Journeymap.getLogger().info("Returning to default " + getPolicyDetails());
                }
                controlCodeAltered = false;
            }
        }
    }

    /**
     * Interface for a named set of Policies.
     */
    public static interface PolicySet
    {
        public Set<Policy> getPolicies();

        public String getName();
    }

    /**
     * Instance holder.
     */
    private static class Holder
    {
        private static final FeatureManager INSTANCE = new FeatureManager();
    }

}
