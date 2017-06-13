package journeymap.client.api.impl;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.PluginHelper;
import journeymap.client.api.util.UIState;
import journeymap.client.io.FileHandler;
import journeymap.client.render.draw.OverlayDrawStep;
import journeymap.client.task.multi.ApiImageTask;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * Implementation of the journeymap-api IClientAPI.
 */
@ParametersAreNonnullByDefault
public enum ClientAPI implements IClientAPI
{
    INSTANCE;

    private final Logger LOGGER = Journeymap.getLogger();
    private final List<OverlayDrawStep> lastDrawSteps = new ArrayList<OverlayDrawStep>();

    private HashMap<String, PluginWrapper> plugins = new HashMap<String, PluginWrapper>();
    private ClientEventManager clientEventManager = new ClientEventManager(plugins.values());
    private boolean drawStepsUpdateNeeded = true;
    private Context.UI lastUi = Context.UI.Any;
    private Context.MapType lastMapType = Context.MapType.Any;
    private int lastDimension = Integer.MIN_VALUE;

    private ClientAPI()
    {
        log("built with JourneyMap API " + IClientAPI.API_VERSION);
    }

    @Override
    public UIState getUIState(Context.UI ui)
    {
        switch (ui)
        {
            case Minimap:
                return MiniMap.uiState();
            case Fullscreen:
                return Fullscreen.uiState();
            default:
                return null;
        }
    }

    @Override
    public void subscribe(String modId, EnumSet<ClientEvent.Type> enumSet)
    {
        try
        {
            getPlugin(modId).subscribe(enumSet);

            // Refresh master set of event types
            clientEventManager.updateSubscribedTypes();
        }
        catch (Throwable t)
        {
            logError("Error subscribing: " + t, t);
        }
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

    /**
     * Remove all mod plugins.
     */
    public void purge()
    {
        try
        {
            this.drawStepsUpdateNeeded = true;
            this.lastDrawSteps.clear();
            this.plugins.clear();
            this.clientEventManager.purge();
        }
        catch (Throwable t)
        {
            logError("Error purging: " + t, t);
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

    /**
     * Note:  This method IS NOT SUPPORTED for most mods. Misuse will lead to severe performance issues.
     * Talk to Techbrew if you need to use this function.
     * <p>
     * Asynchonrously request a BufferedImage map tile from JourneyMap. Requests may be throttled, so use sparingly.
     * The largest image size that will be returned is 512x512 px.
     *
     * @param modId      Mod id
     * @param dimension  The dimension
     * @param apiMapType The map type
     * @param startChunk The NW chunk of the tile.
     * @param endChunk   The SW chunk of the tile.
     * @param chunkY     The vertical chunk (slice) if the maptype isn't day/night/topo
     * @param zoom       The zoom level (0-8)
     * @param showGrid   Whether to include to include the chunk grid overlay
     * @param callback   A callback function which will provide a BufferedImage when/if available.  If it returns null, then no image available.
     */
    public void requestMapTile(String modId, int dimension, Context.MapType apiMapType, ChunkPos startChunk, ChunkPos endChunk,
                        @Nullable Integer chunkY, int zoom, boolean showGrid, final Consumer<BufferedImage> callback)
    {
        log("requestMapTile");

        boolean honorRequest = true;
        final File worldDir = FileHandler.getJMWorldDir(Minecraft.getMinecraft());
        if (!Objects.equals("jmitems", modId))
        {
            honorRequest = false;
            logError("requestMapTile not supported");
        }
        else if (worldDir == null || !worldDir.exists() || !worldDir.isDirectory())
        {
            honorRequest = false;
            logError("world directory not found: " + worldDir);
        }


        {
            try
            {
                if (honorRequest)
                {
                    Journeymap.getClient().queueOneOff(new ApiImageTask(modId, dimension, apiMapType, startChunk, endChunk, chunkY, zoom, showGrid, callback));
                }
                else
                {
                    Minecraft.getMinecraft().addScheduledTask(() -> callback.accept(null));
                }
            }
            catch (Exception e)
            {
                callback.accept(null);
            }
        }
    }

    private boolean playerAccepts(Displayable displayable)
    {
        return playerAccepts(displayable.getModId(), displayable.getDisplayType());
    }

    /**
     * Gets the manager of client event handling.
     *
     * @return clientEventManager
     */
    public ClientEventManager getClientEventManager()
    {
        return clientEventManager;
    }

    /**
     * Get all draw steps from all plugins. Builds and sorts the list only when needed.
     */
    public void getDrawSteps(List<? super OverlayDrawStep> list, UIState uiState)
    {
        if (uiState.ui != lastUi || uiState.dimension != lastDimension || uiState.mapType != lastMapType)
        {
            drawStepsUpdateNeeded = true;
            lastUi = uiState.ui;
            lastDimension = uiState.dimension;
            lastMapType = uiState.mapType;
        }

        if (drawStepsUpdateNeeded)
        {
            lastDrawSteps.clear();
            for (PluginWrapper pluginWrapper : plugins.values())
            {
                pluginWrapper.getDrawSteps(lastDrawSteps, uiState);
            }
            Collections.sort(lastDrawSteps, new Comparator<OverlayDrawStep>()
            {
                @Override
                public int compare(OverlayDrawStep o1, OverlayDrawStep o2)
                {
                    return Integer.compare(o1.getDisplayOrder(), o2.getDisplayOrder());
                }
            });
            drawStepsUpdateNeeded = false;
        }

        list.addAll(lastDrawSteps);
    }

    private PluginWrapper getPlugin(String modId)
    {
        if (Strings.isEmpty(modId))
        {
            throw new IllegalArgumentException("Invalid modId: " + modId);
        }

        PluginWrapper pluginWrapper = plugins.get(modId);
        if (pluginWrapper == null)
        {
            IClientPlugin plugin = PluginHelper.INSTANCE.getPlugins().get(modId);
            if (plugin == null)
            {
                // TODO REMOVE THIS temp
                if (modId.equals("journeymap"))
                {
                    plugin = new IClientPlugin()
                    {
                        @Override
                        public void initialize(IClientAPI jmClientApi)
                        {

                        }

                        @Override
                        public String getModId()
                        {
                            return "journeymap";
                        }

                        @Override
                        public void onEvent(ClientEvent event)
                        {

                        }
                    };
                }
                else
                {
                    throw new IllegalArgumentException("No plugin found for modId: " + modId);
                }
            }
            pluginWrapper = new PluginWrapper(plugin);
            plugins.put(modId, pluginWrapper);
        }

        return pluginWrapper;
    }

    public boolean isDrawStepsUpdateNeeded()
    {
        return drawStepsUpdateNeeded;
    }

    /**
     * Log a message
     *
     * @param message
     */
    void log(String message)
    {
        LOGGER.info(String.format("[%s] %s", getClass().getSimpleName(), message));
    }

    private void logError(String message)
    {
        LOGGER.error(String.format("[%s] %s", getClass().getSimpleName(), message));
    }

    void logError(String message, Throwable t)
    {
        LOGGER.error(String.format("[%s] %s", getClass().getSimpleName(), message), t);
    }

    public void flagOverlaysForRerender()
    {
        for (OverlayDrawStep overlayDrawStep : lastDrawSteps)
        {
            overlayDrawStep.getOverlay().flagForRerender();
        }
    }
}
