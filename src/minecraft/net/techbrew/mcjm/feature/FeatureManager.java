package net.techbrew.mcjm.feature;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.feature.impl.FairPlay;

public class FeatureManager {

	private static final EnumSet<Feature> features = EnumSet.noneOf(Feature.class);
	private static String featureSetName;
	static {
		FeatureSet fs;
		try {
			Class fsClass = Class.forName("net.techbrew.mcjm.feature.impl.Unlimited");
			fs = (FeatureSet) fsClass.newInstance();				
		} catch(Throwable e) {
			fs = new FairPlay();			
		}
		featureSetName = fs.getName();
		features.clear();
		features.addAll(fs.getFeatures());
		JourneyMap.getLogger().info("Loaded FeatureSet: " + featureSetName);
	}
	
	public static boolean isAllowed(Feature feature) {
		return !feature.isCurrentlyRestricted() || features.contains(feature);		
	}
	
	public static Map<Feature,Boolean> getAllowedFeatures() {
		Map<Feature,Boolean> map = new HashMap<Feature,Boolean>(Feature.values().length*2);
		for(Feature feature : Feature.values()) {
			map.put(feature, isAllowed(feature));
		}
		return map;
	}
	
	public String getFeatureSetName() {
		return featureSetName;
	}
	
	public static interface FeatureSet {
		public EnumSet<Feature> getFeatures();
		public String getName();
	}

}
