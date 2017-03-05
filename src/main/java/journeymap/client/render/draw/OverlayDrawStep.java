/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import journeymap.client.api.display.Overlay;
import journeymap.client.render.map.GridRenderer;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Describes additional DrawStep functionality specific to an Overlay.
 */
public interface OverlayDrawStep extends DrawStep
{
    /**
     * Gets the overlay related to this DrawStep
     *
     * @return overlay
     */
    Overlay getOverlay();

    /**
     * Gets the screen bounds of the DrawStep
     *
     * @return bounds
     */
    Rectangle2D.Double getBounds();

    /**
     * Evaluates whether the DrawStep is on screen
     *
     * @param xOffset      the x offset
     * @param yOffset      the y offset
     * @param gridRenderer the grid renderer
     * @param rotation     the rotation
     * @return boolean
     */
    boolean isOnScreen(double xOffset, double yOffset, GridRenderer gridRenderer, double rotation);

    /**
     * Sets the position of the overlay title (tooltip)
     *
     * @param titlePosition the title position
     */
    void setTitlePosition(@Nullable Point2D.Double titlePosition);

    /**
     * Enable or disable the DrawStep
     *
     * @param enabled the enabled
     */
    public void setEnabled(boolean enabled);
}
