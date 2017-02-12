package journeymap.client.world;

import com.google.common.cache.CacheLoader;
import journeymap.client.data.DataCache;
import journeymap.client.forge.event.EventHandlerManager;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * IWorldEventListener provides a way to find out what blocks are changing
 * in the client.  This allows for a targeted means of remapping just the chunks which
 * are actually getting updated.
 */
public enum ChunkMonitor implements IWorldEventListener, EventHandlerManager.EventHandler
{
    INSTANCE;

    private World theWorld;

    public void reset()
    {
        if (theWorld != null)
        {
            theWorld.removeEventListener(ChunkMonitor.INSTANCE);
        }
        theWorld = null;
    }

    public void resetRenderTimes(ChunkPos pos)
    {
        ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(pos);
        if (chunkMD != null)
        {
            chunkMD.resetRenderTimes();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event)
    {
        if (theWorld == null)
        {
            theWorld = event.getWorld();
            theWorld.addEventListener(this);
            event.getWorld();
        }

        Chunk chunk = event.getChunk();
        int cx1 = chunk.xPosition - 1;
        int cz1 = chunk.zPosition - 1;
        int cx2 = chunk.xPosition + 1;
        int cz2 = chunk.zPosition + 1;

        for (int chunkXPos = cx1; chunkXPos < cx2; chunkXPos++)
        {
            for (int chunkZPos = cz1; chunkZPos < cz2; chunkZPos++)
            {
                resetRenderTimes(new ChunkPos(chunkXPos, chunkZPos));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event)
    {
        //ChunkPos pos = event.getChunk().getChunkCoordIntPair();
        //readyChunks.invalidate(pos);
        //DataCache.INSTANCE.invalidateChunkMD(pos);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        try
        {
            World world = event.getWorld();
            if (world == theWorld)
            {
                reset();
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error handling WorldEvent.Unload", e);
        }
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags)
    {
        resetRenderTimes(new ChunkPos(pos));
    }

    @Override
    public void notifyLightSet(BlockPos pos)
    {
        resetRenderTimes(new ChunkPos(pos));
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        int cx1 = x1 >> 4;
        int cz1 = z1 >> 4;
        int cx2 = x2 >> 4;
        int cz2 = z2 >> 4;

        if (cx1 == cx2 && cz1 == cz2)
        {
            resetRenderTimes(new ChunkPos(cx1, cz1));
        }
        else
        {
            for (int chunkXPos = cx1; chunkXPos < cx2; chunkXPos++)
            {
                for (int chunkZPos = cz1; chunkZPos < cz2; chunkZPos++)
                {
                    resetRenderTimes(new ChunkPos(chunkXPos, chunkZPos));
                }
            }
        }

    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch)
    {
    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos)
    {
    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters)
    {
    }

    @Override
    public void onEntityAdded(Entity entityIn)
    {
    }

    @Override
    public void onEntityRemoved(Entity entityIn)
    {
    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data)
    {
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data)
    {
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
    }

    private static class TimestampLoader extends CacheLoader<ChunkPos, Long>
    {
        @Override
        public Long load(ChunkPos key) throws Exception
        {
            return System.currentTimeMillis();
        }
    }
}
