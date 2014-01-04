package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;

import java.util.EnumSet;

/**
 * Scheduled tick handler for the TaskController
 */
public class TaskTickHandler implements IScheduledTickHandler {

    final int tickSpacing;
    final EnumSet<TickType> tickTypes = EnumSet.of(TickType.RENDER);

    public TaskTickHandler(){
        tickSpacing = PropertyManager.getIntegerProp(PropertyManager.Key.UPDATETIMER_CHUNKS) / 50; // millis -> ticks
    }

    @Override
    public int nextTickSpacing() {
        return tickSpacing;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        JourneyMap.getInstance().performTasks();
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
