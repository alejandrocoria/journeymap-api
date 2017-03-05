/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import com.google.common.cache.CacheLoader;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.minimap.EntityDisplay;
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

    boolean useDots;
    int elevationOffset;
    int color;
    boolean hideSneaks;
    boolean showHeading = true;
    Minecraft minecraft = Minecraft.getMinecraft();
    TextureImpl entityTexture;
    TextureImpl locatorTexture;
    WeakReference<EntityLivingBase> entityLivingRef;
    String customName;
    Point2D screenPosition;
    float drawScale = 1f;

    private DrawEntityStep(EntityLivingBase entityLiving)
    {
        super();
        this.entityLivingRef = new WeakReference<>(entityLiving);
        hideSneaks = Journeymap.getClient().getCoreProperties().hideSneakingEntities.get();
    }

    public void update(EntityDisplay entityDisplay, TextureImpl locatorTexture, TextureImpl entityTexture, int color, boolean showHeading)
    {
        EntityLivingBase entityLiving = entityLivingRef.get();
        if (entityLiving != null)
        {
            customName = DataCache.INSTANCE.getEntityDTO(entityLiving).customName;
        }

        this.useDots = entityDisplay.isDots();
        this.color = color;
        this.locatorTexture = locatorTexture;
        this.entityTexture = entityTexture;
        this.drawScale = (entityDisplay == EntityDisplay.SmallIcons) ? .66666666f : 1f;
        this.showHeading = showHeading;
    }


    @Override
    public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
    {
        if (pass == Pass.Tooltip)
        {
            return;
        }

        EntityLivingBase entityLiving = entityLivingRef.get();
        if (pass == Pass.Object)
        {
            if (entityLiving == null
                    || entityLiving.isDead
                    || entityLiving.isInvisibleToPlayer(minecraft.thePlayer)
                    || !entityLiving.addedToChunk
                    || (hideSneaks && entityLiving.isSneaking()))
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
                //float labelOffset = upperTexture != null ? entityTexture.getHeight() / blockSize : 0;
                drawPlayer(pass, drawX, drawY, gridRenderer, alpha, heading, fontScale, rotation);
            }
            else
            {
                drawCreature(pass, drawX, drawY, gridRenderer, alpha, heading, fontScale, rotation);
            }
        }
    }

    private void drawPlayer(Pass pass, double drawX, double drawY, GridRenderer gridRenderer, float alpha, double heading, double fontScale, double rotation)
    {
        EntityLivingBase entityLiving = entityLivingRef.get();
        if (entityLiving == null)
        {
            return;
        }

        if (pass == Pass.Object)
        {
            if (locatorTexture != null)
            {
                DrawUtil.drawColoredEntity(drawX, drawY, locatorTexture, color, alpha, drawScale, showHeading ? heading : -rotation);
            }

            if (entityTexture != null)
            {
                if (useDots)
                {
                    boolean flip = false;
                    elevationOffset = (int) (DataCache.getPlayer().posY - entityLiving.posY);
                    if (elevationOffset < -1 || elevationOffset > 1)
                    {
                        flip = (elevationOffset < -1);
                        // marker-chevron
                        DrawUtil.drawColoredEntity(drawX, drawY, entityTexture, color, alpha, drawScale, flip ? -rotation + 180 : -rotation);
                    }

                }
                else
                {
                    DrawUtil.drawColoredEntity(drawX, drawY, entityTexture, color, alpha, drawScale, heading);
                }
            }
        }

        if (pass == Pass.Text)
        {
            int labelOffset = entityTexture == null ? 0 : rotation == 0 ? -entityTexture.getHeight() / 2 : entityTexture.getHeight() / 2;
            Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, -labelOffset);

            Team team = entityLiving.getTeam();
            if (team == null || !(entityLiving instanceof EntityPlayer))
            {
                DrawUtil.drawLabel(entityLiving.getName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, RGB.BLACK_RGB, .8f, RGB.GREEN_RGB, 1f, fontScale, false, rotation);
            }
            else
            {
                String playerName = ScorePlayerTeam.formatPlayerName(entityLiving.getTeam(), entityLiving.getName());
                DrawUtil.drawLabel(playerName, labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, RGB.BLACK_RGB, .8f, RGB.WHITE_RGB, 1f, fontScale, false, rotation);
            }
        }
    }

    private void drawCreature(Pass pass, double drawX, double drawY, GridRenderer gridRenderer, float alpha, double heading, double fontScale, double rotation)
    {
        EntityLivingBase entityLiving = entityLivingRef.get();
        if (entityLiving == null)
        {
            return;
        }

        if (pass == Pass.Object)
        {
            if (locatorTexture != null)
            {
                DrawUtil.drawColoredEntity(drawX, drawY, locatorTexture, color, alpha, drawScale, showHeading ? heading : -rotation);
            }
        }

        int labelOffset = entityTexture == null ? 8 : rotation == 0 ? entityTexture.getHeight() : -entityTexture.getHeight();

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
            if (entityTexture != null)
            {
                if (useDots)
                {
                    boolean flip = false;
                    elevationOffset = (int) (DataCache.getPlayer().posY - entityLiving.posY);
                    if (elevationOffset < -1 || elevationOffset > 1)
                    {
                        flip = (elevationOffset < -1);
                        // marker-chevron
                        DrawUtil.drawColoredEntity(drawX, drawY, entityTexture, color, alpha, drawScale, flip ? -rotation + 180 : -rotation);
                    }
                }
                else
                {
                    // mob icon
                    DrawUtil.drawEntity(drawX, drawY, -rotation, entityTexture, alpha, drawScale, 0);
                }
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
