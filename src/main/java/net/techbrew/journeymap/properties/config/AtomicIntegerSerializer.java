/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.properties.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerSerializer implements JsonSerializer<AtomicInteger>, JsonDeserializer<AtomicInteger>
{

    AtomicBoolean configFormatChanged;

    public AtomicIntegerSerializer(AtomicBoolean configFormatChanged)
    {
        this.configFormatChanged = configFormatChanged;
    }

    @Override
    public JsonElement serialize(AtomicInteger arg0, Type arg1, JsonSerializationContext arg2)
    {
        return new JsonPrimitive(arg0.get());
    }

    @Override
    public AtomicInteger deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException
    {
        if (arg0.isJsonPrimitive())
        {
            return new AtomicInteger(arg0.getAsInt());
        }
        else
        {
            configFormatChanged.set(true);
            return new AtomicInteger(arg0.getAsJsonObject().get("value").getAsInt());
        }
    }
}