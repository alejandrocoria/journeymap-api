/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.component;

import journeymap.common.properties.config.BooleanField;


/**
 * Button that wraps and syncs with an BooleanField value owned by a config instance.
 */
public class BooleanPropertyButton extends OnOffButton implements IConfigFieldHolder<BooleanField>
{
    final BooleanField booleanField;

    public BooleanPropertyButton(String labelOn, String labelOff, BooleanField field)
    {
        super(labelOn, labelOff, (field != null) && field.get());
        this.booleanField = field;
    }

    public BooleanField getField()
    {
        return booleanField;
    }

    @Override
    public void toggle()
    {
        if (isEnabled())
        {
            setToggled(booleanField.toggleAndSave());
        }
    }

    @Override
    public void refresh()
    {
        if (booleanField != null)
        {
            setToggled(booleanField.get());
        }
    }

    public void setValue(Boolean value)
    {
        if (booleanField == null)
        {
            return;
        }
        booleanField.set(value);
        booleanField.save();
    }

    @Override
    public BooleanField getConfigField()
    {
        return booleanField;
    }
}
