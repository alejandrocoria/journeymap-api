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
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.EntityHelper;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
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
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.MapBlocks;

public class MapOverlay extends GuiScreen {
	
	LinkedList<ZoomLevel> zoomLevels = ZoomLevel.getLevels();
	
	static int currentZoomIndex = 3;
	static ZoomLevel currentZoom;
	
	long entityUpdateInterval = 1000;
	Boolean isScroll = false;
	Boolean zoomInStop = false;
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

	static Constants.MapType mapType;
	static Boolean showCaves = true;
	static Boolean showMonsters = true;
	static Boolean showAnimals = true;
	static Boolean showVillagers = true;
	static Boolean showPets = true;
	static Boolean showPlayers = true;
	static Boolean follow = true;
	static String playerLastPos = "0,0"; //$NON-NLS-1$

	JourneyMap journeyMap;
	int lastWidth = 0;
	int lastHeight = 0;
	int overlayScale = 4;
	private ChunkCoordIntPair[] lastMapBounds;
	private BufferedImage lastMapImg;
	private Integer lastMapImgTextureIndex;
	private BufferedImage lastEntityImg;
	private Integer lastEntityImgTextureIndex;
	long lastEntityUpdate = 0;
	int[] mapBackground = new int[]{0,0,0};
	
	MapButton buttonDayNight, buttonFollow,buttonZoomIn,buttonZoomOut;
	MapButton buttonOptions, buttonClose;
	
	BufferedImage playerImage = EntityHelper.getPlayerImage();

	public MapOverlay(JourneyMap journeyMap) {
		super();
		super.allowUserInput = true;
		this.journeyMap = journeyMap;	
		initButtons();
		
	}

	private void drawButtonBar() {
		drawRectangle(0,0,width,10,216,216,216,255);
		drawRectangle(0,10,width,10,200,200,200,255);
		drawRectangle(0,21,width,2,50,50,50,100);
		
		// zoom underlay
		if(mapType==null || mapType.equals(Constants.MapType.day)) {
			drawRectangle(3,20,20,60,0,0,0,80);
		} else {
			drawRectangle(3,20,20,60,0,0,0,80);
		}
		drawImage(mc.renderEngine.getTexture(FileHandler.WEB_DIR + "/ico/journeymap40.png"), 1F, 3, 1, 20,20); //$NON-NLS-1$
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
			super.drawScreen(i, j, f);
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
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
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
		if(lastWidth!=width || lastHeight!=height) {
			int startX = 50;
			int endX = width - 10;
			int offsetX = bWidth + bHGap;
			int offsetY = bHeight + bVGap;

			//System.out.println("width=" + width + ", startX=" + startX);

			buttonDayNight.xPosition = 30;

			buttonFollow.xPosition = 120;
			
			buttonZoomIn.xPosition = 6;
			buttonZoomIn.yPosition = 8 + offsetY;
			buttonZoomOut.xPosition = 6;
			buttonZoomOut.yPosition = 8 + (offsetY*2);

			
			
			buttonOptions.xPosition = endX - 60 - 8 - 60;
			buttonClose.xPosition = endX - 60;			
		}
		
		buttonZoomIn.enabled = currentZoomIndex>=1;
		buttonZoomOut.enabled = currentZoomIndex<zoomLevels.size()-1 && !zoomInStop;
	}

	@Override
	public void handleMouseInput() {

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
		} else { // TODO: This is wonky
			centerChunkX = (int) (getCanvasWidth()/chunkScale/2) + mapBounds[0].chunkXPos;
			centerChunkZ = (int) (getCanvasHeight()/chunkScale/2) + mapBounds[0].chunkZPos;
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
			//System.out.println("Zoom scale: " + scale + ", center chunk: " + centerChunkX + "," + centerChunkZ);
			setScale(currentZoom.scale);
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

		//System.out.println("CenterMapOnChunk: " + chunkX + "," + chunkZ);

		int maxChunksWide = calculateMaxChunksWide(mapScale);
		int maxChunksHigh = calculateMaxChunksHigh(mapScale);  

		int x1 = chunkX - (int) Math.round(maxChunksWide/2);
		int z1 = chunkZ - (int) Math.round(maxChunksHigh/2);
		mapBounds[0] = new ChunkCoordIntPair(x1,z1);
		checkBounds();
	}

