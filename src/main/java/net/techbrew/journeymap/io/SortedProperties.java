/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io;

import java.util.*;

/**
 * Extension of Properties with sorted keys for alphabetic output.
 * Not optimized for performance nor thread-safety.
 *
 * @author mwoodman
 */
@Deprecated
public class SortedProperties extends Properties
{

    /**
     * Called throughout by Properties, including Properties.store(OutputStream out, String comments).
     */
    @Override
    public synchronized Enumeration<Object> keys()
    {
        return new Vector(this.keySet()).elements();
    }

    /**
     * Called by Properties.stringPropertyNames() and this.keys().
     */
    @Override
    public Set<Object> keySet()
    {
        Set<Object> keySet = super.keySet();
        if (keySet == null)
        {
            return keySet;
        }
        return new TreeSet(keySet);
    }

    /**
     * Called by Properties.toString().
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet()
    {
        Set<Map.Entry<Object, Object>> entrySet = super.entrySet();
        if (entrySet == null)
        {
            return entrySet;
        }

        Set<Map.Entry<Object, Object>> sortedSet = new TreeSet(new EntryComparator());
        sortedSet.addAll(entrySet);
        return sortedSet;
    }

    /**
     * Comparator for sorting Map.Entry by key
     * Assumes non-null entries.
     */
    class EntryComparator implements Comparator<Map.Entry<Object, Object>>
    {

        @Override
        public int compare(Map.Entry<Object, Object> entry1, Map.Entry<Object, Object> entry2)
        {
            return entry1.getKey().toString().compareTo(entry2.getKey().toString());
        }

    }

}
