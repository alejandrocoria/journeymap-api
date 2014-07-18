/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.client.FMLClientHandler;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.io.FileHandler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

;

/**
 * Provides language strings in a Map.
 *
 * @author mwoodman
 */
public class MessagesData extends CacheLoader<Class, Map<String, Object>>
{
    private static final String KEY_PREFIX = "jm.webmap."; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public MessagesData()
    {

    }

    @Override
    public Map<String, Object> load(Class aClass) throws Exception
    {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("locale", Constants.getLocale());
        props.put("lang", FMLClientHandler.instance().getClient().gameSettings.language);

        Properties properties = FileHandler.getLangFile("en_US.lang");
        Enumeration<Object> allKeys = properties.keys();

        while (allKeys.hasMoreElements())
        {
            String key = (String) allKeys.nextElement();
            if (key.startsWith(KEY_PREFIX))
            {
                String name = key.split(KEY_PREFIX)[1];
                String value = Constants.getString(key);
                props.put(name, value);
            }
        }

        return ImmutableMap.copyOf(props);
    }

    /**
     * Return length of time in millis data should be kept.
     */
    public long getTTL()
    {
        return TimeUnit.DAYS.toMillis(1);
    }
}
