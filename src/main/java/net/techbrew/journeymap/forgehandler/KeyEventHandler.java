package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler {

    public KeyEventHandler() {
        for(KeyBinding kb : Constants.KEYBINDINGS) {
            ClientRegistry.registerKeyBinding(kb);
        }
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        KeyEventHandler.onKeypress(false);
    }

    public static void onKeypress(boolean minimapOnly) {
        final int i = Keyboard.getEventKey();
        MapOverlayState mapOverlayState = MapOverlay.state();

        if(mapOverlayState.minimapHotkeys && GuiScreen.isCtrlKeyDown()) {

            if(i==Constants.KB_MAP.getKeyCode()) {
                UIManager.getInstance().toggleMinimap();
                return;
            }
            else if(i==Constants.KB_MAP_ZOOMIN.getKeyCode()) {
                mapOverlayState.zoomIn();
                return;
            }
            else if(i==Constants.KB_MAP_ZOOMOUT.getKeyCode()) {
                mapOverlayState.zoomOut();
                return;
            }
            else if(i==Constants.KB_MAP_DAY.getKeyCode()) {
                mapOverlayState.overrideMapType(Constants.MapType.day);
                return;
            }
            else if(i==Constants.KB_MAP_NIGHT.getKeyCode()) {
                mapOverlayState.overrideMapType(Constants.MapType.night);
                return;
            }
            else if(i==Constants.KB_MINIMAP_POS.getKeyCode()) {
                UIManager.getInstance().getMiniMap().nextPosition();
                return;
            }
        }
        else if(!minimapOnly)
        {
            if(i==Constants.KB_MAP.getKeyCode()) {
                UIManager.getInstance().openMap();
                return;
            }
        }
    }
}
