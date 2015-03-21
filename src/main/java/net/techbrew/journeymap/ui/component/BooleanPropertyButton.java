/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.component;

import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Button that wraps and syncs with an AtomicBoolean value owned by a config instance.
 */
public class BooleanPropertyButton extends OnOffButton implements IPropertyHolder<AtomicBoolean, Boolean>
{
    final PropertiesBase properties;
    final AtomicBoolean valueHolder;

    public BooleanPropertyButton(String labelOn, String labelOff, PropertiesBase properties, AtomicBoolean valueHolderParam)
    {
        super(labelOn, labelOff, (valueHolderParam != null) && valueHolderParam.get());
        this.valueHolder = valueHolderParam;
        this.properties = properties;
    }

    public AtomicBoolean getValueHolder()
    {
        return valueHolder;
    }

    @Override
    public void toggle()
    {
        if (isEnabled())
        {
            if (properties != null)
            {
                setToggled(properties.toggle(valueHolder));
            }
            else
            {
                setToggled(!toggled);
            }
        }
    }

    @Override
    public void refresh()
    {
        if (valueHolder != null)
        {
            setToggled(valueHolder.get());
        }
    }

    @Override
    public Boolean getPropertyValue()
    {
        return valueHolder.get();
    }

    @Override
    public void setPropertyValue(Boolean value)
    {
        if (valueHolder == null)
        {
            return;
        }
        valueHolder.set(value);
        properties.save();
    }

    @Override
    public AtomicBoolean getProperty()
    {
        return valueHolder;
    }
}
