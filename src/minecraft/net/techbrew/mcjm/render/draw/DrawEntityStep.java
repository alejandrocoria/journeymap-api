package net.techbrew.mcjm.render.draw;

import net.techbrew.mcjm.render.overlay.GridRenderer;
import net.techbrew.mcjm.render.texture.TextureImpl;

import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawEntityStep implements DrawStep {
    final double posX;
    final double posZ;
    final Double heading;
    final boolean flip;
    final TextureImpl texture;
    final int bottomMargin;

    public DrawEntityStep(double posX, double posZ, Double heading, boolean flip, TextureImpl texture, int bottomMargin) {
        super();
        this.posX = posX;
        this.posZ = posZ;
        this.heading = heading;
        this.flip = flip;
        this.texture = texture;
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
        Point2D pixel = gridRenderer.getPixel(posX, posZ);
        if (pixel != null) {
            DrawUtil.drawEntity(pixel.getX() + xOffset, pixel.getY() + yOffset, heading, flip, texture, bottomMargin);
        }
    }
}
