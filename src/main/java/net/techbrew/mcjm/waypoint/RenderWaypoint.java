package net.techbrew.mcjm.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.render.draw.DrawUtil;
import net.techbrew.mcjm.render.texture.TextureCache;
import org.lwjgl.opengl.GL11;

/**
 * Created by mwoodman on 12/26/13.
 */
public class RenderWaypoint extends RenderEntity {

    Minecraft mc = Minecraft.getMinecraft();

    public RenderWaypoint(Minecraft mc, RenderManager renderManager) {
        this.mc = mc;
        setRenderManager(renderManager);
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float ignored1, float ignored2) {

        final EntityWaypoint ew = (EntityWaypoint) entity;
        final Waypoint waypoint = ew.getWaypoint();

        final ChunkCoordinates playerLoc = mc.thePlayer.getPlayerCoordinates();


        double var10 = Math.sqrt(entity.getDistanceSqToEntity(renderManager.livingPlayer));
        final String par2Str = new StringBuilder(waypoint.getDisplay()).append(String.format(" [%1.2fm]", new Object[] { var10 })).toString();
        int maxDisplayDistance = 16;
        //if ((var10 <= maxDisplayDistance) || (maxDisplayDistance < 0))
        {

            float var8 = 1.6F;
            float var9 = 0.016666668F * var8;
            FontRenderer var14 = this.getFontRendererFromRenderManager();
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x + 0.5F, (float) y + 1.3F, (float) z);

            GL11.glNormal3f(0.0F, 1.0F, 0.0F);

            GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

            GL11.glScalef(-var9, -var9, var9);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glTranslatef(0.0F, 0.25F / var9, 0.0F);
            //GL11.glDisable(GL11.GL_DEPTH_TEST);
            //GL11.glDepthMask(false);
            DrawUtil.drawColoredImage(TextureCache.instance().getWaypoint(), 255, waypoint.getColor(), -8, 8);




            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);



            Tessellator var15 = Tessellator.instance;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            var15.startDrawingQuads();
            int var16 = var14.getStringWidth(par2Str) / 2;
            var15.setColorRGBA_F(0.0F, 0.0F, 0.0F, .8F);
            var15.addVertex((double) (-var16 - 1), -1.0D, 0.0D);
            var15.addVertex((double)(-var16 - 1), 8.0D, 0.0D);
            var15.addVertex((double)(var16 + 1), 8.0D, 0.0D);
            var15.addVertex((double)(var16 + 1), -1.0D, 0.0D);
            var15.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            var14.drawString(par2Str, -var14.getStringWidth(par2Str) / 2, 0, waypoint.getColor().getRGB());
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            var15.setColorRGBA_F(0.0F, 0.0F, 0.0F, .5F);
            var14.drawString(par2Str, -var14.getStringWidth(par2Str) / 2, 0, waypoint.getColor().getRGB());
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();


//            float var14 = ((float)var10 * 0.1F + 1.0F) * 0.0266F;
//            GL11.glPushMatrix();
//
//            GL11.glTranslatef((float) x + 0.5F, (float) y + 1.3F, (float) z);
//            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
//            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
//            GL11.glScalef(-var14, -var14, var14);
//            GL11.glDisable(GL11.GL_LIGHTING);
//            GL11.glDisable(GL11.GL_FOG);
//            GL11.glDepthMask(false);
//            GL11.glDisable(GL11.GL_DEPTH_TEST);
//
//
//            GL11.glEnable(GL11.GL_BLEND);
//            GL11.glBlendFunc(770, 771);
//            //GL11.glDisable(GL11.GL_TEXTURE_2D);
//
//            GL11.glEnable(3553);
//            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//
//            DrawUtil.drawCenteredLabel(par2Str, 0, 0, Color.darkGray, waypoint.getColor(), 255, 1);
//
//
//            GL11.glEnable(GL11.GL_DEPTH_TEST);
//            GL11.glDepthMask(true);
//            GL11.glEnable(GL11.GL_FOG);
//            GL11.glEnable(GL11.GL_LIGHTING);
//
//
//
//            GL11.glDisable(GL11.GL_BLEND);
//            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//
//            GL11.glPopMatrix();
        }
    }

}
