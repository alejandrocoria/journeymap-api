/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;


import journeymap.client.api.impl.ClientAPI;
import journeymap.client.cartography.RGB;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.UIManager;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The type Jm ui.
 */
public abstract class JmUI extends GuiScreen
{

    /**
     * The Title.
     */
    protected final String title;
    /**
     * The Header height.
     */
    protected final int headerHeight = 35;
    /**
     * The Logger.
     */
    protected final Logger logger = Journeymap.getLogger();
    /**
     * The Return display.
     */
    protected GuiScreen returnDisplay;
    /**
     * The Scale factor.
     */
    protected int scaleFactor = 1;
    /**
     * The Logo.
     */
    protected TextureImpl logo = TextureCache.getTexture(TextureCache.Logo);

    /**
     * Instantiates a new Jm ui.
     *
     * @param title the title
     */
    public JmUI(String title)
    {
        this(title, null);
    }

    /**
     * Instantiates a new Jm ui.
     *
     * @param title         the title
     * @param returnDisplay the return display
     */
    public JmUI(String title, GuiScreen returnDisplay)
    {
        super();
        this.title = title;
        this.returnDisplay = returnDisplay;
        if (this.returnDisplay != null && this.returnDisplay instanceof JmUI)
        {
            // Prevent users from getting into a stupid chain
            // Reallly should use a stack and prevent dups, but whatever.
            JmUI jmReturnDisplay = ((JmUI) this.returnDisplay);
            if (jmReturnDisplay.returnDisplay instanceof JmUI)
            {
                jmReturnDisplay.returnDisplay = null;
            }
        }
    }


    /**
     * Gets minecraft.
     *
     * @return the minecraft
     */
    public Minecraft getMinecraft()
    {
        return this.mc;
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height)
    {
        super.setWorldAndResolution(minecraft, width, height);
        this.scaleFactor = new ScaledResolution(minecraft).getScaleFactor();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    /**
     * Gets font renderer.
     *
     * @return the font renderer
     */
    public FontRenderer getFontRenderer()
    {
        return this.fontRendererObj;
    }

    /**
     * Size display.
     *
     * @param scaled the scaled
     */
    public void sizeDisplay(boolean scaled)
    {
        final int glwidth = scaled ? this.width : mc.displayWidth;
        final int glheight = scaled ? this.height : mc.displayHeight;
        DrawUtil.sizeDisplay(glwidth, glheight);
    }

    /**
     * Is mouse over button boolean.
     *
     * @param mouseX the mouse x
     * @param mouseY the mouse y
     * @return the boolean
     */
    protected boolean isMouseOverButton(int mouseX, int mouseY)
    {
        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            if (guibutton instanceof journeymap.client.ui.component.Button)
            {
                journeymap.client.ui.component.Button button = (journeymap.client.ui.component.Button) guibutton;
                if (button.mouseOver(mouseX, mouseY))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseEvent)
    {
        // 1.7
        // super.mouseMovedOrUp(mouseX, mouseY, mouseEvent);

        // 1.8
        super.mouseReleased(mouseX, mouseY, mouseEvent);
    }

    //    protected boolean mouseOverButtons(int x, int y)
//    {
//        for (int k = 0; k < this.buttonList.size(); ++k)
//        {
//            GuiButton guibutton = (GuiButton) this.buttonList.getTexture(k);
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

    /**
     * Draw logo.
     */
    protected void drawLogo()
    {
        if (logo.isDefunct())
        {
            logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, 8, 8, false, 1, 0);
        DrawUtil.sizeDisplay(width, height);
    }

    /**
     * Draw title.
     */
    protected void drawTitle()
    {
        DrawUtil.drawRectangle(0, 0, this.width, headerHeight, RGB.BLACK_RGB, .4f);
        DrawUtil.drawLabel(this.title, this.width / 2, headerHeight / 2, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle,
                RGB.BLACK_RGB, 0, Color.CYAN.getRGB(), 1f, 1, true, 0);

        // Show API version
        String apiVersion = "API v" + ClientAPI.API_VERSION;
        DrawUtil.drawLabel(apiVersion, this.width - 10, headerHeight / 2, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle,
                RGB.BLACK_RGB, 0, 0xcccccc, 1f, .5f, true, 0);
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
    }

    @Override
    public void drawBackground(int tint)
    {
        if (this.mc.theWorld == null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else
        {
            drawDefaultBackground();
        }
    }

    /**
     * Layout buttons.
     */
    protected abstract void layoutButtons();

    /**
     * Gets button list.
     *
     * @return the button list
     */
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
                    if (guibutton instanceof journeymap.client.ui.component.Button)
                    {
                        journeymap.client.ui.component.Button button = (journeymap.client.ui.component.Button) guibutton;
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
            Journeymap.getLogger().error("Error in UI: " + LogFormatter.toString(t));
            closeAndReturn();
        }
    }

    public void drawGradientRect(int p_73733_1_, int p_73733_2_, int p_73733_3_, int p_73733_4_, int p_73733_5_, int p_73733_6_)
    {
        super.drawGradientRect(p_73733_1_, p_73733_2_, p_73733_3_, p_73733_4_, p_73733_5_, p_73733_6_);
    }

    /**
     * Close.
     */
    public void close()
    {

    }

    /**
     * Close and return.
     */
    protected void closeAndReturn()
    {
        if (returnDisplay == null)
        {
            if (mc.theWorld != null)
            {
                UIManager.INSTANCE.openFullscreenMap();
            }
            else
            {
                UIManager.INSTANCE.closeAll();
            }
        }
        else
        {
            if (returnDisplay instanceof JmUI)
            {
                ((JmUI) returnDisplay).returnDisplay = null;
            }
            UIManager.INSTANCE.open(returnDisplay);
        }
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException
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

    /**
     * Draw hovering text.
     *
     * @param tooltip the tooltip
     * @param mouseX  the mouse x
     * @param mouseY  the mouse y
     */
    public void drawHoveringText(String[] tooltip, int mouseX, int mouseY)
    {
        drawHoveringText(Arrays.asList(tooltip), mouseX, mouseY, getFontRenderer());
    }

    /**
     * Gets return display.
     *
     * @return the return display
     */
    public GuiScreen getReturnDisplay()
    {
        return returnDisplay;
    }

    @Override
    public void drawHoveringText(java.util.List tooltip, int mouseX, int mouseY, FontRenderer fontRenderer)
    {
        // Had to override here because GuiScreen doesn't right-justify bidi text, nor does it calculate mixed string widths correctly
        if (!tooltip.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
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
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }
}
