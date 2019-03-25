package journeymap.common.network.model;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.UUID;

public class PlayersInWorld
{
    public static final Gson GSON = new GsonBuilder().create();

    List<PlayerWorld> playersInWorld = Lists.newArrayList();

    public void add(PlayerWorld player)
    {
        playersInWorld.add(player);
    }

    public List<PlayerWorld> get()
    {
        return playersInWorld;
    }

    public static class PlayerWorld
    {
        private String name;
        private int posX;
        private int posY;
        private int posZ;
        private int chunkX;
        private int chunkY;
        private int chunkZ;
        private float rotationYaw;
        private boolean sneaking;
        private String uuid;
        private boolean fakePlayer;

        public PlayerWorld(String name, int posX, int posY, int posZ, int chunkX, int chunkY, int chunkZ, float rotationYaw, boolean sneaking, UUID uuid)
        {
            this(name, posX, posY, posZ, chunkX, chunkY, chunkZ, rotationYaw, sneaking, uuid, false);
        }

        public PlayerWorld(String name, int posX, int posY, int posZ, int chunkX, int chunkY, int chunkZ, float rotationYaw, boolean sneaking, UUID uuid, boolean fakePlayer)
        {
            this.name = name;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            this.chunkZ = chunkZ;
            this.rotationYaw = rotationYaw;
            this.sneaking = sneaking;
            this.uuid = uuid.toString();
            this.fakePlayer = fakePlayer;
        }

        public String getName()
        {
            return name;
        }

        public int getPosX()
        {
            return posX;
        }

        public int getPosY()
        {
            return posY;
        }

        public int getPosZ()
        {
            return posZ;
        }

        public int getChunkX()
        {
            return chunkX;
        }

        public int getChunkY()
        {
            return chunkY;
        }

        public int getChunkZ()
        {
            return chunkZ;
        }

        public float getRotationYaw()
        {
            return rotationYaw;
        }

        public boolean isSneaking()
        {
            return sneaking;
        }

        public UUID getUuid()
        {
            return UUID.fromString(uuid);
        }

        public boolean isFakePlayer()
        {
            return fakePlayer;
        }

        @Override
        public String toString()
        {
            return "{\"PlayerWorld\":{" +
                    "\"name\":\"" + name + "\"" +
                    ", \"posX\":\"" + posX + "\"" +
                    ", \"posY\":\"" + posY + "\"" +
                    ", \"posZ\":\"" + posZ + "\"" +
                    ", \"chunkX\":\"" + chunkX + "\"" +
                    ", \"chunkY\":\"" + chunkY + "\"" +
                    ", \"chunkZ\":\"" + chunkZ + "\"" +
                    ", \"rotationYaw\":\"" + rotationYaw + "\"" +
                    ", \"sneaking\":\"" + sneaking + "\"" +
                    ", \"uuid\":\"" + uuid + "\"" +
                    ", \"fakePlayer\":\"" + fakePlayer + "\"" +
                    "}}";
        }
    }

    @Override
    public String toString()
    {
        return "{\"PlayersInWorld\":{" +
                "\"playersInWorld\":" + playersInWorld +
                "}}";
    }
}
