package net.techbrew.mcjm.model;

import java.io.File;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;

public class MapOverlayState {

	private Constants.MapType mapType;
	private File worldDir;
	private Integer vSlice;
	private boolean underground;
	private int currentZoom;
	private int dimension;
	
	public MapOverlayState(
			File worldDir, 
			MapType mapType, 
			Integer vSlice, 
			boolean underground,
			int currentZoom,
			int dimension) {
		
		this.mapType = mapType;
		this.vSlice = vSlice;
		this.underground = underground;
		if(underground) vSlice = null;
		this.currentZoom = currentZoom;
		this.worldDir = worldDir;
		this.dimension = dimension;
	}

	public Constants.MapType getMapType() {
		return mapType;
	}

	public void setMapType(Constants.MapType mapType) {
		this.mapType = mapType;
	}

	public int getCurrentZoom() {
		return currentZoom;
	}

	public void setCurrentZoom(int currentZoom) {
		this.currentZoom = currentZoom;
	}

	public File getWorldDir() {
		return worldDir;
	}

	public void setWorldDir(File worldDir) {
		this.worldDir = worldDir;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public Integer getVSlice() {
		return vSlice;
	}

	public void setVSlice(Integer vSlice) {
		this.vSlice = vSlice;
	}

	public boolean isUnderground() {
		return underground;
	}

	public void setUnderground(boolean underground) {
		this.underground = underground;
		if(underground) vSlice=null;
	}

}
