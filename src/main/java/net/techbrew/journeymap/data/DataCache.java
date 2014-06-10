package net.techbrew.journeymap.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Singleton cache of data produced by IDataProviders.
 * <p/>
 * Uses on-demand-holder pattern for singleton management.
 *
 * @author mwoodman
 */
public class DataCache
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    final LoadingCache<Long, Map> all;
    final LoadingCache<Class, Map<String,EntityDTO>> animals;
    final LoadingCache<Class, Map<String,EntityDTO>> mobs;
    final LoadingCache<Class, Map<String,EntityDTO>> players;
    final LoadingCache<Class, Map<String,EntityDTO>> villagers;
    final LoadingCache<Class, Collection<Waypoint>> waypoints;
    final LoadingCache<Class, EntityDTO> player;
    final LoadingCache<Class, WorldData> world;
    final LoadingCache<Class, Map<String,Object>> messages;

    // Private constructor
    private DataCache()
    {
        AllData allData = new AllData();
        all = CacheBuilder.newBuilder().recordStats().maximumSize(1).expireAfterWrite(allData.getTTL(), TimeUnit.MILLISECONDS).build(allData);

        AnimalsData animalsData = new AnimalsData();
        animals = CacheBuilder.newBuilder().recordStats().expireAfterWrite(animalsData.getTTL(), TimeUnit.MILLISECONDS).build(animalsData);

        MobsData mobsData = new MobsData();
        mobs = CacheBuilder.newBuilder().recordStats().expireAfterWrite(mobsData.getTTL(), TimeUnit.MILLISECONDS).build(mobsData);

        PlayerData playerData = new PlayerData();
        player = CacheBuilder.newBuilder().recordStats().expireAfterWrite(playerData.getTTL(), TimeUnit.MILLISECONDS).build(playerData);

        PlayersData playersData = new PlayersData();
        players = CacheBuilder.newBuilder().recordStats().expireAfterWrite(playersData.getTTL(), TimeUnit.MILLISECONDS).build(playersData);

        VillagersData villagersData = new VillagersData();
        villagers = CacheBuilder.newBuilder().recordStats().expireAfterWrite(villagersData.getTTL(), TimeUnit.MILLISECONDS).build(villagersData);

        WaypointsData waypointsData = new WaypointsData();
        waypoints = CacheBuilder.newBuilder().recordStats().expireAfterWrite(waypointsData.getTTL(), TimeUnit.MILLISECONDS).build(waypointsData);
        
        WorldData worldData = new WorldData();
        world = CacheBuilder.newBuilder().recordStats().expireAfterWrite(worldData.getTTL(), TimeUnit.MILLISECONDS).build(worldData);

        MessagesData messagesData = new MessagesData();
        messages = CacheBuilder.newBuilder().recordStats().expireAfterWrite(messagesData.getTTL(), TimeUnit.MILLISECONDS).build(messagesData);
    }

    // On-demand-holder for instance
    private static class Holder
    {
        private static final DataCache INSTANCE = new DataCache();
    }

    // Get singleton instance.  Concurrency-safe.
    public static DataCache instance()
    {
        return Holder.INSTANCE;
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

    public static EntityDTO getPlayer()
    {
        return instance().getPlayer(false);
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
            if(WaypointsData.isReiMinimapEnabled() || WaypointsData.isVoxelMapEnabled())
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
            else if(WaypointsData.isManagerEnabled())
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
    }

    public String getDebugHtml() {
        StringBuilder sb = new StringBuilder();

        sb.append("<pre>");
//        sb.append(LogFormatter.LINEBREAK).append("<b>Block Metadata:</b> ").append(toString(BlockMD.getStats()));
//        sb.append(LogFormatter.LINEBREAK).append("<hr>");
        sb.append(LogFormatter.LINEBREAK).append("<b>All (Web only):</b> ").append(toString(all.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>       Animals:</b> ").append(toString(animals.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>      Messages:</b> ").append(toString(messages.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>          Mobs:</b> ").append(toString(mobs.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>        Player:</b> ").append(toString(player.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>       Players:</b> ").append(toString(players.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>     Villagers:</b> ").append(toString(villagers.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>     Waypoints:</b> ").append(toString(waypoints.stats()));
        sb.append(LogFormatter.LINEBREAK).append("<b>         World:</b> ").append(toString(world.stats()));
        sb.append("</pre>");

        return sb.toString();
    }

    private String toString(CacheStats cacheStats)
    {
        double avgLoadMillis = 0;
        if(cacheStats.totalLoadTime()>0 && cacheStats.loadSuccessCount()>0)
        {
            avgLoadMillis = TimeUnit.NANOSECONDS.toMillis(cacheStats.totalLoadTime())*1.0/cacheStats.loadSuccessCount();
        }
        
        return String.format("Hits: %6s, Misses: %6s, Loads: %6s, Errors: %6s, Avg Load Time: %1.2fms", cacheStats.hitCount(), cacheStats.missCount(), cacheStats.loadCount(), cacheStats.loadExceptionCount(), avgLoadMillis);
    }
}
