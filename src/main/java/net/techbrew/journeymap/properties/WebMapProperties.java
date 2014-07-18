/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for the web map in browser.
 */
public class WebMapProperties extends MapProperties
{
    protected transient static final int CODE_REVISION = 3;
    public final AtomicBoolean enabled = new AtomicBoolean(true);
    public final AtomicInteger port = new AtomicInteger(8080);
    public final AtomicInteger browserPoll = new AtomicInteger(2000);
    public final AtomicReference<String> entityIconSetName = new AtomicReference<String>("3D");
    protected transient final String name = "webmap";

    public WebMapProperties()
    {
    }

    @Override
    public AtomicReference<String> getEntityIconSetName()
    {
        return entityIconSetName;
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

        WebMapProperties that = (WebMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + fileRevision;
        result = 31 * result + enabled.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + browserPoll.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "WebMapProperties: " +
                "fileRevision=" + fileRevision +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", managerEnabled=" + enabled +
                ", port=" + port +
                ", browserPoll=" + browserPoll +
                ", entityIconSetName=" + getEntityIconSetName();
    }
}
