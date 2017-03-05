/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.service;

import journeymap.common.Journeymap;
import journeymap.common.properties.config.StringField;
import se.rupy.http.Event;

import java.util.Arrays;
import java.util.List;


/**
 * Redirect to the Google Map API with the TLD specified in WebMapProperties
 *
 * @author techbrew
 */
public class MapApiService extends FileService
{
    private static final String API_KEY = "AIzaSyDeq8K0022T9N1y-7Q7GBYhwoDS2hruB3c";

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
        String domain = Journeymap.getClient().getWebMapProperties().googleMapApiDomain.get();
        String apiUrl = String.format("http://maps.google%s/maps/api/js?key=%s&libraries=geometry&sensor=false", domain, API_KEY);

        ResponseHeader.on(event).setHeader("Location", apiUrl).noCache();
        event.reply().code("303 See Other");
        throw event;
    }

    /**
     * The type Top level domains.
     */
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
