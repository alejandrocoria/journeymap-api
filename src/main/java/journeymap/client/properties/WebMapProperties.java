/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
    /**
     * The Enabled.
     */
    public final BooleanField enabled = new BooleanField(WebMap, "jm.webmap.enable", false, true);
    /**
     * The Port.
     */
    public final IntegerField port = new IntegerField(WebMap, "jm.advanced.port", 80, 10000, 8080);
    /**
     * The Google map api domain.
     */
    public final StringField googleMapApiDomain = new StringField(WebMap, "jm.webmap.google_domain", MapApiService.TopLevelDomains.class);

    @Override
    public String getName()
    {
        return "webmap";
    }
}
