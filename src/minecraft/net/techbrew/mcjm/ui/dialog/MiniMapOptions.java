package net.techbrew.mcjm.ui.dialog;

import net.minecraft.src.GuiButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;
import net.techbrew.mcjm.ui.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class MiniMapOptions extends JmUI {

    private final String title;
    private int lastWidth = 0;
    private int lastHeight = 0;

	private enum ButtonEnum {MiniMap,Position,Shape,Font,Keyboard,Close, Showfps, CloseAll};
	private MapButton buttonPosition, buttonShape, buttonFont, buttonMiniMap, buttonKeyboard, buttonShowfps, buttonClose, buttonCloseAll;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;

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

        buttonPosition = new MapButton(ButtonEnum.Position.ordinal(), 0, 0, "");
        setPosition(UIManager.getInstance().getMiniMap().getPosition());
        buttonPosition.enabled = minimapOn;
                
        buttonShape = new MapButton(ButtonEnum.Shape.ordinal(), 0, 0, "");
        setShape(UIManager.getInstance().getMiniMap().getShape());
        buttonShape.enabled = minimapOn;
                
        buttonFont = new MapButton(ButtonEnum.Font.ordinal(), 0, 0,
                Constants.getString("MiniMapOverlay.font", Constants.getString("MiniMapOverlay.font_small")),
                Constants.getString("MiniMapOverlay.font", Constants.getString("MiniMapOverlay.font_large")), true);
        buttonFont.enabled = minimapOn;

        buttonKeyboard = new MapButton(ButtonEnum.Keyboard.ordinal(), 0, 0,
                Constants.getString("MiniMapOverlay.hotkeys", on),
                Constants.getString("MiniMapOverlay.hotkeys", off), true);
        buttonKeyboard.enabled = minimapOn;

        boolean isShowFps = UIManager.getInstance().getMiniMap().isShowFps();
        buttonShowfps = new MapButton(ButtonEnum.Showfps.ordinal(), 0, 0,
                Constants.getString("MiniMapOverlay.show_fps", on),
                Constants.getString("MiniMapOverlay.show_fps", off), isShowFps); // TODO:  Pref
        buttonShowfps.enabled = minimapOn;

		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$

        buttonCloseAll = new MapButton(ButtonEnum.CloseAll.ordinal(),0,0,Constants.getString("MiniMapOverlay.return_to_game")); //$NON-NLS-1$

        buttonList.add(buttonMiniMap);
        buttonList.add(buttonPosition);
        buttonList.add(buttonShape);
        buttonList.add(buttonFont);
        buttonList.add(buttonKeyboard);
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

            buttonMiniMap.leftOf(bx).yPosition = by;
            buttonShape.rightOf(buttonMiniMap, hgap).yPosition = by;

            buttonPosition.below(buttonMiniMap, vgap).leftOf(bx);
            buttonFont.below(buttonShape, vgap).rightOf(buttonPosition, hgap);

            buttonKeyboard.below(buttonPosition, vgap).leftOf(bx);
            buttonShowfps.below(buttonFont, vgap).rightOf(buttonKeyboard, hgap);

			buttonClose.below(buttonShowfps, vgap*4).centerHorizontalOn(this.width / 2);
            buttonCloseAll.below(buttonClose, vgap).centerHorizontalOn(this.width / 2);
		}	
	}
	
    @Override
	protected void actionPerformed(GuiButton guibutton) {
    	
    	final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {

            case MiniMap: {
                UIManager uim = UIManager.getInstance();
                final boolean enabled = !uim.isMiniMapEnabled();
                buttonMiniMap.setToggled(enabled);
                uim.setMiniMapEnabled(enabled);
                buttonPosition.enabled = enabled;
                buttonShape.enabled = enabled;
                buttonFont.enabled = enabled;
                buttonKeyboard.enabled = enabled;
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
                buttonFont.setToggled(newScale==1);
                break;
            }

            case Keyboard: {
                // TODO
                buttonKeyboard.toggle();
                break;
            }

            case Showfps: {
                buttonShowfps.toggle();
                UIManager.getInstance().getMiniMap().setShowFps(buttonShowfps.getToggled());
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
        if(nextIndex==DisplayVars.Shape.values().length){
            nextIndex = 0;
        }
        setShape(DisplayVars.Shape.values()[nextIndex]);
    }

    private void setShape(DisplayVars.Shape shape){
        currentShape = shape;
        buttonShape.displayString = Constants.getString("MiniMapOverlay.shape", currentShape.label);
        UIManager.getInstance().getMiniMap().setShape(shape);
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
        buttonPosition.displayString = Constants.getString("MiniMapOverlay.position", currentPosition.label);
        UIManager.getInstance().getMiniMap().setPosition(position);
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
    	super.drawDefaultBackground();

        MiniMapOverlay miniMap = UIManager.getInstance().getMiniMap();
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
