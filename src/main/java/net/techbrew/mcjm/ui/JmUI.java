package net.techbrew.mcjm.ui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.techbrew.mcjm.render.draw.DrawUtil;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

public abstract class JmUI extends GuiScreen {

    TextureImpl logo = TextureCache.instance().getLogo();

	public abstract void close();


    public JmUI() {
        super();
//        width = field_146294_l;
//        height = field_146295_m;
//        mc = field_146297_k;
//        fontRenderer = super.field_146289_q;
//        buttonList = field_146292_n;
    }

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

    public void sizeDisplay(boolean scaled) {
        final int glwidth = scaled ? this.field_146294_l : field_146297_k.displayWidth;
        final int glheight = scaled ? this.field_146295_m : field_146297_k.displayHeight;
        sizeDisplay(glwidth, glheight);
    }

    public void drawLogo() {
        sizeDisplay(field_146297_k.displayWidth, field_146297_k.displayHeight);
        DrawUtil.drawImage(logo, (field_146297_k.displayWidth - logo.width) / 2, 20, false);
        sizeDisplay(field_146294_l, field_146295_m);
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
    // setWorldAndResolution
    public void func_146280_a(Minecraft minecraft, int width, int height) {
        super.func_146280_a(minecraft, width, height);
    }
}
