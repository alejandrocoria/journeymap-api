/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */
package journeymap.client.properties;

import journeymap.common.properties.config.BooleanField;
import net.minecraftforge.fml.client.FMLClientHandler;

import static journeymap.common.properties.Category.Inherit;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends InGameMapProperties
{
    /**
     * Whether to show keybinding info
     */
    public final BooleanField showKeys = new BooleanField(Inherit, "jm.common.show_keys", true);

    /**
     * Instantiates a new Full map properties.
     */
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
