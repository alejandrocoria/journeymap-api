/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.client.Constants;
import journeymap.client.render.draw.DrawUtil;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A glom of extra functionality to try to make buttons less sucky to use.
 */
public class Button extends GuiButton implements ScrollPane.Scrollable
{
    /**
     * The custom frame color light.
     */
    protected Integer customFrameColorLight = new Color(160, 160, 160).getRGB();
    /**
     * The custom frame color dark.
     */
    protected Integer customFrameColorDark = new Color(120, 120, 120).getRGB();
    /**
     * The custom bg color.
     */
    protected Integer customBgColor = new Color(100, 100, 100).getRGB();
    /**
     * The custom bg hover color.
     */
    protected Integer customBgHoverColor = new Color(125, 135, 190).getRGB();
    /**
     * The custom bg hover color 2.
     */
    protected Integer customBgHoverColor2 = new Color(100, 100, 100).getRGB();

    /**
     * The Label color.
     */
    protected Integer labelColor;
    /**
     * The Hover label color.
     */
    protected Integer hoverLabelColor;
    /**
     * The Disabled label color.
     */
    protected Integer disabledLabelColor;

    /**
     * The Disabled bg color.
     */
    protected Integer disabledBgColor = Color.darkGray.getRGB();

    /**
     * The Draw frame.
     */
    protected boolean drawFrame;
    /**
     * The Draw background.
     */
    protected boolean drawBackground;
    /**
     * The Draw label shadow.
     */
    protected boolean drawLabelShadow = true;
    /**
     * The Show disabled hover text.
     */
    protected boolean showDisabledHoverText;
    /**
     * The Default style.
     */
    protected boolean defaultStyle = true;
    /**
     * The Width pad.
     */
    protected int WIDTH_PAD = 12;
    /**
     * The Tooltip.
     */
    protected String[] tooltip;

    /**
     * The Font renderer.
     */
    protected FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;

    protected Rectangle2D.Double bounds;

    protected ArrayList<Function<Button, Boolean>> clickListeners = new ArrayList<>(0);

    /**
     * Instantiates a new Button.
     *
     * @param label the label
     */
    public Button(String label)
    {
        this(0, 0, label);
        resetLabelColors();
    }

    /**
     * Instantiates a new Button.
     *
     * @param width  the width
     * @param height the height
     * @param label  the label
     */
    public Button(int width, int height, String label)
    {
        super(0, 0, 0, width, height, label);
        finishInit();
    }

    /**
     * Reset label colors.
     */
    public void resetLabelColors()
    {
        labelColor = new Color(14737632).getRGB();
        hoverLabelColor = new Color(16777120).getRGB();
        disabledLabelColor = Color.lightGray.getRGB();
    }

    /**
     * Finish init.
     */
    protected void finishInit()
    {
        this.setEnabled(true);
        this.setDrawButton(true);
        this.setDrawFrame(true);
        this.setDrawBackground(true);
        if (height == 0)
        {
            setHeight(20);
        }
        if (width == 0)
        {
            setWidth(200);
        }
        updateBounds();
    }

    /**
     * Update label.
     */
    protected void updateLabel()
    {
    }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public boolean isActive()
    {
        return isEnabled();
    }

    public int getFitWidth(FontRenderer fr)
    {
        int max = fr.getStringWidth(displayString);
        return max + WIDTH_PAD + (fr.getBidiFlag() ? ((int) Math.ceil(max * .25)) : 0);
    }

    /**
     * Fit width.
     *
     * @param fr the fr
     */
    public void fitWidth(FontRenderer fr)
    {
        setWidth(getFitWidth(fr));
    }

