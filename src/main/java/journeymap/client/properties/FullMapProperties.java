/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.forge.helper.ForgeHelper;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends InGameMapProperties
{
    public FullMapProperties()
    {
    }

    @Override
    public void newFileInit()
    {
        if (ForgeHelper.INSTANCE.getFontRenderer().getUnicodeFlag())
        {
            super.fontScale.set(2);
        }
    }

    @Override
    public String getName()
    {
        return "fullmap";
    }
}
