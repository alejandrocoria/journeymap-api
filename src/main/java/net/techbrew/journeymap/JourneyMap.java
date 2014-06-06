package net.techbrew.journeymap;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import modinfo.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
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
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.*;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.server.JMServer;
import net.techbrew.journeymap.task.ITaskManager;
import net.techbrew.journeymap.task.MapPlayerTask;
import net.techbrew.journeymap.task.TaskController;
import net.techbrew.journeymap.thread.JMThreadFactory;
import net.techbrew.journeymap.thread.TaskThread;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    static final String VERSION_URL = "https://dl.dropboxusercontent.com/u/38077766/JourneyMap/journeymap-version.js"; //$NON-NLS-1$

    public static final String WEBSITE_URL = "http://journeymap.techbrew.net/"; //$NON-NLS-1$
    public static final String JM_VERSION = "@JMVERSION@"; //$NON-NLS-1$
    public static final String MC_VERSION = "@MCVERSION@"; //$NON-NLS-1$

    public static final String EDITION = getEdition();
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";
    public static final String MOD_NAME = SHORT_MOD_NAME + " " + EDITION;

    private static JourneyMap INSTANCE;

    public static JourneyMap getInstance()
    {
        return INSTANCE;
    }

    private Logger logger;

    private volatile Boolean initialized = false;

    private boolean enableAnnounceMod = false;
    private JMServer jmServer;

    private boolean threadLogging = false;

    // Time stamp of next chunk update
    public long nextPlayerUpdate = 0;
    public long nextChunkUpdate = 0;

    public ModInfo modInfo;

    // Properties & preferences
    public CoreProperties coreProperties;
    public FullMapProperties fullMapProperties;
    public MiniMapProperties miniMapProperties;
    public WebMapProperties webMapProperties;
    public WaypointProperties waypointProperties;

    // Executor for task threads
    private volatile ScheduledExecutorService taskExecutor;

    // Task controller for issuing tasks in executor
    private TaskController taskController;

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

    private static String getEdition()
    {
        String ed = null;
        try
        {
            ed = JM_VERSION + " " + FeatureManager.getFeatureSetName();
        }
        catch (Throwable t)
        {
            ed = JM_VERSION + " ?";
            t.printStackTrace(System.err);
        }
        return ed;
    }

    public Boolean isInitialized()
    {
        return initialized;
    }

    public Boolean isMapping()
    {
        return taskExecutor != null && !taskExecutor.isShutdown();
    }

    public Boolean isThreadLogging()
    {
        return threadLogging;
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        try
        {
            // Ensure logger inits
            logger = JMLogger.init();
            logger.info(JourneyMap.MOD_NAME + " initialize ENTER");

            modInfo = new ModInfo("UA-28839029-4", "en_US", MOD_ID, MOD_NAME, getEdition());

            if (initialized)
            {
                logger.warning("Already initialized, aborting");
                return;
            }

            mc = FMLClientHandler.instance().getClient();

            // Load properties
            coreProperties = new CoreProperties().load();
            fullMapProperties = new FullMapProperties().load();
            miniMapProperties = new MiniMapProperties().load();
            webMapProperties = new WebMapProperties().load();
            waypointProperties = new WaypointProperties().load();
            PropertyManager.getInstance().migrateLegacyProperties();

            // Log properties
            JMLogger.logProperties();

            logger.info(JourneyMap.MOD_NAME + " initialize EXIT");
        }
        catch (Throwable t)
        {
            if (logger == null)
            {
                logger = Logger.getLogger(MOD_ID);
            }
            logger.severe(LogFormatter.toString(t));
            throw t;
        }
    }

    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event)
    {
        try
        {
            logger.info(JourneyMap.MOD_NAME + " postInitialize ENTER");

            // Register general event handlers
            EventHandlerManager.registerGeneralHandlers();
            EventHandlerManager.registerGuiHandlers();

            // Webserver
            toggleWebserver(webMapProperties.enabled.get(), false);

            // Announce mod?
            enableAnnounceMod = coreProperties.announceMod.get();

            // Check for newer version online
            if (VersionCheck.getVersionIsCurrent() == false)
            {
                ChatLog.announceI18N(Constants.getString("JourneyMap.new_version_available", "")); //$NON-NLS-1$
                ChatLog.announceURL(WEBSITE_URL, WEBSITE_URL);
            }

            //BlockUtils.initialize();

            initialized = true;

            // Override log level now that loading complete
            logger.info("Initialization complete."); //$NON-NLS-1$
            JMLogger.setLevelFromProps();

            // Logging for thread debugging
            threadLogging = getLogger().isLoggable(Level.FINER);

            WaypointsData.reset();
            BlockUtils.initialize();

            announceMod(false);

            logger.info(JourneyMap.MOD_NAME + " postInitialize EXIT");
        }
        catch (Throwable t)
        {
            if (logger == null)
            {
                logger = Logger.getLogger(MOD_ID);
            }
            logger.severe(LogFormatter.toString(t));
        }
    }

    public void toggleWebserver(Boolean enable, boolean forceAnnounce)
    {

        webMapProperties.enabled.set(enable);
        waypointProperties.save();

        if (enable)
        {
            try
            {
                jmServer = new JMServer();
                if (jmServer.isReady())
                {
                    jmServer.start();
                }
                else
                {
                    enable = false;
                }
            }
            catch (Throwable e)
            {
                logger.log(Level.SEVERE, LogFormatter.toString(e));
                enable = false;
            }
            if (!enable)
            {
                ChatLog.announceError(Constants.getMessageJMERR24());
            }
        }
        else
        {
            try
            {
                if (jmServer != null)
                {
                    jmServer.stop();
                }
            }
            catch (Throwable e)
            {
                logger.log(Level.SEVERE, LogFormatter.toString(e));
            }
        }
        if (forceAnnounce)
        {
            enableAnnounceMod = true;
        }
        announceMod(forceAnnounce);
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
            logger.warning("taskController not available");
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
            logger.warning("taskController not available");
            return false;
        }
    }

    public ModInfo getModInfo()
    {
        return this.modInfo;
    }

    /**
     * Starts mapping threads
     */
    public void startMapping()
    {
        synchronized (this)
        {

            if (mc.theWorld == null)
            {
                return;
            }

            modInfo.reportAppView();

            this.reset();

            if (taskExecutor == null || taskExecutor.isShutdown())
            {
                taskExecutor = Executors.newScheduledThreadPool(1, new JMThreadFactory("task")); //$NON-NLS-1$
            }
            else
            {
                logger.severe("TaskExecutor in an unexpected state.  Should be null or shutdown.");
            }

            taskController = new TaskController();
            taskController.enableTasks(mc);

            logger.info("Mapping started in " + WorldData.getWorldName(mc) + " dimension " + mc.theWorld.provider.dimensionId + "."); //$NON-NLS-1$
        }
    }

    /**
     * Halts mapping threads, clears caches.
     */
    public void stopMapping()
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        synchronized (this)
        {

            if (taskExecutor != null || taskController != null)
            {
                String dim = ".";
                if (minecraft.theWorld != null && minecraft.theWorld.provider != null)
                {
                    dim = " dimension " + minecraft.theWorld.provider.dimensionId + "."; //$NON-NLS-1$ //$NON-NLS-2$
                }
                logger.info("Mapping halting in " + WorldData.getWorldName(minecraft) + dim); //$NON-NLS-1$
            }

            if (taskExecutor != null && !taskExecutor.isShutdown())
            {
                taskExecutor.shutdown();
                taskExecutor = null;
            }

            if (taskController != null)
            {
                taskController.disableTasks(minecraft);
                taskController.clear();
                taskController = null;
            }
        }
    }

    private void reset()
    {
        FileHandler.lastJMWorldDir = null;
        //BlockMD.clearCache();
        DataCache.instance().purge();
        MapOverlay.state().follow=true;
        ColorCache.getInstance().reset();
        BlockUtils.initialize();
        DataCache.instance().purge();
        MapPlayerTask.clearCache();
        StatTimer.resetAll();
        TaskThread.reset();
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

            final boolean isDead = mc.currentScreen != null && mc.currentScreen instanceof GuiGameOver;
            if(mc.thePlayer!=null && isDead && isMapping())
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
            logger.severe(Constants.getMessageJMERR00(LogFormatter.toString(t)));
        }
    }

    public void performTasks()
    {
        try
        {
            if (isMapping())
            {
                taskController.performTasks(mc, taskExecutor);
            }
        }
        catch (Throwable t)
        {
            String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
            ChatLog.announceError(error);
            logger.severe(LogFormatter.toString(t));
        }
    }

    private void announceMod(boolean forced)
    {
        if (enableAnnounceMod)
        {
            ChatLog.announceI18N("JourneyMap.ready", MOD_NAME); //$NON-NLS-1$
            if (webMapProperties.enabled.get())
            {
                String keyName = Constants.getKeyName(Constants.KB_MAP);
                String port = jmServer.getPort() == 80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
                String message = Constants.getString("JourneyMap.webserver_and_mapgui_ready", keyName, port); //$NON-NLS-1$
                ChatLog.announceURL(message, "http://localhost" + port); //$NON-NLS-1$
            }
            else
            {
                String keyName = Constants.getKeyName(Constants.KB_MAP); // Should be KeyCode
                ChatLog.announceI18N("JourneyMap.mapgui_only_ready", keyName); //$NON-NLS-1$
            }
            enableAnnounceMod = false; // Only queueAnnouncement mod once per runtime
        }
    }

    public ScheduledExecutorService getChunkExecutor()
    {
        return taskExecutor;
    }

    /**
     * @return
     */
    public static Logger getLogger()
    {
        return INSTANCE.logger;
    }

}
