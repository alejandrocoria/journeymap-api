package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.JourneyMap;

import java.util.EnumSet;

/**
 * Scheduled tick handler for JourneyMap state
 */
public class StateTickHandler implements IScheduledTickHandler {

    final int tickSpacing = 20;
    final EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);

    @Override
    public int nextTickSpacing() {
        return tickSpacing;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        if (mc.theWorld!=null && mc.thePlayer!=null && !mc.thePlayer.isDead) {
            JourneyMap.getInstance().updateState();
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return tickTypes;
    }

    @Override
    public String getLabel() {
        return this.getClass().getName();
    }
}
