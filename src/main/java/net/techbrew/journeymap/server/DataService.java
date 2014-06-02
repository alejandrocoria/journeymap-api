package net.techbrew.journeymap.server;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.*;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide data for the Web UI
 *
 * @author mwoodman
 */
public class DataService extends BaseService
{

    private static final long serialVersionUID = 4412225358529161454L;

    public static final String combinedPath;

    public static final HashMap<String, Class> providerMap;

    static
    {
        providerMap = new HashMap<String, Class>(14);
        providerMap.put("/data/all", AllData.class);  //$NON-NLS-1$
        providerMap.put("/data/animals", AnimalsData.class);  //$NON-NLS-1$
        providerMap.put("/data/mobs", MobsData.class);  //$NON-NLS-1$
        providerMap.put("/data/game", GameData.class);  //$NON-NLS-1$
        providerMap.put("/data/image", ImagesData.class);  //$NON-NLS-1$
        providerMap.put("/data/messages", MessagesData.class);  //$NON-NLS-1$
        providerMap.put("/data/player", PlayerData.class);  //$NON-NLS-1$
        providerMap.put("/data/players", PlayersData.class);  //$NON-NLS-1$
        providerMap.put("/data/world", WorldData.class);  //$NON-NLS-1$
        providerMap.put("/data/villagers", VillagersData.class);  //$NON-NLS-1$
        providerMap.put("/data/waypoints", WaypointsData.class);  //$NON-NLS-1$

        // Compose path string used by RupyService
        StringBuffer sb = new StringBuffer();
        for (String key : providerMap.keySet())
        {
            sb.append(key).append(":");
        }
        combinedPath = sb.toString();
    }


    /**
     * Serves chunk data and player info.
     */
    public DataService()
    {
        super();
    }

    @Override
    public String path()
    {
        return combinedPath;
    }

    @Override
    public void filter(Event event) throws Event, Exception
    {

        try
        {
            // Parse query for parameters
            Query query = event.query();
            query.parse();
            String path = query.path();

            // If not a request for L10N, check world/minecraft status
            if (!path.equals("/data/messages"))
            {
                // Ensure JourneyMap and World is loaded
                if (!JourneyMap.getInstance().isMapping())
                {
                    throwEventException(503, Constants.getMessageJMERR02(), event, false);
                }
                else
                {
                    if (Minecraft.getMinecraft().theWorld == null)
                    {
                        throwEventException(503, Constants.getMessageJMERR09(), event, false);
                    }
                }
            }

            // Get since param
            long since = 0;
            Object sinceVal = query.get("images.since");
            if (sinceVal != null)
            {
                try
                {
                    since = Long.parseLong(sinceVal.toString());
                }
                catch (Exception e)
                {
                    JourneyMap.getLogger().warning("Bad value for images.since: " + sinceVal);
                    since = new Date().getTime();
                }
            }

            // Get cached data provider keyed by the path
            Class dpClass = providerMap.get(path);

            // Get data
            Object data = null;

            if (dpClass == AllData.class)
            {
                data = DataCache.instance().getAll(since);
            }
            else if (dpClass == AnimalsData.class)
            {
                data = DataCache.instance().getAnimals(false);
            }
            else if (dpClass == MobsData.class)
            {
                data = DataCache.instance().getMobs(false);
            }
            else if (dpClass == ImagesData.class)
            {
                data = new ImagesData(since);
            }
            else  if (dpClass == MessagesData.class)
            {
                data = DataCache.instance().getMessages(false);
            }
            else if (dpClass == PlayerData.class)
            {
                data = DataCache.instance().getPlayer(false);
            }
            else if (dpClass == PlayersData.class)
            {
                data = DataCache.instance().getPlayers(false);
            }
            else if (dpClass == WorldData.class)
            {
                data = DataCache.instance().getWorld(false);
            }
            else if (dpClass == VillagersData.class)
            {
                data = DataCache.instance().getVillagers(false);
            }
            else if (dpClass == WaypointsData.class)
            {
                Collection<Waypoint> waypoints = DataCache.instance().getWaypoints(false);
                Map<String,Waypoint> wpMap = new HashMap<String, Waypoint>();
                for(Waypoint waypoint : waypoints)
                {
                    wpMap.put(waypoint.getId(), waypoint);
                }
                data = wpMap;
            }

            String dataString = GSON.toJson(data);

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

            jsonData.append(dataString);

            // Finish function call for JsonP if needed
            if (useJsonP)
            {
                jsonData.append(")"); //$NON-NLS-1$
            }

            // Optimize headers for JSONP
            ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);

            // Gzip response
            gzipResponse(event, jsonData.toString());

        }
        catch (Event eventEx)
        {
            throw eventEx;
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe(LogFormatter.toString(t));
            throwEventException(500, Constants.getMessageJMERR12(path), event, true);
        }
    }

}
