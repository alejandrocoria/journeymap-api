/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends InGameMapProperties
{
    public FullMapProperties()
    {
    }

    @Override
    public void postLoad(boolean isNew)
    {
        super.postLoad(isNew);

        if (isNew && FMLClientHandler.instance().getClient() != null)
        {
            if (FMLClientHandler.instance().getClient().fontRenderer.getUnicodeFlag())
            {
                super.fontScale.set(2);
            }
        }
    }

    @Override
    public String getName()
    {
        return "fullmap";
    }
}
