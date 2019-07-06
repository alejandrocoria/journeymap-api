/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;

import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.network.GetPlayerLocations;
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

import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;

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
    private static int playerUpdateTicks = 5;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent()
    public void onClientTick(TickEvent.ClientTickEvent event)
    {

        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        mc.mcProfiler.startSection("journeymap");

        if (mc.player != null && mc.player.isDead)
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

        if (!javaChecked && mc.player != null && !mc.player.isDead)
        {
            checkJava();
        }

        try
        {
            if (mc.world != null
                    && mc.world.getWorldTime() % playerUpdateTicks == 0
                    && Journeymap.getClient().isJourneyMapServerConnection()
                    && Journeymap.getClient().isPlayerTrackingEnabled()
                    && !Minecraft.getMinecraft().isSingleplayer()
                    && Journeymap.getClient().isMapping())
            {
                new GetPlayerLocations().send(result -> {
                    playerUpdateTicks = result.getAsJson().get(TRACKING_UPDATE_TIME).getAsInt();
                    Journeymap.getClient().setPlayerTrackingEnabled(result.getAsJson().get(TRACKING).getAsBoolean());
                });
            }

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
                if (Journeymap.getClient().isMapping() && mc.world != null)
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
            EntityPlayer player = mc.player;
            if (player == null)
            {
                Journeymap.getLogger().error("Lost reference to player before Deathpoint could be created");
                return;
            }

            WaypointProperties waypointProperties = Journeymap.getClient().getWaypointProperties();
            boolean enabled = waypointProperties.managerEnabled.get() && waypointProperties.createDeathpoints.get();
            boolean cancelled = false;
            // do not create death waypoint below y:2
            double playerY = MathHelper.floor(player.posY) > 2 ? MathHelper.floor(player.posY) : 2D;
            BlockPos pos = new BlockPos(MathHelper.floor(player.posX), playerY, MathHelper.floor(player.posZ));
            if (enabled)
            {
                int dim = FMLClientHandler.instance().getClient().player.world.provider.getDimension();

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
                    player.getName(),
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
