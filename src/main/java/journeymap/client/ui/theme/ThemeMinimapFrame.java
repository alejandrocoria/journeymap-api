/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.cartography.color.RGB;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.minimap.ReticleOrientation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Theme values for rendering the minimap frame.
 */
public class ThemeMinimapFrame
{
    private final Theme theme;
    private final Theme.Minimap.MinimapSpec minimapSpec;
    private final ReticleOrientation reticleOrientation;

    private final String resourcePattern;
    private TextureImpl textureTopLeft;
    private TextureImpl textureTop;
    private TextureImpl textureTopRight;
    private TextureImpl textureRight;
    private TextureImpl textureBottomRight;
    private TextureImpl textureBottom;
    private TextureImpl textureBottomLeft;
    private TextureImpl textureLeft;

    private double[] coordsTopLeft;
    private double[] coordsTop;
    private double[] coordsTopRight;
    private double[] coordsRight;
    private double[] coordsBottomRight;
    private double[] coordsBottom;
    private double[] coordsBottomLeft;
    private double[] coordsLeft;

    private TextureImpl textureCircle;
    private TextureImpl textureCircleMask;
    private TextureImpl textureCompassPoint;

    private final double ttlw;
    private final double tth;
    private final double ttl;
    private final double ttlh;
    private final double tblw;
    private final double tbh;
    private final double trw;
    private final double ttrh;
    private final double ttrw;
    private final double tblh;
    private final double tbrw;
    private final double tbrh;

    private double x;
    private double y;
    private int width;
    private int height;
    private boolean isSquare;
    private boolean showReticle;
    private int reticleOffsetOuter;
    private int reticleOffsetInner;
    private double reticleThickness;
    private double reticleHeadingThickness;
    private Rectangle.Double frameBounds;
    private double[] retNorth = null;
    private double[] retSouth = null;
    private double[] retEast = null;
    private double[] retWest = null;
    private final float frameAlpha;

    public ThemeMinimapFrame(Theme theme, Theme.Minimap.MinimapSpec minimapSpec, MiniMapProperties miniMapProperties, int width, int height)
    {
        this.theme = theme;
        this.minimapSpec = minimapSpec;
        this.width = width;
        this.height = height;
        this.reticleOrientation = miniMapProperties.reticleOrientation.get();

        if (minimapSpec instanceof Theme.Minimap.MinimapSquare)
        {
            isSquare = true;
            Theme.Minimap.MinimapSquare minimapSquare = (Theme.Minimap.MinimapSquare) minimapSpec;
            resourcePattern = "minimap/square/" + minimapSquare.prefix + "%s.png";

            textureTopLeft = getTexture("topleft", minimapSquare.topLeft);
            textureTop = getTexture("top", width - (minimapSquare.topLeft.width / 2) - (minimapSquare.topRight.width / 2), minimapSquare.top.height, true, false);
            textureTopRight = getTexture("topright", minimapSquare.topRight);
            textureRight = getTexture("right", minimapSquare.right.width, height - (minimapSquare.topRight.height / 2) - (minimapSquare.bottomRight.height / 2), true, false);
            textureBottomRight = getTexture("bottomright", minimapSquare.bottomRight);
            textureBottom = getTexture("bottom", width - (minimapSquare.bottomLeft.width / 2) - (minimapSquare.bottomRight.width / 2), minimapSquare.bottom.height, true, false);
            textureBottomLeft = getTexture("bottomleft", minimapSquare.bottomLeft);
            textureLeft = getTexture("left", minimapSquare.left.width, height - (minimapSquare.topLeft.height / 2) - (minimapSquare.bottomLeft.height / 2), true, false);

            // Pre-calculate texture positions for minimap frame
            ttlw = (textureTopLeft.getWidth() / 2D);
            tth = (textureTop.getHeight() / 2D);
            ttl = (textureLeft.getWidth() / 2D);
            ttlh = (textureTopLeft.getHeight() / 2D);
            tblw = (textureBottomLeft.getWidth() / 2D);
            tbh = (textureBottom.getHeight() / 2D);
            trw = (textureRight.getWidth() / 2D);
            ttrh = (textureTopRight.getHeight() / 2D);
            ttrw = (textureTopRight.getWidth() / 2D);
            tblh = (textureBottomLeft.getHeight() / 2D);
            tbrw = (textureBottomRight.getWidth() / 2D);
            tbrh = (textureBottomRight.getHeight() / 2D);
        }
        else
        {
            Theme.Minimap.MinimapCircle minimapCircle = (Theme.Minimap.MinimapCircle) minimapSpec;
            int imgSize = width <= 256 ? 256 : 512;
            resourcePattern = "minimap/circle/" + minimapCircle.prefix + "%s.png";

            TextureImpl tempMask = getTexture("mask_" + imgSize, imgSize, imgSize, false, true);
            textureCircleMask = TextureCache.getScaledCopy("scaledCircleMask", tempMask, width, height, 1f);

            TextureImpl tempCircle = getTexture("rim_" + imgSize, imgSize, imgSize, false, true);
            textureCircle = TextureCache.getScaledCopy("scaledCircleRim", tempCircle, width, height, minimapSpec.frame.alpha);

            // Unused
            ttlw = 0;
            tth = 0;
            ttl = 0;
            ttlh = 0;
            tblw = 0;
            tbh = 0;
            trw = 0;
            ttrh = 0;
            ttrw = 0;
            tblh = 0;
            tbrw = 0;
            tbrh = 0;
        }

        if (minimapSpec.compassPoint != null && minimapSpec.compassPoint.width > 0 && minimapSpec.compassPoint.height > 0)
        {
            textureCompassPoint = getTexture("compass_point", minimapSpec.compassPoint);
        }

        this.reticleThickness = minimapSpec.reticleThickness;
        this.reticleHeadingThickness = minimapSpec.reticleHeadingThickness;
        this.reticleOffsetOuter = minimapSpec.reticleOffsetOuter;
        this.reticleOffsetInner = minimapSpec.reticleOffsetInner;
        this.showReticle = miniMapProperties.showReticle.get() && (minimapSpec.reticle.alpha > 0 || minimapSpec.reticleHeading.alpha > 0);
        this.frameAlpha = Math.max(0f, Math.min(1f, miniMapProperties.frameAlpha.get() / 100f));
    }

