package net.techbrew.mcjm.feature.impl;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager.FeatureSet;

import java.util.EnumSet;

/**
 * Unlimited features.
 * @author mwoodman
 *
 */
public class Unlimited implements FeatureSet {
	
	private final EnumSet<Feature> features;
	private final String name;

	public Unlimited() {
		features = EnumSet.allOf(Feature.class);
		name = Constants.getString("Feature.unlimited");
	}
	
	@Override
	public EnumSet<Feature> getFeatures() {
		return features;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isUnlimited() {
		return true;
	}

}
