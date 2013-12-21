package net.techbrew.mcjm;

import net.minecraft.src.*;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.feature.FeatureManager;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.log.JMLogger;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.ColorCache;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.server.JMServer;
import net.techbrew.mcjm.task.ITaskManager;
import net.techbrew.mcjm.task.MapPlayerTask;
import net.techbrew.mcjm.task.TaskController;
import net.techbrew.mcjm.thread.JMThreadFactory;
import net.techbrew.mcjm.thread.TaskThread;
import net.techbrew.mcjm.ui.MapOverlay;
import net.techbrew.mcjm.ui.UIManager;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
public class JourneyMap {
	
	static final String VERSION_URL = "https://dl.dropboxusercontent.com/u/38077766/JourneyMap/journeymap-version.js"; //$NON-NLS-1$

	public static final String WEBSITE_URL = "http://journeymap.techbrew.net/"; //$NON-NLS-1$
	public static final String JM_VERSION = "3.2.0b1"; //$NON-NLS-1$
	public static final String MC_VERSION = "1.6.4"; //$NON-NLS-1$
	
	public static final String EDITION = getEdition();
	public static final String SHORT_MOD_NAME = "JourneyMap";
	public static final String MOD_NAME = SHORT_MOD_NAME + " " + EDITION;
	
	private static class Holder {
        private static final JourneyMap INSTANCE = new JourneyMap();
    }

    public static JourneyMap getInstance() {
        return Holder.INSTANCE;
    }
    
    private static JMLogger logger;

	private volatile Boolean initialized = false;
	
	private final Boolean modAnnounced = false;	
	private JMServer jmServer;
	
	private boolean threadLogging = false;

	private volatile ChunkMD lastPlayerChunk;

	// Invokes MapOverlay
	public KeyBinding uiKeybinding;

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

	// Announcements
	private final List<String> announcements = Collections.synchronizedList(new LinkedList<String>());

