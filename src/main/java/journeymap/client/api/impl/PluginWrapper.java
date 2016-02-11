package journeymap.client.api.impl;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.*;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.log.StatTimer;
import journeymap.client.model.Waypoint;
import journeymap.client.render.draw.DrawPolygonStep;
import journeymap.client.render.draw.OverlayDrawStep;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * Aggregates objects passed to the ClientAPI for a specific mod.
 */
@ParametersAreNonnullByDefault
class PluginWrapper
{
    private final IClientPlugin plugin;
    private final String modId;
    private final StatTimer eventTimer;

    private final HashMap<Integer, HashBasedTable<String, Overlay, OverlayDrawStep>> dimensionOverlays =
            new HashMap<Integer, HashBasedTable<String, Overlay, OverlayDrawStep>>();

    private final HashBasedTable<String, ModWaypoint, Waypoint> waypoints = HashBasedTable.create();

    private EnumSet<ClientEvent.Type> subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);

    /**
     * Constructor.
     *
     * @param plugin the plugin
     */
    public PluginWrapper(IClientPlugin plugin)
    {
        this.modId = plugin.getModId();
        this.plugin = plugin;
        this.eventTimer = StatTimer.get("pluginClientEvent_" + modId, 1, 200);
    }

    /**
     * Get (create if needed) a table for the overlays in a dimension.
     * @param dimension the current dim
     * @return a table
     */
    private HashBasedTable<String, Overlay, OverlayDrawStep> getOverlays(int dimension)
    {
        HashBasedTable<String, Overlay, OverlayDrawStep> table = dimensionOverlays.get(dimension);
        if(table==null)
        {
            table = HashBasedTable.create();
            dimensionOverlays.put(dimension, table);
        }
        return table;
    }

    /**
     * Add (or update) a displayable object to the player's maps. If you modify a Displayable after it
     * has been added, call this method again to ensure the maps reflect your changes.
     */
    public void show(Displayable displayable) throws Exception
    {
        String displayId = displayable.getDisplayId();
        switch (displayable.getDisplayType())
        {
            case Polygon:
                PolygonOverlay overlay = (PolygonOverlay) displayable;
                getOverlays(((PolygonOverlay) displayable).getDimension()).put(displayId, overlay, new DrawPolygonStep(overlay));
                break;
            case Waypoint:
                ModWaypoint modWaypoint = (ModWaypoint) displayable;
                Waypoint waypoint = new Waypoint(modWaypoint);
                WaypointStore.instance().save(waypoint);
                waypoints.put(displayId, modWaypoint, waypoint);
                break;
            default:
                break;
        }
    }

    /**
     * Remove a displayable from the player's maps.
     */
    public void remove(Displayable displayable)
    {
        String displayId = displayable.getDisplayId();
        switch (displayable.getDisplayType())
        {
            case Waypoint:
                remove((ModWaypoint) displayable);
                break;
            default:
                getOverlays(((PolygonOverlay) displayable).getDimension()).remove(displayId, displayable);
                break;
        }
    }

    /**
     * Remove a waypoint.
     *
     * @param modWaypoint
     */
    public void remove(ModWaypoint modWaypoint)
    {
        String displayId = modWaypoint.getDisplayId();
        Waypoint waypoint = waypoints.remove(displayId, modWaypoint);
        if (waypoint == null)
        {
            // temporary one needed by store right now
            waypoint = new Waypoint(modWaypoint);
        }
        WaypointStore.instance().remove(waypoint);
    }

    /**
     * Remove all displayables by DisplayType from the player's maps.
     * Not efficient, but it probably doesn't matter.
     */
    public void removeAll(DisplayType displayType)
    {
        if (displayType == DisplayType.Waypoint)
        {
            List<ModWaypoint> list = new ArrayList<ModWaypoint>(waypoints.columnKeySet());
            for (ModWaypoint modWaypoint : list)
            {
                remove(modWaypoint);
            }
        }
        else
        {
            for(HashBasedTable<String, Overlay, OverlayDrawStep> overlays : dimensionOverlays.values())
            {
                List<Displayable> list = new ArrayList<Displayable>(overlays.columnKeySet());
                for (Displayable displayable : list)
                {
                    if (displayable.getDisplayType() == displayType)
                    {
                        remove(displayable);
                    }
                }
            }
        }
    }

    /**
     * Remove all displayables.
     */
    public void removeAll()
    {
        if (!waypoints.isEmpty())
        {
            List<ModWaypoint> list = new ArrayList<ModWaypoint>(waypoints.columnKeySet());
            for (ModWaypoint modWaypoint : list)
            {
                remove(modWaypoint);
            }
        }

        if (!dimensionOverlays.isEmpty())
        {
            dimensionOverlays.clear();
        }
    }

    /**
     * Check whether a displayable exists in the Client API.  A return value of true means the Client API has the
     * indicated displayable, but not necessarily that the player has made it visible.
     *
     * @param displayable the object
     */
    public boolean exists(Displayable displayable)
    {
        String displayId = displayable.getDisplayId();
        switch (displayable.getDisplayType())
        {
            case Waypoint:
                return waypoints.containsRow(displayId);
            default:
                if(displayable instanceof Overlay)
                {
                    int dimension = ((Overlay) displayable).getDimension();
                    return getOverlays(dimension).containsRow(displayId);
                }
        }
        return false;
    }

    /**
     * Populates the provided list with all overlay drawsteps.
     * @param list
     * @param dimension
     * @param ui
     */
    public void getDrawSteps(List<OverlayDrawStep> list, int dimension, Context.UI ui)
    {
        HashBasedTable<String, Overlay, OverlayDrawStep> table = getOverlays(dimension);
        for( Table.Cell<String, Overlay, OverlayDrawStep> cell : table.cellSet())
        {
            EnumSet<Context.UI> activeUIs = cell.getColumnKey().getActiveUIs();
            if(activeUIs.contains(Context.UI.Any) || activeUIs.contains(ui))
            {
                list.add(cell.getValue());
            }
        }
    }

    /**
     * Subscribe to a set of event types.
     *
     * @param enumSet can be empty, but not null.
     */
    public void subscribe(EnumSet<ClientEvent.Type> enumSet)
    {
        subscribedClientEventTypes = EnumSet.copyOf(enumSet);
    }

    /**
     * Event types subscribed to.
     *
     * @return a set, which may be empty.
     */
    public EnumSet<ClientEvent.Type> getSubscribedClientEventTypes()
    {
        return subscribedClientEventTypes;
    }

    /**
     * Notify plugin of client event.
     *
     * @param clientEvent event
     */
    public void notify(final ClientEvent clientEvent)
    {
        if (!subscribedClientEventTypes.contains(clientEvent.type))
        {
            return;
        }

        try
        {
            boolean cancelled = clientEvent.isCancelled();
            boolean cancellable = clientEvent.type.cancellable;

            eventTimer.start();
            try
            {
                plugin.onEvent(clientEvent);
                if (cancellable && !cancelled && clientEvent.isCancelled())
                {
                    Journeymap.getLogger().debug(String.format("Plugin %s cancelled event: %s", this, clientEvent.type));
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error(String.format("Plugin %s errored during event: %s", this, clientEvent.type), t);
            }
            finally
            {
                eventTimer.stop();
                if (eventTimer.hasReachedElapsedLimit())
                {
                    Journeymap.getLogger().warn(String.format("Plugin %s too slow handling event: %s", this, clientEvent.type));
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("Plugin %s error during event: %s", this, clientEvent.type), t);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PluginWrapper))
        {
            return false;
        }
        PluginWrapper that = (PluginWrapper) o;
        return Objects.equal(modId, that.modId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(modId);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(plugin)
                .add("modId", modId)
                .toString();
    }


}
