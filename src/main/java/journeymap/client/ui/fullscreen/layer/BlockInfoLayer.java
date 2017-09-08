/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.data.DataCache;
import journeymap.client.io.ThemeLoader;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.option.LocationFormat;
import journeymap.client.ui.theme.Theme;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
     * The Block info step.
     */
    PlayerInfoStep playerInfoStep;
    /**
     * The Block info step.
     */
    BlockInfoStep blockInfoStep;

    private boolean isSinglePlayer;

    private final Fullscreen fullscreen;

    private final Minecraft mc;

    /**
     * Instantiates a new Block info layer.
     */
    public BlockInfoLayer(Fullscreen fullscreen)
    {
        this.fullscreen = fullscreen;
        this.blockInfoStep = new BlockInfoStep();
        this.playerInfoStep = new PlayerInfoStep();
        this.mc = FMLClientHandler.instance().getClient();
        this.isSinglePlayer = mc.isSingleplayer();
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockPos, float fontScale, boolean isScrolling)
    {
        Rectangle2D.Double optionsToolbarRect = fullscreen.getOptionsToolbarBounds();
        Rectangle2D.Double menuToolbarRect = fullscreen.getMenuToolbarBounds();

        if (optionsToolbarRect == null || menuToolbarRect == null)
        {
            return Collections.EMPTY_LIST;
        }

        if (drawStepList.isEmpty())
        {
            this.drawStepList.add(playerInfoStep);
            this.drawStepList.add(blockInfoStep);
        }

        playerInfoStep.update(mc.displayWidth / 2, optionsToolbarRect.getMaxY());

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
                    info = String.format("%s \u25A0 %s", blockMD.getName(), info);
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

            blockInfoStep.update(info, gridRenderer.getWidth() / 2, menuToolbarRect.getMinY());
        }

        return drawStepList;
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
     * The player position drawstep.
     */
    class PlayerInfoStep implements DrawStep
    {
        /**
         * The Bg color.
         */
        private Theme.LabelSpec labelSpec;
        private String prefix;
        private double x;
        private double y;

        /**
         * Update.
         *
         * @param x the x
         * @param y the y
         */
        void update(double x, double y)
        {
            Theme theme = ThemeLoader.getCurrentTheme();
            labelSpec = theme.fullscreen.statusLabel;
            if (prefix == null)
            {
                prefix = mc.player.getName() + " \u25A0 ";
            }
            this.x = x;
            this.y = y + (theme.container.toolbar.horizontal.margin * fullscreen.getScreenScaleFactor());
        }

        @Override
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
        {
            if (pass == Pass.Text)
            {
                DrawUtil.drawLabel(prefix + Fullscreen.state().playerLastPos, labelSpec, x, y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, fontScale, 0);
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

    /**
     * The type Block info step.
     */
    class BlockInfoStep implements DrawStep
    {
        /**
         * The Bg color.
         */
        private Theme.LabelSpec labelSpec;
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
            Theme theme = ThemeLoader.getCurrentTheme();
            labelSpec = theme.fullscreen.statusLabel;
            this.text = text;
            this.x = x;
            this.y = y - (theme.container.toolbar.horizontal.margin * fullscreen.getScreenScaleFactor());
        }

        @Override
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
        {
            if (pass == Pass.Text)
            {
                DrawUtil.drawLabel(text, labelSpec, x, y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, fontScale, 0);
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
