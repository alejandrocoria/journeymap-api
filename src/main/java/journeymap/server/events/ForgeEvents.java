/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.events;

import journeymap.common.Journeymap;
import journeymap.common.feature.JourneyMapTeleport;
import journeymap.common.network.PacketHandler;
import journeymap.common.network.model.InitLogin;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

/**
 * Created by Mysticdrew on 5/5/2016.
 */
public class ForgeEvents
{
    /**
     * On.
     *
     * @param event the event
     */
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void on(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();

            Journeymap.getLogger().info(((EntityPlayerMP) event.getEntity()).getDisplayNameString() + " joining dimension " + event.getEntity().dimension);
            PermissionProperties prop;
            DimensionProperties dimensionProperties = PropertiesManager.getInstance().getDimProperties(player.dimension);


            try
            {
                /**
                 * Cloning since we do not want to modify the permission properties,
                 * We want a brand new copy to send to the client
                 */
                if (dimensionProperties.enabled.get())
                {
                    prop = (DimensionProperties) dimensionProperties.clone();
                }
                else
                {
                    prop = (GlobalProperties) PropertiesManager.getInstance().getGlobalProperties().clone();
                }

                /**
                 * If player is op, set the cave and radar options on the packet to send.
                 * The client only reads radarEnabled and caveMappingEnabled, it ignores the
                 */
                if (isOp(player))
                {
                    prop.radarEnabled.set(prop.opRadarEnabled.get());
                    prop.caveMappingEnabled.set(prop.opCaveMappingEnabled.get());
                }

                PacketHandler.sendDimensionPacketToPlayer(player, prop);
            }
            catch (CloneNotSupportedException e)
            {
                Journeymap.getLogger().error("CloneNotSupportedException: ", e);
            }

        }
    }

    /**
     * Player logged in event.
     *
     * @param event the event
     */
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get())
            {
                PacketHandler.sendPlayerWorldID((EntityPlayerMP) event.player);
            }

            if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get())
            {
                InitLogin init = new InitLogin();
                boolean canPlayerTeleport;
                if (JourneyMapTeleport.isOp(player))
                {
                    canPlayerTeleport = true;
                }
                else
                {
                    canPlayerTeleport = PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get();
                }
                init.setTeleportEnabled(canPlayerTeleport);
                PacketHandler.sendLoginPacket((EntityPlayerMP) event.player, init);
            }

            if (isOp((EntityPlayerMP) event.player))
            {
                //TODO: send op ui packet
//                System.out.println("PLAYER IS OPS");
            }
            else
            {
                //TODO: sendplayer logged in packet(worldid)
//                System.out.println("NOT OP");
            }
        }
    }

    private boolean isOp(EntityPlayerMP player)
    {
        String[] ops = FMLServerHandler.instance().getServer().getPlayerList().getOppedPlayerNames();
        for (String opName : ops)
        {
            if (player.getDisplayNameString().equalsIgnoreCase(opName))
            {
                return true;
            }
        }
        return false;
    }
}
