/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.log;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeVersion;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.ui.option.StringListProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class JMLogger
{
    public static final String DEPRECATED_LOG_FILE = "journeyMap.log"; //$NON-NLS-1$
    public static final String LOG_FILE = "journeymap.log"; //$NON-NLS-1$

    // Singleton error hashcodes
    static private final HashSet<Integer> singletonErrors = new HashSet<Integer>();
    static private final AtomicInteger singletonErrorsCounter = new AtomicInteger(0);

    private static RandomAccessFileAppender fileAppender;

    public static Logger init()
    {
        final Logger logger = LogManager.getLogger(JourneyMap.MOD_ID);

        if (!logger.isInfoEnabled())
        {
            logger.warn("Forge is surpressing INFO-level logging. If you need technical support for JourneyMap, you must return logging to INFO.");
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
            logger.error("Error removing deprecated logfile: " + e.getMessage());
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

            PatternLayout layout = PatternLayout.createLayout(
                    "[%d{HH:mm:ss}] [%t/%level] [%C{1}] %msg%n", null,
                    null, "UTF-8", "true");

            fileAppender = RandomAccessFileAppender
                    .createAppender(
                            logFile.getAbsolutePath(),// filename
                            "true",// append
                            "journeymap-logfile",// name
                            "true",// immediateFlush
                            "true",// ignoreExceptions
                            layout,
                            null,
                            "false",// advertise
                            null,// advertiseURI
                            null// config
                    );

            ((org.apache.logging.log4j.core.Logger) logger).addAppender(fileAppender);
            if (!fileAppender.isStarted())
            {
                fileAppender.start();
            }

            logger.info("JourneyMap log initialized.");

        }
        catch (SecurityException e)
        {
            logger.error("Error adding file handler: " + LogFormatter.toString(e));
        }
        catch (Throwable e)
        {
            logger.error("Error adding file handler: " + LogFormatter.toString(e));
        }

        return logger;
    }

    public static void setLevelFromProperties()
    {
        try
        {
            final Logger logger = LogManager.getLogger(JourneyMap.MOD_ID);
            ((org.apache.logging.log4j.core.Logger) logger).setLevel(Level.toLevel(JourneyMap.getCoreProperties().logLevel.get(), Level.INFO));
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Show system properties and those from the PropertyManager.
     */
    public static void logProperties()
    {
        LogEvent record = new Log4jLogEvent(JourneyMap.MOD_NAME, MarkerManager.getMarker(JourneyMap.MOD_NAME), null, Level.INFO, new SimpleMessage(getPropertiesSummary()), null);
        if (fileAppender != null)
        {
            fileAppender.append(record);
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
                JourneyMap.getMiniMapProperties1(),
                JourneyMap.getMiniMapProperties2(),
                JourneyMap.getFullMapProperties(),
                JourneyMap.getWaypointProperties(),
                JourneyMap.getWebMapProperties(),
                JourneyMap.getCoreProperties()
        );

        for (PropertiesBase config : configs)
        {
            // TODO: Only show non-default values?
            sb.append(LogFormatter.LINEBREAK).append(config);
        }

        return sb.toString();
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

    public static void logOnce(String text, Throwable throwable)
    {
        if (!singletonErrors.contains(text.hashCode()))
        {
            singletonErrors.add(text.hashCode());
            JourneyMap.getLogger().error(text + " (SUPPRESSED)");
            if (throwable != null)
            {
                JourneyMap.getLogger().error(LogFormatter.toString(throwable));
            }
        }
        else
        {
            int count = singletonErrorsCounter.incrementAndGet();
            if (count > 1000)
            {
                singletonErrors.clear();
                singletonErrorsCounter.set(0);
            }
        }
    }

    public static class LogLevelStringProvider implements StringListProvider
    {
        @Override
        public List<String> getStrings()
        {
            Level[] levels = Level.values();
            String[] levelStrings = new String[levels.length];
            for (int i = 0; i < levels.length; i++)
            {
                levelStrings[i] = levels[i].toString();
            }
            return Arrays.asList(levelStrings);
        }

        @Override
        public String getDefaultString()
        {
            return Level.INFO.toString();
        }
    }
}
