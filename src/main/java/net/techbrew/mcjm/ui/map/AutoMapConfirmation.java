package net.techbrew.mcjm.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.task.MapRegionTask;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapButton;
import net.techbrew.mcjm.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class AutoMapConfirmation extends JmUI {

	private enum ButtonEnum {All,Missing,None,Close}
	MapButton buttonAll, buttonMissing, buttonNone, buttonClose;

//        width = field_146294_l;
//        height = field_146295_m;
//        mc = field_146297_k;
//        fontRenderer = super.field_146289_q;
//        buttonList = field_146292_n;
	
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
        
        field_146292_n.add(buttonAll);
        field_146292_n.add(buttonMissing);
        field_146292_n.add(buttonNone);
        field_146292_n.add(buttonClose);
    }

    @Override
    protected void func_146284_a(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.field_146127_k];
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
        func_146270_b(0);
    	
    	final int x = this.field_146294_l / 2;
    	final int y = this.field_146295_m / 4;
    	final int vgap = 3;
    	
        this.drawCenteredString(this.field_146289_q, Constants.getString("MapOverlay.automap_dialog"), x, y - 18, 16777215);
        this.drawCenteredString(this.field_146289_q, Constants.getString("MapOverlay.automap_dialog_text"), x, y, 16777215);
    	
    	buttonAll.centerHorizontalOn(x).setY(y+18);
    	buttonMissing.centerHorizontalOn(x).below(buttonAll, vgap);
    	buttonNone.centerHorizontalOn(x).below(buttonMissing, vgap);
    	buttonClose.centerHorizontalOn(x).below(buttonNone, vgap*4);
        
        super.drawScreen(par1, par2, par3);
    }

    @Override
    public void func_146270_b(int layer) //drawBackground
    {
        super.func_146278_c(layer); // super.drawBackground(0);
        MapOverlay.drawMapBackground(this);
        super.func_146270_b(layer); // super.drawDefaultBackground();

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
