/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;

/**
 * Interface for something that needs to be drawn at a pixel coordinate.
 *
 * @author mwoodman
 */
public interface DrawStep
{

    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation);

}
