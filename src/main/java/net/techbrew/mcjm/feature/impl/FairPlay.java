package net.techbrew.mcjm.feature.impl;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager.FeatureSet;

import java.util.Set;

/**
 * Fair Play features - no Multiplayer features
 * @author mwoodman
 *
 */
public class FairPlay implements FeatureSet {

	private final Set<Feature> features;
	private final String name;

	public FairPlay() {
		features = Feature.getSubset(true, false);
		name = Constants.getString("Feature.fair_play");
	}
	
	@Override
	public Set<Feature> getFeatures() {
		return features;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isUnlimited() {
		return false;
	}

}
