/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.component.BooleanPropertyButton;
import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * @author techbrew 8/30/2014.
 */
public class ThemeButton extends BooleanPropertyButton
{
    /**
     * The Theme.
     */
    protected Theme theme;
    /**
     * The Button spec.
     */
    protected Theme.Control.ButtonSpec buttonSpec;
    /**
     * The Texture on.
     */
    protected TextureImpl textureOn;
    /**
     * The Texture hover.
     */
    protected TextureImpl textureHover;
    /**
     * The Texture off.
     */
    protected TextureImpl textureOff;
    /**
     * The Texture disabled.
     */
    protected TextureImpl textureDisabled;
    /**
     * The Texture icon.
     */
    protected TextureImpl textureIcon;
    /**
     * The Icon on color.
     */
    protected Integer iconOnColor;
    /**
     * The Icon off color.
     */
    protected Integer iconOffColor;
    /**
     * The Icon hover color.
     */
    protected Integer iconHoverColor;
    /**
     * The Icon disabled color.
     */
    protected Integer iconDisabledColor;
    /**
     * The Icon name.
     */
    protected String iconName;
    /**
     * The Additional tooltips.
     */
    protected List<String> additionalTooltips;

    /**
     * Instantiates a new Theme button.
     *
     * @param theme    the theme
     * @param rawLabel the raw label
     * @param iconName the icon name
     */
    public ThemeButton(Theme theme, String rawLabel, String iconName)
    {
        this(theme, Constants.getString(rawLabel), Constants.getString(rawLabel), false, iconName);
    }

    /**
     * Instantiates a new Theme button.
     *
     * @param theme    the theme
     * @param labelOn  the label on
     * @param labelOff the label off
     * @param toggled  the toggled
     * @param iconName the icon name
     */
    public ThemeButton(Theme theme, String labelOn, String labelOff, boolean toggled, String iconName)
    {
        super(labelOn, labelOff, null);
        this.iconName = iconName;
        this.setToggled(toggled);
        updateTheme(theme);
    }

