/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.service.MapApiService;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

import static journeymap.client.properties.ClientCategory.WebMap;

/**
 * Properties for the web map in browser.
 */
public class WebMapProperties extends MapProperties
{
    public final BooleanField enabled = new BooleanField(WebMap, "jm.webmap.enable", false, true);
    public final IntegerField port = new IntegerField(WebMap, "jm.advanced.port", 80, 10000, 8080);
    public final StringField googleMapApiDomain = new StringField(WebMap, "jm.webmap.google_domain", MapApiService.TopLevelDomains.class);

    @Override
    public String getName()
    {
        return "webmap";
    }
}
