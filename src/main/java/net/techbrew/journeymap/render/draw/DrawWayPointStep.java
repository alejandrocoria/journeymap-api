package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureCache;
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

    /**
     * Draw just the editor icon
     * @param posX
     * @param posZ
     */
    public DrawWayPointStep(double posX, double posZ)
    {
        this(posX, posZ, TextureCache.instance().getWaypointEdit(), null, null, Color.white, Color.white, 255, 1f);
    }

    /**
     * Normal waypoint.
     * @param posX
     * @param posZ
     * @param texture
     * @param offScreenTexture
     * @param label
     * @param color
     * @param fontColor
     * @param alpha
     * @param fontScale
     */
    public DrawWayPointStep(double posX, double posZ, TextureImpl texture, TextureImpl offScreenTexture, String label,
                            Color color, Color fontColor, int alpha, double fontScale)
    {
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
        Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(posX + xOffset + .5, posZ + yOffset + .5);
        if (gridRenderer.isOnScreen(pixel))
        {
            double halfTexHeight = texture.height/2;
            if(label!=null)
            {
                DrawUtil.drawLabel(label, pixel.getX(), pixel.getY()-halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, alpha, fontColor, 180, fontScale, false);
            }
            DrawUtil.drawColoredImage(texture, alpha, color, pixel.getX() - (texture.width / 2), pixel.getY() - halfTexHeight);
        }
        else
        {
            gridRenderer.ensureOnScreen(pixel);
            DrawUtil.drawColoredImage(offScreenTexture, alpha, color, pixel.getX() - (offScreenTexture.width / 2), pixel.getY() - (offScreenTexture.height / 2));
        }
    }
}
