/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author techbrew 8/30/2014.
 */
public class ThemeToolbar extends Button
{
    private final ButtonList buttonList;
    private Theme theme;
    private Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
    private TextureImpl textureBegin;
    private TextureImpl textureInner;
    private TextureImpl textureEnd;

    /**
     * Instantiates a new Theme toolbar.
     *
     * @param theme   the theme
     * @param buttons the buttons
     */
    public ThemeToolbar(Theme theme, Button... buttons)
    {
        this(theme, new ButtonList(buttons));
    }

    /**
     * Instantiates a new Theme toolbar.
     *
     * @param theme      the theme
     * @param buttonList the button list
     */
    public ThemeToolbar(Theme theme, ButtonList buttonList)
    {
        super(0, 0, "");
        this.buttonList = buttonList;
        //setToggled(false, false);
        updateTheme(theme);
    }

    /**
     * Update theme.
     *
     * @param theme the theme
     */
    public void updateTheme(Theme theme)
    {
        this.theme = theme;
        updateTextures();
    }

    /**
     * Update textures theme . container . toolbar . toolbar spec.
     *
     * @return the theme . container . toolbar . toolbar spec
     */
    public Theme.Container.Toolbar.ToolbarSpec updateTextures()
    {
        Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
        if (buttonList.isHorizontal())
        {
            toolbarSpec = theme.container.toolbar.horizontal;
            setWidth(toolbarSpec.begin.width + (toolbarSpec.inner.width * buttonList.getVisibleButtonCount()) + toolbarSpec.end.width);
            setHeight(toolbarSpec.inner.height);
        }
        else
        {
            toolbarSpec = theme.container.toolbar.vertical;
            setWidth(toolbarSpec.inner.width);
            setHeight(toolbarSpec.begin.height + (toolbarSpec.inner.height * buttonList.getVisibleButtonCount()) + toolbarSpec.end.height);
        }

        if (this.toolbarSpec == null || toolbarSpec != this.toolbarSpec)
        {
            this.toolbarSpec = toolbarSpec;

            if (toolbarSpec.useThemeImages)
            {
                String pathPattern = "container/" + toolbarSpec.prefix + "toolbar_%s.png";
                textureBegin = TextureCache.getThemeTexture(theme, String.format(pathPattern, "begin"));
                textureInner = TextureCache.getThemeTexture(theme, String.format(pathPattern, "inner"));
                textureEnd = TextureCache.getThemeTexture(theme, String.format(pathPattern, "end"));
            }
        }

        return this.toolbarSpec;
    }

    /**
     * Update layout.
     */
    public void updateLayout()
    {
        updateTextures();

        boolean isHorizontal = buttonList.isHorizontal();

        int drawX, drawY;
        if (isHorizontal)
        {
            drawX = buttonList.getLeftX() - ((width - buttonList.getWidth(toolbarSpec.padding)) / 2);
            drawY = buttonList.getTopY() - ((height - theme.control.button.height) / 2);
        }
        else
        {
            drawX = buttonList.getLeftX() - ((toolbarSpec.inner.width - theme.control.button.width) / 2);
            drawY = buttonList.getTopY() - ((height - buttonList.getHeight(toolbarSpec.padding)) / 2);
        }

        this.setPosition(drawX, drawY);
    }

    /**
     * Gets toolbar spec.
     *
     * @return the toolbar spec
     */
    public Theme.Container.Toolbar.ToolbarSpec getToolbarSpec()
    {
        return toolbarSpec;
    }

    private ButtonList getButtonList()
    {
        return buttonList;
    }

    /**
     * Contains boolean.
     *
     * @param button the button
     * @return the boolean
     */
    public boolean contains(GuiButton button)
    {
        return buttonList.contains(button);
    }

    /**
     * Add.
     *
     * @param <B>     the type parameter
     * @param buttons the buttons
     */
    public <B extends Button> void add(B... buttons)
    {
        buttonList.addAll(Arrays.asList(buttons));
    }

    /**
     * Gets v margin.
     *
     * @return the v margin
     */
    public int getVMargin()
    {
        if (buttonList.isHorizontal())
        {
            int heightDiff = (toolbarSpec.inner.height - theme.control.button.height) / 2;
            return heightDiff + toolbarSpec.margin;
        }
        else
        {
            return toolbarSpec.margin;
        }
    }

    /**
     * Gets h margin.
     *
     * @return the h margin
     */
    public int getHMargin()
    {
        if (buttonList.isHorizontal())
        {
            return toolbarSpec.begin.width + toolbarSpec.margin;
        }
        else
        {
            int widthDiff = (toolbarSpec.inner.width - theme.control.button.width) / 2;
            return widthDiff + toolbarSpec.margin;
        }
    }

