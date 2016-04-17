/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
    public ThemeToggle(Theme theme, String rawlabel, String iconName)
    {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, null);
    }

    public ThemeToggle(Theme theme, String labelOn, String labelOff, String iconName)
    {
        super(theme, labelOn, labelOff, iconName, null);
    }

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
