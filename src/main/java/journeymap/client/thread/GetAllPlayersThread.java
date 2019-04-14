package journeymap.client.thread;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import journeymap.common.network.GetPlayerLocations;
import journeymap.common.thread.JMThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;

public class GetAllPlayersThread implements Runnable
{
    private int updateTime = 1000;
    private static boolean alive = false;

    @Override
    public void run()
    {
        while (Minecraft.getMinecraft() != null)
        {
            try
            {
                if (Journeymap.getClient().isJourneyMapServerConnection() && Journeymap.getClient().isPlayerTrackingEnabled() && !Minecraft.getMinecraft().isSingleplayer())
                {
                    new GetPlayerLocations().send(result -> {
                        JsonArray playerList = result.getAsJson().get("players").getAsJsonArray();
                        updateTime = result.getAsJson().get(TRACKING_UPDATE_TIME).getAsInt();
                        Journeymap.getClient().setPlayerTrackingEnabled(result.getAsJson().get(TRACKING).getAsBoolean());
                        List<EntityPlayer> entityPlayerList = Lists.newArrayList();

                        for (JsonElement p : playerList)
                        {
                            JsonObject player = p.getAsJsonObject();
                            EntityPlayer entityPlayer = buildEntityPlayer(player);
                            if (entityPlayer != null)
                            {
                                entityPlayerList.add(entityPlayer);
                            }
                        }
                        updatePlayerList(entityPlayerList);
                    });
                }
                else
                {
                    // If it is disabled we'll wait 10s to check again to see if it is enabled.
                    updateTime = 10000;
                }
                Thread.sleep(updateTime);
            }
            catch (InterruptedException e)
            {
                alive = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updatePlayerList(List<EntityPlayer> entityPlayerList)
    {

        for (EntityPlayer player : entityPlayerList)
        {
            boolean inBothLists = Journeymap.getClient().playersOnServer.stream().anyMatch(p -> p.getUniqueID().equals(player.getUniqueID()));
            if (!inBothLists)
            {
                Journeymap.getClient().playersOnServer.add(player);
            }
        }

        for (EntityPlayer player : Journeymap.getClient().playersOnServer)
        {
            boolean inBothLists = entityPlayerList.stream().anyMatch(p -> p.getUniqueID().equals(player.getUniqueID()));
            if (inBothLists)
            {
                EntityPlayer playerMp = entityPlayerList.stream().filter((p -> p.getUniqueID().equals(player.getUniqueID()))).findFirst().orElse(null);
                if (playerMp != null)
                {
                    player.posX = playerMp.posX;
                    player.posY = playerMp.posY;
                    player.posZ = playerMp.posZ;
                    player.chunkCoordX = playerMp.chunkCoordX;
                    player.chunkCoordY = playerMp.chunkCoordY;
                    player.chunkCoordZ = playerMp.chunkCoordZ;
                    player.rotationYawHead = playerMp.rotationYawHead;
                    player.setSneaking(playerMp.isSneaking());
                }
            }
            else
            {
                Journeymap.getClient().playersOnServer.remove(player);
                if(Journeymap.getClient().playersOnServer.size() < 1) {
                    // Breaking to prevent ConcurrentModificationException.
                    break;
                }
            }
        }
    }

    private EntityPlayer buildEntityPlayer(JsonObject player)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        List<EntityPlayer> clientEntites = mc.world.playerEntities;

        EntityPlayer playerMp;

        for (NetworkPlayerInfo onlinePlayer : mc.getConnection().getPlayerInfoMap())
        {
            // If player is already in list, they are close enough for the client to see so ignore server tracking.
            boolean playerInList = clientEntites.stream().anyMatch(p -> p.getUniqueID().equals(onlinePlayer.getGameProfile().getId()));

            if (!onlinePlayer.getGameProfile().getId().equals(mc.player.getUniqueID()) && !playerInList)
            {
                playerMp = new EntityOtherPlayerMP(mc.world, onlinePlayer.getGameProfile());
                playerMp.posX = player.get("posX").getAsInt();
                playerMp.posY = player.get("posY").getAsInt();
                playerMp.posZ = player.get("posZ").getAsInt();
                playerMp.chunkCoordX = player.get("chunkX").getAsInt();
                playerMp.chunkCoordY = player.get("chunkY").getAsInt();
                playerMp.chunkCoordZ = player.get("chunkZ").getAsInt();
                playerMp.rotationYawHead = player.get("rotation").getAsFloat();
                playerMp.setSneaking(player.get("sneaking").getAsBoolean());
                playerMp.setUniqueId(UUID.fromString(player.get("playerId").getAsString()));
                playerMp.addedToChunk = true;
                return playerMp;
            }
        }
        return null;
    }


    public static void start()
    {
        if (!alive)
        {
            alive = true;
            GetAllPlayersThread thread = new GetAllPlayersThread();
            JMThreadFactory tf = new JMThreadFactory("player_track");
            ExecutorService es = Executors.newSingleThreadExecutor(tf);
            es.execute(thread);
            Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    alive = false;
                    es.isShutdown();
                }
            }));
        }
    }
}
