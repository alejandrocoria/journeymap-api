/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;


import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.io.FileHandler;
import journeymap.client.log.StatTimer;
import journeymap.client.properties.CoreProperties;
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
import journeymap.common.properties.config.IntegerField;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static journeymap.common.properties.Category.Hidden;

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

    private StatTimer refreshTimer = StatTimer.get("MapState.refresh");
    private StatTimer generateDrawStepsTimer = StatTimer.get("MapState.generateDrawSteps");

    // These must be internally managed
    private MapType lastMapType;
    private File worldDir = null;
    private long lastRefresh = 0;
    private long lastMapTypeChange = 0;

    private IntegerField lastSlice = new IntegerField(Hidden, "", 0, 15, 4);
    private boolean surfaceMappingAllowed = false;
    private boolean caveMappingAllowed = false;
    private boolean caveMappingEnabled = false;
    private boolean topoMappingAllowed = false;
    private List<DrawStep> drawStepList = new ArrayList<DrawStep>();
    private List<DrawWayPointStep> drawWaypointStepList = new ArrayList<DrawWayPointStep>();
    private String playerBiome = "";
    private InGameMapProperties lastMapProperties = null;
    private List<EntityDTO> entityList = new ArrayList<EntityDTO>(32);
    private int lastPlayerChunkX = 0;
    private int lastPlayerChunkY = 0;
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
        World world = mc.world;
        if (world == null || world.provider == null)
        {
            return;
        }

        refreshTimer.start();
        try
        {
            CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
            lastMapProperties = mapProperties;
            this.worldDir = FileHandler.getJMWorldDir(mc);
            if (world != null && world.getActualHeight() != 256 && lastSlice.getMaxValue() != 15)
            {
                int maxSlice = (world.getActualHeight() / 16) - 1;
                int seaLevel = Math.round(world.getSeaLevel() / 16);
                int currentSlice = lastSlice.get();
                lastSlice = new IntegerField(Hidden, "", 0, maxSlice, seaLevel);
                lastSlice.set(currentSlice);
            }

            boolean hasSurface = !(world.provider instanceof WorldProviderHell);
            this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
            this.caveMappingEnabled = caveMappingAllowed && mapProperties.showCaves.get();
            this.surfaceMappingAllowed = hasSurface && FeatureManager.isAllowed(Feature.MapSurface);
            this.topoMappingAllowed = hasSurface && FeatureManager.isAllowed(Feature.MapTopo) && coreProperties.mapTopography.get();
            highQuality = coreProperties.tileHighDisplayQuality.get();
            lastPlayerChunkX = player.chunkCoordX;
            lastPlayerChunkY = player.chunkCoordY;
            lastPlayerChunkZ = player.chunkCoordZ;
            EntityDTO playerDTO = DataCache.getPlayer();
            playerBiome = playerDTO.biome;

            if (lastMapType != null)
            {
                if (player.dimension != lastMapType.dimension)
                {
                    lastMapType = null;
                }
                else if (caveMappingEnabled && follow.get() && playerDTO.underground && !lastMapType.isUnderground())
                {
                    lastMapType = null;
                }
                else if (!lastMapType.isAllowed())
                {
                    lastMapType = null;
                }
            }

            lastMapType = getMapType();

            updateLastRefresh();
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error refreshing MapState: " + LogFormatter.toPartialString(e));
        }
        finally
        {
            refreshTimer.stop();
        }
    }

    /**
     * Sets map type.
     *
     * @param mapTypeName the map type name
     */
    public MapType setMapType(MapType.Name mapTypeName)
    {
        return setMapType(MapType.from(mapTypeName, DataCache.getPlayer()));
    }

    /**
     * Toggle map type to next viable type
     */
    public MapType toggleMapType()
    {
        MapType.Name next = getNextMapType(getMapType().name);
        return setMapType(next);
    }

    /**
     * Provides the next MapType viable for the current world and permissions.
     *
     * @param name MapType.Name
     */
    public MapType.Name getNextMapType(MapType.Name name)
    {
        final EntityDTO player = DataCache.getPlayer();
        final EntityLivingBase playerEntity = player.entityLivingRef.get();
        if (playerEntity == null)
        {
            return name;
        }

        List<MapType.Name> types = new ArrayList<>(4);

        if (this.surfaceMappingAllowed)
        {
            types.add(MapType.Name.day);
            types.add(MapType.Name.night);
        }

        if (caveMappingAllowed && (player.underground || name == MapType.Name.underground))
        {
            types.add(MapType.Name.underground);
        }

        if (topoMappingAllowed)
        {
            types.add(MapType.Name.topo);
        }

        if (name == MapType.Name.none && !types.isEmpty())
        {
            return types.get(0);
        }

        if (types.contains(name))
        {
            Iterator<MapType.Name> cyclingIterator = Iterables.cycle(types).iterator();
            while (cyclingIterator.hasNext())
            {
                MapType.Name current = cyclingIterator.next();
                if (current == name)
                {
                    return cyclingIterator.next();
                }
            }
        }

        // If all else fails
        return name;
    }

    /**
     * Sets map type.
     *
     * @param mapType the map type
     */
    public MapType setMapType(MapType mapType)
    {
        if (!mapType.isAllowed())
        {
            mapType = MapType.from(getNextMapType(mapType.name), DataCache.getPlayer());

            if (!mapType.isAllowed())
            {
                mapType = MapType.none();
            }
        }

        EntityDTO player = DataCache.getPlayer();
        if (player.underground != mapType.isUnderground())
        {
            follow.set(false);
        }

        if (mapType.isUnderground())
        {
            if (player.chunkCoordY != mapType.vSlice)
            {
                follow.set(false);
            }
            lastSlice.set(mapType.vSlice);
        }
        else if (mapType.name != MapType.Name.none && lastMapProperties.preferredMapType.get() != mapType.name)
        {
            lastMapProperties.preferredMapType.set(mapType.name);
            lastMapProperties.save();
        }

        setLastMapTypeChange(mapType);

        return lastMapType;
    }

    /**
     * Gets map type.
     *
     * @return the map type
     */
    public MapType getMapType()
    {
        if (lastMapType == null)
        {
            EntityDTO player = DataCache.getPlayer();
            MapType mapType = null;
            try
            {
                if (caveMappingEnabled && player.underground)
                {
                    mapType = MapType.underground(player);
                }
                else if (follow.get())
                {
                    if (surfaceMappingAllowed && !player.underground)
                    {
                        mapType = MapType.day(player);
                    }
                }

                if (mapType == null)
                {
                    mapType = MapType.from(lastMapProperties.preferredMapType.get(), player);
                }
            }
            catch (Exception e)
            {
                mapType = MapType.day(player);
            }

            setMapType(mapType);
        }

        return lastMapType;
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
        if (!Objects.equal(mapType, lastMapType))
        {
            lastMapTypeChange = System.currentTimeMillis();
            requireRefresh();
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
        return getMapType().isUnderground();
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
        if (ClientAPI.INSTANCE.isDrawStepsUpdateNeeded())
        {
            return true;
        }

        if (MapPlayerTask.getlastTaskCompleted() - lastRefresh > 500)
        {
            return true;
        }

        if (lastMapType == null)
        {
            return true;
        }

        EntityDTO player = DataCache.getPlayer();

        if (this.getMapType().dimension != player.dimension)
        {
            return true;
        }

        // Check for a big jump in coords (like a teleport)
        double d0 = lastPlayerChunkX - player.chunkCoordX;
        double d1 = lastPlayerChunkY - player.chunkCoordY;
        double d2 = lastPlayerChunkZ - player.chunkCoordZ;
        double diff = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        if (diff > 2)
        {
            return true;
        }

        if (lastMapProperties == null || !lastMapProperties.equals(mapProperties))
        {
            return true;
        }

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
     * Is cave mapping enabled.
     *
     * @return the boolean
     */
    public boolean isCaveMappingEnabled()
    {
        return caveMappingEnabled;
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
     * Is topo mapping allowed
     *
     * @return
     */
    public boolean isTopoMappingAllowed()
    {
        return topoMappingAllowed;
    }

    /**
     * Gets dimension.
     *
     * @return the dimension
     */
    public int getDimension()
    {
        return getMapType().dimension;
    }

    public IntegerField getLastSlice()
    {
        return lastSlice;
    }

    public void resetMapType()
    {
        lastMapType = null;
    }
}
