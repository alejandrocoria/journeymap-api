package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.ui.map.MapOverlayActions;
import net.techbrew.journeymap.ui.map.MapOverlayHotkeysHelp;
import net.techbrew.journeymap.ui.map.MapOverlayOptions;
import net.techbrew.journeymap.ui.minimap.MiniMap;
import net.techbrew.journeymap.ui.minimap.MiniMapHotkeysHelp;
import net.techbrew.journeymap.ui.minimap.MiniMapOptions;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UIManager {	
	
	private static class Holder {
        private static final UIManager INSTANCE = new UIManager();
    }

    public static UIManager getInstance() {
        return Holder.INSTANCE;
    }

    private MiniMap miniMap;
	
    private UIManager() {
        miniMap = new MiniMap();
    }
    
    private final Logger logger = JourneyMap.getLogger();
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
    }
    
    public void openInventory() {
    	logger.fine("Opening inventory");
    	closeAll();
    	minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer)); // displayGuiScreen
    }
    
    public void open(Class<? extends JmUI> uiClass) {
    	closeCurrent();
    	try {    		
    		logger.fine("Opening UI " + uiClass.getSimpleName());
			minecraft.displayGuiScreen(uiClass.newInstance()); // displayGuiScreen
            miniMap.setVisible(false);
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception opening UI: " + e); //$NON-NLS-1$
			logger.severe(LogFormatter.toString(e));
			String error = Constants.getMessageJMERR23(e.getMessage());
			//ChatLog.announceError(error);
		}
    }

    public void toggleMinimap() {
        final boolean enabled = !isMiniMapEnabled();
        PropertyManager.set(PropertyManager.Key.PREF_SHOW_MINIMAP, enabled);
        setMiniMapEnabled(enabled);
    }

    public void setMiniMapEnabled(boolean enable) {
        miniMap.setEnabled(enable);
    }

    public boolean isMiniMapEnabled() {
        return miniMap.isEnabled();
    }

    public void drawMiniMap() {
        if(miniMap.isEnabled() && miniMap.isVisible()){
            final GuiScreen currentScreen = minecraft.currentScreen;
            final boolean doDraw = currentScreen==null || currentScreen instanceof GuiChat || currentScreen instanceof MiniMapOptions;
            if(doDraw) {
                miniMap.drawMap();
            }
        }
    }

    public MiniMap getMiniMap() {
        return miniMap;
    }

    public void openMap() {
        KeyBinding.unPressAllKeys();
        open(MapOverlay.class);
    }

    public void openMapHotkeyHelp() {
        open(MapOverlayHotkeysHelp.class);
    }

    public void openMiniMapOptions() {
        open(MiniMapOptions.class);
    }

    public void openMiniMapHotkeyHelp() {
        open(MiniMapHotkeysHelp.class);
    }
    
    public void openMapOptions() {
        open(MapOverlayOptions.class);
    }
    
    public void openMapActions() {
    	open(MapOverlayActions.class);
    }

    public void reset() {
        MapOverlay.reset();
        TileCache.instance().invalidateAll();
        TileCache.instance().cleanUp();
        if(this.miniMap!=null){
            this.miniMap.reset();
        }
        this.miniMap = new MiniMap();
    }
}
