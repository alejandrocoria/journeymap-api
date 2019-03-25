/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.events;

import journeymap.common.network.PacketHandler;
import journeymap.common.network.model.InitLogin;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static journeymap.server.JourneymapServer.isOp;

/**
 * Created by Mysticdrew on 5/5/2018.
 */
public class ForgeEvents
{
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {
//            sendPermissionsPacket((EntityPlayerMP) event.getEntity());
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
//            sendPermissionsPacket((EntityPlayerMP) event.player);
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            sendLoginPacket((EntityPlayerMP) event.player);
//            sendPermissionsPacket((EntityPlayerMP) event.player);
        }
    }


    private void sendLoginPacket(EntityPlayerMP player)
    {
        if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get())
        {
            PacketHandler.getInstance().sendPlayerWorldID((EntityPlayerMP) player);
        }

        InitLogin init = new InitLogin();

        if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get())
        {
            init.setTeleportEnabled(true);
        }
        else if (isOp(player))
        {
            init.setTeleportEnabled(true);
        }
        else
        {
            init.setTeleportEnabled(false);
        }

        if (PropertiesManager.getInstance().getGlobalProperties().playerTrackingEnabled.get())
        {
            init.setTeleportEnabled(true);
        }
        else if (PropertiesManager.getInstance().getGlobalProperties().opPlayerTrackingEnabled.get())
        {
            init.setTeleportEnabled(true);
        }
        else
        {
            init.setTeleportEnabled(false);
        }
        PacketHandler.getInstance().sendLoginPacket(player, init);
    }

}
