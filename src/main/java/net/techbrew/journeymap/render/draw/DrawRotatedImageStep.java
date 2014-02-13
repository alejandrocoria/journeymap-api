package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawRotatedImageStep implements DrawStep {

    final int posX;
    final int posZ;
    final TextureImpl texture;
    final float heading;

    public DrawRotatedImageStep(int posX, int posZ, TextureImpl texture, float heading) {
        super();
        this.posX = posX;
        this.posZ = posZ;
        this.texture = texture;
        this.heading = heading;
    }

    @Override
    public void draw(int xOffset, int yOffset, GridRenderer gridRenderer, float scale) {
        Point2D pixel = gridRenderer.getPixel(posX, posZ);
        if (pixel != null) {
            DrawUtil.drawRotatedImage(texture, pixel.getX() + xOffset, pixel.getY() + yOffset, heading, scale);
        }
    }
}
