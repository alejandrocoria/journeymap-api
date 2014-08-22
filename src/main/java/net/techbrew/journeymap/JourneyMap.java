/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import modinfo.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.forgehandler.EventHandlerManager;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.*;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.server.JMServer;
import net.techbrew.journeymap.task.ITaskManager;
import net.techbrew.journeymap.task.TaskController;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
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
@Mod(modid = JourneyMap.MOD_ID, name = JourneyMap.SHORT_MOD_NAME, version = JourneyMap.JM_VERSION)
public class JourneyMap
{
    public static final String WEBSITE_URL = "http://journeymap.techbrew.net/"; //$NON-NLS-1$
    public static final String JM_VERSION = "@JMVERSION@"; //$NON-NLS-1$
    public static final String FORGE_VERSION = "@FORGEVERSION@"; //$NON-NLS-1$
    public static final String EDITION = getEdition();
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";
    public static final String MOD_NAME = SHORT_MOD_NAME + " " + EDITION;
    static final String VERSION_URL = "https://docs.google.com/uc?id=0B-PlFsIS9WoCWGNLWUZPSl9KNHc"; //$NON-NLS-1$
    private static JourneyMap INSTANCE;

    public ModInfo modInfo;

    // Properties & preferences
    private volatile CoreProperties coreProperties;
    private volatile FullMapProperties fullMapProperties;
    private volatile MiniMapProperties miniMapProperties;
    private volatile WebMapProperties webMapProperties;
    private volatile WaypointProperties waypointProperties;
    private volatile Boolean initialized = false;
    private volatile String currentWorldId = null;
    private Logger logger;
    private JMServer jmServer;
    private boolean threadLogging = false;
    private long lastModInfoKeepAlive = System.currentTimeMillis();

    // Task controller for issuing tasks in executor
    private TaskController taskController;
    private ChunkRenderController chunkRenderController;
    private Minecraft mc;

