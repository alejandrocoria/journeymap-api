/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.server.task.migrate;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.migrate.MigrationTask;
import journeymap.common.version.Version;
import journeymap.server.legacyserver.config.ConfigHandler;
import journeymap.server.legacyserver.config.Configuration;
import journeymap.server.properties.GlobalProperties;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server config migration to 5.2
 */
public class Migrate5_2 implements MigrationTask
{
    Logger logger = LogManager.getLogger(Journeymap.MOD_ID);

    public Migrate5_2()
    {
    }

    @Override
    public boolean isActive(Version currentVersion)
    {
        if (currentVersion.toMajorMinorString().equals("5.2"))
        {
            // Do whatever to determine what needs to be done, return true if the task should be called.
            if (ConfigHandler.getConfigByWorldName("world") != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform the migration task.
     *
     * @return true on success
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception
    {
        try
        {

            String worldName = FMLServerHandler.instance().getServer().getEntityWorld().getWorldInfo().getWorldName();

            GlobalProperties properties = new GlobalProperties();
            Configuration oldConfig = ConfigHandler.getConfigByWorldName(worldName);

            properties.setWorldID(oldConfig.getWorldID());
            properties.radarEnabled.set(oldConfig.getRadar().isPlayerRadar());
            properties.opRadarEnabled.set(oldConfig.getRadar().isOpRadar());
            properties.caveMappingEnabled.set(oldConfig.getCaveMapping().isPlayerCaveMapping());
            properties.opCaveMappingEnabled.set(oldConfig.getCaveMapping().isOpCaveMapping());

            properties.save();
            ConfigHandler.delete(worldName);
            logger.error(String.format("Server config migration to 5.2 complete."));

            return true;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected error in Migrate5_2: %s", LogFormatter.toString(t)));
            return false;
        }
    }
}
