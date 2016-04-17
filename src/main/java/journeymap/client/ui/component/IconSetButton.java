/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.component;

import journeymap.client.Constants;
import journeymap.common.properties.config.StringField;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwoodman on 6/24/2014.
 */
public class IconSetButton extends Button implements IConfigFieldHolder<StringField>
{
    final String messageKey;
    final StringField field;
    final ArrayList<Object> validNames;

    public IconSetButton(StringField field, List validNames, String messageKey)
    {
        super(0, 0, Constants.getString(messageKey, ""));
        this.field = field;
        this.validNames = new ArrayList<Object>(validNames);
        this.messageKey = messageKey;
        updateLabel();

        // Determine width
        fitWidth(fontRenderer);
    }

    protected void updateLabel()
    {
        if (!validNames.contains(field.get()))
        {
            field.set(validNames.get(0).toString());
            field.save();
        }

        displayString = getSafeLabel(field.get());
    }

    protected String getSafeLabel(String label)
    {
        int maxLength = 13;
        if (label.length() > maxLength)
        {
            label = label.substring(0, maxLength - 3).concat("...");
        }

        return Constants.getString(messageKey, label);
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        int maxWidth = 0;
        for (Object iconSetName : validNames)
        {
            String name = getSafeLabel(iconSetName.toString());
            maxWidth = Math.max(maxWidth, fontRenderer.getStringWidth(name));
        }
        return maxWidth + 12;
    }

    public void nextValue()
    {
        int index = validNames.indexOf(field.get()) + 1;

        if (index == validNames.size() || index < 0)
        {
            index = 0;
        }

        field.set(validNames.get(index).toString());
        field.save();

        updateLabel();
    }

    @Override
    public StringField getConfigField()
    {
        return field;
    }
}
