/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.service;

import journeymap.client.JourneymapClient;
import journeymap.common.properties.config.StringField;
import se.rupy.http.Event;

import java.util.Arrays;
import java.util.List;


/**
 * Redirect to the Google Map API with the TLD specified in WebMapProperties
 *
 * @author mwoodman
 */
public class MapApiService extends FileService
{
    /**
     * Default constructor
     */
    public MapApiService()
    {
    }

    @Override
    public String path()
    {
        return "/mapapi"; //$NON-NLS-1$
    }

    /**
     * Serve it.
     */
    @Override
    public void filter(Event event) throws Event, Exception
    {
        String domain = JourneymapClient.getWebMapProperties().googleMapApiDomain.get();
        String apiUrl = String.format("http://maps.google%s/maps/api/js?libraries=geometry&sensor=false", domain);

        ResponseHeader.on(event).setHeader("Location", apiUrl).noCache();
        event.reply().code("303 See Other");
        throw event;
    }

    public static class TopLevelDomains implements StringField.ValuesProvider
    {
        @Override
        public List<String> getStrings()
        {
            return Arrays.asList(".ae", ".cn", ".com", ".es", ".hu", ".kr", ".nl", ".se");
        }

        @Override
        public String getDefaultString()
        {
            return ".com";
        }
    }
}
