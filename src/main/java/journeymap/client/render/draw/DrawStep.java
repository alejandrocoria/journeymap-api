/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import journeymap.client.render.map.GridRenderer;

/**
 * Interface for something that needs to be drawn at a pixel coordinate.
 *
 * @author mwoodman
 */
public interface DrawStep
{
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation);

    public int getDisplayOrder();

    public String getModId();

    enum Pass
    {
        Object,
        Text,
        Tooltip
    }
}
