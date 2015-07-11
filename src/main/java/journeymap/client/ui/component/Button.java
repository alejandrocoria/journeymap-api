/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import journeymap.client.Constants;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.render.draw.DrawUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A glom of extra functionality to try to make buttons less sucky to use.
 */
public class Button extends GuiButton implements ScrollPane.Scrollable
{
    protected Color smallFrameColorLight = new Color(160, 160, 160);
    protected Color smallFrameColorDark = new Color(120, 120, 120);
    protected Color smallBgColor = new Color(100, 100, 100);
    protected Color smallBgHoverColor = new Color(125, 135, 190);
    protected Color smallBgHoverColor2 = new Color(100, 100, 100);

    protected Color labelColor;
    protected Color hoverLabelColor;
    protected Color disabledLabelColor;

    protected Color disabledBgColor = Color.darkGray;

    //protected boolean enabled;
    protected boolean drawFrame;
    protected boolean drawBackground;
    protected boolean drawLabelShadow = true;
    protected boolean showDisabledHoverText;
    protected boolean defaultStyle = true;
    protected int WIDTH_PAD = 12;
    protected String[] tooltip;

    FontRenderer fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();
    private boolean enabled;

    public Button(String label)
    {
        this(0, 0, label);
        resetLabelColors();
    }

    public Button(int width, int height, String label)
    {
        super(0, 0, 0, width, height, label);
        finishInit();
    }

    public void resetLabelColors()
    {
        labelColor = new Color(14737632);
        hoverLabelColor = new Color(16777120);
        disabledLabelColor = Color.lightGray;
    }

    protected void finishInit()
    {
        this.setEnabled(true);
        this.setDrawButton(true);
        this.setDrawFrame(true);
        this.setDrawBackground(true);
        if (height == 0)
        {
            height = 20;
        }
        if (width == 0)
        {
            width = 200;
        }
    }

    protected void updateLabel()
    {
    }

    public boolean isActive()
    {
        return enabled;
    }

    public int getFitWidth(FontRenderer fr)
    {
        int max = fr.getStringWidth(displayString);
        return max + WIDTH_PAD + (fr.getBidiFlag() ? ((int) Math.ceil(max * .25)) : 0);
    }

    public void fitWidth(FontRenderer fr)
    {
        this.width = getFitWidth(fr);
    }

