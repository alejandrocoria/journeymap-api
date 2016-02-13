/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.MarkerOverlay;
import journeymap.client.api.model.MapImage;
import journeymap.client.cartography.RGB;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws a marker image.
 */
public class DrawMarkerStep extends BaseOverlayDrawStep<MarkerOverlay>
{
    Point2D.Double markerPosition;
    TextureImpl iconTexture;

    /**
     * Draw a marker on the map.
     *
     * @param marker
     */
    public DrawMarkerStep(MarkerOverlay marker)
    {
        super(marker);
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if (!isOnScreen(xOffset, yOffset, gridRenderer))
        {
            return;
        }

        MapImage icon = overlay.getIcon();
        if (iconTexture == null)
        {
            try
            {
                // TODO TextureCache this
                iconTexture = new TextureImpl(icon.getImage(), true);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error("Error getting MarkerOverlay image texture: " + e, e);
                iconTexture = TextureCache.instance().getWaypointOffscreen();
            }
        }

        int alpha = RGB.toClampedInt(icon.getOpacity());

        DrawUtil.drawColoredImage(iconTexture, alpha, icon.getColor(),
                markerPosition.x + xOffset - icon.getAnchorX(),
                markerPosition.y + yOffset - icon.getAnchorY(),
                drawScale, rotation);

        super.drawText(xOffset, yOffset, gridRenderer, drawScale, fontScale, rotation);
    }


    @Override
    protected void updatePositions(GridRenderer gridRenderer)
    {
        MapImage icon = overlay.getIcon();

        markerPosition = gridRenderer.getBlockPixelInGrid(overlay.getPoint());

        double iconWidth = icon.getWidth();
        double iconHeight = icon.getHeight();

        // Mouse Y is 0 at bottom of screen, so need to offset rectangle by height
        this.screenBounds = new Rectangle2D.Double(
                markerPosition.x - (iconWidth / 2.0),
                gridRenderer.getHeight() - markerPosition.y - (iconHeight / 2.0),
                iconWidth, iconHeight);
    }


}
