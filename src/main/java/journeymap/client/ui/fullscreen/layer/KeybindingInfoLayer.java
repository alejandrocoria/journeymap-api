/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.forge.event.KeyEventHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.theme.Theme;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows an overlay of current keybindings.
 */
public class KeybindingInfoLayer implements LayerDelegate.Layer
{
    private final List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);

    private FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;

    private final KeybindingInfoStep keybindingInfoStep;

    private FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();

    private final Fullscreen fullscreen;

    private final Minecraft mc;

    /**
     * Constructor.
     */
    public KeybindingInfoLayer(Fullscreen fullscreen)
    {
        this.mc = FMLClientHandler.instance().getClient();
        this.fullscreen = fullscreen;
        keybindingInfoStep = new KeybindingInfoStep();
        drawStepList.add(keybindingInfoStep);
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockPos, float fontScale, boolean isScrolling)
    {
        if (fullMapProperties.showKeys.get())
        {
            if (keybindingInfoStep.panelRect.contains(mousePosition))
            {
                keybindingInfoStep.hide();
            }
            else
            {
                keybindingInfoStep.show();
            }
            return drawStepList;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<DrawStep> onMouseClick(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick, float fontScale)
    {
        return fullMapProperties.showKeys.get() ? drawStepList : Collections.EMPTY_LIST;
    }

    @Override
    public boolean propagateClick()
    {
        return true;
    }

    /**
     * Draws the keybinding info panel
     */
    class KeybindingInfoStep implements DrawStep
    {
        private double screenWidth;
        private double screenHeight;
        private double fontScale;
        private int pad;
        private ArrayList<Tuple<String, String>> lines;
        private int keyNameWidth = 0;
        private int keyDescWidth = 0;
        private int lineHeight = 0;
        Rectangle2D panelRect = new Rectangle2D.Double();
        Theme theme = ThemeLoader.getCurrentTheme();
        Theme.LabelSpec statusLabelSpec;
        int bgColor;
        float fgAlphaDefault = 1f;
        float bgAlphaDefault = .7f;
        float fgAlpha = fgAlphaDefault;
        float bgAlpha = bgAlphaDefault;

        @Override
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
        {
            if (pass == Pass.Text)
            {
                if (fullscreen.getMenuToolbarBounds() == null)
                {
                    return;
                }
                updateLayout(gridRenderer, fontScale);

                DrawUtil.drawRectangle(panelRect.getX(), panelRect.getY(), panelRect.getWidth(), panelRect.getHeight(), bgColor, bgAlpha);

                int x = (int) panelRect.getX() + pad + keyNameWidth;
                int y = (int) panelRect.getY() + pad;
                int firstColor = theme.fullscreen.statusLabel.highlight.getColor();
                int secondColor = theme.fullscreen.statusLabel.foreground.getColor();

                try
                {
                    GlStateManager.enableBlend();
                    for (Tuple<String, String> line : lines)
                    {
                        DrawUtil.drawLabel(line.getFirst(), x, y, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, null, 0f, firstColor, fgAlpha, fontScale, false);
                        DrawUtil.drawLabel(line.getSecond(), x + pad, y, DrawUtil.HAlign.Right, DrawUtil.VAlign.Middle, null, 0f, secondColor, fgAlpha, fontScale, false);
                        y += (lineHeight);
                    }
                }
                finally
                {
                    GlStateManager.disableBlend();
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

        void hide()
        {
            bgAlpha = 0.2f;
            fgAlpha = 0.2f;
        }

        void show()
        {
            bgAlpha = bgAlphaDefault;
            fgAlpha = fgAlphaDefault;
        }

        private void updateLayout(GridRenderer gridRenderer, double fontScale)
        {
            Theme theme = ThemeLoader.getCurrentTheme();
            statusLabelSpec = theme.fullscreen.statusLabel;
            bgColor = statusLabelSpec.background.getColor();

            if (fontScale != this.fontScale || screenWidth != gridRenderer.getWidth() || screenHeight != gridRenderer.getHeight())
            {
                screenWidth = gridRenderer.getWidth();
                screenHeight = gridRenderer.getHeight();
                this.fontScale = fontScale;
                this.pad = (int) (10 * fontScale);
                this.lineHeight = (int) (3 + (fontScale * fontRenderer.FONT_HEIGHT));
                initLines(fontScale);

                int panelWidth = keyNameWidth + keyDescWidth + (4 * pad);
                int panelHeight = (lines.size() * lineHeight) + pad;
                int scaleFactor = fullscreen.getScreenScaleFactor();

                int panelX = (int) screenWidth - (theme.container.toolbar.vertical.margin * scaleFactor) - panelWidth;
                int panelY = (int) screenHeight - (theme.container.toolbar.horizontal.margin * scaleFactor) - panelHeight;

                panelRect.setRect(panelX, panelY, panelWidth, panelHeight);

                Rectangle2D.Double menuToolbarRect = fullscreen.getMenuToolbarBounds();
                if (menuToolbarRect != null && menuToolbarRect.intersects(panelRect))
                {
                    if (panelX <= menuToolbarRect.getMaxX())
                    {
                        panelY = (int) menuToolbarRect.getMinY() - 5 - panelHeight;
                        panelRect.setRect(panelX, panelY, panelWidth, panelHeight);
                    }
                }
            }
        }

        private void initLines(double fontScale)
        {
            lines = new ArrayList<>();
            keyDescWidth = 0;
            keyNameWidth = 0;
            bgAlpha = fgAlphaDefault;
            bgAlpha = bgAlphaDefault;

            for (KeyBinding keyBinding : KeyEventHandler.INSTANCE.getInGuiKeybindings())
            {
                initLine(keyBinding, fontScale);
            }
            initLine(mc.gameSettings.keyBindChat, fontScale);
        }

        private void initLine(KeyBinding keyBinding, double fontScale)
        {
            String keyName = keyBinding.getDisplayName();
            String keyDesc = Constants.getString(keyBinding.getKeyDescription());

            Tuple<String, String> line = new Tuple<>(keyName, keyDesc);
            lines.add(line);

            keyNameWidth = (int) Math.max(keyNameWidth, fontScale * fontRenderer.getStringWidth(keyName));
            keyDescWidth = (int) Math.max(keyDescWidth, fontScale * fontRenderer.getStringWidth(keyDesc));
        }
    }
}