    public void drawPartialScrollable(Minecraft minecraft, int x, int y, int width, int height)
    {
        minecraft.getTextureManager().bindTexture(buttonTextures);
        int k = 0;// this.getHoverState(this.field_82253_i);
        this.drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height);
        this.drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height);
    }

    public void showDisabledOnHover(boolean show)
    {
        showDisabledHoverText = show;
    }

    public boolean isMouseOver()
    {
        // 1.7
        //return field_146123_n;

        // 1.8
        return super.isMouseOver();
    }

    public void setMouseOver(boolean hover)
    {
        // 1.7.10
        // field_146123_n = hover;

        // 1.8
        this.hovered = hover;
    }

    @Override
    // 1.7
    //public void func_146113_a(SoundHandler soundHandler)
    // 1.8
    public void playPressSound(SoundHandler soundHandler)
    {
        // Play button click
        if (isEnabled())
        {
            // 1.7
            //super.func_146113_a(soundHandler);

            // 1.8
            super.playPressSound(soundHandler);
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        if (!isDrawButton())
        {
            return;
        }

        if (defaultStyle)
        {
            if (!enabled)
            {
                enabled = false;
            }
            // Use resource pack texture
            super.drawButton(minecraft, mouseX, mouseY);
        }
        else
        {
            // Use small button colors
            minecraft.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.enabled = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            if (isDrawFrame())
            {
                DrawUtil.drawRectangle(xPosition, yPosition, width, 1, smallFrameColorLight, 255); // Top
                DrawUtil.drawRectangle(xPosition, yPosition, 1, height, smallFrameColorLight, 255); // Left

                DrawUtil.drawRectangle(xPosition, yPosition + height - 1, width - 1, 1, smallFrameColorDark, 255); // Bottom
                DrawUtil.drawRectangle(xPosition + width - 1, yPosition + 1, 1, height - 1, smallFrameColorDark, 255); // Right
            }

            int k = this.getHoverState(this.enabled);

            if (isDrawBackground())
            {
                DrawUtil.drawRectangle(xPosition + 1, yPosition + 1, width - 2, height - 2, k == 2 ? smallBgHoverColor : smallBgColor, 255);
            }
            else if (this.enabled && enabled)
            {
                DrawUtil.drawRectangle(xPosition + 1, yPosition + 1, width - 2, height - 2, smallBgHoverColor2, 128);
            }

            this.mouseDragged(minecraft, mouseX, mouseY);
            Color varLabelColor = labelColor;

            if (!this.isEnabled())
            {
                varLabelColor = disabledLabelColor;

                if (drawBackground)
                {
                    int alpha = 185;
                    int widthOffset = width - ((this.height >= 20) ? 3 : 2);
                    DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, widthOffset, height - 2, disabledBgColor, alpha);
                }
            }
            else
            {
                if (this.enabled)
                {
                    varLabelColor = hoverLabelColor;
                }
                else if (labelColor != null)
                {
                    varLabelColor = labelColor;
                }
                else if (packedFGColour != 0)
                {
                    varLabelColor = new Color(packedFGColour);
                }
            }

            DrawUtil.drawCenteredLabel(this.displayString, this.getCenterX(), this.getMiddleY(), null, 0, varLabelColor, 255, 1, drawLabelShadow);
        }
    }

    /**
     * Renders the specified text to the screen, center-aligned.
     */
    public void drawCenteredString(FontRenderer p_73732_1_, String p_73732_2_, int p_73732_3_, int p_73732_4_, int p_73732_5_)
    {
        p_73732_1_.drawStringWithShadow(p_73732_2_, p_73732_3_ - p_73732_1_.getStringWidth(p_73732_2_) / 2, p_73732_4_, p_73732_5_);
    }

    public void drawUnderline()
    {
        if (isDrawButton())
        {
            DrawUtil.drawRectangle(xPosition, yPosition + height, width, 1, smallFrameColorDark, 255);
        }
    }

    /**
     * Secondary draw call which can be overridden for use in toolbars, etc.
     */
    public void secondaryDrawButton()
    {
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        boolean pressed = isEnabled() && isDrawButton() && i >= getX() && j >= getY() && i < getX() + getWidth() && j < getY() + getHeight();
        return pressed;
    }

    public String getUnformattedTooltip()
    {
        if (tooltip != null && tooltip.length > 0)
        {
            return tooltip[0];
        }
        return null;
    }

    public List<String> getTooltip()
    {
        ArrayList<String> list = new ArrayList<String>();
        if (tooltip != null)
        {
            for (String line : tooltip)
            {
                list.addAll(fontRenderer.listFormattedStringToWidth(line, 200));
            }
            return list;
        }

        if (!this.enabled && showDisabledHoverText)
        {
            list.add(EnumChatFormatting.ITALIC + Constants.getString("jm.common.disabled_feature"));
        }
        return list;
    }

    public void setTooltip(String... tooltip)
    {
        this.tooltip = tooltip;
    }

    public boolean mouseOver(int mouseX, int mouseY)
    {
        if (!visible)
        {
            return false;
        }
        return mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX <= (this.xPosition + this.width)
                && mouseY <= (this.yPosition + this.height);
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
        if (height != 20)
        {
            defaultStyle = false;
        }
    }

    public void setTextOnly(FontRenderer fr)
    {
        setHeight(fr.FONT_HEIGHT + 1);
        fitWidth(fr);
        setDrawBackground(false);
        setDrawFrame(false);
    }

    @Override
    public void drawScrollable(Minecraft mc, int mouseX, int mouseY)
    {
        drawButton(mc, mouseX, mouseY);
    }

    @Override
    public void clickScrollable(Minecraft mc, int mouseX, int mouseY)
    {
        // Do nothing - needs to be handled with Gui actionPerformed
    }

    public int getX()
    {
        return this.xPosition;
    }

    public void setX(int x)
    {
        this.xPosition = x;
    }

    public int getY()
    {
        return this.yPosition;
    }

    public void setY(int y)
    {
        this.yPosition = y;
    }

    public int getCenterX()
    {
        return this.xPosition + (this.width / 2);
    }

    public int getMiddleY()
    {
        return this.yPosition + (this.height / 2);
    }

    public int getBottomY()
    {
        return this.yPosition + this.height;
    }

    public int getRightX()
    {
        return this.xPosition + this.width;
    }

    public void setPosition(int x, int y)
    {
        setX(x);
        setY(y);
    }

    public Button leftOf(int x)
    {
        this.setX(x - getWidth());
        return this;
    }

    public Button rightOf(int x)
    {
        this.setX(x);
        return this;
    }

    public Button centerHorizontalOn(int x)
    {
        this.setX(x - (width / 2));
        return this;
    }

    public Button centerVerticalOn(int y)
    {
        this.setY(y - (height / 2));
        return this;
    }

    public Button leftOf(Button other, int margin)
    {
        this.setX(other.getX() - getWidth() - margin);
        return this;
    }

    public Button rightOf(Button other, int margin)
    {
        this.setX(other.getX() + other.getWidth() + margin);
        return this;
    }

    public Button above(Button other, int margin)
    {
        this.setY(other.getY() - this.getHeight() - margin);
        return this;
    }

    public Button above(int y)
    {
        this.setY(y - this.getHeight());
        return this;
    }

    public Button below(Button other, int margin)
    {
        this.setY(other.getY() + other.getHeight() + margin);
        return this;
    }

    public Button below(ButtonList list, int margin)
    {
        this.setY(list.getBottomY() + margin);
        return this;
    }

    public Button below(int y)
    {
        this.setY(y);
        return this;
    }

    public Button alignTo(Button other, DrawUtil.HAlign hAlign, int hgap, DrawUtil.VAlign vAlign, int vgap)
    {
        int x = getX();
        int y = getY();

        switch (hAlign)
        {
            case Right:
            {
                x = other.getRightX() + hgap;
                break;
            }
            case Left:
            {
                x = other.getX() - hgap;
                break;
            }
            case Center:
            {
                x = other.getCenterX();
                break;
            }
        }

        switch (vAlign)
        {
            case Above:
            {
                y = other.getY() - vgap - getHeight();
                break;
            }
            case Below:
            {
                y = other.getBottomY() + vgap;
                break;
            }
            case Middle:
            {
                y = other.getMiddleY() - (getHeight() / 2);
                break;
            }
        }

        setX(x);
        setY(y);
        return this;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isDrawButton()
    {
        return visible;
    }

    public void setDrawButton(boolean drawButton)
    {
        this.visible = drawButton;
    }

    public boolean isDrawFrame()
    {
        return drawFrame;
    }

    public void setDrawFrame(boolean drawFrame)
    {
        this.drawFrame = drawFrame;
    }

    public boolean isDrawBackground()
    {
        return drawBackground;
    }

    public void setDrawBackground(boolean drawBackground)
    {
        this.drawBackground = drawBackground;
    }

    public boolean isDefaultStyle()
    {
        return defaultStyle;
    }

    public void setDefaultStyle(boolean defaultStyle)
    {
        this.defaultStyle = defaultStyle;
    }

    public boolean keyTyped(char c, int i)
    {
        return false;
    }

    public void setBackgroundColors(Color smallBgColor, Color smallBgHoverColor, Color smallBgHoverColor2)
    {
        this.smallBgColor = smallBgColor;
        this.smallBgHoverColor = smallBgHoverColor;
        this.smallBgHoverColor2 = smallBgHoverColor2;
    }

    public void setDrawLabelShadow(boolean draw)
    {
        this.drawLabelShadow = draw;
    }

    public void setLabelColors(Color labelColor, Color hoverLabelColor, Color disabledLabelColor)
    {
        this.labelColor = labelColor;
        packedFGColour = labelColor.getRGB();
        if (hoverLabelColor != null)
        {
            this.hoverLabelColor = hoverLabelColor;
        }
        if (disabledLabelColor != null)
        {
            this.disabledLabelColor = disabledLabelColor;
        }
    }

    public void refresh()
    {
    }

    public Color getLabelColor()
    {
        return labelColor;
    }
}
