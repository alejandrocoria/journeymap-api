/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.option.LocationFormat;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
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
    /**
     * The Location format.
     */
    LocationFormat locationFormat = new LocationFormat();
    /**
     * The Location format keys.
     */
    LocationFormat.LocationFormatKeys locationFormatKeys;
    /**
     * The Last coord.
     */
    BlockPos lastCoord = null;
    /**
     * The Last clicked.
     */
    long lastClicked = 0;
    /**
     * The Last mouse x.
     */
    int lastMouseX;
    /**
     * The Last mouse y.
     */
    int lastMouseY;
    /**
     * The Block info step.
     */
    BlockInfoStep blockInfoStep;
    /**
     * The Font renderer.
     */
    FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;

    boolean isSinglePlayer;

    /**
     * Instantiates a new Block info layer.
     */
    public BlockInfoLayer()
    {
        blockInfoStep = new BlockInfoStep();
        drawStepList.add(blockInfoStep);
        isSinglePlayer = FMLClientHandler.instance().getClient().isSingleplayer();
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
                if (blockMD == null || blockMD.isIgnore())
                {
                    blockMD = chunkMD.getBlockMD(blockPos.down());
                }

                Biome biome = JmBlockAccess.INSTANCE.getBiome(blockPos);

                info = locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(),
                        blockPos.getX(),
                        blockPos.getZ(),
                        blockPos.getY(),
                        (blockPos.getY() >> 4)) + " " + biome.getBiomeName();

                if (!blockMD.isIgnore())
                {
                    info = blockMD.getName() + " @ " + info;
                }
            }
            else
            {
                info = Constants.getString("jm.common.location_xz_verbose", blockPos.getX(), blockPos.getZ());
                if (isSinglePlayer)
                {
                    Biome biome = JmBlockAccess.INSTANCE.getBiome(blockPos, null);
                    if (biome != null)
                    {
                        info += " " + biome.getBiomeName();
                    }
                }
            }

//            if (Journeymap.JM_VERSION.patch.equals("dev"))
//            {
//                info = RGB.toHexString(BiomeColorHelper.getWaterColorAtPos(JmBlockAccess.INSTANCE, blockPos)) + " " + info;
//                info += " " + new ChunkPos(blockPos);
//            }

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

    /**
     * The type Block info step.
     */
    class BlockInfoStep implements DrawStep
    {
        /**
         * The Bg color.
         */
        Integer bgColor = RGB.DARK_GRAY_RGB;
        /**
         * The Fg color.
         */
        Integer fgColor = RGB.WHITE_RGB;
        /**
         * The Font scale.
         */
        double fontScale = 1;
        /**
         * The Font shadow.
         */
        boolean fontShadow = false;
        /**
         * The Alpha.
         */
        float alpha = 1;
        /**
         * The Ticks.
         */
        int ticks = 20 * 5;
        private double x;
        private double y;
        private String text;

        /**
         * Update.
         *
         * @param text the text
         * @param x    the x
         * @param y    the y
         */
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
