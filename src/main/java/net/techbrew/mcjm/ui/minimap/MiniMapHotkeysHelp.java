package net.techbrew.mcjm.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapButton;
import net.techbrew.mcjm.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class MiniMapHotkeysHelp extends JmUI {

    private final String title;
    private int lastWidth = 0;
    private int lastHeight = 0;

	private enum ButtonEnum {Close};
	private MapButton buttonClose;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;

	public MiniMapHotkeysHelp() {
		title = Constants.getString("MiniMap.hotkeys_help_title");
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        this.field_146292_n.clear();

		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$

        field_146292_n.add(buttonClose);
        
    }
    
    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(field_146292_n.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=field_146294_l || lastHeight!=field_146295_m) {
			
			lastWidth = field_146294_l;
			lastHeight = field_146295_m;
			final int by = (this.field_146295_m / 4) + 60;
            buttonClose.centerHorizontalOn(this.field_146294_l / 2).setY(by);
		}	
	}

    @Override
    protected void func_146284_a(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.field_146127_k];
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
        func_146270_b(0); // super.drawBackground(0);

        layoutButtons();
        
        super.drawScreen(par1, par2, par3);

        // Title
        int y = this.field_146295_m / 4 - 18;
        drawCenteredString(this.field_146289_q, title , this.field_146294_l / 2, y, 16777215);

        // Hotkey help
        y+=12;
        final int x = (this.field_146294_l)/2;
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_toggle"), Constants.getString("MiniMap.hotkeys_help_toggle_key"), x, y+=12);
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_zoom"), Constants.getString("MiniMap.hotkeys_help_zoom_key"), x, y+=12);
        drawHelpStrings(Constants.getString("MiniMap.hotkeys_help_maptype"), Constants.getString("MiniMap.hotkeys_help_maptype_key"), x, y+=12);
    }

    protected void drawHelpStrings(String title, String keys, int x, int y)
    {
        int hgap = 4;
        int tWidth = this.field_146289_q.getStringWidth(title);
        drawString(this.field_146289_q, title, x - tWidth - hgap, y, 16777215);
        int kWidth = this.field_146289_q.getStringWidth(title);
        drawString(this.field_146289_q, keys, x + hgap, y, 16777215);
    }
    
    @Override
	public void func_146270_b(int layer)
	{
    	super.func_146270_b(layer);

        super.drawLogo();
	}
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
            case Keyboard.KEY_ESCAPE : {
                UIManager.getInstance().openMiniMapOptions();
                break;
            }
		}
	}
    
    @Override
	public void close() {	
	}

}
