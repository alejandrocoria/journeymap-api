/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.Constants;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends InGameMapProperties
{
    protected transient static final int CODE_REVISION = 3;
    public final AtomicBoolean showCaves = new AtomicBoolean(true);
    public final AtomicBoolean showGrid = new AtomicBoolean(true);
    public final AtomicReference<String> entityIconSetName = new AtomicReference<String>("3D");
    public final AtomicReference<Constants.MapType> preferredMapType = new AtomicReference<Constants.MapType>(Constants.MapType.day);
    protected transient final String name = "fullmap";

    public FullMapProperties()
    {
    }

    @Override
    public AtomicReference<String> getEntityIconSetName()
    {
        return entityIconSetName;
    }

    @Override
    public AtomicReference<Constants.MapType> getPreferredMapType()
    {
        return preferredMapType;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getCodeRevision()
    {
        return CODE_REVISION;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        FullMapProperties that = (FullMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + showGrid.hashCode();
        result = 31 * result + showCaves.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + fileRevision;
        return result;
    }

    @Override
    public String toString()
    {
        return "FullMapProperties: " +
                "fileRevision=" + fileRevision +
                ", showCaves=" + showCaves +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", showGrid=" + showGrid +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall +
                ", textureSmall=" + textureSmall +
                ", terrainAlpha=" + terrainAlpha +
                ", entityIconSetName=" + entityIconSetName;
    }
}
