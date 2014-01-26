package net.techbrew.mcjm.ui.map;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.render.draw.DrawEntityStep;
import net.techbrew.mcjm.render.draw.DrawStep;
import net.techbrew.mcjm.render.draw.DrawUtil;
import net.techbrew.mcjm.render.overlay.GridRenderer;
import net.techbrew.mcjm.render.overlay.OverlayRadarRenderer;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapButton;
import net.techbrew.mcjm.ui.UIManager;
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

    StatTimer drawScreenTimer = StatTimer.get("MapOverlay.drawScreen");
    StatTimer drawMapTimer = StatTimer.get("MapOverlay.drawScreen.drawMap");
    StatTimer drawMapTimerWithRefresh = StatTimer.get("MapOverlay.drawScreen.drawMap+refreshState");
	
	/**
	 * Default constructor
	 */
	public MapOverlay() {
        field_146297_k = FMLClientHandler.instance().getClient();
        state.refresh(field_146297_k, field_146297_k.thePlayer);
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
		if(state.getDimension()!=field_146297_k.thePlayer.dimension) {
			gridRenderer.clear();
		}
    }

	@Override
	public void drawScreen(int i, int j, float f) {
		try {
            drawScreenTimer.start();
            func_146270_b(0); // drawBackground
			drawMap();
            super.drawScreen(i, j, f); // Buttons
            if(chat!=null) chat.drawScreen(i, j, f);
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			JourneyMap.getInstance().announce(error);
			close();
		} finally {
            drawScreenTimer.stop();
        }
	}

	@Override
    protected void func_146284_a(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.field_146127_k];
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
	public void func_146280_a(Minecraft minecraft, int width, int height) {
		super.func_146280_a(minecraft, width, height);
		
		state.requireRefresh();

		layoutButtons();			

        if(chat==null) {
            chat = new MapChat("", true);
        }
		if(chat!=null) {
			chat.func_146280_a(minecraft, width, height);
		}

        initGui();
		
		drawMap();
	}

    //        width = field_146294_l;
