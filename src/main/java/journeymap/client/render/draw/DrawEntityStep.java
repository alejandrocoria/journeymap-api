/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import com.google.common.cache.CacheLoader;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;

/**
 * Draws an entity.
 */
public class DrawEntityStep implements DrawStep
{
    static final Integer labelBg = RGB.BLACK_RGB;
    static final int labelBgAlpha = 180;
    static final Integer labelFg = RGB.WHITE_RGB;
    static final int labelFgAlpha = 225;

    boolean hideSneaks = JourneymapClient.getCoreProperties().hideSneakingEntities.get();
    boolean showHeading = true;
    Minecraft minecraft = Minecraft.getMinecraft();
    TextureImpl texture;
    TextureImpl locatorTexture;
    WeakReference<EntityLivingBase> entityLivingRef;
    String customName;
    boolean flip;
    Point2D screenPosition;

    private DrawEntityStep(EntityLivingBase entityLiving)
    {
        super();
        this.entityLivingRef = new WeakReference<EntityLivingBase>(entityLiving);
    }

    public void update(boolean flip, TextureImpl locatorTexture, TextureImpl texture, boolean showHeading)
    {
        this.locatorTexture = locatorTexture;
        this.texture = texture;
        this.flip = flip;
        this.showHeading = showHeading;
        EntityLivingBase entityLiving = entityLivingRef.get();
        if (entityLiving != null)
        {
            customName = DataCache.instance().getEntityDTO(entityLiving).customName;
        }
    }

    @Override
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if (pass == Pass.Tooltip)
        {
            return;
        }

        EntityLivingBase entityLiving = entityLivingRef.get();
        if (pass == Pass.Object)
        {
            if (entityLiving == null || entityLiving.isDead || entityLiving.isInvisibleToPlayer(minecraft.thePlayer)
                    || !entityLiving.addedToChunk || (hideSneaks && entityLiving.isSneaking()))
            {
                screenPosition = null;
                return;
            }
            else
            {
                screenPosition = gridRenderer.getPixel(entityLiving.posX, entityLiving.posZ);
            }
        }

        if (screenPosition != null)
        {
            double heading = entityLiving.rotationYawHead;
            double drawX = screenPosition.getX() + xOffset;
            double drawY = screenPosition.getY() + yOffset;

            float alpha = 1f;
            if (entityLiving.posY > minecraft.thePlayer.posY)
            {
                alpha = 1f - Math.max(.1f, (float) ((entityLiving.posY - minecraft.thePlayer.posY) / 32f));
            }

            if (entityLiving instanceof EntityPlayer)
            {
                //int blockSize = (int) Math.pow(2, gridRenderer.getZoom());
                //float labelOffset = texture != null ? texture.getHeight() / blockSize : 0;
                drawPlayer(pass, drawX, drawY, gridRenderer, alpha, heading, drawScale, fontScale, rotation);

            }
            else
            {
                drawCreature(pass, drawX, drawY, gridRenderer, alpha, heading, drawScale, fontScale, rotation);
            }
        }
    }

    private void drawPlayer(Pass pass, double drawX, double drawY, GridRenderer gridRenderer, float alpha, double heading, float drawScale, double fontScale, double rotation)
    {
        EntityLivingBase entityLiving = entityLivingRef.get();
        if (entityLiving == null)
        {
            return;
        }

        if (pass == Pass.Object)
        {
            if (locatorTexture != null && showHeading)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, alpha, drawScale, rotation);
            }

            if (texture != null)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, true, texture, alpha, drawScale * .75f, rotation);
            }
        }

        if (pass == Pass.Text)
        {
            int labelOffset = texture == null ? 0 : rotation == 0 ? -texture.getHeight() / 2 : texture.getHeight() / 2;
            Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, -labelOffset);

            Team team = entityLiving.getTeam();
            if (team == null || !(entityLiving instanceof EntityPlayer))
            {
                DrawUtil.drawLabel(ForgeHelper.INSTANCE.getEntityName(entityLiving), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, RGB.BLACK_RGB, .8f, RGB.GREEN_RGB, 1f, fontScale, false, rotation);
            }
            else
            {
                String playerName = ScorePlayerTeam.formatPlayerName(entityLiving.getTeam(), ForgeHelper.INSTANCE.getEntityName(entityLiving));
                DrawUtil.drawLabel(playerName, labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, RGB.BLACK_RGB, .8f, RGB.WHITE_RGB, 1f, fontScale, false, rotation);
            }
        }
    }

    private void drawCreature(Pass pass, double drawX, double drawY, GridRenderer gridRenderer, float alpha, double heading, float drawScale, double fontScale, double rotation)
    {
        EntityLivingBase entityLiving = entityLivingRef.get();
        if (entityLiving == null)
        {
            return;
        }

        //Math.min(1f, Math.max(0f, (float) (16 - (entityLiving.posY - minecraft.thePlayer.posY))));

        if (pass == Pass.Object)
        {
            if (locatorTexture != null && showHeading)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, alpha, drawScale, rotation);
            }
        }

        int labelOffset = texture == null ? 8 : rotation == 0 ? texture.getHeight() : -texture.getHeight();

        if (pass == Pass.Text)
        {
            if (customName != null)
            {
                Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, labelOffset);
                DrawUtil.drawCenteredLabel(customName, labelPoint.getX(), labelPoint.getY(), labelBg, labelBgAlpha, RGB.WHITE_RGB, labelFgAlpha, fontScale, rotation);
            }
        }

        if (pass == Pass.Object)
        {
            if (texture != null)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, true, texture, alpha, drawScale, rotation);
            }
        }
    }

    @Override
    public int getDisplayOrder()
    {
        return customName != null ? 1 : 0;
    }

    @Override
    public String getModId()
    {
        return Journeymap.MOD_ID;
    }

    public static class SimpleCacheLoader extends CacheLoader<EntityLivingBase, DrawEntityStep>
    {
        @Override
        public DrawEntityStep load(EntityLivingBase entityLiving) throws Exception
        {
            return new DrawEntityStep(entityLiving);
        }
    }
}
