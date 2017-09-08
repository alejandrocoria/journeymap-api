/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;


import com.google.common.base.Objects;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.io.FileHandler;
import journeymap.client.log.StatTimer;
import journeymap.client.properties.InGameMapProperties;
import journeymap.client.properties.MapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.client.render.draw.RadarDrawStepFactory;
import journeymap.client.render.draw.WaypointDrawStepFactory;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Map state.
 */
public class MapState
{
    /**
     * The Min zoom.
     */
    public final int minZoom = 0;
    /**
     * The Max zoom.
     */
    public final int maxZoom = 5;

    /**
     * The Follow.
     */
// These can be safely changed at will
    public AtomicBoolean follow = new AtomicBoolean(true);

    /**
     * The Player last pos.
     */
    public String playerLastPos = "0,0"; //$NON-NLS-1$
    /**
     * The Refresh timer.
     */
    StatTimer refreshTimer = StatTimer.get("MapState.refresh");
    /**
     * The Generate draw steps timer.
     */
    StatTimer generateDrawStepsTimer = StatTimer.get("MapState.generateDrawSteps");

    // These must be internally managed
    private MapType preferredMapType;
    private MapType lastMapType;
    private File worldDir = null;
    private long lastRefresh = 0;
    private long lastMapTypeChange = 0;

    private boolean underground = false;

    private boolean surfaceMappingAllowed = false;
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

    /**
     * Refresh.
     *
     * @param mc            the mc
     * @param player        the player
     * @param mapProperties the map properties
     */
    public void refresh(Minecraft mc, EntityPlayer player, InGameMapProperties mapProperties)
    {
        try
        {
            refreshTimer.start();
            lastMapProperties = mapProperties;
            boolean showCaves = mapProperties.showCaves.get();
            if (lastMapType == null)
            {
                lastMapType = getMapType(showCaves);
            }
            this.surfaceMappingAllowed = FeatureManager.isAllowed(Feature.MapSurface);
            this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
            this.worldDir = FileHandler.getJMWorldDir(mc);

            this.underground = DataCache.getPlayer().underground;
            Integer vSlice = this.underground && showCaves ? player.chunkCoordY : null;
            this.preferredMapType = MapType.from(mapProperties.preferredMapType.get(), vSlice, player.dimension);

            lastPlayerChunkX = player.chunkCoordX;
            lastPlayerChunkZ = player.chunkCoordZ;
            highQuality = Journeymap.getClient().getCoreProperties().tileHighDisplayQuality.get();

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
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error refreshing MapState: " + LogFormatter.toPartialString(e));
        }
    }

    /**
     * Sets map type.
     *
     * @param mapTypeName the map type name
     */
    public void setMapType(MapType.Name mapTypeName)
    {
        setMapType(MapType.from(mapTypeName, DataCache.getPlayer()));
    }

    /**
     * Toggle map type.
     */
    public void toggleMapType()
    {
        EntityDTO player = DataCache.getPlayer();
        MapType currentMapType = getCurrentMapType();
        final EntityLivingBase playerEntity = player.entityLivingRef.get();
        if (playerEntity == null)
        {
            return;
        }

        boolean mapTopo = Journeymap.getClient().getCoreProperties().mapTopography.get();

        if (currentMapType.isUnderground())
        {
            setMapType(MapType.day(player));
        }
        else if (currentMapType.isDay())
        {
            setMapType(MapType.night(player));
        }
        else if (currentMapType.isNight() && mapTopo)
        {
            setMapType(MapType.topo(player));
        }
        else if (currentMapType.isNight() || currentMapType.isTopo())
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
    }

    /**
     * Sets map type.
     *
     * @param mapType the map type
     */
    public void setMapType(MapType mapType)
    {
        if (!mapType.equals(getCurrentMapType()))
        {
            if (!mapType.isUnderground())
            {
                lastMapProperties.preferredMapType.set(mapType.name);
                preferredMapType = mapType;
            }
        }
        lastMapProperties.save();
        setLastMapTypeChange(mapType);
        requireRefresh();
    }

    /**
     * Gets current map type.
     *
     * @return the current map type
     */
    public MapType getCurrentMapType()
    {
        boolean showCaves = lastMapProperties.showCaves.get();
        return getMapType(showCaves);
    }

    /**
     * Gets map type.
     *
     * @param showCaves the show caves
     * @return the map type
     */
    public MapType getMapType(boolean showCaves)
    {
        MapType mapType = null;

        EntityDTO player = DataCache.getPlayer();

        if (underground && caveMappingAllowed && showCaves)
        {
            mapType = MapType.underground(DataCache.getPlayer());
        }
        else
        {
            if (preferredMapType == null)
            {
                this.preferredMapType = MapType.from(lastMapProperties.preferredMapType.get(), DataCache.getPlayer());
            }
            mapType = preferredMapType;
        }

        if (!Objects.equal(mapType, lastMapType))
        {
            setLastMapTypeChange(mapType);
        }

        return mapType;
    }

    /**
     * Gets last map type change.
     *
     * @return the last map type change
     */
    public long getLastMapTypeChange()
    {
        return lastMapTypeChange;
    }

    private void setLastMapTypeChange(MapType mapType)
    {
        if (lastMapType != null && !(mapType.isUnderground() && lastMapType.isUnderground()))
        {
            lastMapTypeChange = System.currentTimeMillis();
        }
        this.lastMapType = mapType;
    }

