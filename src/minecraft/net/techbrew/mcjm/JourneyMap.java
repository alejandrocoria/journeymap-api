package net.techbrew.mcjm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiMultiplayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSelectWorld;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.log.JMLogger;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.server.JMServer;
import net.techbrew.mcjm.task.ITaskManager;
import net.techbrew.mcjm.task.TaskController;
import net.techbrew.mcjm.thread.JMThreadFactory;
import net.techbrew.mcjm.thread.TaskThread;
import net.techbrew.mcjm.ui.MapOverlay;
import net.techbrew.mcjm.ui.MapOverlayOptions;

import org.lwjgl.input.Keyboard;

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
	public static final String JM_VERSION = "3.0.0b1"; //$NON-NLS-1$
	public static final String MC_VERSION = "1.6.4"; //$NON-NLS-1$
	
	private static class Holder {
        private static final JourneyMap INSTANCE = new JourneyMap();
    }

    public static JourneyMap getInstance() {
        return Holder.INSTANCE;
    }

	private volatile Boolean initialized = false;
	
	private final Boolean modAnnounced = false;
	private JMLogger logger;
	private JMServer jmServer;
	
	private boolean threadLogging = false;

	private ChunkCoordIntPair lastPlayerCoord;
	private volatile ChunkStub lastPlayerChunk;

	// Invokes MapOverlay
	public KeyBinding keybinding;

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
		logger.info("JourneyMap v" + JM_VERSION + " starting " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
		logger.environment();
		logger.info("Properties: " + pm.toString()); //$NON-NLS-1$

		// Use property settings		
		enableAnnounceMod = pm.getBoolean(PropertyManager.Key.ANNOUNCE_MODLOADED); 
		
		// Key bindings
		int mapGuiKeyCode = pm.getInteger(PropertyManager.Key.MAPGUI_KEYCODE);
		this.enableMapGui = pm.getBoolean(PropertyManager.Key.MAPGUI_ENABLED); 
		if(this.enableMapGui) {
			this.keybinding = new KeyBinding("JourneyMap", mapGuiKeyCode); //$NON-NLS-1$
		}
		
		// Webserver
		enableWebserver = pm.getBoolean(PropertyManager.Key.WEBSERVER_ENABLED);
		if(enableWebserver) {
			try {			
				//new LibraryLoader().loadLibraries();
				jmServer = new JMServer();
				jmServer.start();			
			}
			catch(Throwable e) {
				logger.throwing("JourneyMap", "constructor", e); //$NON-NLS-1$ //$NON-NLS-2$
				logger.log(Level.SEVERE, LogFormatter.toString(e));
				announce(Constants.getMessageJMERR24()); 
				enableWebserver = false;
			}
		}

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
	
	/**
	 * Toggles automapping
	 * @param enable
	 */
	public void toggleTask(Class<? extends ITaskManager> managerClass, boolean enable) {
		if(taskController!=null) {
    		taskController.toggleTask(managerClass, enable);
    	} else {
    		logger.warning("taskController not available");
    	}
	}
	
	/**
     * Starts mapping threads
     */
    private void startMapping(Minecraft minecraft) {
    	synchronized(this) {
    		MapOverlay.reset();
    		EntityHelper.clearCaches();
	    	DataCache.instance().purge();   

	    	if(taskExecutor==null || taskExecutor.isShutdown()) {			    		
				taskExecutor = Executors.newScheduledThreadPool(1, new JMThreadFactory("task")); //$NON-NLS-1$				
			} else {
				logger.severe("TaskExecutor in an unexpected state.  Should be null or shutdown.");
			}
	    	
	    	taskController = new TaskController();
	    	taskController.enableTasks(minecraft);
	    	
	    	logger.info("Mapping started: " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$	
	    		    	
    	}    	
		if(enableAnnounceMod) announceMod();
    }
    
    /**
     * Halts mapping threads, clears caches.
     */
    public void stopMapping() {
    	synchronized(this) {
    		
    		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    		long[] threadIds = bean.findDeadlockedThreads();
    		if (threadIds != null) {
    		    ThreadInfo[] infos = bean.getThreadInfo(threadIds);

    		    for (ThreadInfo info : infos) {
    		        logger.severe("Deadlock detected: " + info);
    		    }
    		}
    		
	    	if(taskExecutor!=null && !taskExecutor.isShutdown()) {    		
				taskExecutor.shutdown();			
			}	    	
	    	taskExecutor = null;
	    	
	    	Minecraft minecraft = Minecraft.getMinecraft();
	    	
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

		try {

			// If both UIs are disabled, the mod is effectively disabled.
			if(!enableWebserver && !enableMapGui) {
				return true;
			}

			// Check player status
			EntityPlayer player = minecraft.thePlayer;
			if (player==null || player.isDead) {
				return true;
			}

			// Check for world change
			long newHash = Utils.getWorldHash(minecraft);
			if(newHash!=0L) {				
				FileHandler.lastWorldHash=newHash;
			}

			// Check for valid player chunk
			ChunkCoordIntPair playerCoord = new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ);
			if(lastPlayerChunk==null || !playerCoord.equals(lastPlayerCoord)) {
				lastPlayerCoord = playerCoord;
				lastPlayerChunk = ChunkLoader.getChunkStubFromMemory(player.chunkCoordX, player.chunkCoordZ, minecraft, newHash);
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

			// Show announcements
			boolean isGamePaused = minecraft.currentScreen != null;
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
	
	private void announceMod() {

		Minecraft minecraft = Minecraft.getMinecraft();	
		
		if(enableAnnounceMod) {
			announcements.add(0, Constants.getString("JourneyMap.ready", JM_VERSION)); //$NON-NLS-1$ 
			if(enableWebserver && enableMapGui) {
				String keyName = Keyboard.getKeyName(keybinding.keyCode);
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
				announcements.add(1, Constants.getString("JourneyMap.webserver_and_mapgui_ready", keyName, port)); //$NON-NLS-1$ 
			} else if(enableWebserver) {
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
				announcements.add(1, Constants.getString("JourneyMap.webserver_only_ready", port)); //$NON-NLS-1$ 
			} else if(enableMapGui) {
				String keyName = Keyboard.getKeyName(keybinding.keyCode);
				announcements.add(1, Constants.getString("JourneyMap.mapgui_only_ready", keyName)); //$NON-NLS-1$
			} else {
				announcements.add(1, Constants.getString("JourneyMap.webserver_and_mapgui_disabled")); //$NON-NLS-1$
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

		Minecraft minecraft = Minecraft.getMinecraft();
		if(keybinding.keyCode==keybinding.keyCode) {
			if(minecraft.currentScreen==null) {
				try {
					minecraft.displayGuiScreen(new MapOverlay());
					keybinding.unPressAllKeys();
				} catch(Throwable e) {
					logger.log(Level.SEVERE, "Unexpected exception in MapOverlay constructor: " + e); //$NON-NLS-1$
					JourneyMap.getLogger().severe(LogFormatter.toString(e));
					String error = Constants.getMessageJMERR23(e.getMessage());
					JourneyMap.getInstance().announce(error);
				}
			} else if(Minecraft.getMinecraft().currentScreen instanceof MapOverlay) {
				minecraft.displayGuiScreen(null);
				minecraft.setIngameFocus();
				keybinding.unPressAllKeys();
			}
		} else if(keybinding.keyCode==minecraft.gameSettings.keyBindInventory.keyCode) {
			if(minecraft.currentScreen instanceof MapOverlayOptions) { 
				minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));				
			} else if(minecraft.currentScreen instanceof MapOverlay) { 
				minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));				
			}
		}
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
		
		JourneyMap instance = getInstance();
		if(instance.logger==null) {
			instance.logger = new JMLogger();				
		}
		return instance.logger;
	}

	/**
	 * 
	 * @return
	 */
	public ChunkStub getLastPlayerChunk() {
		return lastPlayerChunk;
	}

}
