/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client;

import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.impl.IMCHandler;
import journeymap.client.api.util.PluginHelper;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.cartography.color.ColorPalette;
import journeymap.client.data.DataCache;
import journeymap.client.data.WaypointsData;
import journeymap.client.forge.event.EventHandlerManager;
import journeymap.client.io.FileHandler;
import journeymap.client.io.IconSetFileHandler;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.ChatLog;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.client.model.RegionImageCache;
import journeymap.client.network.WorldInfoHandler;
import journeymap.client.properties.*;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.client.service.WebServer;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.task.main.MainTaskController;
import journeymap.client.task.main.MappingMonitorTask;
import journeymap.client.task.multi.ITaskManager;
import journeymap.client.task.multi.TaskController;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.waypoint.WaypointStore;
import journeymap.client.world.ChunkMonitor;
import journeymap.common.CommonProxy;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.migrate.Migration;
import journeymap.common.network.PacketHandler;
import journeymap.common.version.VersionCheck;
import modinfo.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * Client-side, strong-side!
 */
@SideOnly(Side.CLIENT)
public class JourneymapClient implements CommonProxy
{
    /**
     * The constant FULL_VERSION.
     */
    public static final String FULL_VERSION = Journeymap.MC_VERSION + "-" + Journeymap.JM_VERSION;
    /**
     * The constant MOD_NAME.
     */
    public static final String MOD_NAME = Journeymap.SHORT_MOD_NAME + " " + FULL_VERSION;
    private boolean serverEnabled = false;
    private boolean serverTeleportEnabled = false;

    // Properties & preferences
    private volatile CoreProperties coreProperties;
    private volatile FullMapProperties fullMapProperties;
    private volatile MiniMapProperties miniMapProperties1;
    private volatile MiniMapProperties miniMapProperties2;
    private volatile TopoProperties topoProperties;
    private volatile WebMapProperties webMapProperties;
    private volatile WaypointProperties waypointProperties;
    private volatile Boolean initialized = false;
    private volatile String currentWorldId = null;
    //private final Minecraft mc;
    private Logger logger;
    private boolean threadLogging = false;

    // Main thread tasks
    private final MainTaskController mainThreadTaskController = new MainTaskController();

    // Multithreaded tasks
    private TaskController multithreadTaskController;

    private ChunkRenderController chunkRenderController;

    /**
     * Constructor.
     */
    public JourneymapClient()
    {
    }

    /**
     * Get the core properties.
     *
     * @return the core properties
     */
    public CoreProperties getCoreProperties()
    {
        return coreProperties;
    }

    /**
     * Get the fullmap properties.
     *
     * @return the full map properties
     */
    public FullMapProperties getFullMapProperties()
    {
        return fullMapProperties;
    }

    /**
     * Get the core properties.
     *
     * @return the topo properties
     */
    public TopoProperties getTopoProperties()
    {
        return topoProperties;
    }

    /**
     * Disable the mod.
     */
    public void disable()
    {
        initialized = false;
        EventHandlerManager.unregisterAll();
        stopMapping();
        ClientAPI.INSTANCE.purge();
        DataCache.INSTANCE.purge();
    }

    /**
     * Get the minimap properties for the active minimap.
     *
     * @param which the which
     * @return the mini map properties
     */
    public MiniMapProperties getMiniMapProperties(int which)
    {
        switch (which)
        {
            case 2:
            {
                miniMapProperties2.setActive(true);
                miniMapProperties1.setActive(false);
                return getMiniMapProperties2();
            }
            default:
            {
                miniMapProperties1.setActive(true);
                miniMapProperties2.setActive(false);
                return getMiniMapProperties1();
            }
        }
    }

    /**
     * Get the active minimap id.
     *
     * @return the active minimap id
     */
    public int getActiveMinimapId()
    {
        if (miniMapProperties1.isActive())
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }

    /**
     * Get properties for minimap 1.
     *
     * @return the mini map properties 1
     */
    public MiniMapProperties getMiniMapProperties1()
    {
        return miniMapProperties1;
    }

    /**
     * Get properties for minimap 2.
     *
     * @return the mini map properties 2
     */
    public MiniMapProperties getMiniMapProperties2()
    {
        return miniMapProperties2;
    }

