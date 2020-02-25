/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client;

import com.google.gson.JsonObject;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.impl.IMCHandler;
import journeymap.client.api.util.PluginHelper;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.cartography.color.ColorPalette;
import journeymap.client.data.DataCache;
import journeymap.client.forge.event.EventHandlerManager;
import journeymap.client.io.FileHandler;
import journeymap.client.io.IconSetFileHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.log.ChatLog;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.client.mod.impl.Pixelmon;
import journeymap.client.model.RegionImageCache;
import journeymap.client.properties.CoreProperties;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.properties.TopoProperties;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.properties.WebMapProperties;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.client.service.webmap.Webmap;
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
import journeymap.common.network.GetClientConfig;
import journeymap.common.version.VersionCheck;
import modinfo.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
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

import static journeymap.common.network.Constants.SETTINGS;
import static journeymap.common.network.Constants.WORLD_ID;

/**
 * Client-side, strong-side!
 */
@SideOnly(Side.CLIENT)
public class JourneymapClient implements CommonProxy
{
    public static final String FULL_VERSION = Journeymap.MC_VERSION + "-" + Journeymap.JM_VERSION;
    public static final String MOD_NAME = Journeymap.SHORT_MOD_NAME + " " + FULL_VERSION;
    private boolean journeyMapServerConnection = false;
    private boolean forgeServerConnection = false;
    private boolean playerTrackingEnabled = false;
    private boolean teleportEnabled = false;
    private boolean modInfoReported = false;
    private boolean serverAdmin = false;

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
     */
    public CoreProperties getCoreProperties()
    {
        return coreProperties;
    }

    /**
     * Get the fullmap properties.
     */
    public FullMapProperties getFullMapProperties()
    {
        return fullMapProperties;
    }

    /**
     * Get the core properties.
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

    public MiniMapProperties getActiveMiniMapProperties()
    {
        if (miniMapProperties1.isActive())
        {
            return getMiniMapProperties1();
        }
        else
        {
            return getMiniMapProperties2();
        }
    }

    /**
     * Get the active minimap id.
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
     */
    public MiniMapProperties getMiniMapProperties1()
    {
        return miniMapProperties1;
    }

    /**
     * Get properties for minimap 2.
     */
    public MiniMapProperties getMiniMapProperties2()
    {
        return miniMapProperties2;
    }

    /**
     * Get the webmap properties.
     */
    public WebMapProperties getWebMapProperties()
    {
        return webMapProperties;
    }

    /**
     * Get the waypoint properties.
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
            EventHandlerManager.registerHandlers();

            // Ensure all icons are ready for use.
            IconSetFileHandler.initialize();

            // Ensure all themes are ready for use
            ThemeLoader.initialize(true);

            // Webserver
            if (webMapProperties.enabled.get())
            {
                Webmap.INSTANCE.start();
            }

            ChatLog.announceMod(false);
            initialized = true;

            VersionCheck.getVersionAvailable();

            // Check if Pixelmon is loaded.
            String pixelmonModId = "Pixelmon";
            if (Loader.isModLoaded(pixelmonModId) || Loader.isModLoaded(pixelmonModId.toLowerCase()))
            {
                logger.info(pixelmonModId + " is loaded in class path. Initializing icon display.");
                new Pixelmon(true);
            }

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

    @Mod.EventHandler
    public void handleIMC(FMLInterModComms.IMCEvent event)
    {
        IMCHandler.handle(event);
    }

    /**
     * Whether the instance is initialized.
     *
     * @return
     */
    public Boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Whether the client is mapping.
     *
     * @return
     */
    public Boolean isMapping()
    {
        return initialized && multithreadTaskController != null && multithreadTaskController.isActive();
    }

    /**
     * Whether thread logging is enabled.
     *
     * @return
     */
    public Boolean isThreadLogging()
    {
        return threadLogging;
    }

    /**
     * Initialize the webserver
     *
     * @return
     */
    public Webmap getJmServer()
    {
        return Webmap.INSTANCE;
    }

    /**
     * Queue a Runnable on the multithreaded task controller.
     *
     * @param runnable
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
     * Whether a task is running on the main thread.
     *
     * @return
     */
    public boolean isMainThreadTaskActive()
    {
        if (mainThreadTaskController != null)
        {
            return mainThreadTaskController.isActive();
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

            // Make sure it only fires once and that snooper settings are enabled before reporting app view.
            if (!modInfoReported && mc.gameSettings.snooperEnabled)
            {
                // ModInfo with a single ping per install.
                new ModInfo("UA-28839029-5", "en_US", Journeymap.MOD_ID, MOD_NAME, FULL_VERSION, true);
                modInfoReported = true;
            }

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

            // request permissions
            if (isJourneyMapServerConnection() || FMLClientHandler.instance().getClient().isSingleplayer())
            {
                new GetClientConfig().send();
            }
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
            new GetClientConfig().send(null, response -> {
                JsonObject settings = response.getAsJson().get(SETTINGS).getAsJsonObject();
                if (settings.get(WORLD_ID) != null)
                {
                    setCurrentWorldId(settings.get(WORLD_ID).getAsString());
                }
            });
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
     * @param task
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
     * @return
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
     * Get the current world id.  May be null.
     *
     * @return
     */
    public String getCurrentWorldId()
    {
        return this.currentWorldId;
    }

    /**
     * Set the current world id.
     *
     * @param worldId
     */
    public void setCurrentWorldId(String worldId)
    {
        synchronized (this)
        {
            Minecraft mc = FMLClientHandler.instance().getClient();
            if (!mc.isSingleplayer())
            {
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
    }

    public boolean isForgeServerConnection()
    {
        return forgeServerConnection;
    }

    public void setForgeServerConnection(boolean forgeServerConnection)
    {
        this.forgeServerConnection = forgeServerConnection;
    }

    public boolean isJourneyMapServerConnection()
    {
        return journeyMapServerConnection;
    }

    public void setJourneyMapServerConnection(boolean journeyMapServerConnection)
    {
        Journeymap.getLogger().debug("Connection initiated with Journeymap Server: " + journeyMapServerConnection);
        this.journeyMapServerConnection = journeyMapServerConnection;
    }

    public boolean isPlayerTrackingEnabled()
    {
        return playerTrackingEnabled;
    }

    public void setPlayerTrackingEnabled(boolean playerTrackingEnabled)
    {
        if (FMLClientHandler.instance().getClient().isSingleplayer())
        {
            this.playerTrackingEnabled = false;
            return;
        }
        Journeymap.getLogger().debug("Expanded Radar Enabled:" + playerTrackingEnabled);
        this.playerTrackingEnabled = playerTrackingEnabled;
    }


    public boolean isTeleportEnabled()
    {
        return teleportEnabled;
    }

    public void setTeleportEnabled(boolean teleportEnabled)
    {
        Journeymap.getLogger().debug("Teleport Enabled:" + teleportEnabled);
        this.teleportEnabled = teleportEnabled;
    }

    public boolean isServerAdmin()
    {
        return this.serverAdmin || FMLClientHandler.instance().getClient().isSingleplayer();
    }

    public void setServerAdmin(boolean serverAdmin)
    {
        if (serverAdmin)
        {
            Journeymap.getLogger().debug("Server Admin Enabled:" + serverAdmin);
        }
        this.serverAdmin = serverAdmin;
    }
}
