package journeymap.client.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import journeymap.client.api.display.Context
import journeymap.client.api.display.PolygonOverlay
import journeymap.client.api.impl.ClientAPI
import journeymap.client.cartography.color.RGB
import journeymap.client.render.draw.DrawPolygonStep
import journeymap.client.render.draw.OverlayDrawStep
import spark.kotlin.RouteHandler


private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()


internal fun polygonsGet(handler: RouteHandler): Any
{
    val data = mutableListOf<Any>()
    val steps = mutableListOf<OverlayDrawStep>()
    val uiState = ClientAPI.INSTANCE.getUIState(Context.UI.Fullscreen)

    ClientAPI.INSTANCE.getDrawSteps(steps, uiState!!)

    for (step in steps)
    {
        if (step is DrawPolygonStep)
        {
            val polygon = step.overlay as PolygonOverlay
            val points = mutableListOf<Map<String, Int>>()

            polygon.outerArea.points.forEach { point ->
                points.add(
                    mapOf(
                        "x" to point.x,
                        "y" to point.y,
                        "z" to point.z
                    )
                )
            }

            val holes = mutableListOf<MutableList<Map<String, Int>>>()

            if (polygon.holes != null)
            {
                for (hole in polygon.holes)
                {
                    val holePoints = mutableListOf<Map<String, Int>>()

                    for (holePoint in hole.points)
                    {
                        holePoints.add(
                            mapOf(
                                "x" to holePoint.x,
                                "y" to holePoint.y,
                                "z" to holePoint.z
                            )
                        )
                    }

                    holes.add(holePoints)
                }
            }

            data.add(
                mapOf(
                    "fillColor" to RGB.toHexString(polygon.shapeProperties.fillColor),
                    "fillOpacity" to polygon.shapeProperties.fillOpacity,
                    "strokeColor" to RGB.toHexString(polygon.shapeProperties.strokeColor),
                    "strokeOpacity" to polygon.shapeProperties.strokeOpacity,
                    "strokeWidth" to polygon.shapeProperties.strokeWidth,

                    "holes" to holes,
                    "points" to points
                )
            )
        }
    }

    handler.response.raw().contentType = "application/json"
    return GSON.toJson(data)
}
