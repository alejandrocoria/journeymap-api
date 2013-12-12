package net.techbrew.mcjm.ui;

import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.ui.dialog.MapOverlayActions;
import net.techbrew.mcjm.ui.dialog.MapOverlayOptions;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UIManager {	
	
	private static class Holder {
        private static final UIManager INSTANCE = new UIManager();
    }

    public static UIManager getInstance() {
        return Holder.INSTANCE;
    }

    private MiniMapOverlay miniMap;
	
    private UIManager() {
        miniMap = new MiniMapOverlay();
    }
    
    private final Logger logger = JourneyMap.getLogger();
    private final KeyBinding uiKeybinding = JourneyMap.getInstance().uiKeybinding;
    Minecraft minecraft = Minecraft.getMinecraft();
    
    public void closeAll() {    	
    	closeCurrent();
    	minecraft.displayGuiScreen(null);
		minecraft.setIngameFocus();
        miniMap.setVisible(true);
        TileCache.instance().cleanUp();
    }
    
    public void closeCurrent() {    	
    	if(minecraft.currentScreen!=null && minecraft.currentScreen instanceof JmUI) {
    		logger.fine("Closing " + minecraft.currentScreen.getClass());
			((JmUI) minecraft.currentScreen).close();
		}
		uiKeybinding.unPressAllKeys();
    }
    
    public void openInventory() {
    	logger.fine("Opening inventory");
    	closeAll();
    	minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));
    }
    
    public void open(Class<? extends JmUI> uiClass) {
    	closeCurrent();
    	try {    		
    		logger.fine("Opening UI " + uiClass.getSimpleName());
			minecraft.displayGuiScreen(uiClass.newInstance());
            miniMap.setVisible(false);
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception opening UI: " + e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			JourneyMap.getInstance().announce(error);
		}
    }

    public void drawMiniMap() {
        if(MiniMapOverlay.isEnabled() && miniMap.isVisible()){
            boolean isGamePaused = (minecraft.currentScreen != null) && !(minecraft.currentScreen instanceof MapOverlay) && !(minecraft.currentScreen instanceof GuiChat);
            if(!isGamePaused) {
                miniMap.drawMap();
            }
        }
    }

    public void openMap() {
    	open(MapOverlay.class);
    }
    
    public void openMapOptions() {
    	open(MapOverlayOptions.class);
    }
    
    public void openMapActions() {
    	open(MapOverlayActions.class);
    }
    
	public void keyboardEvent(KeyBinding keybinding)
	{		
		// JourneyMap key
		if(keybinding.keyCode==uiKeybinding.keyCode) {
			if(minecraft.currentScreen==null) {
				openMap();
			} else if(minecraft.currentScreen instanceof MapOverlay) {
				closeAll();
			}
		} 
	}

}
