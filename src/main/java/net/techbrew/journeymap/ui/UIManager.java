package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.ui.map.*;
import net.techbrew.journeymap.ui.minimap.MiniMap;
import net.techbrew.journeymap.ui.minimap.MiniMapHotkeysHelp;
import net.techbrew.journeymap.ui.minimap.MiniMapOptions;
import net.techbrew.journeymap.ui.waypoint.WaypointEditor;
import net.techbrew.journeymap.ui.waypoint.WaypointManager;

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
        KeyBinding.unPressAllKeys();
    }
    
    public void openInventory() {
    	logger.fine("Opening inventory");
    	closeAll();
    	minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer)); // displayGuiScreen
    }
    
    public <T extends JmUI> T open(Class<T> uiClass) {
    	try {
            T ui = uiClass.newInstance();
			return open(ui);
		} catch(Throwable e) {
			logger.log(Level.SEVERE, "Unexpected exception creating UI: " + LogFormatter.toString(e)); //$NON-NLS-1$
            return null;
		}
    }

    public <T extends JmUI> T open(T ui) {
        closeCurrent();
        logger.fine("Opening UI " + ui.getClass().getSimpleName());
        try
        {
            minecraft.displayGuiScreen(ui);
            miniMap.setVisible(false);
        }
        catch(Throwable t)
        {
            logger.severe(String.format("Unexpected exception opening UI %s: %s", ui.getClass(), LogFormatter.toString(t)));
        }
        return ui;
    }

    public void toggleMinimap() {
        setMiniMapEnabled(!isMiniMapEnabled());
    }

    public void setMiniMapEnabled(boolean enable) {
        JourneyMap.getInstance().miniMapProperties.setEnabled(enable);
    }

    public boolean isMiniMapEnabled() {
        return miniMap.isEnabled();
    }

    public void drawMiniMap() {
        try
        {
            if(miniMap.isEnabled() && miniMap.isVisible()){
                final GuiScreen currentScreen = minecraft.currentScreen;
                final boolean doDraw = currentScreen==null || currentScreen instanceof GuiChat || currentScreen instanceof MiniMapOptions;
                if(doDraw) {
                    miniMap.drawMap();
                }
            }
        }
        catch(Throwable e)
        {
            JourneyMap.getLogger().severe("Error drawing minimap: " + LogFormatter.toString(e));
        }
    }

    public MiniMap getMiniMap() {
        return miniMap;
    }

    public void openMap() {
        KeyBinding.unPressAllKeys();
        open(MapOverlay.class);
    }

    public void openMap(Waypoint waypoint)
    {
        try
        {
            if(waypoint.isInPlayerDimension())
            {
                KeyBinding.unPressAllKeys();
                MapOverlay map = open(MapOverlay.class);
                map.centerOn(waypoint);
            }
        }
        catch(Throwable e)
        {
            JourneyMap.getLogger().severe("Error opening map on waypoint: " + LogFormatter.toString(e));
        }
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

    public void openWaypointHelp() {
        open(WaypointHelp.class);
    }

    public void openWaypointManager() {
        if(WaypointsData.isNativeEnabled())
        {
            try
            {
                WaypointManager manager = new WaypointManager();
                open(manager);
            }
            catch(Throwable e)
            {
                JourneyMap.getLogger().severe("Error opening waypoint manager: " + LogFormatter.toString(e));
            }
        }
    }

    public void openWaypointEditor(Waypoint waypoint, boolean isNew, Class<? extends JmUI> returnClass) {
        if(WaypointsData.isNativeEnabled())
        {
            try
            {
                WaypointEditor editor = new WaypointEditor(waypoint, isNew, returnClass);
                open(editor);
            }
            catch(Throwable e)
            {
                JourneyMap.getLogger().severe("Error opening waypoint editor: " + LogFormatter.toString(e));
            }
        }
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
