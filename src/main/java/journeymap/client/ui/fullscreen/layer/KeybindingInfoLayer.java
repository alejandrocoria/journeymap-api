/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.forge.event.KeyEventHandler;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows info about keybindings
 */
public class KeybindingInfoLayer implements LayerDelegate.Layer
{
    private final List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);

    private FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;

    private final KeybindingInfoStep keybindingInfoStep;

    FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();

    /**
     * Instantiates a new Block info layer.
     */
    public KeybindingInfoLayer()
    {
        keybindingInfoStep = new KeybindingInfoStep();
        drawStepList.add(keybindingInfoStep);
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockPos, float fontScale)
    {
        keybindingInfoStep.rightX = gridRenderer.getWidth();
        keybindingInfoStep.bottomY = gridRenderer.getHeight();
        return fullMapProperties.showKeys.get() ? drawStepList : Collections.EMPTY_LIST;
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
     * The type Block info step.
     */
    class KeybindingInfoStep implements DrawStep
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
         * The Font shadow.
         */
        boolean fontShadow = true;

        /**
         * Number of ticks this will be visible
         */
        private double rightX;
        private double bottomY;
        private ArrayList<Tuple<String, String>> lines;
        private int lineWidth = 0;
        private int keyNameWidth = 0;
        private int lineHeight = 0;

        private void initLines()
        {
            int fontScale = fullMapProperties.fontScale.get();
            lines = new ArrayList<>();
            lineHeight = 3 + (fontScale * fontRenderer.FONT_HEIGHT);

            for (KeyBinding keyBinding : KeyEventHandler.INSTANCE.getInGuiKeybindings())
            {
                TextComponentString keyName = new TextComponentString(keyBinding.getDisplayName());
                keyName.getStyle().setColor(TextFormatting.YELLOW);

                TextComponentString keyDesc = new TextComponentString(Constants.getString(keyBinding.getKeyDescription()));
                keyDesc.getStyle().setColor(TextFormatting.AQUA);

                Tuple<String, String> line = new Tuple<>(keyName.getFormattedText(), keyDesc.getFormattedText());
                lines.add(line);
                keyNameWidth = Math.max(keyNameWidth, fontScale * fontRenderer.getStringWidth(keyName.getFormattedText()));
                lineWidth = Math.max(lineWidth, fontScale * fontRenderer.getStringWidth(keyName.getFormattedText() + keyDesc.getFormattedText()));
            }
        }

        @Override
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
        {
            if (pass == Pass.Text)
            {
                if (lines == null)
                {
                    initLines();
                }

                int pad = (int) (10 * fontScale);
                int y = (int) (bottomY - (lines.size() * lineHeight) - pad - pad);
                int x = (int) rightX - (lineWidth) - pad - pad;
                int leftX = (int) x - keyNameWidth - pad;

                DrawUtil.drawRectangle(leftX, y - pad, rightX - leftX - pad, (lines.size() * lineHeight) + (2 * pad), bgColor, .7f);

                for (Tuple<String, String> line : lines)
                {
                    DrawUtil.drawLabel(line.getFirst(), x, y, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, bgColor, 0f, fgColor, 1f, fontScale, fontShadow);
                    DrawUtil.drawLabel(line.getSecond(), x + pad, y, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, bgColor, 0f, 0x00ffff, 1f, fontScale, fontShadow);
                    y += (lineHeight);
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
