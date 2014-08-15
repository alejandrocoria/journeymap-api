/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.data;


import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.EntityDTO;

/**
 * Provides game-related properties in a Map.
 *
 * @author mwoodman
 */
public class PlayerData extends CacheLoader<Class, EntityDTO>
{
    /**
     * Check whether player isn't under sky
     */
    public static boolean playerIsUnderground(Minecraft mc, EntityPlayer player)
    {
        if (player.worldObj.provider.hasNoSky)
        {
            return true;
        }

        final int posX = MathHelper.floor_double(player.posX);
        final int posY = MathHelper.floor_double(player.boundingBox.minY);
        final int posZ =  MathHelper.floor_double(player.posZ);
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

                    ChunkMD chunkMD = DataCache.instance().getChunkMD(new ChunkCoordIntPair(x >> 4, z >> 4), true);
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
        EntityClientPlayerMP player = mc.thePlayer;

        EntityDTO dto = DataCache.instance().getEntityDTO(player);
        dto.update(player, false);
        dto.biome = getPlayerBiome(mc, player);
        dto.underground = playerIsUnderground(mc, player);
        return dto;
    }

    /**
     * Get the biome name where the player is standing.
     */
    private String getPlayerBiome(Minecraft mc, EntityClientPlayerMP player)
    {
        int x = (MathHelper.floor_double(player.posX) >> 4) & 15;
        int z = (MathHelper.floor_double(player.posZ) >> 4) & 15;

        ChunkMD playerChunk = DataCache.instance().getChunkMD(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ));
        if (playerChunk != null)
        {
            return playerChunk.getChunk().getBiomeGenForWorldCoords(x, z, mc.theWorld.getWorldChunkManager()).biomeName;
        }
        else
        {
            return "?"; //$NON-NLS-1$
        }
    }

    public long getTTL()
    {
        return JourneyMap.getInstance().coreProperties.cachePlayerData.get();
    }
}
