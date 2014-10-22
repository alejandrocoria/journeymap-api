/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.option.LocationFormat;
import net.techbrew.journeymap.ui.theme.Theme;
import net.techbrew.journeymap.ui.theme.ThemeCompassPoints;
import net.techbrew.journeymap.ui.theme.ThemeMinimapFrame;

import java.awt.geom.Point2D;

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
    final float terrainAlpha;
    final ScaledResolution scaledResolution;
    final int minimapWidth, minimapHeight;
    final int textureX, textureY;
    final int translateX, translateY;
    final double reticleSegmentLength;
    final int fpsLabelHeight;
    final int locationLabelHeight;
    final Point2D.Double centerPoint;
    final boolean showFps;
    final boolean showBiome;
    final boolean showLocation;
    final boolean showCompass;
    final boolean showReticle;
    final LabelVars labelFps, labelLocation, labelBiome;
    final ThemeMinimapFrame minimapFrame;
    final ThemeCompassPoints minimapCompassPoints;
    final Theme.Minimap.MinimapSpec minimapSpec;
    final LocationFormat.LocationFormatKeys locationFormatKeys;
    final boolean locationFormatVerbose;
    int marginX, marginY;
    boolean forceUnicode;

    /**
     * Constructor.
     *
     * @param mc                Minecraft
     * @param miniMapProperties
     */
    DisplayVars(Minecraft mc, final MiniMapProperties miniMapProperties)
    {
        // Immutable member and local vars
        this.scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        this.showFps = miniMapProperties.showFps.get();
        this.showBiome = miniMapProperties.showBiome.get();
        this.showLocation = miniMapProperties.showLocation.get();
        this.showCompass = miniMapProperties.showCompass.get();
        this.showReticle = miniMapProperties.showReticle.get();
        this.shape = miniMapProperties.shape.get();
        this.position = miniMapProperties.position.get();
        this.orientation = miniMapProperties.orientation.get();
        this.displayWidth = mc.displayWidth;
        this.displayHeight = mc.displayHeight;
        this.terrainAlpha = Math.max(0f, Math.min(1f, miniMapProperties.terrainAlpha.get() / 100f));
        this.locationFormatKeys = new LocationFormat().getFormatKeys(miniMapProperties.locationFormat.get());
        this.locationFormatVerbose = miniMapProperties.locationFormatVerbose.get();
        Theme theme = ThemeFileHandler.getCurrentTheme();

        // Assign shape
        switch (shape)
        {
            case Circle:
            {
                minimapSpec = theme.minimap.circle;
                minimapWidth = miniMapProperties.getSize();
                minimapHeight = miniMapProperties.getSize();
                reticleSegmentLength = minimapHeight / 2;
                break;
            }
            case Rectangle:
            {
                minimapSpec = theme.minimap.square;
                double ratio = mc.displayWidth * 1D / mc.displayHeight;
                minimapHeight = miniMapProperties.getSize();
                minimapWidth = (int) (minimapHeight * ratio);
                reticleSegmentLength = minimapWidth / 1.5;
                break;
            }
            case Square:
            default:
            {
                minimapSpec = theme.minimap.square;
                minimapWidth = miniMapProperties.getSize();
                minimapHeight = miniMapProperties.getSize();
                reticleSegmentLength = Math.sqrt((minimapHeight * minimapHeight) + (minimapWidth * minimapWidth)) / 2;
                break;
            }
        }

        this.fontScale = miniMapProperties.fontScale.get();

        FontRenderer fontRenderer = mc.fontRenderer;
        fpsLabelHeight = (int) (DrawUtil.getLabelHeight(fontRenderer, minimapSpec.fpsLabel.shadow) * this.fontScale);
        locationLabelHeight = (int) (DrawUtil.getLabelHeight(fontRenderer, minimapSpec.locationLabel.shadow) * this.fontScale);

        int compassFontScale = miniMapProperties.compassFontScale.get();
        int compassLabelHeight = 0;
        if (showCompass)
        {
            compassLabelHeight = (int) (DrawUtil.getLabelHeight(mc.fontRenderer, minimapSpec.compassLabel.shadow) * compassFontScale);
        }

        drawScale = (miniMapProperties.textureSmall.get() ? .75f : 1f);

        minimapFrame = new ThemeMinimapFrame(theme, minimapSpec, miniMapProperties, minimapWidth, minimapHeight);
        marginX = marginY = minimapSpec.margin;

        int halfWidth = minimapWidth / 2;
        int halfHeight = minimapHeight / 2;

        if (showCompass)
        {
            double compassPointMargin;
            boolean compassExists = minimapSpec.compassPoint != null && minimapSpec.compassPoint.width > 0;
            if (compassExists)
            {
                TextureImpl compassPointTex = this.minimapFrame.getCompassPoint();
                float compassPointScale = ThemeCompassPoints.getCompassPointScale(compassLabelHeight, minimapSpec, compassPointTex);
                compassPointMargin = compassPointTex.width / 2 * compassPointScale;
            }
            else
            {
                compassPointMargin = compassLabelHeight;
            }
            marginX = (int) Math.max(marginX, Math.ceil(compassPointMargin));
            marginY = (int) Math.max(marginY, Math.ceil(compassPointMargin) + compassLabelHeight / 2);
        }

        // Assign position
        switch (position)
        {
            case BottomRight:
            {
                if (!minimapSpec.labelBottomInside && (showLocation || showBiome))
                {
                    int labels = showLocation ? 1 : 0;
                    labels += showBiome ? 1 : 0;
                    marginY = Math.max(marginY, minimapSpec.labelBottomMargin + (labels * locationLabelHeight) + compassLabelHeight / 2);
                }

                textureX = mc.displayWidth - minimapWidth - marginX;
                textureY = mc.displayHeight - (minimapHeight) - marginY;
                translateX = (mc.displayWidth / 2) - halfWidth - marginX;
                translateY = (mc.displayHeight / 2) - halfHeight - marginY;
                break;
            }
            case TopLeft:
            {
                if (!minimapSpec.labelTopInside && showFps)
                {
                    marginY = Math.max(marginY, Math.max(compassLabelHeight / 2, minimapSpec.labelTopMargin) + fpsLabelHeight);
                }

                textureX = marginX;
                textureY = marginY;
                translateX = -(mc.displayWidth / 2) + halfWidth + marginX;
                translateY = -(mc.displayHeight / 2) + halfHeight + marginY;
                break;
            }
            case BottomLeft:
            {
                if (!minimapSpec.labelBottomInside && (showLocation || showBiome))
                {
                    int labels = showLocation ? 1 : 0;
                    labels += showBiome ? 1 : 0;

                    marginY = Math.max(marginY, minimapSpec.labelBottomMargin + (labels * locationLabelHeight) + compassLabelHeight / 2);
                }

                textureX = marginX;
                textureY = mc.displayHeight - (minimapHeight) - marginY;
                translateX = -(mc.displayWidth / 2) + halfWidth + marginX;
                translateY = (mc.displayHeight / 2) - halfHeight - marginY;
                break;
            }
            case TopCenter:
            {
                if (!minimapSpec.labelTopInside && showFps)
                {
                    marginY = Math.max(marginY, Math.max(compassLabelHeight / 2, minimapSpec.labelTopMargin) + fpsLabelHeight);
                }
                textureX = (mc.displayWidth - minimapWidth) / 2;
                textureY = marginY;
                translateX = 0;
                translateY = -(mc.displayHeight / 2) + halfHeight + marginY;
                break;
            }
            case Center:
            {
                textureX = (mc.displayWidth - minimapWidth) / 2;
                textureY = (mc.displayHeight - minimapHeight) / 2;
                translateX = 0;
                translateY = 0;
                break;
            }
            case TopRight:
            default:
            {
                if (!minimapSpec.labelTopInside && showFps)
                {
                    marginY = Math.max(marginY, Math.max(compassLabelHeight / 2, minimapSpec.labelTopMargin) + fpsLabelHeight);
                }

                textureX = mc.displayWidth - minimapWidth - marginX;
                textureY = marginY;
                translateX = (mc.displayWidth / 2) - halfWidth - marginX;
                translateY = -(mc.displayHeight / 2) + halfHeight + marginY;
                break;
            }
        }

        // Set frame position
        this.minimapFrame.setPosition(textureX, textureY);

        // Assign frame rectangle and centers
        this.centerPoint = new Point2D.Double(textureX + halfWidth, textureY + halfHeight);

        // Set up compass poionts
        this.minimapCompassPoints = new ThemeCompassPoints(textureX, textureY, halfWidth, halfHeight, minimapSpec,
                miniMapProperties, this.minimapFrame.getCompassPoint(), compassLabelHeight);

        // Set up key positions
        double centerX = Math.floor(textureX + (minimapWidth / 2));
        double topY = textureY;
        double bottomY = textureY + minimapHeight;

        if (showFps)
        {
            int topMargin = Math.max(compassLabelHeight / 2, minimapSpec.labelTopMargin);
            int yOffsetFps = minimapSpec.labelTopInside ? minimapSpec.labelTopMargin : -topMargin;
            DrawUtil.VAlign valignFps = minimapSpec.labelTopInside ? DrawUtil.VAlign.Below : DrawUtil.VAlign.Above;
            labelFps = new LabelVars(this, centerX, topY + yOffsetFps, DrawUtil.HAlign.Center, valignFps, fontScale, minimapSpec.fpsLabel);
        }
        else
        {
            labelFps = null;
        }


        int labelMargin = Math.max(compassLabelHeight / 2, minimapSpec.labelBottomMargin);
        int yOffset = minimapSpec.labelBottomInside ? -labelMargin : labelMargin;

        if (showLocation)
        {
            DrawUtil.VAlign vAlign = minimapSpec.labelBottomInside ? DrawUtil.VAlign.Above : DrawUtil.VAlign.Below;
            labelLocation = new LabelVars(this, centerX, bottomY + yOffset, DrawUtil.HAlign.Center, vAlign, fontScale, minimapSpec.locationLabel);
            if (showBiome)
            {
                yOffset += locationLabelHeight;
            }
        }
        else
        {
            labelLocation = null;
        }

        if (showBiome)
        {
            DrawUtil.VAlign vAlign = (minimapSpec.labelBottomInside) ? DrawUtil.VAlign.Above : DrawUtil.VAlign.Below;
            labelBiome = new LabelVars(this, centerX, bottomY + yOffset, DrawUtil.HAlign.Center, vAlign, fontScale, minimapSpec.biomeLabel);
        }
        else
        {
            labelBiome = null;
        }

    }


}
