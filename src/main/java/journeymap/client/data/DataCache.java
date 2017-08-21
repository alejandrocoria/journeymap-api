/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import journeymap.client.io.nbt.ChunkLoader;
import journeymap.client.model.*;
import journeymap.client.render.draw.DrawEntityStep;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The mother of all cache managers.
 *
 * @author techbrew
 */
public enum DataCache
{
    /**
     * Instance data cache.
     */
    INSTANCE;

    /**
     * The All.
     */
    final LoadingCache<Long, Map> all;
    /**
     * The Animals.
     */
    final LoadingCache<Class, Map<String, EntityDTO>> animals;
    /**
     * The Mobs.
     */
    final LoadingCache<Class, Map<String, EntityDTO>> mobs;
    /**
     * The Players.
     */
    final LoadingCache<Class, Map<String, EntityDTO>> players;
    /**
     * The Villagers.
     */
    final LoadingCache<Class, Map<String, EntityDTO>> villagers;
    /**
     * The Waypoints.
     */
    final LoadingCache<Class, Collection<Waypoint>> waypoints;
    /**
     * The Player.
     */
    final LoadingCache<Class, EntityDTO> player;
    /**
     * The World.
     */
    final LoadingCache<Class, WorldData> world;
    /**
     * The Region image sets.
     */
    final LoadingCache<RegionImageSet.Key, RegionImageSet> regionImageSets;
    /**
     * The Messages.
     */
    final LoadingCache<Class, Map<String, Object>> messages;
    /**
     * The Entity draw steps.
     */
    final LoadingCache<EntityLivingBase, DrawEntityStep> entityDrawSteps;
    /**
     * The Waypoint draw steps.
     */
    final LoadingCache<Waypoint, DrawWayPointStep> waypointDrawSteps;
    /**
     * The Entity dt os.
     */
    final LoadingCache<EntityLivingBase, EntityDTO> entityDTOs;
    /**
     * The Region coords.
     */
    final Cache<String, RegionCoord> regionCoords;
    /**
     * The Map types.
     */
    final Cache<String, MapType> mapTypes;
    /**
     * The Block metadata.
     */
    final LoadingCache<IBlockState, BlockMD> blockMetadata;
    /**
     * The Chunk metadata.
     */
    final Cache<ChunkPos, ChunkMD> chunkMetadata;

    /**
     * The Managed caches.
     */
    final HashMap<Cache, String> managedCaches = new HashMap<Cache, String>();
    //final WeakHashMap<Cache, String> privateCaches = new WeakHashMap<Cache, String>();
    private final int chunkCacheExpireSeconds = 30;
    private final int defaultConcurrencyLevel = 2;


    // Private constructor
    private DataCache()
    {
        AllData allData = new AllData();
        all = getCacheBuilder().maximumSize(1).expireAfterWrite(allData.getTTL(), TimeUnit.MILLISECONDS).build(allData);
        managedCaches.put(all, "AllData (web)");

        AnimalsData animalsData = new AnimalsData();
        animals = getCacheBuilder().expireAfterWrite(animalsData.getTTL(), TimeUnit.MILLISECONDS).build(animalsData);
        managedCaches.put(animals, "Animals");

        MobsData mobsData = new MobsData();
        mobs = getCacheBuilder().expireAfterWrite(mobsData.getTTL(), TimeUnit.MILLISECONDS).build(mobsData);
        managedCaches.put(mobs, "Mobs");

        PlayerData playerData = new PlayerData();
        player = getCacheBuilder().expireAfterWrite(playerData.getTTL(), TimeUnit.MILLISECONDS).build(playerData);
        managedCaches.put(player, "Player");

        PlayersData playersData = new PlayersData();
        players = getCacheBuilder().expireAfterWrite(playersData.getTTL(), TimeUnit.MILLISECONDS).build(playersData);
        managedCaches.put(players, "Players");

        VillagersData villagersData = new VillagersData();
        villagers = getCacheBuilder().expireAfterWrite(villagersData.getTTL(), TimeUnit.MILLISECONDS).build(villagersData);
        managedCaches.put(villagers, "Villagers");

        WaypointsData waypointsData = new WaypointsData();
        waypoints = getCacheBuilder().expireAfterWrite(waypointsData.getTTL(), TimeUnit.MILLISECONDS).build(waypointsData);
        managedCaches.put(waypoints, "Waypoints");

        WorldData worldData = new WorldData();
        world = getCacheBuilder().expireAfterWrite(worldData.getTTL(), TimeUnit.MILLISECONDS).build(worldData);
        managedCaches.put(world, "World");

        MessagesData messagesData = new MessagesData();
        messages = getCacheBuilder().expireAfterWrite(messagesData.getTTL(), TimeUnit.MILLISECONDS).build(messagesData);
        managedCaches.put(messages, "Messages (web)");

        entityDrawSteps = getCacheBuilder().weakKeys().build(new DrawEntityStep.SimpleCacheLoader());
        managedCaches.put(entityDrawSteps, "DrawEntityStep");

        waypointDrawSteps = getCacheBuilder().weakKeys().build(new DrawWayPointStep.SimpleCacheLoader());
        managedCaches.put(waypointDrawSteps, "DrawWaypointStep");

        entityDTOs = getCacheBuilder().weakKeys().build(new EntityDTO.SimpleCacheLoader());
        managedCaches.put(entityDTOs, "EntityDTO");

        regionImageSets = RegionImageCache.INSTANCE.initRegionImageSetsCache(getCacheBuilder());
        managedCaches.put(regionImageSets, "RegionImageSet");

        blockMetadata = getCacheBuilder().weakKeys().build(new BlockMD.CacheLoader());
        managedCaches.put(blockMetadata, "BlockMD");

        chunkMetadata = getCacheBuilder().expireAfterAccess(chunkCacheExpireSeconds, TimeUnit.SECONDS).build();
        managedCaches.put(chunkMetadata, "ChunkMD");

        regionCoords = getCacheBuilder().expireAfterAccess(chunkCacheExpireSeconds, TimeUnit.SECONDS).build();
        managedCaches.put(regionCoords, "RegionCoord");

        mapTypes = getCacheBuilder().build();
        managedCaches.put(mapTypes, "MapType");
    }

