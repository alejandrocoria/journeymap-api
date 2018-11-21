/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.texture;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import journeymap.client.io.RegionImageHandler;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Get player face/overlay by IGN lookup. Used for multiplayer and AboutDialog.
 */
public class IgnSkin
{
    private static String ID_LOOKUP_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%s";
    private static String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    /**
     * Blocking.  Use this in a thread.
     *
     * @param playerId the player id, can be null
     * @param username the username
     * @return the buffered image
     */
    public static BufferedImage getFaceImage(UUID playerId, String username)
    {
        BufferedImage face = null;

        if(playerId==null)
        {
            playerId = lookupPlayerId(username);
        }

        // Nice little back-door into fetching player profiles, which includes their skin URL
        GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile(playerId, username));

        try
        {
            MinecraftSessionService mss = Minecraft.getMinecraft().getSessionService();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map =  mss.getTextures(profile, false);
            BufferedImage skinImage = null;
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN))
            {
                MinecraftProfileTexture mpt = map.get(MinecraftProfileTexture.Type.SKIN);
                skinImage = downloadImage(new URL(mpt.getUrl()));
            }
            else
            {
                ResourceLocation resourceLocation = DefaultPlayerSkin.getDefaultSkin(playerId);
                skinImage = TextureCache.getTexture(resourceLocation).getImage();
            }

            face = cropToFace(skinImage);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().warn("Error getting face image for " + username + ": " + e.getMessage());
        }

        return face;
    }

    /**
     * See: https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/skins/2748289-retrieve-your-minecraft-skin-complex
     * @param username
     * @return UUID or null
     */
    private static UUID lookupPlayerId(String username) {
        URL idLookupUrl = null;
        HttpURLConnection conn;
        try
        {
            idLookupUrl = new URL(String.format(ID_LOOKUP_URL, username, Instant.now().getEpochSecond()));
            conn = ((HttpURLConnection) idLookupUrl.openConnection(Minecraft.getMinecraft().getProxy()));
            HttpURLConnection.setFollowRedirects(true);
            conn.setInstanceFollowRedirects(true);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            if (conn.getResponseCode() / 100 == 2) // can't getTexture input stream before response code available
            {
                try (InputStream inputStream = conn.getInputStream()) {
                    final String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    final String id = new JsonParser().parse(json).getAsJsonObject().get("id").getAsString();
                    return UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
                }
            }
            else
            {
                Journeymap.getLogger().debug("Unable to lookup player id: " + idLookupUrl + " : " + conn.getResponseCode());
            }
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error getting player id: " + idLookupUrl + " : " + e.getMessage());
        }

        return null;
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
                img = ImageIO.read(conn.getInputStream());
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

    /**
     * Crop player skin to face, add hat, upscale.
     * @param playerSkin
     * @return
     */
    private static BufferedImage cropToFace(BufferedImage playerSkin)
    {
        BufferedImage result = null;
        if(playerSkin!=null) {
            BufferedImage face = playerSkin.getSubimage(8, 8, 8, 8);

            // Overlay hat if skin has transparency
            if(playerSkin.getColorModel().hasAlpha())
            {
                final Graphics2D g = RegionImageHandler.initRenderingHints(face.createGraphics());
                BufferedImage hat = playerSkin.getSubimage(40, 8, 8, 8);
                g.drawImage(hat, 0, 0, 8, 8, null);
                g.dispose();
            }

            // Upscale to 24x24
            result = new BufferedImage(24, 24, face.getType());
            final Graphics2D g = RegionImageHandler.initRenderingHints(result.createGraphics());
            g.drawImage(face, 0, 0, 24, 24, null);
            g.dispose();
        }
        return result;
    }
}
