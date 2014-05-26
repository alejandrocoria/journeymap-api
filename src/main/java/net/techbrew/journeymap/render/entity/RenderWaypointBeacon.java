package net.techbrew.journeymap.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
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
    static StatTimer timer = StatTimer.get("WaypointBeacon.doRender", 100);
    static StatTimer allTimer = StatTimer.get("WaypointBeacon.renderAll", 100);
    static Minecraft mc = Minecraft.getMinecraft();
    static RenderManager renderManager = RenderManager.instance;
    static String distanceLabel = Constants.getString("Waypoint.distance_meters", "%1.0f");
    static WaypointProperties waypointProperties;

    public static void resetStatTimers()
    {
        timer.reset();
        allTimer.reset();
    }

    public static void renderAll()
    {
        try
        {
            if (waypointProperties == null)
            {
                waypointProperties = JourneyMap.getInstance().waypointProperties;
            }

            Collection<Waypoint> waypoints = WaypointStore.instance().getAll();
            allTimer.start();
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
                        JourneyMap.getLogger().severe("EntityWaypoint failed to render for " + wp + ": " + LogFormatter.toString(t));
                    }
                }
            }
        }
        catch (Throwable t)
        {
            allTimer.cancel();
            JourneyMap.getLogger().severe("Error rendering waypoints: " + LogFormatter.toString(t));
        }
        finally
        {
            allTimer.stop();
        }
    }

    static void doRender(Waypoint waypoint)
    {
        timer.start();

        // Start drawing
        GL11.glPushMatrix();

        try
        {
            final int dimension = renderManager.livingPlayer.dimension;

            // Player coords
            Vec3 playerVec = renderManager.livingPlayer.getPosition(1);

            // Get waypoint coords for dimension
            Vec3 waypointVec = mc.theWorld.getWorldVec3Pool().getVecFromPool(waypoint.getX(dimension), waypoint.getY(dimension), waypoint.getZ(dimension));

            // Get view distance from waypoint
            final double actualDistance = playerVec.distanceTo(waypointVec);
            final int maxDistance = waypointProperties.maxDistance.get();
            if (maxDistance > 0 && actualDistance > maxDistance)
            {
                return;
            }

            // Adjust waypoint render position if needed
            double viewDistance = actualDistance;
            double maxRenderDistance = 256 >> mc.gameSettings.renderDistance;
            if (viewDistance > maxRenderDistance)
            {
                Vec3 delta = waypointVec.subtract(playerVec).normalize();
                waypointVec = playerVec.addVector(-delta.xCoord * maxRenderDistance, -delta.yCoord * maxRenderDistance, -delta.zCoord * maxRenderDistance);
                viewDistance = maxRenderDistance;
            }

            double shiftX = .5 + (waypointVec.xCoord - renderManager.viewerPosX);
            double shiftY = .5 + (waypointVec.yCoord - renderManager.viewerPosY);
            double shiftZ = .5 + (waypointVec.zCoord - renderManager.viewerPosZ);

            boolean showStaticBeam = waypointProperties.showStaticBeam.get();
            boolean showRotatingBeam = waypointProperties.showRotatingBeam.get();
            if (showStaticBeam || showRotatingBeam)
            {
                renderBeam(shiftX - .5, -renderManager.viewerPosY, shiftZ - .5, waypoint.getColor(), showStaticBeam, showRotatingBeam);
            }

            // Check for auto-hidden labels
            boolean labelHidden = false;
            if (waypointProperties.autoHideLabel.get())
            {
                Vec3 playerLookVec = renderManager.livingPlayer.getLook(1.0F).normalize();
                Vec3 delta = mc.theWorld.getWorldVec3Pool().getVecFromPool(waypointVec.xCoord - renderManager.viewerPosX, waypointVec.yCoord - renderManager.viewerPosY, waypointVec.zCoord - renderManager.viewerPosZ);
                double distance = delta.lengthVector();
                delta = delta.normalize();
                double dp = playerLookVec.dotProduct(delta);
                labelHidden = dp < (1.0D - (.5D / distance));
            }

            // Construct label
            StringBuffer sb = new StringBuffer();
            if (!labelHidden)
            {
                if (waypointProperties.boldLabel.get())
                {
                    sb.append("§l");
                }

                if (waypointProperties.showName.get())
                {
                    sb.append(waypoint.getName());
                }

                if (waypointProperties.showDistance.get())
                {
                    sb.append(" ").append(String.format(distanceLabel, actualDistance));
                }
            }

            // Set render scale (1/64)
            double scale = 0.00390625 * ((viewDistance + 4) / 3);
            FontRenderer fr = renderManager.getFontRenderer();

            // Position
            GL11.glTranslated(shiftX, shiftY, shiftZ);
            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScaled(-scale, -scale, scale);

            // Lighting
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glNormal3d(0, 0, -1.0F * scale);
            DrawUtil.resetLightMap();

            final TextureImpl texture = waypoint.getTexture();
            double halfTexHeight = texture.height / 2;

            // Depth-masked and non-masked label
            if (sb.length() > 0)
            {
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                String label = sb.toString();
                final int fontScale = waypointProperties.fontSmall.get() ? 1 : 2;

                boolean forced = DrawUtil.startUnicode(fr, waypointProperties.forceUnicode.get());
                DrawUtil.drawLabel(label, 0, 0 - halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 150, waypoint.getSafeColor(), 255, fontScale, false);

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);

                DrawUtil.drawLabel(label, 0, 0 - halfTexHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 100, waypoint.getSafeColor(), 200, fontScale, false);
                if (forced)
                {
                    DrawUtil.stopUnicode(fr);
                }
            }

            // Depth-masked icon
            if (waypointProperties.showTexture.get())
            {
                // Reset scale for the icon
                GL11.glPopMatrix();
                GL11.glPushMatrix();

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);

                scale = 0.5 * scale * (waypointProperties.textureSmall.get() ? 1 : 2);

                GL11.glTranslated(shiftX, shiftY, shiftZ);
                GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GL11.glScaled(-scale, -scale, scale);
                GL11.glNormal3d(0, 0, -1.0F * scale);

                DrawUtil.drawColoredImage(texture, 255, waypoint.getColor(), 0 - (texture.width / 2), 0 - halfTexHeight);
            }
        }
        finally
        {
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);

            // Reset GL
            GL11.glPopMatrix();

            timer.stop();
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
        float texOffset = -(-time * 0.2F - (float) MathHelper.floor_float(-time * 0.1F)) * .6f;

        if (rotatingBeam)
        {
            byte b0 = 1;
            double d3 = (double) time * 0.025D * (1.0D - (double) (b0 & 1) * 2.5D);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), 60);
            double d4 = (double) b0 * 0.2D;
            double d5 = 0.5D + Math.cos(d3 + 2.356194490192345D) * d4;
            double d6 = 0.5D + Math.sin(d3 + 2.356194490192345D) * d4;
            double d7 = 0.5D + Math.cos(d3 + (Math.PI / 4D)) * d4;
            double d8 = 0.5D + Math.sin(d3 + (Math.PI / 4D)) * d4;
            double d9 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * d4;
            double d10 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * d4;
            double d11 = 0.5D + Math.cos(d3 + 5.497787143782138D) * d4;
            double d12 = 0.5D + Math.sin(d3 + 5.497787143782138D) * d4;
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
