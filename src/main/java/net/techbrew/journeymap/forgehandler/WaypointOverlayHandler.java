package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.waypoint.EntityWaypoint;
import net.techbrew.journeymap.waypoint.RenderWaypoint;

import java.util.EnumSet;

/**
 * RenderGameOverlayEvent handler for rendering the MiniMap
 */
@SideOnly(Side.CLIENT)
public class WaypointOverlayHandler implements EventHandlerManager.EventHandler {

    final Minecraft mc = FMLClientHandler.instance().getClient();

    public WaypointOverlayHandler()
    {
        int waypointEntityId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityWaypoint.class, "JMWaypoint", waypointEntityId);
        EntityRegistry.registerModEntity(EntityWaypoint.class, "JMWaypoint", waypointEntityId, JourneyMap.getInstance(), 128, 1, false);
        RenderManager.instance.entityRenderMap.put(EntityWaypoint.class, new RenderWaypoint(Minecraft.getMinecraft(), RenderManager.instance));
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {

            if(WaypointsData.isNativeEnabled())
            {
                // TODO
            }
        }
    }
}