    /**
     * Gets player.
     *
     * @return the player
     */
    public static EntityDTO getPlayer() {
        return INSTANCE.getPlayer(false);
    }

    /**
     * Get a CacheBuilder with common configuration already set.
     *
     * @return
     */
    private CacheBuilder<Object, Object> getCacheBuilder()
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        builder.concurrencyLevel(defaultConcurrencyLevel);
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get())
        {
            builder.recordStats();
        }
        return builder;
    }

    /**
     * Gets all.
     *
     * @param since the since
     * @return the all
     */
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
                Journeymap.getLogger().error("ExecutionException in getAll: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    /**
     * Gets animals.
     *
     * @param forceRefresh the force refresh
     * @return the animals
     */
    public Map<String, EntityDTO> getAnimals(boolean forceRefresh) {
        synchronized (animals) {
            try {
                if (forceRefresh) {
                    animals.invalidateAll();
                }
                return animals.get(AnimalsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getAnimals: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    /**
     * Gets mobs.
     *
     * @param forceRefresh the force refresh
     * @return the mobs
     */
    public Map<String, EntityDTO> getMobs(boolean forceRefresh) {
        synchronized (mobs) {
            try {
                if (forceRefresh) {
                    mobs.invalidateAll();
                }
                return mobs.get(MobsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getMobs: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    /**
     * Gets players.
     *
     * @param forceRefresh the force refresh
     * @return the players
     */
    public Map<String, EntityDTO> getPlayers(boolean forceRefresh) {
        synchronized (players) {
            try {
                if (forceRefresh) {
                    players.invalidateAll();
                }
                return players.get(PlayersData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getPlayers: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    /**
     * Gets player.
     *
     * @param forceRefresh the force refresh
     * @return the player
     */
    public EntityDTO getPlayer(boolean forceRefresh) {
        synchronized (player) {
            try {
                if (forceRefresh) {
                    player.invalidateAll();
                }
                return player.get(PlayerData.class);
            } catch (Exception e) {
                Journeymap.getLogger().error("ExecutionException in getPlayer: " + LogFormatter.toString(e));
                return null;
            }
        }
    }

    /**
     * Gets villagers.
     *
     * @param forceRefresh the force refresh
     * @return the villagers
     */
    public Map<String, EntityDTO> getVillagers(boolean forceRefresh) {
        synchronized (villagers) {
            try {
                if (forceRefresh) {
                    villagers.invalidateAll();
                }
                return villagers.get(VillagersData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getVillagers: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    /**
     * Gets map type.
     *
     * @param name      the name
     * @param vSlice    the v slice
     * @param dimension the dimension
     * @return the map type
     */
    public MapType getMapType(MapType.Name name, Integer vSlice, int dimension) {
        // Guarantee surface types don't use a slice
        vSlice = (name != MapType.Name.underground) ? null : vSlice;

        MapType mapType = mapTypes.getIfPresent(MapType.toCacheKey(name, vSlice, dimension));
        if (mapType == null) {
            mapType = new MapType(name, vSlice, dimension);
            mapTypes.put(mapType.toCacheKey(), mapType);
        }
        return mapType;
    }

    /**
     * Gets waypoints.
     *
     * @param forceRefresh the force refresh
     * @return the waypoints
     */
    public Collection<Waypoint> getWaypoints(boolean forceRefresh) {
        synchronized (waypoints) {
            if (WaypointsData.isManagerEnabled()) {
                // The store is the cache
                return WaypointStore.INSTANCE.getAll();
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }

    /**
     * Gets messages.
     *
     * @param forceRefresh the force refresh
     * @return the messages
     */
    public Map<String, Object> getMessages(boolean forceRefresh) {
        synchronized (messages) {
            try {
                if (forceRefresh) {
                    messages.invalidateAll();
                }
                return messages.get(MessagesData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getMessages: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    /**
     * Gets world.
     *
     * @param forceRefresh the force refresh
     * @return the world
     */
    public WorldData getWorld(boolean forceRefresh) {
        synchronized (world) {
            try {
                if (forceRefresh) {
                    world.invalidateAll();
                }
                return world.get(WorldData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getWorld: " + LogFormatter.toString(e));
                return new WorldData();
            }
        }
    }

    /**
     * Purge radar caches to ensure changed options are immediately seen.
     */
    public void resetRadarCaches()
    {
        animals.invalidateAll();
        mobs.invalidateAll();
        players.invalidateAll();
        villagers.invalidateAll();
        entityDrawSteps.invalidateAll();
        entityDTOs.invalidateAll();
    }

    /**
     * Gets draw entity step.
     *
     * @param entity the entity
     * @return the draw entity step
     */
    public DrawEntityStep getDrawEntityStep(EntityLivingBase entity) {
        synchronized (entityDrawSteps) {
            return entityDrawSteps.getUnchecked(entity);
        }
    }

    /**
     * Gets entity dto.
     *
     * @param entity the entity
     * @return the entity dto
     */
    public EntityDTO getEntityDTO(EntityLivingBase entity) {
        synchronized (entityDTOs) {
            return entityDTOs.getUnchecked(entity);
        }
    }

    /**
     * Gets draw way point step.
     *
     * @param waypoint the waypoint
     * @return the draw way point step
     */
    public DrawWayPointStep getDrawWayPointStep(Waypoint waypoint) {
        synchronized (waypointDrawSteps) {
            return waypointDrawSteps.getUnchecked(waypoint);
        }
    }

    /**
     * Whether there's a BlockMD for the state.
     */
    public boolean hasBlockMD(final IBlockState aBlockState) {
        try {
            return blockMetadata.getIfPresent(aBlockState) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets a BlockMD for the blockstate, creating if needed.
     *
     * @param blockState the block state
     * @return the block md
     */
    public BlockMD getBlockMD(final IBlockState blockState) {
        try {
            return blockMetadata.get(blockState);
        } catch (Exception e) {
            Journeymap.getLogger().error("Error in getBlockMD() for " + blockState + ": " + e);
            return BlockMD.AIRBLOCK;
        }
    }

    /**
     * An expensive operation. Don't use it frequently.
     *
     * @return
     */
    public int getBlockMDCount() {
        return blockMetadata.asMap().size();
    }

    /**
     * Use BlockMD.getAll() when in doubt.
     *
     * @return
     */
    public Set<BlockMD> getLoadedBlockMDs() {
        return Sets.newHashSet(blockMetadata.asMap().values());
    }

    /**
     * Reset block metadata.
     */
    public void resetBlockMetadata() {
        blockMetadata.invalidateAll();
    }

    /**
     * Gets chunk md.
     *
     * @param blockPos the block pos
     * @return the chunk md
     */
    public ChunkMD getChunkMD(BlockPos blockPos)
    {
        return getChunkMD(new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4));
    }

    /**
     * Gets chunk md.
     *
     * @param coord the coord
     * @return the chunk md
     */
    public ChunkMD getChunkMD(ChunkPos coord) {
        synchronized (chunkMetadata) {
            ChunkMD chunkMD = null;

            try {
                chunkMD = chunkMetadata.getIfPresent(coord);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    return chunkMD;
                }

                chunkMD = ChunkLoader.getChunkMdFromMemory(FMLClientHandler.instance().getClient().world, coord.x, coord.z);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    chunkMetadata.put(coord, chunkMD);
                    return chunkMD;
                }

                if (chunkMD != null) {
                    chunkMetadata.invalidate(coord);
                }
                return null;
            } catch (Throwable e) {
                Journeymap.getLogger().warn("Unexpected error getting ChunkMD from cache: " + e);
                return null;
            }
        }
    }

    /**
     * Add chunk md.
     *
     * @param chunkMD the chunk md
     */
    public void addChunkMD(ChunkMD chunkMD) {
        synchronized (chunkMetadata) {
            chunkMetadata.put(chunkMD.getCoord(), chunkMD);
        }
    }

//    public Set<ChunkPos> getCachedChunkCoordinates()
//    {
//        //synchronized (chunkMetadata)
//        {
//            return chunkMetadata.asMap().keySet();
//        }
//    }
//
//    public void invalidateChunkMD(ChunkPos coord)
//    {
//        //synchronized (chunkMetadata)
//        {
//            chunkMetadata.invalidate(coord);
//        }
//    }

    /**
     * Invalidate chunk md cache.
     */
    public void invalidateChunkMDCache()
    {
        chunkMetadata.invalidateAll();
    }

    /**
     * Stop chunk md retention.
     */
    public void stopChunkMDRetention()
    {
        //synchronized (chunkMetadata)
        {
            for (ChunkMD chunkMD : chunkMetadata.asMap().values()) {
                if (chunkMD != null) {
                    chunkMD.stopChunkRetention();
                }
            }
        }
    }

//    public void addChunkMDListener(RemovalListener<ChunkPos, ChunkMD> listener)
//    {
//        synchronized (chunkMetadataRemovalListener)
//        {
//            chunkMetadataRemovalListener.addDelegateListener(listener);
//        }
//    }

//    public void resetBlockMetadata()
//    {
//        BlockMD.reset();
//    }

    /**
     * Gets region image sets.
     *
     * @return the region image sets
     */
    public LoadingCache<RegionImageSet.Key, RegionImageSet> getRegionImageSets()
    {
        return regionImageSets;
    }

    /**
     * Gets region coords.
     *
     * @return the region coords
     */
    public Cache<String, RegionCoord> getRegionCoords()
    {
        return regionCoords;
    }

    /**
     * Empties the cache
     */
    public void purge()
    {
        // Flush images, do syncronously to ensure it's done before cache invalidates
        RegionImageCache.INSTANCE.flushToDisk(false);

        resetBlockMetadata();

        synchronized (managedCaches)
        {
            for (Cache cache : managedCaches.keySet())
            {
                try
                {
                    cache.invalidateAll();
                }
                catch (Exception e)
                {
                    Journeymap.getLogger().warn("Couldn't purge managed cache: " + cache);
                }
            }
        }

//        synchronized (privateCaches)
//        {
//            for (Cache cache : privateCaches.keySet())
//            {
//                try
//                {
//                    cache.invalidateAll();
//                }
//                catch (Exception e)
//                {
//                    Journeymap.getLogger().warn("Couldn't purge private cache: " + cache);
//                }
//            }
//
//            privateCaches.clear();
//        }
    }

    /**
     * Gets debug html.
     *
     * @return the debug html
     */
    public String getDebugHtml()
    {
        StringBuffer sb = new StringBuffer();
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get()) {
            appendDebugHtml(sb, "Managed Caches", managedCaches);
            //appendDebugHtml(sb, "Private Caches", privateCaches);
        } else {
            sb.append("<b>Cache stat recording disabled.  Set config/journeymap.core.config 'recordCacheStats' to 1.</b>");
        }
        return sb.toString();
    }

    private void appendDebugHtml(StringBuffer sb, String name, Map<Cache, String> cacheMap)
    {
        ArrayList<Map.Entry<Cache, String>> list = new ArrayList<Map.Entry<Cache, String>>(cacheMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Cache, String>>()
        {
            @Override
            public int compare(Map.Entry<Cache, String> o1, Map.Entry<Cache, String> o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        sb.append("<b>").append(name).append(":</b>");
        sb.append("<pre>");
        for (Map.Entry<Cache, String> entry : list)
        {
            sb.append(toString(entry.getValue(), entry.getKey()));
        }
        sb.append("</pre>");
    }

    private String toString(String label, Cache cache)
    {
        double avgLoadMillis = 0;
        CacheStats cacheStats = cache.stats();
        if (cacheStats.totalLoadTime() > 0 && cacheStats.loadSuccessCount() > 0)
        {
            avgLoadMillis = TimeUnit.NANOSECONDS.toMillis(cacheStats.totalLoadTime()) * 1.0 / cacheStats.loadSuccessCount();
        }

        return String.format("%s<b>%20s:</b> Size: %9s, Hits: %9s, Misses: %9s, Loads: %9s, Errors: %9s, Avg Load Time: %1.2fms", LogFormatter.LINEBREAK, label, cache.size(), cacheStats.hitCount(), cacheStats.missCount(), cacheStats.loadCount(), cacheStats.loadExceptionCount(), avgLoadMillis);
    }
}
