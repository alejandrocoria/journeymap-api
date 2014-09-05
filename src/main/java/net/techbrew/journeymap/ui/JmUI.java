/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public abstract class JmUI extends GuiScreen
{

    protected final String title;
    protected final int headerHeight = 25;
    protected final Logger logger = JourneyMap.getLogger();
    protected final Class<? extends JmUI> returnClass;
    protected int scaleFactor = 1;
    protected TextureImpl logo = TextureCache.instance().getLogo();

    public JmUI(String title)
    {
        this(title, null);
    }

    public JmUI(String title, Class<? extends JmUI> returnClass)
    {
        super();
        this.title = title;
        this.returnClass = returnClass;
    }

    public static void sizeDisplay(double width, double height)
    {

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
    }

    public Minecraft getMinecraft()
    {
        return this.mc;
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height)
    {
        super.setWorldAndResolution(minecraft, width, height);
        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        this.scaleFactor = resolution.getScaleFactor();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    public FontRenderer getFontRenderer()
    {
        return this.fontRendererObj;
    }

    public void sizeDisplay(boolean scaled)
    {
        final int glwidth = scaled ? this.width : mc.displayWidth;
        final int glheight = scaled ? this.height : mc.displayHeight;
        sizeDisplay(glwidth, glheight);
    }

    protected boolean isMouseOverButton(int mouseX, int mouseY, int which)
    {
        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            if (guibutton instanceof Button)
            {
                Button button = (Button) guibutton;
                if (button.mouseOver(mouseX, mouseY))
                {
                    return true;
                }
            }
        }
        return false;
    }

    //    protected boolean mouseOverButtons(int x, int y)
//    {
//        for (int k = 0; k < this.buttonList.size(); ++k)
//        {
//            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
//            if (guibutton instanceof Button)
//            {
//                Button button = (Button) guibutton;
//                if (button.mouseOver(x, y))
//                {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    protected void drawLogo()
    {
        sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, 8,8, false, 1, 0);
        sizeDisplay(width, height);
    }

    protected void drawTitle()
    {
        DrawUtil.drawRectangle(0, 0, this.width, headerHeight, Color.black, 100);
        DrawUtil.drawCenteredLabel(this.title, this.width / 2, 12, Color.black, 0, Color.CYAN, 255, 1, 0);
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
    }

    @Override
    public void drawBackground(int layer)
    {
        drawDefaultBackground();
    }

    protected abstract void layoutButtons();

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        try
        {
            drawBackground(0);
            layoutButtons();

            ArrayList<String> tooltip = null;
            for (int k = 0; k < this.buttonList.size(); ++k)
            {
                GuiButton guibutton = (GuiButton) this.buttonList.get(k);
                guibutton.drawButton(this.mc, x, y);
                if (tooltip == null)
                {
                    if (guibutton instanceof Button)
                    {
                        Button button = (Button) guibutton;
                        if (button.mouseOver(x, y))
                        {
                            tooltip = button.getTooltip();
                        }
                    }
                }
            }

            if (tooltip != null && !tooltip.isEmpty())
            {
                drawHoveringText(tooltip, x, y, getFontRenderer());
                drawHoveringText(tooltip, x, y, getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            }

            drawTitle();
            drawLogo();
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Error in UI: " + LogFormatter.toString(t));
            closeAndReturn();
        }
    }

    public void close()
    {

    }

    protected void closeAndReturn()
    {
        if (returnClass == null)
        {
            UIManager.getInstance().openMasterOptions();
        }
        else
        {
            UIManager.getInstance().open(returnClass);
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                closeAndReturn();
                break;
            }
        }
    }
}
