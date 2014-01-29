package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.mcjm.JourneyMap;
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

        if(Keyboard.getEventKey()==uiKeybinding.func_151463_i()) {
            if(mc.currentScreen==null) {
                UIManager.getInstance().openMap();
            } else if(mc.currentScreen instanceof MapOverlay) {
                //UIManager.getInstance().closeAll();
            }
        }
    }
}
