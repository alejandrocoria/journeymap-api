package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.render.entity.RenderWaypointBeacon;

import java.util.EnumSet;

/**
 * Event handler for rendering waypoints in-game.
 */
public class WaypointBeaconHandler implements EventHandlerManager.EventHandler
{
    final Minecraft mc = FMLClientHandler.instance().getClient();
    final WaypointProperties waypointProperties = JourneyMap.getInstance().waypointProperties;

    public WaypointBeaconHandler()
    {
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event)
    {
        if (mc.thePlayer != null && waypointProperties.beaconEnabled.get())
        {
            RenderWaypointBeacon.renderAll();
        }
    }
}
