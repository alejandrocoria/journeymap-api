package net.techbrew.journeymap;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.data.DataCache;
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
import net.techbrew.journeymap.model.WaypointHelper;
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
import org.lwjgl.input.Keyboard;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This software is copyright (C) Mark Woodman (mwoodman@techbrew.net) and is
 * provided as-is with no warrantee whatsoever.
 * 
 * Central class for the JourneyMap mod. 
 * 
 * @author Mark Woodman
 *
 */
@SideOnly(Side.CLIENT)
@Mod(modid = JourneyMap.MOD_ID, name = JourneyMap.SHORT_MOD_NAME, version = JourneyMap.JM_VERSION)
public class JourneyMap {
	
	static final String VERSION_URL = "https://dl.dropboxusercontent.com/u/38077766/JourneyMap/journeymap-version.js"; //$NON-NLS-1$

	public static final String WEBSITE_URL = "http://journeymap.techbrew.net/"; //$NON-NLS-1$
	public static final String JM_VERSION = "@JMVERSION@"; //$NON-NLS-1$
	public static final String MC_VERSION = "@MCVERSION@"; //$NON-NLS-1$
	
	public static final String EDITION = getEdition();
    public static final String MOD_ID = "journeymap";
	public static final String SHORT_MOD_NAME = "JourneyMap";
	public static final String MOD_NAME = SHORT_MOD_NAME + " " + EDITION;

    private static JourneyMap INSTANCE;

    public static JourneyMap getInstance() {
        return INSTANCE;
    }
    
    private JMLogger logger;

	private volatile Boolean initialized = false;
	
	private final Boolean modAnnounced = false;	
	private JMServer jmServer;
	
	private boolean threadLogging = false;

	// Time stamp of next chunk update
	public long nextPlayerUpdate = 0;
	public long nextChunkUpdate = 0;

	// Whether webserver is running
	boolean enableWebserver;
	public boolean enableMapGui;
	boolean enableAnnounceMod;

	// Executor for task threads
	private volatile ScheduledExecutorService taskExecutor;
	
	// Task controller for issuing tasks in executor
	private TaskController taskController;

    private Minecraft mc;

	/**
	 * Constructor.
	 */
	public JourneyMap() {
        if(INSTANCE!=null) throw new IllegalArgumentException("Use getInstance() after initialization is complete");
        INSTANCE = this;
	}
	
	private static String getEdition() {
		String ed = null;
		try {
			ed = JM_VERSION + " " + FeatureManager.getFeatureSetName();
		} catch(Throwable t) {
			ed = JM_VERSION + " ?";
			t.printStackTrace(System.err);
		}
		return ed;
	}
	
    public Boolean isInitialized() {
    	return initialized;
    }
    
    public Boolean isMapping() {
    	return taskExecutor!=null && !taskExecutor.isShutdown();
    }
    
    public Boolean isThreadLogging() {
    	return threadLogging;
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event)
    {
        try {
            // Ensure logger inits
            logger = new JMLogger();

            if(initialized) {
                logger.warning("Already initialized, aborting");
                return;
            }

            mc = FMLClientHandler.instance().getClient();

            final PropertyManager pm = PropertyManager.getInstance();

            // Start logFile
            logger.info(MOD_NAME + " starting " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
            logger.environment();
            logger.info("Properties: " + pm.toString()); //$NON-NLS-1$

            // Use property settings
            enableAnnounceMod = pm.getBoolean(PropertyManager.Key.ANNOUNCE_MODLOADED);

            // Register general event handlers
            EventHandlerManager.registerGeneralHandlers();

            // In-Game UI
            this.enableMapGui = pm.getBoolean(PropertyManager.Key.MAPGUI_ENABLED);
            if(enableMapGui) {
                // Register GUI event handlers
                EventHandlerManager.registerGuiHandlers();
            }

            // Webserver
            toggleWebserver(pm.getBoolean(PropertyManager.Key.WEBSERVER_ENABLED), false);

            // Check for newer version online
            if(VersionCheck.getVersionIsCurrent()==false) {
                ChatLog.announceI18N(Constants.getString("JourneyMap.new_version_available", "")); //$NON-NLS-1$
                ChatLog.announceURL(WEBSITE_URL, WEBSITE_URL);
            }

            BlockUtils.initialize();

            initialized = true;

            // Override log level now that loading complete
            logger.info("Initialization complete."); //$NON-NLS-1$
            logger.setLevelFromProps();

            // Logging for thread debugging
            threadLogging = getLogger().isLoggable(Level.FINER);

        } catch(Throwable t) {
            System.err.println("Error loading " + JourneyMap.MOD_NAME + " for Minecraft " + JourneyMap.MC_VERSION + ". Ensure compatible Minecraft/Modloader/Forge versions.");
            t.printStackTrace(System.err);
        }
    }

    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event) {
        WaypointHelper.reset();
        BlockUtils.initialize();
    }
	
