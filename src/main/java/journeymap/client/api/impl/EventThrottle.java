package journeymap.client.api.impl;

import journeymap.client.api.event.ClientEvent;

/**
 * Simple implementation for a "last wins" throttle of events.
 */
public class EventThrottle<V extends ClientEvent>
{
    private final long delay;
    private V lastEvent;
    private boolean throttleNext;
    private long releaseTime;

    public EventThrottle(long delay)
    {
        this.delay = delay;
    }

    public void add(V event)
    {
        if (releaseTime == 0 && lastEvent != null)
        {
            releaseTime = System.currentTimeMillis() + delay;
        }
        lastEvent = event;
    }

    public boolean canRelease()
    {
        return lastEvent != null && System.currentTimeMillis() >= releaseTime;
    }

    public V get()
    {
        V event = lastEvent;
        lastEvent = null;
        releaseTime = 0;
        return event;
    }

}
