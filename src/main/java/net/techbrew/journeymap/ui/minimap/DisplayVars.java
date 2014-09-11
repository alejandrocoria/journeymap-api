/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.minimap;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.theme.Theme;
import net.techbrew.journeymap.ui.theme.ThemeMinimapFrame;

import java.awt.*;
import java.util.Arrays;

/**
 * Display variables for the Minimap.
 * <p/>
 * Encapsulates all the layout and display specifics for rendering the Minimap
 * given a Shape, Position, screen size, and user preferences.  All of the values
 * only need to be calculated once after a change of shape/position/screen size,
 * so it's done here rather than during the minimap renderloop.
 */
public class DisplayVars
{
    final Position position;
    final Shape shape;
    final Orientation orientation;
    final TextureImpl maskTexture;
    final float drawScale;
    final double fontScale;
    final int displayWidth;
    final int displayHeight;
    final ScaledResolution scaledResolution;
    final int minimapSize, textureX, textureY;
    final int minimapOffset, translateX, translateY;
    final int marginX, marginY, scissorX, scissorY;
    final double viewPortPadX;
    final double viewPortPadY;
    final boolean showFps;
    final LabelVars labelFps, labelLocation, labelBiome;
    final ThemeMinimapFrame minimapFrame;
    boolean forceUnicode;

    /**
     * Constructor.
     *
     * @param mc             Minecraft
     * @param shape          Desired shape
     * @param position       Desired position
     * @param labelFontScale Font scale for labels
     */
    DisplayVars(Minecraft mc, Shape shape, Position position, double labelFontScale)
    {
        // Immutable member and local vars
        final MiniMapProperties miniMapProperties = JourneyMap.getMiniMapProperties();
        this.scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        this.forceUnicode = JourneyMap.getMiniMapProperties().forceUnicode.get();
        this.showFps = JourneyMap.getMiniMapProperties().showFps.get();
        this.shape = shape;
        this.position = position;
        this.orientation = JourneyMap.getMiniMapProperties().orientation.get();
        this.displayWidth = mc.displayWidth;
        this.displayHeight = mc.displayHeight;
        final boolean useFontShadow = false;
        final boolean wasUnicode = mc.fontRenderer.getUnicodeFlag();
        final boolean useUnicode = (forceUnicode || wasUnicode);
        this.fontScale = labelFontScale * (useUnicode ? 2 : 1);
        final int labelHeight = (int) (DrawUtil.getLabelHeight(mc.fontRenderer, useFontShadow) * (useUnicode ? .7 : 1) * this.fontScale);

        // Mutable local vars
        int bottomTextureYMargin = 0;
        boolean labelsOutside = false;
        boolean scissorFps = true;
        boolean scissorLocation = false;
        boolean scissorBiome = true;
        int yOffsetFps = 4;
        int yOffsetBiome = -3;
        int yOffsetLocation = yOffsetBiome + -labelHeight;
        DrawUtil.VAlign valignFps = DrawUtil.VAlign.Below;
        DrawUtil.VAlign valignLocation = DrawUtil.VAlign.Above;
        DrawUtil.VAlign valignBiome = DrawUtil.VAlign.Above;
        float textureScale = (miniMapProperties.textureSmall.get() ? .75f : 1f);
        Theme theme = ThemeFileHandler.getCurrentTheme();

        // Assign shape
        switch (shape)
        {
            case Circle:
            {
                drawScale = 1f * textureScale;
                maskTexture = TextureCache.instance().getMinimapSmallCircleMask();
                minimapSize = miniMapProperties.customSize.get();
                viewPortPadX = 5;
                viewPortPadY = 5;
                if (fontScale == 1)
                {
                    bottomTextureYMargin = 10;
                }
                else
                {
                    bottomTextureYMargin = 20;
                }
                break;
            }
            case Square:
            default:
            {
                drawScale = 1f * textureScale;
                maskTexture = null;
                minimapSize = miniMapProperties.customSize.get();
                viewPortPadX = 2;
                viewPortPadY = 2;
                valignLocation = DrawUtil.VAlign.Above;
                valignBiome = DrawUtil.VAlign.Above;
                yOffsetFps = theme.minimap.padding;
                yOffsetLocation = -theme.minimap.padding;
                yOffsetBiome = -(theme.minimap.padding + labelHeight);
                scissorLocation = false;
                scissorBiome = false;

                break;
            }
        }
        minimapFrame = new ThemeMinimapFrame(theme, minimapSize);
        marginX = marginY = ThemeFileHandler.getCurrentTheme().minimap.margin;
        minimapOffset = minimapSize/2;

        // Assign position
        switch (position)
        {
            case BottomRight:
            {
                textureX = mc.displayWidth - minimapSize - marginX;
                textureY = mc.displayHeight - (minimapSize) - marginY - bottomTextureYMargin;
                translateX = (mc.displayWidth / 2) - minimapOffset;
                translateY = (mc.displayHeight / 2) - minimapOffset - bottomTextureYMargin;
                scissorX = textureX;
                scissorY = marginY + bottomTextureYMargin;
                break;
            }
            case TopLeft:
            {
                textureX = marginX;
                textureY = marginY;
                translateX = -(mc.displayWidth / 2) + minimapOffset;
                translateY = -(mc.displayHeight / 2) + minimapOffset;
                scissorX = textureX;
                scissorY = mc.displayHeight - minimapSize - marginY;
                break;
            }
            case BottomLeft:
            {
                textureX = marginX;
                textureY = mc.displayHeight - (minimapSize) - marginY - bottomTextureYMargin;
                translateX = -(mc.displayWidth / 2) + minimapOffset;
                translateY = (mc.displayHeight / 2) - minimapOffset - bottomTextureYMargin;
                scissorX = textureX;
                scissorY = marginY + bottomTextureYMargin;
                break;
            }
            case Center:
            {
                textureX = (mc.displayWidth - minimapSize)/2;
                textureY = (mc.displayHeight - minimapSize)/2;
                translateX = 0;
                translateY = 0;
                scissorX = textureX;
                scissorY = textureY;
                break;
            }
            case TopRight:
            default:
            {
                textureX = mc.displayWidth - minimapSize - marginX;
                textureY = marginY;
                translateX = (mc.displayWidth / 2) - minimapOffset;
                translateY = -(mc.displayHeight / 2) + minimapOffset;
                scissorX = mc.displayWidth - minimapSize - marginX;
                scissorY = mc.displayHeight - minimapSize - marginY;
                break;
            }
        }

        double centerX = Math.floor(textureX + (minimapSize / 2));
        double topY = textureY;
        double bottomY = textureY + minimapSize;
        labelFps = new LabelVars(centerX, topY + yOffsetFps, DrawUtil.HAlign.Center, valignFps, fontScale, scissorFps, useFontShadow);
        labelLocation = new LabelVars(centerX, bottomY + yOffsetLocation, DrawUtil.HAlign.Center, valignLocation, fontScale, scissorLocation, useFontShadow);
        labelBiome = new LabelVars(centerX, bottomY + yOffsetBiome, DrawUtil.HAlign.Center, valignBiome, fontScale, scissorBiome, useFontShadow);
    }

