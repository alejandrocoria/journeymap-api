package net.techbrew.journeymap.ui.map.layer;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.render.draw.DrawStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Delegates mouse actions in MapOverlay to Layer impls.
 */
public class LayerDelegate {

    private List<DrawStep> drawSteps = new ArrayList<DrawStep>();
    private List<Layer> layers = new ArrayList<Layer>();

    public LayerDelegate()
    {
        layers.add(new BlockInfoLayer());
        layers.add(new WaypointLayer());
    }

    public void onMouseMove(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        drawSteps.clear();
        for(Layer layer : layers)
        {
            try
            {
                drawSteps.addAll(layer.onMouseMove(mc, mouseX, mouseY, gridWidth, gridHeight, blockCoord));
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().severe(LogFormatter.toString(e));
            }
        }
    }

    public void onMouseClicked(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord, int mouseButton)
    {
        drawSteps.clear();
        for(Layer layer : layers)
        {
            try
            {
                drawSteps.addAll(layer.onMouseClick(mc, mouseX, mouseY, gridWidth, gridHeight, blockCoord));
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().severe(LogFormatter.toString(e));
            }
        }
    }

    public List<DrawStep> getDrawSteps() {
        return drawSteps;
    }

    public interface Layer
    {
        public List<DrawStep> onMouseMove(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord);
        public List<DrawStep> onMouseClick(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord);
    }

}
