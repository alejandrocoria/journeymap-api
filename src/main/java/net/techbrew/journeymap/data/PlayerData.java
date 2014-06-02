package net.techbrew.journeymap.data;


import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.nbt.ChunkLoader;
import net.techbrew.journeymap.model.BlockUtils;
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

        EntityDTO dto = new EntityDTO(player, false);
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

        ChunkMD playerChunk = ChunkLoader.getChunkStubFromMemory(player.chunkCoordX, player.chunkCoordZ, mc);
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
        int x = 0, y = 0, z = 0, blockId = 0;
        boolean isUnderground = true;

        if (y < 0)
        {
            return true;
        }

        check:
        {
            for (x = (posX - offset); x <= (posX + offset); x++)
            {
                for (z = (posZ - offset); z <= (posZ + offset); z++)
                {
                    y = posY + 1;
                    if (canSeeSky(player.worldObj, x, y, z))
                    {
                        isUnderground = false;
                        break check;
                    }
                }

            }
        }
        //System.out.println("underground: " + isUnderground);
        return isUnderground;
    }

    /**
     * Potentially dangerous to use anywhere other than for player's current position
     * - seems to cause crashes when used with ChunkRenderer.paintUnderground()
     */
    private static boolean canSeeSky(World world, final int x, final int y, final int z)
    {
        boolean seeSky = true;
        Block block;

        int topY = world.getTopSolidOrLiquidBlock(x, z);
        if (y >= topY)
        {
            return true;
        }

        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        int checkY = topY;
		while(seeSky && checkY>y) {
            try {
                block = chunk.getBlock(x & 15, checkY, z & 15);
                if(BlockUtils.hasFlag(block, BlockUtils.Flag.NotHideSky)) {
                    checkY--;
                } else {
                        seeSky = false;
                        break;
                    }
            } catch (Exception e) {
                checkY--;
                JourneyMap.getLogger().warning(e + " at " + (x & 15) + "," + checkY + "," + (z & 15));
                continue;
            }

        }
        return seeSky;
    }

    public long getTTL()
    {
        return JourneyMap.getInstance().coreProperties.cachePlayerData.get();
    }
}
