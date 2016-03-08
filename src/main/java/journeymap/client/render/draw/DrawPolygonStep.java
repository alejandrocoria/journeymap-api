/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.TextProperties;
import journeymap.client.render.map.GridRenderer;
import net.minecraft.util.BlockPos;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws a polygon.
 */
public class DrawPolygonStep extends BaseOverlayDrawStep<PolygonOverlay>
{
    protected List<Point2D.Double> screenPoints = new ArrayList<Point2D.Double>();

    boolean onScreen;

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
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if(pass == Pass.Object)
        {
            if (overlay.getOuterArea().getPoints().isEmpty())
            {
                onScreen = false;
                return;
            }

            onScreen = isOnScreen(xOffset, yOffset, gridRenderer, rotation);

            if(onScreen)
            {
                DrawUtil.drawPolygon(xOffset, yOffset, screenPoints, overlay.getShapeProperties());
            }
        }
        else if(onScreen)
        {
            super.drawText(pass, xOffset, yOffset, gridRenderer, drawScale, fontScale, rotation);
        }
    }


    @Override
    protected void updatePositions(GridRenderer gridRenderer, double rotation)
    {
        if (overlay.getOuterArea().getPoints().isEmpty())
        {
            onScreen = false;
            return;
        }

        // Convert the polygon block positions to screen positions
        final List<BlockPos> points = overlay.getOuterArea().getPoints();
        this.screenPoints.clear();

        for (BlockPos pos : points)
        {
            Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(pos);
            pixel.setLocation(pixel.getX(), pixel.getY());

            if (this.screenPoints.isEmpty())
            {
                this.screenBounds.setRect(pixel.x, pixel.y, 1, 1);
            }
            else
            {
                this.screenBounds.add(pixel);
            }
            screenPoints.add(pixel);
        }

        // Center label
        TextProperties textProperties = overlay.getTextProperties();
        labelPosition.setLocation(screenBounds.getCenterX() + textProperties.getOffsetX(),
                screenBounds.getCenterY() + textProperties.getOffsetY());


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
