package net.techbrew.journeymap.data;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.nbt.ChunkLoader;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.*;
import net.techbrew.journeymap.render.draw.DrawEntityStep;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The mother of all cache managers.
 *
 * @author mwoodman
 */
public class DataCache
{
    final LoadingCache<Long, Map> all;
    final LoadingCache<Class, Map<String, EntityDTO>> animals;
    final LoadingCache<Class, Map<String, EntityDTO>> mobs;
    final LoadingCache<Class, Map<String, EntityDTO>> players;
    final LoadingCache<Class, Map<String, EntityDTO>> villagers;
    final LoadingCache<Class, Collection<Waypoint>> waypoints;
    final LoadingCache<Class, EntityDTO> player;
    final LoadingCache<Class, WorldData> world;
    final LoadingCache<Class, Map<String, Object>> messages;
    final LoadingCache<Entity, DrawEntityStep> entityDrawSteps;
    final LoadingCache<EntityLivingBase, EntityDTO> entityDTOs;
    final LoadingCache<ChunkCoordIntPair, Optional<ChunkMD>> chunkMetadata;
    final LoadingCache<String, BlockMD> blockMetadata;
    final BlockMDCache blockMetadataLoader;

    // Private constructor
    private DataCache()
    {
        AllData allData = new AllData();
        all = getCacheBuilder().maximumSize(1).expireAfterWrite(allData.getTTL(), TimeUnit.MILLISECONDS).build(allData);

        AnimalsData animalsData = new AnimalsData();
        animals = getCacheBuilder().expireAfterWrite(animalsData.getTTL(), TimeUnit.MILLISECONDS).build(animalsData);

        MobsData mobsData = new MobsData();
        mobs = getCacheBuilder().expireAfterWrite(mobsData.getTTL(), TimeUnit.MILLISECONDS).build(mobsData);

        PlayerData playerData = new PlayerData();
        player = CacheBuilder.newBuilder().expireAfterWrite(playerData.getTTL(), TimeUnit.MILLISECONDS).build(playerData);

        PlayersData playersData = new PlayersData();
        players = getCacheBuilder().expireAfterWrite(playersData.getTTL(), TimeUnit.MILLISECONDS).build(playersData);

        VillagersData villagersData = new VillagersData();
        villagers = getCacheBuilder().expireAfterWrite(villagersData.getTTL(), TimeUnit.MILLISECONDS).build(villagersData);

        WaypointsData waypointsData = new WaypointsData();
        waypoints = getCacheBuilder().expireAfterWrite(waypointsData.getTTL(), TimeUnit.MILLISECONDS).build(waypointsData);

        WorldData worldData = new WorldData();
        world = getCacheBuilder().expireAfterWrite(worldData.getTTL(), TimeUnit.MILLISECONDS).build(worldData);

        MessagesData messagesData = new MessagesData();
        messages = getCacheBuilder().expireAfterWrite(messagesData.getTTL(), TimeUnit.MILLISECONDS).build(messagesData);

        entityDrawSteps = getCacheBuilder().weakKeys().build(new DrawEntityStep.SimpleCacheLoader());

        entityDTOs = getCacheBuilder().weakKeys().build(new EntityDTO.SimpleCacheLoader());

        long colorTimeout = JourneyMap.getInstance().coreProperties.chunkPoll.get() * 5;
        //colors = getCacheBuilder().expireAfterAccess(colorTimeout, TimeUnit.MILLISECONDS).build(new RGB.SimpleCacheLoader());

        long chunkTimeout = JourneyMap.getInstance().coreProperties.chunkPoll.get() * 3;
        chunkMetadata = getCacheBuilder().expireAfterWrite(chunkTimeout, TimeUnit.MILLISECONDS).build(new ChunkMD.SimpleCacheLoader());

        blockMetadataLoader = new BlockMDCache();
        blockMetadata = getCacheBuilder().initialCapacity(256).build(blockMetadataLoader);
    }

    // Get singleton instance.  Concurrency-safe.
    public static DataCache instance()
    {
        return Holder.INSTANCE;
    }

    public static EntityDTO getPlayer()
    {
        return instance().getPlayer(false);
    }

