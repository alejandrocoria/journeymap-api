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
import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeVersion;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.thread.JMThreadFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.*;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;


public class JMLogger
{
    public static final String DEPRECATED_LOG_FILE = "journeyMap.log"; //$NON-NLS-1$
    public static final String LOG_FILE = "journeymap.log"; //$NON-NLS-1$

    private static RandomAccessFileAppender fileAppender;

    public static Logger init()
    {
        final Logger logger = LogManager.getLogger(JourneyMap.MOD_ID);

        if(!logger.isInfoEnabled())
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

            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();

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
                            null,// filter
                            "false",// advertise
                            null,// advertiseURI
                            null// config
                    );

            LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
            //String stringLogLevel = JourneyMap.getInstance().coreProperties.logLevel.get();
            //Level logLevel = Level.toLevel(stringLogLevel, Level.INFO);
            loggerConfig.setLevel(Level.INFO);
            loggerConfig.addAppender(fileAppender, Level.ALL, null);
            context.updateLoggers();

            if(!fileAppender.isStarted())
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
     * Return a handle to the log file used.
     *
     * @return
     */
    public static File getLogFile()
    {
        return new File(FileHandler.getJourneyMapDir(), LOG_FILE);
    }
}
