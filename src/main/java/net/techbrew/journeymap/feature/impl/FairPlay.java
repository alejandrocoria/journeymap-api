/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.feature.impl;

import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager.FeatureSet;

import java.util.Set;

/**
 * Fair Play features - no Multiplayer features
 *
 * @author mwoodman
 */
public class FairPlay implements FeatureSet
{

    private final Set<Feature> features;
    private final String name = "FairPlay";

    public FairPlay()
    {
        features = Feature.getSubset(true, false);
    }

    @Override
    public Set<Feature> getFeatures()
    {
        return features;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isUnlimited()
    {
        return false;
    }

}
