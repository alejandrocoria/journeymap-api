/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.component;


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
import net.techbrew.journeymap.ui.UIManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class JmUI extends GuiScreen
{

    protected final String title;
    protected final int headerHeight = 35;
    protected final Logger logger = JourneyMap.getLogger();
    protected final JmUI returnDisplay;
    protected int scaleFactor = 1;
    protected TextureImpl logo = TextureCache.instance().getLogo();

    public JmUI(String title)
    {
        this(title, null);
    }

    public JmUI(String title, JmUI returnDisplay)
    {
        super();
        this.title = title;
        this.returnDisplay = returnDisplay;
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
        DrawUtil.sizeDisplay(glwidth, glheight);
    }

    protected boolean isMouseOverButton(int mouseX, int mouseY)
    {
        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            if (guibutton instanceof net.techbrew.journeymap.ui.component.Button)
            {
                net.techbrew.journeymap.ui.component.Button button = (net.techbrew.journeymap.ui.component.Button) guibutton;
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
        DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, 8, 8, false, 1, 0);
        DrawUtil.sizeDisplay(width, height);
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

    public java.util.List getButtonList()
    {
        return buttonList;
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        try
        {
            drawBackground(0);
            layoutButtons();

            drawTitle();
            drawLogo();

            List<String> tooltip = null;
            for (int k = 0; k < this.buttonList.size(); ++k)
            {
                GuiButton guibutton = (GuiButton) this.buttonList.get(k);
                guibutton.drawButton(this.mc, x, y);
                if (tooltip == null)
                {
                    if (guibutton instanceof net.techbrew.journeymap.ui.component.Button)
                    {
                        net.techbrew.journeymap.ui.component.Button button = (net.techbrew.journeymap.ui.component.Button) guibutton;
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
                //drawHoveringText(tooltip, x, y, getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            }


        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Error in UI: " + LogFormatter.toString(t));
            closeAndReturn();
        }
    }

    public void drawGradientRect(int p_73733_1_, int p_73733_2_, int p_73733_3_, int p_73733_4_, int p_73733_5_, int p_73733_6_)
    {
        super.drawGradientRect(p_73733_1_, p_73733_2_, p_73733_3_, p_73733_4_, p_73733_5_, p_73733_6_);
    }

    public void close()
    {

    }

    protected void closeAndReturn()
    {
        if (returnDisplay == null)
        {
            UIManager.getInstance().openOptionsManager();
        }
        else
        {
            UIManager.getInstance().open(returnDisplay);
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

    public void drawHoveringText(String[] tooltip, int mouseX, int mouseY)
    {
        drawHoveringText(Arrays.asList(tooltip), mouseX, mouseY, getFontRenderer());
    }

    @Override
    protected void drawHoveringText(java.util.List tooltip, int mouseX, int mouseY, FontRenderer fontRenderer)
    {
        // Had to override here because GuiScreen doesn't right-justify bidi text, nor does it calculate mixed string widths correctly
        if (!tooltip.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int maxLineWidth = 0;
            Iterator iterator = tooltip.iterator();

            while (iterator.hasNext())
            {
                String line = (String) iterator.next();
                int lineWidth = fontRenderer.getStringWidth(line);
                if (fontRenderer.getBidiFlag())
                {
                    lineWidth = (int) Math.ceil(lineWidth * 1.25);
                }

                if (lineWidth > maxLineWidth)
                {
                    maxLineWidth = lineWidth;
                }
            }

            int drawX = mouseX + 12;
            int drawY = mouseY - 12;
            int boxHeight = 8;

            if (tooltip.size() > 1)
            {
                boxHeight += 2 + (tooltip.size() - 1) * 10;
            }

            if (drawX + maxLineWidth > this.width)
            {
                drawX -= 28 + maxLineWidth;
            }

            if (drawY + boxHeight + 6 > this.height)
            {
                drawY = this.height - boxHeight - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int j1 = -267386864;
            this.drawGradientRect(drawX - 3, drawY - 4, drawX + maxLineWidth + 3, drawY - 3, j1, j1);
            this.drawGradientRect(drawX - 3, drawY + boxHeight + 3, drawX + maxLineWidth + 3, drawY + boxHeight + 4, j1, j1);
            this.drawGradientRect(drawX - 3, drawY - 3, drawX + maxLineWidth + 3, drawY + boxHeight + 3, j1, j1);
            this.drawGradientRect(drawX - 4, drawY - 3, drawX - 3, drawY + boxHeight + 3, j1, j1);
            this.drawGradientRect(drawX + maxLineWidth + 3, drawY - 3, drawX + maxLineWidth + 4, drawY + boxHeight + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            this.drawGradientRect(drawX - 3, drawY - 3 + 1, drawX - 3 + 1, drawY + boxHeight + 3 - 1, k1, l1);
            this.drawGradientRect(drawX + maxLineWidth + 2, drawY - 3 + 1, drawX + maxLineWidth + 3, drawY + boxHeight + 3 - 1, k1, l1);
            this.drawGradientRect(drawX - 3, drawY - 3, drawX + maxLineWidth + 3, drawY - 3 + 1, k1, k1);
            this.drawGradientRect(drawX - 3, drawY + boxHeight + 2, drawX + maxLineWidth + 3, drawY + boxHeight + 3, l1, l1);

            for (int i2 = 0; i2 < tooltip.size(); ++i2)
            {
                String line = (String) tooltip.get(i2);
                if (fontRenderer.getBidiFlag())
                {
                    int lineWidth = (int) Math.ceil(fontRenderer.getStringWidth(line) * 1.1);
                    fontRenderer.drawStringWithShadow(line, (drawX + maxLineWidth) - lineWidth, drawY, -1);
                }
                else
                {
                    fontRenderer.drawStringWithShadow(line, drawX, drawY, -1);
                }

                if (i2 == 0)
                {
                    drawY += 2;
                }

                drawY += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }
}
