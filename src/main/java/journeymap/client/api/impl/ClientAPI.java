package journeymap.client.api.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedHashMultimap;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.common.Journeymap;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the journeymap-api IClientAPI.
 */
@ParametersAreNonnullByDefault
public enum ClientAPI implements IClientAPI
{
    INSTANCE;

    private final Logger LOGGER = Journeymap.getLogger();

    private final LoadingCache<String, LinkedHashMultimap<DisplayType, String>> modDisplayables =
            CacheBuilder.newBuilder().build(
                    new CacheLoader<String, LinkedHashMultimap<DisplayType, String>>()
                    {
                        public LinkedHashMultimap<DisplayType, String> load(String key)
                        {
                            LinkedHashMultimap<DisplayType, String> multimap = LinkedHashMultimap.create();
                            return multimap;
                        }
                    });

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public void show(Displayable displayable)
    {
        showDisplayable(displayable.getModId(), DisplayType.of(displayable.getClass()), displayable.getDisplayId());
    }

    private void showDisplayable(String modId, DisplayType displayType, String displayId)
    {
        modDisplayables.getUnchecked(modId).put(displayType, displayId);
        log(String.format("Showed %s:%s:%s", modId, displayType, displayId));
    }

    @Override
    public void remove(Displayable displayable)
    {
        remove(displayable.getModId(), DisplayType.of(displayable.getClass()), displayable.getDisplayId());
    }

    @Override
    public void remove(String modId, DisplayType displayType, String displayId)
    {
        modDisplayables.getUnchecked(modId).remove(displayType, displayId);
        log(String.format("Removed %s:%s:%s", modId, displayType, displayId));
    }

    @Override
    public void removeAll(String modId, DisplayType displayType)
    {
        modDisplayables.getUnchecked(modId).removeAll(displayType);
        log(String.format("Removed all %s:%s", modId, displayType));
    }

    @Override
    public void removeAll(String modId)
    {
        modDisplayables.invalidateAll();
        log(String.format("Removed all %s", modId));
    }

    @Override
    public boolean exists(String modId, DisplayType displayType, String displayId)
    {
        return modDisplayables.getUnchecked(modId).containsEntry(displayType, displayId);
    }

    @Override
    public boolean isVisible(String modId, DisplayType displayType, String displayId)
    {
        return exists(modId, displayType, displayId);
    }

    @Override
    public List<String> getShownIds(String modId, DisplayType displayType)
    {
        return new ArrayList<String>(modDisplayables.getUnchecked(modId).get(displayType));
    }

    @Override
    public boolean playerAccepts(String modId, DisplayType displayType)
    {
        return true;
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
}
