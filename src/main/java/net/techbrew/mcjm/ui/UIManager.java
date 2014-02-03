package net.techbrew.mcjm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.ui.map.MapOverlay;
import net.techbrew.mcjm.ui.map.MapOverlayActions;
import net.techbrew.mcjm.ui.map.MapOverlayOptions;
import net.techbrew.mcjm.ui.minimap.MiniMap;
import net.techbrew.mcjm.ui.minimap.MiniMapHotkeysHelp;
import net.techbrew.mcjm.ui.minimap.MiniMapOptions;

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
    private final KeyBinding uiKeybinding = JourneyMap.getInstance().uiKeybinding;
    Minecraft minecraft = Minecraft.getMinecraft();
    
    public void closeAll() {    	
    	closeCurrent();
    	minecraft.func_147108_a(null);
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
    	minecraft.func_147108_a(new GuiInventory(minecraft.thePlayer)); // displayGuiScreen
    }
    
    public void open(Class<? extends JmUI> uiClass) {
    	closeCurrent();
    	try {    		
    		logger.fine("Opening UI " + uiClass.getSimpleName());
			minecraft.func_147108_a(uiClass.newInstance()); // displayGuiScreen
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
    	open(MapOverlay.class);
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
