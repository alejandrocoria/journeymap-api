/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.component;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mwoodman on 6/24/2014.
 */
public class IconSetButton extends Button implements IPropertyHolder<String>
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
        fitWidth(FMLClientHandler.instance().getClient().fontRenderer);
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
            maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(name));
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
