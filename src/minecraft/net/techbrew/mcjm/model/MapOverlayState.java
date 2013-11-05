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
	private int canvasWidth;
	private int canvasHeight;
	private int blockXOffset;
	private int blockZOffset;
	private int dimension;
	
	public MapOverlayState(File worldDir, MapType mapType, Integer vSlice, 
			boolean underground,
			int currentZoom,
			int canvasWidth, int canvasHeight, int blockXOffset,
			int blockZOffset, int dimension) {
		super();
		this.mapType = mapType;
		this.vSlice = vSlice;
		this.underground = underground;
		if(underground) vSlice = null;
		this.currentZoom = currentZoom;
		this.worldDir = worldDir;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.blockXOffset = blockXOffset;
		this.blockZOffset = blockZOffset;
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

	public int getCanvasWidth() {
		return canvasWidth;
	}

	public void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	public int getCanvasHeight() {
		return canvasHeight;
	}

	public void setCanvasHeight(int canvasHeight) {
		this.canvasHeight = canvasHeight;
	}

	public int getBlockXOffset() {
		return blockXOffset;
	}

	public void setBlockXOffset(int blockXOffset) {
		this.blockXOffset = blockXOffset;
	}

	public int getBlockZOffset() {
		return blockZOffset;
	}

	public void setBlockZOffset(int blockZOffset) {
		this.blockZOffset = blockZOffset;
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
