package net.techbrew.mcjm.ui.dialog;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiSmallButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.task.MapRegionTask;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapOverlay;
import net.techbrew.mcjm.ui.UIManager;

import org.lwjgl.input.Keyboard;

public class AutoMapConfirmation extends JmUI {

	private enum ButtonEnum {All,Missing,Cancel};
	
	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
    	GuiSmallButton buttonAll = new GuiSmallButton(ButtonEnum.All.ordinal(), this.width / 2 - 155, this.height / 5 + 60, Constants.getString("MapOverlay.automap_dialog_all"));        
        GuiSmallButton buttonMissing = new GuiSmallButton(ButtonEnum.Missing.ordinal(), this.width / 2 - 155 + 160, this.height / 5 + 60, Constants.getString("MapOverlay.automap_dialog_missing"));
        GuiSmallButton buttonCancel = new GuiSmallButton(ButtonEnum.Cancel.ordinal(), this.width / 2 - 80, this.height / 5 + 85, Constants.getString("MapOverlay.automap_dialog_cancel"));
        
        boolean enable = !JourneyMap.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class);        
        buttonAll.enabled = enable;
        buttonMissing.enabled = enable;
        
        this.buttonList.add(buttonAll);
        this.buttonList.add(buttonMissing);
        this.buttonList.add(buttonCancel);        
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
	protected void actionPerformed(GuiButton button)
    {
    	final ButtonEnum id = ButtonEnum.values()[button.id];
    	switch(id) {
    		case All : {
    			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.TRUE);
    			break;  
    		}
    		case Missing : {
    			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.FALSE);
    			break;  
    		}
    		case Cancel : {
    			JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, false, null);
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
        this.drawCenteredString(this.fontRenderer, Constants.getString("MapOverlay.automap_dialog"), this.width / 2,      this.height / 5, 16777215);
        this.drawCenteredString(this.fontRenderer, Constants.getString("MapOverlay.automap_dialog_text"), this.width / 2, this.height / 5 + 30, 16777215);
        super.drawScreen(par1, par2, par3);
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
			UIManager.getInstance().openMapActions();
			break;
		}
		}
	}

	@Override
	public void close() {

	}

}
