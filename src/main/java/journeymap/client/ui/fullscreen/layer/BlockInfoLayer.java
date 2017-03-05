/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.option.LocationFormat;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

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
    FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRendererObj;

    public BlockInfoLayer()
    {
        blockInfoStep = new BlockInfoStep();
        drawStepList.add(blockInfoStep);
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockPos, float fontScale)
    {
        if (!blockPos.equals(lastCoord))
        {
            FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();

            locationFormatKeys = locationFormat.getFormatKeys(fullMapProperties.locationFormat.get());

            lastCoord = blockPos;

            // Get block under mouse
            ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(blockPos);
            String info = "";
            if (chunkMD != null && chunkMD.hasChunk())
            {
                BlockMD blockMD = chunkMD.getBlockMD(blockPos.up());
                if (blockMD == null || blockMD.isAir())
                {
                    blockMD = chunkMD.getBlockMD(blockPos.down());
                }

                String biome = chunkMD.getWorld().getBiomeForCoordsBody(blockPos).getBiomeName();

                info = locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(),
                        blockPos.getX(),
                        blockPos.getZ(),
                        blockPos.getY(),
                        (blockPos.getY() >> 4)) + " " + biome;

                if (!blockMD.isAir())
                {
                    info = blockMD.getName() + " @ " + info;

//                    if (Journeymap.JM_VERSION.patch.equals("dev"))
//                    {
//                        info = RGB.toHexString(blockMD.getColor(chunkMD, blockPos)) + "  " + info;
//                    }
                }
            }
            else
            {
                info = Constants.getString("jm.common.location_xz_verbose", blockPos.getX(), blockPos.getZ());
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
        return Journeymap.getClient().getFullMapProperties().fontScale.get();
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
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
        {
            if (pass == Pass.Text)
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
