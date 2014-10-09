package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.render.texture.TextureImpl;

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
