package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class MiniMapOptions extends JmUI {

	private enum ButtonEnum {MiniMap,Position,Shape,Font,Unicode,Keyboard,KeyboardHelp, Close, Showfps, CloseAll};
	private Button buttonPosition, buttonShape, buttonFont, buttonUnicode, buttonMiniMap, buttonKeyboard, buttonKeyboardHelp, buttonShowfps, buttonClose, buttonCloseAll;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;
    private ButtonList leftButtons;
    private ButtonList rightButtons;

	public MiniMapOptions() {
		super(Constants.getString("MiniMap.options"));
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
        buttonMiniMap = new Button(ButtonEnum.MiniMap.ordinal(),0,0,
                Constants.getString("MiniMap.enable_minimap", on),
                Constants.getString("MiniMap.enable_minimap", off),
                minimapOn); //$NON-NLS-1$  //$NON-NLS-2$
        buttonMiniMap.setToggled(minimapOn);

        DisplayVars.Position position = DisplayVars.Position.valueOf(pm.getString(PropertyManager.Key.PREF_MINIMAP_POSITION));
        buttonPosition = new Button(ButtonEnum.Position.ordinal(), 0, 0, "");
        setPosition(position);
        buttonPosition.enabled = minimapOn;

        DisplayVars.Shape shape = DisplayVars.Shape.valueOf(pm.getString(PropertyManager.Key.PREF_MINIMAP_SHAPE));
        buttonShape = new Button(ButtonEnum.Shape.ordinal(), 0, 0, "");
        setShape(shape);
        buttonShape.enabled = minimapOn;

        MapOverlay.state().minimapFontScale = pm.getDouble(PropertyManager.Key.PREF_MINIMAP_FONTSCALE);
        buttonFont = new Button(ButtonEnum.Font.ordinal(), 0, 0,
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_large")),
                (MapOverlay.state().minimapFontScale ==1));
        buttonFont.enabled = minimapOn;

        boolean showHotKeys = pm.getBoolean(PropertyManager.Key.PREF_MINIMAP_HOTKEYS);
        buttonKeyboard = new Button(ButtonEnum.Keyboard.ordinal(), 0, 0,
                Constants.getString("MiniMap.hotkeys", on),
                Constants.getString("MiniMap.hotkeys", off), showHotKeys);

        buttonKeyboardHelp = new Button(ButtonEnum.KeyboardHelp.ordinal(), 0, 0,
                Constants.getString("MapOverlay.hotkeys_button"));

        boolean isShowFps = pm.getBoolean(PropertyManager.Key.PREF_MINIMAP_SHOWFPS);
        buttonShowfps = new Button(ButtonEnum.Showfps.ordinal(), 0, 0,
                Constants.getString("MiniMap.show_fps", on),
                Constants.getString("MiniMap.show_fps", off), isShowFps);
        buttonShowfps.enabled = minimapOn;

        boolean forceUnicode = pm.getBoolean(PropertyManager.Key.PREF_MINIMAP_FORCEUNICODE);
        buttonUnicode = new Button(ButtonEnum.Unicode.ordinal(), 0, 0,
                Constants.getString("MiniMap.force_unicode", on),
                Constants.getString("MiniMap.force_unicode", off), forceUnicode);

		buttonClose = new Button(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$

        buttonCloseAll = new Button(ButtonEnum.CloseAll.ordinal(),0,0,Constants.getString("MiniMap.return_to_game")); //$NON-NLS-1$

        buttonList.add(buttonMiniMap);
        buttonList.add(buttonPosition);
        buttonList.add(buttonShape);
        buttonList.add(buttonFont);
        buttonList.add(buttonKeyboard);
        buttonList.add(buttonKeyboardHelp);
        buttonList.add(buttonShowfps);
        buttonList.add(buttonUnicode);

        buttonList.add(buttonClose);
        buttonList.add(buttonCloseAll);

        leftButtons = new ButtonList(buttonMiniMap, buttonPosition, buttonShowfps, buttonKeyboard);
        rightButtons = new ButtonList(buttonShape, buttonFont, buttonUnicode, buttonKeyboardHelp);
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

        final int hgap = 2;
        final int vgap = 3;
        final int bx = this.width/2;
        final int by = this.height / 4;

        leftButtons.layoutVertical(bx - hgap, by, false, vgap);
        rightButtons.layoutVertical(bx + hgap, by, true, vgap);

        buttonClose.below(leftButtons, vgap).centerHorizontalOn(bx);
        buttonCloseAll.below(buttonClose, vgap).centerHorizontalOn(bx);

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
                double newScale = (MapOverlay.state().minimapFontScale ==1) ? 2 : 1;
                MapOverlay.state().minimapFontScale = newScale;
                PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_FONTSCALE, newScale);
                buttonFont.setToggled(newScale==1);
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
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
                PropertyManager.set(PropertyManager.Key.PREF_MINIMAP_SHOWFPS, showFps);
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                break;
            }

            case Unicode: {
                boolean forceUnicode = !PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_FORCEUNICODE);
                buttonUnicode.setToggled(forceUnicode);
                UIManager.getInstance().getMiniMap().setForceUnicode(forceUnicode);
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
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

    public void nextShape() {
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
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        drawTitle();
        drawLogo();

        MiniMap miniMap = UIManager.getInstance().getMiniMap();
        if(miniMap.isEnabled()){
            miniMap.drawMap();
        }

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }
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

}
