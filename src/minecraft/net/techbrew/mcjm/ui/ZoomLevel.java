package net.techbrew.mcjm.ui;

import java.awt.RenderingHints;
import java.util.LinkedList;

public class ZoomLevel implements Comparable<ZoomLevel> {

	public final Float scale;
	public final int sampling;
	public final boolean antialias;
	public final Object interpolation;
	
	public ZoomLevel(float scale, int sampling, boolean antialias, Object interpolation) {
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
		list.add(new ZoomLevel(8, 1, true, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(4, 1, true, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(2, 1, true, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
		list.add(new ZoomLevel(1.5F, 1, true, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
		list.add(new ZoomLevel(1.0F, 1, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(0.75F, 2, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(0.50F, 4, true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		list.add(new ZoomLevel(0.45F, 16, false, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(0.40F, 16, false, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(0.35F, 16, false, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(0.30F, 16, false, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(0.25F, 16, false, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		list.add(new ZoomLevel(0.20F, 64, false, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		return list;
	}
		
}
