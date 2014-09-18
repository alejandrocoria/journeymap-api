package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
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
    final int bgAlpha;
    final Color bgColor;
    final int fgAlpha;
    final Color fgColor;
    final double fontScale;
    final int compassLabelHeight;
    final Color compassPointColor;
    final TextureImpl compassPointTex;
    final float compassPointScale;
    final int compassPointXOffset;
    final int compassPointYOffset;
    private double x;
    private double y;

    public ThemeCompassPoints(int x, int y, int radius, Theme.Minimap.MinimapSpec minimapSpec, TextureImpl compassPointTex, boolean useUnicode, int labelHeight)
    {
        this.x = x;
        this.y = y;
        pointNorth = new Point2D.Double(x + radius, y);
        pointSouth = new Point2D.Double(x + radius, y + radius + radius);
        pointWest = new Point2D.Double(x, y + radius);
        pointEast = new Point2D.Double(x + radius + radius, y + radius);
        this.fontScale = (JourneyMap.getMiniMapProperties().compassFontSmall.get() ? 1 : 2) * (useUnicode ? 2 : 1);
        this.compassLabelHeight = labelHeight;

        bgAlpha = minimapSpec.compassLabel.backgroundAlpha;
        fgAlpha = minimapSpec.compassLabel.foregroundAlpha;
        bgColor = Theme.getColor(minimapSpec.compassLabel.backgroundColor);
        fgColor = Theme.getColor(minimapSpec.compassLabel.foregroundColor);

        this.compassPointTex = compassPointTex;
        compassPointColor = Theme.getColor(minimapSpec.compassPointColor);
        if(this.compassPointTex != null)
        {
            compassPointScale = (compassLabelHeight + minimapSpec.compassPointPad) / (compassPointTex.height *1f);
            compassPointXOffset = (int)(compassPointTex.width * compassPointScale) / 2;
            compassPointYOffset = (int)(compassPointTex.height * compassPointScale) / 2;
        }
        else
        {
            compassPointScale = 0;
            compassPointXOffset = 0;
            compassPointYOffset = 0;
        }

    }

    public void setPosition(final double x, final double y)
    {
        this.x = x;
        this.y = y;
    }

    public void drawPoints(double rotation)
    {
        if(compassPointTex !=null)
        {
            DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointNorth.getX() - compassPointXOffset, pointNorth.getY() - compassPointYOffset, compassPointScale, 0);
            DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointSouth.getX() - compassPointXOffset, pointSouth.getY() - compassPointYOffset, compassPointScale, 0);
            DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointWest.getX() - compassPointXOffset, pointWest.getY() - compassPointYOffset, compassPointScale, 0);
            DrawUtil.drawColoredImage(compassPointTex, 255, compassPointColor, pointEast.getX() - compassPointXOffset, pointEast.getY() - compassPointYOffset, compassPointScale, 0);
        }
    }

    public void drawLabels(double rotation)
    {
        DrawUtil.drawLabel(textNorth, pointNorth.getX(), pointNorth.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        DrawUtil.drawLabel(textSouth, pointSouth.getX(), pointSouth.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        DrawUtil.drawLabel(textWest, pointWest.getX(), pointWest.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
        DrawUtil.drawLabel(textEast, pointEast.getX(), pointEast.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, bgColor, bgAlpha, fgColor, fgAlpha, fontScale, true, rotation);
    }
}