/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation);

    public int getDisplayOrder();

    public String getModId();
}
