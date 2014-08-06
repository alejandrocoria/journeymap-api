/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.feature.impl;

import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.feature.Policy2;

import java.util.Collections;
import java.util.Set;

/**
 * Unlimited features.
 * @author mwoodman
 *
 */
public class Unlimited implements FeatureManager.PolicySet
{

    private final Set<Policy2> policies;
    private final String name = "Unlimited";

	public Unlimited() {
        policies = Collections.unmodifiableSet(Policy2.bulkCreate(true, true));
    }
	
	@Override
    public Set<Policy2> getPolicies()
    {
        return policies;
    }
	
	@Override
	public String getName() {
		return name;
	}

}
