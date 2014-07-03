package net.techbrew.journeymap.task;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.ChunkMD;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Logger;

public class MapPlayerTask extends BaseMapTask
{

    private static final Logger logger = JourneyMap.getLogger();
    private static Comparator<ChunkCoordIntPair> chunkDistanceComparator = getDistanceComparator();
    private static HashSet<ChunkCoordIntPair> queuedChunks = new HashSet<ChunkCoordIntPair>();
    private static ChunkCoordinates lastPlayerPos;
    private static Boolean lastUnderground;
    private static DataCache dataCache = DataCache.instance();

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

        dataCache.invalidateChunkMD(chunkCoords);
    }

    private static Comparator<ChunkCoordIntPair> getDistanceComparator()
    {
        return new Comparator<ChunkCoordIntPair>()
        {
            Minecraft minecraft = FMLClientHandler.instance().getClient();

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
        final ChunkCoordinates playerPos = new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        final boolean underground = player.worldObj.provider.hasNoSky || (DataCache.getPlayer().underground && JourneyMap.getInstance().fullMapProperties.showCaves.get());

        if(underground && !FeatureManager.isAllowed(Feature.MapCaves))
        {
            return null;
        }

        final int dimension = DataCache.getPlayer().dimension;

        if (lastUnderground == null)
        {
            lastUnderground = underground;
        }
        if (lastPlayerPos == null)
        {
            lastPlayerPos = playerPos;
        }

//		boolean skipUnchanged = (lastUnderground==underground);
//		if(skipUnchanged && underground) {
//			skipUnchanged = (playerPos.posY==lastPlayerPos.posY);
//		}
//
//        skipUnchanged = false;

        int offset = JourneyMap.getInstance().coreProperties.chunkOffset.get();

        if (lastPlayerPos.equals(playerPos) || underground)
        {
            if (offset >= 2)
            {
                offset = offset / 2;
            }
        }

        lastPlayerPos = playerPos;
        lastUnderground = underground;

        final int side = offset + offset + 1;
        final ChunkMD.Set chunks = new ChunkMD.Set(side*3); // *3 to avoid map growth

        // Pull queued coords with as little delay as possible
        TreeSet<ChunkCoordIntPair> queuedCoords = new TreeSet<ChunkCoordIntPair>(chunkDistanceComparator);
        synchronized (queuedChunks)
        {
            queuedCoords.addAll(queuedChunks);
            queuedChunks.clear();
        }

        // Load em if you've got em, ensure they're refreshed
        ChunkMD chunkMd = null;
        int maxChunks = 512;

        // Get queued chunks
        for(ChunkCoordIntPair coord : queuedCoords)
        {
            if(chunks.size()>=maxChunks)
            {
                JourneyMap.getLogger().warning("Max chunks exceeded for MapPlayerTask: " + maxChunks);
                break;
            }

            chunkMd = dataCache.getChunkMD(coord, true);
            if (chunkMd != null)
            {
                chunkMd.render = true;
                chunkMd.setCurrent(false);
                chunks.add(chunkMd);
            }
        }

        // Now add peripheral chunks
        final World world = player.worldObj;
        final Integer chunkY = underground ? lastPlayerPos.posY : null;
        final ChunkCoordIntPair min = new ChunkCoordIntPair(lastPlayerPos.posX - offset, lastPlayerPos.posZ - offset);
        final ChunkCoordIntPair max = new ChunkCoordIntPair(lastPlayerPos.posX + offset, lastPlayerPos.posZ + offset);

        boolean forceCurrent;
        int renderCount = chunks.size();
        for (int x = min.chunkXPos; x <= max.chunkXPos; x++)
        {
            for (int z = min.chunkZPos; z <= max.chunkZPos; z++)
            {
                if(chunks.size()>=maxChunks)
                {
                    JourneyMap.getLogger().warning("Max chunks exceeded for MapPlayerTask: " + maxChunks);
                    break;
                }

                ChunkCoordIntPair coord = new ChunkCoordIntPair(x, z);
                if(queuedCoords.contains(coord))
                {
                    continue; // Was queued
                }

                // Don't force to be current unless player is in it
                forceCurrent = (player.chunkCoordX == x) && (player.chunkCoordZ == z);
                chunkMd = dataCache.getChunkMD(coord, forceCurrent);

                if (chunkMd != null)
                {
                    if(chunkMd.isCurrent())
                    {
                        chunkMd.render = true;
                        chunkMd.setCurrent(false);
                        chunks.add(chunkMd);
                        renderCount++;
                    }
                    else
                    {
                        chunkMd.render = false;
                        chunks.add(chunkMd);
                    }
                }
            }
        }

        System.out.println("Queued: " + queuedCoords.size() + ", Total Chunks to render: " + renderCount);

        return new MapPlayerTask(world, dimension, underground, chunkY, chunks);
    }

    @Override
    public void taskComplete()
    {

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
