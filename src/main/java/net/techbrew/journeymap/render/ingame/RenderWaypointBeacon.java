/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.ingame;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Collection;

/**
 * Renders waypoints in-game.  No longer needs to extend RenderEntity, since waypoint
 * entities weren't worth the overhead.
 */
public class RenderWaypointBeacon
{
    static final ResourceLocation beam = new ResourceLocation("textures/entity/beacon_beam.png");
    //    static StatTimer timer = StatTimer.get("WaypointBeacon.doRender", 100);
//    static StatTimer allTimer = StatTimer.get("WaypointBeacon.renderAll", 100);
    static Minecraft mc = ForgeHelper.INSTANCE.getClient();
    static RenderManager renderManager = ForgeHelper.INSTANCE.getRenderManager();
    static String distanceLabel = Constants.getString("jm.waypoint.distance_meters", "%1.0f");
    static WaypointProperties waypointProperties;

    public static void resetStatTimers()
    {
//        timer.reset();
//        allTimer.reset();
    }

    public static void renderAll()
    {
        try
        {
            waypointProperties = JourneyMap.getWaypointProperties();

            Collection<Waypoint> waypoints = WaypointStore.instance().getAll();
            //allTimer.start();
            final int playerDim = mc.thePlayer.dimension;
            for (Waypoint wp : waypoints)
            {
                if (wp.isEnable() && wp.getDimensions().contains(playerDim))
                {
                    try
                    {
                        doRender(wp);
                    }
                    catch (Throwable t)
                    {
                        JourneyMap.getLogger().error("EntityWaypoint failed to render for " + wp + ": " + LogFormatter.toString(t));
                    }
                }
            }
        }
        catch (Throwable t)
        {
            //allTimer.cancel();
            JourneyMap.getLogger().error("Error rendering waypoints: " + LogFormatter.toString(t));
        }
        finally
        {
            //allTimer.stop();
        }
    }

