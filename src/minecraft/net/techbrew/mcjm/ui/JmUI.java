package net.techbrew.mcjm.ui;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import org.lwjgl.opengl.GL11;

public abstract class JmUI extends GuiScreen {
	
	public abstract void close();
	
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

    void sizeDisplay(boolean scaled) {
        final int glWidth = scaled ? this.width : mc.displayWidth;
        final int glHeight = scaled ? this.height : mc.displayHeight;
        sizeDisplay(glWidth, glHeight);
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

}
