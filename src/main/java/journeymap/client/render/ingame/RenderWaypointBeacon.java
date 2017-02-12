/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.ingame;

import journeymap.client.Constants;
import journeymap.client.cartography.RGB;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

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
    static Minecraft mc = FMLClientHandler.instance().getClient();
    static RenderManager renderManager = mc.getRenderManager();
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
            waypointProperties = Journeymap.getClient().getWaypointProperties();

            Collection<Waypoint> waypoints = WaypointStore.INSTANCE.getAll();
            //allTimer.start();
            final int playerDim = mc.player.dimension;
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
                        Journeymap.getLogger().error("EntityWaypoint failed to render for " + wp + ": " + LogFormatter.toString(t));
                    }
                }
            }
        }
        catch (Throwable t)
        {
            //allTimer.cancel();
            Journeymap.getLogger().error("Error rendering waypoints: " + LogFormatter.toString(t));
        }
        finally
        {
            //allTimer.stop();
        }
    }

    static void doRender(Waypoint waypoint)
    {
        if (renderManager.renderViewEntity == null)
        {
            return;
        }

        //timer.start();

        RenderHelper.enableStandardItemLighting();

        try
        {
            // Player coords
            Vec3d playerVec = renderManager.renderViewEntity.getPositionVector();

            // Move y up to put icon at eye height
            Vec3d waypointVec = waypoint.getPosition().addVector(0, .118, 0);

            // Get view distance from waypoint
            final double actualDistance = playerVec.distanceTo(waypointVec);
            final int maxDistance = waypointProperties.maxDistance.get();

            if (maxDistance > 0 && actualDistance > maxDistance)
            {
                return;
            }

            float fadeAlpha = 1f;
            final int minDistance = waypointProperties.minDistance.get();
            if (minDistance > 0)
            {
                if ((int) actualDistance <= minDistance)
                {
                    return;
                }
                if ((int) actualDistance <= minDistance + 4)
                {
                    fadeAlpha = (float) (actualDistance - minDistance) / 3f;
                }
            }

            // Adjust waypoint render position if needed
            double viewDistance = actualDistance;
            double maxRenderDistance = mc.gameSettings.renderDistanceChunks * 16;
            if (viewDistance > maxRenderDistance)
            {
                Vec3d delta = waypointVec.subtract(playerVec).normalize();
                waypointVec = playerVec.addVector(delta.xCoord * maxRenderDistance, delta.yCoord * maxRenderDistance, delta.zCoord * maxRenderDistance);
                viewDistance = maxRenderDistance;
            }

            double shiftX = (waypointVec.xCoord - renderManager.viewerPosX);
            double shiftY = (waypointVec.yCoord - renderManager.viewerPosY);
            double shiftZ = (waypointVec.zCoord - renderManager.viewerPosZ);

            boolean showStaticBeam = waypointProperties.showStaticBeam.get();
            boolean showRotatingBeam = waypointProperties.showRotatingBeam.get();
            if (showStaticBeam || showRotatingBeam)
            {
                renderBeam(shiftX, -renderManager.viewerPosY, shiftZ, waypoint.getColor(), fadeAlpha, showStaticBeam, showRotatingBeam);
            }

            String label = waypoint.getName();

            // Check for auto-hidden labels
            boolean labelHidden = false;
            if (viewDistance > .5 && waypointProperties.autoHideLabel.get())
            {
                // 2D algorithm (ignore pitch)
                int angle = 5;

                double yaw = Math.atan2(renderManager.viewerPosZ - waypointVec.zCoord, renderManager.viewerPosX - waypointVec.xCoord);
                double degrees = Math.toDegrees(yaw) + 90;
                if (degrees < 0)
                {
                    degrees = 360 + degrees;
                }
                double playerYaw = renderManager.renderViewEntity.getRotationYawHead() % 360;
                if (playerYaw < 0)
                {
                    playerYaw += 360;
                }
                playerYaw = Math.toRadians(playerYaw);
                double playerDegrees = Math.toDegrees(playerYaw);

                degrees += angle;
                playerDegrees += angle;

                labelHidden = Math.abs((degrees + angle) - (playerDegrees + angle)) > angle;
            }

            // Set render scale (1/64)
            double scale = 0.00390625 * ((viewDistance + 4) / 3);

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
                    sb.append(TextFormatting.BOLD);
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
                    GlStateManager.pushMatrix();

                    // Lighting
                    GlStateManager.disableLighting();
                    GL11.glNormal3d(0, 0, -1.0F * scale);

                    // Position
                    GlStateManager.translate(shiftX, shiftY, shiftZ);
                    GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(-scale, -scale, scale);

                    GlStateManager.depthMask(true);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();

                    final int fontScale = waypointProperties.fontScale.get();

                    // Adjust above icon
                    double labelY = (0 - halfTexHeight) - 8;

                    // Depth label
                    DrawUtil.drawLabel(label, 1, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, RGB.BLACK_RGB, .6f * fadeAlpha, waypoint.getSafeColor(), fadeAlpha, fontScale, false);

                    GlStateManager.disableDepth();
                    GlStateManager.depthMask(false);

                    // Front label
                    DrawUtil.drawLabel(label, 1, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, RGB.BLACK_RGB, .4f * fadeAlpha, waypoint.getSafeColor(), fadeAlpha, fontScale, false);

                    GlStateManager.popMatrix();
                }
            }

            // Depth-masked icon
            if (viewDistance > .1 && waypointProperties.showTexture.get())
            {
                // Reset scale for the icon

                GlStateManager.pushMatrix();

                GlStateManager.disableLighting();
                GL11.glNormal3d(0, 0, -1.0F * scale);

                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);

                scale = scale * (waypointProperties.textureSmall.get() ? 1 : 2);

                GlStateManager.translate(shiftX, shiftY, shiftZ);
                GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-scale, -scale, scale);
                GL11.glNormal3d(0, 0, -1.0F * scale);

                // The .5 and .2 below centers the waypoint diamond icon
                DrawUtil.drawColoredImage(texture, waypoint.getColor(), fadeAlpha, 0 - (texture.getWidth() / 2) + .5, 0 - halfTexHeight + .2, 0);

                GlStateManager.popMatrix();
            }
        }
        finally
        {

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.disableFog();

            RenderHelper.disableStandardItemLighting();
        }
    }

    /**
     * Render beam
     */
    static void renderBeam(double x, double y, double z, Integer color, float alpha, boolean staticBeam, boolean rotatingBeam)
    {
        float f1 = alpha;

        mc.renderEngine.bindTexture(beam);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        //GlStateManager.depthMask(false);

        float time = (float) mc.world.getTotalWorldTime();
        if (mc.isGamePaused())
        {
            // Show rotation for Waypoint Options UI
            time = Minecraft.getSystemTime() / 50;
        }

        float texOffset = -(-time * 0.2F - (float) MathHelper.floor(-time * 0.1F)) * .6f;

        if (rotatingBeam)
        {
            byte b0 = 1;
            double d3 = (double) time * 0.025D * (1.0D - (double) (b0 & 1) * 2.5D);
            //double d3 = (double) time * 0.025D * -1.5D;

            int[] rgba = RGB.ints(color, alpha * .45f);
            DrawUtil.startDrawingQuads(true);
            GlStateManager.enableBlend();

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
            DrawUtil.addVertexWithUV(x + d5, y + d13, z + d6, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d5, y, z + d6, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d7, y, z + d8, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d7, y + d13, z + d8, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d11, y + d13, z + d12, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d11, y, z + d12, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d9, y, z + d10, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d9, y + d13, z + d10, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d7, y + d13, z + d8, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d7, y, z + d8, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d11, y, z + d12, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d11, y + d13, z + d12, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d9, y + d13, z + d10, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d9, y, z + d10, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d5, y, z + d6, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d5, y + d13, z + d6, d14, d17, rgba);
            DrawUtil.draw();
        }

        if (staticBeam)
        {
            GlStateManager.disableCull();

            double d26 = (double) (256.0F * f1);
            double d29 = (double) (-1.0F + texOffset);
            double d30 = (double) (256.0F * f1) + d29;
            x -= .5;
            z -= .5;

            // Next 3 lines are for 1.8
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.depthMask(false);

            int[] rgba = RGB.ints(color, alpha * .4f);
            DrawUtil.startDrawingQuads(true);

            DrawUtil.addVertexWithUV(x + .2, y + d26, z + .2, 1, d30, rgba);
            DrawUtil.addVertexWithUV(x + .2, y, z + .2, 1, d29, rgba);
            DrawUtil.addVertexWithUV(x + .8, y, z + .2, 0, d29, rgba);
            DrawUtil.addVertexWithUV(x + .8, y + d26, z + .2, 0, d30, rgba);
            DrawUtil.addVertexWithUV(x + .8, y + d26, z + .8, 1, d30, rgba);
            DrawUtil.addVertexWithUV(x + .8, y, z + .8, 1, d29, rgba);
            DrawUtil.addVertexWithUV(x + .2, y, z + .8, 0, d29, rgba);
            DrawUtil.addVertexWithUV(x + .2, y + d26, z + .8, 0, d30, rgba);
            DrawUtil.addVertexWithUV(x + .8, y + d26, z + .2, 1, d30, rgba);
            DrawUtil.addVertexWithUV(x + .8, y, z + .2, 1, d29, rgba);
            DrawUtil.addVertexWithUV(x + .8, y, z + .8, 0, d29, rgba);
            DrawUtil.addVertexWithUV(x + .8, y + d26, z + .8, 0, d30, rgba);
            DrawUtil.addVertexWithUV(x + .2, y + d26, z + .8, 1, d30, rgba);
            DrawUtil.addVertexWithUV(x + .2, y, z + .8, 1, d29, rgba);
            DrawUtil.addVertexWithUV(x + .2, y, z + .2, 0, d29, rgba);
            DrawUtil.addVertexWithUV(x + .2, y + d26, z + .2, 0, d30, rgba);
            DrawUtil.draw();
            GlStateManager.disableBlend();
        }

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();

    }

}
