package net.techbrew.journeymap.model;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.properties.CoreProperties;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapOverlayState
{

    // One-time setup
    final CoreProperties coreProperties = JourneyMap.getInstance().coreProperties;

    public final int minZoom = 0;
    public final int maxZoom = 5;

    // These can be safely changed at will
    public boolean follow = true;

    public String playerLastPos = "0,0"; //$NON-NLS-1$

    // These must be internally managed
    private Constants.MapType overrideMapType;
    private File worldDir = null;
    private long lastRefresh = 0;
    private Integer vSlice = null;
    private boolean underground = false;
    private int dimension = Integer.MIN_VALUE;
    private boolean caveMappingAllowed = false;
    private List<DrawStep> drawStepList = new ArrayList<DrawStep>();
    private List<DrawWayPointStep> drawWaypointStepList = new ArrayList<DrawWayPointStep>();
    private String playerBiome = "";
    private MapProperties lastMapProperties = null;

    /**
     * Default constructor
     */
    public MapOverlayState()
    {
    }

    public void refresh(Minecraft mc, EntityClientPlayerMP player, MapProperties mapProperties)
    {
        boolean showCaves = JourneyMap.getInstance().fullMapProperties.showCaves.get();
        final MapType lastMapType = getMapType(showCaves);
        lastMapProperties = mapProperties;

        this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
        this.dimension = player.dimension;
        this.underground = DataCache.getPlayer().underground;
        this.vSlice = this.underground ? player.chunkCoordY : null;
        this.worldDir = FileHandler.getJMWorldDir(mc);

        if (player.dimension != this.dimension)
        {
            follow = true;
        }
        else
        {
            if (!worldDir.equals(this.worldDir))
            {
                follow = true;
            }
            else
            {
                if (getMapType(showCaves) == MapType.underground && lastMapType != MapType.underground)
                {
                    follow = true;
                }
            }
        }

        playerBiome = DataCache.getPlayer().biome;

        updateLastRefresh();
    }

    public void overrideMapType(MapType mapType)
    {
        overrideMapType = mapType;
        requireRefresh();
    }

    public MapType getMapType(boolean allowCaves)
    {
        if (underground && caveMappingAllowed && allowCaves)
        {
            return MapType.underground;
        }
        else
        {
            if (overrideMapType != null)
            {
                return overrideMapType;
            }
            else
            {
                final long ticks = (FMLClientHandler.instance().getClient().theWorld.getWorldTime() % 24000L);
                return (ticks < 13800) ? Constants.MapType.day : Constants.MapType.night;
            }
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

    public void generateDrawSteps(Minecraft mc, GridRenderer gridRenderer, OverlayWaypointRenderer waypointRenderer, OverlayRadarRenderer radarRenderer, MapProperties mapProperties, float drawScale, boolean checkWaypointDistance)
    {
        lastMapProperties = mapProperties;

        drawStepList.clear();
        drawWaypointStepList.clear();

        List<EntityDTO> entities = new ArrayList<EntityDTO>(32);
        if(mapProperties.zoomLevel.get()==0)
        {
            drawScale = drawScale*.5f;
        }

        if (FeatureManager.isAllowed(Feature.RadarAnimals))
        {
            if (mapProperties.showAnimals.get() || mapProperties.showPets.get())
            {
                entities.addAll(DataCache.instance().getAnimals(false).values());
            }
        }
        if (FeatureManager.isAllowed(Feature.RadarVillagers))
        {
            if (mapProperties.showVillagers.get())
            {
                entities.addAll(DataCache.instance().getVillagers(false).values());
            }
        }
        if (FeatureManager.isAllowed(Feature.RadarMobs))
        {
            if (mapProperties.showMobs.get())
            {
                entities.addAll(DataCache.instance().getMobs(false).values());
            }
        }


        if (FeatureManager.isAllowed(Feature.RadarPlayers))
        {
            if (mapProperties.showPlayers.get())
            {
                entities.addAll(DataCache.instance().getPlayers(false).values());
            }
        }

        // Sort to keep named entities last
        if (!entities.isEmpty())
        {
            Collections.sort(entities, new EntityHelper.EntityMapComparator());
            drawStepList.addAll(radarRenderer.prepareSteps(entities, gridRenderer, drawScale, mapProperties));
        }

        // Draw waypoints
        if (mapProperties.showWaypoints.get())
        {
            List<Waypoint> waypoints = new ArrayList<Waypoint>(DataCache.instance().getWaypoints(false));
            drawWaypointStepList.addAll(waypointRenderer.prepareSteps(waypoints, gridRenderer, checkWaypointDistance));
        }
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

        if (System.currentTimeMillis() > (lastRefresh + 500 + coreProperties.chunkPoll.get()))
        {
            return true;
        }

        if (this.dimension != mc.theWorld.provider.dimensionId)
        {
            return true;
        }

        if (this.underground != DataCache.getPlayer().underground)
        {
            return true;
        }

        if(lastMapProperties == null || !lastMapProperties.equals(mapProperties))
        {
            return true;
        }

        return false;
    }

}
