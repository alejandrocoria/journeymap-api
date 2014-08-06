/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.feature.impl;

import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.feature.Policy2;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Radar disabled in multiplayer.
 */
public class NoRadar implements FeatureManager.PolicySet
{
    private final Set<Policy2> policies;
    private final String name = "NoRadar";

    public NoRadar()
    {
        EnumSet<Feature> radar = Feature.radar();
        EnumSet<Feature> nonRadar = Feature.all();
        nonRadar.removeAll(radar);

        policies = Policy2.bulkCreate(radar, true, false);
        policies.addAll(Policy2.bulkCreate(nonRadar, true, true));
    }

    @Override
    public Set<Policy2> getPolicies()
    {
        return Collections.unmodifiableSet(policies);
    }

    @Override
    public String getName()
    {
        return name;
    }
}
