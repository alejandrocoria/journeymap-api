package net.techbrew.journeymap.ui.waypoint;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.world.WorldProvider;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
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
            currentWorldProvider = ForgeHelper.INSTANCE.getClient().thePlayer.worldObj.provider;
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
        for (WorldProvider worldProvider : worldProviders)
        {
            String name = Constants.getString("jm.waypoint.dimension", WorldData.getSafeDimensionName(worldProvider));
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

            int currentDimension = ForgeHelper.INSTANCE.getDimension(currentWorldProvider);

            for (WorldProvider worldProvider : worldProviders)
            {
                if (currentDimension == ForgeHelper.INSTANCE.getDimension(worldProvider))
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
