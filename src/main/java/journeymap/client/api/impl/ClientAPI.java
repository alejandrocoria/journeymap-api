package journeymap.client.api.impl;

import journeymap.client.JourneymapClient;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.DisplayUpdateEvent;
import journeymap.client.api.util.PluginHelper;
import journeymap.client.render.draw.DrawStep;
import journeymap.common.Journeymap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.Strings;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Implementation of the journeymap-api IClientAPI.
 */
@ParametersAreNonnullByDefault
public enum ClientAPI implements IClientAPI
{
    INSTANCE;

    private final Logger LOGGER = Journeymap.getLogger();
    private final List<DrawStep> lastDrawSteps = new ArrayList<DrawStep>();
    private final EventThrottle<DisplayUpdateEvent> displayUpdateEventEventThrottle = new EventThrottle<DisplayUpdateEvent>(1000);
    private HashMap<String, PluginWrapper> cache = new HashMap<String, PluginWrapper>();
    private boolean drawStepsUpdateNeeded = true;

    @Override
    public boolean isActive(Enum<? extends Context>... enums)
    {
        // TODO
        return JourneymapClient.getInstance().isMapping();
    }

    @Override
    public void subscribe(EnumSet<ClientEvent.Type> enumSet)
    {
        System.out.println("Subscribe isn't implemented yet.  You're gonna get eveything.");
    }

    @Override
    public void show(Displayable displayable)
    {
        try
        {
            if (playerAccepts(displayable))
            {
                getPlugin(displayable.getModId()).show(displayable);
                drawStepsUpdateNeeded = true;
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
        try
        {
            if (playerAccepts(displayable))
            {
                getPlugin(displayable.getModId()).remove(displayable);
                drawStepsUpdateNeeded = true;
            }
        }
        catch (Throwable t)
        {
            logError("Error removing displayable: " + displayable, t);
        }
    }

    @Override
    public void removeAll(String modId, DisplayType displayType)
    {
        try
        {
            if (playerAccepts(modId, displayType))
            {
                getPlugin(modId).removeAll(displayType);
                drawStepsUpdateNeeded = true;
            }
        }
        catch (Throwable t)
        {
            logError("Error removing all displayables: " + displayType, t);
        }
    }

    @Override
    public void removeAll(String modId)
    {
        try
        {
            for (DisplayType displayType : DisplayType.values())
            {
                removeAll(modId, displayType);
                drawStepsUpdateNeeded = true;
            }

            getPlugin(modId).removeAll();
        }
        catch (Throwable t)
        {
            logError("Error removing all displayables for mod: " + modId, t);
        }
    }

    @Override
    public boolean exists(Displayable displayable)
    {
        try
        {
            if (playerAccepts(displayable))
            {
                return getPlugin(displayable.getModId()).exists(displayable);
            }
        }
        catch (Throwable t)
        {
            logError("Error checking exists: " + displayable, t);
        }
        return false;
    }


    @Override
    public boolean playerAccepts(String modId, DisplayType displayType)
    {
        // TODO
        return true;
    }

    public boolean playerAccepts(Displayable displayable)
    {
        return playerAccepts(displayable.getModId(), displayable.getDisplayType());
    }

    /**
     * Notify plugins of client event.
     *
     * @param clientEvent event
     */
    public void fireClientEvent(final ClientEvent clientEvent, boolean immediate)
    {
        if (!immediate && clientEvent instanceof DisplayUpdateEvent)
        {
            displayUpdateEventEventThrottle.add((DisplayUpdateEvent) clientEvent);
            return;
        }

        try
        {
            for (PluginWrapper wrapper : cache.values())
            {
                wrapper.notify(clientEvent);
            }
        }
        catch (Throwable t)
        {
            logError("Error in fireClientEvent(): " + clientEvent, t);
        }
    }

    public void fireNextClientEvent()
    {
        if (displayUpdateEventEventThrottle.canRelease())
        {
            fireClientEvent(displayUpdateEventEventThrottle.get(), true);
        }
    }

    /**
     * Get all draw steps from all plugins. Builds and sorts the list only when needed.
     * @param list
     * @return
     */
    public List<DrawStep> getDrawSteps(List<DrawStep> list)
    {
        if (drawStepsUpdateNeeded)
        {
            lastDrawSteps.clear();
            for (PluginWrapper pluginWrapper : cache.values())
            {
                pluginWrapper.getDrawSteps(lastDrawSteps);
            }
            Collections.sort(lastDrawSteps, new Comparator<DrawStep>()
            {
                @Override
                public int compare(DrawStep o1, DrawStep o2)
                {
                    return Integer.compare(o1.getDisplayOrder(), o2.getDisplayOrder());
                }
            });
            drawStepsUpdateNeeded = false;
        }
        list.addAll(lastDrawSteps);
        return list;
    }

    private PluginWrapper getPlugin(String modId)
    {
        if (Strings.isEmpty(modId))
        {
            throw new IllegalArgumentException("Invalid modId: " + modId);
        }

        PluginWrapper pluginWrapper = cache.get(modId);
        if (pluginWrapper == null)
        {
            IClientPlugin plugin = PluginHelper.INSTANCE.getPlugins().get(modId);
            if (plugin == null)
            {
                throw new IllegalArgumentException("No plugin found for modId: " + modId);
            }
            pluginWrapper = new PluginWrapper(plugin);
            cache.put(modId, pluginWrapper);
        }

        return pluginWrapper;
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

    private void logError(String message)
    {
        LOGGER.error(String.format("[%s] %s", getClass().getSimpleName(), message));
    }

    private void logError(String message, Throwable t)
    {
        LOGGER.error(String.format("[%s] %s", getClass().getSimpleName(), message), t);
    }
}
