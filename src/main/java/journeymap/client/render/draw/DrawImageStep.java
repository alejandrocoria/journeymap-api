/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.TextProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import net.minecraft.util.ResourceLocation;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Draws an image overlay
 */
public class DrawImageStep extends BaseOverlayDrawStep<ImageOverlay>
{
    private Point2D.Double northWestPosition;
    private Point2D.Double southEastPosition;
    private volatile Future<TextureImpl> iconFuture;
    private TextureImpl iconTexture;
    private boolean hasError;

    /**
     * Draw a marker on the map.
     *
     * @param marker
     */
    public DrawImageStep(ImageOverlay marker)
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
            MapImage icon = overlay.getImage();

            double width = screenBounds.width;
            double height = screenBounds.height;

            DrawUtil.drawColoredSprite(iconTexture,
                    width,
                    height,
                    0,
                    0,
                    iconTexture.getWidth(),
                    iconTexture.getHeight(),
                    icon.getColor(),
                    icon.getOpacity(),
                    northWestPosition.x + xOffset,
                    northWestPosition.y + yOffset,
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
                        MapImage image = overlay.getImage();
                        ResourceLocation resourceLocation = image.getImageLocation();
                        if (resourceLocation == null)
                        {
                            resourceLocation = new ResourceLocation("fake:" + overlay.getGuid());
                            TextureImpl texture = TextureCache.instance().getResourceTexture(resourceLocation);
                            texture.setImage(image.getImage(), true);
                            return texture;
                        }
                        else
                        {
                            return TextureCache.instance().getResourceTexture(resourceLocation);
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
            Journeymap.getLogger().error("Error getting ImageOverlay marimage texture: " + e, e);
            hasError = true;
        }

    }

    @Override
    protected void updatePositions(GridRenderer gridRenderer)
    {
        northWestPosition = gridRenderer.getBlockPixelInGrid(overlay.getNorthWestPoint());
        southEastPosition = gridRenderer.getBlockPixelInGrid(overlay.getSouthEastPoint());

        this.screenBounds = new Rectangle2D.Double(northWestPosition.x, northWestPosition.y, 0, 0);
        screenBounds.add(southEastPosition);

        // Center label
        TextProperties textProperties = overlay.getTextProperties();
        labelPosition.setLocation(screenBounds.getCenterX() + textProperties.getOffsetX(),
                screenBounds.getCenterY() + textProperties.getOffsetY());
    }

}