    /**
     * Position of minimap on screen
     */
    public enum Position
    {
        TopRight("jm.minimap.position_topright"),
        BottomRight("jm.minimap.position_bottomright"),
        BottomLeft("jm.minimap.position_bottomleft"),
        TopLeft("jm.minimap.position_topleft"),
        Center("jm.minimap.position_center");

        public final String label;

        private Position(String label)
        {
            this.label = label;
        }

        public static Position getPreferred()
        {
            final MiniMapProperties miniMapProperties = JourneyMap.getMiniMapProperties();

            DisplayVars.Position position = null;
            try
            {
                position = miniMapProperties.position.get();
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warn("Not a valid minimap position in : " + miniMapProperties.getFile());
            }

            if (position == null)
            {
                position = Position.TopRight;
                miniMapProperties.position.set(position);
                miniMapProperties.save();
            }
            return position;
        }

        public static Position safeValueOf(String name)
        {
            Position value = null;
            try
            {
                value = Position.valueOf(name);
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warn("Not a valid minimap position: " + name);
            }

            if (value == null)
            {
                value = Position.TopRight;
            }
            return value;
        }
    }

    public enum Orientation
    {
        North("jm.minimap.orientation.north"),
        OldNorth("jm.minimap.orientation.oldnorth"),
        PlayerHeading("jm.minimap.orientation.playerheading");

        public final String label;

        private Orientation(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return Constants.getString(this.label);
        }
    }

    /**
     * Shape (and size) of minimap
     */
    public enum Shape
    {
        Square("jm.minimap.shape_square"),
        Circle("jm.minimap.shape_circle");
        public static Shape[] Enabled = {Square, Circle};
        public final String label;

        private Shape(String label)
        {
            this.label = label;
        }

        public static Shape getPreferred()
        {
            final MiniMapProperties miniMapProperties = JourneyMap.getMiniMapProperties();

            DisplayVars.Shape shape = null;
            try
            {
                shape = miniMapProperties.shape.get();
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warn("Not a valid minimap shape in : " + miniMapProperties.getFile());
            }

            if (shape == null)
            {
                shape = Shape.Square;
                miniMapProperties.shape.set(shape);
                miniMapProperties.save();
            }
            return shape;
        }

        public static Shape safeValueOf(String name)
        {
            Shape value = null;
            try
            {
                value = Shape.valueOf(name);
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warn("Not a valid minimap shape: " + name);
            }

            if (value == null || !value.isEnabled())
            {
                value = Shape.Square;
            }
            return value;
        }

        public boolean isEnabled()
        {
            return Arrays.binarySearch(DisplayVars.Shape.Enabled, this) >= 0;
        }
    }

    /**
     * Encapsulation of label attributes.
     */
    class LabelVars
    {
        final double x;
        final double y;
        final double fontScale;
        final boolean scissor;
        final boolean fontShadow;
        DrawUtil.HAlign hAlign;
        DrawUtil.VAlign vAlign;

        private LabelVars(double x, double y, DrawUtil.HAlign hAlign, DrawUtil.VAlign vAlign, double fontScale, boolean scissor, boolean fontShadow)
        {
            this.x = x;
            this.y = y;
            this.hAlign = hAlign;
            this.vAlign = vAlign;
            this.fontScale = fontScale;
            this.scissor = scissor;
            this.fontShadow = fontShadow;
        }

        void draw(String text, Color bgColor, int bgAlpha, Color color, int alpha)
        {
            boolean isUnicode = false;
            FontRenderer fontRenderer = null;
            if (forceUnicode)
            {
                fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
                isUnicode = fontRenderer.getUnicodeFlag();
                if (!isUnicode)
                {
                    fontRenderer.setUnicodeFlag(true);
                }
            }
            DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow);
            if (forceUnicode && !isUnicode)
            {
                fontRenderer.setUnicodeFlag(false);
            }
        }
    }
}
