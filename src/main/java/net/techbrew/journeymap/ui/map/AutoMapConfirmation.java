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

//        width = width;
//        height = height;
//        mc = mc;
//        fontRenderer = super.fontRenderer;
//        buttonList = buttonList;
	
	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
    	
    	buttonAll = new MapButton(ButtonEnum.All.ordinal(), 0, 0, Constants.getString("MapOverlay.automap_dialog_all"));        
    	buttonMissing = new MapButton(ButtonEnum.Missing.ordinal(), 0, 0, Constants.getString("MapOverlay.automap_dialog_missing"));
    	buttonNone = new MapButton(ButtonEnum.None.ordinal(), 0, 0, Constants.getString("MapOverlay.automap_dialog_none"));
    	buttonClose = new MapButton(ButtonEnum.None.ordinal(), 0, 0, Constants.getString("MapOverlay.close"));
        
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
    
    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int par1, int par2, float par3)
    {
        drawBackground(0);
    	
    	final int x = this.width / 2;
    	final int y = this.height / 4;
    	final int vgap = 3;
    	
        this.drawCenteredString(this.fontRendererObj, Constants.getString("MapOverlay.automap_dialog"), x, y - 18, 16777215);
        this.drawCenteredString(this.fontRendererObj, Constants.getString("MapOverlay.automap_dialog_text"), x, y, 16777215);
    	
    	buttonAll.centerHorizontalOn(x).setY(y+18);
    	buttonMissing.centerHorizontalOn(x).below(buttonAll, vgap);
    	buttonNone.centerHorizontalOn(x).below(buttonMissing, vgap);
    	buttonClose.centerHorizontalOn(x).below(buttonNone, vgap*4);
        
        super.drawScreen(par1, par2, par3);
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
			UIManager.getInstance().openMapActions();
			break;
		}
		}
	}

	@Override
	public void close() {

	}

}
