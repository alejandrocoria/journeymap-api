/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.dialog;

import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.URLEncoder;


/**
 * The type Fullscreen actions.
 */
public class FullscreenActions
{
    /**
     * The Patreon logo.
     */
    protected TextureImpl patreonLogo = TextureCache.getTexture(TextureCache.Patreon);

    /**
     * Launch localhost.
     */
    public static void launchLocalhost()
    {
        String url = "http://localhost:" + Journeymap.getClient().getWebMapProperties().port.get();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (IOException e)
        {
            Journeymap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
        }
    }

    /**
     * Launch patreon.
     */
    public static void launchPatreon()
    {
        String url = "http://patreon.com/techbrew";
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (IOException e)
        {
            Journeymap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
        }
    }

    /**
     * Launch the JourneyMap website in the native OS.
     *
     * @param path the path
     */
    public static void launchWebsite(String path)
    {
        String url = Journeymap.WEBSITE_URL + path;
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Could not launch browser with URL: " + url, LogFormatter.toString(e)); //$NON-NLS-1$
        }
    }

    /**
     * Suggest a tweet message
     *
     * @param message the message
     */
    public static void tweet(String message)
    {
        String path = null;
        try
        {
            path = "http://twitter.com/home/?status=@JourneyMapMod+" + URLEncoder.encode(message, "UTF-8");
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(path));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Open the Discord server invite.
     */
    public static void discord()
    {
        String path = null;
        try
        {
            path = "https://discord.gg/eP8gE69";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(path));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Launch the download website in the native OS.
     */
    public static void launchDownloadWebsite()
    {
        String url = VersionCheck.getDownloadUrl();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Could not launch browser with URL: " + url, LogFormatter.toString(e)); //$NON-NLS-1$
        }
    }
}
