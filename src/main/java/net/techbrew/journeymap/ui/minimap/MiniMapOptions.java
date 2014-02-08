package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MapButton;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class MiniMapOptions extends JmUI {

    private final String title;
    private int lastWidth = 0;
    private int lastHeight = 0;

	private enum ButtonEnum {MiniMap,Position,Shape,Font,Keyboard,KeyboardHelp, Close, Showfps, CloseAll};
	private MapButton buttonPosition, buttonShape, buttonFont, buttonMiniMap, buttonKeyboard, buttonKeyboardHelp, buttonShowfps, buttonClose, buttonCloseAll;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;

	public MiniMapOptions() {
		title = Constants.getString("MiniMap.options");
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
        final PropertyManager pm = PropertyManager.getInstance();

        boolean minimapOn = pm.getBoolean(PropertyManager.Key.PREF_SHOW_MINIMAP);
        buttonMiniMap = new MapButton(ButtonEnum.MiniMap.ordinal(),0,0,
                Constants.getString("MiniMap.enable_minimap", on),
                Constants.getString("MiniMap.enable_minimap", off),
                minimapOn); //$NON-NLS-1$  //$NON-NLS-2$
        buttonMiniMap.setToggled(minimapOn);

        DisplayVars.Position position = DisplayVars.Position.valueOf(pm.getString(PropertyManager.Key.PREF_MINIMAP_POSITION));
        buttonPosition = new MapButton(ButtonEnum.Position.ordinal(), 0, 0, "");
        setPosition(position);
        buttonPosition.enabled = minimapOn;

        DisplayVars.Shape shape = DisplayVars.Shape.valueOf(pm.getString(PropertyManager.Key.PREF_MINIMAP_SHAPE));
        buttonShape = new MapButton(ButtonEnum.Shape.ordinal(), 0, 0, "");
        setShape(shape);
        buttonShape.enabled = minimapOn;

        MapOverlay.state().fontScale = pm.getDouble(PropertyManager.Key.PREF_MINIMAP_FONTSCALE);
        buttonFont = new MapButton(ButtonEnum.Font.ordinal(), 0, 0,
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_large")),
                (MapOverlay.state().fontScale==1));
        buttonFont.enabled = minimapOn;

        boolean showHotKeys = pm.getBoolean(PropertyManager.Key.PREF_MINIMAP_HOTKEYS);
        buttonKeyboard = new MapButton(ButtonEnum.Keyboard.ordinal(), 0, 0,
                Constants.getString("MiniMap.hotkeys", on),
                Constants.getString("MiniMap.hotkeys", off), showHotKeys);

        buttonKeyboardHelp = new MapButton(ButtonEnum.KeyboardHelp.ordinal(), 0, 0,
                Constants.getString("MiniMap.hotkeys_help"));

        boolean isShowFps = pm.getBoolean(PropertyManager.Key.PREF_MINIMAP_SHOWFPS);
        buttonShowfps = new MapButton(ButtonEnum.Showfps.ordinal(), 0, 0,
                Constants.getString("MiniMap.show_fps", on),
                Constants.getString("MiniMap.show_fps", off), isShowFps);
        buttonShowfps.enabled = minimapOn;

		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$

        buttonCloseAll = new MapButton(ButtonEnum.CloseAll.ordinal(),0,0,Constants.getString("MiniMap.return_to_game")); //$NON-NLS-1$

        buttonList.add(buttonMiniMap);
        buttonList.add(buttonPosition);
        buttonList.add(buttonShape);
        buttonList.add(buttonFont);
        buttonList.add(buttonKeyboard);
        buttonList.add(buttonKeyboardHelp);
        buttonList.add(buttonShowfps);

        buttonList.add(buttonClose);
        buttonList.add(buttonCloseAll);
        
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
			final int bx = (this.width-hgap)/2;
			final int by = this.height / 4;

            buttonMiniMap.leftOf(bx).setY(by);
            buttonShape.rightOf(buttonMiniMap, hgap).setY(by);

            buttonPosition.below(buttonMiniMap, vgap).leftOf(bx);
            buttonFont.below(buttonShape, vgap).rightOf(buttonPosition, hgap);

            buttonKeyboard.below(buttonPosition, vgap).leftOf(bx);
            buttonShowfps.below(buttonFont, vgap).rightOf(buttonKeyboard, hgap);

            buttonKeyboardHelp.below(buttonKeyboard, vgap).centerHorizontalOn(this.width / 2);

			buttonClose.below(buttonKeyboardHelp, vgap*4).centerHorizontalOn(this.width / 2);
            buttonCloseAll.below(buttonClose, vgap).centerHorizontalOn(this.width / 2);
		}	
	}

    @Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {

            case MiniMap: {
                UIManager uim = UIManager.getInstance();
                final boolean enabled = !uim.isMiniMapEnabled();
                PropertyManager.set(PropertyManager.Key.PREF_SHOW_MINIMAP, enabled);
                buttonMiniMap.setToggled(enabled);
                uim.setMiniMapEnabled(enabled);
                buttonPosition.enabled = enabled;
                buttonShape.enabled = enabled;
                buttonFont.enabled = enabled;
                buttonShowfps.enabled = enabled;
                break;
            }

            case Shape: {
                nextShape();
                break;
            }

            case Position: {
                nextPosition();
                break;
            }

            case Font: {
                double newScale = (MapOverlay.state().fontScale==1) ? 2 : 1;
                MapOverlay.state().fontScale = newScale;
                PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_FONTSCALE, newScale);
                buttonFont.setToggled(newScale==1);
                UIManager.getInstance().getMiniMap().updateDisplayVars();
                break;
            }

            case Keyboard: {
                boolean showHotKeys = !PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_HOTKEYS);
                buttonKeyboard.setToggled(showHotKeys);
                PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_HOTKEYS, showHotKeys);
                break;
            }

            case KeyboardHelp: {
                UIManager.getInstance().openMiniMapHotkeyHelp();
                break;
            }

            case Showfps: {
                boolean showFps = !PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_SHOWFPS);
                buttonShowfps.setToggled(showFps);
                UIManager.getInstance().getMiniMap().setShowFps(showFps);
                PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_SHOWFPS, showFps);
                break;
            }

            case Close: {
                UIManager.getInstance().openMapOptions();
                break;
            }

            case CloseAll: {
                UIManager.getInstance().closeAll();
                break;
            }
		}
	}

    private void nextShape() {
        int nextIndex = currentShape.ordinal()+1;
        if(nextIndex==DisplayVars.Shape.Enabled.length){
            nextIndex = 0;
        }
        setShape(DisplayVars.Shape.Enabled[nextIndex]);
    }

    private void setShape(DisplayVars.Shape shape){
        if(Arrays.binarySearch(DisplayVars.Shape.Enabled, shape)<0){
            shape = DisplayVars.Shape.Enabled[0];
        }
        currentShape = shape;
        buttonShape.displayString = Constants.getString("MiniMap.shape", Constants.getString(currentShape.label));
        UIManager.getInstance().getMiniMap().setShape(shape);
        PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_SHAPE, shape.name());
    }

    private void nextPosition() {
        int nextIndex = currentPosition.ordinal()+1;
        if(nextIndex==DisplayVars.Position.values().length){
            nextIndex = 0;
        }
        setPosition(DisplayVars.Position.values()[nextIndex]);
    }

    private void setPosition(DisplayVars.Position position){
        currentPosition = position;
        buttonPosition.displayString = Constants.getString("MiniMap.position", Constants.getString(currentPosition.label));
        UIManager.getInstance().getMiniMap().setPosition(position);
        PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_POSITION, position.name());
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
        
        int y = this.height / 4 - 18;
        drawCenteredString(this.fontRendererObj, title , this.width / 2, y, 16777215);

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
			UIManager.getInstance().openMap();
			break;
		}
		}
	}
    
    @Override
	public void close() {	
	}

}
