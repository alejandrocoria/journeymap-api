package journeymap.client.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import journeymap.client.service.webmap.kotlin.enums.WebmapStatus
import journeymap.client.ui.minimap.MiniMap
import journeymap.common.Journeymap
import net.minecraft.client.Minecraft
import spark.kotlin.RouteHandler


private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()


internal fun statusGet(handler: RouteHandler): Any
{
    val data: MutableMap<String, Any> = mutableMapOf()

    var status = when
    {
        Minecraft.getMinecraft().world == null -> WebmapStatus.NO_WORLD
        !Journeymap.getClient().isMapping      -> WebmapStatus.STARTING

        else                                   -> WebmapStatus.READY
    }

    if (status == WebmapStatus.READY)
    {
        val mapState = MiniMap.state()

        data["mapType"] = mapState.mapType.name()

        val allowedMapTypes: Map<String, Boolean> = mapOf(
            "cave" to (mapState.isCaveMappingAllowed && mapState.isCaveMappingEnabled),
            "surface" to mapState.isSurfaceMappingAllowed,
            "topo" to mapState.isTopoMappingAllowed
        )

        if (allowedMapTypes.filterValues { it }.isEmpty())
        {
            status = WebmapStatus.DISABLED
        }

        data["allowedMapTypes"] = allowedMapTypes
    }

    data["status"] = status.status

    return GSON.toJson(data)
}
