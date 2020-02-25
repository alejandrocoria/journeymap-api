package journeymap.client.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import journeymap.client.data.DataCache
import journeymap.client.data.ImagesData
import journeymap.client.model.Waypoint
import journeymap.common.Journeymap
import org.apache.logging.log4j.Logger
import spark.kotlin.RouteHandler


private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
private val logger: Logger = Journeymap.getLogger("webmap/routes/data")


val dataTypesRequiringSince = listOf<String>("all", "images")


internal fun dataGet(handler: RouteHandler): Any
{
    val since: Long? = handler.queryMap("images.since").longValue()
    val type = handler.params("type")

    if (type in dataTypesRequiringSince && since == null)
    {
        logger.warn("Data type '$type' requested without 'images.since' parameter")
        handler.status(400)
        return "Data type '$type' requires 'images.since' parameter."
    }

    val data: Any? = when (type)
    {
        "all"       -> DataCache.INSTANCE.getAll(since!!)
        "animals"   -> DataCache.INSTANCE.getAnimals(false)
        "mobs"      -> DataCache.INSTANCE.getMobs(false)
        "images"    -> ImagesData(since!!)
        "messages"  -> DataCache.INSTANCE.getMessages(false)
        "player"    -> DataCache.INSTANCE.getPlayer(false)
        "players"   -> DataCache.INSTANCE.getPlayers(false)
        "world"     -> DataCache.INSTANCE.getWorld(false)
        "villagers" -> DataCache.INSTANCE.getVillagers(false)
        "waypoints" ->
        {
            val waypoints: Collection<Waypoint> = DataCache.INSTANCE.getWaypoints(false)
            val wpMap = mutableMapOf<String, Waypoint>()

            for (waypoint in waypoints)
            {
                wpMap[waypoint.id] = waypoint
            }

            wpMap.toMap()
        }

        else        -> null
    }

    if (data == null)
    {
        logger.warn("Unknown data type '$type'")
        handler.status(400)
        return "Unknown data type '$type'"
    }

    handler.response.raw().contentType = "application/json"
    return GSON.toJson(data)
}
