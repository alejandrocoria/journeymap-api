package net.techbrew.journeymap.ui.map;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.data.PlayerData;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.render.draw.DrawEntityStep;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MapButton;
import net.techbrew.journeymap.ui.UIManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.geom.Point2D;
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

    StatTimer drawScreenTimer = StatTimer.get("MapOverlay.drawScreen");
    StatTimer drawMapTimer = StatTimer.get("MapOverlay.drawScreen.drawMap");
    StatTimer drawMapTimerWithRefresh = StatTimer.get("MapOverlay.drawScreen.drawMap+refreshState");
	
	/**
	 * Default constructor
	 */
	public MapOverlay() {
        mc = FMLClientHandler.instance().getClient();
        state.refresh(mc, mc.thePlayer);
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.setZoom(state.currentZoom);
	}
	
    @Override
	public void initGui()
    {			
    	// TODO: super.allowUserInput = true;
    	Keyboard.enableRepeatEvents(true);
    	initButtons();
    	
    	// When switching dimensions, reset grid
		if(state.getDimension()!=mc.thePlayer.dimension) {
			gridRenderer.clear();
		}
    }

	@Override
	public void drawScreen(int i, int j, float f) {
		try {
            drawScreenTimer.start();
            drawBackground(0); // drawBackground
			drawMap();
            super.drawScreen(i, j, f); // Buttons
            if(chat!=null) chat.drawScreen(i, j, f);
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			ChatLog.announceError(error);
			close();
		} finally {
            drawScreenTimer.stop();
        }
	}

	@Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed

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

		layoutButtons();			

        if(chat==null) {
            chat = new MapChat("", true);
        }
		if(chat!=null) {
			chat.setWorldAndResolution(minecraft, width, height);
		}

        initGui();
		
		drawMap();
	}

    //        width = width;
