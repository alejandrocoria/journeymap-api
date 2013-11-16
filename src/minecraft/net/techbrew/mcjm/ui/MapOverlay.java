package net.techbrew.mcjm.ui;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.data.WaypointsData;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer.DrawEntityStep;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer.DrawStep;
import net.techbrew.mcjm.render.overlay.CoreRenderer;
import net.techbrew.mcjm.render.overlay.MapTexture;
import net.techbrew.mcjm.render.overlay.OverlayRadarRenderer;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Displays the map as an overlay in-game.
 * 
 * @author mwoodman
 *
 */
public class MapOverlay extends GuiScreen {
	
	final long refreshInterval = PropertyManager.getIntegerProp(PropertyManager.Key.UPDATETIMER_CHUNKS);
	Boolean isScrolling = false;
	Boolean hardcore = false;
	int msx, msy, mx, my;

	static Boolean pauseGame = false;
	
	int bWidth = 16;
	int bHeight = 16;
	int bHGap = 8;
	int bVGap = 8;

	// TODO: move to state
	static Constants.MapType mapType;
	static Boolean showCaves = true;
	static Boolean showMonsters = true;
	static Boolean showAnimals = true;
	static Boolean showVillagers = true;
	static Boolean showPets = true;
	static Boolean showPlayers = true;
	static Boolean showWaypoints = true;
	static Boolean follow = true;
	static String playerLastPos = "0,0"; //$NON-NLS-1$
	static int playerLastDimension = Integer.MIN_VALUE;
	static MapTexture logoTexture;
	static CoreRenderer coreRenderer;	
	static OverlayWaypointRenderer waypointRenderer;
	static OverlayRadarRenderer radarRenderer;
	static final int minZoom = 0;
	static final int maxZoom = 5;
	static int currentZoom = 1;

	Logger logger = JourneyMap.getLogger();
	MapOverlayOptions options;
	MapChat chat;
	
	MapOverlayState state = null;
	List<DrawStep> drawStepList = new ArrayList<DrawStep>();
	
	long lastRefresh = 0;
	
	MapButton buttonDayNight, buttonFollow,buttonZoomIn,buttonZoomOut;
	MapButton buttonOptions, buttonClose;
	
