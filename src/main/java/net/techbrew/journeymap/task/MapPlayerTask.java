package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.nbt.ChunkLoader;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.model.ChunkMD;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapPlayerTask extends BaseMapTask
{
    private static final Logger logger = JourneyMap.getLogger();
    private static TreeSet<ChunkCoordIntPair> queuedChunks = new TreeSet<ChunkCoordIntPair>(getDistanceComparator());
    private static ChunkMD.Set lastChunkStubs = new ChunkMD.Set(512);
    private static ChunkCoordinates lastPlayerPos;
    private static boolean forceRefresh;

    public static boolean queueChunk(ChunkCoordIntPair chunkCoords)
    {
        synchronized (queuedChunks)
        {
            return queuedChunks.add(chunkCoords);
        }
    }

    public static void dequeueChunk(ChunkCoordIntPair chunkCoords)
    {
        synchronized (queuedChunks)
        {
            queuedChunks.remove(chunkCoords);
        }
        synchronized (lastChunkStubs)
        {
            lastChunkStubs.remove(chunkCoords);
        }
    }

    private static Comparator<ChunkCoordIntPair> getDistanceComparator()
    {
        return new Comparator<ChunkCoordIntPair>()
        {
            Minecraft minecraft = Minecraft.getMinecraft();

            @Override
            public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2)
            {
                if (minecraft.thePlayer == null)
                {
                    return 0;
                }
                double d1 = minecraft.thePlayer.getDistanceSq(o1.getCenterXPos(), minecraft.thePlayer.posY, o1.getCenterZPosition());
                double d2 = minecraft.thePlayer.getDistanceSq(o2.getCenterXPos(), minecraft.thePlayer.posY, o2.getCenterZPosition());
                return Double.compare(d1, d2);
            }
        };
    }

    private MapPlayerTask(World world, int dimension, boolean underground, Integer chunkY, ChunkMD.Set chunkStubs)
    {
        super(world, dimension, underground, chunkY, chunkStubs, false);
    }

    public static BaseMapTask create(EntityPlayer player)
    {
        int missing = 0;
        int offset = JourneyMap.getInstance().coreProperties.chunkOffset.get();

        final ChunkCoordinates playerPos = new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        final boolean underground = DataCache.getPlayer().underground && FeatureManager.instance().isAllowed(Feature.MapCaves) && JourneyMap.getInstance().fullMapProperties.showCaves.get();
        final int dimension = DataCache.getPlayer().dimension;

        if (lastPlayerPos!=null && lastPlayerPos.equals(playerPos))
        {
            offset = Math.max(2, offset/2);
        }
        lastPlayerPos = playerPos;

        final int side = offset + offset + 1;
        final ChunkMD.Set chunks = new ChunkMD.Set(side*3); // *3 to avoid map growth
        final World world = player.worldObj;

        final Integer chunkY = underground ? lastPlayerPos.posY : null;

        final ChunkCoordIntPair min = new ChunkCoordIntPair(lastPlayerPos.posX - offset, lastPlayerPos.posZ - offset);
        final ChunkCoordIntPair max = new ChunkCoordIntPair(lastPlayerPos.posX + offset, lastPlayerPos.posZ + offset);

        ChunkMD chunkMd;
        ChunkCoordIntPair coord;
        for (int x = min.chunkXPos; x <= max.chunkXPos; x++)
        {
            for (int z = min.chunkZPos; z <= max.chunkZPos; z++)
            {
                coord = new ChunkCoordIntPair(x, z);
                synchronized (lastChunkStubs)
                {
                    chunkMd = lastChunkStubs.get(coord);
                }
                if (chunkMd == null)
                {
                    chunkMd = ChunkLoader.getChunkStubFromMemory(x, z, world);
                }
                else
                {
                    chunkMd = ChunkLoader.refreshChunkStubFromMemory(chunkMd, world);
                }
                if (chunkMd != null)
                {
                    chunkMd.render = true;
                    chunks.add(chunkMd);
                }
                else
                {
                    missing++;
                }
            }
        }

        // Pull queued coords with as little delay as possible
        HashSet<ChunkCoordIntPair> tempQueue = new HashSet<ChunkCoordIntPair>(0);
        int initialQueueSize;
        synchronized (queuedChunks)
        {
            initialQueueSize = queuedChunks.size();
            tempQueue.addAll(queuedChunks);
            queuedChunks.clear();
        }

        // Add remaining queued chunks
        int maxChunks = 512;
        int queued = 0;

        Iterator<ChunkCoordIntPair> iter = tempQueue.iterator();
        while (iter.hasNext())
        {
            coord = iter.next();
            chunkMd = ChunkLoader.getChunkStubFromMemory(coord.chunkXPos, coord.chunkZPos, world);
            if (chunkMd != null)
            {
                chunkMd.render = true;
                chunks.add(chunkMd);
                synchronized (lastChunkStubs)
                {
                    lastChunkStubs.put(coord, chunkMd);
                }
                queued++;
            }
            else
            {
                missing++;
            }
            if (queued > maxChunks)
            {
                logger.info(String.format("Exceeded maxChunks, discarding %s", tempQueue.size() - maxChunks));
            }
        }

        int skipped = 0;

        // Remove unchanged chunkstubs from last task
        synchronized (lastChunkStubs)
        {
            if (!lastChunkStubs.isEmpty())
            {
                ChunkMD.Set removed = new ChunkMD.Set(64);
                for (ChunkMD oldChunk : lastChunkStubs)
                {
                    if (!chunks.containsKey(oldChunk.coord))
                    {
                        if (oldChunk.discard(1) > 4)
                        {
                            if (logger.isLoggable(Level.FINE))
                            {
                                logger.fine("Discarding out-of-range chunk: " + oldChunk);
                            }
                            removed.add(oldChunk);
                        }
                        continue;
                    }
                    else
                    {
                        oldChunk.discard(-1);
                    }

                    ChunkMD newChunk = chunks.get(oldChunk.coord);

                    if (tempQueue.contains(newChunk.coord))
                    {
                        newChunk.render = true;
                    }
                    else // if (skipUnchanged)
                    {
                        if (newChunk.coord.chunkXPos != lastPlayerPos.posX || newChunk.coord.chunkZPos != lastPlayerPos.posZ)
                        {
                            if (newChunk.stub.equalTo(oldChunk.stub))
                            {
                                newChunk.render = false;
                                skipped++;
                            }
                        }
                    }
                }

                lastChunkStubs.keySet().removeAll(removed.keySet());
            }
        }

        if (chunks.size() - skipped > 1)
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("Kept %s of %s queued chunks, mapped %s, skipped %s, missing %s, lastChunkStubs=%s", tempQueue.size(), initialQueueSize, chunks.size() - skipped, skipped, missing, lastChunkStubs.size()));
            }
        }

        return new MapPlayerTask(world, dimension, underground, chunkY, chunks);
    }

    public static void clearCache()
    {
        synchronized (lastChunkStubs)
        {
            lastChunkStubs.clear();
        }
        synchronized (queuedChunks)
        {
            queuedChunks.clear();
        }
        lastPlayerPos = null;
    }

    public static boolean refresh()
    {
        if(forceRefresh)
        {
            return false;
        }
        forceRefresh = true;
        clearCache();
        return true;
    }

    @Override
    public void taskComplete()
    {
        if(forceRefresh)
        {
            forceRefresh = false;
            ChatLog.announceI18N("MapOverlay.force_refresh_result", chunkMdSet.size());
        }
        lastChunkStubs.putAll(chunkMdSet);
    }

    /**
     * ITaskManager for MapPlayerTasks
     *
     * @author mwoodman
     */
    public static class Manager implements ITaskManager
    {

        boolean enabled;

        @Override
        public Class<? extends BaseMapTask> getTaskClass()
        {
            return MapPlayerTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
            enabled = true;
            return enabled;
        }

        @Override
        public boolean isEnabled(Minecraft minecraft)
        {
            return enabled;
        }

        @Override
        public void disableTask(Minecraft minecraft)
        {
            enabled = false;
        }

        @Override
        public BaseMapTask getTask(Minecraft minecraft)
        {
            if (!enabled)
            {
                return null;
            }

            // Ensure player chunk is loaded
            if (minecraft.thePlayer.addedToChunk)
            {
                BaseMapTask baseMapTask = MapPlayerTask.create(minecraft.thePlayer);
                return baseMapTask;
            }
            else
            {
                return null;
            }
        }

        @Override
        public void taskAccepted(boolean accepted)
        {
            // nothing to do
        }

    }
}
