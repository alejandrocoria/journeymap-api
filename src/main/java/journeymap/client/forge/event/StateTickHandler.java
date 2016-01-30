/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import journeymap.client.JourneymapClient;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
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
    Minecraft mc = ForgeHelper.INSTANCE.getClient();
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
                JourneymapClient.getInstance().performMainThreadTasks();
                counter = 0;
                mc.mcProfiler.endSection();
            }
            else if (counter == 10)
            {
                mc.mcProfiler.startSection("multithreadTasks");
                if (JourneymapClient.getInstance().isMapping() && mc.theWorld != null)
                {
                    JourneymapClient.getInstance().performMultithreadTasks();
                }
                counter++;
                mc.mcProfiler.endSection();
            }
            else
            {
                counter++;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Error during performMainThreadTasks: " + e);
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

            WaypointProperties waypointProperties = JourneymapClient.getWaypointProperties();
            boolean doCreate = waypointProperties.managerEnabled.get() && waypointProperties.createDeathpoints.get();

            if (doCreate)
            {
                BlockPos pos = new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
                int dim = ForgeHelper.INSTANCE.getPlayerDimension();
                if (ClientAPI.INSTANCE.notifyDeathWaypoint(pos, dim))
                {
                    Waypoint deathpoint = Waypoint.at(pos, Waypoint.Type.Death, dim);
                    WaypointStore.instance().save(deathpoint);
                }
            }

            Journeymap.getLogger().info(String.format("%s died at x:%s, y:%s, z:%s. Deathpoint created: %s",
                    ForgeHelper.INSTANCE.getEntityName(player),
                    MathHelper.floor_double(player.posX),
                    MathHelper.floor_double(player.posY),
                    MathHelper.floor_double(player.posZ),
                    doCreate));

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
                ForgeHelper.INSTANCE.getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(error));
                Journeymap.getLogger().fatal("JourneyMap requires Java 7 or Java 8. Update your launcher profile to use a newer version of Java.");
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
            JourneymapClient.disable();
        }
    }
}