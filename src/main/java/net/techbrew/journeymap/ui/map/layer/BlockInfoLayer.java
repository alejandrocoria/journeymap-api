package net.techbrew.journeymap.ui.map.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.overlay.GridRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mwoodman on 2/26/14.
 */
public class BlockInfoLayer implements LayerDelegate.Layer
{
    private final List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);

    FullMapProperties fullMapProperties = JourneyMap.getInstance().fullMapProperties;
    BlockCoordIntPair lastCoord = null;
    long lastClicked = 0;
    int lastMouseX;
    int lastMouseY;
    BlockInfoStep blockInfoStep;

    public BlockInfoLayer()
    {
        blockInfoStep = new BlockInfoStep();
        drawStepList.add(blockInfoStep);
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        if (!blockCoord.equals(lastCoord))
        {
            lastCoord = blockCoord;

            // Get block under mouse
            Chunk chunk = mc.theWorld.getChunkFromChunkCoords(blockCoord.x >> 4, blockCoord.z >> 4);
            String info;
            if (!chunk.isEmpty())
            {
                int blockY = chunk.getPrecipitationHeight(blockCoord.x & 15, blockCoord.z & 15);
                String biome = mc.theWorld.getBiomeGenForCoords(blockCoord.x, blockCoord.z).biomeName;
                info = Constants.getString("MapOverlay.location_xzyeb", blockCoord.x, blockCoord.z, blockY, (blockY >> 4), biome);
            }
            else
            {
                info = Constants.getString("MapOverlay.location_xzy", blockCoord.x, blockCoord.z, "?");
            }

            boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, fullMapProperties.forceUnicode.get());
            double infoHeight = DrawUtil.getLabelHeight(mc.fontRenderer, true) * getMapFontScale();
            if (unicodeForced)
            {
                DrawUtil.stopUnicode(mc.fontRenderer);
            }

            blockInfoStep.update(info, gridWidth / 2, gridHeight - infoHeight);
        }
        else
        {
            blockInfoStep.update(blockInfoStep.text, gridWidth / 2, blockInfoStep.y);
        }

        return drawStepList;
    }

    private double getMapFontScale()
    {
        return (fullMapProperties.fontSmall.get() ? 1 : 2) * (fullMapProperties.forceUnicode.get() ? 2 : 1);
    }

    @Override
    public List<DrawStep> onMouseClick(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        return Collections.EMPTY_LIST;
    }

    class BlockInfoStep implements DrawStep
    {

        private double x;
        private double y;
        private String text;
        Color bgColor = Color.darkGray;
        Color fgColor = Color.white;
        double fontScale = 1;
        boolean fontShadow = false;
        int alpha = 255;
        int ticks = 20 * 5;

        void update(String text, double x, double y)
        {
            this.text = text;
            this.x = x;
            this.y = y;
            this.alpha = 255;
            this.ticks = 20 * 5;
        }

        @Override
        public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale)
        {
            if (ticks-- < 0 && alpha > 0)
            {
                alpha -= 5; // Fade
            }
            if (alpha > 0 && text != null)
            {
                DrawUtil.drawLabel(text, x, y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, bgColor, Math.max(0, alpha), fgColor, Math.max(0, alpha), getMapFontScale(), fontShadow);
            }
        }
    }
}
