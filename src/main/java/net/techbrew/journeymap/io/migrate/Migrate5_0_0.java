/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.io.migrate;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.Version;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;

/**
 * Migration from anything before 5.0.0 to 5.0.0
 */
public class Migrate5_0_0 implements Migration.Task
{
    Logger logger = LogManager.getLogger(JourneyMap.MOD_ID);

    protected Migrate5_0_0()
    {
    }

    @Override
    public Version getRequiredVersion()
    {
        return new Version(5, 0, 0);
    }

    @Override
    public Boolean call() throws Exception
    {
        boolean jmDirSuccess = migrateJourneyMapDir();
        boolean configDirSuccess = migrateConfigDir();
        return jmDirSuccess && configDirSuccess;
    }

    private boolean migrateJourneyMapDir()
    {
        try
        {

            File[] files = FileHandler.MinecraftDirectory.listFiles();
            if (files != null)
            {
                for (File legacyDir : files)
                {
                    if (legacyDir.isDirectory() && legacyDir.getName().equals(Constants.JOURNEYMAP_DIR_LEGACY))
                    {

                        try
                        {
                            logger.info(String.format("Renaming \"%s\" to \"%s\".",
                                    legacyDir, FileHandler.JourneyMapDirectory));
                            File backupDir = new File(FileHandler.MinecraftDirectory, Constants.JOURNEYMAP_DIR_BACKUP);
                            legacyDir.renameTo(backupDir);
                            backupDir.renameTo(FileHandler.JourneyMapDirectory);
                            return true;
                        }
                        catch (Throwable t)
                        {
                            logger.error(String.format("Could not rename \"%s\" to \"%s\" ! Please shut down and rename it manually.",
                                    legacyDir, FileHandler.JourneyMapDirectory), t);
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Could not rename old directory to \"%s\" ! Please shut down and rename it manually.",
                    FileHandler.JourneyMapDirectory), t);
            return false;
        }
    }

    /**
     * Config files will be in major.minor directory.  Anything earlier than 5.0.0 should be moved to a backup directory.
     *
     * @return
     */
    private boolean migrateConfigDir()
    {
        try
        {
            File legacyConfigDir = new File(FileHandler.MinecraftDirectory, Constants.CONFIG_DIR_LEGACY);
            if (!legacyConfigDir.exists())
            {
                return true;
            }

            if (legacyConfigDir.isFile())
            {
                logger.warn("Found file instead of directory.  Attempting to rename it to " + legacyConfigDir.getName() + "_bak");
                return legacyConfigDir.renameTo(new File(FileHandler.MinecraftDirectory, Constants.CONFIG_DIR_LEGACY + "_bak"));
            }

            File[] configFiles = legacyConfigDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile() && file.getName().endsWith(".config");
                }
            });

            boolean success = true;
            if (configFiles != null && configFiles.length > 0)
            {
                File backupDir = new File(legacyConfigDir, "backup_" + System.currentTimeMillis());
                backupDir.mkdirs();
                for (File old : configFiles)
                {
                    try
                    {
                        boolean moved = old.renameTo(new File(backupDir, old.getName()));
                        if (moved)
                        {
                            logger.info(String.format("Moved obsolete \"%s\" to \"%s\".", old, backupDir));
                        }
                        else
                        {
                            logger.warn(String.format("Failed to move obsolete \"%s\" to \"%s\".", old, backupDir));
                            success = false;
                        }
                    }
                    catch (Throwable t)
                    {
                        logger.error(String.format("Failed to move obsolete \"%s\" to \"%s\": %s", old, backupDir, LogFormatter.toString(t)));
                        success = false;
                    }
                }
            }
            return success;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected error in migrateConfigDir(): %s", LogFormatter.toString(t)));
            return false;
        }
    }
}