//        height = field_146295_m;
//        mc = field_146297_k;
//        fontRenderer = super.field_146289_q;
//        buttonList = field_146292_n;

	/**
	 * Set up UI buttons.
	 */
	void initButtons() {
        if(field_146292_n.isEmpty()) {
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

            buttonZoomOut.enabled = state.currentZoom>minZoom;
            buttonZoomIn.enabled = state.currentZoom<maxZoom;

            buttonClose   = new MapButton(ButtonEnum.Close.ordinal(),0,0,60,20,Constants.getString("MapOverlay.close")); //$NON-NLS-1$
            buttonOptions = new MapButton(ButtonEnum.Options.ordinal(),0,0,60,20, Constants.getString("MapOverlay.options")); //$NON-NLS-1$
            buttonActions = new MapButton(ButtonEnum.Actions.ordinal(),0,0,60,20, Constants.getString("MapOverlay.actions")); //$NON-NLS-1$

            if(buttonAlert.drawButton) {
                field_146292_n.add(buttonAlert);
            }
            field_146292_n.add(buttonDayNight);
            field_146292_n.add(buttonFollow);
            field_146292_n.add(buttonZoomIn);
            field_146292_n.add(buttonZoomOut);
            field_146292_n.add(buttonClose);
            field_146292_n.add(buttonOptions);
            field_146292_n.add(buttonActions);
        }
	}

	/**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		if(field_146292_n.isEmpty()) {
			initButtons();
		}
		final boolean smallScale = (field_146297_k.gameSettings.guiScale==1);
		final int startX = smallScale ? 60 : 40;
		final int endX = field_146294_l - 3;
		final int startY = 3;
		final int hgap = 3;
		final int vgap = 3;

		buttonDayNight.setPosition(startX,startY);		
		buttonZoomIn.setPosition(smallScale ? 20 : 8, smallScale ? 64 : 32);
		buttonZoomOut.below(buttonZoomIn, 8).setX(buttonZoomIn.getX());
		
		if(field_146294_l>=420) { // across top
			
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
	public void func_146274_d() { // handleMouseInput
		
		if(chat!=null && !chat.isHidden()) {
			chat.func_146274_d();
			//return;
		}

		mx = (Mouse.getEventX() * field_146294_l) / field_146297_k.displayWidth;
		my = field_146295_m - (Mouse.getEventY() * field_146295_m) / field_146297_k.displayHeight - 1;

		if(Mouse.getEventButtonState()) {
			mouseClicked(mx, my, Mouse.getEventButton());
		} else {
			int wheel = Mouse.getEventDWheel();
			if(wheel>0) {
				zoomIn();
			} else if(wheel<0) {
				zoomOut();
			} else {
                func_146286_b(mx, my, Mouse.getEventButton());
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
			for(int l = 0; l < field_146292_n.size(); l++)
			{
				GuiButton guibutton = (GuiButton)field_146292_n.get(l);
				if(guibutton.func_146116_c(field_146297_k, mouseX, mouseY))
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
	protected void func_146286_b(int mouseX, int mouseY, int which) { // mouseMovedOrUp
		super.func_146286_b(mouseX, mouseY, which);
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
					gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), field_146297_k.displayWidth, field_146297_k.displayHeight, false, 0, 0);
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
		if(i==field_146297_k.gameSettings.keyBindForward.func_151463_i()) { // getkeyCode
			moveCanvas(0,-16);
			return;
		}
		
		// West
		if(i==field_146297_k.gameSettings.keyBindLeft.func_151463_i()) {
			moveCanvas(-16, 0);
			return;
		}
		
		// South
		if(i==field_146297_k.gameSettings.keyBindBack.func_151463_i()) {
			moveCanvas(0,16);
			return;
		}
		
		// East
		if(i==field_146297_k.gameSettings.keyBindRight.func_151463_i()) {
			moveCanvas(16, 0);
			return;
		}
		
		// Open inventory
		if(i==field_146297_k.gameSettings.field_151445_Q.func_151463_i()) { // keyBindInventory
			close();
			field_146297_k.func_147108_a(new GuiInventory(field_146297_k.thePlayer));
			return;
		}
		
		// Open chat
		if(i==field_146297_k.gameSettings.keyBindChat.func_151463_i()) {
			openChat("");
			return;
		}
		
		// Open chat with command prefix (Minecraft.java does this in runTick() )
		if(i==field_146297_k.gameSettings.keyBindCommand.func_151463_i()) {
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
    public void func_146270_b(int layer) //drawBackground
	{
		DrawUtil.drawRectangle(0, 0, field_146294_l, field_146295_m, bgColor, 255);
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
			int mouseDragX = (mx-msx)/blockSize;
			int mouseDragY = (my-msy)/blockSize;
			
			xOffset = mouseDragX*blockSize;
			yOffset = mouseDragY*blockSize;

		} else if(refreshReady) {
            refreshState();
		} else {
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        }

        if(state.follow) {
            gridRenderer.center(field_146297_k.thePlayer.posX, field_146297_k.thePlayer.posZ, state.currentZoom);
        }
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), field_146297_k.displayWidth, field_146297_k.displayHeight, false, 0, 0);
		gridRenderer.draw(1f, xOffset, yOffset);
        gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset);

        Point2D playerPixel = gridRenderer.getPixel(field_146297_k.thePlayer.posX, field_146297_k.thePlayer.posZ);
        if(playerPixel!=null) {
            DrawStep drawStep = new DrawEntityStep(field_146297_k.thePlayer.posX, field_146297_k.thePlayer.posZ, EntityHelper.getHeading(field_146297_k.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8);
            gridRenderer.draw(xOffset, yOffset, drawStep);
        }

        DrawUtil.drawImage(TextureCache.instance().getLogo(), 16, 4, false);

		sizeDisplay(true);

        DrawUtil.drawCenteredLabel(state.playerLastPos, field_146294_l / 2, field_146295_m - 11, playerInfoBgColor, playerInfoFgColor, 205, 1);

        timer.stop();
	}
	
	public static void drawMapBackground(JmUI ui) {
		ui.sizeDisplay(false);
        gridRenderer.draw(1f, 0, 0);
		DrawUtil.drawImage(TextureCache.instance().getLogo(), 16, 4, false);
		ui.sizeDisplay(true);
	}
	
	/**
	 * Get a snapshot of the player's biome, effective map state, etc.
	 */
	void refreshState() {
		// Check player status
		EntityClientPlayerMP player = field_146297_k.thePlayer;
		if (player==null) {
			logger.warning("Could not get player"); //$NON-NLS-1$
			return;
		}

		// Update the state first
		state.refresh(field_146297_k, player);

		if(state.getDimension() != gridRenderer.getDimension()) {
			setFollow(true);
		}
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
		
		// Center core renderer
		if(state.follow) {
			gridRenderer.center(field_146297_k.thePlayer.posX, field_146297_k.thePlayer.posZ, state.currentZoom);
		} else {
			gridRenderer.setZoom(state.currentZoom);
		}

        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), field_146297_k.displayWidth, field_146297_k.displayHeight, true, 0, 0);

		// Build list of drawSteps
		state.generateDrawSteps(field_146297_k, gridRenderer, waypointRenderer, radarRenderer);
		
		// Update player pos
		state.playerLastPos = Constants.getString("MapOverlay.player_location",
				Integer.toString((int) field_146297_k.thePlayer.posX),
				Integer.toString((int) field_146297_k.thePlayer.posZ),
				Integer.toString((int) field_146297_k.thePlayer.posY),
				field_146297_k.thePlayer.chunkCoordY,
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
	        chat.func_146280_a(field_146297_k, field_146294_l, field_146295_m);
		}
	}

	@Override
	public void close() {	
		if(chat!=null) {
			chat.close();
		}
	}

    // TODO FORGE: find superclass function
	// @Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }
	
	boolean isRefreshReady() {
		if(isScrolling) {
			return false;
		} else {
			return state.shouldRefresh(super.field_146297_k);
		}
	}

	void moveCanvas(int deltaBlockX, int deltaBlockz){
        refreshState();
        gridRenderer.move(deltaBlockX, deltaBlockz);
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), field_146297_k.displayWidth, field_146297_k.displayHeight, true, 0, 0);
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

