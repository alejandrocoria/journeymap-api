/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;

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
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
    {
        if (toggled && staysOn)
        {
            return false;
        }
        return super.mousePressed(minecraft, mouseX, mouseY);
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
}
