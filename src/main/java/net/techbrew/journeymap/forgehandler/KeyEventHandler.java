package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler {

    private final Minecraft mc = FMLClientHandler.instance().getClient();
    public static final KeyBinding KB_MAP = JourneyMap.getInstance().uiKeybinding;
    public static final KeyBinding KB_CLOSE = new KeyBinding("key.jm.close", Keyboard.KEY_ESCAPE, JourneyMap.SHORT_MOD_NAME);
    public static final KeyBinding KB_ZOOMIN = new KeyBinding("key.jm.zoomin", Keyboard.KEY_ADD, JourneyMap.SHORT_MOD_NAME);
    public static final KeyBinding KB_ZOOMIN_ALT = new KeyBinding("key.jm.zoomin2", Keyboard.KEY_EQUALS, JourneyMap.SHORT_MOD_NAME);
    public static final KeyBinding KB_ZOOMOUT = new KeyBinding("key.jm.zoomout", Keyboard.KEY_MINUS, JourneyMap.SHORT_MOD_NAME);
    public static final KeyBinding KB_DAYMAP = new KeyBinding("key.jm.day", Keyboard.KEY_LBRACKET, JourneyMap.SHORT_MOD_NAME);
    public static final KeyBinding KB_NIGHTMAP = new KeyBinding("key.jm.night", Keyboard.KEY_RBRACKET, JourneyMap.SHORT_MOD_NAME);
    public static final KeyBinding KB_MINIMAP_POS = new KeyBinding("key.jm.position", Keyboard.KEY_BACKSLASH, JourneyMap.SHORT_MOD_NAME);

    private static final KeyBinding[] KEYBINDINGS = new KeyBinding[] {
        KB_MAP, KB_CLOSE, KB_ZOOMIN, KB_ZOOMIN_ALT, KB_ZOOMOUT, KB_DAYMAP, KB_NIGHTMAP, KB_MINIMAP_POS
    };

    private final HashMap<Integer, KeyBinding> keybindingsMap = new HashMap<Integer, KeyBinding>();

    public KeyEventHandler() {

        for(KeyBinding kb : KEYBINDINGS) {
            keybindingsMap.put(kb.getKeyCode(), kb);
        }

        for(KeyBinding kb : KEYBINDINGS) {
            ClientRegistry.registerKeyBinding(kb);
        }
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {

        final int keyPressed = Keyboard.getEventKey();

        KeyBinding kb = keybindingsMap.get(keyPressed);
        if(kb == null) {
            return;
        }

        MapOverlayState mapOverlayState = MapOverlay.state();
        if(mapOverlayState.minimapHotkeys && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {

            if(kb==KB_MAP) {
                UIManager.getInstance().toggleMinimap();
                return;
            }
            else if(kb==KB_ZOOMIN || kb==KB_ZOOMIN_ALT) {
                mapOverlayState.zoomIn();
                return;
            }
            else if(kb==KB_ZOOMOUT) {
                mapOverlayState.zoomOut();
                return;
            }
            else if(kb==KB_DAYMAP) {
                mapOverlayState.overrideMapType(Constants.MapType.day);
                return;
            }
            else if(kb==KB_NIGHTMAP) {
                mapOverlayState.overrideMapType(Constants.MapType.night);
                return;
            }
            else if(kb==KB_MINIMAP_POS) {
                UIManager.getInstance().getMiniMap().nextPosition();
                return;
            }
        }
        else
        {
            if(kb==KB_MAP) {
                UIManager.getInstance().openMap();
                return;
            }
        }
    }
}
