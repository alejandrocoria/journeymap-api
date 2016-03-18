package journeymap.server.oldservercode.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOps;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.UUID;

/**
 * Created by Mysticdrew on 10/8/2014.
 */
public class ForgePlayerUtil implements IPlayerUtil
{

    public static ForgePlayerUtil instance = new ForgePlayerUtil();

    /**
     * Gets the player's GameProfile by their UUID.
     *
     * @param uuid {@Link UUID}
     * @return {@link com.mojang.authlib.GameProfile}
     */
    public GameProfile getPlayerInfoById(UUID uuid)
    {
        MinecraftServer server = FMLServerHandler.instance().getServer();
        // 1.8
        // GameProfile gameProfile = server.getPlayerProfileCache().func_152652_a(uuid);

        // 1.8.8
        GameProfile gameProfile = server.getPlayerProfileCache().getProfileByUUID(uuid);
        return gameProfile;
    }

    /**
     * Gets the player's GameProfile by their UUID.
     *
     * @param playerName
     * @return {@link com.mojang.authlib.GameProfile}
     */
    public GameProfile getPlayerProfileByName(String playerName)
    {
        MinecraftServer server = FMLServerHandler.instance().getServer();
        GameProfile gameProfile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        return gameProfile;
    }

    /**
     * Checks the config manager to see if the user is an op or not.
     *
     * @param playerName
     * @return boolean if the user is an op or not
     */
    public boolean isOp(String playerName)
    {
        EntityPlayerMP player = getPlayerEntityByName(playerName);
        if (player instanceof EntityPlayerMP)
        {
            UserListOps ops = FMLServerHandler.instance().getServer().getPlayerList().getOppedPlayers();
            for (String name : ops.getKeys())
            {
                if (playerName.equals(name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the EntityPlayerMP by a the Player's Name.
     *
     * @param name
     * @return {@link net.minecraft.entity.player.EntityPlayerMP}
     */
    public EntityPlayerMP getPlayerEntityByName(String name)
    {
        MinecraftServer server = FMLServerHandler.instance().getServer();;
        return server.getPlayerList().getPlayerByUsername(name);
    }

    /**
     * Gets the EntityPlayerMP by a the Player's Name.
     *
     * @param uuid
     * @return {@link net.minecraft.entity.player.EntityPlayerMP}
     */
    public EntityPlayerMP getPlayerEntityByUUID(UUID uuid)
    {
        MinecraftServer server = FMLServerHandler.instance().getServer();;
        return server.getPlayerList().createPlayerForUser(getPlayerInfoById(uuid));
    }
}
