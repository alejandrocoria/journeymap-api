package journeymap.server.feature;

import journeymap.common.Journeymap;
import journeymap.common.network.core.NetworkHandler;
import journeymap.common.network.model.PlayersInWorld;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.List;

import static journeymap.server.JourneymapServer.isOp;

public class PlayerTrackingThread implements Runnable
{
    private boolean running = true;
    private long updateTime;
    private boolean sendToEveryone;
    private boolean sendToOps;

    public PlayerTrackingThread(long updateTime, boolean sendToEveryone, boolean sendToOps)
    {
        this.updateTime = updateTime;
        this.sendToEveryone = sendToEveryone;
        this.sendToOps = sendToOps;
    }

    @Override
    public void run()
    {
        while (running && !Thread.currentThread().isInterrupted())
        {
            try
            {
                List<EntityPlayerMP> playerList = null;
                try
                {
                    playerList = FMLServerHandler.instance().getServer().getPlayerList().getPlayers();
                    if (playerList != null && playerList.size() > 1)
                    {
                        for (EntityPlayerMP playerMp : playerList)
                        {
                            if (sendToEveryone || (sendToOps && isOp(playerMp)))
                            {
                                PlayersInWorld playerWorldList = getPlayerList(playerMp);
                                NetworkHandler.getInstance().sendPlayerPacketToPlayer(playerMp, playerWorldList);
                            }

                        }
                    }
                }
                catch (NullPointerException npe)
                {
                    // do nothing, server is likely not started yet.
                }
                Thread.sleep(updateTime);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }
        Journeymap.getLogger().info("Player Tracker thread has been terminated.");
    }

    private PlayersInWorld getPlayerList(EntityPlayerMP entityPlayerMP)
    {
        int receiverDimension = entityPlayerMP.dimension;
        boolean receiverOp = isOp(entityPlayerMP);
        PlayersInWorld playerWorldList = new PlayersInWorld();
        List<EntityPlayerMP> playerList = null;
        playerList = FMLServerHandler.instance().getServer().getPlayerList().getPlayers();
        if (playerList != null || playerList.size() > 1)
        {
            for (EntityPlayerMP playerMp : playerList)
            {
                boolean sneaking = playerMp.isSneaking();
                int dimension = playerMp.dimension;
                if (receiverOp)
                {
                    sneaking = false;
                }

                PlayersInWorld.PlayerWorld player = new PlayersInWorld.PlayerWorld(
                        playerMp.getName(),
                        playerMp.getPosition().getX(),
                        playerMp.getPosition().getY(),
                        playerMp.getPosition().getZ(),
                        playerMp.chunkCoordX,
                        playerMp.chunkCoordY,
                        playerMp.chunkCoordZ,
                        playerMp.rotationYawHead,
                        sneaking,
                        playerMp.getUniqueID()
                );

                // Don't send the player to them self and don't send sneaking players unless op is receiving.
                if (!entityPlayerMP.getUniqueID().equals(player.getUuid()) && !sneaking && receiverDimension == dimension)
                {
                    playerWorldList.add(player);
                }
            }
        }
        return playerWorldList;
    }

    public boolean isAlive()
    {
        return running;
    }

    public void stop()
    {
        running = false;
    }
}
