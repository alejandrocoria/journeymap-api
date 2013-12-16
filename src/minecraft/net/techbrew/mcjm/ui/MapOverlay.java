package net.techbrew.mcjm.ui;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.render.overlay.*;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.ui.dialog.MapChat;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays the map as a full-screen overlay in-game.
 * 
 * @author mwoodman
 *
 */
public class MapOverlay extends JmUI {

	final static MapOverlayState state = new MapOverlayState();
	final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
	final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
	final static GridRenderer gridRenderer = new GridRenderer(5);

	private enum ButtonEnum{Alert,DayNight,Follow,ZoomIn,ZoomOut,Options,Actions,Close,MiniMap}
	
	final int minZoom = 0;
	final int maxZoom = 5;
	
	Boolean isScrolling = false;
	int msx, msy, mx, my;	

	Logger logger = JourneyMap.getLogger();
	MapChat chat;
	
	MapButton buttonDayNight, buttonFollow, buttonZoomIn, buttonZoomOut;
	MapButton buttonAlert, buttonOptions, buttonActions, buttonClose;
    MapButton buttonMiniMap;
	
	Color bgColor = new Color(0x22, 0x22, 0x22);
	Color playerInfoFgColor = new Color(0x8888ff);
	Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);

    StatTimer drawTimer = StatTimer.get("MapOverlay.drawMapRefresh");
    StatTimer refreshTimer = StatTimer.get("MapOverlay.refreshState");
	
	/**
	 * Default constructor
	 */
	public MapOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        state.refresh(mc, mc.thePlayer);
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.setZoom(state.currentZoom);
	}
	
    @Override
	public void initGui()
    {			
    	super.allowUserInput = true;
    	Keyboard.enableRepeatEvents(true);
    	initButtons();
    	
    	// When switching dimensions, reset grid
		if(state.getDimension()!=mc.thePlayer.dimension) {
			gridRenderer.clear();
		}

    	chat = new MapChat(this, "", true);
    }

	@Override
	public void drawScreen(int i, int j, float f) {
		try {
            drawBackground(0);
			drawMap();
            super.drawScreen(i, j, f); // Buttons
            drawPlayerInfo();
            if(chat!=null) chat.drawScreen(i, j, f);
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			JourneyMap.getInstance().announce(error);
			close();
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		
		final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {
			case DayNight: { // day or night			
				toggleDayNight();
				break;
			}
	
			case Follow: { // follow
				toggleFollow();
				break;
			}
			case ZoomIn: { // zoom in
				zoomIn();
				break;
			}
			case ZoomOut: { // zoom out
				zoomOut();
				break;
			}
			case Close: { // close
				UIManager.getInstance().closeAll();
				break;
			}
			case Alert: { // alert
				VersionCheck.launchWebsite();
				break;
			}
			case Options: { // options
				UIManager.getInstance().openMapOptions();
				break;
			}
			case Actions: { // actions
				UIManager.getInstance().openMapActions();
				break;
			}
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft minecraft, int width, int height) {				
		super.setWorldAndResolution(minecraft, width, height);	
		
		state.requireRefresh();
		
		initButtons();
		layoutButtons();			
		
		if(state.follow) {
            boolean moved = gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
            if(moved) {
                gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
            }
		}
		
		if(chat!=null) {
			chat.setWorldAndResolution(minecraft, width, height);
			return;
		}
		
		drawMap();
	}

	/**
	 * Set up UI buttons.
	 */
	void initButtons() {
		buttonList.clear();
		String on = Constants.getString("MapOverlay.on"); //$NON-NLS-1$ 
        String off = Constants.getString("MapOverlay.off"); //$NON-NLS-1$ 
        
		buttonAlert = new MapButton(ButtonEnum.Alert.ordinal(),0,0, Constants.getString("MapOverlay.update_available")); //$NON-NLS-1$ 
		buttonAlert.drawButton = VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent();
		
		buttonDayNight = new MapButton(ButtonEnum.DayNight.ordinal(),0,0,80,20,
				Constants.getString("MapOverlay.day"), //$NON-NLS-1$ 
				Constants.getString("MapOverlay.night"), //$NON-NLS-1$ 
				state.getMapType() == Constants.MapType.day); 

		buttonFollow = new MapButton(ButtonEnum.Follow.ordinal(),0,0,80,20,
				Constants.getString("MapOverlay.follow", on), //$NON-NLS-1$ 
				Constants.getString("MapOverlay.follow", off), //$NON-NLS-1$ 
				state.follow); //$NON-NLS-1$ //$NON-NLS-2$
		
		buttonZoomIn  = new MapButton(ButtonEnum.ZoomIn.ordinal(),0,0,12,12,Constants.getString("MapOverlay.zoom_in"), FileHandler.WEB_DIR + "/img/zoomin.png"); //$NON-NLS-1$ //$NON-NLS-2$
		buttonZoomOut = new MapButton(ButtonEnum.ZoomOut.ordinal(),0,0,12,12,Constants.getString("MapOverlay.zoom_out"), FileHandler.WEB_DIR + "/img/zoomout.png"); //$NON-NLS-1$ //$NON-NLS-2$
		
		buttonZoomOut.enabled = state.currentZoom>minZoom;
		buttonZoomIn.enabled = state.currentZoom<maxZoom;
		
		buttonClose   = new MapButton(ButtonEnum.Close.ordinal(),0,0,60,20,Constants.getString("MapOverlay.close")); //$NON-NLS-1$
		buttonOptions = new MapButton(ButtonEnum.Options.ordinal(),0,0,60,20, Constants.getString("MapOverlay.options")); //$NON-NLS-1$
		buttonActions = new MapButton(ButtonEnum.Actions.ordinal(),0,0,60,20, Constants.getString("MapOverlay.actions")); //$NON-NLS-1$
		
		if(buttonAlert.drawButton) {
			buttonList.add(buttonAlert);
		}
		buttonList.add(buttonDayNight);
		buttonList.add(buttonFollow);
		buttonList.add(buttonZoomIn);
		buttonList.add(buttonZoomOut);
		buttonList.add(buttonClose);
		buttonList.add(buttonOptions);
		buttonList.add(buttonActions);
	}

	/**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		if(buttonList.isEmpty()) {
			initButtons();
		}
		final boolean smallScale = (mc.gameSettings.guiScale==1);
		final int startX = smallScale ? 60 : 30;
		final int endX = width - 3;
		final int startY = 3;
		final int hgap = 3;
		final int vgap = 3;

		buttonDayNight.setPosition(startX,startY);		
		buttonZoomIn.setPosition(smallScale ? 20 : 8, smallScale ? 64 : 32);
		buttonZoomOut.below(buttonZoomIn, 8).xPosition=buttonZoomIn.xPosition;					
		
		if(width>=420) { // across top
			
			buttonFollow.rightOf(buttonDayNight, hgap).yPosition=startY;
			
			buttonClose.leftOf(endX).yPosition=startY;
			buttonActions.leftOf(buttonClose, hgap).yPosition=startY;
			buttonOptions.leftOf(buttonActions, hgap).yPosition=startY;
			
			if(buttonAlert.drawButton) {
				buttonAlert.below(buttonClose, vgap).leftOf(endX);
			}
			
		} else { // down right
			
			buttonFollow.below(buttonDayNight, hgap).xPosition=startX;
			
			if(buttonAlert.drawButton) {
				buttonAlert.leftOf(endX).yPosition=startY;
				buttonClose.leftOf(endX).below(buttonAlert, vgap);
			} else {
				buttonClose.leftOf(endX).yPosition=startY;
			}
			
			buttonActions.below(buttonClose, hgap).leftOf(endX);
			buttonOptions.below(buttonActions, hgap).leftOf(endX);
			
		}
		
		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		buttonDayNight.enabled = !(underground && PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_CAVES));
	
	}

	@Override
	public void handleMouseInput() {
		
		if(chat!=null && !chat.isHidden()) {
			chat.handleMouseInput();
			//return;
		}

		mx = (Mouse.getEventX() * width) / mc.displayWidth;
		my = height - (Mouse.getEventY() * height) / mc.displayHeight - 1;

		if(Mouse.getEventButtonState()) {
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
		if(chat!=null && !chat.isHidden()) {
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
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
		super.mouseMovedOrUp(mouseX, mouseY, which);
		if(Mouse.isButtonDown(0) && !isScrolling) {
			isScrolling=true;
			msx=mx;
			msy=my;
		} else if(!Mouse.isButtonDown(0) && isScrolling) {
			isScrolling=false;
						
			int blockSize = (int) Math.pow(2,state.currentZoom);
			int mouseDragX = (mx-msx)/blockSize;
			int mouseDragY = (my-msy)/blockSize;
			msx=mx;
			msy=my;
			
			if(gridRenderer!=null) {
				try {
					gridRenderer.move(-mouseDragX, -mouseDragY);
					gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0);
					gridRenderer.setZoom(state.currentZoom);
				} catch(Exception e) {
					logger.severe("Error moving grid: " + e);
				}
			}
			
			setFollow(false);			
			refreshState();
		}
	}

	void zoomIn(){
		if(state.currentZoom<maxZoom){
			setZoom(state.currentZoom+1);
		}
	}
	
	void zoomOut(){
		if(state.currentZoom>minZoom){
			setZoom(state.currentZoom-1);
		}
	}

	private void setZoom(int zoom) {
		if(zoom>maxZoom || zoom<minZoom || zoom==state.currentZoom) {
			return;
		}
		state.currentZoom = zoom;
		buttonZoomOut.enabled = state.currentZoom>minZoom;
		buttonZoomIn.enabled = state.currentZoom<maxZoom;		
		refreshState();
	}
	
	void toggleDayNight() {
		buttonDayNight.toggle();
		if(buttonDayNight.getToggled()) {
			state.overrideMapType(Constants.MapType.day);
		} else {
			state.overrideMapType(Constants.MapType.night);
		}
		refreshState();
	}
	
	void toggleFollow() {
		setFollow(!state.follow);
	}

	void setFollow(Boolean onPlayer) {		
		if(state.follow==onPlayer) {
			return;
		}
		buttonFollow.setToggled(onPlayer);
		state.follow = onPlayer;
		if(state.follow) {
			refreshState();
		} 
	}

	@Override
	protected void keyTyped(char c, int i)
	{		
		if(chat!=null && !chat.isHidden()) {
			chat.keyTyped(c, i);
			return;
		}
		
		switch(i) {
			case Keyboard.KEY_ESCAPE : {	
				UIManager.getInstance().closeAll();
				return;
			}
			case Keyboard.KEY_ADD : {
				zoomIn();
				return;
			}
			case Keyboard.KEY_EQUALS : {
				zoomIn();
				return;
			}
			case Keyboard.KEY_MINUS : {
				zoomOut();
				return;
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
		if(chat!=null) {
			chat.updateScreen();
		}
		layoutButtons();
	}

	@Override
	public void drawBackground(int layer)
	{
		BaseOverlayRenderer.drawRectangle(0, 0, width, height, bgColor, 255);
	}

	void drawPlayerInfo() {
		
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(1f,1f,1f,1f);
		//GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		
		int labelWidth = mc.fontRenderer.getStringWidth(state.playerLastPos) + 10;
		int halfBg = width/2;
		
		BaseOverlayRenderer.drawCenteredLabel(state.playerLastPos, width/2, height-6, 14, 0, playerInfoBgColor, playerInfoFgColor, 205);

	}

	void drawMap() {

        final boolean refreshReady = isRefreshReady();
        if(!isRefreshReady()){
            drawTimer.start();
        }

		sizeDisplay(false);

		int xOffset = 0;
		int yOffset = 0;

		if(isScrolling) {
			int blockSize = (int) Math.pow(2,state.currentZoom);
			int mouseDragX = (mx-msx)/blockSize;
			int mouseDragY = (my-msy)/blockSize;
			
			xOffset = mouseDragX*blockSize;
			yOffset = mouseDragY*blockSize;

		} else if(refreshReady) {
            refreshState();
		} else {
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        }

        boolean moved = false;
        if(state.follow) {
            moved = gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
        }
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, moved, 0, 0);
		gridRenderer.draw(1f, xOffset, yOffset);
        gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset);

        Point playerPixel = gridRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
        if(playerPixel!=null) {
            BaseOverlayRenderer.DrawStep drawStep = new BaseOverlayRenderer.DrawEntityStep((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, EntityHelper.getHeading(mc.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8);
            gridRenderer.draw(xOffset, yOffset, drawStep);
        }

        BaseOverlayRenderer.drawImage(TextureCache.instance().getLogo(), 8, 4, false);
		
		sizeDisplay(true);

        if(!refreshReady){
            drawTimer.pause();
        }
	}
	
	public static void drawMapBackground(JmUI ui) {
		ui.sizeDisplay(false);
        gridRenderer.draw(1f, 0, 0);
		BaseOverlayRenderer.drawImage(TextureCache.instance().getLogo(), 8, 4, false);
		ui.sizeDisplay(true);
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

        refreshTimer.start();
		
		// Update the state first
		state.refresh(mc, player);
		
		// Set/update the grid
		if(state.getDimension() != gridRenderer.getDimension()) {
			setFollow(true);
		}
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
		
		// Center core renderer
        boolean moved = false;
		if(state.follow) {
			Minecraft mc = Minecraft.getMinecraft();
			moved = gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
		} else {
			moved = gridRenderer.setZoom(state.currentZoom);
		}

        if(moved) {
            gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
        }

		// Build list of drawSteps
		state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer);
		
		// Update player pos
		String biomeName = (String) DataCache.instance().get(PlayerData.class).get(EntityKey.biome);			
		state.playerLastPos = Constants.getString("MapOverlay.player_location", 
				Integer.toString((int) mc.thePlayer.posX), 
				Integer.toString((int) mc.thePlayer.posZ), 
				Integer.toString((int) mc.thePlayer.posY), 
				mc.thePlayer.chunkCoordY, 
				biomeName); //$NON-NLS-1$ 	
		
		// Reset timer
		state.updateLastRefresh();

        // Clean up expired tiles
        TileCache.instance().cleanUp();

        refreshTimer.pause();
	}
	
	void openChat(String defaultText) {
		if(chat!=null) {			
			chat.setText(defaultText);
			chat.setHidden(false);
		} else {
	        chat = new MapChat(this, defaultText, false);
	        chat.setWorldAndResolution(mc, width, height);
		}
	}

	@Override
	public void close() {	
		if(chat!=null) {
			chat.close();
		}
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
			return state.shouldRefresh();
		}
	}

	void moveCanvas(int deltaBlockX, int deltaBlockz){
        refreshState();
        gridRenderer.move(deltaBlockX, deltaBlockz);
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
		setFollow(false);
	}	
	
	public static synchronized MapOverlayState state() {
		return state;
	}
	
	public static void reset() {
		state.requireRefresh();
		gridRenderer.clear();
        TileCache.instance().invalidateAll();
        TileCache.instance().cleanUp();
	}

}

