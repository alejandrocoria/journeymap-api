/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import journeymap.client.render.map.GridRenderer;

/**
 * Interface for something that needs to be drawn at a pixel coordinate.
 *
 * @author techbrew
 */
public interface DrawStep
{
    /**
     * Draw.
     *
     * @param pass         the pass
     * @param xOffset      the x offset
     * @param yOffset      the y offset
     * @param gridRenderer the grid renderer
     * @param fontScale    the font scale
     * @param rotation     the rotation
     */
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation);

    /**
     * Gets display order.
     *
     * @return the display order
     */
    public int getDisplayOrder();

    /**
     * Gets mod id.
     *
     * @return the mod id
     */
    public String getModId();

    /**
     * The enum Pass.
     */
    enum Pass
    {
        /**
         * Object pass.
         */
        Object,
        /**
         * Text pass.
         */
        Text,
        /**
         * Tooltip pass.
         */
        Tooltip
    }
}
