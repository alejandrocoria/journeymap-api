package net.techbrew.mcjm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiEditSign;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiMultiplayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSelectWorld;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionImageCache;
import net.techbrew.mcjm.log.JMLogger;
import net.techbrew.mcjm.log.LogFormatter;
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
 * Central class for the JourneyMap mod.  Extends the ModLoader BaseMod class.
 * 
 * @author Mark Woodman
 *
 */
public class JourneyMap extends BaseMod {

	/** http://dl.dropbox.com/u/38077766/JourneyMap/journeymap-version.js?client-version=JM2.0_MC1.4.6 */
	static final String VERSION_URL = "http://goo.gl/xo4CR"; //$NON-NLS-1$
	
	public static final String WEBSITE_URL = "http://journeymap.techbrew.net/"; //$NON-NLS-1$
	public static final String JM_VERSION = "2.0"; //$NON-NLS-1$
	public static final String ML_VERSION = "ModLoader 1.4.6"; //$NON-NLS-1$
	public static final String MC_VERSION = "1.4.6"; //$NON-NLS-1$

	private static Boolean modAnnounced = false;
	private static JMLogger logger;
	private static JMServer jmServer;
	
	public volatile Properties remoteWorldProperties = new Properties();
	public static volatile ChunkStub lastPlayerChunk;
	
	// Invokes MapOverlay
	private KeyBinding keybinding;

	// Milliseconds between updates
	public static int chunkDelay;

	// Time stamp of next chunk update
	public static long nextPlayerUpdate = 0;
	public static long nextChunkUpdate = 0;
	private static volatile boolean running = false;
	
	// Whether webserver is running
	boolean enableWebserver;
	boolean enableMapGui;
	boolean enableAnnounceMod;
	
	//private ChannelClient channelClient;
	
	private boolean executorsStarted = false;
	
	// Thread service for writing chunks
	private static ScheduledExecutorService chunkExecutor;
	
	// Announcements
	private static List<String> announcements = Collections.synchronizedList(new LinkedList<String>());

	/**
	 * Constructor.
	 */
	public JourneyMap() {
		ModLoader.setInGameHook(this, true, false);
		ModLoader.setInGUIHook(this, true, false);
	}
	
	@Override
	public String getVersion() {
		return JM_VERSION;
	}

	@Override
	public void load() 
	{		
		Minecraft minecraft = Minecraft.getMinecraft();
		
		// Start logFile
		getLogger().info("JourneyMap v" + JM_VERSION + " starting " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
		logger.showEnvironmentProperties();
		logger.info("Properties: " + PropertyManager.getInstance().toString()); //$NON-NLS-1$
		
		// Check Modloader version
//		if(!ModLoader.VERSION.equals(ML_VERSION)) {
//			String error = Constants.getMessageJMERR01(JourneyMap.JM_VERSION , JourneyMap.ML_VERSION);
//			ModLoader.getLogger().severe(error);
//			getLogger().severe(error);
//			throw new IllegalStateException(error);
//		} else {
//			logger.info(ModLoader.VERSION + " detected."); //$NON-NLS-1$
//		}
		
		// Packet Handler
		//channelClient = new ChannelClient(this);

		// Use property settings
		chunkDelay = PropertyManager.getInstance().getInteger(PropertyManager.UPDATETIMER_CHUNKS_PROP);
		
		enableAnnounceMod = PropertyManager.getInstance().getBoolean(PropertyManager.ANNOUNCE_MODLOADED_PROP); 
		
		// Map GUI keycode
		int mapGuiKeyCode = PropertyManager.getInstance().getInteger(PropertyManager.MAPGUI_KEYCODE_PROP);
		enableMapGui = PropertyManager.getInstance().getBoolean(PropertyManager.MAPGUI_ENABLED_PROP); 
		if(enableMapGui) {
			keybinding = new KeyBinding("JourneyMap", mapGuiKeyCode); //$NON-NLS-1$
			ModLoader.registerKey(this, keybinding, false);
		}
		
		// Register custom packet channel
		//ModLoader.registerPacketChannel(this, ChannelClient.CHANNEL_NAME);
		
		// Register listener for events which signal possible world change
		//
		
		// Webserver
		enableWebserver = PropertyManager.getInstance().getBoolean(PropertyManager.WEBSERVER_ENABLED_PROP);
		if(enableWebserver) {
			try {			
				//new LibraryLoader().loadLibraries();
				jmServer = new JMServer();
				jmServer.start();			
			}
			catch(Throwable e) {
				getLogger().throwing("JourneyMap", "constructor", e); //$NON-NLS-1$ //$NON-NLS-2$
				JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(e));
				announce(Constants.getMessageJMERR24()); 
				enableWebserver = false;
			}
		}
		
		// Check for newer version online
		if(VersionCheck.getVersionIsCurrent()==false) {
			announce(Constants.getString("JourneyMap.new_version_available", WEBSITE_URL)); //$NON-NLS-1$
		}
		
		// Override log level now that loading complete
		logger.info("Load complete."); //$NON-NLS-1$
		logger.setLevelFromProps();
		
		
	}

