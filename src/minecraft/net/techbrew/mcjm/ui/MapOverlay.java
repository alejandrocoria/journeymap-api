package net.techbrew.mcjm.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityDragon;
import net.minecraft.src.EntityGhast;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.Constants.CoordType;
import net.techbrew.mcjm.Constants.MapType;
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
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.reifnsk.WaypointHelper;
import net.techbrew.mcjm.render.MapBlocks;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;

public class MapOverlay extends GuiScreen {
	
	LinkedList<ZoomLevel> zoomLevels = ZoomLevel.getLevels();
	
	static int currentZoomIndex = 5;
	static ZoomLevel currentZoom;
	
	long entityUpdateInterval = 1000;
	Boolean isScroll = false;
	Boolean zoomInStop = false;
	Boolean hardcore = false;
	int msx, msy, mx, my;

	static Boolean pauseGame = false;
	static int mapScale = 4;
	static int chunkScale = mapScale*16;
	static int overlayScale = 8;
	static ChunkCoordIntPair[] mapBounds = new ChunkCoordIntPair[2];
	static {
		mapBounds[0] = new ChunkCoordIntPair(0,0);
		mapBounds[1] = new ChunkCoordIntPair(0,0);
	}

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

	JourneyMap journeyMap;
	MapOverlayOptions options;
	
	int lastWidth = 0;
	int lastHeight = 0;
	private BufferedImage lastMapImg;
	private Integer lastMapImgTextureIndex;
	private Float lastMapRatio;
	private Integer entityChunkSize;
	
	private Integer logoTextureIndex;
	
	private Integer blockXOffset = 0;
	private Integer blockZOffset = 0;
	
	private BufferedImage lastEntityImg;
	private Integer lastEntityImgTextureIndex;
	long lastEntityUpdate = 0;
	int[] mapBackground = new int[]{0,0,0,100};
	
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
		