    public void setPosition(final double x, final double y)
    {
        this.x = x;
        this.y = y;
        this.frameBounds = new Rectangle2D.Double(x, y, width, height);

        double centerX = x + (width / 2);
        double centerY = y + (height / 2);
        double segLengthNorthSouth = centerY - reticleOffsetInner - y - reticleOffsetOuter;
        double segLengthEastWest = centerX - reticleOffsetInner - x - reticleOffsetOuter;

        double thick;
        Theme.ColorSpec colorSpec;
        if (reticleOrientation == ReticleOrientation.Compass)
        {
            thick = reticleHeadingThickness;
            colorSpec = minimapSpec.reticleHeading;
        }
        else
        {
            thick = reticleThickness;
            colorSpec = minimapSpec.reticle;
        }

        // Precalculate numbers needed to draw north reticle segment
        retNorth = null;
        if (thick > 0 && colorSpec.alpha > 0)
        {
            retNorth = new double[6];
            retNorth[0] = centerX - (thick / 2);
            retNorth[1] = y + reticleOffsetOuter;
            retNorth[2] = thick;
            retNorth[3] = segLengthNorthSouth;
            retNorth[4] = colorSpec.getColor();
            retNorth[5] = colorSpec.alpha;
        }

        if (reticleOrientation == ReticleOrientation.PlayerHeading)
        {
            thick = reticleHeadingThickness;
            colorSpec = minimapSpec.reticleHeading;
        }
        else
        {
            thick = reticleThickness;
            colorSpec = minimapSpec.reticle;
        }

        // Precalculate numbers needed to draw south reticle segment
        retSouth = null;
        if (thick > 0 && colorSpec.alpha > 0)
        {
            retSouth = new double[6];
            retSouth[0] = centerX - (thick / 2);
            retSouth[1] = centerY + reticleOffsetInner;
            retSouth[2] = thick;
            retSouth[3] = segLengthNorthSouth;
            retSouth[4] = colorSpec.getColor();
            retSouth[5] = colorSpec.alpha;
        }

        thick = reticleThickness;
        colorSpec = minimapSpec.reticle;

        // Precalculate numbers needed to draw west reticle segment
        retWest = null;
        if (thick > 0 && colorSpec.alpha > 0)
        {
            retWest = new double[6];
            retWest[0] = centerX - reticleOffsetInner - segLengthEastWest;
            retWest[1] = centerY - (thick / 2);
            retWest[2] = segLengthEastWest;
            retWest[3] = reticleThickness;
            retWest[4] = colorSpec.getColor();
            retWest[5] = colorSpec.alpha;
        }

        // Precalculate numbers needed to draw east reticle segment
        retEast = null;
        if (thick > 0 && colorSpec.alpha > 0)
        {
            retEast = new double[6];
            retEast[0] = centerX + reticleOffsetInner;
            retEast[1] = centerY - (thick / 2);
            retEast[2] = segLengthEastWest;
            retEast[3] = reticleThickness;
            retEast[4] = colorSpec.getColor();
            retEast[5] = colorSpec.alpha;
        }

        // Precalculate numbers needed to draw frame
        if (isSquare)
        {
            int frameColor = minimapSpec.frame.getColor();
            float alpha = minimapSpec.frame.alpha * this.frameAlpha;

            coordsTopLeft = new double[]{frameColor, alpha, x - ttlw, y - ttlh, 1, 0};
            coordsTop = new double[]{frameColor, alpha, x + ttlw, y - tth, 1, 0};
            coordsTopRight = new double[]{frameColor, alpha, x + width - ttrw, y - ttrh, 1, 0};
            coordsRight = new double[]{frameColor, alpha, x + width - trw, y + ttrh, 1, 0};
            coordsBottomRight = new double[]{frameColor, alpha, x + width - tbrw, y + height - tbrh, 1, 0};
            coordsBottom = new double[]{frameColor, alpha, x + tblw, y + height - tbh, 1, 0};
            coordsBottomLeft = new double[]{frameColor, alpha, x - tblw, y + height - tblh, 1, 0};
            coordsLeft = new double[]{frameColor, alpha, x - ttl, y + ttlh, 1, 0};
        }
    }

