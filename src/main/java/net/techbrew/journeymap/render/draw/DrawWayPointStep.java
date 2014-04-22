package net.techbrew.journeymap.render.draw;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderHell;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawWayPointStep implements DrawStep {

    final Waypoint waypoint;
    final Color color;
    final Color fontColor;
    final int alpha;
    final boolean isEdit;

    /**
     * Draw a waypoint on the map.
     * @param waypoint
     */
    public DrawWayPointStep(Waypoint waypoint)
    {
        this.waypoint = waypoint;
        this.color = waypoint.getColor();
        this.fontColor = waypoint.isDeathPoint() ? Color.red : this.color;
        this.alpha = 200;
        this.isEdit = false;
    }

    /**
     * Draw a waypoint on the map.
     * @param waypoint
     * @param color
     * @param fontColor
     * @param alpha
     */
    public DrawWayPointStep(Waypoint waypoint, Color color, Color fontColor, int alpha, boolean isEdit)
    {
        this.waypoint = waypoint;
        this.color = color;
        this.fontColor = fontColor;
        this.alpha = alpha;
        this.isEdit = isEdit;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale) {

        double aPosX = waypoint.getX();
        double aPosZ = waypoint.getZ();
        int halfBlock = (int) Math.pow(2,gridRenderer.getZoom())/2;

        Point2D.Double pixel;
        if(WorldProvider.getProviderForDimension(Minecraft.getMinecraft().thePlayer.dimension) instanceof WorldProviderHell)
        {
            pixel = gridRenderer.getBlockPixelInGrid(aPosX, aPosZ);
        }
        else
        {
            pixel = gridRenderer.getBlockPixelInGrid(aPosX, aPosZ);
        }

        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        if (gridRenderer.isOnScreen(pixel))
        {
            TextureImpl tex = waypoint.getTexture();
            double halfTexHeight = tex.height/2;
            DrawUtil.drawLabel(waypoint.getName(), pixel.getX(), pixel.getY()-halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, alpha, fontColor, 255, fontScale, false);
            if(isEdit)
            {
                TextureImpl editTex = TextureCache.instance().getWaypointEdit();
                DrawUtil.drawColoredImage(editTex, alpha, color, pixel.getX() - (editTex.width / 2), pixel.getY() - editTex.height/2);
            }
            DrawUtil.drawColoredImage(tex, alpha, color, pixel.getX() - (tex.width / 2), pixel.getY() - halfTexHeight);
        }
        else if(!isEdit)
        {
            gridRenderer.ensureOnScreen(pixel);
            TextureImpl tex =TextureCache.instance().getWaypointOffscreen();
            DrawUtil.drawColoredImage(tex, alpha, color, pixel.getX() - (tex.width / 2), pixel.getY() - (tex.height / 2));
        }
    }
}