    /**
     * Get the webmap properties.
     *
     * @return the web map properties
     */
    public WebMapProperties getWebMapProperties()
    {
        return webMapProperties;
    }

    /**
     * Get the waypoint properties.
     *
     * @return the waypoint properties
     */
    public WaypointProperties getWaypointProperties()
    {
        return waypointProperties;
    }

    /**
     * Pre-initialize the side.
     *
     * @param event
     * @throws Throwable
     */
    @Override
    public void preInitialize(FMLPreInitializationEvent event) throws Throwable
    {
        try
        {
            PluginHelper.INSTANCE.preInitPlugins(event);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Initialize the client.
     *
     * @param event
     * @throws Throwable
     */
    @Override
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        PacketHandler.init(Side.CLIENT);
        StatTimer timer = null;
        try
        {
            timer = StatTimer.getDisposable("elapsed").start();

            // Migrate tasks
            boolean migrationOk = new Migration("journeymap.client.task.migrate").performTasks();

            // Ensure logger inits
            logger = JMLogger.init();
            logger.info("initialize ENTER");

            if (initialized)
            {
                logger.warn("Already initialized, aborting");
                return;
            }

            // Trigger statics on EntityList (may not be needed anymore?)
            EntityRegistry.instance();

            // Load properties
            loadConfigProperties();

            // Log properties
            JMLogger.logProperties();

            // Logging for thread debugging
            threadLogging = false;

            // Init Plugins
            PluginHelper.INSTANCE.initPlugins(event, ClientAPI.INSTANCE);

            logger.info("initialize EXIT, " + (timer == null ? "" : timer.getLogReportString()));
        }
        catch (Throwable t)
        {
            if (logger == null)
            {
                logger = LogManager.getLogger(Journeymap.MOD_ID);
            }
            logger.error(LogFormatter.toString(t));
            throw t;
        }
    }

    /**
     * Post-initialize the client.
     *
     * @param event
     */
    @Override
    public void postInitialize(FMLPostInitializationEvent event)
    {
        StatTimer timer = null;
        try
        {
            logger.debug("postInitialize ENTER");
            timer = StatTimer.getDisposable("elapsed").start();

            // Main thread task controller
            queueMainThreadTask(new MappingMonitorTask());

            // Register general event handlers
            EventHandlerManager.registerGeneralHandlers();
            EventHandlerManager.registerGuiHandlers();

            // Resets detection results of Voxel/Rei's
            WaypointsData.enableRecheck();

            // Ensure all icons are ready for use.
            IconSetFileHandler.initialize();

            // Ensure all themes are ready for use
            ThemeFileHandler.initialize(true);

            // Webserver
            WebServer.setEnabled(webMapProperties.enabled.get(), false);
            initialized = true;

            VersionCheck.getVersionAvailable();

            // ModInfo with a single ping
            ModInfo modInfo = new ModInfo("UA-28839029-5", "en_US", Journeymap.MOD_ID, MOD_NAME, FULL_VERSION, false);
            modInfo.reportAppView();

            // threadLogging = getLogger().isTraceEnabled();
        }
        catch (Throwable t)
        {
            if (logger == null)
            {
                logger = LogManager.getLogger(Journeymap.MOD_ID);
            }
            logger.error(LogFormatter.toString(t));
        }
        finally
        {
            logger.debug("postInitialize EXIT, " + (timer == null ? "" : timer.stopAndReport()));
        }

        JMLogger.setLevelFromProperties();
    }

    /**
     * Accept any modlist on server
     *
     * @param modList
     * @param side
     * @return
     */
    @Override
    public boolean checkModLists(Map<String, String> modList, Side side)
    {
        // TODO: Check for JMServer and enable/disable worldid checking, etc.
        return true;
    }

    @Override
    public boolean isUpdateCheckEnabled()
    {
        return getCoreProperties().checkUpdates.get();
    }

    /**
     * Handle imc.
     *
     * @param event the event
     */
    @Mod.EventHandler
    public void handleIMC(FMLInterModComms.IMCEvent event)
    {
        IMCHandler.handle(event);
    }

    /**
     * Whether the instance is initialized.
     *
     * @return boolean
     */
    public Boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Whether the client is mapping.
     *
     * @return boolean
     */
    public Boolean isMapping()
    {
        return initialized && multithreadTaskController != null && multithreadTaskController.isActive();
    }

    /**
     * Whether thread logging is enabled.
     *
     * @return boolean
     */
    public Boolean isThreadLogging()
    {
        return threadLogging;
    }

    /**
     * Initialize the webserver
     *
     * @return jm server
     */
    public WebServer getJmServer()
    {
        return WebServer.getInstance();
    }

    /**
     * Queue a Runnable on the multithreaded task controller.
     *
     * @param runnable the runnable
     * @throws Exception the exception
     */
    public void queueOneOff(Runnable runnable) throws Exception
    {
        if (multithreadTaskController != null)
        {
            multithreadTaskController.queueOneOff(runnable);
        }
    }

    /**
     * Toggles a recurring task
     *
     * @param managerClass the manager class
     * @param enable       the enable
     * @param params       the params
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
     * @param managerClass the manager class
     * @return boolean
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
            Minecraft mc = FMLClientHandler.instance().getClient();

            if (mc == null || mc.world == null || !initialized || !coreProperties.mappingEnabled.get())
            {
                return;
            }

            File worldDir = FileHandler.getJMWorldDir(mc, currentWorldId);
            if (worldDir == null)
            {
                return;
            }

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

            multithreadTaskController = new TaskController();
            multithreadTaskController.enableTasks();

            long totalMB = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long freeMB = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            String memory = String.format("Memory: %sMB total, %sMB free", totalMB, freeMB);
            int dimension = mc.world.provider.getDimension();
            logger.info(String.format("Mapping started in %s%sDIM%s. %s ", FileHandler.getJMWorldDir(mc, currentWorldId),
                    File.separator,
                    dimension,
                    memory));

            ClientAPI.INSTANCE.getClientEventManager().fireMappingEvent(true, dimension);
            UIManager.INSTANCE.getMiniMap().reset();
        }
    }

    /**
     * Halts mapping threads, clears caches.
     */
    public void stopMapping()
    {
        synchronized (this)
        {
            ChunkMonitor.INSTANCE.reset();

            Minecraft mc = FMLClientHandler.instance().getClient();
            if ((isMapping()) && mc != null)
            {
                logger.info(String.format("Mapping halted in %s%sDIM%s", FileHandler.getJMWorldDir(mc, currentWorldId),
                        File.separator, mc.world.provider.getDimension()));
                RegionImageCache.INSTANCE.flushToDiskAsync(true);

                ColorPalette colorPalette = ColorPalette.getActiveColorPalette();
                if (colorPalette != null)
                {
                    colorPalette.writeToFile();
                }
            }

            if (multithreadTaskController != null)
            {
                multithreadTaskController.disableTasks();
                multithreadTaskController.clear();
                multithreadTaskController = null;
            }

            if (mc != null)
            {
                int dimension = mc.world != null ? mc.world.provider.getDimension() : 0;
                ClientAPI.INSTANCE.getClientEventManager().fireMappingEvent(false, dimension);
            }
        }
    }

    /**
     * Reset state on everything in the client.
     */
    private void reset()
    {
        if (!FMLClientHandler.instance().getClient().isSingleplayer() && currentWorldId == null)
        {
            WorldInfoHandler.requestWorldID();
        }

        loadConfigProperties();
        DataCache.INSTANCE.purge();
        ChunkMonitor.INSTANCE.reset();
        chunkRenderController = new ChunkRenderController();
        Fullscreen.state().requireRefresh();
        Fullscreen.state().follow.set(true);
        StatTimer.resetAll();
        TileDrawStepCache.clear();
        UIManager.INSTANCE.getMiniMap().reset();
        UIManager.INSTANCE.reset();
        WaypointStore.INSTANCE.reset();
    }

    /**
     * Queue a task that has to be run on the main thread.
     *
     * @param task the task
     */
    public void queueMainThreadTask(IMainThreadTask task)
    {
        mainThreadTaskController.addTask(task);
    }

    /**
     * Perform tasks on the main thread.
     */
    public void performMainThreadTasks()
    {
        mainThreadTaskController.performTasks();
    }

    /**
     * Perform tasks on other threads.
     */
    public void performMultithreadTasks()
    {
        try
        {
            synchronized (this)
            {
                if (isMapping())
                {
                    multithreadTaskController.performTasks();
                }
            }
        }
        catch (Throwable t)
        {
            String error = "Error in JourneyMap.performMultithreadTasks(): " + t.getMessage();
            ChatLog.announceError(error);
            logger.error(LogFormatter.toString(t));
        }
    }

    /**
     * Get the chunk render controller   May be null.
     *
     * @return chunk render controller
     */
    public ChunkRenderController getChunkRenderController()
    {
        return chunkRenderController;
    }

    /**
     * (Re)load all the properties from their files.
     */
    public void saveConfigProperties()
    {
        if (coreProperties != null)
        {
            coreProperties.save();
        }
        if (fullMapProperties != null)
        {
            fullMapProperties.save();
        }
        if (miniMapProperties1 != null)
        {
            miniMapProperties1.save();
        }
        if (miniMapProperties2 != null)
        {
            miniMapProperties2.save();
        }
        if (miniMapProperties2 != null)
        {
            miniMapProperties2.save();
        }
        if (topoProperties != null)
        {
            topoProperties.save();
        }
        if (webMapProperties != null)
        {
            webMapProperties.save();
        }
        if (waypointProperties != null)
        {
            waypointProperties.save();
        }
    }

    /**
     * (Re)load all the properties from their files.
     */
    public void loadConfigProperties()
    {
        saveConfigProperties();
        coreProperties = new CoreProperties().load();
        fullMapProperties = new FullMapProperties().load();
        miniMapProperties1 = new MiniMapProperties(1).load();
        miniMapProperties2 = new MiniMapProperties(2).load();
        topoProperties = new TopoProperties().load();
        webMapProperties = new WebMapProperties().load();
        waypointProperties = new WaypointProperties().load();
    }

    /**
     * Handling of the worldIdPacket message.
     *
     * @param worldId
     * @param playerEntity
     */
    @Override
    public void handleWorldIdMessage(String worldId, EntityPlayerMP playerEntity)
    {
        setCurrentWorldId(worldId);
    }

    /**
     * Get the current world id.  May be null.
     *
     * @return current world id
     */
    public String getCurrentWorldId()
    {
        return this.currentWorldId;
    }

    /**
     * Set the current world id.
     *
     * @param worldId the world id
     */
    public void setCurrentWorldId(String worldId)
    {
        synchronized (this)
        {
            Minecraft mc = FMLClientHandler.instance().getClient();
            File currentWorldDirectory = FileHandler.getJMWorldDirForWorldId(mc, currentWorldId);
            File newWorldDirectory = FileHandler.getJMWorldDir(mc, worldId);

            boolean worldIdUnchanged = Constants.safeEqual(worldId, currentWorldId);
            boolean directoryUnchanged = currentWorldDirectory != null && newWorldDirectory != null && currentWorldDirectory.getPath().equals(newWorldDirectory.getPath());

            if (worldIdUnchanged && directoryUnchanged && worldId != null)
            {
                Journeymap.getLogger().info("World UID hasn't changed: " + worldId);
                return;
            }

            boolean wasMapping = isMapping();
            if (wasMapping)
            {
                stopMapping();
            }

            this.currentWorldId = worldId;
            Journeymap.getLogger().info("World UID is set to: " + worldId);
        }
    }

    /**
     * Is server enabled boolean.
     *
     * @return the boolean
     */
    public boolean isServerEnabled()
    {
        return serverEnabled;
    }

    /**
     * Sets server enabled.
     *
     * @param serverEnabled the server enabled
     */
    public void setServerEnabled(boolean serverEnabled)
    {
        this.serverEnabled = serverEnabled;
    }

    /**
     * Is server teleport enabled boolean.
     *
     * @return the boolean
     */
    public boolean isServerTeleportEnabled()
    {
        return serverTeleportEnabled;
    }

    /**
     * Sets server teleport enabled.
     *
     * @param serverTeleportEnabled the server teleport enabled
     */
    public void setServerTeleportEnabled(boolean serverTeleportEnabled)
    {
        this.serverTeleportEnabled = serverTeleportEnabled;
    }
}
