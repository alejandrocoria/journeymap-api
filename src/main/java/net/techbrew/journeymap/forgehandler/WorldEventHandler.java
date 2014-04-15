package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.waypoint.WaypointHelper;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class WorldEventHandler implements EventHandlerManager.EventHandler {

    String playerName;

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @ForgeSubscribe
    public void invoke(LivingDeathEvent event)
    {
        try
        {
            if (WaypointHelper.isNativeLoaded())
            {
                Entity deadEntity = event.entity;
                if (deadEntity != null && deadEntity instanceof EntityPlayer)
                {
                    if (playerName == null)
                    {
                        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                        if (player != null)
                        {
                            playerName = player.getEntityName();
                        }
                    }

                    if (playerName != null)
                    {
                        if (playerName.equals(deadEntity.getEntityName()))
                        {
                            WaypointStore.instance().save(Waypoint.deathOf(deadEntity));
                        }
                    }
                }
            }
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().warning("Error handling LivingDeathEvent: " + t.getMessage());
        }
    }

    @ForgeSubscribe
    public void invoke(WorldEvent.Unload event)
    {
        JourneyMap.getInstance().stopMapping();
    }
}
