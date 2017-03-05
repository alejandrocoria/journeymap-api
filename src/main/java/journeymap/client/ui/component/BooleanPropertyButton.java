/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
            if (booleanField != null)
            {
                setToggled(booleanField.toggleAndSave());
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
        if (booleanField != null)
        {
            setToggled(booleanField.get());
        }
    }

    public void setValue(Boolean value)
    {
        if (booleanField == null)
        {
            toggled = value;
        }
        else
        {
            booleanField.set(value);
            booleanField.save();
        }
    }

    @Override
    public BooleanField getConfigField()
    {
        return booleanField;
    }
}