//        height = height;
//        mc = mc;
//        fontRenderer = super.fontRenderer;
//        buttonList = buttonList;

	/**
	 * Set up UI buttons.
	 */
	void initButtons() {
        if(buttonList.isEmpty()) {
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

//            buttonZoomIn  = new MapButton(ButtonEnum.ZoomIn.ordinal(),0,0,12,12,Constants.getString("MapOverlay.zoom_in"), FileHandler.WEB_DIR + "/img/zoomin.png"); //$NON-NLS-1$ //$NON-NLS-2$
//            buttonZoomOut = new MapButton(ButtonEnum.ZoomOut.ordinal(),0,0,12,12,Constants.getString("MapOverlay.zoom_out"), FileHandler.WEB_DIR + "/img/zoomout.png"); //$NON-NLS-1$ //$NON-NLS-2$

            buttonZoomIn  = new MapButton(ButtonEnum.ZoomIn.ordinal(),0,0,20,20, "+"); //$NON-NLS-1$ //$NON-NLS-2$
            buttonZoomOut = new MapButton(ButtonEnum.ZoomOut.ordinal(),0,0,20,20, "-"); //$NON-NLS-1$ //$NON-NLS-2$

            buttonZoomOut.enabled = state.currentZoom>state.minZoom;
            buttonZoomIn.enabled = state.currentZoom<state.maxZoom;

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
		final int startX = smallScale ? 60 : 40;
		final int endX = width - 3;
		final int startY = 3;
		final int hgap = 3;
		final int vgap = 3;

		buttonDayNight.setPosition(startX,startY);		
		buttonZoomIn.setPosition(smallScale ? 20 : 8, smallScale ? 64 : 32);
		buttonZoomOut.below(buttonZoomIn, 8).setX(buttonZoomIn.getX());
		
		if(width>=420) { // across top
			
			buttonFollow.rightOf(buttonDayNight, hgap).setY(startY);
			
			buttonClose.leftOf(endX).setY(startY);
			buttonActions.leftOf(buttonClose, hgap).setY(startY);
			buttonOptions.leftOf(buttonActions, hgap).setY(startY);
			
			if(buttonAlert.drawButton) {
				buttonAlert.below(buttonClose, vgap).leftOf(endX);
			}
			
		} else { // down right
			
			buttonFollow.below(buttonDayNight, hgap).setX(startX);
			
			if(buttonAlert.drawButton) {
				buttonAlert.leftOf(endX).setY(startY);
				buttonClose.leftOf(endX).below(buttonAlert, vgap);
			} else {
				buttonClose.leftOf(endX).setY(startY);
			}
			
			buttonActions.below(buttonClose, hgap).leftOf(endX);
			buttonOptions.below(buttonActions, hgap).leftOf(endX);
			
		}
		
		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		buttonDayNight.enabled = !(underground && PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_CAVES));
	
	}

	@Override
	public void handleMouseInput() { // handleMouseInput
		
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
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which) { // mouseMovedOrUp
		super.mouseMovedOrUp(mouseX, mouseY, which);
		if(Mouse.isButtonDown(0) && !isScrolling) {
			isScrolling=true;
			msx=mx;
			msy=my;
		} else if(!Mouse.isButtonDown(0) && isScrolling) {
			isScrolling=false;
						
			int blockSize = (int) Math.pow(2,state.currentZoom);
			int mouseDragX = (mx-msx)*2/blockSize;
			int mouseDragY = (my-msy)*2/blockSize;
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
		if(state.currentZoom<state.maxZoom){
			setZoom(state.currentZoom+1);
		}
	}
	
	void zoomOut(){
		if(state.currentZoom>state.minZoom){
			setZoom(state.currentZoom-1);
		}
	}

	private void setZoom(int zoom) {
		if(state.setZoom(zoom))
        {
            buttonZoomOut.enabled = state.currentZoom>state.minZoom;
            buttonZoomIn.enabled = state.currentZoom<state.maxZoom;
		    refreshState();
        }
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

        if(i==Keyboard.KEY_ESCAPE) {
            UIManager.getInstance().closeAll();
            return;
        }
        else if(i==Constants.KB_MAP_ZOOMIN.keyCode) {
            zoomIn();
            return;
        }
        else if(i==Constants.KB_MAP_ZOOMOUT.keyCode) {
            zoomOut();
            return;
        }
        else if(i==Constants.KB_MAP_DAY.keyCode) {
            state.overrideMapType(Constants.MapType.day);
            return;
        }
        else if(i==Constants.KB_MAP_NIGHT.keyCode) {
            state.overrideMapType(Constants.MapType.night);
            return;
        }

		// North
		if(i==mc.gameSettings.keyBindForward.keyCode) { // getkeyCode
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
		if(i==mc.gameSettings.keyBindInventory.keyCode) { // keyBindInventory
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
		DrawUtil.drawRectangle(0, 0, width, height, bgColor, 255);
	}

	void drawMap() {

        final boolean refreshReady = isRefreshReady();
        final StatTimer timer = refreshReady ? drawMapTimerWithRefresh : drawMapTimer;
        timer.start();

		sizeDisplay(false);

		int xOffset = 0;
		int yOffset = 0;

		if(isScrolling) {
			int blockSize = (int) Math.pow(2,state.currentZoom);
			int mouseDragX = (mx-msx)*2/blockSize;
			int mouseDragY = (my-msy)*2/blockSize;
			
			xOffset = (mouseDragX*blockSize);
			yOffset = (mouseDragY*blockSize);

		} else if(refreshReady) {
            refreshState();
		} else {
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        }

        if(state.follow) {
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, state.currentZoom);
        }
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0);
		gridRenderer.draw(1f, xOffset, yOffset);
        gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset, 1f);

        Point2D playerPixel = gridRenderer.getPixel(mc.thePlayer.posX, mc.thePlayer.posZ);
        if(playerPixel!=null) {
            TextureImpl tex = state.currentZoom==0 ? TextureCache.instance().getPlayerLocatorSmall() : TextureCache.instance().getPlayerLocator();
            DrawStep drawStep = new DrawEntityStep(mc.thePlayer.posX, mc.thePlayer.posZ, EntityHelper.getHeading(mc.thePlayer), false, tex, 8);
            gridRenderer.draw(xOffset, yOffset, 1f, drawStep);
        }

        DrawUtil.drawImage(TextureCache.instance().getLogo(), 16, 4, false, 1f);

		sizeDisplay(true);

        DrawUtil.drawCenteredLabel(state.playerLastPos, width / 2, height - 11, playerInfoBgColor, playerInfoFgColor, 205, 1);

        timer.stop();
	}
	
	public static void drawMapBackground(JmUI ui) {
		ui.sizeDisplay(false);
        gridRenderer.draw(1f, 0, 0);
		DrawUtil.drawImage(TextureCache.instance().getLogo(), 16, 4, false, 1f);
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

		// Update the state first
		state.refresh(mc, player);

		if(state.getDimension() != gridRenderer.getDimension()) {
			setFollow(true);
		}
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
		
		// Center core renderer
		if(state.follow) {
			gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, state.currentZoom);
		} else {
			gridRenderer.setZoom(state.currentZoom);
		}

        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);

		// Build list of drawSteps
		state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, 1f);
		
		// Update player pos
		state.playerLastPos = Constants.getString("MapOverlay.player_location",
				Integer.toString((int) mc.thePlayer.posX),
				Integer.toString((int) mc.thePlayer.posZ),
				Integer.toString((int) mc.thePlayer.posY),
				mc.thePlayer.chunkCoordY,
				state.getPlayerBiome()); //$NON-NLS-1$
		
		// Reset timer
		state.updateLastRefresh();

        // Clean up expired tiles
        TileCache.instance().cleanUp();
	}
	
	void openChat(String defaultText) {
		if(chat!=null) {			
			chat.setText(defaultText);
			chat.setHidden(false);
		} else {
	        chat = new MapChat(defaultText, false);
	        chat.setWorldAndResolution(mc, width, height);
		}
	}

	@Override
	public void close() {	
		if(chat!=null) {
			chat.close();
		}
	}

	// @Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }
	
	boolean isRefreshReady() {
		if(isScrolling) {
			return false;
		} else {
			return state.shouldRefresh(super.mc);
		}
	}

	void moveCanvas(int deltaBlockX, int deltaBlockz){
        refreshState();
        gridRenderer.move(deltaBlockX, deltaBlockz);
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
		setFollow(false);
	}

    @Override
    public final boolean doesGuiPauseGame() {
        return false;
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

