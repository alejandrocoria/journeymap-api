package journeymap.client.api.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.*;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.UIState;
import journeymap.client.log.StatTimer;
import journeymap.client.model.Waypoint;
import journeymap.client.render.draw.DrawImageStep;
import journeymap.client.render.draw.DrawMarkerStep;
import journeymap.client.render.draw.DrawPolygonStep;
import journeymap.client.render.draw.OverlayDrawStep;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

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

    private final HashBasedTable<String, journeymap.client.api.display.Waypoint, Waypoint> waypoints = HashBasedTable.create();

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
     *
     * @param dimension the current dim
     * @return a table
     */
    private HashBasedTable<String, Overlay, OverlayDrawStep> getOverlays(int dimension)
    {
        HashBasedTable<String, Overlay, OverlayDrawStep> table = dimensionOverlays.get(dimension);
        if (table == null)
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
        String displayId = displayable.getId();
        switch (displayable.getDisplayType())
        {
            case Polygon:
                PolygonOverlay polygon = (PolygonOverlay) displayable;
                getOverlays(polygon.getDimension()).put(displayId, polygon, new DrawPolygonStep(polygon));
                break;
            case Marker:
                MarkerOverlay marker = (MarkerOverlay) displayable;
                getOverlays(marker.getDimension()).put(displayId, marker, new DrawMarkerStep(marker));
                break;
            case Image:
                ImageOverlay imageOverlay = (ImageOverlay) displayable;
                getOverlays(imageOverlay.getDimension()).put(displayId, imageOverlay, new DrawImageStep(imageOverlay));
                break;
            case Waypoint:
                journeymap.client.api.display.Waypoint modWaypoint = (journeymap.client.api.display.Waypoint) displayable;
                Waypoint waypoint = new Waypoint(modWaypoint);
                WaypointStore.INSTANCE.save(waypoint);
                waypoints.put(displayId, modWaypoint, waypoint);
                break;
            default:
                break;
        }
    }

    /**
     * Remove a displayable from API management
     */
    public void remove(Displayable displayable)
    {
        String displayId = displayable.getId();
        try
        {
            switch (displayable.getDisplayType())
            {
                case Waypoint:
                    remove((journeymap.client.api.display.Waypoint) displayable);
                    break;
                default:
                    Overlay overlay = (Overlay) displayable;
                    OverlayDrawStep drawStep = getOverlays(overlay.getDimension()).remove(displayId, displayable);
                    if (drawStep != null)
                    {
                        drawStep.setEnabled(false);
                    }
                    break;
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error removing DrawMarkerStep: " + t, LogFormatter.toString(t));
        }
    }

    /**
     * Remove a waypoint.
     *
     * @param modWaypoint
     */
    public void remove(journeymap.client.api.display.Waypoint modWaypoint)
    {
        String displayId = modWaypoint.getId();
        Waypoint waypoint = waypoints.remove(displayId, modWaypoint);
        if (waypoint == null)
        {
            // temporary one needed by store right now
            waypoint = new Waypoint(modWaypoint);
        }
        WaypointStore.INSTANCE.remove(waypoint);
    }

    /**
     * Remove all displayables by DisplayType from the player's maps.
     * Not efficient, but it probably doesn't matter.
     */
    public void removeAll(DisplayType displayType)
    {
        if (displayType == DisplayType.Waypoint)
        {
            List<journeymap.client.api.display.Waypoint> list = new ArrayList<journeymap.client.api.display.Waypoint>(waypoints.columnKeySet());
            for (journeymap.client.api.display.Waypoint modWaypoint : list)
            {
                remove(modWaypoint);
            }
        }
        else
        {
            for (HashBasedTable<String, Overlay, OverlayDrawStep> overlays : dimensionOverlays.values())
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
            List<journeymap.client.api.display.Waypoint> list = new ArrayList<journeymap.client.api.display.Waypoint>(waypoints.columnKeySet());
            for (journeymap.client.api.display.Waypoint modWaypoint : list)
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
        String displayId = displayable.getId();
        switch (displayable.getDisplayType())
        {
            case Waypoint:
                return waypoints.containsRow(displayId);
            default:
                if (displayable instanceof Overlay)
                {
                    int dimension = ((Overlay) displayable).getDimension();
                    return getOverlays(dimension).containsRow(displayId);
                }
        }
        return false;
    }

    /**
     * Populates the provided list with all overlay drawsteps.
     */
    public void getDrawSteps(List<OverlayDrawStep> list, UIState uiState)
    {
        HashBasedTable<String, Overlay, OverlayDrawStep> table = getOverlays(uiState.dimension);
        for (Table.Cell<String, Overlay, OverlayDrawStep> cell : table.cellSet())
        {
            if (cell.getColumnKey().isActiveIn(uiState))
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
        return MoreObjects.toStringHelper(plugin)
                .add("modId", modId)
                .toString();
    }


}
