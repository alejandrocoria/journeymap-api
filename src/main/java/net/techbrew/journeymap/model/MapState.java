/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.properties.CoreProperties;
import net.techbrew.journeymap.properties.InGameMapProperties;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.draw.RadarDrawStepFactory;
import net.techbrew.journeymap.render.draw.WaypointDrawStepFactory;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.task.MapPlayerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapState
{
    public final int minZoom = 0;
    public final int maxZoom = 5;
    // One-time setup
    final CoreProperties coreProperties = JourneyMap.getCoreProperties();
    // These can be safely changed at will
    public AtomicBoolean follow = new AtomicBoolean(true);

    public String playerLastPos = "0,0"; //$NON-NLS-1$
    StatTimer refreshTimer = StatTimer.get("MapState.refresh");
    StatTimer generateDrawStepsTimer = StatTimer.get("MapState.generateDrawSteps");
    // These must be internally managed
    private Constants.MapType preferredMapType;
    private File worldDir = null;
    private long lastRefresh = 0;
    private Integer vSlice = null;
    private boolean underground = false;
    private Integer dimension = null;
    private boolean caveMappingAllowed = false;
    private List<DrawStep> drawStepList = new ArrayList<DrawStep>();
    private List<DrawWayPointStep> drawWaypointStepList = new ArrayList<DrawWayPointStep>();
    private String playerBiome = "";
    private MapProperties lastMapProperties = null;
    private List<EntityDTO> entityList = new ArrayList<EntityDTO>(32);
    private int lastPlayerChunkX = 0;
    private int lastPlayerChunkZ = 0;

    /**
     * Default constructor
     */
    public MapState()
    {
    }

    public void refresh(Minecraft mc, EntityClientPlayerMP player, MapProperties mapProperties)
    {
        refreshTimer.start();

        boolean showCaves = JourneyMap.getFullMapProperties().showCaves.get();
        final MapType lastMapType = getMapType(showCaves);
        lastMapProperties = mapProperties;

        this.preferredMapType = mapProperties.getPreferredMapType().get();
        this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
        this.dimension = player.dimension;
        this.underground = DataCache.getPlayer().underground;
        this.vSlice = this.underground ? player.chunkCoordY : null;
        this.worldDir = FileHandler.getJMWorldDir(mc);

        lastPlayerChunkX = player.chunkCoordX;
        lastPlayerChunkZ = player.chunkCoordZ;

        if (player.dimension != this.dimension)
        {
            follow.set(true);
        }
        else
        {
            if (!worldDir.equals(this.worldDir))
            {
                follow.set(true);
            }
            else
            {
                if (getMapType(showCaves) == MapType.underground && lastMapType != MapType.underground)
                {
                    follow.set(true);
                }
            }
        }

        playerBiome = DataCache.getPlayer().biome;//+ (DataCache.getPlayer().underground ? " Underground" : " Surface");

        updateLastRefresh();

        refreshTimer.stop();
    }

    public void setMapType(MapType mapType)
    {
        if (mapType != getCurrentMapType())
        {
            if (mapType != MapType.underground)
            {
                lastMapProperties.getPreferredMapType().set(mapType);
                lastMapProperties.save();
                preferredMapType = mapType;
            }
        }


        requireRefresh();
    }

    public MapType getCurrentMapType()
    {
        boolean showCaves = JourneyMap.getFullMapProperties().showCaves.get();
        return getMapType(showCaves);
    }

    public MapType getMapType(boolean showCaves)
    {
        if (underground && caveMappingAllowed && showCaves)
        {
            return MapType.underground;
        }
        else
        {
            return preferredMapType;
        }
    }

    public Integer getVSlice()
    {
        return vSlice;
    }

    public boolean isUnderground()
    {
        return underground;
    }

    public int getDimension()
    {
        return dimension;
    }

    public File getWorldDir()
    {
        return worldDir;
    }

    public String getPlayerBiome()
    {
        return playerBiome;
    }

    public List<DrawStep> getDrawSteps()
    {
        return drawStepList;
    }

    public List<DrawWayPointStep> getDrawWaypointSteps()
    {
        return drawWaypointStepList;
    }

    public void generateDrawSteps(Minecraft mc, GridRenderer gridRenderer, WaypointDrawStepFactory waypointRenderer, RadarDrawStepFactory radarRenderer, InGameMapProperties mapProperties, float drawScale, boolean checkWaypointDistance)
    {
        generateDrawStepsTimer.start();
        lastMapProperties = mapProperties;

        drawStepList.clear();
        drawWaypointStepList.clear();
        entityList.clear();

        if (mapProperties.zoomLevel.get() == 0)
        {
            drawScale = drawScale * .5f;
        }

        if (FeatureManager.isAllowed(Feature.RadarAnimals))
        {
            if (mapProperties.showAnimals.get() || mapProperties.showPets.get())
            {
                entityList.addAll(DataCache.instance().getAnimals(false).values());
            }
        }
        if (FeatureManager.isAllowed(Feature.RadarVillagers))
        {
            if (mapProperties.showVillagers.get())
            {
                entityList.addAll(DataCache.instance().getVillagers(false).values());
            }
        }
        if (FeatureManager.isAllowed(Feature.RadarMobs))
        {
            if (mapProperties.showMobs.get())
            {
                entityList.addAll(DataCache.instance().getMobs(false).values());
            }
        }

        if (FeatureManager.isAllowed(Feature.RadarPlayers))
        {
            if (mapProperties.showPlayers.get())
            {
                entityList.addAll(DataCache.instance().getPlayers(false).values());
            }
        }

        // Sort to keep named entities last
        if (!entityList.isEmpty())
        {
            Collections.sort(entityList, EntityHelper.entityMapComparator);
            drawStepList.addAll(radarRenderer.prepareSteps(entityList, gridRenderer, drawScale, mapProperties));
        }

        // Draw waypoints
        if (mapProperties.showWaypoints.get())
        {
            boolean showLabel = mapProperties.showWaypointLabels.get();
            drawWaypointStepList.addAll(waypointRenderer.prepareSteps(DataCache.instance().getWaypoints(false), gridRenderer, checkWaypointDistance, showLabel));
        }

        generateDrawStepsTimer.stop();
    }

    public boolean zoomIn()
    {
        if (lastMapProperties.zoomLevel.get() < maxZoom)
        {
            return setZoom(lastMapProperties.zoomLevel.get() + 1);
        }
        return false;
    }

    public boolean zoomOut()
    {
        if (lastMapProperties.zoomLevel.get() > minZoom)
        {
            return setZoom(lastMapProperties.zoomLevel.get() - 1);
        }
        return false;
    }

    public boolean setZoom(int zoom)
    {
        if (zoom > maxZoom || zoom < minZoom || zoom == lastMapProperties.zoomLevel.get())
        {
            return false;
        }
        lastMapProperties.zoomLevel.set(zoom);
        requireRefresh();
        return true;
    }

    public int getZoom()
    {
        return lastMapProperties.zoomLevel.get();
    }

    public void requireRefresh()
    {
        this.lastRefresh = 0;
    }

    public void updateLastRefresh()
    {
        this.lastRefresh = System.currentTimeMillis();
    }

    public boolean shouldRefresh(Minecraft mc, MapProperties mapProperties)
    {
        EntityDTO player = DataCache.getPlayer();

        if (System.currentTimeMillis() > (lastRefresh + MapPlayerTask.getLastChunkStatsTime() + 500))
        {
            return true;
        }

        if (this.dimension != mc.theWorld.provider.dimensionId)
        {
            return true;
        }

        if (this.underground != player.underground)
        {
            return true;
        }

        if (this.vSlice != null && (!player.underground || this.vSlice != player.chunkCoordY))
        {
            return true;
        }

        int diffX = Math.abs(lastPlayerChunkX - player.chunkCoordX);
        if (diffX > 2)
        {
            return true; // should happen on a teleport
        }

        int diffZ = Math.abs(lastPlayerChunkZ - player.chunkCoordZ);
        if (diffZ > 2)
        {
            return true; // should happen on a teleport
        }

        if (lastMapProperties == null || !lastMapProperties.equals(mapProperties))
        {
            return true;
        }

        lastPlayerChunkX = player.chunkCoordX;
        lastPlayerChunkZ = player.chunkCoordZ;

        return false;
    }

    public boolean isCaveMappingAllowed()
    {
        return caveMappingAllowed;
    }
}