	public void toggleWebserver(Boolean enable, boolean forceAnnounce) {
		PropertyManager.getInstance().setProperty(PropertyManager.Key.WEBSERVER_ENABLED, enable);
		enableWebserver = enable;
		if(enableWebserver) {
			try {			
				jmServer = new JMServer();
				if(jmServer.isReady()) {
					jmServer.start();		
				} else {
					enableWebserver = false;
				}
			} catch(Throwable e) {
				logger.throwing("JourneyMap", "constructor", e); //$NON-NLS-1$ //$NON-NLS-2$
				logger.log(Level.SEVERE, LogFormatter.toString(e));				
				enableWebserver = false;
			}
			if(!enableWebserver) {
                ChatLog.announceError(Constants.getMessageJMERR24());
			}
		} else {
			enableWebserver = false;
			try {			
				if(jmServer!=null) {
					jmServer.stop();
				}
			} catch(Throwable e) {
				logger.throwing("JourneyMap", "constructor", e); //$NON-NLS-1$ //$NON-NLS-2$
				logger.log(Level.SEVERE, LogFormatter.toString(e));				
				enableWebserver = false;
			}
		}
		if(forceAnnounce) {
			enableAnnounceMod = true;
		}
		announceMod(forceAnnounce);
	}
	
	/**
	 * Toggles automapping
	 * @param enable
	 */
	public void toggleTask(Class<? extends ITaskManager> managerClass, boolean enable, Object params) {
		if(taskController!=null) {
    		taskController.toggleTask(managerClass, enable, params);
    	} else {
    		logger.warning("taskController not available");
    	}
	}
	
	/**
	 * Checks whether a task manager is enabled.
	 * @param managerClass
	 * @return
	 */
	public boolean isTaskManagerEnabled(Class<? extends ITaskManager> managerClass) {
		if(taskController!=null) {
    		return taskController.isTaskManagerEnabled(managerClass);
    	} else {
    		logger.warning("taskController not available");
    		return false;
    	}
	}
	
