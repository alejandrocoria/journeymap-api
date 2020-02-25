package journeymap.client.service.webmap.kotlin.routes

import journeymap.client.render.texture.IgnSkin
import net.minecraft.client.Minecraft
import spark.kotlin.RouteHandler
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO


internal fun skinGet(handler: RouteHandler): Any
{
    val uuid = UUID.fromString(handler.params("uuid"))
    val username = Minecraft.getMinecraft().connection?.getPlayerInfo(uuid)?.gameProfile?.name
    val img: BufferedImage

    img = if (username == null)
    {
        BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB)
    }
    else
    {
        IgnSkin.getFaceImage(uuid, username)
    }

    handler.response.raw().contentType = "image/png"
    ImageIO.write(img, "png", handler.response.raw().outputStream)
    handler.response.raw().outputStream.flush()

    return handler.response
}
