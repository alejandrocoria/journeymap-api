package net.techbrew.mcjm.feature.impl;

import java.util.EnumSet;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager.FeatureSet;

/**
 * Fair Play features - no Multiplayer features
 * @author mwoodman
 *
 */
public class FairPlay implements FeatureSet {

	@Override
	public EnumSet<Feature> getFeatures() {
		return EnumSet.noneOf(Feature.class);
	}

	@Override
	public String getName() {
		return Constants.getString("Feature.fair_play");
	}

}