    /**
     * Get a CacheBuilder with common configuration already set.
     *
     * @return
     */
    private CacheBuilder<Object, Object> getCacheBuilder()
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        if (JourneyMap.getInstance().coreProperties.recordCacheStats.get())
        {
            builder.recordStats();
        }
        return builder;
    }

    public Map getAll(long since)
    {
        synchronized (all)
        {
            try
            {
                return all.get(since);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getAll: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getAnimals(boolean forceRefresh)
    {
        synchronized (animals)
        {
            try
            {
                if (forceRefresh)
                {
                    animals.invalidateAll();
                }
                return animals.get(AnimalsData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getAnimals: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getMobs(boolean forceRefresh)
    {
        synchronized (mobs)
        {
            try
            {
                if (forceRefresh)
                {
                    mobs.invalidateAll();
                }
                return mobs.get(MobsData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getMobs: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getPlayers(boolean forceRefresh)
    {
        synchronized (players)
        {
            try
            {
                if (forceRefresh)
                {
                    players.invalidateAll();
                }
                return players.get(PlayersData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getPlayers: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public EntityDTO getPlayer(boolean forceRefresh)
    {
        synchronized (player)
        {
            try
            {
                if (forceRefresh)
                {
                    player.invalidateAll();
                }
                return player.get(PlayersData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getPlayer: " + LogFormatter.toString(e));
                return null;
            }
        }
    }

    public Map<String, EntityDTO> getVillagers(boolean forceRefresh)
    {
        synchronized (villagers)
        {
            try
            {
                if (forceRefresh)
                {
                    villagers.invalidateAll();
                }
                return villagers.get(VillagersData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getVillagers: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public Collection<Waypoint> getWaypoints(boolean forceRefresh)
    {
        synchronized (waypoints)
        {
            if (WaypointsData.isReiMinimapEnabled() || WaypointsData.isVoxelMapEnabled())
            {
                // Caching needed
                try
                {
                    if (forceRefresh)
                    {
                        waypoints.invalidateAll();
                    }

                    return waypoints.get(WaypointsData.class);
                }
                catch (ExecutionException e)
                {
                    JourneyMap.getLogger().severe("ExecutionException in getVillagers: " + LogFormatter.toString(e));
                    return Collections.EMPTY_LIST;
                }
            }
            else if (WaypointsData.isManagerEnabled())
            {
                // The store is the cache
                return WaypointStore.instance().getAll();
            }
            else
            {
                return Collections.EMPTY_LIST;
            }
        }
    }

    public Map<String, Object> getMessages(boolean forceRefresh)
    {
        synchronized (messages)
        {
            try
            {
                if (forceRefresh)
                {
                    messages.invalidateAll();
                }
                return messages.get(MessagesData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getMessages: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public WorldData getWorld(boolean forceRefresh)
    {
        synchronized (world)
        {
            try
            {
                if (forceRefresh)
                {
                    world.invalidateAll();
                }
                return world.get(WorldData.class);
            }
            catch (ExecutionException e)
            {
                JourneyMap.getLogger().severe("ExecutionException in getWorld: " + LogFormatter.toString(e));
                return new WorldData();
            }
        }
    }

    public DrawEntityStep getDrawEntityStep(Entity entity)
    {
        synchronized (entityDrawSteps)
        {
            return entityDrawSteps.getUnchecked(entity);
        }
    }

    public EntityDTO getEntityDTO(EntityLivingBase entity)
    {
        synchronized (entityDTOs)
        {
            return entityDTOs.getUnchecked(entity);
        }
    }

//    public RGB getColor(Color color)
//    {
//        return getColor(color.getRGB());
//    }
//
//    public RGB getColor(int rgbInt)
//    {
//        synchronized (colors)
//        {
//            return colors.getUnchecked(rgbInt);
//        }
//    }

    public ChunkMD getChunkMD(ChunkCoordIntPair coord)
    {
        return getChunkMD(coord, false);
    }

    public ChunkMD getChunkMD(ChunkCoordIntPair coord, boolean ensureCurrent)
    {
        synchronized (chunkMetadata)
        {
            ChunkMD chunkMD = null;

            try
            {
                Optional<ChunkMD> optional = chunkMetadata.get(coord);
                if(optional.isPresent())
                {
                    chunkMD = optional.get();
                }
                else
                {
                    chunkMetadata.invalidate(coord); // removes empty Optional
                }
            }
            catch(Throwable e)
            {
                JourneyMap.getLogger().warning("Unexpected error getting ChunkMD from cache: " + e);
            }

            if (ensureCurrent && chunkMD != null && !chunkMD.isCurrent())
            {
                chunkMD = ChunkLoader.refreshChunkMdFromMemory(chunkMD);
            }

            return chunkMD;
        }
    }

    public Set<ChunkCoordIntPair> getCachedChunkCoordinates()
    {
        synchronized (chunkMetadata)
        {
            return chunkMetadata.asMap().keySet();
        }
    }

    public void invalidateChunkMD(ChunkCoordIntPair coord)
    {
        synchronized (chunkMetadata)
        {
            chunkMetadata.invalidate(coord);
        }
    }

    public BlockMDCache getBlockMetadata()
    {
        return blockMetadataLoader;
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     */
    public BlockMD getBlockMD(ChunkMD chunkMd, int x, int y, int z)
    {
        return blockMetadataLoader.getBlockMD(blockMetadata, chunkMd, x, y, z);
    }

    /**
     * Empties the cache
     */
    public void purge()
    {
        synchronized (all)
        {
            all.invalidateAll();
        }

        synchronized (animals)
        {
            animals.invalidateAll();
        }

        synchronized (mobs)
        {
            mobs.invalidateAll();
        }

        synchronized (players)
        {
            players.invalidateAll();
        }

        synchronized (player)
        {
            player.invalidateAll();
        }

        synchronized (villagers)
        {
            villagers.invalidateAll();
        }

        synchronized (waypoints)
        {
            waypoints.invalidateAll();
        }

        synchronized (world)
        {
            world.invalidateAll();
        }

        synchronized (messages)
        {
            messages.invalidateAll();
        }

        synchronized (entityDrawSteps)
        {
            entityDrawSteps.invalidateAll();
        }

        synchronized (entityDTOs)
        {
            entityDTOs.invalidateAll();
        }

//        synchronized (colors)
//        {
//            colors.invalidateAll();
//        }

        synchronized (chunkMetadata)
        {
            chunkMetadata.invalidateAll();
        }

        synchronized (blockMetadata)
        {
            blockMetadataLoader.initialize();
            blockMetadata.invalidateAll();
        }
    }

    public String getDebugHtml()
    {
        StringBuilder sb = new StringBuilder();

        if (JourneyMap.getInstance().coreProperties.recordCacheStats.get())
        {
            sb.append("<pre>");
            sb.append(toString("All (Web only)", all));
            //sb.append(toString("Colors", colors));
            sb.append(toString("Blocks", blockMetadata));
            sb.append(toString("Chunks", chunkMetadata));
            sb.append(toString("DrawEntitySteps", entityDrawSteps));
            sb.append(toString("EntityDTOs", entityDTOs));
            sb.append(toString("Animals", animals));
            sb.append(toString("Messages", messages));
            sb.append(toString("Mobs", mobs));
            sb.append(toString("Players", players));
            sb.append(toString("Villagers", villagers));
            sb.append(toString("Waypoints", waypoints));
            sb.append(toString("World", world));
            sb.append("</pre>");
        }
        else
        {
            sb.append("<b>Cache stat recording disabled.  Set config/journeymap.core.config 'recordCacheStats' to 1.</b>");
        }
        return sb.toString();
    }

    private String toString(String label, LoadingCache cache)
    {
        double avgLoadMillis = 0;
        CacheStats cacheStats = cache.stats();
        if (cacheStats.totalLoadTime() > 0 && cacheStats.loadSuccessCount() > 0)
        {
            avgLoadMillis = TimeUnit.NANOSECONDS.toMillis(cacheStats.totalLoadTime()) * 1.0 / cacheStats.loadSuccessCount();
        }

        return String.format("%s<b>%20s:</b> Size: %9s, Hits: %9s, Misses: %9s, Loads: %9s, Errors: %9s, Avg Load Time: %1.2fms", LogFormatter.LINEBREAK, label, cache.size(), cacheStats.hitCount(), cacheStats.missCount(), cacheStats.loadCount(), cacheStats.loadExceptionCount(), avgLoadMillis);
    }

    // On-demand-holder for instance
    private static class Holder
    {
        private static final DataCache INSTANCE = new DataCache();
    }
}
