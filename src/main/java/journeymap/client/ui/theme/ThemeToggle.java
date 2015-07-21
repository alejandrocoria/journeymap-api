/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.theme;

import journeymap.common.Constants;
import journeymap.client.properties.PropertiesBase;
import journeymap.client.render.texture.TextureImpl;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Toggle-type button for Theme
 */
public class ThemeToggle extends ThemeButton
{
    public ThemeToggle(Theme theme, String rawlabel, String iconName)
    {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, null, null);
    }

    public ThemeToggle(Theme theme, String labelOn, String labelOff, String iconName)
    {
        super(theme, labelOn, labelOff, iconName, null, null);
    }

    public ThemeToggle(Theme theme, String rawlabel, String iconName, PropertiesBase properties, AtomicBoolean property)
    {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, properties, property);
        if (property != null)
        {
            setToggled(property.get());
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