	@Override
	public boolean onTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen)
    {
		try {
						
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
				if(executorsStarted) {
					getLogger().info("Shutting down JourneyMap threads"); //$NON-NLS-1$
					FileHandler.lastWorldHash = -1;
					FileHandler.lastWorldDir = null;
					getChunkExecutor().shutdown();
					executorsStarted = false;
					RegionImageCache.getInstance().flushToDisk();
				}
			}
		} catch(Exception e) {
			getLogger().severe(LogFormatter.toString(e));
		}
		return true;
    }

	@Override
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
			
			// Don't do anything when game is paused
			if(minecraft.isSingleplayer() && minecraft.isGamePaused) {
				//return true; // TODO
			}
			
			// Check for world change
			long newHash = Utils.getWorldHash(minecraft);
			if(newHash!=0L) {
				getLogger().info("Map data: " + FileHandler.getWorldDir(minecraft, newHash));
				FileHandler.lastWorldHash=newHash;
			}

			// Multiplayer:  Bail if server info not available or hash has changed
			if(!minecraft.isSingleplayer()) {	
				if(minecraft.getServerData()==null) {
					running = false;
					return true;
				}				
			}
			
			// Check for valid player chunk
			Chunk pChunk = Utils.getChunkIfAvailable(minecraft.theWorld, player.chunkCoordX, player.chunkCoordZ);
			if(pChunk==null){
				lastPlayerChunk = null;
				getLogger().finer("Player chunk not known: " + (player.chunkCoordX) + "," +(player.chunkCoordZ));
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
			while(!minecraft.isGamePaused && !announcements.isEmpty()) {
				player.addChatMessage(announcements.remove(0));
			}
			
			ThreadMXBean bean = ManagementFactory.getThreadMXBean();
			long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.

			if (threadIds != null) {
			    ThreadInfo[] infos = bean.getThreadInfo(threadIds);

			    for (ThreadInfo info : infos) {
			        StackTraceElement[] stack = info.getStackTrace();
			        getLogger().severe("Deadlocked thread: " + Arrays.asList(stack));
			    }
			}
			
			// Start executors
			if(!executorsStarted) {
				
				getLogger().info("Starting up JourneyMap threads for " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$
				executorsStarted = true;

				// Start chunkExecutor
				chunkExecutor = Executors.newSingleThreadScheduledExecutor(new JMThreadFactory("chunkExecutor"));
				getChunkExecutor().scheduleWithFixedDelay(new ChunkUpdateThread(this, minecraft.theWorld), 1500, chunkDelay, TimeUnit.MILLISECONDS);
			} else {
				
				try {
					// Populate ChunkStubs on ChunkUpdateThread if it is waiting
					if(ChunkUpdateThread.getBarrier().isBroken()) {
						getLogger().finer("Resetting broken Barrier "); 
						ChunkUpdateThread.getBarrier().reset();
					}
					if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
						if(ChunkUpdateThread.currentThread!=null) {
							synchronized(ChunkUpdateThread.currentThread) {
								long start = System.currentTimeMillis();
								int[] result = ChunkUpdateThread.currentThread.fillChunkStubs(minecraft.thePlayer, lastPlayerChunk, minecraft.theWorld, FileHandler.lastWorldHash);
								long stop = System.currentTimeMillis();
								if(getLogger().isLoggable(Level.FINER)) {
									getLogger().finer("Stubbed/skipped: " + result[0] + "," + result[1] + " in " + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								}
								
							}
						} else {
							if(getLogger().isLoggable(Level.FINER)) {
								getLogger().finer("ChunkUpdateThread.currentThread==null"); //$NON-NLS-1$ 
							}
						}
						getLogger().finer("JourneyMap done with fillChunkStubs"); //$NON-NLS-1$ 
						if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
							ChunkUpdateThread.getBarrier().reset(); // Let the chunkthread continue
						}
					} else {
						if(getLogger().isLoggable(Level.FINER)) {
							//getLogger().finer("ChunkUpdateThread.getBarrier().getNumberWaiting()==" + ChunkUpdateThread.getBarrier().getNumberWaiting()); //$NON-NLS-1$ 
						}
					}
				} catch(Throwable t) {
					getLogger().info(LogFormatter.toString(t));
				}
				
			}
			
		} catch (Throwable t) {
			String error = Constants.getMessageJMERR00(t.getMessage()); //$NON-NLS-1$
			announce(error);
			getLogger().throwing("JourneyMap", "OnTickInGame", t); //$NON-NLS-1$ //$NON-NLS-2$
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));			
		} 
		return true;
	}
	
	private void announceMod() {
		
		Boolean announceReady = false;
		Minecraft minecraft = Minecraft.getMinecraft();
		if(minecraft.isSingleplayer()==false) {
			if(minecraft.getServerData()!=null) {
				announceReady = true;
				logger.info("Mapping in multiplayer world: " + minecraft.getServerData().serverName + " , " + WorldData.getWorldName(minecraft)); //$NON-NLS-1$ //$NON-NLS-2$
			}
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
	
	@Override
	public void keyboardEvent(KeyBinding keybinding)
    {
		if(!running) return; 
		
		Minecraft minecraft = Minecraft.getMinecraft();
		if(keybinding.keyCode==keybinding.keyCode) {
			if(Minecraft.getMinecraft().currentScreen==null) {
				ModLoader.openGUI(minecraft.thePlayer, new MapOverlayOptions(new MapOverlay(this)));
			} else if(ModLoader.isGUIOpen(MapOverlay.class)) {
				minecraft.displayGuiScreen(null);
				minecraft.setIngameFocus();
			}
		} else if(keybinding.keyCode==minecraft.gameSettings.keyBindInventory.keyCode) {
			if(ModLoader.isGUIOpen(MapOverlayOptions.class)) { 
				minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));				
			}
		}
    }		
	
	/**
	 * Queue an announcement to be shown in the UI.
	 * @param message
	 */
	public static void announce(String message) {
		String[] lines = message.split("\n"); //$NON-NLS-1$
		lines[0] = Constants.getString("JourneyMap.chat_announcement", lines[0]); //$NON-NLS-1$
		for(String line : lines) {
			announcements.add(line);
		}
	}

	public static ScheduledExecutorService getChunkExecutor() {
		return chunkExecutor;
	}
	
	/**
	 * TODO: Make threadsafe
	 * @return
	 */
	public static Logger getLogger() {
		if(logger==null) {
			synchronized(JourneyMap.class) {
				logger = new JMLogger();				
			}
		}
		return logger;
	}
	
	/**
	 * TODO: Make threadsafe
	 * @return
	 */
	public static ChunkStub getLastPlayerChunk() {
		return lastPlayerChunk;
	}
	
	/**
	 * TODO: Make threadsafe
	 * @return
	 */
	public static Boolean isRunning() {
		return running;
	}
	
}
