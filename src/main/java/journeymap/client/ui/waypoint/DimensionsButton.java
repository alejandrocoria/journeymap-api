/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.waypoint;

import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.ui.component.Button;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;

/**
 * @author techbrew 10/12/2014.
 */
class DimensionsButton extends Button
{
    /**
     * The Need init.
     */
    static boolean needInit = true;
    /**
     * The Current world provider.
     */
    static WorldData.DimensionProvider currentWorldProvider;
    /**
     * The Dimension providers.
     */
    final List<WorldData.DimensionProvider> dimensionProviders = WorldData.getDimensionProviders(WaypointStore.INSTANCE.getLoadedDimensions());

    /**
     * Instantiates a new Dimensions button.
     */
    public DimensionsButton()
    {
        super(0, 0, "");

        if (needInit || currentWorldProvider != null)
        {
            currentWorldProvider = new WorldData.WrappedProvider(FMLClientHandler.instance().getClient().thePlayer.worldObj.provider);
            needInit = false;
        }
        updateLabel();

        // Determine width
        fitWidth(FMLClientHandler.instance().getClient().fontRendererObj);
    }

    protected void updateLabel()
    {
        String dimName;

        if (currentWorldProvider != null)
        {
            dimName = WorldData.getSafeDimensionName(currentWorldProvider);
        }
        else
        {
            dimName = Constants.getString("jm.waypoint.dimension_all");
        }
        displayString = Constants.getString("jm.waypoint.dimension", dimName);
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        int maxWidth = 0;
        for (WorldData.DimensionProvider dimensionProvider : dimensionProviders)
        {
            String name = Constants.getString("jm.waypoint.dimension", WorldData.getSafeDimensionName(dimensionProvider));
            maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRendererObj.getStringWidth(name));
        }
        return maxWidth + 12;
    }


    /**
     * Next value.
     */
    public void nextValue()
    {
        int index;

        if (currentWorldProvider == null)
        {
            index = 0;
        }
        else
        {
            index = -1;

            int currentDimension = currentWorldProvider.getDimension();

            for (WorldData.DimensionProvider dimensionProvider : dimensionProviders)
            {
                if (currentDimension == dimensionProvider.getDimension())
                {
                    index = dimensionProviders.indexOf(dimensionProvider) + 1;
                    break;
                }
            }
        }

        if (index >= dimensionProviders.size() || index < 0)
        {
            currentWorldProvider = null; // "All"
        }
        else
        {
            currentWorldProvider = dimensionProviders.get(index);
        }

        updateLabel();
    }
}
