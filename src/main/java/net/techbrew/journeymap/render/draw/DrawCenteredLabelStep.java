package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawCenteredLabelStep implements DrawStep {

    final double posX;
    final double posZ;
    final String text;
    final int labelYOffset;
    final Color bgColor;
    final Color fgColor;
    final double fontScale;

    public DrawCenteredLabelStep(double posX, double posZ, String text, int labelYOffset, Color bgColor, Color fgColor, double fontScale) {
        this.posX = posX;
        this.posZ = posZ;
        this.text = text;
        this.labelYOffset = labelYOffset;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.fontScale = fontScale;
    }

    @Override
    public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
        Point2D pixel = gridRenderer.getPixel(posX, posZ);
        if (pixel != null) {
            DrawUtil.drawCenteredLabel(text, pixel.getX() + xOffset, pixel.getY() + yOffset + labelYOffset, bgColor, fgColor, 205, fontScale);
        }
    }
}
