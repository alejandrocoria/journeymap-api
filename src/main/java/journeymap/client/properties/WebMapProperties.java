/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.service.MapApiService;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;


import static journeymap.common.properties.Category.WebMap;

/**
 * Properties for the web map in browser.
 */
public class WebMapProperties extends MapProperties
{
    public final BooleanField enabled = new BooleanField(WebMap, "jm.webmap.enable", false, true);
    public final IntegerField port = new IntegerField(WebMap, "jm.advanced.port", 80, 10000, 8080);
    public final StringField googleMapApiDomain = new StringField(WebMap, "jm.webmap.google_domain", MapApiService.TopLevelDomains.class);

    public WebMapProperties()
    {
    }

    @Override
    public String getName()
    {
        return "webmap";
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
        result = 31 * result + getName().hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + enabled.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "WebMapProperties: " +
                ", enabled=" + enabled +
                ", port=" + port +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", entityIconSetName=" + entityIconSetName;
    }

}
