/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;

/**
 * Tick handler for JourneyMap state
 */
@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler
{
    static boolean javaChecked = false;
    Minecraft mc = FMLClientHandler.instance().getClient();
    int counter = 0;
    private boolean deathpointCreated;


    @SideOnly(Side.CLIENT)
    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent()
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        mc.mcProfiler.startSection("journeymap");

        if (mc.thePlayer != null && mc.thePlayer.isDead)
        {
            if (!deathpointCreated)
            {
                deathpointCreated = true;
                createDeathpoint();
            }
        }
        else
        {
            deathpointCreated = false;
        }

        if (!javaChecked && mc.thePlayer != null && !mc.thePlayer.isDead)
        {
            checkJava();
        }

        try
        {
            if (counter == 20)
            {
                mc.mcProfiler.startSection("mainTasks");
                Journeymap.getClient().performMainThreadTasks();
                counter = 0;
                mc.mcProfiler.endSection();
            }
            else if (counter == 10)
            {
                mc.mcProfiler.startSection("multithreadTasks");
                if (Journeymap.getClient().isMapping() && mc.theWorld != null)
                {
                    Journeymap.getClient().performMultithreadTasks();
                }
                counter++;
                mc.mcProfiler.endSection();
            }
            else if (counter == 5 || counter == 15)
            {
                mc.mcProfiler.startSection("clientApiEvents");
                ClientAPI.INSTANCE.getClientEventManager().fireNextClientEvents();
                counter++;
                mc.mcProfiler.endSection();
            }
            else
            {
                counter++;
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().warn("Error during onClientTick: " + LogFormatter.toPartialString(t));
        }
        finally
        {
            mc.mcProfiler.endSection();
        }
    }

    private void createDeathpoint()
    {
        try
        {
            EntityPlayer player = mc.thePlayer;
            if (player == null)
            {
                Journeymap.getLogger().error("Lost reference to player before Deathpoint could be created");
                return;
            }

            WaypointProperties waypointProperties = Journeymap.getClient().getWaypointProperties();
            boolean enabled = waypointProperties.managerEnabled.get() && waypointProperties.createDeathpoints.get();
            boolean cancelled = false;

            BlockPos pos = new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
            if (enabled)
            {
                int dim = ForgeHelper.INSTANCE.getPlayerDimension();

                DeathWaypointEvent event = new DeathWaypointEvent(pos, dim);
                ClientAPI.INSTANCE.getClientEventManager().fireDeathpointEvent(event);
                if (!event.isCancelled())
                {
                    Waypoint deathpoint = Waypoint.at(pos, Waypoint.Type.Death, dim);
                    WaypointStore.INSTANCE.save(deathpoint);
                }
                else
                {
                    cancelled = true;
                }
            }

            Journeymap.getLogger().info(String.format("%s died at %s. Deathpoints enabled: %s. Deathpoint created: %s",
                    ForgeHelper.INSTANCE.getEntityName(player),
                    pos,
                    enabled,
                    cancelled ? "cancelled" : true));


        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected Error in createDeathpoint(): " + LogFormatter.toString(t));
        }
    }

    private void checkJava()
    {
        // Ensure Java 7
        javaChecked = true;
        try
        {
            Class.forName("java.util.Objects");
        }
        catch (ClassNotFoundException e)
        {
            try
            {
                String error = I18n.format("jm.error.java6");
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(error));
                Journeymap.getLogger().fatal("JourneyMap requires Java 7 or Java 8. Update your launcher profile to use a newer version of Java.");
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
            Journeymap.getClient().disable();
        }
    }
}
