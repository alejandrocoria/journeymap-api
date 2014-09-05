package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.render.texture.TextureImpl;

/**
 * Created by Mark on 8/30/2014.
 */
public class ThemeToggle extends ThemeButton
{
    public ThemeToggle(Enum enumId, Theme theme, String iconName)
    {
        super(enumId.ordinal(), theme, "", "", false, iconName);
    }

    public ThemeToggle(Enum enumId, Theme theme, String label, String iconName)
    {
        this(enumId.ordinal(), theme, label, label, false, iconName);
    }

    public ThemeToggle(int id,Theme theme,  String label, String iconName)
    {
        this(id, theme, label, label, false, iconName);
    }

    public ThemeToggle(Enum enumId, Theme theme, String labelOn, String labelOff, boolean toggled, String iconName)
    {
        this(enumId.ordinal(), theme, labelOn, labelOff, toggled, iconName);
    }

    public ThemeToggle(int id, Theme theme, String labelOn, String labelOff, boolean toggled, String iconName)
    {
        super(id, theme, labelOn, labelOff, toggled, iconName);
        setToggled(toggled);
    }

    @Override
    protected String getPathPattern()
    {
        return "control/toggle_%s.png";
    }

    @Override
    protected Theme.Control.ButtonSpec getButtonSpec(Theme theme)
    {
        return theme.control.toggle;
    }

    @Override
    protected TextureImpl getActiveTexture(boolean isMouseOver)
    {
        if(isEnabled())
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
