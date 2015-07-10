/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.ui.component;

import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mwoodman on 6/24/2014.
 */
public class IconSetButton extends Button implements IPropertyHolder<AtomicReference<String>, String>
{
    final String messageKey;
    final PropertiesBase baseProperties;
    final AtomicReference<String> valueHolder;
    final ArrayList<Object> validNames;

    public IconSetButton(PropertiesBase baseProperties, AtomicReference<String> valueHolder, List validNames, String messageKey)
    {
        super(0, 0, Constants.getString(messageKey, ""));
        this.baseProperties = baseProperties;
        this.valueHolder = valueHolder;
        this.validNames = new ArrayList<Object>(validNames);
        this.messageKey = messageKey;
        updateLabel();

        // Determine width
        fitWidth(fontRenderer);
    }

    protected void updateLabel()
    {
        if (!validNames.contains(valueHolder.get()))
        {
            valueHolder.set(validNames.get(0).toString());
            baseProperties.save();
        }

        displayString = getSafeLabel(valueHolder.get());
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
        int index = validNames.indexOf(valueHolder.get()) + 1;

        if (index == validNames.size() || index < 0)
        {
            index = 0;
        }

        valueHolder.set(validNames.get(index).toString());
        baseProperties.save();

        updateLabel();
    }

    @Override
    public AtomicReference<String> getProperty()
    {
        return valueHolder;
    }

    @Override
    public String getPropertyValue()
    {
        return valueHolder.get();
    }

    @Override
    public void setPropertyValue(String value)
    {
        if (valueHolder == null)
        {
            return;
        }
        valueHolder.set(value);
        baseProperties.save();
    }
}