    /**
     * Is underground boolean.
     *
     * @return the boolean
     */
    public boolean isUnderground()
    {
        return underground;
    }

    /**
     * Gets world dir.
     *
     * @return the world dir
     */
    public File getWorldDir()
    {
        return worldDir;
    }

    /**
     * Gets player biome.
     *
     * @return the player biome
     */
    public String getPlayerBiome()
    {
        return playerBiome;
    }

    /**
     * Gets draw steps.
     *
     * @return the draw steps
     */
    public List<? extends DrawStep> getDrawSteps()
    {
        return drawStepList;
    }

    /**
     * Gets draw waypoint steps.
     *
     * @return the draw waypoint steps
     */
    public List<DrawWayPointStep> getDrawWaypointSteps()
    {
        return drawWaypointStepList;
    }

    /**
     * Generate draw steps.
     *
     * @param mc                    the mc
     * @param gridRenderer          the grid renderer
     * @param waypointRenderer      the waypoint renderer
     * @param radarRenderer         the radar renderer
     * @param mapProperties         the map properties
     * @param checkWaypointDistance the check waypoint distance
     */
    public void generateDrawSteps(Minecraft mc, GridRenderer gridRenderer, WaypointDrawStepFactory waypointRenderer, RadarDrawStepFactory radarRenderer, InGameMapProperties mapProperties, boolean checkWaypointDistance)
    {
        generateDrawStepsTimer.start();
        lastMapProperties = mapProperties;

        drawStepList.clear();
        drawWaypointStepList.clear();
        entityList.clear();

        ClientAPI.INSTANCE.getDrawSteps(drawStepList, gridRenderer.getUIState());

        if (FeatureManager.isAllowed(Feature.RadarAnimals))
        {
            if (mapProperties.showAnimals.get() || mapProperties.showPets.get())
            {
                entityList.addAll(DataCache.INSTANCE.getAnimals(false).values());
            }
        }
        if (FeatureManager.isAllowed(Feature.RadarVillagers))
        {
            if (mapProperties.showVillagers.get())
            {
                entityList.addAll(DataCache.INSTANCE.getVillagers(false).values());
            }
        }
        if (FeatureManager.isAllowed(Feature.RadarMobs))
        {
            if (mapProperties.showMobs.get())
            {
                entityList.addAll(DataCache.INSTANCE.getMobs(false).values());
            }
        }

        if (FeatureManager.isAllowed(Feature.RadarPlayers))
        {
            if (mapProperties.showPlayers.get())
            {
                entityList.addAll(DataCache.INSTANCE.getPlayers(false).values());
            }
        }

        // Sort to keep named entities last
        if (!entityList.isEmpty())
        {
            Collections.sort(entityList, EntityHelper.entityMapComparator);
            drawStepList.addAll(radarRenderer.prepareSteps(entityList, gridRenderer, mapProperties));
        }

        // Draw waypoints
        if (mapProperties.showWaypoints.get())
        {
            boolean showLabel = mapProperties.showWaypointLabels.get();
            drawWaypointStepList.addAll(waypointRenderer.prepareSteps(DataCache.INSTANCE.getWaypoints(false), gridRenderer, checkWaypointDistance, showLabel));
        }

        generateDrawStepsTimer.stop();
    }

    /**
     * Zoom in boolean.
     *
     * @return the boolean
     */
    public boolean zoomIn()
    {
        if (lastMapProperties.zoomLevel.get() < maxZoom)
        {
            return setZoom(lastMapProperties.zoomLevel.get() + 1);
        }
        return false;
    }

    /**
     * Zoom out boolean.
     *
     * @return the boolean
     */
    public boolean zoomOut()
    {
        if (lastMapProperties.zoomLevel.get() > minZoom)
        {
            return setZoom(lastMapProperties.zoomLevel.get() - 1);
        }
        return false;
    }

    /**
     * Sets zoom.
     *
     * @param zoom the zoom
     * @return the zoom
     */
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

    /**
     * Gets zoom.
     *
     * @return the zoom
     */
    public int getZoom()
    {
        return lastMapProperties.zoomLevel.get();
    }

    /**
     * Require refresh.
     */
    public void requireRefresh()
    {
        this.lastRefresh = 0;
    }

    /**
     * Update last refresh.
     */
    public void updateLastRefresh()
    {
        this.lastRefresh = System.currentTimeMillis();
    }

    /**
     * Should refresh boolean.
     *
     * @param mc            the mc
     * @param mapProperties the map properties
     * @return the boolean
     */
    public boolean shouldRefresh(Minecraft mc, MapProperties mapProperties)
    {
        EntityDTO player = DataCache.getPlayer();

        if (ClientAPI.INSTANCE.isDrawStepsUpdateNeeded())
        {
            return true;
        }

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

    /**
     * Is high quality boolean.
     *
     * @return the boolean
     */
    public boolean isHighQuality()
    {
        return highQuality;
    }

    /**
     * Is cave mapping allowed.
     *
     * @return the boolean
     */
    public boolean isCaveMappingAllowed()
    {
        return caveMappingAllowed;
    }

    /**
     * Is surface mapping allowed.
     *
     * @return the boolean
     */
    public boolean isSurfaceMappingAllowed()
    {
        return surfaceMappingAllowed;
    }

    /**
     * Gets dimension.
     *
     * @return the dimension
     */
    public int getDimension()
    {
        return getCurrentMapType().dimension;
    }
}
