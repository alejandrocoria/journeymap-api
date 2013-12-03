package net.techbrew.mcjm.model;

import java.io.File;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;

public class MapOverlayState {

	// One-time setup
	final long refreshInterval = PropertyManager.getIntegerProp(PropertyManager.Key.UPDATETIMER_CHUNKS);
	
	// These can be safely changed at will
	public boolean follow;
	public int currentZoom;
	public String playerLastPos = "0,0"; //$NON-NLS-1$
	

	// These must be internally managed
	private boolean hardcore = true;
	private Constants.MapType overrideMapType;
	private File worldDir = null;
	private long lastRefresh = 0;
	private Integer vSlice = null;
	private boolean underground = false;
	private int dimension = Integer.MIN_VALUE;
	
	/**
	 * Default constructor
	 */
	public MapOverlayState() {
		
		// Set preferences-based values
		
	}
	
	public void refresh(Minecraft mc, EntityClientPlayerMP player) {
	
		final MapType lastMapType = getMapType();
		final boolean lastUnderground = this.underground;				
		final int lastDimension = this.currentZoom;
		final File lastWorldDir = this.worldDir;
				
		this.hardcore = WorldData.isHardcoreAndMultiplayer();
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
						
		updateLastRefresh();
	}
	
	public void overrideMapType(MapType mapType) {
		overrideMapType = mapType;
	}
	
	public MapType getMapType() {
		if(underground && PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_CAVES) && !hardcore) {
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
	
	public Boolean getHardcore() {
		return hardcore;
	}

	public int getDimension() {
		return dimension;
	}
	
	public File getWorldDir() {
		return worldDir;
	}

	public void requireRefresh() {
		this.lastRefresh = 0;
	}
	
	public void updateLastRefresh() {
		this.lastRefresh = System.currentTimeMillis();
	}
	
	public boolean shouldRefresh() {
		return System.currentTimeMillis() > (lastRefresh+refreshInterval);
	}
	
}
