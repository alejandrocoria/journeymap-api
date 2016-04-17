/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.feature.impl;

import journeymap.client.feature.FeatureManager;
import journeymap.client.feature.Policy;

import java.util.Collections;
import java.util.Set;

/**
 * Unlimited features.
 *
 * @author mwoodman
 */
public class Unlimited implements FeatureManager.PolicySet
{

    private final Set<Policy> policies;
    private final String name = "Unlimited";

    public Unlimited()
    {
        policies = Collections.unmodifiableSet(Policy.bulkCreate(true, true));
    }

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

}
