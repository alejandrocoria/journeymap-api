package net.techbrew.journeymap.ui.map.layer;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.render.draw.DrawStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwoodman on 2/26/14.
 */
public class LayerDelegate {

    private List<DrawStep> drawSteps = new ArrayList<DrawStep>();

    BlockInfoLayer blockInfoLayer = new BlockInfoLayer();

    public LayerDelegate()
    {

    }

    public void onMouseMove(Minecraft mc, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        drawSteps.clear();
        drawSteps.addAll(blockInfoLayer.onMouseMove(mc, gridWidth, gridHeight, blockCoord));
    }

    public List<DrawStep> getDrawSteps() {
        return drawSteps;
    }

}
