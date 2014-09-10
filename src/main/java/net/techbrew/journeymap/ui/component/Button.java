/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.draw.DrawUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

/**
 * A glom of extra functionality to try to make buttons less sucky to use.
 */
public class Button extends GuiButton implements ScrollPane.Scrollable
{
    protected static Color smallFrameColorLight = new Color(160, 160, 160);
    protected static Color smallFrameColorDark = new Color(120, 120, 120);
    protected static Color smallBgColor = new Color(100, 100, 100);
    protected static Color smallBgHoverColor = new Color(125, 135, 190);
    protected Boolean toggled = true;
    protected String labelOn;
    protected String labelOff;
    //protected boolean enabled;
    protected boolean drawFrame;
    protected boolean drawBackground;
    protected boolean showDisabledHoverText;
    protected boolean defaultStyle = true;
    protected ArrayList<ToggleListener> toggleListeners = new ArrayList<ToggleListener>(0);
    protected PropertyAdapter propertyAdapter;

    public Button(Enum enumValue, String label)
    {
        this(enumValue.ordinal(), 0, 0, label);
    }

    public Button(int id, String label)
    {
        this(id, 0, 0, label);
    }

    public Button(Enum enumValue, int width, int height, String label)
    {
        this(enumValue.ordinal(), width, height, label);
    }

    public Button(Enum enumValue, String labelOn, String labelOff, boolean toggled)
    {
        this(enumValue.ordinal(), 0, 0, labelOn, labelOff, toggled);
    }

    public Button(int id, String labelOn, String labelOff, boolean toggled)
    {
        this(id, 0, 0, labelOn, labelOff, toggled);
    }

    public Button(int id, int width, int height, String label)
    {
        super(id, 0, 0, width, height, label);
        finishInit();
    }

    public Button(int id, int width, int height, String labelOn, String labelOff, boolean toggled)
    {
        super(id, 0, 0, width, height, toggled ? labelOn : labelOff);
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        this.setToggled(toggled);
        finishInit();
    }

    private void finishInit()
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

    private void updateLabel()
    {
        if (labelOn != null && labelOff != null)
        {
            super.displayString = getToggled() ? labelOn : labelOff;
        }
    }

    public void setLabels(String labelOn, String labelOff)
    {
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        updateLabel();
    }

    public void toggle()
    {
        setToggled(!getToggled());
    }

    public void addToggleListener(ToggleListener toggleListener)
    {
        this.toggleListeners.add(toggleListener);
    }

    public void setPropertyAdapter(PropertyAdapter propertyAdapter, String rawLabel)
    {
        this.propertyAdapter = propertyAdapter;
        propertyAdapter.setButton(this, rawLabel);
    }

    public int getFitWidth(FontRenderer fr)
    {
        int max = fr.getStringWidth(displayString);
        if (this.labelOn != null)
        {
            max = Math.max(max, fr.getStringWidth(labelOn));
        }
        if (this.labelOff != null)
        {
            max = Math.max(max, fr.getStringWidth(labelOff));
        }
        return max + 12;
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

    @Override
    public void func_146113_a(SoundHandler soundHandler)
    {
        // Play button click
        if(isEnabled())
        {
            super.func_146113_a(soundHandler);
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
            if(!enabled)
            {
                enabled = false;
            }
            // Use resource pack texture
            super.drawButton(minecraft, mouseX, mouseY);
        }
        else
        {
            // Use small button colors
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            if (isDrawFrame())
            {
                DrawUtil.drawRectangle(xPosition, yPosition, width, 1, smallFrameColorLight, 255); // Top
                DrawUtil.drawRectangle(xPosition, yPosition, 1, height, smallFrameColorLight, 255); // Left

                DrawUtil.drawRectangle(xPosition, yPosition + height - 1, width - 1, 1, smallFrameColorDark, 255); // Bottom
                DrawUtil.drawRectangle(xPosition + width - 1, yPosition + 1, 1, height - 1, smallFrameColorDark, 255); // Right
            }

            if (isDrawBackground())
            {
                int k = this.getHoverState(this.field_146123_n);
                DrawUtil.drawRectangle(xPosition + 1, yPosition + 1, width - 2, height - 2, k == 2 ? smallBgHoverColor : smallBgColor, 255);
            }

            this.mouseDragged(minecraft, mouseX, mouseY);
            int l = 14737632;

            if (!this.isEnabled())
            {
                l = -6250336;

                int alpha = 185;
                int widthOffset = width - ((this.height >= 20) ? 3 : 2);
                DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, widthOffset, height - 2, Color.darkGray, alpha);
            }
            else
            {
                if (this.field_146123_n)
                {
                    l = 16777120;
                }
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        }
    }

    public void drawUnderline()
    {
        if (isDrawButton())
        {
            DrawUtil.drawRectangle(xPosition, yPosition + height, width, 1, Button.smallFrameColorDark, 255);
        }
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        return isEnabled() && isDrawButton() && i >= getX() && j >= getY() && i < getX() + getWidth() && j < getY() + getHeight();
    }

    public ArrayList<String> getTooltip()
    {
        ArrayList<String> list = new ArrayList<String>();
        if (!this.enabled && showDisabledHoverText)
        {
            list.add(EnumChatFormatting.ITALIC + Constants.getString("jm.common.disabled_feature"));
        }
        return list;
    }

    public boolean mouseOver(int mouseX, int mouseY)
    {
        if(!visible)
        {
            return false;
        }
        return mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX <= (this.xPosition + this.width)
                && mouseY <= (this.yPosition + this.height);
    }

    public Boolean getToggled()
    {
        return toggled;
    }

    public void setToggled(Boolean toggled)
    {
        setToggled(toggled, true);
    }

    public void setToggled(Boolean toggled, boolean notifyToggleListener)
    {
        if(this.toggled==toggled || !this.enabled || !this.visible)
        {
            return;
        }

        boolean allowChange = true;
        try
        {
            if (notifyToggleListener && !toggleListeners.isEmpty())
            {
                for (ToggleListener listener : toggleListeners)
                {
                    allowChange = listener.onToggle(this, toggled);
                    if (!allowChange)
                    {
                        break;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Error trying to toggle button '" + displayString + "': " + LogFormatter.toString(t));
            allowChange = false;
        }

        if(allowChange)
        {
            this.toggled = toggled;
            updateLabel();
        }
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
        this.setY(y + (height / 2));
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

    public static interface ToggleListener<T extends Button>
    {
        public boolean onToggle(T button, boolean toggled);
    }
}
