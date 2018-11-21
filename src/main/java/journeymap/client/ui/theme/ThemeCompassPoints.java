/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
     * The Font scale.
     */
    final float fontScale;
    /**
     * The Compass label height.
     */
    final int compassLabelHeight;

    /**
     * Spec for compass labels
     */
    final Theme.LabelSpec compassLabel;

    /**
     * Spec for compass point color
     */
    final Theme.ColorSpec compassPoint;

    /**
     * The Compass point tex.
     */
    final TextureImpl compassPointTex;

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
        this.compassLabel = minimapSpec.compassLabel;
        this.compassPoint = minimapSpec.compassPoint;

        this.compassPointTex = compassPointTex;
        if (this.compassPointTex != null)
        {
            // Deal with theme-specified offsets
            this.shiftVert = minimapSpec.compassPointOffset * fontScale;
            this.shiftHorz = minimapSpec.compassPointOffset * fontScale;

            pointNorth.setLocation(pointNorth.getX(), pointNorth.getY() - shiftVert);
            pointSouth.setLocation(pointSouth.getX(), pointSouth.getY() + shiftVert);

            pointWest.setLocation(pointWest.getX() - shiftHorz, pointWest.getY());
            pointEast.setLocation(pointEast.getX() + shiftHorz, pointEast.getY());

            xOffset = (int) (((compassPointTex.getWidth() * fontScale) / 2));
            yOffset = (int) (((compassPointTex.getHeight() * fontScale) / 2));

        }
        else
        {
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
            int color = compassPoint.getColor();
            float alpha = compassPoint.alpha;

            if (showNorth)
            {
                DrawUtil.drawColoredImage(compassPointTex, color, alpha, pointNorth.getX() - xOffset, pointNorth.getY() - yOffset, fontScale, 0);
            }

            if (showSouth)
            {
                DrawUtil.drawColoredImage(compassPointTex, color, alpha, pointSouth.getX() - xOffset, pointSouth.getY() - yOffset, fontScale, 180);
            }

            if (showWest)
            {
                DrawUtil.drawColoredImage(compassPointTex, color, alpha, pointWest.getX() - xOffset, pointWest.getY() - yOffset, fontScale, -90);
            }

            if (showEast)
            {
                DrawUtil.drawColoredImage(compassPointTex, color, alpha, pointEast.getX() - xOffset, pointEast.getY() - yOffset, fontScale, 90);
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
            DrawUtil.drawLabel(textNorth, compassLabel, pointNorth.getX(), pointNorth.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, fontScale, rotation);
        }

        if (showSouth)
        {
            DrawUtil.drawLabel(textSouth, compassLabel, pointSouth.getX(), pointSouth.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, fontScale, rotation);
        }

        if (showWest)
        {
            DrawUtil.drawLabel(textWest, compassLabel, pointWest.getX(), pointWest.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, fontScale, rotation);
        }

        if (showEast)
        {
            DrawUtil.drawLabel(textEast, compassLabel, pointEast.getX(), pointEast.getY() + labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, fontScale, rotation);
        }
    }
}