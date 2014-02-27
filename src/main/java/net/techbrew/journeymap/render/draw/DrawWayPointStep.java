package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawWayPointStep implements DrawStep {

    final double posX;
    final double posZ;
    final TextureImpl texture;
    final TextureImpl offScreenTexture;
    final String label;
    final Color color;
    final Color fontColor;
    final int alpha;
    final double fontScale;

    public DrawWayPointStep(double posX, double posZ, TextureImpl texture, TextureImpl offScreenTexture, String label,
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
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float scale) {
        Point2D pixel = gridRenderer.getBlockPixelInGrid(posX, posZ);
        if (gridRenderer.isOnScreen(pixel.getX(), pixel.getY()))
        {
            if (gridRenderer.isOnScreen(pixel.getX() + xOffset, pixel.getY() + yOffset)) {
                DrawUtil.drawColoredImage(texture, alpha, color, pixel.getX() + xOffset - (texture.width / 2), pixel.getY() + yOffset - (texture.height / 2));
                DrawUtil.drawCenteredLabel(label, pixel.getX() + xOffset, pixel.getY() + yOffset - texture.height, Color.black, alpha, fontColor, 255, fontScale);
            }
            else
            {
                pixel.setLocation(pixel.getX() + xOffset, pixel.getY() + yOffset);
                gridRenderer.ensureOnScreen(pixel);
                DrawUtil.drawColoredImage(offScreenTexture, alpha, color, pixel.getX() - (offScreenTexture.width / 2), pixel.getY() - (offScreenTexture.height / 2));
            }
        }
        else
        {
            if (gridRenderer.isOnScreen(pixel.getX() + xOffset, pixel.getY() + yOffset)) {
                DrawUtil.drawColoredImage(texture, alpha, color, pixel.getX() + xOffset - (texture.width / 2), pixel.getY() + yOffset - (texture.height / 2));
                DrawUtil.drawCenteredLabel(label, pixel.getX() + xOffset, pixel.getY() + yOffset - texture.height, Color.black, alpha, fontColor, 255, fontScale);
            }
            else {
                gridRenderer.ensureOnScreen(pixel);
                DrawUtil.drawColoredImage(offScreenTexture, alpha, color, pixel.getX() - (offScreenTexture.width / 2), pixel.getY()  - (offScreenTexture.height / 2));
            }
        }
    }


}
