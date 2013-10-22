package net.techbrew.mcjm.ui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.DynamicTexture;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.data.WaypointsData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.MapBlocks;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.render.overlay.OverlayEntityRenderer;
import net.techbrew.mcjm.render.overlay.OverlayMapRenderer;
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
	
	static LinkedList<ZoomLevel> zoomLevels = ZoomLevel.getLevels();

	static Integer currentZoomIndex;
	static ZoomLevel currentZoom;
	
	final long refreshInterval = 1000;
	Boolean isScroll = false;
	Boolean hardcore = false;
	int msx, msy, mx, my;

	static Boolean pauseGame = false;
	static int mapScale = 4;
	static int chunkScale = mapScale*16;
	static ChunkCoordIntPair[] mapBounds = new ChunkCoordIntPair[2];
	static {
		mapBounds[0] = new ChunkCoordIntPair(0,0);
		mapBounds[1] = new ChunkCoordIntPair(0,0);
	}
	
	int bWidth = 16;
	int bHeight = 16;
	int bHGap = 8;
	int bVGap = 8;

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

	JourneyMap journeyMap;
	MapOverlayOptions options;
	
	int lastWidth = 0;
	int lastHeight = 0;

	private DynamicTexture logoTexture;
	
	private final Integer blockXOffset = 0;
	private final Integer blockZOffset = 0;
	
	OverlayMapRenderer lastMapRenderer;
	OverlayEntityRenderer lastEntityRenderer;

	MapOverlayState state = null;
	
	long lastRefresh = 0;
	
	MapButton buttonDayNight, buttonFollow,buttonZoomIn,buttonZoomOut;
	MapButton buttonOptions, buttonClose;
	
	BufferedImage playerImage = EntityHelper.getPlayerImage();

	public MapOverlay(JourneyMap journeyMap) {
		super();
		super.allowUserInput = true;
		this.journeyMap = journeyMap;
		this.mc = Minecraft.getMinecraft();
		
		initButtons();		
		
		// Set preferences-based values
		PropertyManager pm = PropertyManager.getInstance();
		showCaves = pm.getBoolean(PropertyManager.Key.PREF_SHOW_CAVES);
		showMonsters = pm.getBoolean(PropertyManager.Key.PREF_SHOW_MOBS);
		showAnimals = pm.getBoolean(PropertyManager.Key.PREF_SHOW_ANIMALS);
		showVillagers = pm.getBoolean(PropertyManager.Key.PREF_SHOW_VILLAGERS);
		showPets = pm.getBoolean(PropertyManager.Key.PREF_SHOW_PETS);
		showPlayers = pm.getBoolean(PropertyManager.Key.PREF_SHOW_PLAYERS);
		showWaypoints = pm.getBoolean(PropertyManager.Key.PREF_SHOW_WAYPOINTS);
		
		if(currentZoomIndex == null) {
			currentZoomIndex = ZoomLevel.getLevels().size()/2;
			setZoom(currentZoomIndex);
		}		
		
		// When switching dimensions, reset follow to true
		if(playerLastDimension!=mc.thePlayer.dimension) {
			playerLastDimension = mc.thePlayer.dimension;
			follow = true;
			centerMapOnPlayer();
		}
	}

	private void drawButtonBar() {	
		
		// zoom buttons enabled/disabled
		buttonZoomIn.enabled = currentZoomIndex>0;
		buttonZoomOut.enabled = currentZoomIndex<zoomLevels.size()-1;
		
		// zoom underlay
		if(options==null) {
			BaseOverlayRenderer.drawRectangle(3,25,20,55,0,0,0,150);
		}
		
		if(logoTexture==null) {
			String path = FileHandler.WEB_DIR + "/ico/journeymap40.png";
			logoTexture = BaseOverlayRenderer.getTexture(path);
		}
		BaseOverlayRenderer.drawImage(logoTexture, 1F, 3, 1, 20,20); 
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		int oldGuiScale = mc.gameSettings.guiScale;
		mc.gameSettings.guiScale = 2;
		try {
			if(follow) {    
				centerMapOnPlayer();                
			}

			drawBackground(0);
			drawMap();	
			drawButtonBar();	
			if(options==null) {				
				super.drawScreen(i, j, f);
			} else {
				options.drawScreen(i, j, f);
			}
			drawPlayerInfo();
			lastWidth = width;
			lastHeight = height;
		} catch(Throwable e) {
			JourneyMap.getLogger().log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + e); //$NON-NLS-1$
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			JourneyMap.getInstance().announce(error);
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
			forceRefresh();
			break;
		}

		case 3: { // follow
			setFollow(!follow);
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
		hardcore = !minecraft.isSingleplayer() && minecraft.theWorld.getWorldInfo().isHardcoreModeEnabled();
		initButtons();
		layoutButtons();
		//setZoom(currentZoomIndex);
		if(follow) centerMapOnPlayer();
		
		if(options!=null) {
			options.setWorldAndResolution(minecraft, i, j);
			return;
		}
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
		if(Mouse.isButtonDown(0) && !isScroll) {
			scrollCanvas(true);
		} else if(!Mouse.isButtonDown(0) && isScroll) {
			scrollCanvas(false);
		}
	}

	void zoomIn(){
		if(currentZoomIndex>0){
			setZoom(currentZoomIndex-1);
		}
	}
	
	void zoomOut(){
		if(currentZoomIndex<zoomLevels.size()-1){
			setZoom(currentZoomIndex+1);
		}
	}

	private void setZoom(int index) {

		// Get the zoom
		final int oldIndex = currentZoomIndex;
		currentZoomIndex = index;
		currentZoom = zoomLevels.get(index);
		
		// Get the chunk
		int centerChunkX, centerChunkZ;
		if(follow) {
			Minecraft mc = Minecraft.getMinecraft();
			centerChunkX = mc.thePlayer.chunkCoordX;
			centerChunkZ = mc.thePlayer.chunkCoordZ;
		} else {
			centerChunkX = (mapBounds[0].chunkXPos + mapBounds[1].chunkXPos) / 2;
			centerChunkZ = (mapBounds[0].chunkZPos + mapBounds[1].chunkZPos) / 2;
		}

		// Check to see if scale is viable
		ChunkCoordIntPair testPair = calculateMaxChunk(getCanvasWidth(), getCanvasHeight(), currentZoom.scale);
		if(testPair.chunkXPos==mapBounds[1].chunkXPos || testPair.chunkZPos==mapBounds[1].chunkZPos) {
			if(index>oldIndex){
				zoomOut();
				return;
			}else if(index<oldIndex){
				zoomIn();
			} 
		} else {
			
			setScale(currentZoom.scale);
			centerMapOnChunk(centerChunkX, centerChunkZ);
			
			// Reset timer for entity updates
			forceRefresh();
		}

	}
	
	void toggleFollow() {
		setFollow(!follow);
	}

	void setFollow(Boolean onPlayer) {
		buttonFollow.setToggled(onPlayer);
		if(follow==onPlayer) {
			return;
		}
		follow = onPlayer;
		if(follow) {
			centerMapOnPlayer();
			forceRefresh();
		}
	}

	void centerMapOnPlayer() {
		centerMapOnChunk(mc.thePlayer.chunkCoordX, mc.thePlayer.chunkCoordZ);
		
	}

	void toggleShowCaves() {	   
		setShowCaves(!showCaves);
	}
	
	static void setShowCaves(Boolean show) {	   
		showCaves = show;
	}

	int getBackgroundWidth() {
		//	   int bw = width-10;
		//	   while(bw % 16 !=0) bw--;
		return width;
	}

	int getBackgroundHeight() {
		//	   int bh = height-10;
		//	   while(bh % 16 !=0) bh--;
		return height;
	}

	int getCanvasWidth() {
		return Math.min(1024, getBackgroundWidth());
	}

	int getCanvasHeight() {
		return Math.min(1024, getBackgroundHeight());
	}

	static void setScale(int newScale) {
		mapScale = newScale;
		chunkScale = mapScale*16;
	}

	void centerMapOnChunk(int chunkX, int chunkZ) {

		int maxChunksWide = calculateMaxChunksWide(getCanvasWidth(), mapScale);
		int maxChunksHigh = calculateMaxChunksHigh(getCanvasHeight(), mapScale);  

		int x1 = chunkX - maxChunksWide/2;
		if(maxChunksWide<5) x1++;
		int z1 = chunkZ - maxChunksHigh/2;

		mapBounds[0] = new ChunkCoordIntPair(x1,z1);
		mapBounds[1] = new ChunkCoordIntPair( mapBounds[0].chunkXPos + maxChunksWide,  mapBounds[0].chunkZPos + maxChunksHigh);
		
		// TODO: Block offsets to truely center player
//		// Adjust block offsets if following player
//		if(follow) {
//			blockXOffset = 0 - (int) (Math.floor(mc.thePlayer.posX % 16) * mapScale )/2;
//			blockZOffset = 0 - (int) (Math.floor(mc.thePlayer.posZ % 16) * mapScale )/2;
//			System.out.println("blockXOffset: " + blockXOffset + ", blockZOffset: " + blockZOffset);
//		} else {
//			blockXOffset = 0;
//			blockZOffset = 0;
//		}
	}

	void checkBounds() {
		int maxChunksWide = calculateMaxChunksWide(getCanvasWidth(), mapScale);
		int maxChunksHigh = calculateMaxChunksHigh(getCanvasHeight(), mapScale);  
		mapBounds[1] = new ChunkCoordIntPair( mapBounds[0].chunkXPos + maxChunksWide,  mapBounds[0].chunkZPos + maxChunksHigh);
	}

	static int calculateMaxChunksWide(int canvasWidth, int aMapScale) {
		int cw = canvasWidth/aMapScale;
		int chunks = cw >> 4;
		if(cw % 16 > 0) {
			chunks++;
		}
		return chunks;
	}

	static int calculateMaxChunksHigh(int canvasHeight, int aMapScale) {
		int ch = canvasHeight/aMapScale;
		int chunks = ch >> 4;
		if(ch % 16 > 0) {
			chunks++;
		}
		return chunks;
	}

	static ChunkCoordIntPair calculateMaxChunk(int canvasWidth, int canvasHeight, int aMapScale) {
		// determine how many chunks we can display
		int x2 = mapBounds[0].chunkXPos + calculateMaxChunksWide(canvasWidth, aMapScale);
		int z2 = mapBounds[0].chunkZPos + calculateMaxChunksHigh(canvasHeight, aMapScale);
		return new ChunkCoordIntPair(x2,z2);
	}

	@Override
	protected void keyTyped(char c, int i)
	{		
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
			case Keyboard.KEY_E : {
				close();
				mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
				break;
			}
			case Keyboard.KEY_W : {
				moveCanvas(i);
				break;
			}
			case Keyboard.KEY_A : {
				moveCanvas(i);
				break;
			}
			case Keyboard.KEY_S : {
				moveCanvas(i);
				break;
			}
			case Keyboard.KEY_D : {
				moveCanvas(i);
				break;
			}
			case Keyboard.KEY_SLASH : {
				if(mc.isSingleplayer()==false) {
					close();
					mc.displayGuiScreen(new GuiChat());
				}
				break;
			}
		}		
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		if(options!=null) {
			options.updateScreen();
			return;
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
		//this.drawDefaultBackground();
		super.drawBackground(0);
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
		int halfBg = getBackgroundWidth()/2;
		
		BaseOverlayRenderer.drawRectangle(halfBg - (labelWidth/2), height-18, labelWidth, 12, 0x22, 0x22, 0x22, 220);			
		drawCenteredString(mc.fontRenderer, playerLastPos, getBackgroundWidth()/2, height-16, 0x8888ff);
	}

	void drawMap() {

		// Don't draw if during scroll
		if(isScroll) {
			scrollingCanvas();
			return;
		}
			
		// Update images
		if(isRefreshReady()) {
			//JourneyMap.getLogger().info("drawMap updating");
			
			clearCaches();
			refreshState();
			
			lastMapRenderer = new OverlayMapRenderer(mapBounds[0], mapBounds[1], getCanvasWidth(), getCanvasHeight(), 0, 0);
			
			int mapBlocksWide = (mapBounds[1].chunkXPos - mapBounds[0].chunkXPos) * 16;
			int mapBlocksHigh = (mapBounds[1].chunkZPos - mapBounds[0].chunkZPos) * 16;
			
			int overlayScale = BaseOverlayRenderer.MAX_TEXTURE_SIZE / lastMapRenderer.getTextureSize();
			
			int layerWidth = mapBlocksWide * overlayScale;
			int layerHeight = mapBlocksHigh * overlayScale;
			int entityChunkSize = 16 * overlayScale; 
			
			lastMapRenderer.setLayerDimensions(layerWidth, layerHeight);
			
			lastEntityRenderer = new OverlayEntityRenderer(mapBounds[0], mapBounds[1], getCanvasWidth(), getCanvasHeight(), layerWidth, layerHeight);

			Graphics2D g2D = lastEntityRenderer.getGraphics();		
			FontMetrics fm = g2D.getFontMetrics();
			
			// Calculate how much of the bottom and right of the layer will be offscreen
			int cw = getCanvasWidth()/mapScale;
			int chunks = cw >> 4;
			int maxWidth = chunks * entityChunkSize;
			if(cw % 16 > 0) {
				maxWidth = maxWidth +  ((cw%16)*(entityChunkSize/16));
			}
			
			int ch = getCanvasHeight()/mapScale;
			chunks = ch >> 4;
			int maxHeight = chunks * entityChunkSize;
			if(ch % 16 > 0) {
				maxHeight = maxHeight + ((ch%16)*(entityChunkSize/16));
			}
			
			int xCutoff = layerWidth-maxWidth;
			int zCutoff = layerHeight-maxHeight;
			
			// Renderer for entities and player
			OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer(lastEntityRenderer, layerWidth, layerHeight, xCutoff, zCutoff, showAnimals, showPets);
						
			if(!hardcore) {
				List<Map> critters = new ArrayList<Map>(16);
				
				if(showAnimals || showPets) {
					critters.addAll((List<Map>) DataCache.instance().get(AnimalsData.class).get(EntityKey.root));
				}
				if(showVillagers) {
					critters.addAll((List<Map>) DataCache.instance().get(VillagersData.class).get(EntityKey.root));
				}
				if(showMonsters) {
					critters.addAll((List<Map>) DataCache.instance().get(MobsData.class).get(EntityKey.root));
				}
				if(!mc.isSingleplayer() && showPlayers) {
					critters.addAll((List<Map>) DataCache.instance().get(PlayersData.class).get(EntityKey.root));
				}
				
				// Sort to keep named entities last
				Collections.sort(critters, new EntityHelper.EntityMapComparator());
				
				radarRenderer.render(critters, g2D);
			}		
			
			// Draw waypoints
			if(showWaypoints && WaypointHelper.waypointsEnabled()) {
				List<Waypoint> waypoints = (List<Waypoint>) DataCache.instance().get(WaypointsData.class).get(EntityKey.root);

				new OverlayWaypointRenderer(lastEntityRenderer, layerWidth, layerHeight, xCutoff, zCutoff).render(waypoints, g2D);
			}

			// Draw player if within bounds
			if(radarRenderer.inBounds(mc.thePlayer)) {
				g2D.setComposite(MapBlocks.OPAQUE);
				radarRenderer.drawEntity(mc.thePlayer.chunkCoordX, (int) Math.floor(mc.thePlayer.posX), 
						mc.thePlayer.chunkCoordZ, (int) Math.floor(mc.thePlayer.posZ),
						EntityHelper.getHeading(mc.thePlayer), false, playerImage, g2D);				
			}			
			
			// Update player pos
			String biomeName = (String) DataCache.instance().get(PlayerData.class).get(EntityKey.biome);			
			playerLastPos = Constants.getString("MapOverlay.player_location", 
					Integer.toString((int) mc.thePlayer.posX), 
					Integer.toString((int) mc.thePlayer.posZ), 
					Integer.toString((int) mc.thePlayer.posY), 
					mc.thePlayer.chunkCoordY, 
					biomeName); //$NON-NLS-1$ 
			
	
			//playerLastPos = mapBounds[0].chunkXPos + "," + mapBounds[0].chunkZPos + " to " +  mapBounds[1].chunkXPos + "," + mapBounds[1].chunkZPos;
			//playerLastPos = getCanvasWidth() + "x" + getCanvasHeight() + " mapTex size: " + lastMapRenderer.getTextureSize() + " entTex size: " + lastEntityRenderer.getTextureSize();
			//playerLastPos = mc.displayWidth + "x" + mc.displayHeight + " vs " + getCanvasWidth() + "x" + getCanvasHeight() + " mapTex size: " + lastMapRenderer.getTextureSize() + " entTex size: " + lastEntityRenderer.getTextureSize();
							
			if(g2D!=null) {
				g2D.dispose();
			}
			
			// Reset timer
			lastRefresh = System.currentTimeMillis();
		}

		// Draw map image
		if(lastMapRenderer!=null && state!=null) {
			lastMapRenderer.render(state, null);
		}
		
		// Draw entity image
		if(lastEntityRenderer!=null && state!=null) {
			lastEntityRenderer.render(state, null);		
		}
				
	}
	
	/**
	 * Get a snapshot of the player's biome, effective map state, etc.
	 */
	void refreshState() {
		// Check player status
		EntityClientPlayerMP player = mc.thePlayer;
		if (player==null) {
			JourneyMap.getLogger().warning("Could not get player"); //$NON-NLS-1$
			return;
		}
		
		// Check location
		final int ccx = player.chunkCoordX;				
		final int ccz = player.chunkCoordZ;
		//final int ccy = player.chunkCoordY;

		// Check chunk
//		final Chunk playerChunk = Utils.getChunkIfAvailable(mc.theWorld, ccx, ccz);
//		if(playerChunk==null || !playerChunk.isChunkLoaded) {
//			JourneyMap.getLogger().fine("Could not get player chunk"); //$NON-NLS-1$
//			return;
//		}

		// Maptype
		Constants.MapType effectiveMapType = null;
		final boolean underground = (Boolean) DataCache.playerDataValue(EntityKey.underground);
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
					
		state = new MapOverlayState(effectiveMapType, currentZoom, worldDir, getCanvasWidth(), getCanvasHeight(), blockXOffset, blockZOffset);
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
					JourneyMap.getLogger().severe(LogFormatter.toString(t));					
					return;
				}
			}			
		}, 0, TimeUnit.MILLISECONDS);		

	}

	void close() {
		mc.displayGuiScreen(null);
		mc.setIngameFocus();
	}
	
    @Override
	public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
    }
    
	@Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        if(lastMapRenderer!=null) {
        	lastMapRenderer.eraseCachedImg();
		}
        if(lastEntityRenderer!=null) {
			lastEntityRenderer.eraseCachedImg();
		}
    }

	void scrollCanvas(Boolean startScroll) {
		if(startScroll && Mouse.isButtonDown(0)) {
			//JourneyMap.getLogger().info("startScroll true");
			isScroll=true;
			msx=mx;
			msy=my;
		} else {
			//JourneyMap.getLogger().info("startScroll false");
			
			int[] offsets = getMouseDragOffsets();
			int xOffset = offsets[0];
			int zOffset = offsets[1];

			if(xOffset==0 && zOffset==0) {
				return;
			}

			ChunkCoordIntPair newCorner = new ChunkCoordIntPair(mapBounds[0].chunkXPos - xOffset, mapBounds[0].chunkZPos - zOffset);
			mapBounds[0] = newCorner;

			setFollow(false);
			checkBounds();

			isScroll=false;
			
			forceRefresh();
//			drawBackground(0);
//			drawMap();
//			drawButtonBar();
//			drawPlayerInfo();
			
		}
	}
	
	boolean isRefreshReady() {
		if(isScroll) {
			return false;
		} else {
			return lastMapRenderer==null || lastEntityRenderer==null || state==null || (System.currentTimeMillis() > (lastRefresh+refreshInterval));
		}
	}
	
	void forceRefresh() {
		state = null;
		clearCaches();		
	}
	
	void clearCaches() {
		if(isScroll) {
			return;
		}
		if(lastMapRenderer!=null) {
        	lastMapRenderer.eraseCachedImg();
		}
        if(lastEntityRenderer!=null) {
			lastEntityRenderer.eraseCachedImg();
		}
	}

	void scrollingCanvas(){
		if(isScroll) {
			
			if(lastMapRenderer==null) {
				return;
			}
			
			int[] offsets = getMouseDragOffsets();
			
			int mapBlocksWide = (mapBounds[1].chunkXPos - mapBounds[0].chunkXPos) * 16;
			int mapBlocksHigh = (mapBounds[1].chunkZPos - mapBounds[1].chunkZPos) * 16;
			int actualMaxImgDim = Math.max(mapBlocksWide, mapBlocksHigh);
			
			double screen = Math.max(getCanvasWidth(), getCanvasHeight());
			int w = calculateMaxChunksWide(getCanvasWidth(), mapScale);
			int h = calculateMaxChunksHigh(getCanvasHeight(), mapScale);
			int max = Math.max(w,h) * chunkScale;
			
			int tex = lastMapRenderer.getTextureSize();
			//int actual = actualMaxImgDim;
			double pct = screen/max;

			double xOffset = pct * (offsets[0] * chunkScale);
			double zOffset = pct * (offsets[1] * chunkScale);
			
			if(lastMapRenderer!=null) {
				lastMapRenderer.draw(.9f, xOffset, zOffset);
			}
			
			if(lastEntityRenderer!=null) {
				int entTex = lastEntityRenderer.getTextureSize();
				double scale = entTex/screen;

				xOffset = xOffset * scale;
				zOffset = zOffset * scale;
				lastEntityRenderer.draw(.6f, xOffset, zOffset);
			}
			
		} 
		//drawPlayerInfo();
	}
	
	int[] getMouseDragOffsets() {
		int mouseDragX = (mx-msx);
		int mouseDragY = (my-msy);
		float size = Math.min(chunkScale, 64);
		int xOffset = (int) Math.floor(mouseDragX / size);
		int zOffset = (int) Math.floor(mouseDragY / size);
		return new int[]{xOffset, zOffset};
	}

	void moveCanvas(int dir){
		ChunkCoordIntPair old;
		switch(dir){
		case Keyboard.KEY_A:
			old = mapBounds[0];
			mapBounds[0] = new ChunkCoordIntPair(old.chunkXPos-1, old.chunkZPos);
			//System.out.println("Pan west");
			break;
		case Keyboard.KEY_D:
			old = mapBounds[0];
			mapBounds[0] = new ChunkCoordIntPair(old.chunkXPos+1, old.chunkZPos);
			//System.out.println("Pan east");
			break;
		case Keyboard.KEY_W:
			old = mapBounds[0];
			mapBounds[0] = new ChunkCoordIntPair(old.chunkXPos, old.chunkZPos-1);
			//System.out.println("Pan north");
			break;
		case Keyboard.KEY_S:
			old = mapBounds[0];
			mapBounds[0] = new ChunkCoordIntPair(old.chunkXPos, old.chunkZPos+1);
			//System.out.println("Pan south");
			break;
		}

		//eraseCachedEntityImg();
		setFollow(false);

		// Set new bounds
		checkBounds();
	}

	protected void eraseCachedLogoImg() {
		if(logoTexture!=null) {
			GL11.glDeleteTextures(logoTexture.getGlTextureId());
			logoTexture = null;
		}
	}

	protected static void launchLocalhost() {
		String port = PropertyManager.getInstance().getString(PropertyManager.Key.WEBSERVER_PORT);
		String url = "http://localhost:" + port; //$NON-NLS-1$
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (IOException e) {
			JourneyMap.getLogger().log(Level.SEVERE, "Could not launch browser with URL: " + url, e); //$NON-NLS-1$
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
		}
	}

	protected static void launchWebsite() {
		String url = JourneyMap.WEBSITE_URL;
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (Throwable e) {
			JourneyMap.getLogger().log(Level.SEVERE, "Could not launch browser with URL: " + url, e); //$NON-NLS-1$
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
		}
	}
	
	void showOptions() {
		if(options==null) {
			try {
				options = new MapOverlayOptions(this);
				options.setWorldAndResolution(this.mc, width, height);
			} catch (Throwable t) {
				JourneyMap.getLogger().severe("Couldn't init Map options: " + LogFormatter.toString(t));
				options = null;
			}
		}
	}

}

