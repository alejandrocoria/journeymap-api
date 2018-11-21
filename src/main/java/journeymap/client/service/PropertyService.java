/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.service;

import journeymap.client.properties.WebMapProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.BooleanField;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide player data
 *
 * @author techbrew
 */
public class PropertyService extends BaseService
{
    public static final String CALLBACK_PARAM = "callback";  //$NON-NLS-1$
    WebMapProperties webMapProperties;
    HashMap<String, BooleanField> propMap = new HashMap<String, BooleanField>();

    /**
     * Serves / saves property info
     */
    public PropertyService()
    {
        super();


    }

    @Override
    public String path()
    {
        return "/properties";
    }

    private void init()
    {
        if (propMap.isEmpty())
        {
            webMapProperties = Journeymap.getClient().getWebMapProperties();
            propMap.put("showCaves", webMapProperties.showCaves);
            propMap.put("showGrid", webMapProperties.showGrid);
//            propMap.put("showAnimals", webMapProperties.showAnimals);
//            propMap.put("showMobs", webMapProperties.showMobs);
//            propMap.put("showPets", webMapProperties.showPets);
//            propMap.put("showPlayers", webMapProperties.showPlayers);
//            propMap.put("showVillagers", webMapProperties.showVillagers);
            propMap.put("showWaypoints", webMapProperties.showWaypoints);
        }
    }

    @Override
    public void filter(Event event) throws Event, Exception
    {

        try
        {
            init();

            // Parse query for parameters
            Query query = event.query();
            query.parse();
            String path = query.path();

            if (query.method() == Query.POST)
            {
                post(event);
                return;
            }
            else if (query.method() != Query.GET)
            {
                throw new Exception("HTTP method not allowed");
            }

            // Build the response string
            StringBuffer jsonData = new StringBuffer();

            // Check for callback to determine Json or JsonP
            boolean useJsonP = query.containsKey(CALLBACK_PARAM);
            if (useJsonP)
            {
                jsonData.append(URLEncoder.encode(query.get(CALLBACK_PARAM).toString(), UTF8.name()));
                jsonData.append("("); //$NON-NLS-1$
            }
            else
            {
                jsonData.append("data="); //$NON-NLS-1$
            }

            // Put map into json form
            Map<String, Boolean> valMap = new HashMap<>();
            for (Map.Entry<String, BooleanField> entry : propMap.entrySet())
            {
                valMap.put(entry.getKey(), entry.getValue().get());
            }
            jsonData.append(GSON.toJson(valMap));

            // Finish function call for JsonP if needed
            if (useJsonP)
            {
                jsonData.append(")"); //$NON-NLS-1$

                // Optimize headers for JSONP
                ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
            }

            // Gzip response
            gzipResponse(event, jsonData.toString());

        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            throwEventException(500, "Error trying " + path, event, true);
        }
    }

    public void post(Event event) throws Event, Exception
    {

        try
        {
            Query query = event.query();
            String[] param = query.parameters().split("=");
            if (param.length != 2)
            {
                throw new Exception("Expected single key-value pair");
            }
            String key = param[0];
            String value = param[1];

            if (propMap.containsKey(key))
            {
                Boolean boolValue = Boolean.parseBoolean(value);
                propMap.get(key).set(boolValue);
                webMapProperties.save();
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            throwEventException(500, "Error trying " + path, event, true);
        }
    }
}
