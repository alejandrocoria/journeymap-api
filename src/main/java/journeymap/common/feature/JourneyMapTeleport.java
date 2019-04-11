/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.feature;

import com.mojang.authlib.GameProfile;
import journeymap.common.Journeymap;
import journeymap.server.properties.DimensionProperties;
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

import static journeymap.server.JourneymapServer.isOp;

/**
 * Created by Mysticdrew on 9/15/2018.
 */
public class JourneyMapTeleport
{
    private static final JourneyMapTeleport INSTANCE = new JourneyMapTeleport();

    private JourneyMapTeleport()
    {
    }

    public static JourneyMapTeleport instance()
    {
        return INSTANCE;
    }

    public boolean attemptTeleport(Entity entity, Location location)
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

            if (isTeleportAvailable(entity, location)
                    || creative
                    || cheatMode
                    || isOp((EntityPlayerMP) entity))
            {

                return teleportEntity(mcServer, destinationWorld, entity, location, entity.rotationYaw);
            }
            else
            {
                entity.sendMessage(new TextComponentString("Server has disabled JourneyMap teleport usage for your current or destination dimension."));
                return false;
            }
        }
        return false;
    }

    private boolean isTeleportAvailable(Entity entity, Location location)
    {
        DimensionProperties destinationProperty = PropertiesManager.getInstance().getDimProperties(location.getDim());
        DimensionProperties entityLocationProperty = PropertiesManager.getInstance().getDimProperties(entity.dimension);
        return canDimTeleport(destinationProperty) && canDimTeleport(entityLocationProperty);
    }

    private boolean canDimTeleport(DimensionProperties properties)
    {
        if (properties.enabled.get())
        {
            return properties.teleportEnabled.get();
        }
        return PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get();
    }


    private boolean teleportEntity(MinecraftServer server, World destinationWorld, Entity entity, Location location, float yaw)
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

    private void transferPlayerToWorld(Entity entity, WorldServer toWorldIn)
    {
        entity.setLocationAndAngles(entity.posX + 0.5D, entity.posY, entity.posZ + 0.5D, entity.rotationYaw, entity.rotationPitch);
        toWorldIn.spawnEntity(entity);
        toWorldIn.updateEntityWithOptionalForce(entity, false);
        entity.setWorld(toWorldIn);
    }
}
