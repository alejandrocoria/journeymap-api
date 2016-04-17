/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.migrate;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.migrate.MigrationTask;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Migration from 5.1.x to 5.2
 */
public class Migrate5_2 implements MigrationTask
{
    // GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    Logger logger = LogManager.getLogger(Journeymap.MOD_ID);

    public Migrate5_2()
    {
    }

    @Override
    public boolean isActive(Version currentVersion)
    {
        if (currentVersion.toMajorMinorString().equals("5.2"))
        {

            if (JourneymapClient.getCoreProperties() == null)
            {
                JourneymapClient.getInstance().loadConfigProperties();
            }

            // Check current configs to see if they've already been updated
            String optionsManagerViewed = JourneymapClient.getCoreProperties().optionsManagerViewed.get();
            if (Strings.isNullOrEmpty(optionsManagerViewed))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean call() throws Exception
    {
        return migrateConfigs();
    }

    /**
     * Check for 5.1 configs and copy in their values.
     *
     * @return
     */
    private boolean migrateConfigs()
    {
        try
        {
            String path5_1 = Joiner.on(File.separator).join(Constants.JOURNEYMAP_DIR, "config", "5.1");
            File legacyConfigDir = new File(FileHandler.MinecraftDirectory, path5_1);
            if (!legacyConfigDir.canRead())
            {
                return true;
            }

            logger.info("Migrating configs from 5.1 to 5.2");

            List<? extends PropertiesBase> propertiesList = Arrays.asList(
                    JourneymapClient.getCoreProperties(),
                    JourneymapClient.getFullMapProperties(),
                    JourneymapClient.getMiniMapProperties(1),
                    JourneymapClient.getMiniMapProperties(2),
                    JourneymapClient.getWaypointProperties(),
                    JourneymapClient.getWebMapProperties()
            );

            for (PropertiesBase properties : propertiesList)
            {
                File oldConfigfile = new File(legacyConfigDir, properties.getFile().getName());
                if (oldConfigfile.canRead())
                {
                    try
                    {
                        properties.load(oldConfigfile, false);
                        properties.save();
                    }
                    catch (Throwable t)
                    {
                        logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t)));
                    }
                }
            }

            JourneymapClient.getCoreProperties().optionsManagerViewed.set("5.1");

            return true;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t)));
            return false;
        }
    }
}
