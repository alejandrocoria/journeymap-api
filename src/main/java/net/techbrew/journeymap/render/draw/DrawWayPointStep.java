package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawWayPointStep implements DrawStep {

    final int posX;
    final int posZ;
    final TextureImpl texture;
    final TextureImpl offScreenTexture;
    final String label;
    final Color color;
    final Color fontColor;
    final int alpha;
    final double fontScale;

    public DrawWayPointStep(int posX, int posZ, TextureImpl texture, TextureImpl offScreenTexture, String label,
                            Color color, Color fontColor, int alpha, double fontScale) {
        super();
        this.posX = posX;
        this.posZ = posZ;
        this.texture = texture;
        this.offScreenTexture = offScreenTexture;
        this.label = label;
        this.color = color;
        this.fontColor = fontColor;
        this.alpha = alpha;
        this.fontScale = fontScale;
    }

    @Override
    public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
        Point2D pixel = gridRenderer.getBlockPixelInGrid(posX, posZ);
        if (gridRenderer.isOnScreen(pixel.getX(), pixel.getY())) {
            DrawUtil.drawColoredImage(texture, alpha, color, pixel.getX() + xOffset - (texture.width / 2), pixel.getY() + yOffset - (texture.height / 2));
            DrawUtil.drawCenteredLabel(label, pixel.getX(), pixel.getY() - texture.height, Color.black, fontColor, alpha, fontScale);
        } else {
            gridRenderer.ensureOnScreen(pixel);
            DrawUtil.drawColoredImage(offScreenTexture, alpha, color, pixel.getX() + xOffset - (offScreenTexture.width / 2), pixel.getY() + yOffset - (offScreenTexture.height / 2));
        }
    }
}
