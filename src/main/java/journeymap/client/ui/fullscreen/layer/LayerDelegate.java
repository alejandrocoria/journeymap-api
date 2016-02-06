/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.render.draw.DrawStep;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Delegates mouse actions in MapOverlay to Layer impls.
 */
public class LayerDelegate
{

    private List<DrawStep> drawSteps = new ArrayList<DrawStep>();
    private List<Layer> layers = new ArrayList<Layer>();

    public LayerDelegate()
    {
        layers.add(new BlockInfoLayer());
        layers.add(new WaypointLayer());
    }

    public void onMouseMove(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockPos blockCoord)
    {
        drawSteps.clear();
        for (Layer layer : layers)
        {
            try
            {
                drawSteps.addAll(layer.onMouseMove(mc, mouseX, mouseY, gridWidth, gridHeight, blockCoord));
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(LogFormatter.toString(e));
            }
        }
    }

    public void onMouseClicked(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockPos blockCoord, int mouseButton)
    {
        drawSteps.clear();
        for (Layer layer : layers)
        {
            try
            {
                drawSteps.addAll(layer.onMouseClick(mc, mouseX, mouseY, gridWidth, gridHeight, blockCoord));
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(LogFormatter.toString(e));
            }
        }
    }

    public List<DrawStep> getDrawSteps()
    {
        return drawSteps;
    }

    public interface Layer
    {
        public List<DrawStep> onMouseMove(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockPos blockCoord);

        public List<DrawStep> onMouseClick(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockPos blockCoord);
    }

}