    /**
     * Instantiates a new Theme button.
     *
     * @param theme    the theme
     * @param labelOn  the label on
     * @param labelOff the label off
     * @param iconName the icon name
     * @param field    the field
     */
    protected ThemeButton(Theme theme, String labelOn, String labelOff, String iconName, BooleanField field)
    {
        super(labelOn, labelOff, field);
        this.iconName = iconName;
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
        this.buttonSpec = getButtonSpec(theme);

        if (buttonSpec.useThemeImages)
        {
            String pattern = getPathPattern();
            String prefix = buttonSpec.prefix;
            textureOn = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "on"));
            textureOff = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "off"));
            textureHover = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "hover"));
            textureDisabled = TextureCache.getThemeTexture(theme, String.format(pattern, prefix, "disabled"));
        }
        else
        {
            textureOn = null;
            textureOff = null;
            textureHover = null;
            textureDisabled = null;
        }

        iconOnColor = Theme.getColor(buttonSpec.iconOnColor);
        iconOffColor = Theme.getColor(buttonSpec.iconOffColor);
        iconHoverColor = Theme.getColor(buttonSpec.iconHoverColor);
        iconDisabledColor = Theme.getColor(buttonSpec.iconDisabledColor);

        textureIcon = TextureCache.getThemeTexture(theme, String.format("icon/%s.png", iconName));

        setWidth(buttonSpec.width);
        setHeight(buttonSpec.height);
        setToggled(false, false);
    }

    /**
     * Has valid textures boolean.
     *
     * @return the boolean
     */
    public boolean hasValidTextures()
    {
        if (buttonSpec.useThemeImages)
        {
            return GL11.glIsTexture(textureOn.getGlTextureId(false))
                    && GL11.glIsTexture(textureOff.getGlTextureId(false));
        }
        else
        {
            return true;
        }
    }

    /**
     * Gets path pattern.
     *
     * @return the path pattern
     */
    protected String getPathPattern()
    {
        return "control/%sbutton_%s.png";
    }

    /**
     * Gets button spec.
     *
     * @param theme the theme
     * @return the button spec
     */
    protected Theme.Control.ButtonSpec getButtonSpec(Theme theme)
    {
        return theme.control.button;
    }

    /**
     * Gets active texture.
     *
     * @param isMouseOver the is mouse over
     * @return the active texture
     */
    protected TextureImpl getActiveTexture(boolean isMouseOver)
    {
        if (isEnabled())
        {
            TextureImpl activeTexture = isMouseOver ? Mouse.isButtonDown(0) ? textureOn : textureHover : textureOff;
            return activeTexture;
        }
        else
        {
            return textureDisabled;
        }
    }

    /**
     * Gets icon color.
     *
     * @param isMouseOver the is mouse over
     * @return the icon color
     */
    protected Integer getIconColor(boolean isMouseOver)
    {
        if (!isEnabled())
        {
            return iconDisabledColor;
        }

        if (isMouseOver)
        {
            return iconHoverColor;
        }

        return toggled ? iconOnColor : iconOffColor;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        if (!isDrawButton())
        {
            return;
        }

        // Check hover
        boolean hover = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        setMouseOver(hover);

        // Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
        int hoverState = this.getHoverState(hover);
        boolean isMouseOver = (hoverState == 2);

        TextureImpl activeTexture = getActiveTexture(isMouseOver);

        int drawX = getX();
        int drawY = getY();

        if (buttonSpec.useThemeImages)
        {
            float buttonScale = 1f;
            if (buttonSpec.width != activeTexture.getWidth())
            {
                buttonScale = (1f * buttonSpec.width / activeTexture.getWidth());
            }

            // Theme Button Background
            DrawUtil.drawImage(activeTexture, drawX, drawY, false, buttonScale, 0);
        }
        else
        {
            // Use resourcepack textures
            drawNativeButton(minecraft, mouseX, mouseY);
        }

        // Icon
        float iconScale = 1f;
        if (theme.icon.width != textureIcon.getWidth())
        {
            iconScale = (1f * theme.icon.width / textureIcon.getWidth());
        }

        //drawX += (((width - textureIcon.width)/2));
        //drawY += (((height - textureIcon.height)/2));
        //DrawUtil.drawImage(textureIcon, drawX, drawY, false, scale, 0);

        if (!buttonSpec.useThemeImages)
        {
            DrawUtil.drawColoredImage(textureIcon, RGB.BLACK_RGB, 1f, drawX + .5, drawY + .5, iconScale, 0);
        }

        Integer iconColor = getIconColor(isMouseOver);
        DrawUtil.drawColoredImage(textureIcon, iconColor, 1f, drawX, drawY, iconScale, 0);
    }

    /**
     * Draw native button.
     *
     * @param minecraft the minecraft
     * @param mouseX    the mouse x
     * @param mouseY    the mouse y
     */
    public void drawNativeButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        int magic = 20;
        minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.getHoverState(isMouseOver());
        GlStateManager.enableBlend();
        //GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * magic, this.width / 2, this.height);
        this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * magic, this.width / 2, this.height);
        this.mouseDragged(minecraft, mouseX, mouseY);
        int l = 14737632;
    }

    /**
     * Sets additional tooltips.
     *
     * @param additionalTooltips the additional tooltips
     */
    public void setAdditionalTooltips(List<String> additionalTooltips)
    {
        this.additionalTooltips = additionalTooltips;
    }

    @Override
    public List<String> getTooltip()
    {
        if (!visible)
        {
            return null;
        }
        List<String> list = super.getTooltip();

        String style = null;
        if (!isEnabled())
        {
            style = buttonSpec.tooltipDisabledStyle;
        }
        else
        {
            style = toggled ? buttonSpec.tooltipOnStyle : buttonSpec.tooltipOffStyle;
        }

        list.add(0, style + displayString);

        if (additionalTooltips != null)
        {
            list.addAll(additionalTooltips);
        }
        return list;
    }
}
