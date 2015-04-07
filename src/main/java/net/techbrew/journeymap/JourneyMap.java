/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import modinfo.ModInfo;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.common.CommonProxy;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.forge.event.EventHandlerManager;
import net.techbrew.journeymap.forge.event.WorldInfoHandler;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.io.migrate.Migration;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.properties.*;
import net.techbrew.journeymap.render.map.TileDrawStepCache;
import net.techbrew.journeymap.server.JMServer;
import net.techbrew.journeymap.task.main.IMainThreadTask;
import net.techbrew.journeymap.task.main.MainTaskController;
import net.techbrew.journeymap.task.main.MappingMonitorTask;
import net.techbrew.journeymap.task.multi.ITaskManager;
import net.techbrew.journeymap.task.multi.TaskController;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;


/**
 * This software is copyright (C) Mark Woodman (mwoodman@techbrew.net) and is
 * provided as-is with no warrantee whatsoever.
 * <p/>
 * Central class for the JourneyMap mod.
 *
 * @author Mark Woodman
 */
@SideOnly(Side.CLIENT)
public class JourneyMap implements CommonProxy
{
    public static final String WEBSITE_URL = "http://journeymap.techbrew.net/";
    public static final String DOWNLOAD_URL = WEBSITE_URL + "download";
    public static final Version JM_VERSION = Version.from("@MAJOR@", "@MINOR@", "@MICRO@", "@PATCH@", new Version(5, 1, 0, "dev"));
    public static final String FORGE_VERSION = "@FORGEVERSION@";
    public static final String EDITION = getEdition();

    public static final String MOD_NAME = CommonProxy.SHORT_MOD_NAME + " " + EDITION;
    public static final String VERSION_URL = "https://dl.dropboxusercontent.com/u/38077766/JourneyMap/journeymap-versions.json";

    private static JourneyMap INSTANCE;

    public ModInfo modInfo;

    // Properties & preferences
    private volatile CoreProperties coreProperties;
    private volatile FullMapProperties fullMapProperties;
    private volatile MiniMapProperties miniMapProperties1;
    private volatile MiniMapProperties2 miniMapProperties2;
    private volatile WebMapProperties webMapProperties;
    private volatile WaypointProperties waypointProperties;
    private volatile Boolean initialized = false;
    private volatile String currentWorldId = null;
    private Logger logger;
    private boolean threadLogging = false;
    private String playerName;

    // Task controller for issuing tasks in executor
    private TaskController multithreadTaskController;
    private ChunkRenderController chunkRenderController;

    // Controller for tasks that have to be done on the main thread
    private volatile MainTaskController mainThreadTaskController;

    private volatile Minecraft mc;

    /**
     * Constructor.
     */
    public JourneyMap()
    {
        if (INSTANCE != null)
        {
            throw new IllegalArgumentException("Use instance() after initialization is complete");
        }
        INSTANCE = this;
    }

    public static JourneyMap getInstance()
    {
        return INSTANCE;
    }

    private static String getEdition()
    {
        String ed = null;
        try
        {
            ed = JM_VERSION + " " + FeatureManager.getPolicySetName();
        }
        catch (Throwable t)
        {
            ed = JM_VERSION + " ?";
            t.printStackTrace(System.err);
        }
        return ed;
    }

    /**
     * @return
     */
    public static Logger getLogger()
    {
        return LogManager.getLogger(JourneyMap.MOD_ID);
    }

    public static CoreProperties getCoreProperties()
    {
        return INSTANCE.coreProperties;
    }

    public static FullMapProperties getFullMapProperties()
    {
        return INSTANCE.fullMapProperties;
    }

    public static void disable()
    {
        INSTANCE.initialized = false;
        EventHandlerManager.unregisterAll();
        INSTANCE.stopMapping();
        DataCache.instance().purge();
    }

    public static MiniMapProperties getMiniMapProperties(int which)
    {
        switch (which)
        {
            case 2:
            {
                INSTANCE.miniMapProperties2.setActive(true);
                INSTANCE.miniMapProperties1.setActive(false);
                return getMiniMapProperties2();
            }
            default:
            {
                INSTANCE.miniMapProperties1.setActive(true);
                INSTANCE.miniMapProperties2.setActive(false);
                return getMiniMapProperties1();
            }
        }
    }

    public static int getActiveMinimapId()
    {
        if (INSTANCE.miniMapProperties1.isActive())
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }

    public static MiniMapProperties getMiniMapProperties1()
    {
        return INSTANCE.miniMapProperties1;
    }

