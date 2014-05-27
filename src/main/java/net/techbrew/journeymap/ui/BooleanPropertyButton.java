package net.techbrew.journeymap.ui;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Button that wraps and syncs with an AtomicBoolean value owned by a config instance.
 */
public class BooleanPropertyButton extends Button
{
    public enum Type
    {
        OnOff, SmallLarge
    }

    final PropertiesBase properties;
    final AtomicBoolean valueHolder;

    public static BooleanPropertyButton create(int id, PropertiesBase properties, AtomicBoolean valueHolder)
    {
        return create(id, Type.OnOff, properties, valueHolder);
    }

    public static BooleanPropertyButton create(int id, Type type, PropertiesBase properties, AtomicBoolean valueHolder)
    {
        String labelOn, labelOff;
        if (Type.OnOff == type)
        {
            labelOn = Constants.getString("MapOverlay.on");
            labelOff = Constants.getString("MapOverlay.off");
        }
        else
        {
            labelOn = Constants.getString("MiniMap.font_small");
            labelOff = Constants.getString("MiniMap.font_large");
        }
        return new BooleanPropertyButton(id, labelOn, labelOff, properties, valueHolder);
    }

    public static BooleanPropertyButton create(int id, String rawLabel, PropertiesBase properties, AtomicBoolean valueHolder)
    {
        return create(id, Type.OnOff, rawLabel, properties, valueHolder);
    }

    public static BooleanPropertyButton create(int id, Type type, String rawLabel, PropertiesBase properties, AtomicBoolean valueHolder)
    {
        String labelOn, labelOff;
        if (Type.OnOff == type)
        {
            labelOn = Constants.getString(rawLabel, Constants.getString("MapOverlay.on"));
            labelOff = Constants.getString(rawLabel, Constants.getString("MapOverlay.off"));
        }
        else
        {
            labelOn = Constants.getString(rawLabel, Constants.getString("MiniMap.font_small"));
            labelOff = Constants.getString(rawLabel, Constants.getString("MiniMap.font_large"));
        }
        return new BooleanPropertyButton(id, labelOn, labelOff, properties, valueHolder);
    }

    public BooleanPropertyButton(int id, String labelOn, String labelOff, PropertiesBase properties, AtomicBoolean valueHolder)
    {
        super(id, 0, 0, labelOn, labelOff, valueHolder.get());
        this.valueHolder = valueHolder;
        this.properties = properties;
        if (properties == null || valueHolder == null)
        {
            this.setEnabled(false);
            this.setNoDisableText(true);
        }
    }

    @Override
    public void toggle()
    {
        if (isEnabled())
        {
            setToggled(properties.toggle(valueHolder));
        }
    }
}
