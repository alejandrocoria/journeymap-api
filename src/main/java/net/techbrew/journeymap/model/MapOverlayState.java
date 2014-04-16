package net.techbrew.journeymap.model;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.data.*;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;
import net.techbrew.journeymap.waypoint.WaypointHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

;

public class MapOverlayState {

	// One-time setup
	final long refreshInterval = PropertyManager.getIntegerProp(PropertyManager.Key.UPDATETIMER_CHUNKS);
    public final int minZoom = 0;
    public final int maxZoom = 5;
	
	// These can be safely changed at will
	public boolean follow = true;
	public int currentZoom;
    public double fontScale = 1;
	public String playerLastPos = "0,0"; //$NON-NLS-1$
    public boolean minimapEnabled = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_MINIMAP);
    public boolean minimapHotkeys = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_HOTKEYS);
	
	// These must be internally managed
	private Constants.MapType overrideMapType;
	private File worldDir = null;
	private long lastRefresh = 0;
	private Integer vSlice = null;
	private boolean underground = false;
	private int dimension = Integer.MIN_VALUE;
	private boolean caveMappingAllowed = false;
    private List<DrawStep> drawStepList = new ArrayList<DrawStep>();
    private String playerBiome = "";
	
	/**
	 * Default constructor
	 */
	public MapOverlayState() {
	}
	
	public void refresh(Minecraft mc, EntityClientPlayerMP player) {

        this.minimapEnabled = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_MINIMAP);
        this.minimapHotkeys = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_HOTKEYS);

		final MapType lastMapType = getMapType();
		final boolean lastUnderground = this.underground;				
		final int lastDimension = this.currentZoom;
		final File lastWorldDir = this.worldDir;
				
		this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
		this.dimension = player.dimension;
		this.underground = (Boolean) DataCache.playerDataValue(EntityKey.underground);
		this.vSlice = this.underground ? player.chunkCoordY : null;
		this.worldDir = FileHandler.getJMWorldDir(mc);

		if(player.dimension!=this.dimension) {
			follow = true;
		} else if(!worldDir.equals(this.worldDir)) {
			follow=true;
		} else if(getMapType()==MapType.underground && lastMapType!=MapType.underground) {
			follow = true;
		}

        playerBiome = (String) DataCache.instance().get(PlayerData.class).get(EntityKey.biome);
						
		updateLastRefresh();
	}
	
	public void overrideMapType(MapType mapType) {
		overrideMapType = mapType;
        requireRefresh();
	}
	
	public MapType getMapType() {
		if(underground && caveMappingAllowed && PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_CAVES)) {
			return MapType.underground;
		} else if(overrideMapType!=null) {
			return overrideMapType;			
		} else {
			final long ticks = (Minecraft.getMinecraft().theWorld.getWorldTime() % 24000L);
			return (ticks<13800) ? Constants.MapType.day : Constants.MapType.night;				
		}
	}
	
	public Integer getVSlice() {
		return vSlice;
	}

	public boolean isUnderground() {
		return underground;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public File getWorldDir() {
		return worldDir;
	}

    public String getPlayerBiome() {
        return playerBiome;
    }

    public List<DrawStep> getDrawSteps() {
        return drawStepList;
    }

    public void generateDrawSteps(Minecraft mc, GridRenderer gridRenderer, OverlayWaypointRenderer waypointRenderer, OverlayRadarRenderer radarRenderer, float drawScale) {
        drawStepList.clear();

        List<Map> entities = new ArrayList<Map>(16);
        PropertyManager pm = PropertyManager.getInstance();
        if(this.currentZoom>0) {
            if(FeatureManager.isAllowed(Feature.RadarAnimals)) {
                if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_ANIMALS) || pm.getBoolean(PropertyManager.Key.PREF_SHOW_PETS)) {
                    Map map = (Map) DataCache.instance().get(AnimalsData.class).get(EntityKey.root);
                    entities.addAll(map.values());
                }
            }
            if(FeatureManager.isAllowed(Feature.RadarVillagers)) {
                if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_VILLAGERS)) {
                    Map map = (Map) DataCache.instance().get(VillagersData.class).get(EntityKey.root);
                    entities.addAll(map.values());
                }
            }
            if(FeatureManager.isAllowed(Feature.RadarMobs)) {
                if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_MOBS)) {
                    Map map = (Map) DataCache.instance().get(MobsData.class).get(EntityKey.root);
                    entities.addAll(map.values());
                }
            }
        }

        if(FeatureManager.isAllowed(Feature.RadarPlayers)) {
            if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_PLAYERS)) {
                Map map = (Map) DataCache.instance().get(PlayersData.class).get(EntityKey.root);
                entities.addAll(map.values());
            }
        }

        // Sort to keep named entities last
        if(!entities.isEmpty()) {
            Collections.sort(entities, new EntityHelper.EntityMapComparator());
            drawStepList.addAll(radarRenderer.prepareSteps(entities, gridRenderer, fontScale, drawScale));
        }

        // Draw waypoints
        if(WaypointHelper.waypointsEnabled() && PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_WAYPOINTS)) {
            Map map = (Map) DataCache.instance().get(WaypointsData.class).get(EntityKey.root);
            List<Waypoint> waypoints = new ArrayList<Waypoint>(map.values());

            drawStepList.addAll(waypointRenderer.prepareSteps(waypoints, gridRenderer, fontScale));
        }
    }

    public boolean zoomIn(){
        if(currentZoom<maxZoom){
            return setZoom(currentZoom+1);
        }
        return false;
    }

    public boolean zoomOut(){
        if(currentZoom>minZoom){
            return setZoom(currentZoom-1);
        }
        return false;
    }

    public boolean setZoom(int zoom) {
        if(zoom>maxZoom || zoom<minZoom || zoom==currentZoom) {
            return false;
        }
        currentZoom = zoom;
        requireRefresh();
        return true;
    }

    public void requireRefresh() {
		this.lastRefresh = 0;
	}
	
	public void updateLastRefresh() {
		this.lastRefresh = System.currentTimeMillis();
	}
	
	public boolean shouldRefresh(Minecraft mc) {

		if(System.currentTimeMillis() > (lastRefresh+refreshInterval)) {
            return true;
        }

        if(this.dimension != mc.theWorld.provider.dimensionId) {
            return true;
        }

        if(this.underground != (Boolean) DataCache.playerDataValue(EntityKey.underground))
        {
            return true;
        }

        return false;
	}
	
}
