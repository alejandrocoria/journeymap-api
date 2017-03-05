/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.migrate;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
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
public class Migrate5_3 implements MigrationTask
{
    /**
     * The constant UTF8.
     */
// GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * The Gson.
     */
// Gson for file persistence
    protected transient final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * The Logger.
     */
    Logger logger = LogManager.getLogger(Journeymap.MOD_ID);

    /**
     * Instantiates a new Migrate 5 3.
     */
    public Migrate5_3()
    {
    }

    @Override
    public boolean isActive(Version currentVersion)
    {
        if (currentVersion.toMajorMinorString().equals("5.3"))
        {
            if (Journeymap.getClient().getCoreProperties() == null)
            {
                Journeymap.getClient().loadConfigProperties();
            }

            // Check current configs to see if they've already been updated
            String optionsManagerViewed = Journeymap.getClient().getCoreProperties().optionsManagerViewed.get();
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
     * Check for 5.2 configs and copy in their values.
     *
     * @return
     */
    private boolean migrateConfigs()
    {
        try
        {
            String path5_2 = Joiner.on(File.separator).join(Constants.JOURNEYMAP_DIR, "config", "5.2");
            File legacyConfigDir = new File(FileHandler.MinecraftDirectory, path5_2);
            if (!legacyConfigDir.canRead())
            {
                return true;
            }

            logger.info("Migrating configs from 5.2 to 5.3");

            List<? extends PropertiesBase> propertiesList = Arrays.asList(
                    Journeymap.getClient().getCoreProperties(),
                    Journeymap.getClient().getFullMapProperties(),
                    Journeymap.getClient().getMiniMapProperties(1),
                    Journeymap.getClient().getMiniMapProperties(2),
                    Journeymap.getClient().getWaypointProperties(),
                    Journeymap.getClient().getWebMapProperties()
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

            Journeymap.getClient().getCoreProperties().optionsManagerViewed.set("5.2");

            return true;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t)));
            return false;
        }
    }
}
