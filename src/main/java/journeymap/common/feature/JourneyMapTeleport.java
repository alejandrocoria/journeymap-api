package journeymap.common.feature;

import com.mojang.authlib.GameProfile;
import journeymap.common.Journeymap;
import journeymap.common.network.model.Location;
import journeymap.server.JourneymapServer;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Created by Mysticdrew on 9/15/2016.
 */
public class JourneyMapTeleport
{
    public static boolean attemptTeleport(Entity entity, Location location)
    {
        MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        boolean creative = false;
        boolean cheatMode = false;
        World destinationWorld;

        if (entity == null)
        {
            Journeymap.getLogger().error("Attempted to teleport null entity.");
            return false;
        }
        if (entity instanceof EntityPlayerMP)
        {
            creative = ((EntityPlayerMP) entity).capabilities.isCreativeMode;
            cheatMode = mcServer.getPlayerList().canSendCommands(new GameProfile(entity.getUniqueID(), entity.getName()));


            if (mcServer == null)
            {
                entity.sendMessage(new TextComponentString("Cannot Find World"));
                return false;
            }

            destinationWorld = mcServer.getWorld(location.getDim());
            if (!entity.isEntityAlive())
            {
                entity.sendMessage(new TextComponentString("Cannot teleport when dead."));
                return false;
            }

            if (destinationWorld == null)
            {
                entity.sendMessage(new TextComponentString("Could not get world for Dimension " + location.getDim()));
                return false;
            }

            if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get()
                    || debugOverride(entity)
                    || creative
                    || cheatMode
                    || isOp((EntityPlayerMP) entity))
            {
                teleportEntity(mcServer, destinationWorld, entity, location, entity.rotationYaw);
                return true;
            }
            else
            {
                entity.sendMessage(new TextComponentString("Server has disabled JourneyMap teleporting."));
                return false;
            }
        }
        return false;
    }

    private static boolean teleportEntity(MinecraftServer server, World destinationWorld, Entity entity, Location location, float yaw)
    {
        World startWorld = entity.world;
        boolean changedWorld = startWorld != destinationWorld;
        PlayerList playerList = server.getPlayerList();

        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            player.dismountRidingEntity();

            if (changedWorld)
            {
                player.dimension = location.getDim();
                player.connection.sendPacket(new SPacketRespawn(player.dimension, player.world.getDifficulty(), destinationWorld.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
                playerList.updatePermissionLevel(player);
                startWorld.removeEntityDangerously(player);
                player.isDead = false;
                transferPlayerToWorld(player, (WorldServer) destinationWorld);
                playerList.preparePlayer(player, (WorldServer) startWorld);
                player.connection.setPlayerLocation(location.getX() + 0.5D, location.getY(), location.getZ() + 0.5D, yaw, entity.rotationPitch);
                player.interactionManager.setWorld((WorldServer) destinationWorld);
                player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
                playerList.updateTimeAndWeatherForPlayer(player, (WorldServer) destinationWorld);
                playerList.syncPlayerInventory(player);

                for (PotionEffect potioneffect : player.getActivePotionEffects())
                {
                    player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
                }

                FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, player.dimension, location.getDim());
                return true;
            }
            else
            {
                player.connection.setPlayerLocation(location.getX() + 0.5D, location.getY(), location.getZ() + 0.5D, yaw, entity.rotationPitch);
                ((WorldServer) destinationWorld).getChunkProvider().loadChunk((int) location.getX() >> 4, (int) location.getZ() >> 4);
                return true;
            }
        }

        return false;
    }

    private static void transferPlayerToWorld(Entity entity, WorldServer toWorldIn)
    {
        entity.setLocationAndAngles(entity.posX + 0.5D, entity.posY, entity.posZ + 0.5D, entity.rotationYaw, entity.rotationPitch);
        toWorldIn.spawnEntity(entity);
        toWorldIn.updateEntityWithOptionalForce(entity, false);
        entity.setWorld(toWorldIn);
    }

    private static boolean debugOverride(Entity sender)
    {
        if ((JourneymapServer.DEV_MODE)
                && ("mysticdrew".equalsIgnoreCase(sender.getName()) || "techbrew".equalsIgnoreCase(sender.getName())))
        {
            return true;
        }
        return false;
    }

    public static boolean isOp(EntityPlayerMP player)
    {
        String[] ops = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayerNames();
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