	/**
	 * Constructor.
	 */
	public JourneyMap() {
    	logger = new JMLogger();
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
    
	/**
	 * Initialize
	 */
	public void initialize(Minecraft minecraft) {
		
		// Ensure logger inits
		getLogger();
		
		if(initialized) {
			logger.warning("Already initialized, aborting");
			return;
		}

		final PropertyManager pm = PropertyManager.getInstance();
		
		// Start logFile
		logger.info(MOD_NAME + " starting " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
		logger.environment();
		logger.info("Properties: " + pm.toString()); //$NON-NLS-1$

		// Use property settings		
		enableAnnounceMod = pm.getBoolean(PropertyManager.Key.ANNOUNCE_MODLOADED); 
		
		// Key bindings
		int mapGuiKeyCode = pm.getInteger(PropertyManager.Key.MAPGUI_KEYCODE);
		this.enableMapGui = pm.getBoolean(PropertyManager.Key.MAPGUI_ENABLED); 
		if(this.enableMapGui) {
			this.uiKeybinding = new KeyBinding("JourneyMap", mapGuiKeyCode); //$NON-NLS-1$
		}
		
		// Webserver
		toggleWebserver(pm.getBoolean(PropertyManager.Key.WEBSERVER_ENABLED), false);
		
		// Check for newer version online
		if(VersionCheck.getVersionIsCurrent()==false) {
			announce(Constants.getString("JourneyMap.new_version_available", WEBSITE_URL)); //$NON-NLS-1$
		}		

		initialized = true;
		
		// Override log level now that loading complete
		logger.info("Initialization complete."); //$NON-NLS-1$
		logger.setLevelFromProps();
		
		// Logging for thread debugging
		threadLogging = getLogger().isLoggable(Level.FINER); 

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
				announce(Constants.getMessageJMERR24()); 
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
    private void startMapping(Minecraft minecraft) {
    	synchronized(this) {
            StatTimer.resetAll();
            UIManager.getInstance().reset();
	    	DataCache.instance().purge();
	    	TextureCache.instance().purge();
            Constants.refreshBundle();

	    	if(taskExecutor==null || taskExecutor.isShutdown()) {			    		
				taskExecutor = Executors.newScheduledThreadPool(1, new JMThreadFactory("task")); //$NON-NLS-1$				
			} else {
				logger.severe("TaskExecutor in an unexpected state.  Should be null or shutdown.");
			}
	    	
	    	taskController = new TaskController();
	    	taskController.enableTasks(minecraft);
	    	
	    	logger.info("Mapping started: " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$	
	    		    	
    	}    			
    }
    
    /**
     * Halts mapping threads, clears caches.
     */
    public void stopMapping() {
    	
    	Minecraft minecraft = Minecraft.getMinecraft();
    	
    	synchronized(this) {

	    	if(taskExecutor!=null && !taskExecutor.isShutdown()) {    		
				taskExecutor.shutdown();			
			}	    	
	    	taskExecutor = null;
	    	
	    	if(taskController!=null) {
	    		taskController.disableTasks(minecraft);
	    		taskController.clear();
	    		taskController = null;
	    	}
	    		    	
	    	lastPlayerChunk = null;
			FileHandler.lastWorldHash = -1;
			FileHandler.lastJMWorldDir = null;
			
			TaskThread.reset();
			RegionImageCache.getInstance().flushToDisk();
			RegionImageCache.getInstance().clear();
			TextureCache.instance().purge();
			ColorCache.getInstance().serializeCache();
			ColorCache.getInstance().reset();
            UIManager.getInstance().reset();
			MapPlayerTask.clearCache();
            TileCache.instance().invalidateAll();
            StatTimer.reportAll();
			
			logger.info("Mapping halted: " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$
    	}
    }   

	/**
	 * Called via Modloader
	 * @param f
	 * @param minecraft
	 * @param guiscreen
	 * @return
	 */
	public boolean onTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen) {
		try {
			if(!isMapping()) return true;

			if(guiscreen instanceof GuiMainMenu ||
					guiscreen instanceof GuiSelectWorld ||
					guiscreen instanceof GuiMultiplayer) {
				stopMapping();
			}

            if(!(guiscreen instanceof MapOverlay)) {
                TileCache.pause();
            }

		} catch(Exception e) {
			logger.severe(LogFormatter.toString(e));
		}
		return true;
	}

	/**
	 * Called via Modloader
	 * @param f
	 * @param minecraft
	 * @return
	 */
	public boolean onTickInGame(float f, final Minecraft minecraft) {

        // If both UIs are disabled, the mod is effectively disabled.
        if(!enableWebserver && !enableMapGui) {
            return true;
        }

        // Check player status
        EntityPlayer player = minecraft.thePlayer;
        if (player==null || player.isDead) {
            return true;
        }

		try {

			// Check for world change
			long newHash = Utils.getWorldHash(minecraft);
			if(newHash!=0L) {				
				FileHandler.lastWorldHash=newHash;
			}

			// Check for valid player chunk
			ChunkCoordIntPair playerCoord = new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ);
			if(lastPlayerChunk==null || !playerCoord.equals(lastPlayerChunk.coord)) {
				lastPlayerChunk = ChunkLoader.getChunkStubFromMemory(player.chunkCoordX, player.chunkCoordZ, minecraft);
				if(lastPlayerChunk==null) {
					if(logger.isLoggable(Level.FINE)) {
						logger.fine("Player chunk unknown: " + playerCoord);
					}
					return true;
				}
			}

			// We got this far
			if(!isMapping()) {
				startMapping(minecraft);
			}

            final boolean isGamePaused = minecraft.currentScreen != null && !(minecraft.currentScreen instanceof MapOverlay);

            // Manage tiles
            if(isGamePaused) {
                TileCache.pause();
            } else {
                TileCache.resume();
            }

            // Draw Minimap
            UIManager.getInstance().drawMiniMap();

			// Show announcements
			while(!isGamePaused && !announcements.isEmpty()) {
				player.addChatMessage(announcements.remove(0));
			}			
		
			// Perform the next mapping tasks
			taskController.performTasks(minecraft, newHash, taskExecutor);

		} catch (Throwable t) {
			String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
			announce(error);
			logger.severe(LogFormatter.toString(t));
		}

		return true;
	}
	
	private void announceMod(boolean forced) {

		Minecraft minecraft = Minecraft.getMinecraft();	
		int pos = forced ? Math.max(0,announcements.size()-1) : 0;
		
		if(enableAnnounceMod) {
			announcements.add(pos, Constants.getString("JourneyMap.ready", MOD_NAME)); //$NON-NLS-1$ 
			if(enableWebserver && enableMapGui) {
				String keyName = Keyboard.getKeyName(uiKeybinding.keyCode);
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
				announcements.add(pos+1, Constants.getString("JourneyMap.webserver_and_mapgui_ready", keyName, port)); //$NON-NLS-1$ 
			} else if(enableWebserver) {
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
				announcements.add(pos+1, Constants.getString("JourneyMap.webserver_only_ready", port)); //$NON-NLS-1$ 
			} else if(enableMapGui) {
				String keyName = Keyboard.getKeyName(uiKeybinding.keyCode);
				announcements.add(pos+1, Constants.getString("JourneyMap.mapgui_only_ready", keyName)); //$NON-NLS-1$
			} else {
				announcements.add(pos+1, Constants.getString("JourneyMap.webserver_and_mapgui_disabled")); //$NON-NLS-1$
			}
			enableAnnounceMod = false; // Only announce mod once per runtime
		}
	}

	/**
	 * Called via Modloader
	 * @param keybinding
	 */
	public void keyboardEvent(KeyBinding keybinding)
	{
		if(!isMapping()) return; 
		UIManager.getInstance().keyboardEvent(keybinding);
	}		

	/**
	 * Queue an announcement to be shown in the UI.
	 * @param message
	 */
	public void announce(String message) {
		announce(message, null);
	}
	
	/**
	 * Queue an announcement to be shown in the UI.
	 * @param message
	 */
	public void announce(String message, Level logLevel) {
		if(logLevel!=null) {
			logger.log(logLevel, message);
		}
		String[] lines = message.split("\n"); //$NON-NLS-1$
		lines[0] = Constants.getString("JourneyMap.chat_announcement", lines[0]); //$NON-NLS-1$
		for(String line : lines) {
			announcements.add(line);
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
		return Holder.INSTANCE.logger;
	}

	/**
	 * 
	 * @return
	 */
	public ChunkMD getLastPlayerChunk() {
		return lastPlayerChunk;
	}

}
