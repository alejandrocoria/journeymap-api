package journeymap.client.service.webmap.kotlin.routes

import journeymap.client.properties.WebMapProperties
import journeymap.common.Journeymap
import journeymap.common.properties.config.BooleanField
import spark.kotlin.RouteHandler


val webMapProperties: WebMapProperties = Journeymap.getClient().webMapProperties
var propertiesMap: Map<String, BooleanField>? = null


internal fun propertiesGet(handler: RouteHandler): Any
{
    handler.response.raw().contentType = "application/json"
    return Journeymap.getClient().webMapProperties.toJsonString(true)
}


internal fun propertiesPost(handler: RouteHandler): Any
{
    if (propertiesMap == null || propertiesMap!!.isEmpty())
    {
        val properties: WebMapProperties = Journeymap.getClient().webMapProperties
        val propMap = mutableMapOf<String, BooleanField>()

        propMap["showCaves"] = properties.showCaves
        propMap["showEntityNames"] = properties.showEntityNames
        propMap["showGrid"] = properties.showGrid
        propMap["showSelf"] = properties.showSelf
        propMap["showWaypoints"] = properties.showWaypoints

        propertiesMap = propMap.toMap()
    }

    for (key in handler.queryMap().toMap().keys)
    {
        if (key in propertiesMap!!)
        {
            (
                    propertiesMap!![key] ?: error("Properties value for $key is null")
                    ).set(handler.queryMap(key).booleanValue())
        }
    }

    return webMapProperties.save()
}