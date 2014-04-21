package net.techbrew.journeymap.render.draw;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderHell;
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

    /**
     * Draw just the editor icon
     * @param posX
     * @param posZ
     */
    public DrawWayPointStep(double posX, double posZ)
    {
        this(posX, posZ, TextureCache.instance().getWaypointEdit(), TextureCache.instance().getWaypointEdit(), null, Color.white, Color.white, 255);
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
     */
    public DrawWayPointStep(double posX, double posZ, TextureImpl texture, TextureImpl offScreenTexture, String label,
                            Color color, Color fontColor, int alpha)
    {
        this.posX = posX;
        this.posZ = posZ;
        this.texture = texture;
        this.offScreenTexture = offScreenTexture;
        this.label = label;
        this.color = color;
        this.fontColor = fontColor;
        this.alpha = alpha;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale) {

        double aPosX = posX + .5;
        double aPosZ = posZ + .5;
        int halfBlock = (int) Math.pow(2,gridRenderer.getZoom())/2;

        Point2D.Double pixel;
        if(WorldProvider.getProviderForDimension(Minecraft.getMinecraft().thePlayer.dimension) instanceof WorldProviderHell)
        {
            pixel = gridRenderer.getBlockPixelInGrid(aPosX/8, aPosZ/8);
        }
        else
        {
            pixel = gridRenderer.getBlockPixelInGrid(aPosX, aPosZ);
        }

        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        if (gridRenderer.isOnScreen(pixel))
        {
            double halfTexHeight = texture.height/2;
            if(label!=null)
            {
                DrawUtil.drawLabel(label, pixel.getX(), pixel.getY()-halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, alpha, fontColor, 255, fontScale, false);
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
