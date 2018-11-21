/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.log;

import journeymap.client.Constants;
import journeymap.client.forge.event.KeyEventHandler;
import journeymap.client.service.WebServer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides messages to both chat GUI and log.
 */
public class ChatLog
{
    /**
     * The constant announcements.
     */
// Announcements
    static final List<TextComponentTranslation> announcements = Collections.synchronizedList(new LinkedList<TextComponentTranslation>());
    /**
     * The constant enableAnnounceMod.
     */
    public static boolean enableAnnounceMod = false;
    private static boolean initialized = false;

    /**
     * Announce chat component.
     *
     * @param chat the chat
     */
    public static void queueAnnouncement(ITextComponent chat)
    {
        TextComponentTranslation wrap = new TextComponentTranslation("jm.common.chat_announcement", new Object[]{chat});
        announcements.add(wrap);
    }

    /**
     * Announce URL with link.
     *
     * @param message the message
     * @param url     the url
     */
    public static void announceURL(String message, String url)
    {
        TextComponentString chat = new TextComponentString(message);
        chat.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        chat.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(url)));
        //chat.getChatStyle().setUnderlined(false);
        queueAnnouncement(chat);
    }

    /**
     * Announce file with link.
     *
     * @param message the message
     * @param file    the file
     */
    public static void announceFile(String message, File file)
    {
        TextComponentString chat = new TextComponentString(message);
        try
        {
            chat.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getCanonicalPath()));
            chat.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(file.getCanonicalPath())));
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Couldn't build ClickEvent for file: " + LogFormatter.toString(e));
        }
        queueAnnouncement(chat);
    }

    /**
     * Queue an announcement to be shown in the UI.
     *
     * @param key   i18n key
     * @param parms message parms (optional)
     */
    public static void announceI18N(String key, Object... parms)
    {
        String text = Constants.getString(key, parms);
        TextComponentString chat = new TextComponentString(text);
        queueAnnouncement(chat);
    }

    /**
     * Queue an announcement to be shown in the UI.
     *
     * @param text the text
     */
    public static void announceError(String text)
    {
        ErrorChat chat = new ErrorChat(text);
        queueAnnouncement(chat);
    }


    /**
     * Show queued announcements in chat and log.
     *
     * @param mc the mc
     */
    public static void showChatAnnouncements(Minecraft mc)
    {

        if (!initialized)
        {
            // Announce mod?
            enableAnnounceMod = Journeymap.getClient().getCoreProperties().announceMod.get();
            if (enableAnnounceMod)
            {
                announceMod();
            }

            // Check for newer version online
            VersionCheck.getVersionIsCurrent();
            initialized = true;
        }

        while (!announcements.isEmpty())
        {
            TextComponentTranslation message = announcements.remove(0);
            if (message != null)
            {
                try
                {
                    mc.ingameGUI.getChatGUI().printChatMessage(message);
                }
                catch (Exception e)
                {
                    Journeymap.getLogger().error("Could not display announcement in chat: " + LogFormatter.toString(e));
                }
                finally
                {
                    Level logLevel = message.getFormatArgs()[0] instanceof ErrorChat ? Level.ERROR : Level.INFO;
                    Journeymap.getLogger().log(logLevel, StringUtils.stripControlCodes(message.getUnformattedComponentText()));
                }
            }
        }
    }

    /**
     * Announce mod.
     *
     * @param forced the forced
     */
    public static void announceMod()
    {
        if (enableAnnounceMod)
        {
            String keyName = KeyEventHandler.INSTANCE.kbFullscreenToggle.getDisplayName();
            if (Journeymap.getClient().getWebMapProperties().enabled.get())
            {
                try
                {
                    WebServer webServer = Journeymap.getClient().getJmServer();
                    String port = webServer.getPort() == 80 ? "" : ":" + Integer.toString(webServer.getPort());
                    String message = Constants.getString("jm.common.webserver_and_mapgui_ready", keyName, port);
                    ChatLog.announceURL(message, "http://localhost" + port); 
                }
                catch (Throwable t)
                {
                    Journeymap.getLogger().error("Couldn't check webserver: " + LogFormatter.toString(t));
                }
            }
            else
            {
                ChatLog.announceI18N("jm.common.mapgui_only_ready", keyName); 
            }

            if (!Journeymap.getClient().getCoreProperties().mappingEnabled.get())
            {
                ChatLog.announceI18N("jm.common.enable_mapping_false_text");
            }
            enableAnnounceMod = false; // Only queueAnnouncement mod once per runtime
        }
    }

    /**
     * Decorator to indicate log level should be ERROR.
     */
    private static class ErrorChat extends TextComponentString
    {

        /**
         * Instantiates a new Error chat.
         *
         * @param text the text
         */
        public ErrorChat(String text)
        {
            super(text);
        }
    }

}
