package net.techbrew.journeymap.ui.map;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.render.draw.DrawEntityStep;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.layer.LayerDelegate;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
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
    final LayerDelegate layerDelegate = new LayerDelegate();
    final FullMapProperties fullMapProperties = JourneyMap.getInstance().fullMapProperties;

    private enum ButtonEnum{Alert,DayNight,Follow,ZoomIn,ZoomOut,Options,Actions,Close,Mode,WaypointManager}
	
	Boolean isScrolling = false;
	int msx, msy, mx, my;

	Logger logger = JourneyMap.getLogger();
	MapChat chat;
	
	Button buttonDayNight, buttonFollow, buttonZoomIn, buttonZoomOut;
	Button buttonAlert, buttonOptions, buttonActions, buttonClose;
    Button buttonMode, buttonWaypointManager;

    ButtonList rightButtons;
	
	Color bgColor = new Color(0x22, 0x22, 0x22);
	Color playerInfoFgColor = Color.lightGray;
	Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);

    StatTimer drawScreenTimer = StatTimer.get("MapOverlay.drawScreen");
    StatTimer drawMapTimer = StatTimer.get("MapOverlay.drawScreen.drawMap");
    StatTimer drawMapTimerWithRefresh = StatTimer.get("MapOverlay.drawScreen.drawMap+refreshState");
	
	/**
	 * Default constructor
	 */
	public MapOverlay() {
        super(null);
        mc = FMLClientHandler.instance().getClient();
        state.refresh(mc, mc.thePlayer, fullMapProperties);
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.setZoom(state.currentZoom);
	}
	
    @Override
	public void initGui()
    {
    	Keyboard.enableRepeatEvents(true);
    	initButtons();

    	// When switching dimensions, reset grid
		if(state.getDimension()!=mc.thePlayer.dimension) {
			gridRenderer.clear();
		}
    }

	@Override
	public void drawScreen(int width, int height, float f) {
		try {
            drawScreenTimer.start();
            drawBackground(0); // drawBackground
			drawMap();

            layoutButtons();
            //ButtonList.drawOutlines(1, Color.black, 96, buttonList);
            for (int k = 0; k < this.buttonList.size(); ++k)
            {
                GuiButton guibutton = (GuiButton)this.buttonList.get(k);
                guibutton.drawButton(this.mc, width, height);
            }

            if(chat!=null) chat.drawScreen(width, height, f);

		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + LogFormatter.toString(e)); //$NON-NLS-1$
			UIManager.getInstance().closeAll();
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
                UIManager.getInstance().openMasterOptions();
                break;
			}
			case Actions: { // actions
				UIManager.getInstance().openMapActions();
				break;
			}
            case WaypointManager: {
                UIManager.getInstance().openWaypointManager(null);
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

	/**
	 * Set up UI buttons.
	 */
	void initButtons() {
        if(buttonList.isEmpty()) {

            FontRenderer fr = getFontRenderer();

            String on = Constants.getString("MapOverlay.on"); //$NON-NLS-1$
            String off = Constants.getString("MapOverlay.off"); //$NON-NLS-1$

            buttonAlert = new Button(ButtonEnum.Alert.ordinal(),0,0, Constants.getString("MapOverlay.update_available")); //$NON-NLS-1$
            buttonAlert.fitWidth(fr);
            buttonAlert.drawButton = VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent();

            buttonDayNight = new Button(ButtonEnum.DayNight.ordinal(),0,0,80,20,
                    Constants.getString("MapOverlay.day"), //$NON-NLS-1$
                    Constants.getString("MapOverlay.night"), //$NON-NLS-1$
                    state.getMapType(fullMapProperties.showCaves.get()) == Constants.MapType.day);
            buttonDayNight.fitWidth(fr);

            buttonFollow = new Button(ButtonEnum.Follow.ordinal(),0,0,80,20,
                    Constants.getString("MapOverlay.follow", on), //$NON-NLS-1$
                    Constants.getString("MapOverlay.follow", off), //$NON-NLS-1$
                    state.follow); //$NON-NLS-1$ //$NON-NLS-2$
            buttonFollow.fitWidth(fr);

            buttonZoomIn  = new Button(ButtonEnum.ZoomIn.ordinal(),0,0,20,20, "+"); //$NON-NLS-1$ //$NON-NLS-2$
            buttonZoomIn.noDisableText = true;

            buttonZoomOut = new Button(ButtonEnum.ZoomOut.ordinal(),0,0,20,20, "-"); //$NON-NLS-1$ //$NON-NLS-2$
            buttonZoomOut.noDisableText = true;

            buttonZoomOut.enabled = state.currentZoom>state.minZoom;
            buttonZoomIn.enabled = state.currentZoom<state.maxZoom;

            buttonClose   = new Button(ButtonEnum.Close.ordinal(),0,0,60,20,Constants.getString("MapOverlay.close")); //$NON-NLS-1$
            buttonClose.fitWidth(fr);

            buttonOptions = new Button(ButtonEnum.Options.ordinal(),0,0,60,20, Constants.getString("MapOverlay.options")); //$NON-NLS-1$
            buttonOptions.fitWidth(fr);
            buttonActions = new Button(ButtonEnum.Actions.ordinal(),0,0,60,20, Constants.getString("MapOverlay.actions")); //$NON-NLS-1$
            buttonActions.fitWidth(fr);

//            buttonMode = new Button(ButtonEnum.Mode.ordinal(),0,0,60,20, Constants.getString("MapOverlay.mode")); //$NON-NLS-1$
//            buttonMode.fitWidth(fr);

            buttonWaypointManager = new Button(ButtonEnum.WaypointManager.ordinal(),0,0,60,20, Constants.getString("Waypoint.waypoints")); //$NON-NLS-1$
            buttonWaypointManager.fitWidth(fr);
            buttonWaypointManager.drawButton = WaypointsData.isNativeEnabled();

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
            buttonList.add(buttonWaypointManager);

            rightButtons = new ButtonList(buttonAlert, buttonWaypointManager, buttonOptions, buttonActions, buttonClose);
            Collections.reverse(rightButtons);

            //ButtonList.setHeights(mc.fontRenderer.FONT_HEIGHT+5, buttonList);
        }
	}

	/**
	 * Center buttons in UI.
	 */
    @Override
	protected void layoutButtons() {
		// Buttons
		if(buttonList.isEmpty()) {
			initButtons();
		}

		final int startX = 40;
		final int endX = width - 3;
		final int startY = 3;
		final int hgap = 3;
		final int vgap = 3;

        buttonZoomIn.setPosition(8, 32);
        buttonZoomOut.below(buttonZoomIn, 8).setX(8);

        final boolean underground = (Boolean) DataCache.playerDataValue(EntityKey.underground) && FeatureManager.isAllowed(Feature.MapCaves) && JourneyMap.getInstance().fullMapProperties.showCaves.get();
        buttonDayNight.enabled = !(underground);
		buttonDayNight.setPosition(startX,startY);

        buttonWaypointManager.drawButton = JourneyMap.getInstance().waypointProperties.enabled.get();

        buttonFollow.rightOf(buttonDayNight, hgap).setY(startY);

        int rightX = buttonFollow.getX()+buttonFollow.getWidth()+hgap+hgap;
        if(rightX <= width - rightButtons.getWidth(hgap))
        {
            if(!rightButtons.isHorizontal())
            {
                rightButtons.setFitWidths(mc.fontRenderer);
            }
            rightButtons.layoutHorizontal(endX, startY, false, hgap);
        }
        else
        {
            if(rightButtons.isHorizontal())
            {
                rightButtons.setUniformWidths(mc.fontRenderer);
            }
            rightButtons.layoutVertical(endX, startY, false, vgap);
        }
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
        if(!guiButtonUsed)
        {
            BlockCoordIntPair blockCoord = gridRenderer.getBlockUnderMouse(mouseX, mouseY, width, height);
            double gridMouseX = (1.0 * mouseX * gridRenderer.getWidth()) / width;
            double gridMouseY = (1.0 * mouseY * gridRenderer.getHeight()) / height;
            layerDelegate.onMouseClicked(mc, gridMouseX, gridMouseY, gridRenderer.getWidth(), gridRenderer.getHeight(), blockCoord, mouseButton);
        }
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which) { // mouseMovedOrUp
		super.mouseMovedOrUp(mouseX, mouseY, which);

        if(Mouse.isButtonDown(0) && mouseOverButtons(mouseX, mouseY)) {
            return;
        }

        int blockSize = (int) Math.pow(2,state.currentZoom);

		if(Mouse.isButtonDown(0) && !isScrolling) {
			isScrolling=true;
			msx=mx;
			msy=my;
		} else if(!Mouse.isButtonDown(0) && isScrolling) {
			isScrolling=false;
			int mouseDragX = (mx-msx)*2/blockSize;
			int mouseDragY = (my-msy)*2/blockSize;
			msx=mx;
			msy=my;

            try {
                gridRenderer.move(-mouseDragX, -mouseDragY);
                gridRenderer.updateTextures(state.getMapType(fullMapProperties.showCaves.get()), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0, fullMapProperties);
                gridRenderer.setZoom(state.currentZoom);
            } catch(Exception e) {
                logger.severe("Error moving grid: " + e);
            }

			setFollow(false);			
			refreshState();
		}

        if(!isScrolling && which==-1) {
            BlockCoordIntPair blockCoord = gridRenderer.getBlockUnderMouse(mouseX, mouseY, width, height);
            double gridMouseX = (1.0 * mouseX * gridRenderer.getWidth()) / width;
            double gridMouseY = (1.0 * mouseY * gridRenderer.getHeight()) / height;
            layerDelegate.onMouseMove(mc, gridMouseX, gridMouseY, gridRenderer.getWidth(), gridRenderer.getHeight(), blockCoord);
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
	public void keyTyped(char c, int i)
	{		
		if(chat!=null && !chat.isHidden()) {
			chat.keyTyped(c, i);
			return;
		}

        if(i==Keyboard.KEY_ESCAPE || Constants.isPressed(Constants.KB_MAP)) {
            UIManager.getInstance().closeAll();
            return;
        }
        else if(Constants.isPressed(Constants.KB_MAP_ZOOMIN)) {
            zoomIn();
            return;
        }
        else if(Constants.isPressed(Constants.KB_MAP_ZOOMOUT)) {
            zoomOut();
            return;
        }
        else if(Constants.isPressed(Constants.KB_MAP_DAY)) {
            state.overrideMapType(Constants.MapType.day);
            return;
        }
        else if(Constants.isPressed(Constants.KB_MAP_NIGHT)) {
            state.overrideMapType(Constants.MapType.night);
            return;
        }
        else if(Constants.isPressed(Constants.KB_WAYPOINT)) {
            Waypoint waypoint = Waypoint.of(mc.thePlayer);
            UIManager.getInstance().openWaypointEditor(waypoint, true, null);
            return;
        }

		// North
        if(Constants.isPressed(mc.gameSettings.keyBindForward)) {
			moveCanvas(0,-16);
			return;
		}
		
		// West
        if(Constants.isPressed(mc.gameSettings.keyBindLeft)) {
			moveCanvas(-16, 0);
			return;
		}
		
		// South
        if(Constants.isPressed(mc.gameSettings.keyBindBack)) {
			moveCanvas(0,16);
			return;
		}
		
		// East
        if(Constants.isPressed(mc.gameSettings.keyBindRight)) {
			moveCanvas(16, 0);
			return;
		}
		
		// Open inventory
        if(Constants.isPressed(mc.gameSettings.keyBindInventory)) {
			UIManager.getInstance().openInventory();
			return;
		}
		
		// Open chat
        if(Constants.isPressed(mc.gameSettings.keyBindChat)) {
			openChat("");
			return;
		}
		
		// Open chat with command prefix (Minecraft.java does this in runTick() )
        if(Constants.isPressed(mc.gameSettings.keyBindCommand)) {
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

        boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, fullMapProperties.forceUnicode.get());

        if(state.follow) {
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, state.currentZoom);
        }
        gridRenderer.updateTextures(state.getMapType(fullMapProperties.showCaves.get()), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0, fullMapProperties);
        gridRenderer.draw(1f, xOffset, yOffset);
        gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset, 1f, getMapFontScale());

        Point2D playerPixel = gridRenderer.getPixel(mc.thePlayer.posX, mc.thePlayer.posZ);
        if(playerPixel!=null) {
            TextureImpl tex = state.currentZoom==0 ? TextureCache.instance().getPlayerLocatorSmall() : TextureCache.instance().getPlayerLocator();
            DrawStep drawStep = new DrawEntityStep(mc.thePlayer.posX, mc.thePlayer.posZ, EntityHelper.getHeading(mc.thePlayer), false, tex, 8);
            gridRenderer.draw(xOffset, yOffset, 1f, getMapFontScale(), drawStep);
        }

        gridRenderer.draw(layerDelegate.getDrawSteps(), xOffset, yOffset, 1f, getMapFontScale());

        DrawUtil.drawLabel(state.playerLastPos, mc.displayWidth / 2, mc.displayHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, playerInfoBgColor, 235, playerInfoFgColor, 255, getMapFontScale(), true);

        if(unicodeForced) DrawUtil.stopUnicode(mc.fontRenderer);

        drawLogo();

		sizeDisplay(true);

        timer.stop();
	}

    private int getMapFontScale()
    {
        return (fullMapProperties.fontSmall.get() ? 1 : 2) * (fullMapProperties.forceUnicode.get() ? 2 : 1);
    }

    public void centerOn(Waypoint waypoint)
    {
        if(waypoint.getDimensions().contains(mc.thePlayer.dimension))
        {
            state.follow = false;
            state.requireRefresh();
            int x = waypoint.getX(mc.thePlayer.dimension);
            int z = waypoint.getZ(mc.thePlayer.dimension);

            gridRenderer.center(x, z, state.currentZoom);
            refreshState();
            updateScreen();
        }
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
		state.refresh(mc, player, fullMapProperties);

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

        gridRenderer.updateTextures(state.getMapType(fullMapProperties.showCaves.get()), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0, fullMapProperties);

		// Build list of drawSteps
		state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, fullMapProperties, 1f);
		
		// Update player pos
		state.playerLastPos = Constants.getString("MapOverlay.location_xzyeb",
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
        gridRenderer.updateTextures(state.getMapType(fullMapProperties.showCaves.get()), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0, fullMapProperties);
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

