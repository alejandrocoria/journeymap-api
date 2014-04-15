package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.waypoint.EntityWaypoint;
import net.techbrew.journeymap.waypoint.RenderWaypoint;
import net.techbrew.journeymap.waypoint.WaypointHelper;

import java.util.EnumSet;
import java.util.Map;

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

    @ForgeSubscribe
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {

            if(WaypointHelper.isNativeLoaded()) {

                Map<String, Map> waypoints = (Map<String, Map>) DataCache.instance().get(WaypointsData.class).get(EntityKey.root);
                // TODO
            }
        }
    }
}