    /**
     * Sets draw toolbar.
     *
     * @param draw the draw
     */
    public void setDrawToolbar(boolean draw)
    {
        super.setDrawButton(draw);
        for (Button button : buttonList)
        {
            button.setDrawButton(draw);
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        if (!visible)
        {
            return;
        }

        boolean isHorizontal = buttonList.isHorizontal();

        double drawX = getX();
        double drawY = getY();

        if (!toolbarSpec.useThemeImages)
        {
            return;
        }

        // Draw self
        if (visible)
        {
            float scale = 1f;

            // Draw Begin
            if (toolbarSpec.begin.width != textureBegin.getWidth())
            {
                scale = (1f * toolbarSpec.begin.width / textureBegin.getWidth());
            }
            DrawUtil.drawClampedImage(textureBegin, drawX, drawY, scale, 0);

            if (isHorizontal)
            {
                drawX += (toolbarSpec.begin.width);
            }
            else
            {
                drawY += (toolbarSpec.begin.height);
            }

            // Draw Inner
            scale = 1f;
            if (toolbarSpec.inner.width != textureInner.getWidth())
            {
                scale = (1f * toolbarSpec.inner.width / textureInner.getWidth());
            }
            for (Button button : buttonList)
            {
                if (button.isDrawButton())
                {
                    DrawUtil.drawClampedImage(textureInner, drawX, drawY, scale, 0);
                    if (isHorizontal)
                    {
                        drawX += toolbarSpec.inner.width;
                    }
                    else
                    {
                        drawY += toolbarSpec.inner.height;
                    }
                }
            }

            // Draw End
            scale = 1f;
            if (toolbarSpec.end.width != textureEnd.getWidth())
            {
                scale = (1f * toolbarSpec.end.width / textureEnd.getWidth());
            }
            DrawUtil.drawClampedImage(textureEnd, drawX, drawY, scale, 0);
        }
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

    @Override
    public ArrayList<String> getTooltip()
    {
        return null;
    }

    /**
     * Layout horizontal button list.
     *
     * @param startX      the start x
     * @param y           the y
     * @param leftToRight the left to right
     * @param hgap        the hgap
     * @return the button list
     */
    public ButtonList layoutHorizontal(int startX, final int y, boolean leftToRight, int hgap)
    {
        buttonList.layoutHorizontal(startX, y, leftToRight, hgap);
        updateLayout();
        return buttonList;
    }

    /**
     * Layout centered vertical button list.
     *
     * @param x           the x
     * @param centerY     the center y
     * @param leftToRight the left to right
     * @param vgap        the vgap
     * @return the button list
     */
    public ButtonList layoutCenteredVertical(final int x, final int centerY, final boolean leftToRight, final int vgap)
    {
        buttonList.layoutCenteredVertical(x, centerY, leftToRight, vgap);
        updateLayout();
        return buttonList;
    }

    /**
     * Layout vertical button list.
     *
     * @param x           the x
     * @param startY      the start y
     * @param leftToRight the left to right
     * @param vgap        the vgap
     * @return the button list
     */
    public ButtonList layoutVertical(final int x, int startY, boolean leftToRight, int vgap)
    {
        buttonList.layoutVertical(x, startY, leftToRight, vgap);
        updateLayout();
        return buttonList;
    }

    /**
     * Layout centered horizontal button list.
     *
     * @param centerX     the center x
     * @param y           the y
     * @param leftToRight the left to right
     * @param hgap        the hgap
     * @return the button list
     */
    public ButtonList layoutCenteredHorizontal(final int centerX, final int y, final boolean leftToRight, final int hgap)
    {
        buttonList.layoutCenteredHorizontal(centerX, y, leftToRight, hgap);
        updateLayout();
        return buttonList;
    }

    /**
     * Layout distributed horizontal button list.
     *
     * @param leftX       the left x
     * @param y           the y
     * @param rightX      the right x
     * @param leftToRight the left to right
     * @return the button list
     */
    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight)
    {
        buttonList.layoutDistributedHorizontal(leftX, y, rightX, leftToRight);
        updateLayout();
        return buttonList;
    }

    /**
     * Layout filled horizontal button list.
     *
     * @param fr          the fr
     * @param leftX       the left x
     * @param y           the y
     * @param rightX      the right x
     * @param hgap        the hgap
     * @param leftToRight the left to right
     * @return the button list
     */
    public ButtonList layoutFilledHorizontal(FontRenderer fr, final int leftX, final int y, final int rightX, final int hgap, final boolean leftToRight)
    {
        buttonList.layoutFilledHorizontal(fr, leftX, y, rightX, hgap, leftToRight);
        updateLayout();
        return buttonList;
    }

    /**
     * Sets layout.
     *
     * @param layout    the layout
     * @param direction the direction
     */
    public void setLayout(ButtonList.Layout layout, ButtonList.Direction direction)
    {
        buttonList.setLayout(layout, direction);
        updateLayout();
    }

    /**
     * Reverse button list.
     *
     * @return the button list
     */
    public ButtonList reverse()
    {
        buttonList.reverse();
        updateLayout();
        return buttonList;
    }

    /**
     * Add all buttons.
     *
     * @param gui the gui
     */
    public void addAllButtons(JmUI gui)
    {
        gui.getButtonList().add(this);
        gui.getButtonList().addAll(this.buttonList);
    }
}
