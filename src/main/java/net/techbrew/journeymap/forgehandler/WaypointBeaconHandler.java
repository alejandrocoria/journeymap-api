package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.techbrew.journeymap.data.WaypointsData;
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
    @ForgeSubscribe
    public void onRenderWorldLastEvent(RenderWorldLastEvent event)
    {
        if (mc.thePlayer != null && waypointProperties.enabled.get())
        {
            RenderWaypointBeacon.renderAll();
        }
    }
}
