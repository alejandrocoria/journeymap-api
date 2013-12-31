package net.techbrew.mcjm.render.draw;

import net.techbrew.mcjm.render.overlay.GridRenderer;

/**
 * Interface for something that needs to be drawn at a pixel coordinate.
 *
 * @author mwoodman
 */
public interface DrawStep {

    public void draw(int xOffset, int yOffset, GridRenderer gridRenderer);

}
