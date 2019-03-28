package journeymap.common.network.impl.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the callback management.
 */
public class CallbackService
{
    private final Map<UUID, CallbackWrapper> callbackMap;
    private static CallbackService INSTANCE;

    private CallbackService()
    {
        callbackMap = new HashMap<>();
    }

    public static CallbackService getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new CallbackService();
        }
        return INSTANCE;
    }

    /**
     * Saves a callback by id.
     *
     * @param id       - The Id.
     * @param callback - The callback.
     */
    public void saveCallback(UUID id, AsyncCallback callback)
    {
        callbackMap.put(id, new CallbackWrapper(callback));
    }

    /**
     * Gets the callback from an id.
     *
     * @param id - The Id.
     * @return - The callback.
     */
    public AsyncCallback getCallback(UUID id)
    {
        CallbackWrapper callbackWrapper = callbackMap.get(id);
        if (callbackWrapper != null)
        {
            return callbackWrapper.getCallback();
        }
        return null;
    }

    /**
     * Removes the callback by id. This needs to be called after the onSuccess is called.
     *
     * @param id - The id.
     */
    public void removeCallback(UUID id)
    {
        callbackMap.remove(id);
    }

    private class CallbackWrapper
    {
        AsyncCallback callback;

        public CallbackWrapper(AsyncCallback callback)
        {
            this.callback = callback;
        }

        public AsyncCallback getCallback()
        {
            return callback;
        }
    }
}