	void checkBounds() {
		int maxChunksWide = calculateMaxChunksWide(mapScale);
		int maxChunksHigh = calculateMaxChunksHigh(mapScale);  
		mapBounds[1] = new ChunkCoordIntPair( mapBounds[0].chunkXPos + maxChunksWide,  mapBounds[0].chunkZPos + maxChunksHigh);
	}

	int calculateMaxChunksWide(float aMapScale) {
		int maxChunksWide = (int) Math.ceil(getCanvasWidth()/aMapScale/16);
		while((maxChunksWide-1) % 2 !=0) {
			maxChunksWide++;
		}
		return maxChunksWide;
	}

	int calculateMaxChunksHigh(float aMapScale) {
		int maxChunksHigh = (int) Math.ceil(getCanvasHeight()/aMapScale/16);
		while((maxChunksHigh-1) % 2 !=0) {
			maxChunksHigh++;
		}
		return maxChunksHigh;
	}

	ChunkCoordIntPair calculateMaxChunk(float aMapScale) {
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
		layoutButtons();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return pauseGame;
	}

	@Override
	public void drawBackground(int layer)
	{
		drawRectangle(0,0,width,height,mapBackground[0],mapBackground[1],mapBackground[2],255);

		if(isScroll) {
			scrollingCanvas();
		}

		drawButtonBar();

	}

	void drawPlayerInfo() {
		drawRectangle(0,height-12,width,height,0,0,85,255);		
		drawCenteredString(mc.fontRenderer, playerLastPos, getBackgroundWidth()/2, height-10, 0x8888ff);
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

		BufferedImage mapImg = null;
		Integer textureIndex = null;

		// Use cached image if bounds haven't changed
//		if(lastMapImg!=null && lastMapImgTextureIndex!=null && lastMapBounds!=null && lastMapBounds[0].equals(mapBounds[0]) && lastMapBounds[1].equals(mapBounds[1])) {
//			mapImg = lastMapImg;
//			textureIndex = lastMapImgTextureIndex;
//		} else {
			lastMapBounds = new ChunkCoordIntPair[2];
			lastMapBounds[0] = mapBounds[0];
			lastMapBounds[1] = mapBounds[1];
			//System.out.println("Drawing Map");

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
			Constants.MapType tempMapType = null;
			final boolean underground = (Boolean) DataCache.playerDataValue(EntityKey.underground);
			if(underground && showCaves && !hardcore) {
				tempMapType = Constants.MapType.underground;
			} else {
				if(mapType==null) {
					final long ticks = (mc.theWorld.getWorldTime() % 24000L);
					mapType = ticks<13800 ? Constants.MapType.day : Constants.MapType.night;	
					buttonDayNight.setToggled(mapType.equals(Constants.MapType.day));
				}
				tempMapType = mapType;
			}
			File worldDir = FileHandler.getWorldDir(mc);
			
			if(tempMapType.equals(Constants.MapType.day)) {
				mapBackground = new int[]{34,34,34};
			} else {
				mapBackground = new int[]{0,0,0};
			}

			// Remove the former map image from the texture cache
			eraseCachedMapImg();

			// Get the map image		
			try {
				final Constants.CoordType cType = Constants.CoordType.convert(tempMapType, mc.theWorld.provider.dimensionId);
				//System.out.println("MapOverlay " + currentZoom);
				mapImg = RegionFileHandler.getMergedChunks(worldDir, mapBounds[0].chunkXPos, mapBounds[0].chunkZPos,  mapBounds[1].chunkXPos, mapBounds[1].chunkZPos, 
						tempMapType, ccy, cType, true, currentZoom);		
				
				Graphics2D g2D = mapImg.createGraphics();
				g2D.setColor(Color.MAGENTA);
				g2D.setComposite(MapBlocks.OPAQUE);
				float span = 16 ;
				for(int x = -1 ;x<mapImg.getWidth();x+=span) {
					for(int z = -1;z<mapImg.getHeight();z+=span) {
						g2D.fillRect(x,z, 1, 1);
					}
				}
				
				lastMapImg = mapImg;
				
				textureIndex = mc.renderEngine.allocateAndSetupTexture((BufferedImage) mapImg);
				lastMapImgTextureIndex = textureIndex;
			} catch (IOException e) {
				JourneyMap.getLogger().warning("Could not get merged chunks image from player position: " + ccx + "," + ccz); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			} catch (java.nio.BufferOverflowException e) {
				// Can't use this zoom level
				zoomLevels.remove(currentZoom);
				zoom(true);
			}
		//}

		// Put the map image into a texture and draw it
		if(textureIndex==null) {
			//System.out.println("Why isn't there a texture index?");
		} else {

			drawCenteredImage(textureIndex, 1.0F, mapImg.getWidth(), mapImg.getHeight(), getCanvasWidth(), getCanvasHeight());
		}

		drawPlayerInfo();
	}
	
