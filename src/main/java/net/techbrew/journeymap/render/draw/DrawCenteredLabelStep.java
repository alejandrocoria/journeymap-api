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

    public DrawCenteredLabelStep(double posX, double posZ, String text, int labelYOffset, Color bgColor, Color fgColor) {
        this.posX = posX;
        this.posZ = posZ;
        this.text = text;
        this.labelYOffset = labelYOffset;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale) {
        Point2D pixel = gridRenderer.getPixel(posX, posZ);
        if (pixel != null) {
            DrawUtil.drawCenteredLabel(text, pixel.getX() + xOffset, pixel.getY() + yOffset + labelYOffset, bgColor, 205, fgColor, 255, fontScale);
        }
    }
}
