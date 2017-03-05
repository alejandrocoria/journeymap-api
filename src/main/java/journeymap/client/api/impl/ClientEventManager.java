/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.api.impl;

import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.event.DisplayUpdateEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * Event-related logic for the ClientAPI.
 */
@ParametersAreNonnullByDefault
public class ClientEventManager
{
    private final DisplayUpdateEventThrottle displayUpdateEventThrottle = new DisplayUpdateEventThrottle();
    private final Collection<PluginWrapper> plugins;
    private EnumSet<ClientEvent.Type> subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);

    /**
     * Instantiates a new Client event manager.
     *
     * @param plugins the plugins
     */
    public ClientEventManager(Collection<PluginWrapper> plugins)
    {
        this.plugins = plugins;
    }

    /**
     * Refresh master set of event types
     */
    public void updateSubscribedTypes()
    {
        subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);
        for (PluginWrapper wrapper : plugins)
        {
            subscribedClientEventTypes.addAll(wrapper.getSubscribedClientEventTypes());
        }
    }

    /**
     * Whether the type of event needs to be be fired.
     *
     * @param type the type
     * @return true if at least one plugin subscribes to the type.
     */
    public boolean canFireClientEvent(ClientEvent.Type type)
    {
        return subscribedClientEventTypes.contains(type);
    }

    /**
     * Notify plugins of MAPPING_STARTED
     *
     * @param started   if true, event is MAPPING_STARTED
     * @param dimension if false, event is MAPPING_STOPPED
     */
    public void fireMappingEvent(boolean started, int dimension)
    {
        ClientEvent.Type type = started ? ClientEvent.Type.MAPPING_STARTED : ClientEvent.Type.MAPPING_STOPPED;
        if (plugins.isEmpty() || !subscribedClientEventTypes.contains(type))
        {
            return;
        }

        ClientEvent clientEvent = new ClientEvent(type, dimension);
        for (PluginWrapper wrapper : plugins)
        {
            try
            {
                wrapper.notify(clientEvent);
            }
            catch (Throwable t)
            {
                ClientAPI.INSTANCE.logError("Error in fireMappingEvent(): " + clientEvent, t);
            }
        }
    }

    /**
     * Notify plugins of client event.
     *
     * @param clientEvent event
     */
    public void fireDeathpointEvent(final DeathWaypointEvent clientEvent)
    {
        if (plugins.isEmpty() || !subscribedClientEventTypes.contains(ClientEvent.Type.DEATH_WAYPOINT))
        {
            return;
        }

        //ClientAPI.INSTANCE.log(clientEvent.toString());
        for (PluginWrapper wrapper : plugins)
        {
            try
            {
                wrapper.notify(clientEvent);
            }
            catch (Throwable t)
            {
                ClientAPI.INSTANCE.logError("Error in fireDeathpointEvent(): " + clientEvent, t);
            }
        }
    }

    /**
     * Notify plugins of DisplayUpdateEvent, which may be deferred.
     *
     * @param clientEvent event
     */
    public void fireDisplayUpdateEvent(final DisplayUpdateEvent clientEvent)
    {
        if (plugins.size() == 0 || !subscribedClientEventTypes.contains(ClientEvent.Type.DISPLAY_UPDATE))
        {
            return;
        }

        try
        {
            displayUpdateEventThrottle.add(clientEvent);
            //fireNextClientEvents();
        }
        catch (Throwable t)
        {
            ClientAPI.INSTANCE.logError("Error in fireDisplayUpdateEvent(): " + clientEvent, t);
        }
    }

    /**
     * Fires any deferred DisplayUpdateEvents.
     */
    public void fireNextClientEvents()
    {
        if (!plugins.isEmpty() && displayUpdateEventThrottle.isReady())
        {
            Iterator<DisplayUpdateEvent> iterator = displayUpdateEventThrottle.iterator();
            while (iterator.hasNext())
            {
                DisplayUpdateEvent clientEvent = iterator.next();
                iterator.remove();

                //ClientAPI.INSTANCE.log(clientEvent.toString());
                for (PluginWrapper wrapper : plugins)
                {
                    try
                    {
                        wrapper.notify(clientEvent);
                    }
                    catch (Throwable t)
                    {
                        ClientAPI.INSTANCE.logError("Error in fireDeathpointEvent(): " + clientEvent, t);
                    }
                }
            }
        }
    }

    /**
     * Clear all listeners
     */
    void purge()
    {
        this.plugins.clear();
        this.subscribedClientEventTypes.clear();
    }
}
