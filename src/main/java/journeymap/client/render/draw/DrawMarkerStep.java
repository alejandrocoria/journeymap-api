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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Draws a marker image.
 */
public class DrawMarkerStep extends BaseOverlayDrawStep<MarkerOverlay>
{
    private Point2D.Double markerPosition;
    private volatile Future<TextureImpl> iconFuture;
    private TextureImpl iconTexture;
    private boolean hasError;

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

        ensureTexture();

        if (!hasError && iconTexture != null)
        {
            MapImage icon = overlay.getIcon();
            DrawUtil.drawColoredSprite(iconTexture,
                    icon.getDisplayWidth(),
                    icon.getDisplayHeight(),
                    icon.getTextureX(),
                    icon.getTextureY(),
                    icon.getTextureWidth(),
                    icon.getTextureHeight(),
                    RGB.toClampedInt(icon.getOpacity()),
                    icon.getColor(),
                    markerPosition.x + xOffset - icon.getAnchorX(),
                    markerPosition.y + yOffset - icon.getAnchorY(),
                    drawScale, icon.getRotation());
        }

        super.drawText(xOffset, yOffset, gridRenderer, drawScale, fontScale, rotation);
    }

    /**
     * Fetch and bind the marker icon texture as needed.
     */
    protected void ensureTexture()
    {
        if (iconTexture != null)
        {
            return;
        }

        try
        {
            if (iconFuture == null || iconFuture.isCancelled())
            {
                iconFuture = TextureCache.instance().scheduleTextureTask(new Callable<TextureImpl>()
                {
                    @Override
                    public TextureImpl call() throws Exception
                    {
                        MapImage icon = overlay.getIcon();
                        return TextureCache.instance().getResourceTexture(icon.getImageLocation());
                    }
                });
            }
            else
            {
                if (iconFuture.isDone())
                {
                    iconTexture = iconFuture.get();
                    if (iconTexture.isBindNeeded())
                    {
                        iconTexture.bindTexture();
                    }
                    iconFuture = null;
                }
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error getting MarkerOverlay image texture: " + e, e);
            hasError = true;
        }

    }

    @Override
    protected void updatePositions(GridRenderer gridRenderer)
    {
        MapImage icon = overlay.getIcon();

        markerPosition = gridRenderer.getBlockPixelInGrid(overlay.getPoint());

        double iconWidth = icon.getDisplayWidth();
        double iconHeight = icon.getTextureHeight();

        // Mouse Y is 0 at bottom of screen, so need to offset rectangle by height
        this.screenBounds = new Rectangle2D.Double(
                markerPosition.x - (iconWidth / 2.0),
                gridRenderer.getHeight() - markerPosition.y - (iconHeight / 2.0),
                iconWidth, iconHeight);
    }

}
