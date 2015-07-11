/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import com.google.common.cache.CacheLoader;
import journeymap.client.JourneymapClient;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.EntityDTO;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawEntityStep implements DrawStep
{
    static final Color labelBg = Color.black;
    static final int labelBgAlpha = 180;
    static final Color labelFg = Color.white;
    static final int labelFgAlpha = 225;
    boolean hideSneaks = JourneymapClient.getCoreProperties().hideSneakingEntities.get();
    boolean showHeading = true;
    Minecraft minecraft = Minecraft.getMinecraft();
    EntityDTO entityDTO;
    TextureImpl texture;
    TextureImpl locatorTexture;
    EntityLivingBase entityLiving;
    boolean flip;

    private DrawEntityStep(EntityDTO entityDTO)
    {
        super();
        this.entityDTO = entityDTO;
        this.entityLiving = entityDTO.entityLiving;
    }

    public void update(boolean flip, TextureImpl locatorTexture, TextureImpl texture, boolean showHeading)
    {
        this.locatorTexture = locatorTexture;
        this.texture = texture;
        this.flip = flip;
        this.showHeading = showHeading;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {

        if (entityLiving == null || entityLiving.isDead || entityLiving.isInvisibleToPlayer(minecraft.thePlayer)
                || !entityLiving.addedToChunk || (hideSneaks && entityLiving.isSneaking()))
        {
            return;
        }

        Point2D pixel = gridRenderer.getPixel(entityDTO.entityLiving.posX, entityDTO.entityLiving.posZ);
        if (pixel != null)
        {
            double heading = entityDTO.entityLiving.rotationYawHead;
            double drawX = pixel.getX() + xOffset;
            double drawY = pixel.getY() + yOffset;

            float alpha = 1f;
            if (entityLiving.posY > minecraft.thePlayer.posY)
            {
                alpha = 1f - Math.max(.1f, (float) ((entityLiving.posY - minecraft.thePlayer.posY) / 32f));
            }

            if (entityDTO.entityLiving instanceof EntityPlayer)
            {
                int blockSize = (int) Math.pow(2, gridRenderer.getZoom());
                float labelOffset = texture != null ? texture.getHeight() / blockSize : 0;
                drawPlayer(drawX, drawY, gridRenderer, alpha, heading, drawScale, fontScale, rotation);

            }
            else
            {
                drawCreature(drawX, drawY, gridRenderer, alpha, heading, drawScale, fontScale, rotation);
            }
        }
    }

    private void drawPlayer(double drawX, double drawY, GridRenderer gridRenderer, float alpha, double heading, float drawScale, double fontScale, double rotation)
    {
        if (locatorTexture != null && showHeading)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, alpha, drawScale, rotation);
        }

        if (texture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, alpha, drawScale * .75f, rotation);
        }
        int labelOffset = texture == null ? 0 : rotation == 0 ? -texture.getHeight() / 2 : texture.getHeight() / 2;
        Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, -labelOffset);

        Team team = entityLiving.getTeam();
        if (team == null || !(entityLiving instanceof EntityPlayer))
        {
            DrawUtil.drawLabel(ForgeHelper.INSTANCE.getEntityName(entityLiving), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, Color.black, 205, Color.green, 255, fontScale, false, rotation);
        }
        else
        {
            String playerName = ScorePlayerTeam.formatPlayerName(entityLiving.getTeam(), ForgeHelper.INSTANCE.getEntityName(entityLiving));
            DrawUtil.drawLabel(playerName, labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, Color.black, 205, Color.white, 255, fontScale, false, rotation);
        }
    }

    private void drawCreature(double drawX, double drawY, GridRenderer gridRenderer, float alpha, double heading, float drawScale, double fontScale, double rotation)
    {
        Math.min(1f, Math.max(0f, (float) (16 - (entityLiving.posY - minecraft.thePlayer.posY))));

        if (locatorTexture != null && showHeading)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, alpha, drawScale, rotation);
        }

        int labelOffset = texture == null ? 8 : rotation == 0 ? texture.getHeight() : -texture.getHeight();
        //entityDTO.customName = EnumChatFormatting.LIGHT_PURPLE  + entityDTO.entityLiving.getCommandSenderName(); // TODO REMOVE
        if (entityDTO.customName != null)
        {
            Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, labelOffset);
            DrawUtil.drawCenteredLabel(entityDTO.customName, labelPoint.getX(), labelPoint.getY(), labelBg, labelBgAlpha, Color.white, labelFgAlpha, fontScale, rotation);
        }

        if (texture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, alpha, drawScale, rotation);
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<EntityDTO, DrawEntityStep>
    {
        @Override
        public DrawEntityStep load(EntityDTO entityDTO) throws Exception
        {
            return new DrawEntityStep(entityDTO);
        }
    }
}
