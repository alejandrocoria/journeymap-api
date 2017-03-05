/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
