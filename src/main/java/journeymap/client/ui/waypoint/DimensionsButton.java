/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.waypoint;

import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.ui.component.Button;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;

/**
 * Created by Mark on 10/12/2014.
 */
class DimensionsButton extends Button
{
    static boolean needInit = true;
    static WorldData.DimensionProvider currentWorldProvider;
    final List<WorldData.DimensionProvider> dimensionProviders = WorldData.getDimensionProviders(WaypointStore.INSTANCE.getLoadedDimensions());

    public DimensionsButton()
    {
        super(0, 0, "");

        if (needInit || currentWorldProvider != null)
        {
            currentWorldProvider = new WorldData.WrappedProvider(FMLClientHandler.instance().getClient().player.world.provider);
            needInit = false;
        }
        updateLabel();

        // Determine width
        fitWidth(ForgeHelper.INSTANCE.getFontRenderer());
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
            maxWidth = Math.max(maxWidth, ForgeHelper.INSTANCE.getFontRenderer().getStringWidth(name));
        }
        return maxWidth + 12;
    }


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
