/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.render.map.GridRenderer;
import net.minecraft.util.BlockPos;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws a polygon.
 */
public class DrawPolygonStep extends BaseOverlayDrawStep<PolygonOverlay>
{
    protected List<Point2D.Double> screenPoints;

    /**
     * Draw a polygon on the map.
     *
     * @param polygon
     */
    public DrawPolygonStep(PolygonOverlay polygon)
    {
        super(polygon);
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if (!isOnScreen(xOffset, yOffset, gridRenderer))
        {
            return;
        }

        DrawUtil.drawPolygon(xOffset, yOffset, screenPoints, overlay.getShapeProperties());

        super.drawText(xOffset, yOffset, gridRenderer, drawScale, fontScale, rotation);
    }


    @Override
    protected void updatePositions(GridRenderer gridRenderer)
    {
        // Convert the polygon block positions to screen positions
        final double blockSize = Math.pow(2, gridRenderer.getZoom());
        final List<BlockPos> points = overlay.getOuterArea().getPoints();
        this.screenPoints = new ArrayList<Point2D.Double>(points.size());
        this.screenBounds = null;

        for (BlockPos pos : points)
        {
            Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(pos);
            pixel.setLocation(pixel.getX(), pixel.getY());
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

        // Mouse Y is 0 at bottom of screen, so need to offset rectangle by height
        if (screenBounds != null)
        {
            screenBounds.setRect(screenBounds.x, gridRenderer.getHeight() - screenBounds.y - screenBounds.height, screenBounds.width, screenBounds.height);
        }


        // TODO: Triangulate the polygon
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
    }


}