		setZoom(currentZoomIndex);
	}

	private void drawButtonBar() {
//		drawRectangle(0,0,width,20,mapBackground[0],mapBackground[1],mapBackground[2],mapBackground[3]);
//		drawRectangle(0,21,width,2,50,50,50,100);
		
		// zoom underlay
		if(options==null) {
			drawRectangle(3,25,20,55,0,0,0,150);
		}
		
		if(logoTextureIndex==null) {
			logoTextureIndex = mc.renderEngine.getTexture(FileHandler.WEB_DIR + "/ico/journeymap40.png");
		}
		drawImage(logoTextureIndex, 1F, 3, 1, 20,20); //$NON-NLS-1$
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
			drawEntityLayer();
			drawButtonBar();		
			if(options==null) {
				super.drawScreen(i, j, f);
			}
			drawPlayerInfo();
			lastWidth = width;
			lastHeight = height;
		} catch(Throwable e) {
			JourneyMap.getLogger().log(Level.SEVERE, "Unexpected exception in MapOverlay.drawScreen(): " + e); //$NON-NLS-1$
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			JourneyMap.announce(error);
		} finally {
			mc.gameSettings.guiScale = oldGuiScale;
		}
		
		if(options!=null) {
			options.drawScreen(i, j, f);
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
			lastMapImg = null;
			break;
		}

		case 3: { // follow
			setFollow(!follow);
			if(follow) eraseCachedEntityImg();
			break;
		}
		case 4: { // zoom in
			zoom(true);
			break;
		}
		case 5: { // zoom out
			zoom(false);
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
		setZoom(currentZoomIndex);
		centerMapOnPlayer();
		
		if(options!=null) {
			options.setWorldAndResolution(minecraft, i, j);
			return;
		}
	}

	int bWidth = 16;
	int bHeight = 16;
	int bHGap = 8;
	int bVGap = 8;

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

		//System.out.println("width=" + width + ", startX=" + startX);

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
	
		
		buttonZoomIn.enabled = currentZoomIndex>=1;
		buttonZoomOut.enabled = currentZoomIndex<zoomLevels.size()-1 && !zoomInStop;
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
				zoom(true);
			} else if(wheel<0) {
				zoom(false);
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
	protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton)
	{
		super.mouseMovedOrUp(mouseX, mouseY, mouseButton);
		if(Mouse.isButtonDown(0) && !isScroll) {
			scrollCanvas(true);
		} else if(!Mouse.isButtonDown(0) && isScroll) {
			scrollCanvas(false);
		}
	}

	void zoom(boolean in){
		if(in && currentZoomIndex>0){
			setZoom(currentZoomIndex-1);
		}else if(!in && currentZoomIndex<zoomLevels.size()-1){
			setZoom(currentZoomIndex+1);
		} else {
			return;
		}
	}

	private void setZoom(int index) {

		// Get the zoom
		final int oldIndex = this.currentZoomIndex;
		currentZoomIndex = index;
		currentZoom = zoomLevels.get(index);
		
		// Get the chunk
		int centerChunkX, centerChunkZ;
		if(follow) {
			centerChunkX = mc.thePlayer.chunkCoordX;
			centerChunkZ = mc.thePlayer.chunkCoordZ;
		} else {
			centerChunkX = (mapBounds[0].chunkXPos + mapBounds[1].chunkXPos) / 2;
			centerChunkZ = (mapBounds[0].chunkZPos + mapBounds[1].chunkZPos) / 2;
		}

		// Check to see if scale is viable
		ChunkCoordIntPair testPair = calculateMaxChunk(currentZoom.scale);
		if(testPair.chunkXPos==mapBounds[1].chunkXPos || testPair.chunkZPos==mapBounds[1].chunkZPos) {
			if(index>oldIndex){
				zoom(false);
				return;
			}else if(index<oldIndex){
				zoom(true);
			} 
		} else {
			
			setScale(currentZoom.scale);
			// TODO: 
			overlayScale = mapScale << 1;
			overlayScale = Math.max(overlayScale, 4);			
			
			centerMapOnChunk(centerChunkX, centerChunkZ);
			zoomInStop = false;	
			// Reset timer for entity updates
			lastEntityUpdate = 0;
		}

	}
	
	void toggleFollow() {
		setFollow(!follow);
	}

	void setFollow(Boolean onPlayer) {
		buttonFollow.setToggled(onPlayer);
		follow = onPlayer;
		if(follow) {
			centerMapOnPlayer();
		} else {
			blockXOffset = 0;
			blockZOffset = 0;
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
		return getBackgroundWidth();
	}

	int getCanvasHeight() {
		return getBackgroundHeight();
	}

	void setScale(int newScale) {
		mapScale = newScale;
		chunkScale = mapScale*16;
	}

	void centerMapOnChunk(int chunkX, int chunkZ) {

		int maxChunksWide = calculateMaxChunksWide(mapScale);
		int maxChunksHigh = calculateMaxChunksHigh(mapScale);  

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
		int maxChunksWide = calculateMaxChunksWide(mapScale);
		int maxChunksHigh = calculateMaxChunksHigh(mapScale);  
		mapBounds[1] = new ChunkCoordIntPair( mapBounds[0].chunkXPos + maxChunksWide,  mapBounds[0].chunkZPos + maxChunksHigh);
	}

	int calculateMaxChunksWide(int aMapScale) {
		int cw = getCanvasWidth()/aMapScale;
		int chunks = cw >> 4;
		if(cw % 16 > 0) {
			chunks++;
		}
		return chunks;
	}

	int calculateMaxChunksHigh(int aMapScale) {
		int ch = getCanvasHeight()/aMapScale;
		int chunks = ch >> 4;
		if(ch % 16 > 0) {
			chunks++;
		}
		return chunks;
	}

	ChunkCoordIntPair calculateMaxChunk(int aMapScale) {
		// determine how many chunks we can display
		int x2 = mapBounds[0].chunkXPos + calculateMaxChunksWide(aMapScale);
		int z2 = mapBounds[0].chunkZPos + calculateMaxChunksHigh(aMapScale);
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
			zoom(true);
			break;
		}
		case Keyboard.KEY_EQUALS : {
			zoom(true);
			break;
		}
		case Keyboard.KEY_MINUS : {
			zoom(false);
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
		this.drawDefaultBackground();
        GL11.glEnable(GL11.GL_BLEND);
		
		if(isScroll) {
			scrollingCanvas();
		}

		if(options==null) {
			drawButtonBar();
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
		
		drawRectangle(halfBg - (labelWidth/2), height-18, labelWidth, 12, mapBackground[0],mapBackground[1],mapBackground[2],220);			
		drawCenteredString(mc.fontRenderer, playerLastPos, getBackgroundWidth()/2, height-16, 0x8888ff);
	}

	void drawMap() {

		// Don't draw if during scroll
		if(isScroll) {
			return;
		}

		// Check player status
		EntityPlayer player = mc.thePlayer;
		if (player==null) {
			JourneyMap.getLogger().warning("Could not get player"); //$NON-NLS-1$
			return;
		}
		
		// Check location
		final int ccx = player.chunkCoordX;				
		final int ccz = player.chunkCoordZ;
		final int ccy = player.chunkCoordY;

		// Check chunk
		final Chunk playerChunk = Utils.getChunkIfAvailable(mc.theWorld, ccx, ccz);
		if(playerChunk==null || !playerChunk.isChunkLoaded) {				
			return;
		}

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
		File worldDir = FileHandler.getWorldDir(mc);
		
		if(effectiveMapType.equals(Constants.MapType.day)) {
			mapBackground = new int[]{34,34,34,100};
		} else {
			mapBackground = new int[]{0,0,0,100};
		}
			
		if(System.currentTimeMillis() >= (lastEntityUpdate+entityUpdateInterval)) {
			
			BufferedImage mapImg = null;
			Integer textureIndex = null;
			Float mapRatio = null;
	
			// Get the map image		
			try {
				final Constants.CoordType cType = Constants.CoordType.convert(effectiveMapType, mc.theWorld.provider.dimensionId);
	
				mapImg = RegionFileHandler.getMergedChunks(worldDir, 
						mapBounds[0].chunkXPos, mapBounds[0].chunkZPos, 
						mapBounds[1].chunkXPos, mapBounds[1].chunkZPos, 
						effectiveMapType, ccy, cType, true, currentZoom);		
				
				// Remove the former map image from the texture cache
				eraseCachedMapImg();
				
				// Determine the display ratio for the new image
				float ratioWidth = getCanvasWidth()*1f/mapImg.getWidth()*1f;
				float ratioHeight = getCanvasHeight()*1f/mapImg.getHeight()*1f;
				mapRatio = Math.max(ratioWidth, ratioHeight);
				
				// Allocate the new map image as a texture
				textureIndex = mc.renderEngine.allocateAndSetupTexture((BufferedImage) mapImg);
				
				lastMapImg = mapImg;
				lastMapRatio = mapRatio;
				lastMapImgTextureIndex = textureIndex;
				entityChunkSize = mapImg.getWidth()/calculateMaxChunksWide(mapScale) * overlayScale;
				
			} catch (IOException e) {
				JourneyMap.getLogger().warning("Could not get merged chunks image from player position: " + ccx + "," + ccz); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			} catch (java.nio.BufferOverflowException e) {
				// Can't use this zoom level
				JourneyMap.getLogger().warning("Could not use zoom level: " + currentZoom); //$NON-NLS-1$ //$NON-NLS-2$
				//zoomLevels.remove(currentZoom);
				zoom(true);
			}
		}

		// Draw the map image
		if(lastMapImgTextureIndex!=null && lastMapImg!=null && lastMapRatio!=null) {
			int maxWidth = calculateMaxChunksWide(mapScale) * chunkScale;
			int maxHeight = calculateMaxChunksHigh(mapScale) * chunkScale;
			drawImage(lastMapImgTextureIndex, 1f, blockXOffset, blockZOffset, maxWidth, maxHeight);
		}

	}
	
	int getScaledEntityX(int chunkX, double posX) {
		int xDelta = chunkX - mapBounds[0].chunkXPos;
		if(chunkX<0) {
			xDelta++;
		}
		int scaledChunkX = (xDelta) * entityChunkSize;		
		int scaledBlockX = (int) (Math.floor(posX) % 16) * (entityChunkSize/16);
		return (scaledChunkX + scaledBlockX);
	}

	int getScaledEntityZ(int chunkZ, double posZ) {
		int zDelta = chunkZ - mapBounds[0].chunkZPos;
		if(chunkZ<0) {
			zDelta++;
		}
		int scaledChunkZ = (zDelta) * entityChunkSize;
		int scaledBlockZ = (int) (Math.floor(posZ) % 16) * (entityChunkSize/16);
		return (scaledChunkZ + scaledBlockZ);
	}

	boolean inBounds(Entity entity) {
		int chunkX = entity.chunkCoordX;
		int chunkZ = entity.chunkCoordZ;
		return (chunkX>=mapBounds[0].chunkXPos && chunkX<=mapBounds[1].chunkXPos && 
				chunkZ>=mapBounds[0].chunkZPos && chunkZ<=mapBounds[1].chunkZPos);
	}
	
	boolean inBounds(Map entityMap) {
		try {
		int chunkX = (Integer) entityMap.get(EntityKey.chunkCoordX);
		int chunkZ = (Integer) entityMap.get(EntityKey.chunkCoordZ);
		return (chunkX>=mapBounds[0].chunkXPos && chunkX<=mapBounds[1].chunkXPos && 
				chunkZ>=mapBounds[0].chunkZPos && chunkZ<=mapBounds[1].chunkZPos);
		} catch(NullPointerException e) {
			return false;
		}
	}

	void drawEntityLayer() {

		// Don't draw if during scroll
		if(isScroll) {
			return;
		}

		BufferedImage entityOverlay = null;
		Integer textureIndex = null; 

		if(lastEntityImg!=null && (System.currentTimeMillis() < (lastEntityUpdate+entityUpdateInterval))) {
			entityOverlay = lastEntityImg;
			textureIndex = lastEntityImgTextureIndex;
		} else {
			
			if(lastMapImg==null) {
				return;
			}
			
			// Null obsolete image
			eraseCachedEntityImg();

			int layerWidth = lastMapImg.getWidth() * overlayScale;
			int layerHeight = lastMapImg.getHeight() * overlayScale;

			entityOverlay = new BufferedImage(layerWidth, layerHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = entityOverlay.createGraphics();			
			g2D.setFont(new Font("Arial", Font.BOLD, 16)); //$NON-NLS-1$
			g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			FontMetrics fm = g2D.getFontMetrics();
			
			if(!hardcore) {
				// Draw nearby mobs
						
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
				
				int cx, cz, x, z;
				double heading;
				BufferedImage entityIcon, locatorImg;
				String filename, owner;
				Boolean isHostile, isPet, isPlayer;
				boolean filterAnimals = (showAnimals!=showPets);
				
				for(Map critter : critters) {
					
					isHostile = Boolean.TRUE.equals(critter.get(EntityKey.hostile));
					
					owner = (String) critter.get(EntityKey.owner);
					isPet = mc.thePlayer.username.equals(owner);					
					
					// Skip animals/pets if needed
					if(filterAnimals && !isHostile) {						
						if(showPets != isPet) {
							continue;
						}
					}
					
					if(inBounds(critter)) {						
						filename = (String) critter.get(EntityKey.filename);
						cx = (Integer) critter.get(EntityKey.chunkCoordX);
						cz = (Integer) critter.get(EntityKey.chunkCoordZ);
						x = (Integer) critter.get(EntityKey.posX);
						z = (Integer) critter.get(EntityKey.posZ);
						heading = (Double) critter.get(EntityKey.heading);
						
						isPlayer = EntityHelper.PLAYER_FILENAME.equals(filename);
								
						// Determine and draw locator
						if(isHostile) {
							locatorImg = EntityHelper.getHostileLocator();
						} else if(isPet) {
							locatorImg = EntityHelper.getPetLocator();
						} else if(isPlayer) {
							locatorImg = EntityHelper.getOtherLocator();
						} else {
							locatorImg = EntityHelper.getNeutralLocator();
						}			
						g2D.setComposite(MapBlocks.SLIGHTLYCLEAR);
						drawEntity(cx, x, cz, z, heading, locatorImg, g2D);
						
						// Draw entity image
						entityIcon = EntityHelper.getEntityImage(filename);
						if(entityIcon!=null) {
							g2D.setComposite(MapBlocks.OPAQUE);
							drawEntity(cx, x, cz, z, 0.0, entityIcon, g2D);
						}
						
						if(isPlayer) {
							
							// Draw Label			
							String username = (String) critter.get(EntityKey.username);
							
							int lx = getScaledEntityX(cx, x);
							int ly = getScaledEntityZ(cz, z) + entityIcon.getHeight();
							
							lx = lx - (fm.stringWidth(username)/2) - entityIcon.getWidth()/2;	
							g2D.setComposite(MapBlocks.OPAQUE);
							g2D.setPaint(Color.black);
							g2D.drawString(username, lx +1, ly + 1);
							g2D.drawString(username, lx +2, ly + 2);
							g2D.drawString(username, lx +3, ly + 3);
							g2D.setPaint(Color.green);
							g2D.drawString(username, lx, ly);
						}
					}
				}
			}			

			// Draw player if within bounds
			if(inBounds(mc.thePlayer)) {
				g2D.setComposite(MapBlocks.OPAQUE);
				drawEntity(mc.thePlayer.chunkCoordX, mc.thePlayer.posX, 
						mc.thePlayer.chunkCoordZ, mc.thePlayer.posZ,
						EntityHelper.getHeading(mc.thePlayer),playerImage, g2D);				
			}
			
			// Draw waypoints
			if(showWaypoints && WaypointHelper.waypointsEnabled()) {
				List<Waypoint> waypoints = (List<Waypoint>) DataCache.instance().get(WaypointsData.class).get(EntityKey.root);

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

				new OverlayWaypointRenderer(mapBounds[0], entityChunkSize, layerWidth, layerHeight, xCutoff, zCutoff).render(waypoints, g2D);
			}
							
			lastEntityImg = entityOverlay;
			try {
				textureIndex = mc.renderEngine.allocateAndSetupTexture(entityOverlay);
			} catch (BufferOverflowException e) {
				JourneyMap.getLogger().info("Couldn't allocate entity texture at overlay scale " + overlayScale); //$NON-NLS-1$
				if(overlayScale>4) {
					overlayScale = 4;
				} else if(overlayScale==4) {
					overlayScale = 2;
				} else {
					overlayScale = 1;
					drawEntityLayer();
				}
				return;
			}
			lastEntityImgTextureIndex = textureIndex;
			lastEntityUpdate = System.currentTimeMillis();

			// Update data
			String biomeName = (String) DataCache.instance().get(PlayerData.class).get(EntityKey.biome);
			
			long vslice = Math.round(mc.thePlayer.posY) >> 4;
			String playerPos = Constants.getString("MapOverlay.player_location", 
					Integer.toString((int) mc.thePlayer.posX), 
					Integer.toString((int) mc.thePlayer.posZ), 
					Integer.toString((int) mc.thePlayer.posY), 
					vslice, 
					biomeName); //$NON-NLS-1$ 
			if(!playerPos.equals(playerLastPos)) {
				playerLastPos = playerPos;
			}

		}

		// Draw the entity layer image
		if(lastEntityImgTextureIndex!=null && lastMapRatio!=null) {
			int maxWidth = calculateMaxChunksWide(mapScale) * chunkScale;
			int maxHeight = calculateMaxChunksHigh(mapScale) * chunkScale;
			
			drawImage(lastEntityImgTextureIndex, 1f, blockXOffset, blockZOffset, maxWidth, maxHeight);
		}
		
	}
	
	/**
	 * Draw the entity's location and heading on the overlay image
	 * using the provided icon.
	 * @param entity
	 * @param entityIcon
	 * @param overlayImg
	 */
	private void drawEntity(int chunkX, double posX, int chunkZ, double posZ, Double heading, BufferedImage entityIcon, Graphics2D g2D) {
		int radius = entityIcon.getWidth()/2;
		int size = entityIcon.getWidth();
		
		int offset = 0;
		int x = getScaledEntityX(chunkX, posX) + offset;
		int y = getScaledEntityZ(chunkZ, posZ) + offset;
		
		final Graphics2D gCopy = (Graphics2D) g2D.create();
		
		gCopy.translate(x, y);
		if(heading!=null) {
			gCopy.rotate(heading);
		}
		gCopy.translate(-radius, -radius);
		gCopy.drawImage(entityIcon, 0, 0, size, size, null);
		gCopy.dispose();
		
	}
	
	/**
	 * Draw the entity's location and heading on the overlay image
	 * using the provided icon.
	 * @param entity
	 * @param entityIcon
	 * @param g2D
	 */
	private void drawEntity(Entity entity, BufferedImage entityIcon, Graphics2D g2D) {
		drawEntity(entity.chunkCoordX, entity.posX, entity.chunkCoordZ, entity.posZ, EntityHelper.getHeading(entity), entityIcon, g2D);
	}
	
	void drawRectangle(int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		Tessellator tessellator = Tessellator.instance;
		
		GL11.glDisable(3553 /*GL_TEXTURE_2D*/);
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);

		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(red,green,blue,alpha);

		tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
		tessellator.addVertexWithUV(x + width, height + y, 0.0D, 1, 1);
		tessellator.addVertexWithUV(x + width, y, 0.0D, 1, 0);
		tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
		tessellator.draw();
		
		GL11.glEnable(3553 /*GL_TEXTURE_2D*/);
	}

	private void drawImage(int bufferedImage, float transparency, int startX, int startY, int srcWidth, int srcHeight) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(transparency, transparency, transparency, transparency);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, bufferedImage);

		tessellator.startDrawingQuads();

		tessellator.addVertexWithUV(startX, srcHeight + startY, 0.0D, 0, 1);
		tessellator.addVertexWithUV(startX + srcWidth, srcHeight + startY, 0.0D, 1, 1);
		tessellator.addVertexWithUV(startX + srcWidth, startY, 0.0D, 1, 0);
		tessellator.addVertexWithUV(startX, startY, 0.0D, 0, 0);
		tessellator.draw();
	}

	void save() {

		if(mc==null) {
			mc = Minecraft.getMinecraft();
		}
		final File worldDir = FileHandler.getWorldDir(mc);
		final File saveDir = FileHandler.getJourneyMapDir();

		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		Constants.MapType checkMapType = mapType;
		if(underground && showCaves) {
			checkMapType = Constants.MapType.underground;
		}
		final Constants.MapType useMapType = checkMapType;
		final File mapFile = new File(saveDir, mc.theWorld.getWorldInfo().getWorldName() + "_" + useMapType + ".png");	 //$NON-NLS-1$ //$NON-NLS-2$

		JourneyMap.announce(Constants.getString("MapOverlay.saving_map_to_file", useMapType)); //$NON-NLS-1$
		close();
		
		JourneyMap.getChunkExecutor().schedule(new Runnable() {
			public void run() {							
				try {			
					new MapSaver().saveMapToFile(worldDir, useMapType, mc.thePlayer.chunkCoordY, mc.theWorld.provider.dimensionId, mapFile);
				} catch (Throwable t) {	
					String error = Constants.getMessageJMERR18(t.getMessage());
					JourneyMap.getLogger().severe(error);
					JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
					JourneyMap.announce(error);
					return;
				}
			}			
		}, 0, TimeUnit.MILLISECONDS);		

	}

	void close() {
		mc.displayGuiScreen(null);
		mc.setIngameFocus();
	}
	
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
    }
    
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        eraseCachedEntityImg();
        eraseCachedMapImg();
    }

	void scrollCanvas(Boolean startScroll) {
		if(startScroll && Mouse.isButtonDown(0)) {
			isScroll=true;
			msx=mx;
			msy=my;
		} else {
			
			int[] offsets = getMouseDragOffsets();
			int xOffset = offsets[0];
			int zOffset = offsets[1];

			if(xOffset==0 && zOffset==0) {
				return;
			}

			ChunkCoordIntPair newCorner = new ChunkCoordIntPair(mapBounds[0].chunkXPos - xOffset, mapBounds[0].chunkZPos - zOffset);
			mapBounds[0] = newCorner;

			if(follow) {    
				centerMapOnPlayer();   
			} else {
				checkBounds();
			}

			// Obsolete the entity image
			eraseCachedEntityImg();

			isScroll=false;

		}
	}

	void scrollingCanvas(){
		if(isScroll) {
			
			// Scroll the cached map image
			if(lastMapImgTextureIndex!=null && lastMapImg!=null) {
							
				int[] offsets = getMouseDragOffsets();
				int xOffset = offsets[0];
				int zOffset = offsets[1];
	
				if(Math.abs(xOffset)>0 || Math.abs(zOffset)>0) {
					setFollow(false);
				} 				
				
				xOffset = xOffset * chunkScale;
				zOffset = zOffset * chunkScale;

				int maxWidth = calculateMaxChunksWide(mapScale) * chunkScale;
				int maxHeight = calculateMaxChunksHigh(mapScale) * chunkScale;
				drawImage(lastMapImgTextureIndex, 1f, xOffset + blockXOffset, zOffset + blockXOffset, maxWidth, maxHeight);
				
				// Draw the entity layer image
				if(lastEntityImgTextureIndex!=null && lastMapRatio!=null && lastEntityImg!=null) {
					drawImage(lastEntityImgTextureIndex, 1f, xOffset + blockXOffset, zOffset + blockZOffset, maxWidth, maxHeight);
				}
			} 			
		} 
		//drawPlayerInfo();
	}
	
	int[] getMouseDragOffsets() {
		int mouseDragX = (mx-msx);
		int mouseDragY = (my-msy);
		float size = chunkScale;
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

	protected void eraseCachedEntityImg() {
		if(lastEntityImgTextureIndex!=null) {
			mc.renderEngine.deleteTexture(lastEntityImgTextureIndex);
			lastEntityUpdate = 0;
			lastEntityImgTextureIndex = null;
			lastEntityImg = null;
		}
	}
	
	protected void eraseCachedMapImg() {
		if(lastMapImg!=null && lastMapImgTextureIndex!=null) {
			mc.renderEngine.deleteTexture(lastMapImgTextureIndex);
			lastMapImg = null;
			lastMapImgTextureIndex = null;
		}
	}
	
	protected void eraseCachedLogoImg() {
		if(logoTextureIndex!=null) {
			mc.renderEngine.deleteTexture(logoTextureIndex);
			logoTextureIndex = null;
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
		options = new MapOverlayOptions(this);
		options.setWorldAndResolution(this.mc, width, height);
	}

}

