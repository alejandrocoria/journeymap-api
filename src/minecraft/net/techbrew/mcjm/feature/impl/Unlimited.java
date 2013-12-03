package net.techbrew.mcjm.feature.impl;

import java.util.EnumSet;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager.FeatureSet;

/**
 * Unlimited features - "classic" JourneyMap behavior disregarding hardcore worlds.
 * @author mwoodman
 *
 */
public class Unlimited implements FeatureSet {

	@Override
	public EnumSet<Feature> getFeatures() {
		return EnumSet.allOf(Feature.class);
	}
	
	@Override
	public String getName() {
		return Constants.getString("Feature.unlimited");
	}

}
