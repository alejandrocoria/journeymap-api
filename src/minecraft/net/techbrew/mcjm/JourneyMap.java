package net.techbrew.mcjm;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiMultiplayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSelectWorld;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ServerData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.JMLogger;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.server.JMServer;
import net.techbrew.mcjm.thread.ChunkUpdateThread;
import net.techbrew.mcjm.thread.JMThreadFactory;
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
	public static final String JM_VERSION = "2.7.0b1"; //$NON-NLS-1$
	public static final String MC_VERSION = "1.6.1"; //$NON-NLS-1$
	
	private static class Holder {
        private static final JourneyMap INSTANCE = new JourneyMap();
    }

    public static JourneyMap getInstance() {
        return Holder.INSTANCE;
    }

	private volatile Boolean initialized = false;
	
	private Boolean modAnnounced = false;
	private JMLogger logger;
	private JMServer jmServer;

	public volatile ChunkStub lastPlayerChunk;

	// Invokes MapOverlay
	public KeyBinding keybinding;

	// Milliseconds between updates
	public int chunkDelay;

	// Time stamp of next chunk update
	public long nextPlayerUpdate = 0;
	public long nextChunkUpdate = 0;
	private volatile boolean running = false;

	// Whether webserver is running
	boolean enableWebserver;
	public boolean enableMapGui;
	boolean enableAnnounceMod;

	// Thread service for writing chunks
	private volatile ScheduledExecutorService chunkExecutor;

	// Announcements
	private List<String> announcements = Collections.synchronizedList(new LinkedList<String>());

	/**
	 * Constructor.
	 */
	public JourneyMap() {
		
	}
	
    public Boolean isInitialized() {
    	return initialized;
    }
    
	/**
	 * Initialize
	 */
	public void initialize(Minecraft minecraft) {
		
		if(initialized) {
			logger.warning("Already initialized, aborting");
			return;
		}

		// Start logFile
		logger.info("JourneyMap v" + JM_VERSION + " starting " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
		logger.environment();
		logger.info("Properties: " + PropertyManager.getInstance().toString()); //$NON-NLS-1$

		// Use property settings
		chunkDelay = PropertyManager.getInstance().getInteger(PropertyManager.Key.UPDATETIMER_CHUNKS);
		enableAnnounceMod = PropertyManager.getInstance().getBoolean(PropertyManager.Key.ANNOUNCE_MODLOADED); 

		// Webserver
		enableWebserver = PropertyManager.getInstance().getBoolean(PropertyManager.Key.WEBSERVER_ENABLED);
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
			
			if(!initialized) {
				initialize(minecraft);
			}

			if(!running) return true;

			if(guiscreen instanceof GuiMainMenu ||
					guiscreen instanceof GuiSelectWorld ||
					guiscreen instanceof GuiMultiplayer) {
				running = false;
			} 
			if(guiscreen instanceof GuiMultiplayer) {
				GuiMultiplayer guiMulti = (GuiMultiplayer) guiscreen;
			}
			if(!running) {
				if(chunkExecutor!=null && !chunkExecutor.isTerminated()) {
					logger.info("Shutting down JourneyMap threads"); //$NON-NLS-1$
					FileHandler.lastWorldHash = -1;
					FileHandler.lastWorldDir = null;
					chunkExecutor.shutdown();
					RegionImageCache.getInstance().flushToDisk();
				}
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
			
			if(!initialized) {
				initialize(minecraft);
			}

			// If both UIs are disabled, the mod is effectively disabled.
			if(!enableWebserver && !enableMapGui) {
				return true;
			}

			// Check player status
			EntityPlayer player = minecraft.thePlayer;
			if (player==null || player.isDead) {
				return true;
			}

			// Don't do anything when game is paused
			boolean isGamePaused = minecraft.isSingleplayer() && minecraft.currentScreen != null && minecraft.currentScreen.doesGuiPauseGame() && !minecraft.getIntegratedServer().getPublic();
			if(isGamePaused) {
				//return true; // TODO : pause the game?
			}

			// Check for world change
			long newHash = Utils.getWorldHash(minecraft);
			if(newHash!=0L) {
				logger.info("Map data: " + FileHandler.getWorldDir(minecraft, newHash));
				FileHandler.lastWorldHash=newHash;
			}

			// Multiplayer:  Bail if server info not available or hash has changed
			// TODO
//			if(!minecraft.isSingleplayer()) {	
//				running = false;
//				return true;			
//			}

			// Check for valid player chunk
			Chunk pChunk = Utils.getChunkIfAvailable(minecraft.theWorld, player.chunkCoordX, player.chunkCoordZ);
			if(pChunk==null){
				lastPlayerChunk = null;
				logger.finer("Player chunk not known: " + (player.chunkCoordX) + "," +(player.chunkCoordZ));
				return true;
			} else {
				if(lastPlayerChunk==null || (player.chunkCoordX!=lastPlayerChunk.xPosition || player.chunkCoordZ!=lastPlayerChunk.zPosition)) {
					lastPlayerChunk = new ChunkStub(pChunk, true, minecraft.theWorld, FileHandler.lastWorldHash);
				}				
			}

			// We got this far
			if(!running) {
				DataCache.instance().purge();
				running = true;				
				if(enableAnnounceMod) announceMod();
			}

			// Show announcements
			while(!isGamePaused && !announcements.isEmpty()) {
				player.addChatMessage(announcements.remove(0));
			}

			// Start executors
			if(chunkExecutor==null || chunkExecutor.isShutdown()) {
				logger.info("Starting up JourneyMap threads for " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$
				chunkExecutor = Executors.newSingleThreadScheduledExecutor(new JMThreadFactory("chunk"));
				chunkExecutor.scheduleWithFixedDelay(new ChunkUpdateThread(this, minecraft.theWorld), 1500, chunkDelay, TimeUnit.MILLISECONDS);
			} else {

				try {
					// Populate ChunkStubs on ChunkUpdateThread if it is waiting
					if(ChunkUpdateThread.getBarrier().isBroken()) {
						logger.finer("Resetting broken Barrier "); 
						ChunkUpdateThread.getBarrier().reset();
					}
					if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
						if(ChunkUpdateThread.currentThread!=null) {
							synchronized(ChunkUpdateThread.currentThread) {
								long start = System.currentTimeMillis();
								int[] result = ChunkUpdateThread.currentThread.fillChunkStubs(minecraft.thePlayer, lastPlayerChunk, minecraft.theWorld, FileHandler.lastWorldHash);
								long stop = System.currentTimeMillis();
								if(logger.isLoggable(Level.FINER)) {
									logger.finer("Stubbed/skipped: " + result[0] + "," + result[1] + " in " + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								}

							}
						} else {
							if(logger.isLoggable(Level.FINER)) {
								logger.finer("ChunkUpdateThread.currentThread==null"); //$NON-NLS-1$ 
							}
						}
						logger.finer("JourneyMap done with fillChunkStubs"); //$NON-NLS-1$ 
						if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
							ChunkUpdateThread.getBarrier().reset(); // Let the chunkthread continue
						}
					} else {
						if(logger.isLoggable(Level.FINER)) {
							//logger.finer("ChunkUpdateThread.getBarrier().getNumberWaiting()==" + ChunkUpdateThread.getBarrier().getNumberWaiting()); //$NON-NLS-1$ 
						}
					}
				} catch(Throwable t) {
					logger.info(LogFormatter.toString(t));
				}

			}

		} catch (Throwable t) {
			String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
			announce(error);
			logger.throwing("JourneyMap", "OnTickInGame", t); //$NON-NLS-1$ //$NON-NLS-2$
			logger.log(Level.SEVERE, LogFormatter.toString(t));			
		} 
		return true;
	}

	private void announceMod() {

		Boolean announceReady = false;
		Minecraft minecraft = Minecraft.getMinecraft();
		if(minecraft.isSingleplayer()==false) {		
			logger.info("Mapping in multiplayer world: " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			announceReady = true;
			logger.info("Mapping in singleplayer world: " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$
		}	
		if(enableAnnounceMod && announceReady) {
			announcements.add(Constants.getString("JourneyMap.ready", JM_VERSION)); //$NON-NLS-1$ 
			if(enableWebserver && enableMapGui) {
				String keyName = Keyboard.getKeyName(keybinding.keyCode);
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
				announcements.add(Constants.getString("JourneyMap.webserver_and_mapgui_ready", keyName, port)); //$NON-NLS-1$ 
			} else if(enableWebserver) {
				String port = jmServer.getPort()==80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
				announcements.add(Constants.getString("JourneyMap.webserver_only_ready", port)); //$NON-NLS-1$ 
			} else if(enableMapGui) {
				String keyName = Keyboard.getKeyName(keybinding.keyCode);
				announcements.add(Constants.getString("JourneyMap.mapgui_only_ready", keyName)); //$NON-NLS-1$
			} else {
				announcements.add(Constants.getString("JourneyMap.webserver_and_mapgui_disabled")); //$NON-NLS-1$
			}
			enableAnnounceMod = false; // Announcement now only happens once, not on every world switch
		}
	}

	/**
	 * Called via Modloader
	 * @param keybinding
	 */
	public void keyboardEvent(KeyBinding keybinding)
	{
		if(!running) return; 

		Minecraft minecraft = Minecraft.getMinecraft();
		if(keybinding.keyCode==keybinding.keyCode) {
			if(minecraft.currentScreen==null) {
				minecraft.displayGuiScreen(new MapOverlay(this));
				keybinding.unPressAllKeys();
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
		String[] lines = message.split("\n"); //$NON-NLS-1$
		lines[0] = Constants.getString("JourneyMap.chat_announcement", lines[0]); //$NON-NLS-1$
		for(String line : lines) {
			announcements.add(line);
		}
	}

	public ScheduledExecutorService getChunkExecutor() {
		return chunkExecutor;
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

	/**
	 * 
	 * @return
	 */
	public Boolean isRunning() {
		return running;
	}

}
