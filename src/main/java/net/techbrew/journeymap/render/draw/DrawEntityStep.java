package net.techbrew.journeymap.render.draw;

import com.google.common.cache.CacheLoader;
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
    Entity entity;
    TextureImpl texture;
    TextureImpl locatorTexture;
    int bottomMargin;
    boolean flip;

    private DrawEntityStep(Entity entity)
    {
        super();
        this.entity = entity;
    }

    public void update(boolean flip, TextureImpl locatorTexture, TextureImpl texture, int bottomMargin)
    {
        this.locatorTexture = locatorTexture;
        this.texture = texture;
        this.flip = flip;
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale)
    {
        if(this.texture==null || entity.isDead || !entity.addedToChunk || entity.isSneaking())
        {
            return;
        }

        Point2D pixel = gridRenderer.getPixel(entity.posX, entity.posZ);
        if (pixel != null)
        {
            double heading = EntityHelper.getHeading(entity);
            double drawX = pixel.getX() + xOffset;
            double drawY = pixel.getY() + yOffset;

            if(locatorTexture!=null)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, bottomMargin*8, drawScale);
            }
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, bottomMargin, drawScale);
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<Entity, DrawEntityStep>
    {
        @Override
        public DrawEntityStep load(Entity entity) throws Exception
        {
            return new DrawEntityStep(entity);
        }
    }
}
