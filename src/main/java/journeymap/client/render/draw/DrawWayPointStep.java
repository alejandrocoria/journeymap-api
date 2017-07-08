/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import com.google.common.cache.CacheLoader;
import journeymap.client.cartography.color.RGB;
import journeymap.client.model.Waypoint;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;

import java.awt.geom.Point2D;

/**
 * The type Draw way point step.
 *
 * @author techbrew 12/26/13.
 */
public class DrawWayPointStep implements DrawStep
{
    /**
     * The Waypoint.
     */
    public final Waypoint waypoint;
    /**
     * The Color.
     */
    final Integer color;
    /**
     * The Font color.
     */
    final Integer fontColor;
    /**
     * The Texture.
     */
    final TextureImpl texture;
    /**
     * The Is edit.
     */
    final boolean isEdit;
    /**
     * The Last position.
     */
    Point2D.Double lastPosition;
    /**
     * The Last on screen.
     */
    boolean lastOnScreen;
    /**
     * The Show label.
     */
    boolean showLabel;

    /**
     * Draw a waypoint on the map.
     *
     * @param waypoint the waypoint
     */
    public DrawWayPointStep(Waypoint waypoint)
    {
        this(waypoint, waypoint.getColor(), waypoint.isDeathPoint() ? RGB.RED_RGB : waypoint.getSafeColor(), false);
    }

    /**
     * Draw a waypoint on the map.
     *
     * @param waypoint  the waypoint
     * @param color     the color
     * @param fontColor the font color
     * @param isEdit    the is edit
     */
    public DrawWayPointStep(Waypoint waypoint, Integer color, Integer fontColor, boolean isEdit)
    {
        this.waypoint = waypoint;
        this.color = color;
        this.fontColor = fontColor;
        this.isEdit = isEdit;
        this.texture = waypoint.getTexture();
    }

    /**
     * Sets show label.
     *
     * @param showLabel the show label
     */
    public void setShowLabel(boolean showLabel)
    {
        this.showLabel = showLabel;
    }

    @Override
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
    {
        if (!waypoint.isInPlayerDimension())
        {
            return;
        }

        Point2D.Double pixel = getPosition(xOffset, yOffset, gridRenderer, true);
        if (gridRenderer.isOnScreen(pixel))
        {
            if (showLabel && pass == Pass.Text)
            {
                Point2D labelPoint = gridRenderer.shiftWindowPosition(pixel.getX(), pixel.getY(), 0, rotation == 0 ? -texture.getHeight() : texture.getHeight());
                DrawUtil.drawLabel(waypoint.getName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, RGB.BLACK_RGB, .7f, fontColor, 1f, fontScale, false, rotation);
            }
            else if (isEdit && pass == Pass.Object)
            {
                TextureImpl editTex = TextureCache.getTexture(TextureCache.WaypointEdit);
                DrawUtil.drawColoredImage(editTex, color, 1f, pixel.getX() - (editTex.getWidth() / 2), pixel.getY() - editTex.getHeight() / 2, -rotation);
            }

            if (pass == Pass.Object)
            {
                DrawUtil.drawColoredImage(texture, color, 1f, pixel.getX() - (texture.getWidth() / 2), pixel.getY() - (texture.getHeight() / 2), -rotation);
            }
        }
        else if (!isEdit && pass == Pass.Object)
        {
            gridRenderer.ensureOnScreen(pixel);
            //DrawUtil.drawColoredImage(offscreenTexture, color, 1f, pixel.getX() - (offscreenTexture.width / 2), pixel.getY() - (offscreenTexture.height / 2));
            DrawUtil.drawColoredImage(texture, color, 1f, pixel.getX() - (texture.getWidth() / 2), pixel.getY() - (texture.getHeight() / 2), -rotation);
        }
    }

    /**
     * Draw offscreen.
     *
     * @param pass     the pass
     * @param pixel    the pixel
     * @param rotation the rotation
     */
    public void drawOffscreen(Pass pass, Point2D pixel, double rotation)
    {
        if (pass == Pass.Object)
        {
            DrawUtil.drawColoredImage(texture, color, 1f, pixel.getX() - (texture.getWidth() / 2), pixel.getY() - (texture.getHeight() / 2), -rotation);
        }
    }

    /**
     * Gets position.
     *
     * @param xOffset      the x offset
     * @param yOffset      the y offset
     * @param gridRenderer the grid renderer
     * @param forceUpdate  the force update
     * @return the position
     */
    public Point2D.Double getPosition(double xOffset, double yOffset, GridRenderer gridRenderer, boolean forceUpdate)
    {
        if (!forceUpdate && lastPosition != null)
        {
            return lastPosition;
        }

        double x = waypoint.getX();
        double z = waypoint.getZ();
        double halfBlock = Math.pow(2, gridRenderer.getZoom()) / 2;

        Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(x, z);
        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        lastPosition = pixel;
        //lastWindowPosition = gridRenderer.getWindowPosition(lastPosition);
        return pixel;
    }

    /**
     * Is on screen boolean.
     *
     * @return the boolean
     */
    public boolean isOnScreen()
    {
        return lastOnScreen;
    }

    /**
     * Sets on screen.
     *
     * @param lastOnScreen the last on screen
     */
    public void setOnScreen(boolean lastOnScreen)
    {
        this.lastOnScreen = lastOnScreen;
    }

    @Override
    public int getDisplayOrder()
    {
        return 0;
    }

    @Override
    public String getModId()
    {
        return waypoint.getOrigin();
    }

    /**
     * The type Simple cache loader.
     */
    public static class SimpleCacheLoader extends CacheLoader<Waypoint, DrawWayPointStep>
    {
        @Override
        public DrawWayPointStep load(Waypoint waypoint) throws Exception
        {
            return new DrawWayPointStep(waypoint);
        }
    }
}