	int getScaledEntityX(int chunkX, double posX) {
		int scaledChunkX = (chunkX - mapBounds[0].chunkXPos) * chunkScale;
		int scaledBlockX = (int) (Math.round(posX-1) % 16) * mapScale;
		return (scaledChunkX + scaledBlockX) * overlayScale;
	}

	int getScaledEntityZ(int chunkZ, double posZ) {
		int scaledChunkZ = (chunkZ - mapBounds[0].chunkZPos) * chunkScale;
		int scaledBlockZ = (int) (Math.round(posZ-1) % 16) * mapScale;
		return (scaledChunkZ + scaledBlockZ) * overlayScale;
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
			// Null obsolete image
			eraseCachedEntityImg();

			int layerWidth = (int) ((mapBounds[1].chunkXPos - mapBounds[0].chunkXPos)*chunkScale) * overlayScale;
			int layerHeight = (int) ((mapBounds[1].chunkZPos - mapBounds[0].chunkZPos)*chunkScale) * overlayScale;

			entityOverlay = new BufferedImage(layerWidth, layerHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = entityOverlay.createGraphics();
			FontMetrics fm = g2D.getFontMetrics();
			g2D.setFont(new Font("Arial", Font.PLAIN, 20)); //$NON-NLS-1$
			g2D.setComposite(MapBlocks.CLEAR);
			g2D.setPaint(Color.black);
			g2D.fillRect(0,0,entityOverlay.getWidth(),entityOverlay.getHeight());
			g2D.setComposite(MapBlocks.OPAQUE);
			g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);


			BasicStroke circleStroke = new BasicStroke(2F);
			
			if(!hardcore) {
				// Draw nearby mobs
						
				List<Map> critters = new ArrayList<Map>(16);
				
				if(showAnimals || showPets) {
					critters.addAll((List<Map>) DataCache.instance().get(AnimalsData.class).get(EntityKey.root));
				} 
				if(showMonsters) {
					critters.addAll((List<Map>) DataCache.instance().get(MobsData.class).get(EntityKey.root));
				}
				if(showVillagers) {
					critters.addAll((List<Map>) DataCache.instance().get(VillagersData.class).get(EntityKey.root));
				}
				if(!mc.isSingleplayer() && showPlayers) {
					critters.addAll((List<Map>) DataCache.instance().get(PlayersData.class).get(EntityKey.root));
				}

				
				int cx, cz, x, z;
				double heading;
				BufferedImage mobImage;
				String type;
				Boolean hostile;
				boolean filterAnimals = (showAnimals!=showPets);
				
				for(Map critter : critters) {
					
					hostile = (Boolean) critter.get(EntityKey.hostile);
					
					// Skip animals/pets if needed
					if(filterAnimals && Boolean.FALSE.equals(hostile)) {
						String owner = (String) critter.get(EntityKey.owner);
						boolean isPet = mc.thePlayer.username.equals(owner);
						if(showPets != isPet) {
							continue;
						}
					}
					
					if(inBounds(critter)) {						
						type = (String) critter.get(EntityKey.type);
						cx = (Integer) critter.get(EntityKey.chunkCoordX);
						cz = (Integer) critter.get(EntityKey.chunkCoordZ);
						x = (Integer) critter.get(EntityKey.posX);
						z = (Integer) critter.get(EntityKey.posZ);
						heading = (Double) critter.get(EntityKey.heading);
								
						mobImage = EntityHelper.getEntityImage(type);		
						drawEntity(cx, x, cz, z, heading, mobImage, entityOverlay);
						
						if(EntityHelper.PLAYER_TYPE.equals(type)) {
							
							// Label			
							String username = (String) critter.get(EntityKey.username);
							int offset = x - (fm.stringWidth(username)/2);							
							g2D.setPaint(Color.black);
							g2D.drawString(username, offset -2, z + 36);
							g2D.drawString(username, offset +2, z + 40);
							g2D.setPaint(Color.green);
							g2D.drawString(username, offset, z + 38);
						}
					}
				}
			}			

			// Draw player if within bounds
			if(inBounds(mc.thePlayer)) {
				drawEntity(mc.thePlayer, playerImage, entityOverlay);				
			}

			lastEntityImg = entityOverlay;
			try {
				textureIndex = mc.renderEngine.allocateAndSetupTexture(entityOverlay);
			} catch (BufferOverflowException e) {
				JourneyMap.getLogger().warning("Couldn't allocate entity texture at overlay scale " + overlayScale); //$NON-NLS-1$
				if(overlayScale>=2) {
					overlayScale--;
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

		// Draw the composite layer image
		//if(textureIndex!=null) {
			drawCenteredImage(textureIndex, 1.0F, entityOverlay.getWidth(), entityOverlay.getHeight(), getBackgroundWidth(), getBackgroundHeight());
		//}
	}
	
	/**
	 * Draw the entity's location and heading on the overlay image
	 * using the provided icon.
	 * @param entity
	 * @param entityIcon
	 * @param overlayImg
	 */
	private void drawEntity(int chunkX, double posX, int chunkZ, double posZ, double heading, BufferedImage entityIcon, BufferedImage overlayImg) {
		int radius = entityIcon.getWidth()/2;
		
		int x = getScaledEntityX(chunkX, posX);
		int y = getScaledEntityZ(chunkZ, posZ);
		
		// Player icon				
		Graphics2D g2D = overlayImg.createGraphics();
		g2D.setComposite(MapBlocks.OPAQUE);
		
		g2D.translate(x, y);
		g2D.rotate(heading);
		g2D.translate(-radius, -radius);
		g2D.drawImage(entityIcon, 0, 0, null);
	}
	
	/**
	 * Draw the entity's location and heading on the overlay image
	 * using the provided icon.
	 * @param entity
	 * @param entityIcon
	 * @param overlayImg
	 */
	private void drawEntity(Entity entity, BufferedImage entityIcon, BufferedImage overlayImg) {
		drawEntity(entity.chunkCoordX, entity.posX, entity.chunkCoordZ, entity.posZ, EntityHelper.getHeading(entity), entityIcon, overlayImg);
	}

	private void drawCenteredImage(int bufferedImage, float transparency, int srcWidth, int srcHeight, int destWidth, int destHeight) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(transparency, transparency, transparency, transparency);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, bufferedImage);

		// Preserve aspect ratio of source image
		float destHeightAdjusted = destWidth*srcHeight/srcWidth;
		float destWidthAdjusted = destWidth; //destHeight*srcWidth/srcHeight;

		float offsetWidth = (width-destWidthAdjusted)/2;
		float offsetHeight = (height-destHeightAdjusted)/2;
		tessellator.startDrawingQuads();

		tessellator.addVertexWithUV(offsetWidth, destHeightAdjusted + offsetHeight, 0.0D, 0, 1);
		tessellator.addVertexWithUV(offsetWidth + destWidthAdjusted, destHeightAdjusted + offsetHeight, 0.0D, 1, 1);
		tessellator.addVertexWithUV(offsetWidth + destWidthAdjusted, offsetHeight, 0.0D, 1, 0);
		tessellator.addVertexWithUV(offsetWidth, offsetHeight, 0.0D, 0, 0);
		tessellator.draw();
	}
	
