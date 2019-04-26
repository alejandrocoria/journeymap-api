package journeymap.client.thread;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import journeymap.common.Journeymap;
import journeymap.common.network.GetPlayerLocations;
import journeymap.common.thread.JMThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;

public class GetAllPlayersThread implements Runnable
{
    private static ExecutorService es;
    private int updateTime = 1000;
    private static boolean alive = false;

    @Override
    public void run()
    {
        while (Minecraft.getMinecraft() != null)
        {
            try
            {
                if (Journeymap.getClient().isJourneyMapServerConnection()
                        && Journeymap.getClient().isPlayerTrackingEnabled()
                        && !Minecraft.getMinecraft().isSingleplayer()
                        && Journeymap.getClient().isMapping())
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
        // Compare list on the server with the list on the client. Add that do not exist on the client.
        for (EntityPlayer player : entityPlayerList)
        {
            boolean inBothLists = PlayerRadarManager.getInstance().getPlayers().stream().anyMatch(p -> p.getUniqueID().equals(player.getUniqueID()));
            if (!inBothLists)
            {
                // Add players from the list on the server to the list on the client.
                PlayerRadarManager.getInstance().addPlayer(player);
            }
        }

        // Compare list on the client with the list on the server. Update players that exist on both, remove players that are no longer on the server list.
        for (Iterator<EntityPlayer> iterator = PlayerRadarManager.getInstance().getPlayers().iterator(); iterator.hasNext();)
        {
            EntityPlayer player = iterator.next();
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
                // Remove any users that are not on the server and that exist on the client.
                iterator.remove();
            }
        }
    }

    /**
     * Create a list of fake players that are not in the client's entity list. The client's list only contains entities that
     * are with in proximity of the player. Any entity that is not in the client list will be created. If they are in the client list, they will not be created.
     * and this method will return null.
     *
     * @param player - The json representation of the player.
     * @return - The EntityPlayer or null.
     */
    private EntityPlayer buildEntityPlayer(JsonObject player)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer playerMp;

        UUID playerUUID = UUID.fromString(player.get("playerId").getAsString());
        String playerName = player.get("name").getAsString();
        if (!playerUUID.equals(mc.player.getUniqueID()))
        {
            playerMp = new EntityOtherPlayerMP(mc.world, new GameProfile(playerUUID, playerName));
            playerMp.posX = player.get("posX").getAsInt();
            playerMp.posY = player.get("posY").getAsInt();
            playerMp.posZ = player.get("posZ").getAsInt();
            playerMp.chunkCoordX = player.get("chunkX").getAsInt();
            playerMp.chunkCoordY = player.get("chunkY").getAsInt();
            playerMp.chunkCoordZ = player.get("chunkZ").getAsInt();
            playerMp.rotationYawHead = player.get("rotation").getAsFloat();
            playerMp.setSneaking(player.get("sneaking").getAsBoolean());
            playerMp.setUniqueId(playerUUID);
            playerMp.addedToChunk = true;
            return playerMp;
        }

        return null;
    }

    public static void stop()
    {
        if (alive)
        {
            alive = false;
            es.shutdown();
        }
    }

    public static void start()
    {
        if (!alive)
        {
            alive = true;
            GetAllPlayersThread runnable = new GetAllPlayersThread();
            JMThreadFactory tf = new JMThreadFactory("player_track");
            es = Executors.newSingleThreadExecutor(tf);
            es.execute(runnable);
            Runtime.getRuntime().addShutdownHook(tf.newThread(() -> {
                alive = false;
                es.shutdown();
            }));
        }
    }
}
