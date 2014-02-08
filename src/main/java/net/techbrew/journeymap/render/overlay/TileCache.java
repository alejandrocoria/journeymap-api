package net.techbrew.journeymap.render.overlay;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.techbrew.journeymap.JourneyMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a common cache for multiple GridRenderers to make use of.
 */
public class TileCache implements RemovalListener<Integer, Tile>{

    private static class Holder {
        private static final TileCache INSTANCE = new TileCache();
    }

    public static Cache<Integer, Tile> instance() {
        return Holder.INSTANCE.cache;
    }

    private final Logger logger = JourneyMap.getLogger();
    private final Cache<Integer, Tile> cache;
    private final Map<Integer,Tile> retained;

    private boolean paused;

    private TileCache() {
        this.cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .maximumSize(50)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .removalListener(this)
            .build();
        this.retained = new HashMap<Integer,Tile>();
    }

    public static void pause() {
        Holder.INSTANCE.retainTiles();
    }

    public static void resume() {
        Holder.INSTANCE.restoreExpired();
    }

    public static synchronized Tile getOrCreate(final File worldDir, final int tileX, final int tileZ, final int zoom, final int dimension) {
        final int hash = Tile.toHashCode(tileX, tileZ, zoom, dimension);
        TileCache tc = Holder.INSTANCE;
        synchronized (tc.cache) {
            Tile tile = tc.cache.getIfPresent(hash);
            if(tile==null) {
                tile = new Tile(worldDir, tileX, tileZ, zoom, dimension);
                tc.cache.put(hash, tile);
            }
            return tile;
        }
    }

    private void retainTiles() {
        if(!paused) {
            retained.putAll(cache.asMap());
            //logger.info("Will retain tiles: " + retained.size());
            paused = true;
        }
    }

    private void restoreExpired() {
        if(paused) {
            for(Integer key : retained.keySet()){
                if(cache.getIfPresent(key)==null){
                    //logger.info("Restoring retained tile: " + retained.get(key));
                    cache.put(key, retained.get(key));
                }
            }
            retained.clear();
            paused = false;
        }
    }

    @Override
    public void onRemoval(RemovalNotification<Integer, Tile> notification) {
        Tile oldTile = notification.getValue();
        if(!retained.containsValue(oldTile)) {
            if(logger.isLoggable(Level.FINER)) logger.finer("Expired:" + notification.getValue() + " because: " + notification.getCause() + ". Size now: " + cache.size());
            oldTile.clear();
        }
    }

}