    public void drawMask()
    {
        if (isSquare)
        {
            DrawUtil.drawRectangle(x, y, this.width, this.height, RGB.WHITE_RGB, 1f);
        }
        else
        {
            DrawUtil.drawQuad(textureCircleMask, 0xffffff, 1f, x, y, this.width, this.height, 0, 0, 1, 1, 0, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
        }
    }

    public void drawReticle()
    {
        if (showReticle)
        {
            // North
            if (retNorth != null)
            {
                DrawUtil.drawRectangle(retNorth[0], retNorth[1], retNorth[2], retNorth[3], (int) retNorth[4], (float) retNorth[5]);
            }

            // South
            if (retSouth != null)
            {
                DrawUtil.drawRectangle(retSouth[0], retSouth[1], retSouth[2], retSouth[3], (int) retSouth[4], (float) retSouth[5]);
            }

            // West
            if (retWest != null)
            {
                DrawUtil.drawRectangle(retWest[0], retWest[1], retWest[2], retWest[3], (int) retWest[4], (float) retWest[5]);
            }

            // East
            if (retEast != null)
            {
                DrawUtil.drawRectangle(retEast[0], retEast[1], retEast[2], retEast[3], (int) retEast[4], (float) retEast[5]);
            }
        }
    }

    public void drawFrame()
    {
        if (minimapSpec.frame.alpha > 0)
        {
            if (isSquare)
            {
                DrawUtil.drawClampedImage(textureTopLeft, (int) coordsTopLeft[0], (float) coordsTopLeft[1], coordsTopLeft[2], coordsTopLeft[3], (float) coordsTopLeft[4], coordsTopLeft[5]);
                DrawUtil.drawClampedImage(textureTop, (int) coordsTop[0], (float) coordsTop[1], coordsTop[2], coordsTop[3], (float) coordsTop[4], coordsTop[5]);
                DrawUtil.drawClampedImage(textureTopRight, (int) coordsTopRight[0], (float) coordsTopRight[1], coordsTopRight[2], coordsTopRight[3], (float) coordsTopRight[4], coordsTopRight[5]);
                DrawUtil.drawClampedImage(textureRight, (int) coordsRight[0], (float) coordsRight[1], coordsRight[2], coordsRight[3], (float) coordsRight[4], coordsRight[5]);
                DrawUtil.drawClampedImage(textureBottomRight, (int) coordsBottomRight[0], (float) coordsBottomRight[1], coordsBottomRight[2], coordsBottomRight[3], (float) coordsBottomRight[4], coordsBottomRight[5]);
                DrawUtil.drawClampedImage(textureBottom, (int) coordsBottom[0], (float) coordsBottom[1], coordsBottom[2], coordsBottom[3], (float) coordsBottom[4], coordsBottom[5]);
                DrawUtil.drawClampedImage(textureBottomLeft, (int) coordsBottomLeft[0], (float) coordsBottomLeft[1], coordsBottomLeft[2], coordsBottomLeft[3], (float) coordsBottomLeft[4], coordsBottomLeft[5]);
                DrawUtil.drawClampedImage(textureLeft, (int) coordsLeft[0], (float) coordsLeft[1], coordsLeft[2], coordsLeft[3], (float) coordsLeft[4], coordsLeft[5]);
            }
            else
            {
                float alpha = minimapSpec.frame.alpha * this.frameAlpha;
                DrawUtil.drawQuad(textureCircle, minimapSpec.frame.getColor(), alpha, x, y, this.width, this.height, 0, 0, 1, 1, 0, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
            }
        }
    }

    public TextureImpl getCompassPoint()
    {
        return textureCompassPoint;
    }

    private TextureImpl getTexture(String suffix, Theme.ImageSpec imageSpec)
    {
        return getTexture(suffix, imageSpec.width, imageSpec.height, true, false);
    }

    private TextureImpl getTexture(String suffix, int width, int height, boolean resize, boolean retain)
    {
        return TextureCache.getSizedThemeTexture(theme, String.format(resourcePattern, suffix), width, height, resize, 1f, retain);
    }

    public Rectangle.Double getFrameBounds()
    {
        return frameBounds;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public ReticleOrientation getReticleOrientation()
    {
        return reticleOrientation;
    }
}
