package net.techbrew.journeymap.feature.impl;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager.FeatureSet;

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