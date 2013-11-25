package net.techbrew.mcjm.ui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.GuiButton;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.task.MapRegionTask;
import net.techbrew.mcjm.task.SaveMapTask;
import net.techbrew.mcjm.ui.dialog.AutoMapConfirmation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class MapOverlayActions extends JmUI {

	private final Logger logger = JourneyMap.getLogger();
	
	final String title = Constants.getString("MapOverlay.actions_title", JourneyMap.JM_VERSION);
	int lastWidth = 0;
	int lastHeight = 0;
	MapButton buttonAutomap, buttonSave, buttonClose, buttonBrowser;
	Color titleColor = new Color(0,0,100);
	
	public MapOverlayActions() {
		
	}
	
	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        this.buttonList.clear();
        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");

		buttonSave = new MapButton(6,0,0,Constants.getString("MapOverlay.save_map")); //$NON-NLS-1$ 
		buttonClose = new MapButton(7,0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 
		buttonBrowser = new MapButton(9,0,0,Constants.getString("MapOverlay.use_browser")); //$NON-NLS-1$ 
		
		buttonAutomap = new MapButton(17,0,0,
				Constants.getString("MapOverlay.automap_title", on),
				Constants.getString("MapOverlay.automap_title", off),
				PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED)); //$NON-NLS-1$ //$NON-NLS-2$
		buttonAutomap.setHoverText(Constants.getString("MapOverlay.automap_text")); //$NON-NLS-1$
		buttonAutomap.enabled = Minecraft.getMinecraft().isSingleplayer();
	
		buttonList.add(buttonSave);
		buttonList.add(buttonClose);
		buttonList.add(buttonBrowser);
		buttonList.add(buttonAutomap);
    }
    
    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}
		
		buttonSave.enabled = !PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED);
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;
			
			final int rowHeight = 22;
			final int hgap = 160;
			final int bx = this.width / 2 - hgap + 5;
			final int by = (this.height / 4);
			int row = 0;
			
			layoutButton(buttonAutomap, bx + hgap/2, by + (rowHeight*row++));	
			layoutButton(buttonSave, bx + hgap/2, by + (rowHeight*row++));				
			layoutButton(buttonBrowser, bx + hgap/2, by + (rowHeight*row++));	
			
			row++;
			layoutButton(buttonClose, bx + hgap/2, by + (rowHeight*row++));	

		}
		
	}
	
	private void layoutButton(GuiButton guibutton, int x, int y) {
		guibutton.xPosition = x;
		guibutton.yPosition = y;
	}


    @Override
	protected void actionPerformed(GuiButton guibutton) {
    	
		switch(guibutton.id) {
			case 6: { // save
				save();
				UIManager.getInstance().openMap();
				break;
			}
			case 7: { // close
				UIManager.getInstance().openMap();
				break;
			}

			case 9: { // browser
				launchLocalhost();
				UIManager.getInstance().openMap();
				break;
			}
			
			case 17: { // automap
				
				boolean enable = !PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED);
				if(enable) {
					UIManager.getInstance().open(AutoMapConfirmation.class);
				} else {
					JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, false, null);
					buttonAutomap.setToggled(false);
					PropertyManager.getInstance().setProperty(PropertyManager.Key.AUTOMAP_ENABLED, false);
					updateScreen();
				}
				break;
			}
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
	
    void save() {
		if(mc==null) {
			mc = Minecraft.getMinecraft();
		}
		final File worldDir = FileHandler.getJMWorldDir(mc);
		final File saveDir = FileHandler.getJourneyMapDir();

		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		final Integer vSlice = underground ? mc.thePlayer.chunkCoordY : null;
		Constants.MapType checkMapType = MapOverlay.mapType;
		if(underground && MapOverlay.showCaves) {
			checkMapType = Constants.MapType.underground;
		}
		final Constants.MapType useMapType = checkMapType;
		final MapSaver mapSaver = new MapSaver(worldDir, useMapType, vSlice , mc.thePlayer.dimension);
		if(mapSaver.isValid()) {
			JourneyMap.getInstance().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
			JourneyMap.getInstance().announce(Constants.getString("MapOverlay.save_filename", mapSaver.getSaveFileName())); //$NON-NLS-1$
		}		
		close();
	}

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {        
    	drawBackground(0);
    	layoutButtons();
    	super.drawScreen(par1, par2, par3);
    	drawTitle();
    }
    
    @Override
	public void drawBackground(int layer)
	{    	
    	super.drawBackground(0);
    	MapOverlay.drawMapBackground(this);
    	super.drawDefaultBackground();
	}
    
    private void drawTitle() {
    	int labelWidth = mc.fontRenderer.getStringWidth(title) + 10;
		int halfBg = width/2;
		
		int by = (this.height / 4);
		
		GL11.glEnable(GL11.GL_BLEND);
		BaseOverlayRenderer.drawRectangle(halfBg - (labelWidth/2), by-20, labelWidth, 12, titleColor, 255);
		drawCenteredString(this.fontRenderer, title , this.width / 2, by-18, 16777215);
    }
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
		case Keyboard.KEY_ESCAPE : {
			UIManager.getInstance().openMap();
			break;
		}
		}
	}
    

	@Override
	public void close() {	
	}
}
