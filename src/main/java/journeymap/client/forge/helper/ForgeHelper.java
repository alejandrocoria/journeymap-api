/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import journeymap.client.forge.helper.impl.ForgeHelper_1_8;

/**
 * Delegates to a version-specific implementation of IForgeHelper.INSTANCE.
 */
public class ForgeHelper
{
    public static final IForgeHelper INSTANCE = new ForgeHelper_1_8();
}
