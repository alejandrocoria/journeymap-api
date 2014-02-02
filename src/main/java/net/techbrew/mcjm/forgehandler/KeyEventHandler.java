package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.ui.UIManager;
import net.techbrew.mcjm.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler {

    private final Minecraft mc = FMLClientHandler.instance().getClient();
    private final KeyBinding uiKeybinding = JourneyMap.getInstance().uiKeybinding;

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {

        final int keyPressed = Keyboard.getEventKey();

        MapOverlayState mapOverlayState = MapOverlay.state();
        if(mapOverlayState.minimapHotkeys && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
            if(keyPressed==uiKeybinding.func_151463_i()) {
                UIManager.getInstance().toggleMinimap();
            }
            else
            {
                switch(keyPressed) {
                    case Keyboard.KEY_ESCAPE : {
                        UIManager.getInstance().closeAll();
                        return;
                    }
                    case Keyboard.KEY_ADD : {
                        mapOverlayState.zoomIn();
                        return;
                    }
                    case Keyboard.KEY_EQUALS : {
                        mapOverlayState.zoomIn();
                        return;
                    }
                    case Keyboard.KEY_MINUS : {
                        mapOverlayState.zoomOut();
                        return;
                    }
                    case Keyboard.KEY_LBRACKET : {
                        mapOverlayState.overrideMapType(Constants.MapType.day);
                        return;
                    }
                    case Keyboard.KEY_RBRACKET : {
                        mapOverlayState.overrideMapType(Constants.MapType.night);
                        return;
                    }
                }
            }
        }
        else
        {
            if(keyPressed==uiKeybinding.func_151463_i()) {
                if(mc.currentScreen==null) {
                    UIManager.getInstance().openMap();
                }
            }
        }
    }
}
