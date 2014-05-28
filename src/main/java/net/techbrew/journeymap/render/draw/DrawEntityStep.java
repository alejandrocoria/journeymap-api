package net.techbrew.journeymap.render.draw;

import net.minecraft.entity.Entity;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawEntityStep implements DrawStep
{
    final TextureImpl texture;
    final int bottomMargin;
    final Entity entity;
    final boolean flip;

    public DrawEntityStep(Entity entity, boolean flip, TextureImpl texture, int bottomMargin)
    {
        super();
        this.entity = entity;
        this.texture = texture;
        this.flip = flip;
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale)
    {
        Point2D pixel = gridRenderer.getPixel(entity.posX, entity.posZ);
        if (pixel != null)
        {
            DrawUtil.drawEntity(pixel.getX() + xOffset, pixel.getY() + yOffset, EntityHelper.getHeading(entity), flip, texture, bottomMargin, drawScale);
        }
    }
}
