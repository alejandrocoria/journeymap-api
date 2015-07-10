/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.ui.theme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.component.BooleanPropertyButton;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Mark on 8/30/2014.
 */
public class ThemeButton extends BooleanPropertyButton
{
    protected Theme theme;
    protected Theme.Control.ButtonSpec buttonSpec;
    protected TextureImpl textureOn;
    protected TextureImpl textureHover;
    protected TextureImpl textureOff;
    protected TextureImpl textureDisabled;
    protected TextureImpl textureIcon;
    protected Color iconOnColor;
    protected Color iconOffColor;
    protected Color iconHoverColor;
    protected Color iconDisabledColor;
    protected String iconName;
    protected List<String> additionalTooltips;

    public ThemeButton(Theme theme, String rawLabel, String iconName)
    {
        this(theme, Constants.getString(rawLabel), Constants.getString(rawLabel), false, iconName);
    }

    public ThemeButton(Theme theme, String labelOn, String labelOff, boolean toggled, String iconName)
    {
        super(labelOn, labelOff, null, null);
        this.iconName = iconName;
        this.setToggled(toggled);
        updateTheme(theme);
    }

    protected ThemeButton(Theme theme, String labelOn, String labelOff, String iconName, PropertiesBase properties, AtomicBoolean property)
    {
        super(labelOn, labelOff, properties, property);
        this.iconName = iconName;
        updateTheme(theme);
    }

    public void updateTheme(Theme theme)
    {
        this.theme = theme;
        this.buttonSpec = getButtonSpec(theme);
        TextureCache tc = TextureCache.instance();

        if (buttonSpec.useThemeImages)
        {
            String pattern = getPathPattern();
            String prefix = buttonSpec.prefix;
            textureOn = tc.getThemeTexture(theme, String.format(pattern, prefix, "on"));
            textureOff = tc.getThemeTexture(theme, String.format(pattern, prefix, "off"));
            textureHover = tc.getThemeTexture(theme, String.format(pattern, prefix, "hover"));
            textureDisabled = tc.getThemeTexture(theme, String.format(pattern, prefix, "disabled"));
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

        textureIcon = tc.getThemeTexture(theme, String.format("icon/%s.png", iconName));

        setWidth(buttonSpec.width);
        setHeight(buttonSpec.height);
        setToggled(false, false);
    }

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

    protected String getPathPattern()
    {
        return "control/%sbutton_%s.png";
    }

    protected Theme.Control.ButtonSpec getButtonSpec(Theme theme)
    {
        return theme.control.button;
    }

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

    protected Color getIconColor(boolean isMouseOver)
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
        this.hovered = hover; // 1.7.10 field_146123_n

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
            DrawUtil.drawColoredImage(textureIcon, 255, Color.black, drawX + .5, drawY + .5, iconScale, 0);
        }

        Color iconColor = getIconColor(isMouseOver);
        DrawUtil.drawColoredImage(textureIcon, 255, iconColor, drawX, drawY, iconScale, 0);
    }

    public void drawNativeButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        int magic = 20;
        minecraft.getTextureManager().bindTexture(buttonTextures);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.getHoverState(this.hovered); // 1.7.10 field_146123_n
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * magic, this.width / 2, this.height);
        this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * magic, this.width / 2, this.height);
        this.mouseDragged(minecraft, mouseX, mouseY);
        int l = 14737632;
    }

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
