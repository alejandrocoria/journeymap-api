package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler extends KeyBindingRegistry.KeyHandler {

    private final Minecraft mc = FMLClientHandler.instance().getClient();
    private final KeyBinding uiKeybinding = JourneyMap.getInstance().uiKeybinding;

    public KeyEventHandler() {
        super(new KeyBinding[] {
                JourneyMap.getInstance().uiKeybinding,
                new KeyBinding("key.jm.close", Keyboard.KEY_ESCAPE),
                new KeyBinding("key.jm.zoomin", Keyboard.KEY_ADD),
                new KeyBinding("key.jm.zoomin2", Keyboard.KEY_EQUALS),
                new KeyBinding("key.jm.zoomout", Keyboard.KEY_MINUS),
                new KeyBinding("key.jm.day", Keyboard.KEY_LBRACKET),
                new KeyBinding("key.jm.night", Keyboard.KEY_RBRACKET),
                new KeyBinding("key.jm.position", Keyboard.KEY_BACKSLASH),
        }, new boolean[]{
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        });
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        //final int keyPressed = Keyboard.getEventKey();
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
        if(!tickEnd) return;
        final int keyPressed = Keyboard.getEventKey();

        MapOverlayState mapOverlayState = MapOverlay.state();
        if(mapOverlayState.minimapHotkeys && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
            if(keyPressed==uiKeybinding.keyCode) {
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
                    case Keyboard.KEY_BACKSLASH : {
                        UIManager.getInstance().getMiniMap().nextPosition();
                        return;
                    }
                }
            }
        }
        else
        {
            if(keyPressed==uiKeybinding.keyCode) {
                if(mc.currentScreen==null) {
                    UIManager.getInstance().openMap();
                }
            }
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel() {
        return getClass().getName();
    }
}
