package net.techbrew.journeymap.forgehandler;

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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler extends KeyBindingRegistry.KeyHandler {

    private static boolean[] boolarray;
    static {
        boolarray = new boolean[Constants.KEYBINDINGS.length];
        Arrays.fill(boolarray, false);
    }

    public KeyEventHandler() {
        super(Constants.KEYBINDINGS, boolarray);
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
        if(tickEnd) KeyEventHandler.onKeypress(false);
    }

    public static void onKeypress(boolean minimapOnly) {
        final int i = Keyboard.getEventKey();
        MapOverlayState mapOverlayState = MapOverlay.state();
        if(mapOverlayState.minimapHotkeys && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {

            if(i==Constants.KB_MAP.keyCode) {
                UIManager.getInstance().toggleMinimap();
                return;
            }
            else if(i==Constants.KB_MAP_ZOOMIN.keyCode) {
                mapOverlayState.zoomIn();
                return;
            }
            else if(i==Constants.KB_MAP_ZOOMOUT.keyCode) {
                mapOverlayState.zoomOut();
                return;
            }
            else if(i==Constants.KB_MAP_DAY.keyCode) {
                mapOverlayState.overrideMapType(Constants.MapType.day);
                return;
            }
            else if(i==Constants.KB_MAP_NIGHT.keyCode) {
                mapOverlayState.overrideMapType(Constants.MapType.night);
                return;
            }
            else if(i==Constants.KB_MINIMAP_POS.keyCode) {
                UIManager.getInstance().getMiniMap().nextPosition();
                JourneyMap.getLogger().info("next!");
                return;
            }
        }
        else if(!minimapOnly)
        {
            if(i==Constants.KB_MAP.keyCode) {
                if(Minecraft.getMinecraft().currentScreen==null) {
                    UIManager.getInstance().openMap();
                } else if(Minecraft.getMinecraft().currentScreen instanceof MapOverlay) {
                    UIManager.getInstance().closeAll();
                }
                return;
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

