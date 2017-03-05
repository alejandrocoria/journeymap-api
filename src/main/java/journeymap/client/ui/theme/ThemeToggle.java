/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.properties.config.BooleanField;

/**
 * Toggle-type button for Theme
 */
public class ThemeToggle extends ThemeButton
{
    /**
     * Instantiates a new Theme toggle.
     *
     * @param theme    the theme
     * @param rawlabel the rawlabel
     * @param iconName the icon name
     */
    public ThemeToggle(Theme theme, String rawlabel, String iconName)
    {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, null);
    }

    /**
     * Instantiates a new Theme toggle.
     *
     * @param theme    the theme
     * @param labelOn  the label on
     * @param labelOff the label off
     * @param iconName the icon name
     */
    public ThemeToggle(Theme theme, String labelOn, String labelOff, String iconName)
    {
        super(theme, labelOn, labelOff, iconName, null);
    }

    /**
     * Instantiates a new Theme toggle.
     *
     * @param theme    the theme
     * @param rawlabel the rawlabel
     * @param iconName the icon name
     * @param field    the field
     */
    public ThemeToggle(Theme theme, String rawlabel, String iconName, BooleanField field)
    {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, field);
        if (field != null)
        {
            setToggled(field.get());
        }
    }

    @Override
    protected String getPathPattern()
    {
        return "control/%stoggle_%s.png";
    }

    @Override
    protected Theme.Control.ButtonSpec getButtonSpec(Theme theme)
    {
        return theme.control.toggle;
    }

    @Override
    protected TextureImpl getActiveTexture(boolean isMouseOver)
    {
        if (isEnabled())
        {
            TextureImpl activeTexture = this.toggled ? textureOn : textureOff;
            return activeTexture;
        }
        else
        {
            return textureDisabled;
        }
    }
}
