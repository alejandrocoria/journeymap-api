/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.feature;

import com.google.common.reflect.ClassPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Governs what features are available at runtime.
 */
public class FeatureManager
{
    private static final String NAME_FAIRPLAY = "FairPlay";
    private static final String IMPL_PACKAGE = "net.techbrew.journeymap.feature.impl";
    private static final String CLASS_UNLIMITED = String.format("%s.Unlimited", IMPL_PACKAGE);
    private final PolicySet policySet;
    private final HashMap<Feature, Policy> policyMap = new HashMap<Feature, Policy>();

    /**
     * Private constructure.  Use instance()
     */
    private FeatureManager()
    {
        policySet = locatePolicySet();
        for (Policy policy : policySet.getPolicies())
        {
            policyMap.put(policy.feature, policy);
        }
    }

    /**
     * Gets a detailed description of all policies.
     */
    public static String getPolicyDetails()
    {
        StringBuilder sb = new StringBuilder(String.format("%s Features: ", getPolicySetName()));
        for (Feature feature : Feature.values())
        {
            boolean single = false;
            boolean multi = false;
            if(Holder.INSTANCE.policyMap.containsKey(feature))
            {
                single = Holder.INSTANCE.policyMap.get(feature).allowInSingleplayer;
                multi = Holder.INSTANCE.policyMap.get(feature).allowInMultiplayer;
            }

            sb.append(String.format("\n\t%s : singleplayer = %s , multiplayer = %s", feature.name(), single, multi));
        }
        return sb.toString();
    }

    /**
     * Gets the singleton.
     */
    public static FeatureManager instance()
    {
        return Holder.INSTANCE;
    }

    /**
     * Whether the specified feature is currently permitted.
     * @param feature the feature to check
     * @return  true if permitted
     */
    public static boolean isAllowed(Feature feature)
    {
        Policy policy = Holder.INSTANCE.policyMap.get(feature);
        return (policy != null) && policy.isCurrentlyAllowed();
    }

    /**
     * Returns a map of all features and whether they are currently permitted.
     * @return
     */
    public static Map<Feature, Boolean> getAllowedFeatures()
    {
        Map<Feature, Boolean> map = new HashMap<Feature, Boolean>(Feature.values().length * 2);
        for (Feature feature : Feature.values())
        {
            map.put(feature, isAllowed(feature));
        }
        return map;
    }

    /**
     * Gets the name of the PolicySet.
     * @return
     */
    public static String getPolicySetName()
    {
        return instance().policySet.getName();
    }


    /**
     * Finds the FeatureSet via reflection.
     * @return
     */
    private PolicySet locatePolicySet()
    {
        PolicySet fs = null;
        try
        {
            ClassPath cp = ClassPath.from(getClass().getClassLoader());
            Set<ClassPath.ClassInfo> classInfos = cp.getTopLevelClasses(IMPL_PACKAGE);
            if (classInfos.size() > 1)
            {
                try
                {
                    Class fsClass = Class.forName(CLASS_UNLIMITED);
                    fs = (PolicySet) fsClass.newInstance();
                }
                catch (Throwable e)
                {
                }
            }

            if (fs == null)
            {
                for (ClassPath.ClassInfo classInfo : classInfos)
                {
                    Class aClass = classInfo.load();
                    if (PolicySet.class.isAssignableFrom(aClass))
                    {
                        fs = (PolicySet) aClass.newInstance();
                        break;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }

        return (fs != null) ? fs : createFairPlay();
    }

    /**
     * Generates a FeatureSet that disables all features in multiplayer.
     * @return
     */
    private PolicySet createFairPlay()
    {
        return new PolicySet()
        {
            // All features allowed in singleplayer, but none in multiplayer
            private final Set<Policy> policies = Policy.bulkCreate(true, false);
            private final String name = NAME_FAIRPLAY;

            @Override
            public Set<Policy> getPolicies()
            {
                return policies;
            }

            @Override
            public String getName()
            {
                return name;
            }

        };
    }

    /**
     * Interface for a named set of Policies.
     */
    public static interface PolicySet
    {
        public Set<Policy> getPolicies();

        public String getName();
    }

    /**
     * Instance holder.
     */
    private static class Holder
    {
        private static final FeatureManager INSTANCE = new FeatureManager();
    }

}
