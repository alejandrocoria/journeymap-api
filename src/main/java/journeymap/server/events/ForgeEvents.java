/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.events;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import journeymap.common.network.GetClientConfig;
import journeymap.common.network.GetPlayerLocations;
import journeymap.common.util.PlayerConfigController;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import static journeymap.server.JourneymapServer.isOp;

/**
 * Created by Mysticdrew on 5/5/2018.
 */
public class ForgeEvents
{
    private static int playerUpdateTicks = 5;

    @SubscribeEvent
    public void onServerTickEvent(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        if (PropertiesManager.getInstance().getGlobalProperties().playerTrackingEnabled.get()
                && FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().size() > 1)
        {
            playerUpdateTicks = PropertiesManager.getInstance().getGlobalProperties().playerTrackingUpdateTime.get();
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            if (world != null && world.getWorldTime() % playerUpdateTicks == 0)
            {
                sendPlayersOnRadarToPlayers();
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {
            sendConfigsToPlayer((EntityPlayerMP) event.getEntity());
        }
    }


    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            sendConfigsToPlayer((EntityPlayerMP) event.player);
        }
    }


    private void sendConfigsToPlayer(EntityPlayerMP player)
    {
        JsonObject config = PlayerConfigController.getInstance().getPlayerConfig(player);
        new GetClientConfig().sendToPlayer(config, player);
    }

    private void sendPlayersOnRadarToPlayers()
    {
        GlobalProperties prop = PropertiesManager.getInstance().getGlobalProperties();
        boolean sendToEveryone = prop.playerTrackingEnabled.get();
        boolean sendToOps = prop.opPlayerTrackingEnabled.get();

        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
        {
            boolean playerRadarEnabled = PropertiesManager.getInstance().getDimProperties(player.dimension).playerRadarEnabled.get();
            boolean receiverOp = isOp(player);
            if ((sendToEveryone && playerRadarEnabled) || (sendToOps && receiverOp))
            {
                try
                {
                    sendPlayerTrackingData(player);
                }
                catch (ConcurrentModificationException cme)
                {
                    // do nothing.
                }
            }
        }
    }

    private void sendPlayerTrackingData(EntityPlayerMP entityPlayerMP)
    {
        int receiverDimension = entityPlayerMP.dimension;
        boolean receiverOp = isOp(entityPlayerMP);
        List<EntityPlayerMP> serverPlayers = new ArrayList<>(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());
        List<JsonObject> playerList = new ArrayList<>();
        if (serverPlayers != null || serverPlayers.size() > 1)
        {
            for (EntityPlayerMP playerMp : serverPlayers)
            {
                boolean sneaking = playerMp.isSneaking();
                int dimension = playerMp.dimension;
                UUID playerId = playerMp.getUniqueID();

                if (!entityPlayerMP.getUniqueID().equals(playerId) && !sneaking /*&& receiverDimension == dimension*/)
                {
                    playerList.add(buildJsonPlayer(playerMp, receiverOp));
                }
            }
            sendPlayerList(playerList, entityPlayerMP);
        }
    }

    /**
     * Sends 10 players at a time due to limitations in the packet size.
     *
     * @param allPlayers - The all players list.
     */
    private void sendPlayerList(List<JsonObject> allPlayers, EntityPlayerMP player)
    {
        List<List<JsonObject>> partitionedPlayerList = Lists.partition(allPlayers, 10);
        for (List<JsonObject> playerList : partitionedPlayerList)
        {
            JsonArray playerArray = new JsonArray();
            for (JsonObject playerJsonObject : playerList)
            {
                playerArray.add(playerJsonObject);
            }
            JsonObject payload = new JsonObject();
            payload.add("players", playerArray);
            new GetPlayerLocations().sendToPlayer(payload, player);
        }
    }

    private JsonObject buildJsonPlayer(EntityPlayer playerMp, boolean receiverOp)
    {
        boolean sneaking = playerMp.isSneaking();
        UUID playerId = playerMp.getUniqueID();
        if (receiverOp)
        {
            sneaking = false;
        }
        JsonObject player = new JsonObject();
        player.addProperty("name", playerMp.getName());
        player.addProperty("posX", playerMp.getPosition().getX());
        player.addProperty("posY", playerMp.getPosition().getY());
        player.addProperty("posZ", playerMp.getPosition().getZ());
        player.addProperty("chunkX", playerMp.chunkCoordX);
        player.addProperty("chunkY", playerMp.chunkCoordY);
        player.addProperty("chunkZ", playerMp.chunkCoordZ);
        player.addProperty("rotation", playerMp.rotationYawHead);
        player.addProperty("sneaking", sneaking);
        player.addProperty("playerId", playerId.toString());
        player.addProperty("dim", playerMp.dimension);
        return player;
    }
}