    public void drawPartialScrollable(Minecraft minecraft, int x, int y, int width, int height)
    {
        minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
        int k = 0;// this.getHoverState(this.field_82253_i);
        this.drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height);
        this.drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height);
    }

    /**
     * Show disabled on hover.
     *
     * @param show the show
     */
    public void showDisabledOnHover(boolean show)
    {
        showDisabledHoverText = show;
    }

    public boolean isMouseOver()
    {
        return super.isMouseOver();
    }

    /**
     * Sets mouse over.
     *
     * @param hover the hover
     */
    public void setMouseOver(boolean hover)
    {
        this.setHovered(hover);
    }

    @Override
    public void playPressSound(SoundHandler soundHandler)
    {
        if (isEnabled())
        {
            super.playPressSound(soundHandler);
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks)
    {
        if (!isVisible())
        {
            return;
        }

        if (defaultStyle)
        {
            super.drawButton(minecraft, mouseX, mouseY, partialTicks);
        }
        else
        {
            // Use custom button colors
            minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1, 1, 1, 1);

            this.setHovered(mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);
            int hoverState = this.getHoverState(this.isHovered());

            if (isDrawFrame())
            {
                DrawUtil.drawRectangle(x, y, width, 1, customFrameColorLight, 1f); // Top
                DrawUtil.drawRectangle(x, y, 1, height, customFrameColorLight, 1f); // Left

                DrawUtil.drawRectangle(x, y + height - 1, width - 1, 1, customFrameColorDark, 1f); // Bottom
                DrawUtil.drawRectangle(x + width - 1, y + 1, 1, height - 1, customFrameColorDark, 1f); // Right
            }

            if (isDrawBackground())
            {
                DrawUtil.drawRectangle(x + 1, y + 1, width - 2, height - 2, hoverState == 2 ? customBgHoverColor : customBgColor, 1f);
            }
            else if (this.isEnabled() && isHovered())
            {
                DrawUtil.drawRectangle(x + 1, y + 1, width - 2, height - 2, customBgHoverColor2, .5f);
            }

            this.mouseDragged(minecraft, mouseX, mouseY);
            Integer varLabelColor = labelColor;

            if (!this.isEnabled())
            {
                varLabelColor = disabledLabelColor;

                if (drawBackground)
                {
                    float alpha = .7f;
                    int widthOffset = width - ((this.height >= 20) ? 3 : 2);
                    DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, widthOffset, height - 2, disabledBgColor, alpha);
                }
            }
            else
            {
                if (this.isHovered())
                {
                    varLabelColor = hoverLabelColor;
                }
                else if (labelColor != null)
                {
                    varLabelColor = labelColor;
                }
                else if (packedFGColour != 0)
                {
                    varLabelColor = packedFGColour;
                }
            }

            DrawUtil.drawCenteredLabel(this.displayString, this.getCenterX(), this.getMiddleY(), null, 0, varLabelColor, 1f, 1, drawLabelShadow);
        }
    }

    /**
     * Renders the specified text to the screen, center-aligned.
     */
    public void drawCenteredString(FontRenderer fontRenderer, String text, float x, float y, int color)
    {
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
    }

    /**
     * Draw underline.
     */
    public void drawUnderline()
    {
        if (isVisible())
        {
            DrawUtil.drawRectangle(x, y + height, width, 1, customFrameColorDark, 1f);
        }
    }

    /**
     * Secondary draw call which can be overridden for use in toolbars, etc.
     */
    public void secondaryDrawButton()
    {
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
    {
        return mousePressed(minecraft, mouseX, mouseY, true);
    }

    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY, boolean checkClickListeners)
    {
        boolean clicked = isEnabled() && isVisible() && mouseOver(mouseX, mouseY);
        return clicked && checkClickListeners();
    }

    public boolean checkClickListeners()
    {
        boolean clicked = true;
        if (!clickListeners.isEmpty())
        {
            try
            {
                for (Function<Button, Boolean> listener : clickListeners)
                {
                    if (!listener.apply(this))
                    {
                        break;
                    }
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Error trying to toggle button '" + displayString + "': " + LogFormatter.toString(t));
                clicked = false;
            }
        }
        return clicked;
    }

    /**
     * Gets unformatted tooltip.
     *
     * @return the unformatted tooltip
     */
    public String getUnformattedTooltip()
    {
        if (tooltip != null && tooltip.length > 0)
        {
            return tooltip[0];
        }
        return null;
    }

    /**
     * Gets tooltip.
     *
     * @return the tooltip
     */
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

        if (!this.isEnabled() && showDisabledHoverText)
        {
            list.add(TextFormatting.ITALIC + Constants.getString("jm.common.disabled_feature"));
        }
        return list;
    }

    /**
     * Sets tooltip.
     *
     * @param tooltip the tooltip
     */
    public void setTooltip(String... tooltip)
    {
        this.tooltip = tooltip;
    }

    /**
     * Mouse over boolean.
     *
     * @param mouseX the mouse x
     * @param mouseY the mouse y
     * @return the boolean
     */
    public boolean mouseOver(int mouseX, int mouseY)
    {
        return isVisible() && getBounds().contains(mouseX, mouseY);
    }

    protected Rectangle2D.Double updateBounds()
    {
        this.bounds = new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
        return this.bounds;
    }

    public Rectangle2D.Double getBounds()
    {
        if (this.bounds == null)
        {
            return updateBounds();
        }
        return bounds;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        if (this.width != width)
        {
            this.width = width;
            bounds = null;
        }
    }

    @Override
    public void setScrollableWidth(int width)
    {
        setWidth(width);
    }

    public int getHeight()
    {
        return height;
    }

    /**
     * Sets height.
     *
     * @param height the height
     */
    public void setHeight(int height)
    {
        if (this.height != height)
        {
            this.height = height;
            bounds = null;
            if (height != 20)
            {
                defaultStyle = false;
            }
        }
    }

    /**
     * Sets text only.
     *
     * @param fr the fr
     */
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
        drawButton(mc, mouseX, mouseY, 0f);
    }

    @Override
    public void clickScrollable(Minecraft mc, int mouseX, int mouseY)
    {
        // Do nothing - needs to be handled with Gui actionPerformed
    }

    public int getX()
    {
        return this.x;
    }

    /**
     * Sets x.
     *
     * @param x the x
     */
    public void setX(int x)
    {
        if (this.x != x)
        {
            this.x = x;
            bounds = null;
        }
    }

    public int getY()
    {
        return this.y;
    }

    /**
     * Sets y.
     *
     * @param y the y
     */
    public void setY(int y)
    {
        if (this.y != y)
        {
            this.y = y;
            bounds = null;
        }
    }


    /**
     * Gets center x.
     *
     * @return the center x
     */
    public int getCenterX()
    {
        return this.x + (this.width / 2);
    }

    /**
     * Gets middle y.
     *
     * @return the middle y
     */
    public int getMiddleY()
    {
        return this.y + (this.height / 2);
    }

    /**
     * Gets bottom y.
     *
     * @return the bottom y
     */
    public int getBottomY()
    {
        return this.y + this.height;
    }

    /**
     * Gets right x.
     *
     * @return the right x
     */
    public int getRightX()
    {
        return this.x + this.width;
    }

    public void setPosition(int x, int y)
    {
        setX(x);
        setY(y);
    }

    /**
     * Left of button.
     *
     * @param x the x
     * @return the button
     */
    public Button leftOf(int x)
    {
        this.setX(x - getWidth());
        return this;
    }

    /**
     * Right of button.
     *
     * @param x the x
     * @return the button
     */
    public Button rightOf(int x)
    {
        this.setX(x);
        return this;
    }

    /**
     * Center horizontal on button.
     *
     * @param x the x
     * @return the button
     */
    public Button centerHorizontalOn(int x)
    {
        this.setX(x - (width / 2));
        return this;
    }

    /**
     * Center vertical on button.
     *
     * @param y the y
     * @return the button
     */
    public Button centerVerticalOn(int y)
    {
        this.setY(y - (height / 2));
        return this;
    }

    /**
     * Left of button.
     *
     * @param other  the other
     * @param margin the margin
     * @return the button
     */
    public Button leftOf(Button other, int margin)
    {
        this.setX(other.getX() - getWidth() - margin);
        return this;
    }

    /**
     * Right of button.
     *
     * @param other  the other
     * @param margin the margin
     * @return the button
     */
    public Button rightOf(Button other, int margin)
    {
        this.setX(other.getX() + other.getWidth() + margin);
        return this;
    }

    /**
     * Above button.
     *
     * @param other  the other
     * @param margin the margin
     * @return the button
     */
    public Button above(Button other, int margin)
    {
        this.setY(other.getY() - this.getHeight() - margin);
        return this;
    }

    /**
     * Above button.
     *
     * @param y the y
     * @return the button
     */
    public Button above(int y)
    {
        this.setY(y - this.getHeight());
        return this;
    }

    /**
     * Below button.
     *
     * @param other  the other
     * @param margin the margin
     * @return the button
     */
    public Button below(Button other, int margin)
    {
        this.setY(other.getY() + other.getHeight() + margin);
        return this;
    }

    /**
     * Below button.
     *
     * @param list   the list
     * @param margin the margin
     * @return the button
     */
    public Button below(ButtonList list, int margin)
    {
        this.setY(list.getBottomY() + margin);
        return this;
    }

    /**
     * Below button.
     *
     * @param y the y
     * @return the button
     */
    public Button below(int y)
    {
        this.setY(y);
        return this;
    }

    /**
     * Align to button.
     *
     * @param other  the other
     * @param hAlign the h align
     * @param hgap   the hgap
     * @param vAlign the v align
     * @param vgap   the vgap
     * @return the button
     */
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

    /**
     * Is enabled boolean.
     *
     * @return the boolean
     */
    public boolean isEnabled()
    {
        return super.enabled;
    }

    /**
     * Sets enabled.
     *
     * @param enabled the enabled
     */
    public void setEnabled(boolean enabled)
    {
        super.enabled = enabled;
    }

    /**
     * Is draw button boolean.
     *
     * @return the boolean
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Sets visible.
     *
     * @param visible the enabled
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Sets draw button.
     *
     * @param drawButton the draw button
     */
    public void setDrawButton(boolean drawButton)
    {
        if (drawButton != visible)
        {
            this.visible = drawButton;
        }
    }

    /**
     * Is draw frame boolean.
     *
     * @return the boolean
     */
    public boolean isDrawFrame()
    {
        return drawFrame;
    }

    /**
     * Sets draw frame.
     *
     * @param drawFrame the draw frame
     */
    public void setDrawFrame(boolean drawFrame)
    {
        this.drawFrame = drawFrame;
    }

    /**
     * Is draw background boolean.
     *
     * @return the boolean
     */
    public boolean isDrawBackground()
    {
        return drawBackground;
    }

    /**
     * Sets draw background.
     *
     * @param drawBackground the draw background
     */
    public void setDrawBackground(boolean drawBackground)
    {
        this.drawBackground = drawBackground;
    }

    /**
     * Is default style boolean.
     *
     * @return the boolean
     */
    public boolean isDefaultStyle()
    {
        return defaultStyle;
    }

    /**
     * Sets default style.
     *
     * @param defaultStyle the default style
     */
    public void setDefaultStyle(boolean defaultStyle)
    {
        this.defaultStyle = defaultStyle;
    }

    /**
     * Key typed boolean.
     *
     * @param c the c
     * @param i the
     * @return the boolean
     */
    public boolean keyTyped(char c, int i)
    {
        return false;
    }

    /**
     * Sets background colors.
     *
     * @param customBgColor       the custom bg color
     * @param customBgHoverColor  the custom bg hover color
     * @param customBgHoverColor2 the custom bg hover color 2
     */
    public void setBackgroundColors(Integer customBgColor, Integer customBgHoverColor, Integer customBgHoverColor2)
    {
        this.customBgColor = customBgColor;
        this.customBgHoverColor = customBgHoverColor;
        this.customBgHoverColor2 = customBgHoverColor2;
    }

    /**
     * Sets draw label shadow.
     *
     * @param draw the draw
     */
    public void setDrawLabelShadow(boolean draw)
    {
        this.drawLabelShadow = draw;
    }

    /**
     * Sets label colors.
     *
     * @param labelColor         the label color
     * @param hoverLabelColor    the hover label color
     * @param disabledLabelColor the disabled label color
     */
    public void setLabelColors(Integer labelColor, Integer hoverLabelColor, Integer disabledLabelColor)
    {
        this.labelColor = labelColor;
        packedFGColour = labelColor;
        if (hoverLabelColor != null)
        {
            this.hoverLabelColor = hoverLabelColor;
        }
        if (disabledLabelColor != null)
        {
            this.disabledLabelColor = disabledLabelColor;
        }
    }

    /**
     * Refresh.
     */
    public void refresh()
    {
    }

    /**
     * Gets label color.
     *
     * @return the label color
     */
    public Integer getLabelColor()
    {
        return labelColor;
    }

    /**
     * Is hovered boolean.
     *
     * @return the boolean
     */
    public boolean isHovered()
    {
        return super.hovered;
    }

    /**
     * Sets hovered.
     *
     * @param hovered the hovered
     */
    public void setHovered(boolean hovered)
    {
        super.hovered = hovered;
    }

    /**
     * Add listener to be notified when button is used
     *
     * @param listener the toggle listener
     */
    public void addClickListener(Function<Button, Boolean> listener)
    {
        this.clickListeners.add(listener);
    }

}
