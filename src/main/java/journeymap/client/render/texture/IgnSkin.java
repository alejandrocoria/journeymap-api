package journeymap.client.render.texture;

import journeymap.client.io.RegionImageHandler;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Get player face/overlay by IGN lookup. Used for multiplayer and AboutDialog.
 * TODO: Find a way to go from IGN name to profile hash so the native MinecraftProfileTexture can be used instead.
 */
public class IgnSkin
{
    private static String SKINS = "http://skins.minecraft.net/MinecraftSkins/%s.png";
    private static String DEFAULT = "Herobrine";

    /**
     * Blocks.  Use this in a thread.
     *
     * @param username the username
     * @return the buffered image
     */
    public static BufferedImage downloadSkin(String username)
    {
        BufferedImage img = null;
        HttpURLConnection conn = null;
        try
        {
            String skinPath = String.format(SKINS, StringUtils.stripControlCodes(username));
            img = downloadImage(new URL(skinPath));
            if (img == null)
            {
                skinPath = String.format(SKINS, DEFAULT);
                img = downloadImage(new URL(skinPath));
            }
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().warn("Error getting skin image for " + username + ": " + e.getMessage());
        }
        return img;
    }

    private static BufferedImage downloadImage(URL imageURL)
    {
        BufferedImage img = null;
        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) imageURL.openConnection(Minecraft.getMinecraft().getProxy());
            HttpURLConnection.setFollowRedirects(true);
            conn.setInstanceFollowRedirects(true);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            if (conn.getResponseCode() / 100 == 2) // can't getTexture input stream before response code available
            {
                BufferedImage fullImage = ImageIO.read(conn.getInputStream());
                BufferedImage face = fullImage.getSubimage(8, 8, 8, 8);

                // Overlay hat if skin has transparency
                if(fullImage.getColorModel().hasAlpha())
                {
                    final Graphics2D g = RegionImageHandler.initRenderingHints(face.createGraphics());
                    BufferedImage hat = fullImage.getSubimage(40, 8, 8, 8);
                    g.drawImage(hat, 0, 0, 8, 8, null);
                    g.dispose();
                }

                // Upscale to 24x24
                img = new BufferedImage(24, 24, face.getType());
                final Graphics2D g = RegionImageHandler.initRenderingHints(img.createGraphics());
                g.drawImage(face, 0, 0, 24, 24, null);
                g.dispose();
            }
            else
            {
                Journeymap.getLogger().debug("Bad Response getting image: " + imageURL + " : " + conn.getResponseCode());
            }
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error getting skin image: " + imageURL + " : " + e.getMessage());
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }

        return img;
    }
}
