package net.techbrew.journeymap.feature.impl;

import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager.FeatureSet;

import java.util.EnumSet;

/**
 * Unlimited features.
 * @author mwoodman
 *
 */
public class Unlimited implements FeatureSet {
	
	private final EnumSet<Feature> features;
	private final String name = "Unlimited";

	public Unlimited() {
		features = EnumSet.allOf(Feature.class);
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
