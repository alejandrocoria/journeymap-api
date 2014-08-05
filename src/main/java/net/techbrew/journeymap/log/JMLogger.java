/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.log;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeVersion;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.thread.JMThreadFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public class JMLogger
{
    public static final String DEPRECATED_LOG_FILE = "journeyMap.log"; //$NON-NLS-1$
    public static final String LOG_FILE = "journeymap.log"; //$NON-NLS-1$

    private static java.util.logging.FileHandler fileHandler;

    public static Logger init()
    {
        final Logger logger = Logger.getLogger(JourneyMap.MOD_ID);

        if (logger.getLevel() == null || logger.getLevel().intValue() > Level.INFO.intValue())
        {
            logger.setLevel(Level.INFO);
        }

        // Remove deprecated logfile
        try
        {
            File deprecatedLog = new File(FileHandler.getJourneyMapDir(), DEPRECATED_LOG_FILE);
            if (deprecatedLog.exists())
            {
                deprecatedLog.delete();
            }
        }
        catch (Exception e)
        {
            logger.severe("Error removing deprecated logfile: " + e.getMessage());
        }

        // File logging
        try
        {
            final File logFile = getLogFile();
            if (logFile.exists())
            {
                logFile.delete();
            }
            else
            {
                logFile.getParentFile().mkdirs();
            }

            LogFormatter formatter = new LogFormatter();

            // Fix console format
            ConsoleHandler consoleHandler = null;
            for(Handler handler : logger.getHandlers())
            {
                if(handler instanceof ConsoleHandler)
                {
                    consoleHandler = (ConsoleHandler) handler;
                    break;
                }
            }
            if(consoleHandler==null)
            {
                consoleHandler = new ConsoleHandler();
                logger.addHandler(consoleHandler);
            }
            consoleHandler.setFormatter(formatter);

            // Set file format
            fileHandler = new java.util.logging.FileHandler(logFile.getAbsolutePath(), 0, 1, false);
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new JMThreadFactory("log").newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    fileHandler.flush();
                    fileHandler.close();
                }
            }));
        }
        catch (SecurityException e)
        {
            logger.severe("Error adding file handler: " + LogFormatter.toString(e));
        }
        catch (IOException e)
        {
            logger.severe("Error adding file handler: " + LogFormatter.toString(e));
        }

        return logger;
    }

    /**
     * Show system properties and those from the PropertyManager.
     */
    public static void logProperties()
    {
        LogRecord record = new LogRecord(Level.INFO, getPropertiesSummary());
        record.setSourceClassName("JMLogger");
        record.setSourceMethodName("logProperties");
        if (fileHandler != null)
        {
            fileHandler.publish(record);
        }
    }

    /**
     * TODO: Clean up
     */
    public static String getPropertiesSummary()
    {
        LinkedHashMap<String, String> props = new LinkedHashMap<String, String>();

        // Versions
        props.put("Version", JourneyMap.MOD_NAME + ", built with Forge " + JourneyMap.FORGE_VERSION);
        props.put("Forge", ForgeVersion.getVersion());

        // Memory
        long totalMB = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMB = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        props.put("Memory", String.format("%sMB total, %sMB free", totalMB, freeMB));

        // Environment
        List<String> envProps = Arrays.asList("os.name, os.arch, java.version, user.country, user.language");
        StringBuilder sb = new StringBuilder();
        for (String env : envProps)
        {
            sb.append(env).append("=").append(System.getProperty(env)).append(", ");
        }
        sb.append("game language=").append(Minecraft.getMinecraft().gameSettings.language).append(", ");
        sb.append("locale=").append(Constants.getLocale());
        props.put("Environment", sb.toString());

        // Put all props in same format
        sb = new StringBuilder();
        for (Map.Entry<String, String> prop : props.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append(LogFormatter.LINEBREAK);
            }
            sb.append(prop.getKey()).append(": ").append(prop.getValue());
        }

        // Add Features
        sb.append(LogFormatter.LINEBREAK).append(FeatureManager.getPolicyDetails());

        // Add config files
        JourneyMap jm = JourneyMap.getInstance();
        List<? extends PropertiesBase> configs = Arrays.asList(
                jm.coreProperties,
                jm.fullMapProperties,
                jm.miniMapProperties,
                jm.waypointProperties,
                jm.webMapProperties
        );

        for (PropertiesBase config : configs)
        {
            sb.append(LogFormatter.LINEBREAK).append(config);
        }

        return sb.toString();
    }

    /**
     * Set the logging level from the value in the properties file.
     */
    public static void setLevelFromProps()
    {
        final Logger logger = Logger.getLogger(JourneyMap.MOD_ID);

        String propLevel = "";
        Level level = Level.INFO;
        try
        {
            propLevel = JourneyMap.getInstance().coreProperties.logLevel.get();
            level = Level.parse(propLevel);
            if (level != logger.getLevel())
            {
                logger.setLevel(level);
                if (level.intValue() < Level.INFO.intValue())
                {
                    ChatLog.announceI18N("jm.common.log_warning", level.getName());
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            logger.warning("Illegal value for logLevel in " + JourneyMap.getInstance().coreProperties.getFile().getName() + ": " + propLevel); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (Throwable t)
        {
            logger.severe(LogFormatter.toString(t));
        }
        finally
        {
            logger.setLevel(level);
        }
    }

    /**
     * Return a handle to the log file used.
     *
     * @return
     */
    public static File getLogFile()
    {
        return new File(FileHandler.getJourneyMapDir(), LOG_FILE);
    }
}
