package net.techbrew.journeymap.data;


import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.EntityDTO;

/**
 * Provides game-related properties in a Map.
 *
 * @author mwoodman
 */
public class PlayerData extends CacheLoader<Class, EntityDTO>
{
    @Override
    public EntityDTO load(Class aClass) throws Exception
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityClientPlayerMP player = mc.thePlayer;

        EntityDTO dto = DataCache.instance().getEntityDTO(player);
        dto.update(player, false);
        dto.biome = getPlayerBiome(mc, player) ;
        dto.underground = playerIsUnderground(mc, player) ;
        return dto;
    }

    /**
     * Get the biome name where the player is standing.
     */
    private String getPlayerBiome(Minecraft mc, EntityClientPlayerMP player)
    {
        int x = ((int) Math.floor(player.posX) % 16) & 15;
        int z = ((int) Math.floor(player.posZ) % 16) & 15;

        ChunkMD playerChunk = DataCache.instance().getChunkMD(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ));
        if (playerChunk != null)
        {
            return playerChunk.stub.getBiomeGenForWorldCoords(x, z, mc.theWorld.getWorldChunkManager()).biomeName;
        }
        else
        {
            return "?"; //$NON-NLS-1$
        }
    }

    /**
     * Check whether player isn't under sky
     */
    public static boolean playerIsUnderground(Minecraft mc, EntityPlayer player)
    {
        if (player.worldObj.provider.hasNoSky)
        {
            return true;
        }

        final int posX = (int) Math.floor(player.posX);
        final int posY = (int) Math.floor(player.posY) - 1;
        final int posZ = (int) Math.floor(player.posZ);
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
                        if(chunkMD.ceiling(x & 15, z & 15)<=y)
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

    public long getTTL()
    {
        return JourneyMap.getInstance().coreProperties.cachePlayerData.get();
    }
}
