package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Handles the drawing of compass points on minimap
 */
public class ThemeCompassPoints
{
    final String textNorth = Constants.getString("jm.minimap.compass.n");
    final String textSouth = Constants.getString("jm.minimap.compass.s");
    final String textEast = Constants.getString("jm.minimap.compass.e");
    final String textWest = Constants.getString("jm.minimap.compass.w");
    final Point2D pointNorth;
    final Point2D pointSouth;
    final Point2D pointWest;
    final Point2D pointEast;
    final boolean showNorth;
    final boolean showSouth;
    final boolean showEast;
    final boolean showWest;
    final int bgAlpha;
    final Color bgColor;
    final int fgAlpha;
    final Color fgColor;
    final double fontScale;
    final int compassLabelHeight;
    final Color compassPointColor;
    final TextureImpl compassPointTex;
    final float compassPointScale;
    final int xOffset;
    final int yOffset;
    final double shiftVert;
    final double shiftHorz;
    private double x;
    private double y;

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

        bgAlpha = minimapSpec.compassLabel.backgroundAlpha;
        fgAlpha = minimapSpec.compassLabel.foregroundAlpha;
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

            xOffset = (int) (((compassPointTex.width * compassPointScale) / 2));
            yOffset = (int) (((compassPointTex.height * compassPointScale) / 2));

        }
        else
        {
            compassPointScale = 0;
            xOffset = 0;
            yOffset = 0;
            shiftHorz = 0;
            shiftVert = 0;
        }

        showNorth = minimapSpec.compassShowNorth;
        showSouth = minimapSpec.compassShowSouth;
        showEast = minimapSpec.compassShowEast;
        showWest = minimapSpec.compassShowWest;
    }

    public static float getCompassPointScale(int compassLabelHeight, Theme.Minimap.MinimapSpec minimapSpec, TextureImpl compassPointTex)
    {
        return (compassLabelHeight + minimapSpec.compassPointLabelPad) / (compassPointTex.height * 1f);
    }

    public void setPosition(final double x, final double y)
    {
        this.x = x;
        this.y = y;
    }

    public void drawPoints(double rotation)
    {
        if (compassPointTex != null)
        {
            if (showNorth)
            {
                DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointNorth.getX() - xOffset, pointNorth.getY() - yOffset, compassPointScale, 0);
            }

            if (showSouth)
            {
                DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointSouth.getX() - xOffset, pointSouth.getY() - yOffset, compassPointScale, 180);
            }

            if (showWest)
            {
                DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointWest.getX() - xOffset, pointWest.getY() - yOffset, compassPointScale, -90);
            }

            if (showEast)
            {
                DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointEast.getX() - xOffset, pointEast.getY() - yOffset, compassPointScale, 90);
            }
        }
    }

    public void drawLabels(double rotation)
    {
        if (showNorth)
        {
            DrawUtil.drawLabel(textNorth, pointNorth.getX(), pointNorth.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }

        if (showSouth)
        {
            DrawUtil.drawLabel(textSouth, pointSouth.getX(), pointSouth.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }

        if (showWest)
        {
            DrawUtil.drawLabel(textWest, pointWest.getX(), pointWest.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }

        if (showEast)
        {
            DrawUtil.drawLabel(textEast, pointEast.getX(), pointEast.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        }
    }
}