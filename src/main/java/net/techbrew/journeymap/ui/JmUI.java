package net.techbrew.journeymap.ui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.logging.Logger;

public abstract class JmUI extends GuiScreen {

    TextureImpl logo = TextureCache.instance().getLogo();

    protected final String title;
    protected final int headerHeight = 25;
    protected int scaleFactor = 1;
    protected final Logger logger = JourneyMap.getLogger();

    public JmUI(String title) {
        super();
        this.title = title;
    }

    public Minecraft getMinecraft() {
        return this.mc;
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        super.setWorldAndResolution(minecraft, width, height);
        ScaledResolution resolution = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight);
        this.scaleFactor = resolution.getScaleFactor();
    }

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }

    public void sizeDisplay(boolean scaled) {
        final int glwidth = scaled ? this.width : mc.displayWidth;
        final int glheight = scaled ? this.height : mc.displayHeight;
        sizeDisplay(glwidth, glheight);
    }

    protected boolean mouseOverButtons(int x, int y) {

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            if(guibutton instanceof MapButton) {
                if(((MapButton)guibutton).mouseOver(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void drawLogo() {
        sizeDisplay(mc.displayWidth, mc.displayHeight);

        final boolean smallScale = (scaleFactor==1);
        DrawUtil.drawImage(logo, smallScale ? 8 : 16, 4, false, smallScale ? .5f : 1f);
        sizeDisplay(width, height);
    }

    protected void drawTitle() {
        DrawUtil.drawRectangle(0, 0, this.width, headerHeight, Color.black, 100);
        DrawUtil.drawCenteredLabel(this.title, this.width/2, 12, Color.black, 0, Color.CYAN, 255, 1);
    }

    @Override
    public void drawBackground(int layer)
    {
        super.drawBackground(layer);
        drawLogo();
    }

    protected abstract void layoutButtons();

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }

        drawTitle();
        drawLogo();
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

    public void close()
    {
    }
}
