package journeymap.client.api.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import journeymap.client.JourneymapClient;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.PluginHelper;
import journeymap.client.log.StatTimer;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the journeymap-api IClientAPI.
 */
@ParametersAreNonnullByDefault
public enum ClientAPI implements IClientAPI
{
    INSTANCE;

    private final Logger LOGGER = Journeymap.getLogger();
    private final LoadingCache<String, ModObjects> modObjectsCache =
            CacheBuilder.newBuilder().build(
                    new CacheLoader<String, ModObjects>()
                    {
                        public ModObjects load(String modId)
                        {
                            return new ModObjects(modId);
                        }
                    });
    private StatTimer eventTimer = StatTimer.get("pluginClientEvent", 1, 500);

    @Override
    public boolean isActive()
    {
        return JourneymapClient.getInstance().isMapping();
    }

    @Override
    public void show(Displayable displayable)
    {
        try
        {
            if (playerAccepts(displayable))
            {
                ModObjects modObjects = modObjectsCache.getUnchecked(displayable.getModId());
                DisplayablePair displayablePair = modObjects.add(displayable);

                // Waypoints can be handled immediately
                if (DisplayType.of(displayable.getClass()) == DisplayType.Waypoint)
                {
                    ModWaypoint modWaypoint = (ModWaypoint) displayable;
                    Waypoint waypoint = new Waypoint(modWaypoint);
                    WaypointStore.instance().save(waypoint);
                    displayablePair.setInternal(waypoint);
                }
            }
        }
        catch (Throwable t)
        {
            logError("Error showing displayable: " + displayable, t);
        }
    }

    @Override
    public void remove(Displayable displayable)
    {
        remove(displayable.getModId(), DisplayType.of(displayable.getClass()), displayable.getDisplayId());
    }

    @Override
    public void remove(String modId, DisplayType displayType, String displayId)
    {
        try
        {
            if (playerAccepts(modId, displayType))
            {
                modObjectsCache.getUnchecked(modId).remove(displayType, displayId);
            }
        }
        catch (Throwable t)
        {
            logError(String.format("Error removing displayable: %s %s %s", modId, displayType, displayId), t);
        }
    }

    @Override
    public void removeAll(String modId, DisplayType displayType)
    {
        try
        {
            if (playerAccepts(modId, displayType))
            {
                modObjectsCache.getUnchecked(modId).removeAll(displayType);
            }
        }
        catch (Throwable t)
        {
            logError(String.format("Error removing all displayables: %s %s", modId, displayType), t);
        }
    }

    @Override
    public void removeAll(String modId)
    {
        modObjectsCache.invalidate(modId);
    }

    @Override
    public boolean exists(String modId, DisplayType displayType, String displayId)
    {
        return modObjectsCache.getUnchecked(modId).exists(displayType, displayId);
    }

    @Override
    public boolean isVisible(String modId, DisplayType displayType, String displayId)
    {
        DisplayablePair pair = modObjectsCache.getUnchecked(modId).get(displayType, displayId);

        // TODO:  This only returns whether an internal object has been created, not whether it's actually visible
        return pair != null && pair.getInternal() != null;
    }

    @Override
    public List<String> getShownIds(String modId, DisplayType displayType)
    {
        try
        {
            List<DisplayablePair> pairs = new ArrayList<DisplayablePair>(modObjectsCache.getUnchecked(modId).getDisplayablePairs(displayType));
            ArrayList<String> ids = new ArrayList<String>(pairs.size());
            for (DisplayablePair pair : pairs)
            {
                ids.add(pair.getDisplayable().getDisplayId());
            }
            return ids;
        }
        catch (Throwable t)
        {
            logError(String.format("Error in getShownIds(): %s %s", modId, displayType), t);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean playerAccepts(String modId, DisplayType displayType)
    {
        // TODO
        return true;
    }

    public boolean playerAccepts(Displayable displayable)
    {
        return playerAccepts(displayable.getModId(), DisplayType.of(displayable.getClass()));
    }

    /**
     * Notify plugins that the map displays have started.
     * @param dimension
     */
    public void notifyDisplayStarted(int dimension)
    {
        notifyPlugins(new ClientEvent(ClientEvent.Type.DISPLAY_STARTED, dimension));
    }

    /**
     * Notify plugins that a death waypoint has been created.
     *
     * @param point death point
     * @return true if the death waypoint should be used.
     */
    public boolean notifyDeathWaypoint(final BlockPos point, int dimension)
    {
        ClientEvent event = new ClientEvent(ClientEvent.Type.DEATH_WAYPOINT, dimension, point);
        notifyPlugins(event);
        return !event.isCancelled();
    }

    /**
     * Notify plugins of client event.
     *
     * @param clientEvent event
     */
    public void notifyPlugins(final ClientEvent clientEvent)
    {
        try
        {
            List<IClientPlugin> plugins = PluginHelper.INSTANCE.getPlugins();
            if (plugins != null && !plugins.isEmpty())
            {
                boolean cancelled = clientEvent.isCancelled();
                // TODO: Add clientEvent.isCancellable()
                boolean cancellable = (clientEvent.type == ClientEvent.Type.DEATH_WAYPOINT);

                for (IClientPlugin plugin : plugins)
                {
                    eventTimer.start();
                    try
                    {
                        plugin.onEvent(clientEvent);
                        if (cancellable && !cancelled && clientEvent.isCancelled())
                        {
                            cancelled = true;
                            LOGGER.info(String.format("Plugin %s cancelled event: %s", plugin.getClass(), clientEvent.type));
                        }
                    }
                    catch (Throwable t)
                    {
                        LOGGER.error(String.format("Plugin %s errored during event: %s", plugin.getClass(), clientEvent.type), t);
                    }
                    finally
                    {
                        eventTimer.stop();
                    }
                }
            }
        }
        catch (Throwable t)
        {
            logError("Error in notifyPlugins(): " + clientEvent, t);
        }
    }

    /**
     * Log a message
     *
     * @param message
     */
    private void log(String message)
    {
        LOGGER.info(String.format("[%s] %s", getClass().getSimpleName(), message));
    }

    private void logError(String message, Throwable t)
    {
        LOGGER.info(String.format("[%s] %s", getClass().getSimpleName(), message), t);
    }
}
