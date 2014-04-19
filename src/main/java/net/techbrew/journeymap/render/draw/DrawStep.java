package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;

/**
 * Interface for something that needs to be drawn at a pixel coordinate.
 *
 * @author mwoodman
 */
public interface DrawStep {

    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale);

}
