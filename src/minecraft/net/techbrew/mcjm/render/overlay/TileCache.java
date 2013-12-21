package net.techbrew.mcjm.render.overlay;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.techbrew.mcjm.JourneyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private void retainTiles() {
        if(!paused) {
            retained.putAll(cache.asMap());
            //logger.info("Will retain tiles: " + retained.size());
            paused = true;
        }
    }

    private void restoreExpired() {
        if(paused) {
            //logger.info("Restoring retained tiles: " + retained.size());
            cache.putAll(retained);
            retained.clear();
            paused = false;
        }
    }

    @Override
    public void onRemoval(RemovalNotification<Integer, Tile> notification) {
        Tile oldTile = notification.getValue();
        oldTile.clear();
        if(logger.isLoggable(Level.FINE)) logger.fine("Expired:" + notification.getValue() + " because: " + notification.getCause() + ". Size now: " + cache.size());
    }

}
