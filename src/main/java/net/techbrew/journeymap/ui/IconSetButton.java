/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mwoodman on 6/24/2014.
 */
public class IconSetButton extends Button
{
    final String messageKey;
    final PropertiesBase baseProperties;
    final AtomicReference<String> valueHolder;
    final ArrayList<String> validNames;

    public IconSetButton(int id, PropertiesBase baseProperties, AtomicReference<String> valueHolder, ArrayList<String> validNames, String messageKey)
    {
        super(id, 0, 0, Constants.getString(messageKey, ""));
        this.baseProperties = baseProperties;
        this.valueHolder = valueHolder;
        this.validNames = validNames;
        this.messageKey = messageKey;
        updateLabel();

        // Determine width
        fitWidth(FMLClientHandler.instance().getClient().fontRenderer);
    }

    protected void updateLabel()
    {
        if (!validNames.contains(valueHolder.get()))
        {
            valueHolder.set(validNames.get(0));
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
        for (String iconSetName : validNames)
        {
            String name = getSafeLabel(iconSetName);
            maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(name));
        }
        return maxWidth + 12;
    }

    @Override
    public void toggle()
    {
        int index = validNames.indexOf(valueHolder.get()) + 1;

        if (index == validNames.size() || index < 0)
        {
            index = 0;
        }

        valueHolder.set(validNames.get(index));
        baseProperties.save();

        updateLabel();
    }
}
