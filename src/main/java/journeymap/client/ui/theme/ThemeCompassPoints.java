/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureImpl;

import java.awt.geom.Point2D;

/**
 * Handles the drawing of compass points on minimap
 */
public class ThemeCompassPoints
{
    /**
     * The Text north.
     */
    final String textNorth = Constants.getString("jm.minimap.compass.n");
    /**
     * The Text south.
     */
    final String textSouth = Constants.getString("jm.minimap.compass.s");
    /**
     * The Text east.
     */
    final String textEast = Constants.getString("jm.minimap.compass.e");
    /**
     * The Text west.
     */
    final String textWest = Constants.getString("jm.minimap.compass.w");
    /**
     * The Point north.
     */
    final Point2D pointNorth;
    /**
     * The Point south.
     */
    final Point2D pointSouth;
    /**
     * The Point west.
     */
    final Point2D pointWest;
    /**
     * The Point east.
     */
    final Point2D pointEast;
    /**
     * The Show north.
     */
    final boolean showNorth;
    /**
     * The Show south.
     */
    final boolean showSouth;
    /**
     * The Show east.
     */
    final boolean showEast;
    /**
     * The Show west.
     */
    final boolean showWest;
    /**
     * The Bg alpha.
     */
    final float bgAlpha;
    /**
     * The Bg color.
     */
    final Integer bgColor;
    /**
     * The Fg alpha.
     */
    final float fgAlpha;
    /**
     * The Fg color.
     */
    final Integer fgColor;
    /**
     * The Font scale.
     */
    final double fontScale;
    /**
     * The Compass label height.
     */
    final int compassLabelHeight;
    /**
     * The Compass point color.
     */
    final Integer compassPointColor;
    /**
     * The Compass point tex.
     */
    final TextureImpl compassPointTex;
    /**
     * The Compass point scale.
     */
    final float compassPointScale;
    /**
     * The X offset.
     */
    final int xOffset;
    /**
     * The Y offset.
     */
    final int yOffset;
    /**
     * The Shift vert.
     */
    final double shiftVert;
    /**
     * The Shift horz.
     */
    final double shiftHorz;
    /**
     * The Label shift vert.
     */
    final int labelShiftVert;
    private double x;
    private double y;

    /**
     * Instantiates a new Theme compass points.
     *
     * @param x                 the x
     * @param y                 the y
     * @param halfWidth         the half width
     * @param halfHeight        the half height
     * @param minimapSpec       the minimap spec
     * @param miniMapProperties the mini map properties
     * @param compassPointTex   the compass point tex
     * @param labelHeight       the label height
     */
    public ThemeCompassPoints(int x, int y, int halfWidth, int halfHeight, Theme.Minimap.MinimapSpec minimapSpec, MiniMapProperties miniMapProperties, TextureImpl compassPointTex, int labelHeight)
    {
        this.x = x;
        this.y = y;

        pointNorth = new Point2D.Double(x + halfWidth, y);
        pointSouth = new Point2D.Double(x + halfWidth, y + halfHeight + halfHeight);
        pointWest = new Point2D.Double(x, y + halfHeight);
        pointEast = new Point2D.Double(x + halfWidth + halfWidth, y + halfHeight);
        this.fontScale = miniMapProperties.compassFontScale.get();
        this.compassLabelHeight = labelHeight;

        bgAlpha = Theme.getAlpha(minimapSpec.compassLabel.backgroundAlpha);
        fgAlpha = Theme.getAlpha(minimapSpec.compassLabel.foregroundAlpha);
        bgColor = Theme.getColor(minimapSpec.compassLabel.backgroundColor);
        fgColor = Theme.getColor(minimapSpec.compassLabel.foregroundColor);

        this.compassPointTex = compassPointTex;
        compassPointColor = Theme.getColor(minimapSpec.compassPointColor);
        if (this.compassPointTex != null)
        {
            // Scale to accommodate font
            compassPointScale = getCompassPointScale(compassLabelHeight, minimapSpec, compassPointTex);

            // Deal with theme-specified offsets
            this.shiftVert = minimapSpec.compassPointOffset * compassPointScale;
            this.shiftHorz = minimapSpec.compassPointOffset * compassPointScale;

            pointNorth.setLocation(pointNorth.getX(), pointNorth.getY() - shiftVert);
            pointSouth.setLocation(pointSouth.getX(), pointSouth.getY() + shiftVert);

            pointWest.setLocation(pointWest.getX() - shiftHorz, pointWest.getY());
            pointEast.setLocation(pointEast.getX() + shiftHorz, pointEast.getY());

            xOffset = (int) (((compassPointTex.getWidth() * compassPointScale) / 2));
            yOffset = (int) (((compassPointTex.getHeight() * compassPointScale) / 2));

        }
        else
        {
            compassPointScale = 0;
            xOffset = 0;
            yOffset = 0;
            shiftHorz = 0;
            shiftVert = 0;
        }

        labelShiftVert = 0;//Minecraft.getMinecraft().fontRenderer.getUnicodeFlag() ? (int) fontScale : 0;

        showNorth = minimapSpec.compassShowNorth;
        showSouth = minimapSpec.compassShowSouth;
        showEast = minimapSpec.compassShowEast;
        showWest = minimapSpec.compassShowWest;
    }

    /**
     * Gets compass point scale.
     *
     * @param compassLabelHeight the compass label height
     * @param minimapSpec        the minimap spec
     * @param compassPointTex    the compass point tex
     * @return the compass point scale
     */
    public static float getCompassPointScale(int compassLabelHeight, Theme.Minimap.MinimapSpec minimapSpec, TextureImpl compassPointTex)
    {
        return (compassLabelHeight + minimapSpec.compassPointLabelPad) / (compassPointTex.getHeight() * 1f);
    }

    /**
     * Sets position.
     *
     * @param x the x
     * @param y the y
     */
    public void setPosition(final double x, final double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Draw points.
     *
     * @param rotation the rotation
     */
    public void drawPoints(double rotation)
    {
        if (compassPointTex != null)
        {
            if (showNorth)
            {
                DrawUtil.drawColoredImage(compassPointTex, compassPointColor, 1f, pointNorth.getX() - xOffset, pointNorth.getY() - yOffset, compassPointScale, 0);
            }

            if (showSouth)
            {
                DrawUtil.drawColoredImage(compassPointTex, compassPointColor, 1f, pointSouth.getX() - xOffset, pointSouth.getY() - yOffset, compassPointScale, 180);
            }

            if (showWest)
            {
                DrawUtil.drawColoredImage(compassPointTex, compassPointColor, 1f, pointWest.getX() - xOffset, pointWest.getY() - yOffset, compassPointScale, -90);
            }

            if (showEast)
            {
                DrawUtil.drawColoredImage(compassPointTex, compassPointColor, 1f, pointEast.getX() - xOffset, pointEast.getY() - yOffset, compassPointScale, 90);
            }
        }
    }

    /**
     * Draw labels.
     *
     * @param rotation the rotation
     */
    public void drawLabels(double rotation)
    {

        if (showNorth)
        {
            DrawUtil.drawLabel(textNorth, pointNorth.getX(), pointNorth.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }

        if (showSouth)
        {
            DrawUtil.drawLabel(textSouth, pointSouth.getX(), pointSouth.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }

        if (showWest)
        {
            DrawUtil.drawLabel(textWest, pointWest.getX(), pointWest.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }

        if (showEast)
        {
            DrawUtil.drawLabel(textEast, pointEast.getX(), pointEast.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }
    }
}