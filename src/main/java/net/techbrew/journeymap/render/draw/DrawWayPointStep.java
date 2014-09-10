/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;

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

        Point2D.Double pixel = getPosition(xOffset, yOffset, gridRenderer);
        double halfTexHeight = texture.height / 2;
        if (gridRenderer.isOnScreen(pixel))
        {
            DrawUtil.drawLabel(waypoint.getName(), pixel.getX(), pixel.getY() - halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 255, fontColor, 255, fontScale, false, rotation);
            if (isEdit)
            {
                TextureImpl editTex = TextureCache.instance().getWaypointEdit();
                DrawUtil.drawColoredImage(editTex, 255, color, pixel.getX() - (editTex.width / 2), pixel.getY() - editTex.height / 2, -rotation);
            }
            DrawUtil.drawColoredImage(texture, 255, color, pixel.getX() - (texture.width / 2), pixel.getY() - halfTexHeight, -rotation);
        }
        else if (!isEdit)
        {
            gridRenderer.ensureOnScreen(pixel);
            //DrawUtil.drawColoredImage(offscreenTexture, 255, color, pixel.getX() - (offscreenTexture.width / 2), pixel.getY() - (offscreenTexture.height / 2));
            DrawUtil.drawColoredImage(texture, 255, color, pixel.getX() - (texture.width / 2), pixel.getY() - halfTexHeight, -rotation);
        }
    }

    protected Point2D.Double getPosition(double xOffset, double yOffset, GridRenderer gridRenderer)
    {
        double x = waypoint.getX();
        double z = waypoint.getZ();
        double halfBlock = Math.pow(2, gridRenderer.getZoom()) / 2;

        Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(x, z);
        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        return pixel;
    }

    public boolean isOnScreen(double xOffset, double yOffset, GridRenderer gridRenderer)
    {
        return gridRenderer.isOnScreen(getPosition(xOffset, yOffset, gridRenderer));
    }
}
