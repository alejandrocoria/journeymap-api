package net.techbrew.journeymap.feature;

import com.google.common.base.Joiner;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.impl.FairPlay;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureManager {

    private static String UNLIMITED_CLASS = "net.techbrew.journeymap.feature.impl.Unlimited";

	private static class Holder {
        private static final FeatureManager INSTANCE = new FeatureManager();
    }

    public static FeatureManager instance() {
        return Holder.INSTANCE;
    }

    private final Minecraft mc = FMLClientHandler.instance().getClient();

    private final FeatureSet featureSet;

    private final Map<Feature,Boolean> effectiveFeatures;
    
    private FeatureManager() {
    	
    	FeatureSet fs = null;
		try {
			Class fsClass = Class.forName(UNLIMITED_CLASS);
			fs = (FeatureSet) fsClass.newInstance();				
		} catch(Throwable e) {
			fs = new FairPlay();			
		}
		featureSet = fs;
        effectiveFeatures = new ConcurrentHashMap<Feature, Boolean>(8,.75f,2);
        effectiveFeatures.putAll(getCoreFeatures());
    }
    
	public boolean isAllowed(Feature feature)
    {
        if(!feature.isCurrentlyRestricted(mc))
        {
            return true;
        }

        Boolean allowed = effectiveFeatures.get(feature);
        return allowed == null ? false : allowed;
	}
	
	public Map<Feature,Boolean> getAllowedFeatures() {
		Map<Feature,Boolean> map = new HashMap<Feature,Boolean>(Feature.values().length*2);
		for(Feature feature : Feature.values()) {
			map.put(feature, isAllowed(feature));
		}
		return map;
	}

    private Map<Feature,Boolean> getCoreFeatures() {
        Map<Feature,Boolean> map = new HashMap<Feature,Boolean>(Feature.values().length*2);
        for(Feature feature : Feature.values()) {
            map.put(feature, isAllowed(feature));
        }
        return map;
    }
	
	public String getFeatureSetName() {
		return instance().featureSet.getName();
	}

    public void forceFairPlay()
    {
        JourneyMap.getLogger().info("FeatureManager: Forcing FairPlay");
        Map<Feature, Boolean> overrides = new HashMap<Feature, Boolean>();
        Set<Feature> allowed = new FairPlay().getFeatures();
        for(Feature feature : Feature.values())
        {
            overrides.put(feature, allowed.contains(feature));
        }
        overrideFeatures(overrides);
    }

    public void resetOverrides()
    {
        JourneyMap.getLogger().info("FeatureManager: Resetting Overrides");
        synchronized (effectiveFeatures)
        {
            effectiveFeatures.clear();
            effectiveFeatures.putAll(getCoreFeatures());
        }
    }

    public void overrideFeatures(Map<Feature,Boolean> overrides)
    {
        if(overrides==null || overrides.isEmpty())
        {
            JourneyMap.getLogger().warning("Ignoring empty overrides.");
            return;
        }

        try
        {
            Map<Feature, Boolean> updatedFeatures = instance().getCoreFeatures();
            updatedFeatures.putAll(overrides);

            List<String> allowed = new ArrayList<String>(overrides.size());
            List<String> disallowed = new ArrayList<String>(overrides.size());

            for (Map.Entry<Feature, Boolean> effective : updatedFeatures.entrySet())
            {
                if (effective.getValue())
                {
                    allowed.add(effective.getKey().getLocalizedName());
                }
                else
                {
                    disallowed.add(effective.getKey().getLocalizedName());
                }
            }

            synchronized (instance().effectiveFeatures)
            {
                instance().effectiveFeatures.clear();
                instance().effectiveFeatures.putAll(updatedFeatures);
            }

            if(!allowed.isEmpty())
            {
                ChatLog.announceI18N("Feature.server_enabled_message", Joiner.on(", ").join(allowed));
            }

            if(!disallowed.isEmpty())
            {
                ChatLog.announceI18N("Feature.server_disabled_message", Joiner.on(", ").join(disallowed));
            }
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().severe("Error trying to override features: " + LogFormatter.toString(t));
        }
    }
	
	public static interface FeatureSet {
		public Set<Feature> getFeatures();
		public String getName();
        public String getLocalizedName();
	}

}
