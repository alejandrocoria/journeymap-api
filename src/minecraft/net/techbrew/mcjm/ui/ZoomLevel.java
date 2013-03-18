package net.techbrew.mcjm.ui;

import java.awt.RenderingHints;
import java.util.LinkedList;

public class ZoomLevel implements Comparable<ZoomLevel> {

	public final Integer scale;
	public final int sampling;
	public final boolean antialias;
	public final Object interpolation;
	
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
		list.add(new ZoomLevel(12, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(11, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(10, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(9, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(8, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(7, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(6, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(5, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(4, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(3, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(2, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(1, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		return list;
	}
		
}
