/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.MarkerOverlay;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.TextProperties;
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
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
    {
        if (!isOnScreen(xOffset, yOffset, gridRenderer, rotation))
        {
            return;
        }

        if (pass == Pass.Object)
        {
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
                        icon.getColor(),
                        icon.getOpacity(),
                        markerPosition.x + xOffset - icon.getAnchorX(),
                        markerPosition.y + yOffset - icon.getAnchorY(),
                        1f, icon.getRotation() - rotation);
            }
        }
        else
        {
            super.drawText(pass, xOffset, yOffset, gridRenderer, fontScale, rotation);
        }
    }

    /**
     * Fetch and bind the marker icon upperTexture as needed.
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
                iconFuture = TextureCache.scheduleTextureTask(new Callable<TextureImpl>()
                {
                    @Override
                    public TextureImpl call() throws Exception
                    {
                        MapImage icon = overlay.getIcon();
                        if (icon.getImageLocation() != null)
                        {
                            return TextureCache.getTexture(icon.getImageLocation());
                        }
                        else if (icon.getImage() != null)
                        {
                            return new TextureImpl(icon.getImage());
                        }
                        else
                        {
                            return null;
                        }
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
            Journeymap.getLogger().error("Error getting MarkerOverlay image upperTexture: " + e, e);
            hasError = true;
        }

    }

    @Override
    protected void updatePositions(GridRenderer gridRenderer, double rotation)
    {
        MapImage icon = overlay.getIcon();

        // Get marker position
        markerPosition = gridRenderer.getBlockPixelInGrid(overlay.getPoint());

        // Center marker within block
        int halfBlock = (int) lastUiState.blockSize / 2;
        markerPosition.setLocation(markerPosition.x + halfBlock, markerPosition.y + halfBlock);

        // Center label
        TextProperties textProperties = overlay.getTextProperties();
        int xShift = (rotation % 360 == 0) ? -textProperties.getOffsetX() : textProperties.getOffsetX();
        int yShift = (rotation % 360 == 0) ? -textProperties.getOffsetY() : textProperties.getOffsetY();
        if (xShift != 0 && yShift != 0)
        {
            Point2D shiftedPoint = gridRenderer.shiftWindowPosition(markerPosition.x, markerPosition.y, xShift, yShift);
            labelPosition.setLocation(shiftedPoint.getX(), shiftedPoint.getY());
        }
        else
        {
            labelPosition.setLocation(markerPosition.x, markerPosition.y);
        }

        // Start screenbounds to cover the block
        this.screenBounds.setRect(markerPosition.x, markerPosition.y, lastUiState.blockSize, lastUiState.blockSize);

        // Expand screenbounds to include label position
        // TODO: Doesn't really include the text of the label, though
        screenBounds.add(labelPosition);

        // Expand screenbounds to include the icon
        Rectangle2D.Double iconBounds = new Rectangle2D.Double(markerPosition.x - icon.getAnchorX(),
                markerPosition.y - icon.getAnchorY(),
                icon.getDisplayWidth(),
                icon.getDisplayHeight());
        screenBounds.add(iconBounds);

    }

}
