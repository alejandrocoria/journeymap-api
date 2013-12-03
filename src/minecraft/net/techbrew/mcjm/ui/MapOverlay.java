package net.techbrew.mcjm.ui;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.data.WaypointsData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer.DrawEntityStep;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer.DrawStep;
import net.techbrew.mcjm.render.overlay.GridRenderer;
import net.techbrew.mcjm.render.overlay.OverlayRadarRenderer;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.ui.dialog.MapChat;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Displays the map as an overlay in-game.
 * 
 * @author mwoodman
 *
 */
public class MapOverlay extends JmUI {

	final static MapOverlayState state = new MapOverlayState();
	final static OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
	final static OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
	static GridRenderer gridRenderer;
	
	private enum ButtonEnum{Alert,DayNight,Follow,ZoomIn,ZoomOut,Options,Actions,Close};
	
	final int minZoom = 0;
	final int maxZoom = 5;
	
	Boolean isScrolling = false;
	int msx, msy, mx, my;	

	Logger logger = JourneyMap.getLogger();
	MapChat chat;

	List<DrawStep> drawStepList = new ArrayList<DrawStep>();
	
	MapButton buttonDayNight, buttonFollow, buttonZoomIn, buttonZoomOut;
	MapButton buttonAlert, buttonOptions, buttonActions, buttonClose;
	
	Color bgColor = new Color(0x22, 0x22, 0x22);
	Color playerInfoFgColor = new Color(0x8888ff);
	Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);
	
	/**
	 * Default constructor
	 */
	public MapOverlay() {
	}
	
    @Override
	public void initGui()
    {			
    	super.allowUserInput = true;
    	Keyboard.enableRepeatEvents(true);
    	initButtons();
    	
    	// When switching dimensions, reset grid
		if(state.getDimension()!=mc.thePlayer.dimension) {
			if(gridRenderer!=null) gridRenderer.clear();
		}

    	chat = new MapChat(this, "", true);
    }

	@Override
	public void drawScreen(int i, int j, float f) {
//		int oldGuiScale = mc.gameSettings.guiScale;
//		mc.gameSettings.guiScale = 2;
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
			
		} finally {
//			mc.gameSettings.guiScale = oldGuiScale;
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
			gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
		}
		
		if(chat!=null) {
			chat.setWorldAndResolution(minecraft, width, height);
			return;
		}
		
		if(gridRenderer!=null) {
			drawMap();
		}
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
					gridRenderer.updateTextures(state.getMapType(), state.getVSlice());
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
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		
		int labelWidth = mc.fontRenderer.getStringWidth(state.playerLastPos) + 10;
		int halfBg = width/2;
		
		BaseOverlayRenderer.drawCenteredLabel(state.playerLastPos, width/2, height-6, 14, 0, playerInfoBgColor, playerInfoFgColor, 205);

	}

	void drawMap() {
		
		scaleResolution(this,false);

		int xOffset = 0;
		int yOffset = 0;
		
		if(isScrolling) {
			
			int blockSize = (int) Math.pow(2,state.currentZoom);
			int mouseDragX = (mx-msx)/blockSize;
			int mouseDragY = (my-msy)/blockSize;
			
			xOffset = mouseDragX*blockSize;
			yOffset = mouseDragY*blockSize;

		} else if(isRefreshReady()) {						
			refreshState();			
		}
			
		if(gridRenderer!=null) {
			gridRenderer.draw(1f, xOffset, yOffset);
			BaseOverlayRenderer.draw(drawStepList, xOffset, yOffset);
		}
				
		BaseOverlayRenderer.drawImage(TextureCache.instance().getLogo(), 8, 4, false); 
		
		scaleResolution(this,true);
				
	}
	
	public static void drawMapBackground(JmUI ui) {
		scaleResolution(ui,false);

		if(gridRenderer!=null) {
			gridRenderer.draw(1f, 0, 0);
		}
				
		BaseOverlayRenderer.drawImage(TextureCache.instance().getLogo(), 8, 4, false); 
		
		scaleResolution(ui,true);
	}
	
	static void scaleResolution(JmUI ui, boolean doScale) {		
		
		Minecraft mc = Minecraft.getMinecraft();
		final int glWidth = doScale ? ui.width : mc.displayWidth;
		final int glHeight = doScale ? ui.height : mc.displayHeight;
		
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
		
		// Update the state first
		state.refresh(mc, player);
		
		// Set/update the grid
		if(gridRenderer==null || !gridRenderer.isUsing(state.getWorldDir(), state.getDimension())) {
			if(gridRenderer!=null) gridRenderer.clear();
			setFollow(true);
			gridRenderer = new GridRenderer(state.getWorldDir(), state.getDimension());
		}			
		
		// Center core renderer
		if(state.follow) {
			Minecraft mc = Minecraft.getMinecraft();
			gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
		} else {
			gridRenderer.setZoom(state.currentZoom);
		}
		gridRenderer.updateTextures(state.getMapType(), state.getVSlice());
		
		// Build list of drawSteps
		drawStepList.clear();
		if(!state.getHardcore()) {
			List<Map> critters = new ArrayList<Map>(16);
			PropertyManager pm = PropertyManager.getInstance();
			if(state.currentZoom>0) {
				if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_ANIMALS) || pm.getBoolean(PropertyManager.Key.PREF_SHOW_PETS)) {
					Map map = (Map) DataCache.instance().get(AnimalsData.class).get(EntityKey.root);
					critters.addAll(map.values());
				}
				if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_VILLAGERS)) {
					Map map = (Map) DataCache.instance().get(VillagersData.class).get(EntityKey.root);
					critters.addAll(map.values());
				}
				if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_MOBS)) {
					Map map = (Map) DataCache.instance().get(MobsData.class).get(EntityKey.root);
					critters.addAll(map.values());
				}
			}
			
			if(pm.getBoolean(PropertyManager.Key.PREF_SHOW_PLAYERS)) {
				Map map = (Map) DataCache.instance().get(PlayersData.class).get(EntityKey.root);
				critters.addAll(map.values());
			}
			
			// Sort to keep named entities last
			Collections.sort(critters, new EntityHelper.EntityMapComparator());
			
			drawStepList.addAll(radarRenderer.prepareSteps(critters, gridRenderer));
		}		
		
		// Draw waypoints
		if(WaypointHelper.waypointsEnabled()) {
			Map map = (Map) DataCache.instance().get(WaypointsData.class).get(EntityKey.root);
			List<Waypoint> waypoints = new ArrayList<Waypoint>(map.values());

			drawStepList.addAll(waypointRenderer.prepareSteps(waypoints, gridRenderer));
		}

		// Draw player if within bounds
		Point playerPixel = gridRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
		if(playerPixel!=null) {
			drawStepList.add(new DrawEntityStep(playerPixel, EntityHelper.getHeading(mc.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8));				
		}			
		
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
		gridRenderer.move(deltaBlockX, deltaBlockz);
		setFollow(false);
		refreshState();
	}	
	
	public static synchronized MapOverlayState state() {
		return state;
	}
	
	public static void reset() {
		state.requireRefresh();
		if(gridRenderer!=null) {
			gridRenderer.clear();
			gridRenderer = null;
		}
	}

}

