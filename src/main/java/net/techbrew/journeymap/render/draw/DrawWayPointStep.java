/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;

import com.google.common.cache.CacheLoader;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawWayPointStep implements DrawStep
{

    public final Waypoint waypoint;
    Point2D.Double lastPosition;
    Point2D.Double lastWindowPosition;
    boolean lastOnScreen;
    final Color color;
    final Color fontColor;
    final TextureImpl texture;
    final boolean isEdit;

    /**
     * Draw a waypoint on the map.
     *
     * @param waypoint
     */
    public DrawWayPointStep(Waypoint waypoint)
    {
        this(waypoint, waypoint.getColor(), waypoint.isDeathPoint() ? Color.red : waypoint.getSafeColor(), false);
    }

    /**
     * Draw a waypoint on the map.
     *
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
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if (!waypoint.isInPlayerDimension())
        {
            return;
        }

        Point2D.Double pixel = getPosition(xOffset, yOffset, gridRenderer, true);
        if (gridRenderer.isOnScreen(pixel))
        {
            Point2D labelPoint = gridRenderer.shiftWindowPosition(pixel.getX(), pixel.getY(), 0, rotation==0 ? -texture.height : texture.height);

            DrawUtil.drawLabel(waypoint.getName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, Color.black, 255, fontColor, 255, fontScale, false, rotation);
            if (isEdit)
            {
                TextureImpl editTex = TextureCache.instance().getWaypointEdit();
                DrawUtil.drawColoredImage(editTex, 255, color, pixel.getX() - (editTex.width / 2), pixel.getY() - editTex.height / 2, -rotation);
            }
            DrawUtil.drawColoredImage(texture, 255, color, pixel.getX() - (texture.width / 2), pixel.getY() - (texture.height/2), -rotation);
        }
        else if (!isEdit)
        {
            gridRenderer.ensureOnScreen(pixel);
            //DrawUtil.drawColoredImage(offscreenTexture, 255, color, pixel.getX() - (offscreenTexture.width / 2), pixel.getY() - (offscreenTexture.height / 2));
            DrawUtil.drawColoredImage(texture, 255, color, pixel.getX() - (texture.width / 2), pixel.getY() - (texture.height/2), -rotation);
        }
    }

    public void drawOffscreen(Point2D pixel, double rotation)
    {
        DrawUtil.drawColoredImage(texture, 255, color, pixel.getX() - (texture.width / 2), pixel.getY() - (texture.height/2), -rotation);
    }

    public Point2D.Double getPosition(double xOffset, double yOffset, GridRenderer gridRenderer, boolean forceUpdate)
    {
        if(!forceUpdate && lastPosition!=null)
        {
            return lastPosition;
        }

        double x = waypoint.getX();
        double z = waypoint.getZ();
        double halfBlock = Math.pow(2, gridRenderer.getZoom()) / 2;

        Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(x, z);
        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        lastPosition = pixel;
        lastWindowPosition = gridRenderer.getWindowPosition(lastPosition);
        return pixel;
    }

    public Point2D.Double getLastWindowPosition()
    {
        return lastWindowPosition;
    }

    public int getTextureHeight()
    {
        return texture.height;
    }

    public int getTextureSize()
    {
        return Math.max(texture.height, texture.width);
    }

    public boolean isOnScreen()
    {
        return lastOnScreen;
    }

    public void setOnScreen(boolean lastOnScreen)
    {
        this.lastOnScreen = lastOnScreen;
    }

    public static class SimpleCacheLoader extends CacheLoader<Waypoint, DrawWayPointStep>
    {
        @Override
        public DrawWayPointStep load(Waypoint waypoint) throws Exception
        {
            return new DrawWayPointStep(waypoint);
        }
    }
}
