/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.Context;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.TextProperties;
import journeymap.client.render.map.GridRenderer;
import net.minecraft.util.BlockPos;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Draws a polygon.
 * <p/>
 * TODO:  Ensure this is only created when the dimension is correct and when gridRenderer.getDisplay()
 * matches the polygon's settings.
 */
public class DrawPolygonStep implements DrawStep
{
    public final PolygonOverlay polygon;

    private List<Point2D.Double> screenPoints;
    private Rectangle2D.Double screenBounds;
    private double[] lastGridSettings = new double[0];
    private double[] lastLabelSettings = new double[0];
    private boolean showLabel = true;

    /**
     * Draw a polygon on the map.
     *
     * @param polygon
     */
    public DrawPolygonStep(PolygonOverlay polygon)
    {
        this.polygon = polygon;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        final int zoom = gridRenderer.getZoom();
        if (polygon.getMinZoom() <= zoom && polygon.getMaxZoom() >= zoom)
        {
            double[] currentValues = new double[]{gridRenderer.getCenterBlockX(), gridRenderer.getCenterBlockZ(), xOffset, yOffset, zoom};
            if (!Arrays.equals(currentValues, lastGridSettings) || lastLabelSettings.length == 0)
            {
                updateRenderValues(xOffset, yOffset, gridRenderer);
                lastGridSettings = currentValues;
            }

            if (gridRenderer.isOnScreen(screenBounds))
            {
                DrawUtil.drawPolygon(screenPoints, polygon.getShapeProperties(), rotation);

                if (showLabel)
                {
                    DrawUtil.drawLabel(polygon.getLabel(),
                            lastLabelSettings[0], lastLabelSettings[1],
                            DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle,
                            (int) lastLabelSettings[2],
                            (int) lastLabelSettings[3],
                            (int) lastLabelSettings[4],
                            (int) lastLabelSettings[5],
                            lastLabelSettings[6] * fontScale, true, rotation);
                }
            }
        }
    }

    /**
     * Turns world coords into screen pixels.
     *
     * @param xOffset
     * @param yOffset
     * @param gridRenderer
     */
    protected void updateRenderValues(double xOffset, double yOffset, GridRenderer gridRenderer)
    {
        final double blockSize = Math.pow(2, gridRenderer.getZoom());
        final List<BlockPos> points = polygon.getOuterArea().getPoints();

        this.screenPoints = new ArrayList<Point2D.Double>(points.size());
        this.screenBounds = null;

        for (BlockPos pos : points)
        {
            Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(pos);
            pixel.setLocation(pixel.getX() + blockSize + xOffset, pixel.getY() + blockSize + yOffset);
            screenPoints.add(pixel);
            if (this.screenBounds == null)
            {
                this.screenBounds = new Rectangle2D.Double(pixel.x, pixel.y, 0, 0);
            }
            else
            {
                this.screenBounds.add(pixel);
            }
        }

//        Delaunay dt = Delaunay.create(Arrays.asList(
//                new Point2D.Double(screenBounds.getMinX(), screenBounds.getMaxY()),
//                new Point2D.Double(screenBounds.getMaxX(), screenBounds.getMaxY()),
//                new Point2D.Double(screenBounds.getMaxX(), screenBounds.getMinY()),
//                new Point2D.Double(screenBounds.getMinX(), screenBounds.getMinY())
//        ));
//        for(Point2D.Double point : screenPoints)
//        {
//            dt.insertSite(new Vertex(point.x, point.y));
//        }
//        dt.compute();
//        screenPoints.clear();
//
//        for(Edge edge : dt.delEdges)
//        {
//            screenPoints.add(new Point2D.Double(edge.data.xPos, edge.data.yPos));
//        }

        TextProperties textProperties = polygon.getTextProperties();
        this.lastLabelSettings = new double[]{
                screenBounds.getCenterX(),
                screenBounds.getCenterY(),
                textProperties.getBackgroundColor(),
                (int) Math.max(0, textProperties.getBackgroundOpacity() * 255),
                textProperties.getColor(),
                (int) Math.max(0, textProperties.getOpacity() * 255),
                textProperties.getScale()
        };

        EnumSet<Context.UI> activeUIs = textProperties.getActiveUIs();

        switch (gridRenderer.getDisplay())
        {
            case Fullscreen:
                showLabel = activeUIs.contains(Context.UI.Any) || activeUIs.contains(Context.UI.Fullscreen);
                break;
            case Minimap:
                showLabel = activeUIs.contains(Context.UI.Any) || activeUIs.contains(Context.UI.Minimap);
                break;
        }
    }

    @Override
    public int getDisplayOrder()
    {
        return polygon.getDisplayOrder();
    }

    @Override
    public String getModId()
    {
        return polygon.getModId();
    }

    @Override
    public String getGroupName()
    {
        return polygon.getOverlayGroupName();
    }

}
