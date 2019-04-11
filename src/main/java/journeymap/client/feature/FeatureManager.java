/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.feature;

import journeymap.client.data.DataCache;
import journeymap.client.model.MapType;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
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
//        reset();

        if (!properties.caveMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature disabled: " + Feature.MapCaves);
            nextMapType(Feature.MapCaves);
            policyMap.put(Feature.MapCaves, new Policy(Feature.MapCaves, false, false));

        }
        else if (!policyMap.get(Feature.MapCaves).isCurrentlyAllowed() && properties.caveMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature enabled: " + Feature.MapCaves);
            policyMap.put(Feature.MapCaves, new Policy(Feature.MapCaves, true, true));
            if (MapType.none().equals(Fullscreen.state().getMapType()))
            {
                setMapType(MapType.underground(DataCache.getPlayer()));
            }
        }

        if (!properties.topoMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature disabled: " + Feature.MapTopo);
            nextMapType(Feature.MapTopo);
            policyMap.put(Feature.MapTopo, new Policy(Feature.MapTopo, false, false));
        }
        else if (!policyMap.get(Feature.MapTopo).isCurrentlyAllowed() && properties.topoMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature enabled: " + Feature.MapTopo);
            policyMap.put(Feature.MapTopo, new Policy(Feature.MapTopo, true, true));
            if (MapType.none().equals(Fullscreen.state().getMapType()))
            {
                setMapType(MapType.topo(DataCache.getPlayer()));
            }
        }

        if (!properties.surfaceMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature disabled: " + Feature.MapSurface);
            nextMapType(Feature.MapSurface);
            policyMap.put(Feature.MapSurface, new Policy(Feature.MapSurface, false, false));
        }
        else if (!policyMap.get(Feature.MapSurface).isCurrentlyAllowed() && properties.surfaceMappingEnabled.get())
        {
            Journeymap.getLogger().info("Feature enabled: " + Feature.MapSurface);
            policyMap.put(Feature.MapSurface, new Policy(Feature.MapSurface, true, true));

            if (MapType.none().equals(Fullscreen.state().getMapType()))
            {
                final long time = DataCache.getPlayer().entityLivingRef.get().world.getWorldInfo().getWorldTime() % 24000L;
                MapType mapType = (time < 13800) ? MapType.day(DataCache.getPlayer()) : MapType.night(DataCache.getPlayer());
                setMapType(mapType);
            }
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

    private void nextMapType(Feature feature)
    {
        if (policyMap.get(feature).isCurrentlyAllowed() && Fullscreen.state() != null)
        {
            if (Fullscreen.state().isSurfaceMappingAllowed() && !Feature.MapSurface.equals(feature))
            {
                setMapType(MapType.day(DataCache.getPlayer()));
                return;
            }
            else if (Fullscreen.state().isTopoMappingAllowed() && !Feature.MapTopo.equals(feature))
            {
                setMapType(MapType.topo(DataCache.getPlayer()));
                return;
            }
            else if (Fullscreen.state().isCaveMappingAllowed() && !Feature.MapCaves.equals(feature))
            {
                setMapType(MapType.underground(DataCache.getPlayer()));
                return;
            }
        }
//        setMapType(MapType.none());
    }

    private void setMapType(MapType to)
    {
        Fullscreen.state().setMapType(to);
        MiniMap.state().setMapType(to);
    }

    private void setMultiplayerFeature(Feature feature, boolean enable)
    {
        if (!enable)
        {
            Journeymap.getLogger().info("Feature disabled: " + feature);
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
