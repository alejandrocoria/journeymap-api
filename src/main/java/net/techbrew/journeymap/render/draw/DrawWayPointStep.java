package net.techbrew.journeymap.render.draw;

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
    final TextureImpl texture;
    final TextureImpl offscreenTexture = TextureCache.instance().getWaypointOffscreen();
    final boolean isEdit;

    /**
     * Draw a waypoint on the map.
     * @param waypoint
     */
    public DrawWayPointStep(Waypoint waypoint)
    {
        this(waypoint, waypoint.getColor(), waypoint.isDeathPoint() ? Color.red : waypoint.getSafeColor(), false);
    }

    /**
     * Draw a waypoint on the map.
     * @param waypoint
     * @param color
     * @param fontColor
     */
    public DrawWayPointStep(Waypoint waypoint, Color color, Color fontColor, boolean isEdit)
    {
        this.waypoint = waypoint;
        this.color = color;
        this.fontColor = fontColor;
        this.isEdit = isEdit;
        this.texture = waypoint.getTexture();
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale) {

        double x = waypoint.getX(gridRenderer.getDimension());
        double z = waypoint.getZ(gridRenderer.getDimension());
        double halfBlock = Math.pow(2,gridRenderer.getZoom())/2;

        Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(x, z);
        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        if (gridRenderer.isOnScreen(pixel))
        {
            double halfTexHeight = texture.height/2;
            DrawUtil.drawLabel(waypoint.getName(), pixel.getX(), pixel.getY()-halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 255, fontColor, 255, fontScale, false);
            if(isEdit)
            {
                TextureImpl editTex = TextureCache.instance().getWaypointEdit();
                DrawUtil.drawColoredImage(editTex, 255, color, pixel.getX() - (editTex.width / 2), pixel.getY() - editTex.height/2);
            }
            DrawUtil.drawColoredImage(texture, 255, color, pixel.getX() - (texture.width / 2), pixel.getY() - halfTexHeight);
        }
        else if(!isEdit)
        {
            gridRenderer.ensureOnScreen(pixel);
            DrawUtil.drawColoredImage(offscreenTexture, 255, color, pixel.getX() - (offscreenTexture.width / 2), pixel.getY() - (offscreenTexture.height / 2));
        }
    }
}
