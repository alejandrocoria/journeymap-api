package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.minimap.DisplayVars;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class MapOverlayHotkeysHelp extends JmUI {

    private int lastWidth = 0;
    private int lastHeight = 0;

	private enum ButtonEnum {Close};
	private Button buttonClose;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;

    private KeyEventHandler keyEventHandler;

	public MapOverlayHotkeysHelp() {
		super(Constants.getString("MapOverlay.hotkeys_title"));
        keyEventHandler = new KeyEventHandler();
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        this.buttonList.clear();

		buttonClose = new Button(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$

        buttonList.add(buttonClose);
        
    }
    
    /**
	 * Center buttons in UI.
	 */
    @Override
    protected void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;
			final int by = (this.height / 4) + 60;
            buttonClose.centerHorizontalOn(this.width / 2).setY(by);
		}	
	}

    @Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {


            case Close: {
                UIManager.getInstance().openMapOptions();
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
        super.drawScreen(par1, par2, par3);

        // Title
        int y = this.height / 4 - 18;

        // Hotkey help
        y+=12;
        final int x = (this.width)/2;
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_toggle"), Keyboard.getKeyName(Constants.KB_MAP.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_zoom_in"), Keyboard.getKeyName(Constants.KB_MAP_ZOOMIN.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_zoom_out"), Keyboard.getKeyName(Constants.KB_MAP_ZOOMOUT.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_day"), Keyboard.getKeyName(Constants.KB_MAP_DAY.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_night"), Keyboard.getKeyName(Constants.KB_MAP_NIGHT.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_north"), Keyboard.getKeyName(mc.gameSettings.keyBindForward.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_west"), Keyboard.getKeyName(mc.gameSettings.keyBindLeft.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_south"), Keyboard.getKeyName(mc.gameSettings.keyBindBack.getKeyCode()), x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_east"), Keyboard.getKeyName(mc.gameSettings.keyBindRight.getKeyCode()), x, y+=12);
        buttonClose.setY(y + 16);
    }

    protected void drawHelpStrings(String title, String key, int x, int y)
    {
        int hgap = 8;
        int tWidth = getFontRenderer().getStringWidth(title);
        drawString(getFontRenderer(), title, x - tWidth - hgap, y, 16777215);

        drawString(getFontRenderer(), key, x + hgap, y, Color.YELLOW.getRGB());
    }
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
            case Keyboard.KEY_ESCAPE : {
                UIManager.getInstance().openMapOptions();
                return;
            }
		}
	}
}
