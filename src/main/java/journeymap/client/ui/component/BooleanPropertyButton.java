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
    /**
     * The Boolean field.
     */
    final BooleanField booleanField;

    /**
     * Instantiates a new Boolean property button.
     *
     * @param labelOn  the label on
     * @param labelOff the label off
     * @param field    the field
     */
    public BooleanPropertyButton(String labelOn, String labelOff, BooleanField field)
    {
        super(labelOn, labelOff, (field != null) && field.get());
        this.booleanField = field;
    }

    /**
     * Gets field.
     *
     * @return the field
     */
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

    /**
     * Sets value.
     *
     * @param value the value
     */
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
