/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.option.LocationFormat;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows info about the block under the mouse.
 */
public class BlockInfoLayer implements LayerDelegate.Layer
{
    private final List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);
    LocationFormat locationFormat = new LocationFormat();
    LocationFormat.LocationFormatKeys locationFormatKeys;
    BlockPos lastCoord = null;
    long lastClicked = 0;
    int lastMouseX;
    int lastMouseY;
    BlockInfoStep blockInfoStep;
    FontRenderer fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();

    public BlockInfoLayer()
    {
        blockInfoStep = new BlockInfoStep();
        drawStepList.add(blockInfoStep);
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, float fontScale)
    {
        if (!blockCoord.equals(lastCoord))
        {
            FullMapProperties fullMapProperties = JourneymapClient.getFullMapProperties();

            locationFormatKeys = locationFormat.getFormatKeys(fullMapProperties.locationFormat.get());

            lastCoord = blockCoord;

            // Get block under mouse
            Chunk chunk = mc.theWorld.getChunkFromChunkCoords(blockCoord.getX() >> 4, blockCoord.getZ() >> 4);
            String info;
            if (!chunk.isEmpty())
            {
                ChunkMD chunkMD = DataCache.instance().getChunkMD(chunk.getChunkCoordIntPair());
                int blockY = chunkMD.getPrecipitationHeight(blockCoord.getX() & 15, blockCoord.getZ() & 15);
                String biome = ForgeHelper.INSTANCE.getBiome(blockCoord.getX(), blockY, blockCoord.getZ()).biomeName;

                info = locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(),
                        blockCoord.getX(),
                        blockCoord.getZ(),
                        blockY,
                        (blockY >> 4)) + " " + biome;
            }
            else
            {
                info = Constants.getString("jm.common.location_xz_verbose", blockCoord.getX(), blockCoord.getZ());
            }

            double infoHeight = DrawUtil.getLabelHeight(fontRenderer, true) * getMapFontScale();
            blockInfoStep.update(info, gridRenderer.getWidth() / 2, gridRenderer.getHeight() - infoHeight);
        }
        else
        {
            blockInfoStep.update(blockInfoStep.text, gridRenderer.getWidth() / 2, blockInfoStep.y);
        }

        return drawStepList;
    }

    private double getMapFontScale()
    {
        return JourneymapClient.getFullMapProperties().fontScale.get();
    }

    @Override
    public List<DrawStep> onMouseClick(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick, float fontScale)
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean propagateClick()
    {
        return true;
    }

    class BlockInfoStep implements DrawStep
    {
        Integer bgColor = RGB.DARK_GRAY_RGB;
        Integer fgColor = RGB.WHITE_RGB;
        double fontScale = 1;
        boolean fontShadow = false;
        float alpha = 1;
        int ticks = 20 * 5;
        private double x;
        private double y;
        private String text;

        void update(String text, double x, double y)
        {
            this.text = text;
            this.x = x;
            this.y = y;
            this.alpha = 1f;
            this.ticks = 20 * 5;
        }

        @Override
        public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
        {
            if (ticks-- < 0 && alpha > 0)
            {
                alpha -= .01; // Fade
            }
            if (alpha > .1 && text != null)
            {
                DrawUtil.drawLabel(text, x, y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, bgColor, Math.max(0, alpha), fgColor, Math.max(0, alpha), getMapFontScale(), fontShadow);
            }
        }

        @Override
        public int getDisplayOrder()
        {
            return 0;
        }

        @Override
        public String getModId()
        {
            return Journeymap.MOD_ID;
        }
    }
}
