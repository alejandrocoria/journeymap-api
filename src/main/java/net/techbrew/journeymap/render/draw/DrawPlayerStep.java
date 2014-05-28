package net.techbrew.journeymap.render.draw;

import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawPlayerStep implements DrawStep
{
    final TextureImpl texture;
    final EntityPlayer entity;

    public DrawPlayerStep(EntityPlayer entity, TextureImpl texture)
    {
        super();
        this.entity = entity;
        this.texture = texture;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale)
    {
        if(entity.isDead || !entity.addedToChunk || entity.isSneaking())
        {
            return;
        }

        float labelOffset = texture!=null ? texture.height : 0;
        Point2D pixel = gridRenderer.getPixel(entity.posX, entity.posZ);
        if (pixel != null)
        {
            if(texture!=null)
            {
                DrawUtil.drawEntity(pixel.getX() + xOffset, pixel.getY() + yOffset, EntityHelper.getHeading(entity), true, texture, 0, drawScale *.75f);
            }
            DrawUtil.drawCenteredLabel(entity.getCommandSenderName(), pixel.getX() + xOffset, pixel.getY() + yOffset - labelOffset, Color.black, 205, Color.green, 255, fontScale);
        }
    }
}
