package net.techbrew.journeymap.ui.waypoint;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.world.WorldProvider;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.List;

/**
 * Created by Mark on 10/12/2014.
 */
class DimensionsButton extends Button
{
    static boolean needInit = true;
    static WorldProvider currentWorldProvider;
    final List<WorldProvider> worldProviders = WorldData.getDimensionProviders(WaypointStore.instance().getLoadedDimensions());

    public DimensionsButton()
    {
        super(0, 0, "");

        if (needInit || currentWorldProvider != null)
        {
            currentWorldProvider = FMLClientHandler.instance().getClient().thePlayer.worldObj.provider;
            needInit = false;
        }
        updateLabel();

        // Determine width
        fitWidth(FMLClientHandler.instance().getClient().fontRenderer);
    }

    protected void updateLabel()
    {
        String dimName;

        if (currentWorldProvider != null)
        {
            dimName = currentWorldProvider.getDimensionName();
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
        for (WorldProvider worldProvider : worldProviders)
        {
            String name = Constants.getString("jm.waypoint.dimension", worldProvider.getDimensionName());
            maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(name));
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

            for (WorldProvider worldProvider : worldProviders)
            {
                if (worldProvider.dimensionId == currentWorldProvider.dimensionId)
                {
                    index = worldProviders.indexOf(worldProvider) + 1;
                    break;
                }
            }
        }

        if (index >= worldProviders.size() || index < 0)
        {
            currentWorldProvider = null; // "All"
        }
        else
        {
            currentWorldProvider = worldProviders.get(index);
        }

        updateLabel();
    }
}