    /**
     * Constructor.
     */
    public JourneyMap()
    {
        if (INSTANCE != null)
        {
            throw new IllegalArgumentException("Use getInstance() after initialization is complete");
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

    private static String getVersion()
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
        return INSTANCE.logger == null ? LogManager.getLogger(JourneyMap.MOD_ID) : INSTANCE.logger;
    }

    public Boolean isInitialized()
    {
        return initialized;
    }

    public Boolean isMapping()
    {
        return taskController != null && taskController.isMapping();
    }

    public Boolean isThreadLogging()
    {
        return threadLogging;
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        StatTimer timer = null;
        try
        {
            timer = StatTimer.getDisposable("elapsed").start();

            // Ensure logger inits
            logger = JMLogger.init();
            logger.info("initialize ENTER");

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
            PropertyManager.getInstance().migrateLegacyProperties();

            // Log properties
            JMLogger.logProperties();

            // Logging for thread debugging
            threadLogging = false;

            logger.info("initialize EXIT, " + (timer == null ? "" : timer.stopAndReport()));
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

    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event)
    {
        StatTimer timer = null;
        try
        {
            logger.info("postInitialize ENTER");
            timer = StatTimer.getDisposable("elapsed").start();

            // Register general event handlers
            EventHandlerManager.registerGeneralHandlers();
            EventHandlerManager.registerGuiHandlers();

            // Resets detection results of Voxel/Rei's
            WaypointsData.reset();

            // Now that all blocks should be registered, init BlockUtils
            // BlockUtils.initialize();

            // Ensure all mob icons files are ready for use.
            FileHandler.initMobIconSets();

            // Webserver
            jmServer = JMServer.setEnabled(jmServer, webMapProperties.enabled.get(), false);
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
    }

    public JMServer getJmServer()
    {
        return jmServer;
    }

    public void toggleWebserver(boolean enable, boolean announce)
    {
        jmServer = JMServer.setEnabled(jmServer, enable, announce);
    }

    /**
     * Toggles automapping
     *
     * @param enable
     */
    public void toggleTask(Class<? extends ITaskManager> managerClass, boolean enable, Object params)
    {
        if (taskController != null)
        {
            taskController.toggleTask(managerClass, enable, params);
        }
        else
        {
            logger.warn("taskController not available");
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
        if (taskController != null)
        {
            return taskController.isTaskManagerEnabled(managerClass);
        }
        else
        {
            logger.warn("taskController not available");
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
            if (mc == null || mc.theWorld == null)
            {
                return;
            }

            if (modInfo != null)
            {
                modInfo.reportAppView();
                lastModInfoKeepAlive = System.currentTimeMillis();
            }

            this.reset();
            ColorCache.getInstance().prefetchResourcePackColors();

            taskController = new TaskController();
            taskController.enableTasks();

            logger.info(String.format("Mapping started in %s%sDIM%s", FileHandler.getJMWorldDir(mc, currentWorldId), File.separator, mc.theWorld.provider.dimensionId)); //$NON-NLS-1$
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
                    dim = " dimension " + mc.theWorld.provider.dimensionId + "."; //$NON-NLS-1$ //$NON-NLS-2$
                }
                logger.info(String.format("Mapping halted in %s%sDIM%s", FileHandler.getJMWorldDir(mc, currentWorldId), File.separator, mc.theWorld.provider.dimensionId)); //$NON-NLS-1$
            }

            if (taskController != null)
            {
                taskController.disableTasks();
                taskController.clear();
                taskController = null;
            }

            currentWorldId = null;
        }
    }

    private void reset()
    {
        loadConfigProperties();
        DataCache.instance().purge();
        DataCache.instance().resetBlockMetadata();
        chunkRenderController = new ChunkRenderController();
        MapOverlay.state().follow = true;
        ColorCache.getInstance().reset();
        StatTimer.resetAll();
        TextureCache.instance().purge();
        TileCache.instance().invalidateAll();
        RegionImageCache.getInstance().flushToDisk();
        RegionImageCache.getInstance().clear();
        UIManager.getInstance().reset();
        WaypointStore.instance().reset();

        if (waypointProperties.managerEnabled.get())
        {
            WaypointStore.instance().load();
        }
    }

    public void updateState()
    {
        try
        {
            if (mc == null)
            {
                mc = FMLClientHandler.instance().getClient();
            }

//            if (modInfo != null)
//            {
//                if (System.currentTimeMillis() - lastModInfoKeepAlive > 600000) // 10 minutes
//                {
//                    lastModInfoKeepAlive = System.currentTimeMillis();
//                    modInfo.keepAlive();
//                }
//            }

            final boolean isDead = mc.currentScreen != null && mc.currentScreen instanceof GuiGameOver;
            if (mc.thePlayer != null && isDead && isMapping())
            {
                stopMapping();
                if (waypointProperties.managerEnabled.get())
                {
                    WaypointStore.instance().save(Waypoint.deathOf(mc.thePlayer));
                }
                return;
            }

            if (mc.theWorld == null)
            {
                if (isMapping())
                {
                    stopMapping();
                }
                return;
            }
            else
            {
                if (!isMapping() && !isDead)
                {
                    startMapping();
                }
            }

            final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof MapOverlay);
            if (isGamePaused)
            {
                TileCache.pause();

                if (!isMapping())
                {
                    return;
                }

                // TODO: Does this happen ever?
                GuiScreen guiScreen = mc.currentScreen;
                if (guiScreen instanceof GuiMainMenu ||
                        guiScreen instanceof GuiSelectWorld ||
                        guiScreen instanceof GuiMultiplayer)
                {
                    stopMapping();
                    return;
                }
            }
            else
            {
                TileCache.resume();
            }

            TileCache.resume();

            // Show announcements
            if (!isGamePaused)
            {
                ChatLog.showChatAnnouncements(mc);
            }

            // We got this far
            if (!isMapping())
            {
                startMapping();
            }
        }
        catch (Throwable t)
        {
            logger.error(Constants.getMessageJMERR00(LogFormatter.toString(t)));
        }
    }

    public void performTasks()
    {
        try
        {
            if (isMapping())
            {
                taskController.performTasks();
            }
        }
        catch (Throwable t)
        {
            String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
            ChatLog.announceError(error);
            logger.error(LogFormatter.toString(t));
        }
    }

    public ChunkRenderController getChunkRenderController()
    {
        return chunkRenderController;
    }

    private void loadConfigProperties()
    {
        coreProperties = PropertiesBase.reload(coreProperties, CoreProperties.class);
        fullMapProperties = PropertiesBase.reload(fullMapProperties, FullMapProperties.class);
        miniMapProperties = PropertiesBase.reload(miniMapProperties, MiniMapProperties.class);
        webMapProperties = PropertiesBase.reload(webMapProperties, WebMapProperties.class);
        waypointProperties = PropertiesBase.reload(waypointProperties, WaypointProperties.class);
    }

    public static CoreProperties getCoreProperties()
    {
        return INSTANCE.coreProperties;
    }

    public static FullMapProperties getFullMapProperties()
    {
        return INSTANCE.fullMapProperties;
    }

    public static MiniMapProperties getMiniMapProperties()
    {
        return INSTANCE.miniMapProperties;
    }

    public static WebMapProperties getWebMapProperties()
    {
        return INSTANCE.webMapProperties;
    }

    public static WaypointProperties getWaypointProperties()
    {
        return INSTANCE.waypointProperties;
    }

    public String getCurrentWorldId()
    {
        return this.currentWorldId;
    }

    public void setCurrentWorldId(String worldId)
    {
        boolean unchanged = Constants.safeEqual(worldId, currentWorldId);
        if(unchanged)
        {
            getLogger().info("World UID hasn't changed: " + worldId);
            return;
        }

        boolean restartMapping = isMapping();

        if(restartMapping)
        {
            stopMapping();
        }

        this.currentWorldId = worldId;

        if(restartMapping)
        {
            startMapping();
        }
    }
}
