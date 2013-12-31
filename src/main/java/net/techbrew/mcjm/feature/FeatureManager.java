package net.techbrew.mcjm.feature;

import net.techbrew.mcjm.feature.impl.FairPlay;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatureManager {

	private static class Holder {
        private static final FeatureManager INSTANCE = new FeatureManager();
    }

    public static FeatureManager instance() {
        return Holder.INSTANCE;
    }
    
    private final FeatureSet featureSet;
    
    private FeatureManager() {
    	
    	FeatureSet fs = null;
		try {
			Class fsClass = Class.forName("net.techbrew.mcjm.feature.impl.Unlimited");
			fs = (FeatureSet) fsClass.newInstance();				
		} catch(Throwable e) {
			fs = new FairPlay();			
		}
		featureSet = fs;
    }
    
	public static boolean isAllowed(Feature feature) {
		return Holder.INSTANCE.featureSet.isUnlimited() 
				|| !feature.isCurrentlyRestricted() 
				|| Holder.INSTANCE.featureSet.getFeatures().contains(feature);		
	}
	
	public static Map<Feature,Boolean> getAllowedFeatures() {
		Map<Feature,Boolean> map = new HashMap<Feature,Boolean>(Feature.values().length*2);
		for(Feature feature : Feature.values()) {
			map.put(feature, isAllowed(feature));
		}
		return map;
	}
	
	public static String getFeatureSetName() {
		return instance().featureSet.getName();
	}
	
	public static interface FeatureSet {
		public Set<Feature> getFeatures();
		public String getName();
		public boolean isUnlimited();
	}

}
