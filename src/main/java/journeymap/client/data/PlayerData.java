/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.data;


import com.google.common.cache.CacheLoader;
import journeymap.client.log.JMLogger;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.EntityDTO;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * Provides game-related properties in a Map.
 *
 * @author mwoodman
 */
public class PlayerData extends CacheLoader<Class, EntityDTO>
{
    /**
     * "Underground" means the player isn't under sky (within a 1 block radius),
     * or is under a block that should be treated as potential sky like glass or ladders.
     */
    public static boolean playerIsUnderground(Minecraft mc, EntityPlayer player)
    {
        if(player.getEntityWorld().provider instanceof WorldProviderHell)
        {
            return true;
        }

        final int posX = MathHelper.floor(player.posX);
        final int posY = MathHelper.floor(player.getEntityBoundingBox().minY);
        final int posZ = MathHelper.floor(player.posZ);
        final int offset = 1;

        boolean isUnderground = true;

        if (posY < 0)
        {
            return true;
        }

        check:
        {
            int y = posY;
            for (int x = (posX - offset); x <= (posX + offset); x++)
            {
                for (int z = (posZ - offset); z <= (posZ + offset); z++)
                {
                    y = posY + 1;

                    ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(new ChunkPos(x >> 4, z >> 4));
                    if (chunkMD != null)
                    {
                        if (chunkMD.ceiling(x & 15, z & 15) <= y)
                        {
                            isUnderground = false;
                            break check;
                        }
                    }
                }
            }
        }

        return isUnderground;
    }

    @Override
    public EntityDTO load(Class aClass) throws Exception
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.player;

        EntityDTO dto = DataCache.INSTANCE.getEntityDTO(player);
        dto.update(player, false);
        dto.biome = getPlayerBiome(player);
        dto.underground = playerIsUnderground(mc, player);
        return dto;
    }

    /**
     * Get the biome name where the player is standing.
     */
    private String getPlayerBiome(EntityPlayer player)
    {
        if (player != null)
        {
            try
            {
                Biome biome = FMLClientHandler.instance().getClient().world.getBiomeForCoordsBody(player.getPosition());
                // Can be null right after spawn/teleport before chunks loaded
                if (biome != null)
                {
                    return biome.getBiomeName();
                }
                //return playerChunk.getBiome(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ)).biomeName;
            }
            catch (Exception e)
            {
                JMLogger.logOnce("Couldn't get player biome: " + e.getMessage(), e);
            }
        }
        return "?";
    }

    public long getTTL()
    {
        return Journeymap.getClient().getCoreProperties().cachePlayerData.get();
    }
}