    static void doRender(Waypoint waypoint)
    {
        if (renderManager.livingPlayer == null)
        {
            return;
        }

        //timer.start();

        RenderHelper.enableStandardItemLighting();

        try
        {
            final int dimension = renderManager.livingPlayer.dimension;

            // Player coords
            Vec3 playerVec = ForgeHelper.INSTANCE.getEntityPositionVector(renderManager.livingPlayer);

            // Get waypoint coords for dimension, raise Y to eye level for icon
            Vec3 waypointVec = waypoint.getPosition().add(new Vec3(0, .118, 0));

            // Get view distance from waypoint
            final double actualDistance = playerVec.distanceTo(waypointVec);
            final int maxDistance = waypointProperties.maxDistance.get();
            if (maxDistance > 0 && actualDistance > maxDistance)
            {
                return;
            }

            // Adjust waypoint render position if needed
            double viewDistance = actualDistance;
            double maxRenderDistance = mc.gameSettings.renderDistanceChunks * 16;
            if (viewDistance > maxRenderDistance)
            {
                Vec3 delta = waypointVec.subtract(playerVec).normalize();
                waypointVec = playerVec.addVector(-delta.xCoord * maxRenderDistance, -delta.yCoord * maxRenderDistance, -delta.zCoord * maxRenderDistance);
                viewDistance = maxRenderDistance;
            }

            double shiftX = (waypointVec.xCoord - renderManager.viewerPosX);
            double shiftY = (waypointVec.yCoord - renderManager.viewerPosY);
            double shiftZ = (waypointVec.zCoord - renderManager.viewerPosZ);

            boolean showStaticBeam = waypointProperties.showStaticBeam.get();
            boolean showRotatingBeam = waypointProperties.showRotatingBeam.get();
            if (showStaticBeam || showRotatingBeam)
            {
                renderBeam(shiftX, -renderManager.viewerPosY, shiftZ, waypoint.getColor(), showStaticBeam, showRotatingBeam);
            }

            String label = waypoint.getName();

            // Check for auto-hidden labels
            boolean labelHidden = false;
            if (viewDistance > .5 && waypointProperties.autoHideLabel.get())
            {
                // 3D algorithm
//                Vec3 playerLookVec = renderManager.livingPlayer.getLook(1.0F).normalize();
//                Vec3 delta = mc.theWorld.getWorldVec3Pool().getVecFromPool(waypointVec.xCoord - renderManager.viewerPosX, waypointVec.yCoord - renderManager.viewerPosY, waypointVec.zCoord - renderManager.viewerPosZ);
//                delta.yCoord = 0;
//                double distance = delta.lengthVector();
//                delta = delta.normalize();
//                double dp = playerLookVec.dotProduct(delta);
//                labelHidden = dp < (1.0D - (.5D / distance));

                // 2D algorithm (ignore pitch)
                int angle = 5;

                double yaw = Math.atan2(renderManager.viewerPosZ - waypointVec.zCoord, renderManager.viewerPosX - waypointVec.xCoord);
                double degrees = Math.toDegrees(yaw) + 90;
                if (degrees < 0)
                {
                    degrees = 360 + degrees;
                }
                double playerYaw = renderManager.livingPlayer.getRotationYawHead() % 360;
                if (playerYaw < 0)
                {
                    playerYaw += 360;
                }
                playerYaw = Math.toRadians(playerYaw);
                double playerDegrees = Math.toDegrees(playerYaw);

                degrees += angle;
                playerDegrees += angle;

                labelHidden = Math.abs((degrees + angle) - (playerDegrees + angle)) > angle;
//
//                label = String.format("degrees %1.2f & playerDegrees %1.2f = %s", degrees, playerDegrees, labelHidden);
//                labelHidden = false;
            }

            // Set render scale (1/64)
            double scale = 0.00390625 * ((viewDistance + 4) / 3);
            FontRenderer fr = renderManager.getFontRenderer();

            final TextureImpl texture = waypoint.getTexture();
            double halfTexHeight = texture.getHeight() / 2;

            // Depth-masked and non-masked label
            final boolean showName = waypointProperties.showName.get() && label != null && label.length() > 0;
            final boolean showDistance = waypointProperties.showDistance.get();
            if (!labelHidden && (showName || showDistance))
            {
                // Construct label
                StringBuilder sb = new StringBuilder();

                if (waypointProperties.boldLabel.get())
                {
                    sb.append(EnumChatFormatting.BOLD);
                }
                if (showName)
                {
                    sb.append(label);
                }
                if (showName && showDistance)
                {
                    sb.append(" ");
                }
                if (showDistance)
                {
                    sb.append(String.format(distanceLabel, actualDistance));
                }

                if (sb.length() > 0)
                {
                    label = sb.toString();

                    // Start drawing
                    GL11.glPushMatrix();

                    // Lighting
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glNormal3d(0, 0, -1.0F * scale);

                    // Position
                    GL11.glTranslated(shiftX, shiftY, shiftZ);
                    GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                    GL11.glScaled(-scale, -scale, scale);

                    GL11.glDepthMask(true);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);

                    final int fontScale = waypointProperties.fontScale.get();

                    // Adjust above icon
                    double labelY = (0 - halfTexHeight) - 8;

                    // Depth label
                    DrawUtil.drawLabel(label, 1, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 150, waypoint.getSafeColor(), 255, fontScale, false);

                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glDepthMask(false);

                    // Front label
                    DrawUtil.drawLabel(label, 1, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 100, waypoint.getSafeColor(), 255, fontScale, false);

                    GL11.glPopMatrix();
                }
            }

            // Depth-masked icon
            if (viewDistance > .1 && waypointProperties.showTexture.get())
            {
                // Reset scale for the icon

                GL11.glPushMatrix();

                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glNormal3d(0, 0, -1.0F * scale);

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);

                scale = scale * (waypointProperties.textureSmall.get() ? 1 : 2);

                GL11.glTranslated(shiftX, shiftY, shiftZ);
                GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GL11.glScaled(-scale, -scale, scale);
                GL11.glNormal3d(0, 0, -1.0F * scale);

                // The .5 and .2 below centers the waypoint diamond icon
                DrawUtil.drawColoredImage(texture, 255, waypoint.getColor(), 0 - (texture.getWidth() / 2) + .5, 0 - halfTexHeight + .2, 0);

                GL11.glPopMatrix();
            }
        }
        finally
        {

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);
            // GL11.glEnable(GL11.GL_ALPHA_TEST);

            // Reset GL


            //timer.stop();

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_FOG);

            RenderHelper.disableStandardItemLighting();
        }
    }

    /**
     * Render beam
     */
    static void renderBeam(double x, double y, double z, Color color, boolean staticBeam, boolean rotatingBeam)
    {
        float f1 = 1f;

        Tessellator tessellator = Tessellator.instance;
        mc.renderEngine.bindTexture(beam);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        float time = (float) mc.theWorld.getTotalWorldTime();
        if (mc.isGamePaused())
        {
            // Show rotation for Waypoint Options UI
            time = Minecraft.getSystemTime() / 50;
        }

        float texOffset = -(-time * 0.2F - (float) MathHelper.floor_float(-time * 0.1F)) * .6f;

        if (rotatingBeam)
        {
            byte b0 = 1;
            double d3 = (double) time * 0.025D * (1.0D - (double) (b0 & 1) * 2.5D);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), 80);
            double d4 = (double) b0 * 0.2D;
            double d5 = Math.cos(d3 + 2.356194490192345D) * d4;
            double d6 = Math.sin(d3 + 2.356194490192345D) * d4;
            double d7 = Math.cos(d3 + (Math.PI / 4D)) * d4;
            double d8 = Math.sin(d3 + (Math.PI / 4D)) * d4;
            double d9 = Math.cos(d3 + 3.9269908169872414D) * d4;
            double d10 = Math.sin(d3 + 3.9269908169872414D) * d4;
            double d11 = Math.cos(d3 + 5.497787143782138D) * d4;
            double d12 = Math.sin(d3 + 5.497787143782138D) * d4;
            double d13 = (double) (256.0F * f1);
            double d14 = 0.0D;
            double d15 = 1.0D;
            double d16 = (double) (-1.0F + texOffset);
            double d17 = (double) (256.0F * f1) * (0.5D / d4) + d16;
            tessellator.addVertexWithUV(x + d5, y + d13, z + d6, d15, d17);
            tessellator.addVertexWithUV(x + d5, y, z + d6, d15, d16);
            tessellator.addVertexWithUV(x + d7, y, z + d8, d14, d16);
            tessellator.addVertexWithUV(x + d7, y + d13, z + d8, d14, d17);
            tessellator.addVertexWithUV(x + d11, y + d13, z + d12, d15, d17);
            tessellator.addVertexWithUV(x + d11, y, z + d12, d15, d16);
            tessellator.addVertexWithUV(x + d9, y, z + d10, d14, d16);
            tessellator.addVertexWithUV(x + d9, y + d13, z + d10, d14, d17);
            tessellator.addVertexWithUV(x + d7, y + d13, z + d8, d15, d17);
            tessellator.addVertexWithUV(x + d7, y, z + d8, d15, d16);
            tessellator.addVertexWithUV(x + d11, y, z + d12, d14, d16);
            tessellator.addVertexWithUV(x + d11, y + d13, z + d12, d14, d17);
            tessellator.addVertexWithUV(x + d9, y + d13, z + d10, d15, d17);
            tessellator.addVertexWithUV(x + d9, y, z + d10, d15, d16);
            tessellator.addVertexWithUV(x + d5, y, z + d6, d14, d16);
            tessellator.addVertexWithUV(x + d5, y + d13, z + d6, d14, d17);
            tessellator.draw();
        }

        if (staticBeam)
        {
            GL11.glDisable(GL11.GL_CULL_FACE);

            double d26 = (double) (256.0F * f1);
            double d29 = (double) (-1.0F + texOffset);
            double d30 = (double) (256.0F * f1) + d29;

            x -= .5;
            z -= .5;

            tessellator.startDrawingQuads();
            tessellator.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), 40);
            tessellator.addVertexWithUV(x + .2, y + d26, z + .2, 1, d30);
            tessellator.addVertexWithUV(x + .2, y, z + .2, 1, d29);
            tessellator.addVertexWithUV(x + .8, y, z + .2, 0, d29);
            tessellator.addVertexWithUV(x + .8, y + d26, z + .2, 0, d30);
            tessellator.addVertexWithUV(x + .8, y + d26, z + .8, 1, d30);
            tessellator.addVertexWithUV(x + .8, y, z + .8, 1, d29);
            tessellator.addVertexWithUV(x + .2, y, z + .8, 0, d29);
            tessellator.addVertexWithUV(x + .2, y + d26, z + .8, 0, d30);
            tessellator.addVertexWithUV(x + .8, y + d26, z + .2, 1, d30);
            tessellator.addVertexWithUV(x + .8, y, z + .2, 1, d29);
            tessellator.addVertexWithUV(x + .8, y, z + .8, 0, d29);
            tessellator.addVertexWithUV(x + .8, y + d26, z + .8, 0, d30);
            tessellator.addVertexWithUV(x + .2, y + d26, z + .8, 1, d30);
            tessellator.addVertexWithUV(x + .2, y, z + .8, 1, d29);
            tessellator.addVertexWithUV(x + .2, y, z + .2, 0, d29);
            tessellator.addVertexWithUV(x + .2, y + d26, z + .2, 0, d30);
            tessellator.draw();
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

}