    public static MiniMapProperties getMiniMapProperties2()
    {
        return INSTANCE.miniMapProperties2;
    }

    public static WebMapProperties getWebMapProperties()
    {
        return INSTANCE.webMapProperties;
    }

    public static WaypointProperties getWaypointProperties()
    {
        return INSTANCE.waypointProperties;
    }

    public Boolean isInitialized()
    {
        return initialized;
    }

    public Boolean isMapping()
    {
        return initialized && multithreadTaskController != null && multithreadTaskController.isMapping();
    }

    public Boolean isThreadLogging()
    {
        return threadLogging;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        StatTimer timer = null;
        try
        {
            timer = StatTimer.getDisposable("elapsed").start();

            // Migrate tasks
            boolean migrationOk = new Migration().performTasks();

            // Ensure logger inits
            logger = JMLogger.init();
            logger.info("ensureCurrent ENTER");

            if (initialized)
            {
                logger.warn("Already initialized, aborting");
                return;
            }

            // Init ModInfo
            modInfo = new ModInfo("UA-28839029-4", "en_US", MOD_ID, MOD_NAME, getEdition());

            // Trigger statics on EntityList (may not be needed anymore?)
            EntityRegistry.instance();

            // Load properties
            loadConfigProperties();

            // Log properties
            JMLogger.logProperties();

            // Logging for thread debugging
            threadLogging = false;

            logger.info("ensureCurrent EXIT, " + (timer == null ? "" : timer.stopAndReport()));
        }
        catch (Throwable t)
        {
            if (logger == null)
            {
                logger = LogManager.getLogger(JourneyMap.MOD_ID);
            }
            logger.error(LogFormatter.toString(t));
            throw t;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postInitialize(FMLPostInitializationEvent event)
    {
        StatTimer timer = null;
        try
        {
            logger.info("postInitialize ENTER");
            timer = StatTimer.getDisposable("elapsed").start();

            // Get ref to Minecraft
            mc = FMLClientHandler.instance().getClient();

            // Main thread task controller
            mainThreadTaskController = new MainTaskController(mc, this);
            mainThreadTaskController.addTask(new MappingMonitorTask());

            // Register general event handlers
            EventHandlerManager.registerGeneralHandlers();
            EventHandlerManager.registerGuiHandlers();

            // Resets detection results of Voxel/Rei's
            WaypointsData.enableRecheck();

            // Ensure all icons are ready for use.
            IconSetFileHandler.initialize();

            // Ensure all themese are ready for use
            ThemeFileHandler.initialize();

            // Webserver
            JMServer.setEnabled(webMapProperties.enabled.get(), false);
            initialized = true;

            // threadLogging = getLogger().isTraceEnabled();
        }
        catch (Throwable t)
        {
            if (logger == null)
            {
                logger = LogManager.getLogger(JourneyMap.MOD_ID);
            }
            logger.error(LogFormatter.toString(t));
        }
        finally
        {
            logger.info("postInitialize EXIT, " + (timer == null ? "" : timer.stopAndReport()));
        }

        JMLogger.setLevelFromProperties();
    }

    public JMServer getJmServer()
    {
        return JMServer.getInstance();
    }

    /**
     * Toggles automapping
     *
     * @param enable
     */
    public void toggleTask(Class<? extends ITaskManager> managerClass, boolean enable, Object params)
    {
        if (multithreadTaskController != null)
        {
            multithreadTaskController.toggleTask(managerClass, enable, params);
        }
    }

    /**
     * Checks whether a task manager is managerEnabled.
     *
     * @param managerClass
     * @return
     */
    public boolean isTaskManagerEnabled(Class<? extends ITaskManager> managerClass)
    {
        if (multithreadTaskController != null)
        {
            return multithreadTaskController.isTaskManagerEnabled(managerClass);
        }
        else
        {
            return false;
        }
    }

    /**
     * Starts mapping threads
     */
    public void startMapping()
    {
        synchronized (this)
        {
            if (mc == null || mc.theWorld == null || !initialized || !coreProperties.mappingEnabled.get())
            {
                return;
            }

            if (modInfo != null)
            {
                modInfo.reportAppView();
                //lastModInfoKeepAlive = System.currentTimeMillis();
            }

            File worldDir = FileHandler.getJMWorldDir(mc, currentWorldId);
            if (!worldDir.exists())
            {
                boolean created = worldDir.mkdirs();
                if (!created)
                {
                    JMLogger.logOnce("CANNOT CREATE DATA DIRECTORY FOR WORLD: " + worldDir.getPath(), null);
                    return;
                }
            }

            this.reset();
            ColorCache.instance().ensureCurrent();

            multithreadTaskController = new TaskController();
            multithreadTaskController.enableTasks();

            long totalMB = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long freeMB = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            String memory = String.format("Memory: %sMB total, %sMB free", totalMB, freeMB);
            logger.info(String.format("Mapping started in %s%sDIM%s. %s ", FileHandler.getJMWorldDir(mc, currentWorldId),
                    File.separator,
                    mc.theWorld.provider.dimensionId,
                    memory));
        }
    }

    /**
     * Halts mapping threads, clears caches.
     */
    public void stopMapping()
    {
        synchronized (this)
        {
            if ((isMapping()) && mc != null)
            {
                String dim = ".";
                if (mc.theWorld != null && mc.theWorld.provider != null)
                {
                    dim = " dimension " + mc.theWorld.provider.dimensionId + ".";
                }
                logger.info(String.format("Mapping halted in %s%sDIM%s", FileHandler.getJMWorldDir(mc, currentWorldId), File.separator, mc.theWorld.provider.dimensionId));
            }

            if (multithreadTaskController != null)
            {
                multithreadTaskController.disableTasks();
                multithreadTaskController.clear();
                multithreadTaskController = null;
            }
        }
    }

    private void reset()
    {
        if (mc != null && mc.thePlayer != null)
        {
            playerName = mc.thePlayer.getCommandSenderName();
        }

        if (!mc.isSingleplayer() && currentWorldId == null)
        {
            WorldInfoHandler.requestWorldID();
        }

        loadConfigProperties();
        DataCache.instance().purge();
        chunkRenderController = new ChunkRenderController();
        Fullscreen.reset();
        Fullscreen.state().follow.set(true);
        StatTimer.resetAll();
        TileDrawStepCache.clear();
        UIManager.getInstance().getMiniMap().reset();
        UIManager.getInstance().reset();
        WaypointStore.instance().reset();
    }

    public void queueMainThreadTask(IMainThreadTask task)
    {
        mainThreadTaskController.addTask(task);
    }

    public void performMainThreadTasks()
    {
        try
        {
            mainThreadTaskController.performTasks();
        }
        catch (Throwable t)
        {
            String error = "Error in JourneyMap.performMainThreadTasks(): " + t.getMessage();
            ChatLog.announceError(error);
            logger.error(LogFormatter.toString(t));
        }
    }

    public void performMultithreadTasks()
    {
        try
        {
            if (isMapping())
            {
                multithreadTaskController.performTasks();
            }
        }
        catch (Throwable t)
        {
            String error = "Error in JourneyMap.performMultithreadTasks(): " + t.getMessage();
            ChatLog.announceError(error);
            logger.error(LogFormatter.toString(t));
        }
    }

    public ChunkRenderController getChunkRenderController()
    {
        return chunkRenderController;
    }

    public void loadConfigProperties()
    {
        coreProperties = PropertiesBase.reload(coreProperties, CoreProperties.class);
        fullMapProperties = PropertiesBase.reload(fullMapProperties, FullMapProperties.class);
        miniMapProperties1 = PropertiesBase.reload(miniMapProperties1, MiniMapProperties.class);
        miniMapProperties2 = PropertiesBase.reload(miniMapProperties2, MiniMapProperties2.class);
        webMapProperties = PropertiesBase.reload(webMapProperties, WebMapProperties.class);
        waypointProperties = PropertiesBase.reload(waypointProperties, WaypointProperties.class);
    }

    public String getCurrentWorldId()
    {
        return this.currentWorldId;
    }

    public void setCurrentWorldId(String worldId)
    {
        synchronized (this)
        {
            File currentWorldDirectory = FileHandler.getJMWorldDirForWorldId(mc, currentWorldId);
            File newWorldDirectory = FileHandler.getJMWorldDir(mc, worldId);

            boolean worldIdUnchanged = Constants.safeEqual(worldId, currentWorldId);
            boolean directoryUnchanged = currentWorldDirectory != null && newWorldDirectory != null && currentWorldDirectory.getPath().equals(newWorldDirectory.getPath());

            if (worldIdUnchanged && directoryUnchanged && worldId != null)
            {
                getLogger().info("World UID hasn't changed: " + worldId);
                return;
            }

            boolean wasMapping = isMapping();
            if (wasMapping)
            {
                stopMapping();
            }

            this.currentWorldId = worldId;
            getLogger().info("World UID is set to: " + worldId);
        }
    }

    public String getPlayerName()
    {
        return playerName;
    }
}
