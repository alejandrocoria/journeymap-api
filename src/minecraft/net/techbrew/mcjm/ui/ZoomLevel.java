package net.techbrew.mcjm.ui;

import java.awt.RenderingHints;
import java.util.LinkedList;

public class ZoomLevel implements Comparable<ZoomLevel> {

	private static final int MINSCALE = 1;
	private static final int MAXSCALE = 10;
	
	public final Integer scale;
	public final int sampling;
	public final boolean antialias;
	public final Object interpolation;
	
	public ZoomLevel(int scale) {
		this(scale, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	public ZoomLevel(int scale, int sampling, boolean antialias, Object interpolation) {
		this.scale = scale;
		this.sampling = sampling;
		this.antialias = antialias;
		this.interpolation = interpolation;
	}

	@Override
	public int compareTo(ZoomLevel other) {
		return scale.compareTo(other.scale);
	}
	
	@Override
	public String toString() {
		return "ZoomLevel [scale=" + scale + ", sampling=" + sampling //$NON-NLS-1$ //$NON-NLS-2$
				+ ", antialias=" + antialias + ", interpolation=" //$NON-NLS-1$ //$NON-NLS-2$
				+ interpolation + "]"; //$NON-NLS-1$
	}

	public static LinkedList<ZoomLevel> getLevels() {
		final LinkedList<ZoomLevel> list = new LinkedList<ZoomLevel>();
		for(int i = MAXSCALE; i>=MINSCALE; i--) {
			list.add(new ZoomLevel(i));
		}
		return list;
	}
	
	public static ZoomLevel getDefault() {
		return getLevels().get(MAXSCALE/2);
	}
		
}
