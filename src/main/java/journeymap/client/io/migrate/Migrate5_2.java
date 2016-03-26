/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io.migrate;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.*;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.client.model.GridSpecs;
import journeymap.client.properties.MiniMapProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;
import journeymap.common.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Migration from 5.1.x to 5.2
 */
public class Migrate5_2 implements Migration.Task
{
    // GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    Logger logger = LogManager.getLogger(Journeymap.MOD_ID);

    protected Migrate5_2()
    {
    }

    @Override
    public Version getRequiredVersion()
    {
        return new Version(5, 2, 0);
    }

    @Override
    public Boolean call() throws Exception
    {
        return migrateConfigDir();
    }


    /**
     * Check for 5.1 configs and copy in their values.
     *
     * @return
     */
    private boolean migrateConfigDir()
    {
        try
        {
            if (JourneymapClient.getCoreProperties() == null)
            {
                JourneymapClient.getInstance().loadConfigProperties();
            }

            // Check current configs to see if they've already been updated
            String optionsManagerViewed = JourneymapClient.getCoreProperties().optionsManagerViewed.get();
            if (!Strings.isNullOrEmpty(optionsManagerViewed))
            {
                return true;
            }

            String path5_1 = Joiner.on(File.separator).join(Constants.JOURNEYMAP_DIR, "config", "5.1");
            File legacyConfigDir = new File(FileHandler.MinecraftDirectory, path5_1);

            List<? extends PropertiesBase> propertiesList = Arrays.asList(JourneymapClient.getCoreProperties(),
                    JourneymapClient.getFullMapProperties(),
                    JourneymapClient.getMiniMapProperties(1), JourneymapClient.getMiniMapProperties(2),
                    JourneymapClient.getWaypointProperties(), JourneymapClient.getWebMapProperties());

            for (PropertiesBase properties : propertiesList)
            {
                File oldConfigfile = new File(legacyConfigDir, properties.getFile().getName());
                if (oldConfigfile.canRead())
                {
                    updateValues(properties, oldConfigfile);
                }
            }

            return true;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected error in migrateConfigDir(): %s", LogFormatter.toString(t)));
            return false;
        }
    }

    private boolean updateValues(PropertiesBase properties, File oldConfigFile)
    {
        try
        {
            JsonObject jsonObject = new JsonParser().parse(Files.toString(oldConfigFile, UTF8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> member : jsonObject.entrySet())
            {
                try
                {
                    Field field = properties.getClass().getField(member.getKey());
                    if (field != null)
                    {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        if (Boolean.class.isAssignableFrom(fieldType))
                        {
                            boolean value = member.getValue().getAsBoolean();
                            field.set(properties, value);
                        }
                        else if (BooleanField.class.isAssignableFrom(fieldType))
                        {
                            BooleanField booleanField = (BooleanField) field.get(properties);
                            boolean value = member.getValue().getAsBoolean();
                            booleanField.set(value);
                        }
                        else if (Integer.class.isAssignableFrom(fieldType))
                        {
                            int value = member.getValue().getAsInt();
                            field.set(properties, value);
                        }
                        else if (IntegerField.class.isAssignableFrom(fieldType))
                        {
                            IntegerField integerField = (IntegerField) field.get(properties);
                            int value = member.getValue().getAsInt();
                            integerField.set(value);
                        }
                        else if (String.class.isAssignableFrom(fieldType))
                        {
                            String value = member.getValue().getAsString();
                            field.set(properties, value);
                        }
                        else if (StringField.class.isAssignableFrom(fieldType))
                        {
                            StringField stringField = (StringField) field.get(properties);
                            String value = member.getValue().getAsString();
                            stringField.set(value);
                        }
                        else if (EnumField.class.isAssignableFrom(fieldType))
                        {
                            EnumField enumField = (EnumField) field.get(properties);
                            String value = member.getValue().getAsString();
                            Enum enumValue = Enum.valueOf(enumField.getEnumClass(), value);
                            enumField.set(enumValue);
                        }
                        else if (GridSpecs.class.isAssignableFrom(fieldType))
                        {
                            GridSpecs oldGridSpecs = gson.fromJson(member.getValue(), GridSpecs.class);
                            GridSpecs newGridSpecs = (GridSpecs) field.get(properties);
                            newGridSpecs.updateFrom(oldGridSpecs);
                        }
                        else if (Version.class.isAssignableFrom(fieldType))
                        {
                            Version oldVersion = gson.fromJson(member.getValue(), Version.class);
                            field.set(properties, oldVersion);
                        }
                    }
                }
                catch (NoSuchFieldException t)
                {
                    if(member.getKey().equals("active") && properties instanceof MiniMapProperties)
                    {
                        ((MiniMapProperties) properties).setActive(member.getValue().getAsBoolean());
                    }
                    else
                    {
                        logger.warn(String.format("Skipped migrating 5.1 value: %s.%s",
                                properties.getName(),
                                member.getKey()));
                    }
                }
                catch (Throwable t)
                {
                    logger.error(String.format("Couldn't migrate 5.1 value: %s.%s",
                            properties.getName(),
                            member.getKey()),
                            LogFormatter.toPartialString(t));
                }
            }

            properties.save();
            return true;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected error in updateValues(): %s", LogFormatter.toString(t)));
            return false;
        }
    }
}
