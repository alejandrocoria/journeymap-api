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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.theme.Theme;
import net.techbrew.journeymap.ui.theme.ThemeCompassPoints;
import net.techbrew.journeymap.ui.theme.ThemeMinimapFrame;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
    final double fontScale;
    final float drawScale;
    final int displayWidth;
    final int displayHeight;
    final ScaledResolution scaledResolution;
    final int minimapSize, textureX, textureY;
    final int minimapRadius, translateX, translateY;
    int marginX, marginY;
    final int fpsLabelHeight;
    final int locationLabelHeight;
    final Rectangle2D.Double frameRect;
    final AxisAlignedBB frameAABB;
    final Point2D.Double centerPoint;
    final Vec3 centerVec;
    final boolean showFps;
    final boolean showBiome;
    final boolean showLocation;
    final boolean showCompass;
    final LabelVars labelFps, labelLocation, labelBiome;
    final ThemeMinimapFrame minimapFrame;
    final ThemeCompassPoints minimapCompassPoints;
    final Theme.Minimap.MinimapSpec minimapSpec;
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
        this.showBiome = JourneyMap.getMiniMapProperties().showBiome.get();
        this.showLocation = JourneyMap.getMiniMapProperties().showLocation.get();
        this.showCompass = JourneyMap.getMiniMapProperties().showCompass.get();
        this.shape = shape;
        this.position = position;
        this.orientation = JourneyMap.getMiniMapProperties().orientation.get();
        this.displayWidth = mc.displayWidth;
        this.displayHeight = mc.displayHeight;
        this.minimapSize = miniMapProperties.customSize.get();

        Theme theme = ThemeFileHandler.getCurrentTheme();

        // Assign shape
        switch (shape)
        {
            case Circle:
            {
                minimapSpec = theme.minimap.circle;
                break;
            }
            case Square:
            default:
            {
                minimapSpec = theme.minimap.square;
                break;
            }
        }

        final boolean wasUnicode = mc.fontRenderer.getUnicodeFlag();
        final boolean useUnicode = (forceUnicode || wasUnicode);
        this.fontScale = labelFontScale * (useUnicode ? 2 : 1);

        fpsLabelHeight = (int) (DrawUtil.getLabelHeight(mc.fontRenderer, minimapSpec.fpsLabel.shadow) * (useUnicode ? .7 : 1) * this.fontScale);
        locationLabelHeight = (int) (DrawUtil.getLabelHeight(mc.fontRenderer, minimapSpec.locationLabel.shadow) * (useUnicode ? .7 : 1) * this.fontScale);

        int compassFontScale = (JourneyMap.getMiniMapProperties().compassFontSmall.get() ? 1 : 2) * (useUnicode ? 2 : 1);
        int compassLabelHeight = (int) (DrawUtil.getLabelHeight(mc.fontRenderer, minimapSpec.compassLabel.shadow) * (useUnicode ? .7 : 1) * compassFontScale);

        drawScale = (miniMapProperties.textureSmall.get() ? .75f : 1f);

        minimapFrame = new ThemeMinimapFrame(theme, minimapSpec, minimapSize);
        marginX = marginY = minimapSpec.margin;
        minimapRadius = minimapSize/2;

        boolean showCompass = minimapSpec.compassPoint!=null && minimapSpec.compassPoint.width>0;

        if(showCompass)
        {
            TextureImpl compassPointTex = this.minimapFrame.getCompassPoint();
            float compassPointScale = ThemeCompassPoints.getCompassPointScale(compassLabelHeight, minimapSpec, compassPointTex);
            double compassPointMargin = compassPointTex.width/2 * compassPointScale;
            marginX = (int) Math.max(marginX, Math.ceil(compassPointMargin));
            marginY = (int) Math.max(marginY, Math.ceil(compassPointMargin));
        }

        // Assign position
        switch (position)
        {
            case BottomRight:
            {
                if(!minimapSpec.labelBottomInside && (showLocation || showBiome))
                {
                    int labels = showLocation ? 1 : 0;
                    labels += showBiome ? 1 : 0;
                    marginY = Math.max(marginY, minimapSpec.labelBottomMargin + (labels*locationLabelHeight));
                }

                textureX = mc.displayWidth - minimapSize - marginX;
                textureY = mc.displayHeight - (minimapSize) - marginY;
                translateX = (mc.displayWidth / 2) - minimapRadius - marginX;
                translateY = (mc.displayHeight / 2) - minimapRadius - marginY;
                break;
            }
            case TopLeft:
            {
                if(!minimapSpec.labelTopInside && showFps)
                {
                    marginY = Math.max(marginY, fpsLabelHeight);
                }

                textureX = marginX;
                textureY = marginY;
                translateX = -(mc.displayWidth / 2) + minimapRadius + marginX;
                translateY = -(mc.displayHeight / 2) + minimapRadius + marginY;
                break;
            }
            case BottomLeft:
            {
                if(!minimapSpec.labelBottomInside && (showLocation || showBiome))
                {
                    int labels = showLocation ? 1 : 0;
                    labels += showBiome ? 1 : 0;

                    marginY = Math.max(marginY, minimapSpec.labelBottomMargin + (labels*locationLabelHeight));
                }

                textureX = marginX;
                textureY = mc.displayHeight - (minimapSize) - marginY;
                translateX = -(mc.displayWidth / 2) + minimapRadius + marginX;
                translateY = (mc.displayHeight / 2) - minimapRadius - marginY;
                break;
            }
            case Center:
            {
                textureX = (mc.displayWidth - minimapSize)/2;
                textureY = (mc.displayHeight - minimapSize)/2;
                translateX = 0;
                translateY = 0;
                break;
            }
            case TopRight:
            default:
            {
                if(!minimapSpec.labelTopInside && showFps)
                {
                    marginY = Math.max(marginY, fpsLabelHeight);
                }

                textureX = mc.displayWidth - minimapSize - marginX;
                textureY = marginY;
                translateX = (mc.displayWidth / 2) - minimapRadius - marginX;
                translateY = -(mc.displayHeight / 2) + minimapRadius + marginY;
                break;
            }
        }

        // Set frame position
        this.minimapFrame.setPosition(textureX, textureY);

        // Assign frame rectangle and centers
        this.centerPoint = new Point2D.Double(textureX + minimapRadius, textureY + minimapRadius);
        this.centerVec = Vec3.createVectorHelper(centerPoint.getX(), centerPoint.getY(), 0);
        this.frameRect = new Rectangle2D.Double(textureX, textureY, minimapSize, minimapSize);
        this.frameAABB = AxisAlignedBB.getBoundingBox(frameRect.x, frameRect.y, 0, frameRect.getMaxX(), frameRect.getMaxY(), 0);

        // Set up compass poionts
        if(showCompass)
        {
            this.minimapCompassPoints = new ThemeCompassPoints(textureX, textureY, minimapRadius, minimapSpec, this.minimapFrame.getCompassPoint(), useUnicode, compassLabelHeight);
        }
        else
        {
            this.minimapCompassPoints = null;
        }

        // Set up label positions
        double centerX = Math.floor(textureX + (minimapSize / 2));
        double topY = textureY;
        double bottomY = textureY + minimapSize;

        if(showFps)
        {
            int yOffsetFps = minimapSpec.labelTopInside ? minimapSpec.labelTopMargin : -marginY;
            DrawUtil.VAlign valignFps = minimapSpec.labelTopInside ? DrawUtil.VAlign.Below : DrawUtil.VAlign.Above;
            labelFps = new LabelVars(centerX, topY + yOffsetFps, DrawUtil.HAlign.Center, valignFps, fontScale, minimapSpec.fpsLabel);
        }
        else
        {
            labelFps = null;
        }


        int labelMargin = minimapSpec.labelBottomMargin + locationLabelHeight;
        int yOffset = minimapSpec.labelBottomInside ? -labelMargin : labelMargin;

        if(showLocation)
        {
            DrawUtil.VAlign vAlign = minimapSpec.labelBottomInside ? DrawUtil.VAlign.Below : DrawUtil.VAlign.Above;
            labelLocation = new LabelVars(centerX, bottomY + yOffset, DrawUtil.HAlign.Center, vAlign, fontScale, minimapSpec.locationLabel);
        }
        else
        {
            labelLocation = null;
        }

        if(showBiome)
        {
            DrawUtil.VAlign vAlign = minimapSpec.labelBottomInside && showLocation ? DrawUtil.VAlign.Above : DrawUtil.VAlign.Below;
            labelBiome = new LabelVars(centerX, bottomY + yOffset, DrawUtil.HAlign.Center, vAlign, fontScale, minimapSpec.biomeLabel);
        }
        else
        {
            labelBiome = null;
        }

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
        final boolean fontShadow;
        DrawUtil.HAlign hAlign;
        DrawUtil.VAlign vAlign;
        Color bgColor;
        int bgAlpha;
        Color fgColor;

        private LabelVars(double x, double y, DrawUtil.HAlign hAlign, DrawUtil.VAlign vAlign, double fontScale, Theme.LabelSpec labelSpec)
        {
            this.x = x;
            this.y = y;
            this.hAlign = hAlign;
            this.vAlign = vAlign;
            this.fontScale = fontScale;
            this.fontShadow = labelSpec.shadow;
            this.bgColor = Theme.getColor(labelSpec.backgroundColor);
            this.bgAlpha = labelSpec.backgroundAlpha;
            this.fgColor = Theme.getColor(labelSpec.foregroundColor);
        }

        void draw(String text)
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
            DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, fgColor, 255, fontScale, fontShadow);
            if (forceUnicode && !isUnicode)
            {
                fontRenderer.setUnicodeFlag(false);
            }
        }
    }
}
