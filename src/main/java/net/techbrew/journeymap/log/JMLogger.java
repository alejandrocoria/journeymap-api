package net.techbrew.journeymap.log;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.thread.JMThreadFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

        // Start logFile
        logger.info(JourneyMap.MOD_NAME + " initializing"); //$NON-NLS-1$ //$NON-NLS-2$

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

            fileHandler = new java.util.logging.FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new LogFormatter());
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
        String message = "Environment: os.name=" + System.getProperty("os.name") + //$NON-NLS-1$ //$NON-NLS-2$
                ", os.arch=" + System.getProperty("os.arch") +  //$NON-NLS-1$ //$NON-NLS-2$
                ", user.country=" + System.getProperty("user.country") + //$NON-NLS-1$ //$NON-NLS-2$
                ", user.language=" + System.getProperty("user.language") + //$NON-NLS-1$ //$NON-NLS-2$
                ", java.version=" + System.getProperty("java.version") +
                ", Game settings language: " + Minecraft.getMinecraft().gameSettings.language + " / Locale: " + Constants.getLocale();

        long totalMB = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMB = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        message += String.format("\nMemory: %sMB total, %sMB free", totalMB, freeMB);

        JourneyMap jm = JourneyMap.getInstance();

        message += String.format("\n%s\n%s\n%s\n%s\n%s",
                jm.coreProperties,
                jm.fullMapProperties,
                jm.miniMapProperties,
                jm.waypointProperties,
                jm.webMapProperties
        );

        return message;
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
                    ChatLog.announceI18N("JourneyMap.log_warning", level.getName());
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
