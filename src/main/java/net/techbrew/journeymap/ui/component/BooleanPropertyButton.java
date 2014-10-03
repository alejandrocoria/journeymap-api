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
 * Button that wraps and syncs with an AtomicBoolean value owned by a config instance.
 */
public class BooleanPropertyButton extends ToggleButton
{
    final PropertiesBase properties;
    final AtomicBoolean valueHolder;
    final Type type;

    protected BooleanPropertyButton(int id, Type type, String labelOn, String labelOff, PropertiesBase properties, AtomicBoolean valueHolderParam)
    {
        super(id, labelOn, labelOff, (valueHolderParam != null) && valueHolderParam.get());
        this.type = type;
        this.valueHolder = valueHolderParam;
        this.properties = properties;
    }

    public static BooleanPropertyButton create(int id, PropertiesBase properties, AtomicBoolean valueHolder)
    {
        return create(id, Type.OnOff, properties, valueHolder);
    }

    public static BooleanPropertyButton create(int id, Type type, PropertiesBase properties, AtomicBoolean valueHolder)
    {
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
        return new BooleanPropertyButton(id, type, labelOn, labelOff, properties, valueHolder);
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
            labelOn = Constants.getString(rawLabel, Constants.getString("jm.common.on"));
            labelOff = Constants.getString(rawLabel, Constants.getString("jm.common.off"));
        }
        else
        {
            labelOn = Constants.getString(rawLabel, Constants.getString("jm.common.font_small"));
            labelOff = Constants.getString(rawLabel, Constants.getString("jm.common.font_large"));
        }
        return new BooleanPropertyButton(id, type, labelOn, labelOff, properties, valueHolder);
    }

    public Type getType()
    {
        return type;
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

    public enum Type
    {
        OnOff, SmallLarge
    }
}
