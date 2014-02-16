package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.task.MapRegionTask;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MapButton;
import net.techbrew.journeymap.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class AutoMapConfirmation extends JmUI {

    private enum ButtonEnum {All,Missing,None,Close}
	MapButton buttonAll, buttonMissing, buttonNone, buttonClose;


    public AutoMapConfirmation() {
        super(Constants.getString("MapOverlay.automap_dialog"));
    }
	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
    	buttonAll = new MapButton(ButtonEnum.All.ordinal(), 0, 0, Constants.getString("MapOverlay.automap_dialog_all"));
        buttonAll.noDisableText = true;

    	buttonMissing = new MapButton(ButtonEnum.Missing.ordinal(), 0, 0, Constants.getString("MapOverlay.automap_dialog_missing"));
        buttonMissing.noDisableText = true;

    	buttonNone = new MapButton(ButtonEnum.None.ordinal(), 0, 0, Constants.getString("MapOverlay.automap_dialog_none"));
        buttonNone.noDisableText = true;

    	buttonClose = new MapButton(ButtonEnum.None.ordinal(), 0, 0, Constants.getString("MapOverlay.close"));
        buttonClose.noDisableText = true;
        
        boolean enable = !JourneyMap.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class);        
        buttonAll.enabled = enable;
        buttonMissing.enabled = enable;
        buttonNone.enabled = !enable;
        
        buttonList.add(buttonAll);
        buttonList.add(buttonMissing);
        buttonList.add(buttonNone);
        buttonList.add(buttonClose);
    }

    @Override
    protected void layoutButtons() {

        if(this.buttonList.isEmpty()) {
            this.initGui();
        }

        final int x = this.width / 2;
        final int y = this.height / 4;
        final int vgap = 3;

        this.drawCenteredString(getFontRenderer(), Constants.getString("MapOverlay.automap_dialog_text"), x, y, 16777215);

        buttonAll.centerHorizontalOn(x).setY(y+18);
        buttonMissing.centerHorizontalOn(x).below(buttonAll, vgap);
        buttonNone.centerHorizontalOn(x).below(buttonMissing, vgap);
        buttonClose.centerHorizontalOn(x).below(buttonNone, vgap*4);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {
    		case All : {
    			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.TRUE);
    			break;  
    		}
    		case Missing : {
    			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.FALSE);
    			break;  
    		}
    		case None : {
    			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, false, null);
    			break;      			
    		}    
    		case Close: {
    			break;
    		}
    	}        	
    	UIManager.getInstance().openMap();
    }

    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
		case Keyboard.KEY_ESCAPE : {
			UIManager.getInstance().openMapActions();
			break;
		}
		}
	}
}
