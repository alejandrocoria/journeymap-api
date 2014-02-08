package net.techbrew.journeymap.ui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

public abstract class JmUI extends GuiScreen {

    TextureImpl logo = TextureCache.instance().getLogo();

	public abstract void close();


    public JmUI() {
        super();
    }

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

    public void sizeDisplay(boolean scaled) {
        final int glwidth = scaled ? this.width : mc.displayWidth;
        final int glheight = scaled ? this.height : mc.displayHeight;
        sizeDisplay(glwidth, glheight);
    }

    public void drawLogo() {
        sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, (mc.displayWidth - logo.width) / 2, 20, false);
        sizeDisplay(width, height);
    }

    public static void sizeDisplay(double width, double height) {

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        super.setWorldAndResolution(minecraft, width, height);
    }
}
