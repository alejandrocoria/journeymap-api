/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.api.impl;

import journeymap.client.api.event.DisplayUpdateEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Simple implementation for a "last wins" throttle of events,
 * separated by the UI display of origin.
 */
class DisplayUpdateEventThrottle
{
    private final Queue fullscreenQueue = new Queue(1000);
    private final Queue minimapQueue = new Queue(2000);
    // private final Queue webmapQueue = new Queue(5000);
    private final Queue[] queues = new Queue[]{fullscreenQueue, minimapQueue};
    private final ArrayList<DisplayUpdateEvent> readyEvents = new ArrayList<DisplayUpdateEvent>(3);

    // Comparator to sort by event timestamp.
    private final Comparator<DisplayUpdateEvent> comparator = new Comparator<DisplayUpdateEvent>()
    {
        @Override
        public int compare(DisplayUpdateEvent o1, DisplayUpdateEvent o2)
        {
            return Long.compare(o1.timestamp, o2.timestamp);
        }
    };

    /**
     * Add an event to be potentially throttled.
     *
     * @param event the event
     */
    public void add(DisplayUpdateEvent event)
    {
        switch (event.uiState.ui)
        {
            case Fullscreen:
                fullscreenQueue.offer(event);
                break;
            case Minimap:
                minimapQueue.offer(event);
                break;
//            case Webmap:
//                webmapQueue.offer(event);
//                break;
            default:
                throw new UnsupportedOperationException("Can't throttle events for UI." + event.uiState.ui);
        }
    }

    /**
     * Iterator of events ready to fire.  Caller should
     * use remove() after each event is used.
     *
     * @return iterator
     */
    public Iterator<DisplayUpdateEvent> iterator()
    {
        long now = System.currentTimeMillis();
        for (Queue queue : queues)
        {
            if (queue.lastEvent != null && now >= queue.releaseTime)
            {
                readyEvents.add(queue.remove());
            }
        }

        if (readyEvents.size() > 0)
        {
            Collections.sort(readyEvents, comparator);
        }

        return readyEvents.iterator();
    }

    /**
     * Whether there are events ready to fire.
     *
     * @return true if events can be fired
     */
    public boolean isReady()
    {
        long now = System.currentTimeMillis();
        for (Queue queue : queues)
        {
            if (queue.lastEvent != null && now >= queue.releaseTime)
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Trivial last-one-wins queue that only enforces a delay
     * if multiple events come in.
     */
    class Queue
    {
        private final long delay;
        private DisplayUpdateEvent lastEvent;
        private boolean throttleNext;
        private long releaseTime;

        Queue(long delay)
        {
            this.delay = delay;
        }

        void offer(DisplayUpdateEvent event)
        {
            if (releaseTime == 0 && lastEvent != null)
            {
                releaseTime = System.currentTimeMillis() + delay;
            }
            lastEvent = event;
        }

        DisplayUpdateEvent remove()
        {
            DisplayUpdateEvent event = lastEvent;
            lastEvent = null;
            releaseTime = 0;
            return event;
        }
    }

}