	/**
     * Starts mapping threads
     */
    public void startMapping() {
    	synchronized(this) {

            if(mc.theWorld==null) return;

            this.reset();

	    	if(taskExecutor==null || taskExecutor.isShutdown()) {			    		
				taskExecutor = Executors.newScheduledThreadPool(1, new JMThreadFactory("task")); //$NON-NLS-1$				
			} else {
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
    public void stopMapping() {
    	
    	Minecraft minecraft = Minecraft.getMinecraft();
    	
    	synchronized(this) {

            if(taskExecutor!=null || taskController!=null)
            {
                String dim = ".";
                if(minecraft.theWorld!=null && minecraft.theWorld.provider!=null) {
                    dim = " dimension " + minecraft.theWorld.provider.dimensionId + "."; //$NON-NLS-1$ //$NON-NLS-2$
                }
                logger.info("Mapping halting in " + WorldData.getWorldName(minecraft) + dim); //$NON-NLS-1$
            }

	    	if(taskExecutor!=null && !taskExecutor.isShutdown()) {    		
				taskExecutor.shutdown();
                taskExecutor = null;
			}	    	

	    	if(taskController!=null) {
	    		taskController.disableTasks(minecraft);
	    		taskController.clear();
	    		taskController = null;
	    	}
    	}
    }

    private void reset() {

        FileHandler.lastJMWorldDir = null;
        //BlockMD.clearCache();
        //ColorCache.getInstance().serializeCache();
        ColorCache.getInstance().reset();
        DataCache.instance().purge();
        MapPlayerTask.clearCache();
        StatTimer.resetAll();
        TaskThread.reset();
        TextureCache.instance().purge();
        TileCache.instance().invalidateAll();
        RegionImageCache.getInstance().flushToDisk();
        RegionImageCache.getInstance().clear();
        UIManager.getInstance().reset();
    }

    public void updateState() {
        try {

            if(mc==null) mc= FMLClientHandler.instance().getClient();

            if(mc.theWorld==null) {
                if(isMapping()) {
                    stopMapping();
                }
                return;
            } else {
                if(!isMapping()) {
                    startMapping();
                }
            }

            // If both UIs are disabled, the mod is effectively disabled.
            if(!enableWebserver && !enableMapGui) {
                return;
            }

            final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof MapOverlay);
            if(isGamePaused) {
                TileCache.pause();

                if(!isMapping()) {
                    return;
                }

                GuiScreen guiScreen = mc.currentScreen;
                if(guiScreen instanceof GuiMainMenu ||
                        guiScreen instanceof GuiSelectWorld ||
                        guiScreen instanceof GuiMultiplayer) {
                    stopMapping();
                    return;
                }
            }

            TileCache.resume();

            // Show announcements
            if(!isGamePaused) {
                ChatLog.showChatAnnouncements(mc);
            }

            // We got this far
            if(!isMapping()) {
                startMapping();
            }

        } catch (Throwable t) {
            String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
            logger.severe(LogFormatter.toString(t));
        }
    }

    public void performTasks() {
        try {
            if(isMapping()){
                taskController.performTasks(mc, taskExecutor);
            }
        } catch (Throwable t) {
            String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
            ChatLog.announceError(error);
            logger.severe(LogFormatter.toString(t));
        }
    }
	
	private void announceMod(boolean forced) {

		if(enableAnnounceMod) {
            ChatLog.announceI18N("JourneyMap.ready", MOD_NAME); //$NON-NLS-1$
			if(enableWebserver && enableMapGui) {
				String keyName = Keyboard.getKeyName(Constants.KB_MAP.keyCode); // Should be KeyCode
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
                String message = Constants.getString("JourneyMap.webserver_and_mapgui_ready", keyName, port); //$NON-NLS-1$
                ChatLog.announceURL(message, "http://localhost" + port); //$NON-NLS-1$
			} else if(enableWebserver) {
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
                String message = Constants.getString("JourneyMap.webserver_only_ready", port); //$NON-NLS-1$
                ChatLog.announceURL(message, "http://localhost" + port); //$NON-NLS-1$
			} else if(enableMapGui) {
				String keyName = Keyboard.getKeyName(Constants.KB_MAP.keyCode); // Should be KeyCode
                ChatLog.announceI18N("JourneyMap.mapgui_only_ready", keyName); //$NON-NLS-1$
			} else {
                ChatLog.announceI18N("JourneyMap.webserver_and_mapgui_disabled"); //$NON-NLS-1$
			}
			enableAnnounceMod = false; // Only queueAnnouncement mod once per runtime
		}
	}

	public ScheduledExecutorService getChunkExecutor() {
		return taskExecutor;
	}

	/**
	 * 
	 * @return
	 */
	public static Logger getLogger() {
		return INSTANCE.logger;
	}

}