	Color playerInfoFgColor = new Color(0x8888ff);
	Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);
	
	public MapOverlay() {
		super();
	}
	
    @Override
	public void initGui()
    {	
		// Set preferences-based values
		PropertyManager pm = PropertyManager.getInstance();
		showCaves = pm.getBoolean(PropertyManager.Key.PREF_SHOW_CAVES);
		showMonsters = pm.getBoolean(PropertyManager.Key.PREF_SHOW_MOBS);
		showAnimals = pm.getBoolean(PropertyManager.Key.PREF_SHOW_ANIMALS);
		showVillagers = pm.getBoolean(PropertyManager.Key.PREF_SHOW_VILLAGERS);
		showPets = pm.getBoolean(PropertyManager.Key.PREF_SHOW_PETS);
		showPlayers = pm.getBoolean(PropertyManager.Key.PREF_SHOW_PLAYERS);
		showWaypoints = pm.getBoolean(PropertyManager.Key.PREF_SHOW_WAYPOINTS);
		
		if(logoTexture==null) {
			logoTexture = new MapTexture(FileHandler.getWebImage("ico/journeymap40.png"));
		}			
		
		// When switching dimensions, reset follow to true
		if(playerLastDimension!=mc.thePlayer.dimension) {
			if(coreRenderer!=null) {
				coreRenderer.clear();
				coreRenderer = null;
			}
			playerLastDimension = mc.thePlayer.dimension;
			follow = true;
		}
		
    	super.allowUserInput = true;    			
        Keyboard.enableRepeatEvents(true);
    	initButtons();
    	chat = new MapChat(this, "", true);
    }

	private void drawButtonBar() {	
		
		// zoom buttons enabled/disabled
		buttonZoomOut.enabled = currentZoom>minZoom;
		buttonZoomIn.enabled = currentZoom<maxZoom;
		
		// zoom underlay
		if(options==null) {
			BaseOverlayRenderer.drawRectangle(3,25,20,55,Color.black,150);
		}
				
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		int oldGuiScale = mc.gameSettings.guiScale;
		mc.gameSettings.guiScale = 2;
		try {
			drawBackground(0);
			drawMap();	
			drawButtonBar();	
			if(options==null) {				
				super.drawScreen(i, j, f);
				if(chat!=null) {
					chat.drawScreen(i, j, f);
				}
			} else {
				options.drawScreen(i, j, f);
			}
			drawPlayerInfo();
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			JourneyMap.getInstance().announce(error);
			close();
			
		} finally {
			mc.gameSettings.guiScale = oldGuiScale;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		
		if(options!=null) {
			options.actionPerformed(guibutton);
			return;
		}
		
		switch(guibutton.id) {
		case 0: { // day or night			
			buttonDayNight.toggle();
			if(buttonDayNight.getToggled()) {
				mapType = Constants.MapType.day;
			} else {
				mapType = Constants.MapType.night;
			}
			refreshState();
			break;
		}

		case 3: { // follow
			toggleFollow();
			break;
		}
		case 4: { // zoom in
			zoomIn();
			break;
		}
		case 5: { // zoom out
			zoomOut();
			break;
		}
		case 6: { // save
			save();
			break;
		}
		case 7: { // close
			close();
			break;
		}
		case 8: { // alert
			launchWebsite();
			break;
		}
		case 9: { // browser
			launchLocalhost();
			break;
		}
		case 15: { // options
			showOptions();
			break;
		}
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft minecraft, int i, int j) {		
		super.setWorldAndResolution(minecraft, i, j);		
		
		hardcore = WorldData.isHardcoreAndMultiplayer();
		initButtons();
		layoutButtons();			
		
		if(follow && coreRenderer!=null) {
			Minecraft mc = Minecraft.getMinecraft();
			coreRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, currentZoom);
		}
		
		if(options!=null) {
			options.setWorldAndResolution(minecraft, i, j);
			return;
		}
		
		if(chat!=null) {
			chat.setWorldAndResolution(minecraft, i, j);
			return;
		}

		lastRefresh=0;		
	}

	/**
	 * Set up UI buttons.
	 */
	void initButtons() {
		buttonList.clear();
		String on = Constants.getString("MapOverlay.on"); //$NON-NLS-1$ 
        String off = Constants.getString("MapOverlay.off"); //$NON-NLS-1$ 
        
		buttonDayNight = new MapButton(0,0,0,80,20,
				Constants.getString("MapOverlay.day"), //$NON-NLS-1$ 
				Constants.getString("MapOverlay.night"), //$NON-NLS-1$ 
				mapType == Constants.MapType.day); 

		buttonFollow = new MapButton(3,0,0,80,20,
				Constants.getString("MapOverlay.follow", on), //$NON-NLS-1$ 
				Constants.getString("MapOverlay.follow", off), //$NON-NLS-1$ 
				follow); //$NON-NLS-1$ //$NON-NLS-2$
		
		buttonZoomIn = new MapButton(4,0,0,bWidth,bHeight,Constants.getString("MapOverlay.zoom_in"), FileHandler.WEB_DIR + "/img/zoomin.png"); //$NON-NLS-1$ //$NON-NLS-2$
		buttonZoomOut = new MapButton(5,0,0,bWidth,bHeight,Constants.getString("MapOverlay.zoom_out"), FileHandler.WEB_DIR + "/img/zoomout.png"); //$NON-NLS-1$ //$NON-NLS-2$
		buttonClose = new MapButton(7,0,0,60,20,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ //$NON-NLS-2$
		buttonOptions = new MapButton(15,0,0,60,20, Constants.getString("MapOverlay.options"));
		
		buttonList.add(buttonDayNight);
		buttonList.add(buttonFollow);
		buttonList.add(buttonZoomIn);
		buttonList.add(buttonZoomOut);
		buttonList.add(buttonClose);
		buttonList.add(buttonOptions);
	}

	/**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		if(buttonList.isEmpty()) {
			initButtons();
		}

		int startX = 50;
		int endX = width - 10;
		int offsetX = bWidth + bHGap;
		int offsetY = bHeight + bVGap;

		buttonDayNight.xPosition = 30;
		buttonDayNight.yPosition = 3;

		buttonFollow.xPosition = 120;
		buttonFollow.yPosition = 3;
		
		buttonOptions.xPosition = 210;
		buttonOptions.yPosition = 3;
		
		buttonZoomIn.xPosition = 6;
		buttonZoomIn.yPosition = 8 + offsetY;
		buttonZoomOut.xPosition = 6;
		buttonZoomOut.yPosition = 8 + (offsetY*2);

		buttonClose.xPosition = endX - 60;	
		buttonClose.yPosition = 3;
		
		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		buttonDayNight.enabled = !(underground && showCaves);
	
	}

	@Override
	public void handleMouseInput() {
		
		if(options!=null) {
			options.handleMouseInput();
			//return;
		}
		
		if(chat!=null && !chat.hidden) {
			chat.handleMouseInput();
			//return;
		}

		mx = (Mouse.getEventX() * width) / mc.displayWidth;
		my = height - (Mouse.getEventY() * height) / mc.displayHeight - 1;

		if(Mouse.getEventButtonState())
		{
			mouseClicked(mx, my, Mouse.getEventButton());
		} else {
			int wheel = Mouse.getEventDWheel();
			if(wheel>0) {
				zoomIn();
			} else if(wheel<0) {
				zoomOut();
			} else {
				mouseMovedOrUp(mx, my, Mouse.getEventButton());
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	{
		if(options!=null) {
			//return;
		}
		
		if(chat!=null && !chat.hidden) {
			chat.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
		Boolean guiButtonUsed = false;
		if(mouseButton == 0)
		{
			for(int l = 0; l < buttonList.size(); l++)
			{
				GuiButton guibutton = (GuiButton)buttonList.get(l);
				if(guibutton.mousePressed(mc, mouseX, mouseY))
				{
					guiButtonUsed = true;
					break;
				}
			}

		}
		if(!guiButtonUsed) {
			//			if(Mouse.isButtonDown(0)) {
			//				scrollCanvas(true);
			//			}
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which)
	{
		super.mouseMovedOrUp(mouseX, mouseY, which);
		if(Mouse.isButtonDown(0) && !isScrolling) {
			isScrolling=true;
			msx=mx;
			msy=my;
		} else if(!Mouse.isButtonDown(0) && isScrolling) {
			isScrolling=false;
						
			int blockSize = (int) Math.pow(2,currentZoom);
			int mouseDragX = (mx-msx)/blockSize;
			int mouseDragY = (my-msy)/blockSize;
			msx=mx;
			msy=my;
			
			coreRenderer.move(-mouseDragX, -mouseDragY);
			updateCoreRenderer();
			
			setFollow(false);
			
			refreshState();
		}
	}

	void zoomIn(){
		if(currentZoom<maxZoom){
			setZoom(currentZoom+1);
		}
	}
	
	void zoomOut(){
		if(currentZoom>minZoom){
			setZoom(currentZoom-1);
		}
	}

	private void setZoom(int zoom) {
		if(zoom>maxZoom || zoom<minZoom || zoom==currentZoom) {
			return;
		}
		currentZoom = zoom;
		refreshState();
	}
	
	void toggleFollow() {
		setFollow(!follow);
	}

	void setFollow(Boolean onPlayer) {		
		if(follow==onPlayer) {
			return;
		}
		buttonFollow.setToggled(onPlayer);
		follow = onPlayer;
		if(follow) {
			//Minecraft mc = Minecraft.getMinecraft();
			//coreRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, currentZoom);
			refreshState();
		} 
	}

	void toggleShowCaves() {	   
		setShowCaves(!showCaves);
	}
	
	static void setShowCaves(Boolean show) {	   
		showCaves = show;
	}


	@Override
	protected void keyTyped(char c, int i)
	{		
		if(chat!=null && !chat.hidden) {
			chat.keyTyped(c, i);
			return;
		}
		
		switch(i) {
			case Keyboard.KEY_ESCAPE : {				
				if(options!=null) {
					options.close();
					break;
				}		
				close();
				break;
			}
			case Keyboard.KEY_ADD : {
				zoomIn();
				break;
			}
			case Keyboard.KEY_EQUALS : {
				zoomIn();
				break;
			}
			case Keyboard.KEY_MINUS : {
				zoomOut();
				break;
			}		
		}		
		
		// North
		if(i==mc.gameSettings.keyBindForward.keyCode) {
			moveCanvas(0,-16);
			return;
		}
		
		// West
		if(i==mc.gameSettings.keyBindLeft.keyCode) {
			moveCanvas(-16, 0);
			return;
		}
		
		// South
		if(i==mc.gameSettings.keyBindBack.keyCode) {
			moveCanvas(0,16);
			return;
		}
		
		// East
		if(i==mc.gameSettings.keyBindRight.keyCode) {
			moveCanvas(16, 0);
			return;
		}
		
		// Open inventory
		if(i==mc.gameSettings.keyBindInventory.keyCode) {
			close();
			mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
			return;
		}
		
		// Open chat
		if(i==mc.gameSettings.keyBindChat.keyCode) {
			openChat("");
			return;
		}
		
		// Open chat with command prefix (Minecraft.java does this in runTick() )
		if(i==mc.gameSettings.keyBindCommand.keyCode) {
			openChat("/");
			return;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		if(options!=null) {
			options.updateScreen();
			return;
		}
		
		if(chat!=null) {
			chat.updateScreen();
		}
		
		layoutButtons();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return pauseGame;
	}

	@Override
	public void drawBackground(int layer)
	{

		if(state!=null && state.getMapType()==MapType.underground) {
			BaseOverlayRenderer.drawRectangle(0, 0, width, height, Color.black, 255);
		} else {
			super.drawBackground(0);
		}
		
        GL11.glEnable(GL11.GL_BLEND);		

		if(options==null) {
			drawButtonBar();
		} else {
			options.drawBackground(0);
		}

	}

	void drawPlayerInfo() {
		
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(1f,1f,1f,1f);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		
		int labelWidth = mc.fontRenderer.getStringWidth(playerLastPos) + 10;
		int halfBg = width/2;
		
		BaseOverlayRenderer.drawCenteredLabel(playerLastPos, width/2, height-6, 14, 0, playerInfoBgColor, playerInfoFgColor, 205);

	}

	void drawMap() {
		
		scaleResolution(false);

		int xOffset = 0;
		int yOffset = 0;
		
		if(isScrolling) {
			
			int blockSize = (int) Math.pow(2,currentZoom);
			int mouseDragX = (mx-msx)/blockSize;
			int mouseDragY = (my-msy)/blockSize;
			
			xOffset = mouseDragX*blockSize;
			yOffset = mouseDragY*blockSize;

		} else if(isRefreshReady()) {						
			refreshState();			
		}
			
		if(coreRenderer!=null) {
			coreRenderer.draw(1f, xOffset, yOffset);
			BaseOverlayRenderer.draw(drawStepList, xOffset, yOffset);
		}
				
		BaseOverlayRenderer.drawImage(logoTexture, 6, 1, false); 
		
		scaleResolution(true);
				
	}
	
	void scaleResolution(boolean doScale) {		
		
		final int glWidth = doScale ? this.width : mc.displayWidth;
		final int glHeight = doScale ? this.height : mc.displayHeight;
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, glWidth, glHeight, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);		
	}
	
	/**
	 * Get a snapshot of the player's biome, effective map state, etc.
	 */
	void refreshState() {
		// Check player status
		EntityClientPlayerMP player = mc.thePlayer;
		if (player==null) {
			logger.warning("Could not get player"); //$NON-NLS-1$
			return;
		}
		
		Integer vSlice = player.chunkCoordY;

		// Maptype
		Constants.MapType effectiveMapType = null;
		final boolean underground = (Boolean) DataCache.playerDataValue(EntityKey.underground);
		if(!underground) vSlice=null;
		if(underground && showCaves && !hardcore) {
			effectiveMapType = Constants.MapType.underground;
		} else {
			if(mapType==null) {
				final long ticks = (mc.theWorld.getWorldTime() % 24000L);
				mapType = ticks<13800 ? Constants.MapType.day : Constants.MapType.night;	
				buttonDayNight.setToggled(mapType.equals(Constants.MapType.day));
			}
			effectiveMapType = mapType;
		}
		File worldDir = FileHandler.getJMWorldDir(mc);
		
		if(state!=null) {
			if(!state.getWorldDir().equals(worldDir) || state.getDimension()!=playerLastDimension || 
				effectiveMapType.equals(MapType.underground) && !state.getMapType().equals(MapType.underground)) {
				setFollow(true);
			}
		}
					
		// Refresh state
		state = new MapOverlayState(worldDir, effectiveMapType, vSlice, underground, currentZoom, playerLastDimension);
		
		// Get core renderer updated
		if(coreRenderer==null || !coreRenderer.isUsing(state.getWorldDir(), state.getDimension())) {
			if(coreRenderer!=null) coreRenderer.clear();
			setFollow(true);
			coreRenderer = new CoreRenderer(state.getWorldDir(), state.getDimension());
		}				
		if(follow) {
			Minecraft mc = Minecraft.getMinecraft();
			coreRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, currentZoom);
		} else {
			coreRenderer.setZoom(currentZoom);
		}
		coreRenderer.updateTextures(state.getMapType(), state.getVSlice());
		

		// Ensure radarRenderer is updated
		if(radarRenderer==null) {
			radarRenderer = new OverlayRadarRenderer(showAnimals, showPets);
		} else {
			radarRenderer.setShowAnimals(showAnimals);
			radarRenderer.setShowPets(showPets);
			radarRenderer.clear();
		}
		
		// Ensure waypointRenderer
		if(waypointRenderer==null) {
			waypointRenderer = new OverlayWaypointRenderer();
			waypointRenderer.clear();
		}
				
		// Build list of drawSteps
		drawStepList.clear();
		if(!hardcore) {
			List<Map> critters = new ArrayList<Map>(16);
			
			if(currentZoom>0) {
				if(showAnimals || showPets) {
					Map map = (Map) DataCache.instance().get(AnimalsData.class).get(EntityKey.root);
					critters.addAll(map.values());
				}
				if(showVillagers) {
					Map map = (Map) DataCache.instance().get(VillagersData.class).get(EntityKey.root);
					critters.addAll(map.values());
				}
				if(showMonsters) {
					Map map = (Map) DataCache.instance().get(MobsData.class).get(EntityKey.root);
					critters.addAll(map.values());
				}
			}
			
			if(showPlayers) {
				Map map = (Map) DataCache.instance().get(PlayersData.class).get(EntityKey.root);
				critters.addAll(map.values());
			}
			
			// Sort to keep named entities last
			Collections.sort(critters, new EntityHelper.EntityMapComparator());
			
			drawStepList.addAll(radarRenderer.prepareSteps(critters, coreRenderer));
		}		
		
		// Draw waypoints
		if(showWaypoints && WaypointHelper.waypointsEnabled()) {
			Map map = (Map) DataCache.instance().get(WaypointsData.class).get(EntityKey.root);
			List<Waypoint> waypoints = new ArrayList<Waypoint>(map.values());

			drawStepList.addAll(waypointRenderer.prepareSteps(waypoints, coreRenderer));
		}

		// Draw player if within bounds
		Point playerPixel = coreRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
		if(playerPixel!=null) {
			drawStepList.add(new DrawEntityStep(playerPixel, EntityHelper.getHeading(mc.thePlayer), false, EntityHelper.getPlayerImage(), 8));				
		}			
		
		// Update player pos
		String biomeName = (String) DataCache.instance().get(PlayerData.class).get(EntityKey.biome);			
		playerLastPos = Constants.getString("MapOverlay.player_location", 
				Integer.toString((int) mc.thePlayer.posX), 
				Integer.toString((int) mc.thePlayer.posZ), 
				Integer.toString((int) mc.thePlayer.posY), 
				mc.thePlayer.chunkCoordY, 
				biomeName); //$NON-NLS-1$ 	
		
		// Reset timer
		lastRefresh = System.currentTimeMillis();
				
	}
	
	void updateCoreRenderer() {
		
		if(state==null || coreRenderer==null) return;

		try {
			coreRenderer.updateTextures(state.getMapType(), state.getVSlice());
			coreRenderer.setZoom(currentZoom);
		} catch(Exception e) {
			logger.severe(LogFormatter.toString(e));
		}
	}

	void save() {

		if(mc==null) {
			mc = Minecraft.getMinecraft();
		}
		final File worldDir = FileHandler.getJMWorldDir(mc);
		final File saveDir = FileHandler.getJourneyMapDir();

		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		final Integer vSlice = underground ? mc.thePlayer.chunkCoordY : null;
		Constants.MapType checkMapType = mapType;
		if(underground && showCaves) {
			checkMapType = Constants.MapType.underground;
		}
		final Constants.MapType useMapType = checkMapType;
		close();
		
		JourneyMap.getInstance().getChunkExecutor().schedule(new Runnable() {
			@Override
			public void run() {							
				try {			
					new MapSaver().saveMap(worldDir, useMapType, vSlice , mc.thePlayer.dimension);
				} catch (java.lang.OutOfMemoryError e) {
					String error = Constants.getMessageJMERR18("Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.");
					JourneyMap.getInstance().announce(error, Level.SEVERE);
				} catch (Throwable t) {	
					String error = Constants.getMessageJMERR18(t.getMessage());
					JourneyMap.getInstance().announce(error, Level.SEVERE);
					logger.severe(LogFormatter.toString(t));					
					return;
				}
			}			
		}, 0, TimeUnit.MILLISECONDS);		

	}
	
	void openChat(String defaultText) {
		if(chat!=null) {			
			chat.inputField.setText(defaultText);
			chat.hidden = false;
		} else {
	        chat = new MapChat(this, defaultText, false);
	        chat.setWorldAndResolution(mc, width, height);
		}
	}

	void close() {
		if(coreRenderer!=null) {
			//tiles.clear();
		}		
		if(chat!=null) {
			chat.close();
		}
		mc.displayGuiScreen(null);
		mc.setIngameFocus();
	}
    
	@Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }
	
	boolean isRefreshReady() {
		if(isScrolling) {
			return false;
		} else {
			return state==null || (System.currentTimeMillis() > (lastRefresh+refreshInterval));
		}
	}

	void moveCanvas(int deltaBlockX, int deltaBlockz){
		coreRenderer.move(deltaBlockX, deltaBlockz);
		setFollow(false);
		refreshState();
	}

	protected void eraseCachedLogoImg() {
		if(logoTexture!=null) {
			GL11.glDeleteTextures(logoTexture.getGlTextureId());
			logoTexture = null;
		}
	}

	protected void launchLocalhost() {
		String port = PropertyManager.getInstance().getString(PropertyManager.Key.WEBSERVER_PORT);
		String url = "http://localhost:" + port; //$NON-NLS-1$
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not launch browser with URL: " + url, e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
		}
	}

	protected void launchWebsite() {
		String url = JourneyMap.WEBSITE_URL;
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not launch browser with URL: " + url, e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
		}
	}
	
	void showOptions() {
		if(options==null) {
			try {
				options = new MapOverlayOptions(this);
				options.setWorldAndResolution(this.mc, width, height);
			} catch (Throwable t) {
				logger.severe("Couldn't init Map options: " + LogFormatter.toString(t));
				options = null;
			}
		}
	}
	
	public static void reset() {
		mapType = null;
		follow = true;
		currentZoom = 1;
		playerLastPos = "0,0"; //$NON-NLS-1$
		playerLastDimension = Integer.MIN_VALUE;
		
		if(logoTexture!=null) {
			logoTexture.clear();
			logoTexture = null;
		}
		if(coreRenderer!=null) {
			coreRenderer.clear();
			coreRenderer = null;
		}
		if(waypointRenderer!=null) {
			waypointRenderer.clear();
			waypointRenderer = null;
		}
		if(radarRenderer!=null) {
			radarRenderer.clear();
			radarRenderer = null;
		}		
	}

}

