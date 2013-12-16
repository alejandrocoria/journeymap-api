package net.techbrew.mcjm.ui.dialog;

import net.minecraft.src.GuiButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapButton;
import net.techbrew.mcjm.ui.MapOverlay;
import net.techbrew.mcjm.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class MiniMapOptions extends JmUI {

	final String title;
	int lastWidth = 0;
	int lastHeight = 0;

	private enum ButtonEnum {MiniMap,Close,Position,Shape};
	MapButton buttonPosition, buttonShape, buttonMiniMap, buttonClose;

	public MiniMapOptions() {
		title = Constants.getString("MiniMapOverlay.options");
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

        boolean minimapOn = UIManager.getInstance().isMiniMapEnabled();
        buttonMiniMap = new MapButton(ButtonEnum.MiniMap.ordinal(),0,0,
                Constants.getString("MiniMapOverlay.enable_minimap", on),
                Constants.getString("MiniMapOverlay.enable_minimap", off),
                minimapOn); //$NON-NLS-1$  //$NON-NLS-2$
        buttonMiniMap.setToggled(minimapOn);

		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 

		buttonList.add(buttonClose);

        buttonList.add(buttonMiniMap);
    }
    
    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;
			
			final int hgap = 4;
			final int vgap = 3;
			final int bx = (this.width / 2) - (buttonMiniMap.getWidth() - hgap/2);
			final int by = this.height / 4;

            buttonMiniMap.setPosition(bx, by);

			
			buttonClose.below(buttonMiniMap, vgap*2).centerHorizontalOn(this.width / 2);

		}	
	}
	
    @Override
	protected void actionPerformed(GuiButton guibutton) {
    	
    	final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {

			case Close: {
				UIManager.getInstance().openMapOptions();
				break;
			}

            case MiniMap: {
                UIManager uim = UIManager.getInstance();
                buttonMiniMap.setToggled(!uim.isMiniMapEnabled());
                uim.setMiniMapEnabled(!uim.isMiniMapEnabled());
                break;
            }
		}
	}
    
    @Override
	public void updateScreen() {
		super.updateScreen();
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
	public void drawBackground(int layer)
	{    	
    	super.drawBackground(0);
    	MapOverlay.drawMapBackground(this);
    	super.drawDefaultBackground();
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
