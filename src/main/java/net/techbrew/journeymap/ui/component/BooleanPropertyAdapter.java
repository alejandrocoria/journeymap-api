/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.component;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps and syncs with an AtomicBoolean value owned by a config instance.
 */
public class BooleanPropertyAdapter implements PropertyAdapter
{
    final Type type;
    final AtomicBoolean valueHolder;
    final PropertiesBase properties;


    public BooleanPropertyAdapter(final PropertiesBase properties, final AtomicBoolean valueHolder)
    {
        this(properties, valueHolder, Type.OnOff);
    }

    public BooleanPropertyAdapter(final PropertiesBase properties, final AtomicBoolean valueHolder, Type type)
    {
        this.type = type;
        this.valueHolder = valueHolder;
        this.properties = properties;
    }

    public void setButton(Button button, String rawLabel)
    {
        // Derive labels
        String labelOn, labelOff;
        if (Type.OnOff == type)
        {
            labelOn = Constants.getString("jm.common.on");
            labelOff = Constants.getString("jm.common.off");
        }
        else
        {
            labelOn = Constants.getString("jm.common.font_small");
            labelOff = Constants.getString("jm.common.font_large");
        }

        labelOn = Constants.getString(rawLabel, labelOn);
        labelOff = Constants.getString(rawLabel, labelOff);
        button.setLabels(labelOn, labelOff);

        // Hook into toggle behavior
        if (valueHolder == null)
        {
            button.setEnabled(false);
        }
        else
        {
            button.setEnabled(true);
            button.setToggled(valueHolder.get(), false);
            button.addToggleListener(new Button.ToggleListener()
            {
                @Override
                public boolean onToggle(Button button, boolean toggled)
                {
                    if (valueHolder.get() != toggled)
                    {
                        if (properties != null)
                        {
                            properties.toggle(valueHolder);
                        }
                        else
                        {
                            valueHolder.set(toggled);
                            button.setToggled(toggled, false);
                        }
                    }
                    return true;
                }
            });
        }
    }

    public Type getType()
    {
        return type;
    }

    public AtomicBoolean getValueHolder()
    {
        return valueHolder;
    }

    public enum Type
    {
        OnOff, SmallLarge
    }
}
