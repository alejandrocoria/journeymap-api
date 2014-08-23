/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.log;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.server.JMServer;
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
    // Announcements
    static final List<ChatComponentTranslation> announcements = Collections.synchronizedList(new LinkedList<ChatComponentTranslation>());
    public static boolean enableAnnounceMod = false;
    private static boolean initialized = false;

    /**
     * Announce chat component.
     *
     * @param chat
     */
    public static void queueAnnouncement(IChatComponent chat)
    {
        ChatComponentTranslation wrap = new ChatComponentTranslation("jm.common.chat_announcement", new Object[]{chat});
        announcements.add(wrap);
    }

    /**
     * Announce URL with link.
     *
     * @param message
     * @param url
     */
    public static void announceURL(String message, String url)
    {
        ChatComponentText chat = new ChatComponentText(message);
        chat.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        chat.getChatStyle().setUnderlined(Boolean.valueOf(true));
        queueAnnouncement(chat);
    }

    /**
     * Announce file with link.
     *
     * @param message
     * @param file
     */
    public static void announceFile(String message, File file)
    {
        ChatComponentText chat = new ChatComponentText(message);
        try
        {
            chat.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getCanonicalPath()));
            chat.getChatStyle().setUnderlined(Boolean.valueOf(true));
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn("Couldn't build ClickEvent for file: " + LogFormatter.toString(e));
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
        ChatComponentText chat = new ChatComponentText(text);
        queueAnnouncement(chat);
    }

    /**
     * Queue an announcement to be shown in the UI.
     *
     * @param text
     */
    public static void announceError(String text)
    {
        ErrorChat chat = new ErrorChat(text);
        queueAnnouncement(chat);
    }


    /**
     * Show queued announcements in chat and log.
     *
     * @param mc
     */
    public static void showChatAnnouncements(Minecraft mc)
    {

        if (!initialized)
        {
            // Announce mod?
            enableAnnounceMod = JourneyMap.getCoreProperties().announceMod.get();
            announceMod(false);

            // Check for newer version online
            VersionCheck.getVersionIsCurrent();
            initialized = true;
        }

        while (!announcements.isEmpty())
        {
            ChatComponentTranslation message = announcements.remove(0);
            if (message != null)
            {
                try
                {
                    mc.ingameGUI.getChatGUI().printChatMessage(message);
                }
                catch (Exception e)
                {
                    JourneyMap.getLogger().error("Could not display announcement in chat: " + LogFormatter.toString(e));
                }
                finally
                {
                    Level logLevel = message.getFormatArgs()[0] instanceof ErrorChat ? Level.ERROR : Level.INFO;
                    JourneyMap.getLogger().log(logLevel, StringUtils.stripControlCodes(message.getUnformattedTextForChat()));
                }
            }
        }
    }

    public static void announceMod(boolean forced)
    {
        if (enableAnnounceMod)
        {
            ChatLog.announceI18N("jm.common.ready", JourneyMap.MOD_NAME); //$NON-NLS-1$
            if (JourneyMap.getWebMapProperties().enabled.get())
            {
                try
                {
                    JMServer jmServer = JourneyMap.getInstance().getJmServer();
                    String keyName = Constants.getKeyName(Constants.KB_MAP);
                    String port = jmServer.getPort() == 80 ? "" : ":" + Integer.toString(jmServer.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
                    String message = Constants.getString("jm.common.webserver_and_mapgui_ready", keyName, port); //$NON-NLS-1$
                    ChatLog.announceURL(message, "http://localhost" + port); //$NON-NLS-1$
                }
                catch(Throwable t)
                {
                    JourneyMap.getLogger().error("Couldn't check webserver: " + LogFormatter.toString(t));
                }
            }
            else
            {
                String keyName = Constants.getKeyName(Constants.KB_MAP); // Should be KeyCode
                ChatLog.announceI18N("jm.common.mapgui_only_ready", keyName); //$NON-NLS-1$
            }
            enableAnnounceMod = false; // Only queueAnnouncement mod once per runtime
        }
    }

    /**
     * Decorator to indicate log level should be ERROR.
     */
    private static class ErrorChat extends ChatComponentText
    {

        public ErrorChat(String text)
        {
            super(text);
        }
    }

}
