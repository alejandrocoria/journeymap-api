/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
 * Theme values for rendering the fullscreen map toolbar.
 */
public class ThemeToolbar extends Button
{
    private final ButtonList buttonList;
    private Theme theme;
    private Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
    private TextureImpl textureBegin;
    private TextureImpl textureInner;
    private TextureImpl textureEnd;

    public ThemeToolbar(Theme theme, Button... buttons)
    {
        this(theme, new ButtonList(buttons));
    }

    public ThemeToolbar(Theme theme, ButtonList buttonList)
    {
        super(buttonList.getWidth(), buttonList.getHeight(), "");
        this.buttonList = buttonList;
        //setToggled(false, false);
        updateTheme(theme);
    }

    public void updateTheme(Theme theme)
    {
        this.theme = theme;
        updateTextures();
    }

    public Theme.Container.Toolbar.ToolbarSpec updateTextures()
    {
        Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
        if (buttonList.isHorizontal())
        {
            toolbarSpec = theme.container.toolbar.horizontal;
        }
        else
        {
            toolbarSpec = theme.container.toolbar.vertical;
        }

        setWidth(buttonList.getWidth());
        setHeight(buttonList.getHeight());

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

    public void updateLayout()
    {
        updateTextures();

        int drawX, drawY;

        drawX = buttonList.getLeftX() - 1;
        drawY = buttonList.getTopY() - 1;

        this.setPosition(drawX, drawY);
    }

    public Theme.Container.Toolbar.ToolbarSpec getToolbarSpec()
    {
        return toolbarSpec;
    }

    private ButtonList getButtonList()
    {
        return buttonList;
    }

    public boolean contains(GuiButton button)
    {
        return buttonList.contains(button);
    }

    public <B extends Button> void add(B... buttons)
    {
        buttonList.addAll(Arrays.asList(buttons));
    }

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

    public void setDrawToolbar(boolean draw)
    {
        super.setDrawButton(draw);
        for (Button button : buttonList)
        {
            button.setDrawButton(draw);
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float f)
    {
        if (!visible)
        {
            return;
        }

        double drawX = getX();
        double drawY = getY();

        if (!toolbarSpec.useThemeImages)
        {
            return;
        }

        // Draw self
        if (visible)
        {
            DrawUtil.drawQuad(
                    textureBegin,
                    this.toolbarSpec.begin.getColor(),
                    this.toolbarSpec.begin.alpha,
                    drawX,
                    drawY,
                    buttonList.getWidth() + 1,
                    buttonList.getHeight() + 1,
                    false,
                    0);
        }
    }

    public int getCenterX()
    {
        return this.x + (this.width / 2);
    }

    public int getMiddleY()
    {
        return this.y + (this.height / 2);
    }

    public int getBottomY()
    {
        return this.y + this.height;
    }

    public int getRightX()
    {
        return this.x + this.width;
    }

    @Override
    public ArrayList<String> getTooltip()
    {
        return null;
    }

    public void equalizeWidths(FontRenderer fr)
    {
        buttonList.equalizeWidths(fr);
    }

    public void equalizeWidths(FontRenderer fr, int hgap, int maxTotalWidth)
    {
        buttonList.equalizeWidths(fr, hgap, maxTotalWidth);
    }

    public ButtonList layoutHorizontal(int startX, final int y, boolean leftToRight, int hgap)
    {
        return layoutHorizontal(startX, y, leftToRight, hgap, false);
    }

    public ButtonList layoutHorizontal(int startX, final int y, boolean leftToRight, int hgap, boolean alignCenter)
    {
        buttonList.layoutHorizontal(startX, y, leftToRight, hgap, alignCenter);
        updateLayout();
        return buttonList;
    }

    public ButtonList layoutCenteredVertical(final int x, final int centerY, final boolean leftToRight, final int vgap)
    {
        buttonList.layoutCenteredVertical(x, centerY, leftToRight, vgap);
        updateLayout();
        return buttonList;
    }

    public ButtonList layoutVertical(final int x, int startY, boolean leftToRight, int vgap)
    {
        buttonList.layoutVertical(x, startY, leftToRight, vgap);
        updateLayout();
        return buttonList;
    }

    public ButtonList layoutCenteredHorizontal(final int centerX, final int y, final boolean leftToRight, final int hgap)
    {
        buttonList.layoutCenteredHorizontal(centerX, y, leftToRight, hgap);
        updateLayout();
        return buttonList;
    }

    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight)
    {
        buttonList.layoutDistributedHorizontal(leftX, y, rightX, leftToRight);
        updateLayout();
        return buttonList;
    }

    public ButtonList layoutFilledHorizontal(FontRenderer fr, final int leftX, final int y, final int rightX, final int hgap, final boolean leftToRight)
    {
        buttonList.layoutFilledHorizontal(fr, leftX, y, rightX, hgap, leftToRight);
        updateLayout();
        return buttonList;
    }

    public void setLayout(ButtonList.Layout layout, ButtonList.Direction direction)
    {
        buttonList.setLayout(layout, direction);
        updateLayout();
    }

    public ButtonList reverse()
    {
        buttonList.reverse();
        updateLayout();
        return buttonList;
    }

    public void addAllButtons(JmUI gui)
    {
        gui.getButtonList().add(this);
        gui.getButtonList().addAll(this.buttonList);
    }
}
