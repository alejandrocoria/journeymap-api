package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;

import java.util.EnumSet;

/**
 * Tick handler for JourneyMap state
 */
@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler, IScheduledTickHandler {

    Minecraft mc = FMLClientHandler.instance().getClient();
    final int tickSpacing = 10;
    boolean flag;
    final EnumSet<TickType> tickTypes = EnumSet.of(TickType.RENDER);

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.ScheduledTickRegistry);
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        Minecraft mc = FMLClientHandler.instance().getClient();

        flag = !flag;
        if(flag) {
            if(JourneyMap.getInstance().isMapping() && mc.theWorld!=null) {
                JourneyMap.getInstance().performTasks();
            }
        } else {
            JourneyMap.getInstance().updateState();
        }
    }

    @Override
    public int nextTickSpacing() {
        return tickSpacing;
    }

    @Override
    public EnumSet<TickType> ticks() {
        return tickTypes;
    }

    @Override
    public String getLabel() {
        return getClass().getName();
    }
}
