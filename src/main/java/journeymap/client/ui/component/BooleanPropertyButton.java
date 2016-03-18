/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.component;

import journeymap.common.properties.CommonProperties;
import journeymap.common.properties.config.BooleanField;


/**
 * Button that wraps and syncs with an BooleanField value owned by a config instance.
 */
public class BooleanPropertyButton extends OnOffButton implements IPropertyHolder<BooleanField, Boolean>
{
    final CommonProperties properties;
    final BooleanField valueHolder;

    public BooleanPropertyButton(String labelOn, String labelOff, CommonProperties properties, BooleanField valueHolderParam)
    {
        super(labelOn, labelOff, (valueHolderParam != null) && valueHolderParam.get());
        this.valueHolder = valueHolderParam;
        this.properties = properties;
    }

    public BooleanField getValueHolder()
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
    public BooleanField getProperty()
    {
        return valueHolder;
    }
}
