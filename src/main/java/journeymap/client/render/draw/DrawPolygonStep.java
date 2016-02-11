/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import com.google.common.base.Strings;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.Overlay;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.UIState;
import journeymap.client.cartography.RGB;
import journeymap.client.render.map.GridRenderer;
import net.minecraft.util.BlockPos;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Draws a polygon.
 */
public class DrawPolygonStep implements OverlayDrawStep
{
    public final PolygonOverlay polygon;

    private List<Point2D.Double> screenPoints;
    private Rectangle2D.Double screenBounds = new Rectangle2D.Double();
    private Point2D.Double titlePosition = null;
    private double[] lastArgs = new double[0];

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
        if (!isOnScreen(xOffset, yOffset, gridRenderer))
        {
            return;
        }

        DrawUtil.drawPolygon(screenPoints, polygon.getShapeProperties(), rotation);

        TextProperties textProperties = polygon.getTextProperties();
        EnumSet<Context.UI> activeUIs = textProperties.getActiveUIs();
        int zoom = gridRenderer.getZoom();
        boolean showLabel = (textProperties.getActiveUIs().contains(Context.UI.Any) || activeUIs.contains(Context.UI.Fullscreen))
                && (zoom >= textProperties.getMinZoom() && zoom <= textProperties.getMaxZoom());
        if (showLabel)
        {
            DrawUtil.drawLabel(polygon.getLabel(),
                    screenBounds.getCenterX(),
                    screenBounds.getCenterY(),
                    DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle,
                    textProperties.getBackgroundColor(),
                    RGB.toClampedInt(textProperties.getBackgroundOpacity()),
                    textProperties.getColor(),
                    RGB.toClampedInt(textProperties.getOpacity()),
                    textProperties.getScale() * fontScale,
                    textProperties.hasFontShadow(),
                    rotation);

            if (titlePosition != null)
            {
                String title = polygon.getTitle();
                if (!Strings.isNullOrEmpty(title))
                {
                    DrawUtil.drawLabel(title,
                            titlePosition.x + 5 + xOffset,
                            titlePosition.y + yOffset,
                            DrawUtil.HAlign.Right, DrawUtil.VAlign.Above,
                            textProperties.getBackgroundColor(),
                            RGB.toClampedInt(textProperties.getBackgroundOpacity()),
                            textProperties.getColor(),
                            RGB.toClampedInt(textProperties.getOpacity()),
                            textProperties.getScale() * fontScale,
                            textProperties.hasFontShadow(),
                            rotation);
                }
            }
        }
    }

    /**
     * Determines whether this is onscreen, updating cached values as needed.
     *
     * @param xOffset
     * @param yOffset
     * @param gridRenderer
     * @return false if not rendered
     */
    public boolean isOnScreen(double xOffset, double yOffset, GridRenderer gridRenderer)
    {
        final int zoom = gridRenderer.getZoom();
        final UIState uiState = gridRenderer.getUIState();
        if (!polygon.isActiveIn(uiState.ui, uiState.mapType) || polygon.getMinZoom() > zoom || polygon.getMaxZoom() < zoom)
        {
            // Not displayed
            return false;
        }

        // Use an array of doubles to compare the current grid state / args to previous ones
        double[] currentArgs = new double[]{
                gridRenderer.getCenterBlockX() + xOffset,
                gridRenderer.getCenterBlockZ() + yOffset,
                zoom,
                gridRenderer.getHeight(),
                gridRenderer.getWidth()};

        if (!Arrays.equals(currentArgs, lastArgs))
        {
            lastArgs = currentArgs;

            // Convert the polygon block positions to screen positions
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
        }

        // Verify screenbounds within grid
        return gridRenderer.isOnScreen(screenBounds);

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

    @Override
    public void setTitlePosition(@Nullable Point2D.Double titlePosition)
    {
        this.titlePosition = titlePosition;
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
    public Rectangle2D.Double getBounds()
    {
        return screenBounds;
    }

    @Override
    public Overlay getOverlay()
    {
        return polygon;
    }


}
