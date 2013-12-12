package net.techbrew.mcjm.render.overlay;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.techbrew.mcjm.JourneyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<Tile> obsolete;

    private TileCache() {
        this.cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .maximumSize(50)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .removalListener(this)
            .build();

        this.obsolete = Collections.synchronizedList(new ArrayList<Tile>((int)cache.size()));
    }

    @Override
    public void onRemoval(RemovalNotification<Integer, Tile> notification) {
        Tile oldTile = notification.getValue();
        if(oldTile!=null) {
            oldTile.clear();
            if(logger.isLoggable(Level.FINER)) logger.finer("Expired:" + notification.getValue() + " because: " + notification.getCause() + ". Size now: " + cache.size());
        }
    }

}
