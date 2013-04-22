package net.techbrew.mcjm.model;

import java.io.File;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.ui.ZoomLevel;

public class MapOverlayState {

	private Constants.MapType mapType;
	private ZoomLevel currentZoom;
	private File worldDir;
	private int canvasWidth;
	private int canvasHeight;
	private int blockXOffset;
	private int blockZOffset;
	
	public MapOverlayState(MapType mapType, ZoomLevel currentZoom,
			File worldDir, int canvasWidth, int canvasHeight, int blockXOffset,
			int blockZOffset) {
		super();
		this.mapType = mapType;
		this.currentZoom = currentZoom;
		this.worldDir = worldDir;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.blockXOffset = blockXOffset;
		this.blockZOffset = blockZOffset;
	}

	public Constants.MapType getMapType() {
		return mapType;
	}

	public void setMapType(Constants.MapType mapType) {
		this.mapType = mapType;
	}

	public ZoomLevel getCurrentZoom() {
		return currentZoom;
	}

	public void setCurrentZoom(ZoomLevel currentZoom) {
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
	
	
	
}
