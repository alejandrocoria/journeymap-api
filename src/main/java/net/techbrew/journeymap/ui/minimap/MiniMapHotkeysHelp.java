package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MapButton;
import net.techbrew.journeymap.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class MiniMapHotkeysHelp extends JmUI {

    private final String title;
    private int lastWidth = 0;
    private int lastHeight = 0;

	private enum ButtonEnum {Close};
	private MapButton buttonClose;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;

    private KeyEventHandler keyEventHandler;

	public MiniMapHotkeysHelp() {
		title = Constants.getString("MiniMap.hotkeys_help_title");
        keyEventHandler = new KeyEventHandler();
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        this.buttonList.clear();

		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$

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
                UIManager.getInstance().openMiniMapOptions();
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
        drawBackground(0); // super.drawBackground(0);

        layoutButtons();
        
        super.drawScreen(par1, par2, par3);

        // Title
        int y = this.height / 4 - 18;
        drawCenteredString(this.fontRenderer, title , this.width / 2, y, 16777215);

        // Hotkey help
        y+=12;
        final int x = (this.width)/2;
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_toggle"), Constants.getString("MiniMap.hotkeys_help_toggle_key"), x, y+=12);
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_zoom"), Constants.getString("MiniMap.hotkeys_help_zoom_key"), x, y+=12);
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_maptype"), Constants.getString("MiniMap.hotkeys_help_maptype_key"), x, y+=12);
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_position"), Constants.getString("MiniMap.hotkeys_help_position_key"), x, y+=12);
    }

    protected void drawHelpStrings(String title, String keys, int x, int y)
    {
        int hgap = 4;
        int tWidth = this.fontRenderer.getStringWidth(title);
        drawString(this.fontRenderer, title, x - tWidth - hgap, y, 16777215);
        int kWidth = this.fontRenderer.getStringWidth(title);
        drawString(this.fontRenderer, keys, x + hgap + hgap, y, 16777215);
    }
    
    @Override
	public void drawBackground(int layer)
	{
        super.drawBackground(layer);

        MiniMap miniMap = UIManager.getInstance().getMiniMap();
        if(miniMap.isEnabled()){
            miniMap.drawMap();
        }

        super.drawLogo();
	}
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
            case Keyboard.KEY_ESCAPE : {
                UIManager.getInstance().openMiniMapOptions();
                return;
            }
		}
	}
    
    @Override
	public void close() {	
	}

}
