package net.techbrew.mcjm.ui;

public class ScreenPosition {

	private static final double perPixel = 1.0/Tiles.TILESIZE; 
	
	public final double screenX;
	public final double screenZ;
	
	public ScreenPosition(final double screenX, final double screenZ) {
		this.screenX = screenX;
		this.screenZ = screenZ;
	}
	
	public static ScreenPosition fromBlockPosition(final int blockX, final int blockZ) {
		double center = 0.5 * perPixel;
		double screenX = (blockX * perPixel) + center;
		double screenZ = (blockZ * perPixel) + center;	
		return new ScreenPosition(screenX, screenZ);
	}

}
