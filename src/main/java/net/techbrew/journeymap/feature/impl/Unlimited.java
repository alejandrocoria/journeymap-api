package net.techbrew.journeymap.feature.impl;

import net.techbrew.journeymap.Constants;
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
    private static final String localizedName = "Unlimited";
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
    public String getLocalizedName() {
        return localizedName;
    }
}
