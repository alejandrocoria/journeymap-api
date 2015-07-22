/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import journeymap.client.Constants;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.io.FileHandler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
        props.put("lang", ForgeHelper.INSTANCE.getClient().gameSettings.language);

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