	private void drawRectangle(int x, int y, int width, int height, int red, int green, int blue, int alpha) {
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
			int mouseDragX = (mx-msx);
			int mouseDragY = (my-msy);
			//System.out.println("Mouse dragged: " + mouseDragX + "," + mouseDragY);

			int xOffset = (int) Math.ceil(mouseDragX / chunkScale);
			int zOffset = (int) Math.ceil(mouseDragY / chunkScale);

			if(xOffset==0 && zOffset==0) {
				return;
			}
			//System.out.println("Scrolled: " + xOffset + "," + zOffset);

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
			//document.body.style.cursor = "move";
			//getMouse(e);

			int mouseDragX = (mx-msx);
			int mouseDragY = (my-msy);
			int xOffset = (int) Math.ceil(mouseDragX / chunkScale);
			int zOffset = (int) Math.ceil(mouseDragY / chunkScale);

			//System.out.println("Scrolling: " + xOffset + "," + zOffset);

			if(Math.abs(xOffset)>0 || Math.abs(zOffset)>0) {
				setFollow(false);
			} 

			// Then scroll the cached map image
			if(lastMapImgTextureIndex!=null && lastMapImg!=null) {

				Tessellator tessellator = Tessellator.instance;
				GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
				GL11.glDepthMask(false);
				GL11.glBlendFunc(770, 771);
				GL11.glColor4f(1F, 1F, 1F, 1F);
				GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
				GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, lastMapImgTextureIndex);

				// calculate display ratio

				float destHeightAdjusted = getCanvasWidth()*lastMapImg.getHeight(null)/lastMapImg.getWidth(null);
				float destWidthAdjusted = getCanvasWidth(); 

				float offsetWidth = ((width-destWidthAdjusted)/2) + (xOffset*chunkScale);
				float offsetHeight = ((height-destHeightAdjusted)/2) + (zOffset*chunkScale);
				tessellator.startDrawingQuads();

				tessellator.addVertexWithUV(offsetWidth, destHeightAdjusted + offsetHeight, 0.0D, 0, 1);
				tessellator.addVertexWithUV(offsetWidth + destWidthAdjusted, destHeightAdjusted + offsetHeight, 0.0D, 1, 1);
				tessellator.addVertexWithUV(offsetWidth + destWidthAdjusted, offsetHeight, 0.0D, 1, 0);
				tessellator.addVertexWithUV(offsetWidth, offsetHeight, 0.0D, 0, 0);
				tessellator.draw();


			} else {
				//System.out.println("Why isn't there a cached map image?");
			}

			drawPlayerInfo();
		} 
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

		eraseCachedEntityImg();
		setFollow(false);

		// Set new bounds
		checkBounds();
	}

	protected void eraseCachedEntityImg() {
		if(lastEntityImgTextureIndex!=null) {
			mc.renderEngine.deleteTexture(lastEntityImgTextureIndex);
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

	protected static void launchLocalhost() {
		String port = PropertyManager.getInstance().getString(PropertyManager.WEBSERVER_PORT_PROP);
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
		MapOverlayOptions opts = new MapOverlayOptions(this);
		mc.displayGuiScreen(opts);
		this.lastWidth = 0;
		this.lastHeight = 0;
	}

}

