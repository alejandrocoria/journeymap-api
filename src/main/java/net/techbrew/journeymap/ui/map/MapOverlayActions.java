package net.techbrew.journeymap.ui.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.io.MapSaver;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.task.MapRegionTask;
import net.techbrew.journeymap.task.SaveMapTask;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MapButton;
import net.techbrew.journeymap.ui.UIManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MapOverlayActions extends JmUI {

	private final Logger logger = JourneyMap.getLogger();
	
	private enum ButtonEnum {Automap,Check,Save,Browser,Close};
	
	final String title = Constants.getString("MapOverlay.actions");
	int lastWidth = 0;
	int lastHeight = 0;
	MapButton buttonAutomap, buttonSave, buttonClose, buttonBrowser, buttonCheck;
	
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

		buttonSave = new MapButton(ButtonEnum.Save.ordinal(),0,0,Constants.getString("MapOverlay.save_map")); //$NON-NLS-1$ 
		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 
		buttonBrowser = new MapButton(ButtonEnum.Browser.ordinal(),0,0,Constants.getString("MapOverlay.use_browser")); //$NON-NLS-1$ 	
		buttonBrowser.enabled = PropertyManager.getBooleanProp(PropertyManager.Key.WEBSERVER_ENABLED);
		
		buttonAutomap = new MapButton(ButtonEnum.Automap.ordinal(),0,0,
				Constants.getString("MapOverlay.automap_title", on),
				Constants.getString("MapOverlay.automap_title", off),
				true); //$NON-NLS-1$ //$NON-NLS-2$
		buttonAutomap.enabled = Minecraft.getMinecraft().isSingleplayer();
		
		buttonCheck = new MapButton(ButtonEnum.Check.ordinal(),0,0, Constants.getString("MapOverlay.update_check")); //$NON-NLS-1$ 
	
		buttonList.add(buttonAutomap);
		buttonList.add(buttonSave);
		buttonList.add(buttonCheck);
		buttonList.add(buttonBrowser);
		buttonList.add(buttonClose);
		
    }
    
    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}
		
		buttonSave.enabled = !JourneyMap.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class);
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;

			final int hgap = 4;
			final int vgap = 3;
			final int bx = (this.width-hgap)/2;
			final int by = this.height / 4;
			
			buttonAutomap.leftOf(bx).setY(by);
            buttonBrowser.rightOf(buttonAutomap, hgap).setY(by);

            buttonSave.below(buttonAutomap, vgap).leftOf(bx);
			buttonCheck.below(buttonBrowser, vgap).rightOf(buttonSave, hgap);
			
			buttonClose.below(buttonSave, vgap*4).centerHorizontalOn(bx);

		}
		
	}

    @Override
	protected void actionPerformed(GuiButton guibutton) { // actionPerformed
    	
    	final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {
			case Save: { 
				save();
				UIManager.getInstance().openMap();
				break;
			}
			case Close: { 
				UIManager.getInstance().openMap();
				break;
			}
			case Browser: { 
				launchLocalhost();
				UIManager.getInstance().openMap();
				break;
			}
			case Automap: { 			
				UIManager.getInstance().open(AutoMapConfirmation.class);
				break;
			}
			case Check: {	
				VersionCheck.launchWebsite();
				UIManager.getInstance().openMap();
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
		final MapOverlayState state = MapOverlay.state();
		final MapType mapType = state.getMapType();
		final Integer vSlice = state.getMapType()==MapType.underground ? state.getVSlice() : null;
		final MapSaver mapSaver = new MapSaver(state.getWorldDir(), mapType, vSlice, state.getDimension());
		if(mapSaver.isValid()) {
			JourneyMap.getInstance().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
			ChatLog.announceI18N("MapOverlay.save_filename", mapSaver.getSaveFileName()); //$NON-NLS-1$
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

    	int y = this.height / 4 - 18;
        drawCenteredString(this.fontRenderer, title , this.width / 2, y, 16777215);
    }
    
    @Override
	public void drawBackground(int layer) //drawBackground
	{
        super.drawWorldBackground(layer); // super.drawBackground(0);
        MapOverlay.drawMapBackground(this);
        super.drawBackground(layer); // super.drawDefaultBackground();

        super.drawLogo();
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
