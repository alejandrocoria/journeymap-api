/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;


import com.google.common.base.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.properties.InGameMapProperties;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.draw.RadarDrawStepFactory;
import net.techbrew.journeymap.render.draw.WaypointDrawStepFactory;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.task.multi.MapPlayerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapState
{
    public final int minZoom = 0;
    public final int maxZoom = 5;

    // These can be safely changed at will
    public AtomicBoolean follow = new AtomicBoolean(true);

    public String playerLastPos = "0,0"; //$NON-NLS-1$
    StatTimer refreshTimer = StatTimer.get("MapState.refresh");
    StatTimer generateDrawStepsTimer = StatTimer.get("MapState.generateDrawSteps");

    // These must be internally managed
    private MapType preferredMapType;
    private MapType lastMapType;
    private File worldDir = null;
    private long lastRefresh = 0;
    private long lastMapTypeChange = 0;

    private boolean underground = false;

    private boolean caveMappingAllowed = false;
    private List<DrawStep> drawStepList = new ArrayList<DrawStep>();
    private List<DrawWayPointStep> drawWaypointStepList = new ArrayList<DrawWayPointStep>();
    private String playerBiome = "";
    private InGameMapProperties lastMapProperties = null;
    private List<EntityDTO> entityList = new ArrayList<EntityDTO>(32);
    private int lastPlayerChunkX = 0;
    private int lastPlayerChunkZ = 0;
    private boolean highQuality;

    /**
     * Default constructor
     */
    public MapState()
    {
    }

    public void refresh(Minecraft mc, EntityClientPlayerMP player, InGameMapProperties mapProperties)
    {
        refreshTimer.start();
        lastMapProperties = mapProperties;
        boolean showCaves = mapProperties.showCaves.get();
        if (lastMapType == null)
        {
            lastMapType = getMapType(showCaves);
        }

        this.underground = DataCache.getPlayer().underground;
        Integer vSlice = this.underground ? player.chunkCoordY : null;
        this.preferredMapType = MapType.from(mapProperties.getPreferredMapType().get(), vSlice, player.dimension);
        this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
        this.worldDir = FileHandler.getJMWorldDir(mc);

        lastPlayerChunkX = player.chunkCoordX;
        lastPlayerChunkZ = player.chunkCoordZ;
        highQuality = JourneyMap.getCoreProperties().tileHighDisplayQuality.get();

        if (player.dimension != this.getCurrentMapType().dimension)
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
                if (lastMapType == null || getMapType(showCaves).isUnderground() != lastMapType.isUnderground())
                {
                    follow.set(true);
                }
            }
        }

        playerBiome = DataCache.getPlayer().biome;

        updateLastRefresh();

        refreshTimer.stop();
    }

    public void setMapType(MapType.Name mapTypeName)
    {
        setMapType(MapType.from(mapTypeName, DataCache.getPlayer()));
    }

    public void toggleMapType()
    {
        EntityDTO player = DataCache.getPlayer();
        MapType currentMapType = getCurrentMapType();
        if (currentMapType.isUnderground())
        {
            if (!player.entityLiving.worldObj.provider.hasNoSky)
            {
                lastMapProperties.showCaves.set(false);
                setMapType(MapType.day(player));
            }
            else
            {
                setMapType(MapType.underground(player));
            }
        }
        else if (currentMapType.isDay())
        {
            setMapType(MapType.night(player));
        }
        else if (currentMapType.isNight())
        {
            if (underground && caveMappingAllowed)
            {
                lastMapProperties.showCaves.set(true);
                setMapType(MapType.underground(player));
            }
            else
            {
                setMapType(MapType.day(player));
            }
        }
        lastMapTypeChange = System.currentTimeMillis();
    }

    public void setMapType(MapType mapType)
    {
        if (!mapType.equals(getCurrentMapType()))
        {
            if (!mapType.isUnderground())
            {
                lastMapProperties.getPreferredMapType().set(mapType.name);
                preferredMapType = mapType;
            }
        }
        lastMapProperties.save();
        lastMapTypeChange = System.currentTimeMillis();
        requireRefresh();
    }

    public MapType getCurrentMapType()
    {
        boolean showCaves = lastMapProperties.showCaves.get();
        return getMapType(showCaves);
    }

    public MapType getMapType(boolean showCaves)
    {
        MapType mapType = null;

        if (DataCache.getPlayer().entityLiving.worldObj.provider.hasNoSky)
        {
            mapType = MapType.underground(DataCache.getPlayer());
        }
        else if (underground && caveMappingAllowed && showCaves)
        {
            mapType = MapType.underground(DataCache.getPlayer());
        }
        else
        {
            if (preferredMapType == null)
            {
                this.preferredMapType = MapType.from(lastMapProperties.getPreferredMapType().get(), DataCache.getPlayer());
            }
            mapType = preferredMapType;
        }

        if (!Objects.equal(mapType, lastMapType))
        {
            lastMapType = mapType;
            lastMapTypeChange = System.currentTimeMillis();
        }

        return mapType;
    }

    public long getLastMapTypeChange()
    {
        return lastMapTypeChange;
    }

    public boolean isUnderground()
    {
        return underground;
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

        if (MapPlayerTask.getlastTaskCompleted() - lastRefresh > 500)
        {
            return true;
        }

        if (this.getCurrentMapType().dimension != player.dimension)
        {
            return true;
        }

        if (this.underground != player.underground)
        {
            return true;
        }

        if (this.getCurrentMapType().vSlice != null && (!player.underground || this.getCurrentMapType().vSlice != player.chunkCoordY))
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

    public boolean isHighQuality()
    {
        return highQuality;
    }

    public boolean isCaveMappingAllowed()
    {
        return caveMappingAllowed;
    }

    public int getDimension()
    {
        return getCurrentMapType().dimension;
    }
}
