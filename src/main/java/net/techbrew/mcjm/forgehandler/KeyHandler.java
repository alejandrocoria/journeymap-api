package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.mcjm.JourneyMap;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/3/14.
 */
public class KeyHandler extends KeyBindingRegistry.KeyHandler {

    public KeyHandler() {
        super(new KeyBinding[]{JourneyMap.getInstance().uiKeybinding}, new boolean[]{false});
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        JourneyMap.getInstance().keyboardEvent(kb);
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {

    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel() {
        return null;
    }
}
