package net.techbrew.journeymap.log;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Provides messages to both chat GUI and log.
 */
public class ChatLog {

    // Announcements
    static final List<Chat> announcements = Collections.synchronizedList(new LinkedList<Chat>());

    /**
     * Announce chat component.
     * @param chat
     */
    public static void queueAnnouncement(Chat chat) {
        chat.text = Constants.getString("JourneyMap.chat_announcement", new Object[] {chat.text});
        announcements.add(chat);
    }

    /**
     * Announce URL with link.
     * @param message
     * @param url
     */
    public static void announceURL(String message, String url) {
        // Clickable text doesn't exist yet
        queueAnnouncement(new Chat(message));
    }

    /**
     * Announce file with link.
     * @param message
     * @param file
     */
    public static void announceFile(String message, File file) {
        // Clickable text doesn't exist yet
        queueAnnouncement(new FileChat(message, file));
    }

    /**
     * Queue an announcement to be shown in the UI.
     * @param key i18n key
     * @param parms message parms (optional)
     */
    public static void announceI18N(String key, Object... parms) {
        String text = Constants.getString(key, parms);
        queueAnnouncement(new Chat(text));
    }

    /**
     * Queue an announcement to be shown in the UI.
     * @param text
     */
    public static void announceError(String text) {
        Chat chat = new Chat(text);
        chat.logLevel = Level.SEVERE;
        queueAnnouncement(chat);
    }


    /**
     * Show queued announcements in chat and log.
     * @param mc
     */
    public static void showChatAnnouncements(Minecraft mc) {
        while(!announcements.isEmpty()) {
            Chat message = announcements.remove(0);
            if(message!=null) {
                try {
                    mc.ingameGUI.getChatGUI().printChatMessage(message.text);
                    if(message instanceof FileChat) {
                        FileHandler.open(((FileChat) message).file);
                    }
                } catch(Exception e){
                    JourneyMap.getLogger().severe("Could not display announcement in chat: " + LogFormatter.toString(e));
                } finally {
                    JourneyMap.getLogger().log(message.logLevel, StringUtils.stripControlCodes(message.text));
                }
            }
        }
    }

    /**
     * Decorator class for chat message
     */
    private static class Chat {
        Level logLevel = Level.INFO;
        String text;
        public Chat(String text) {
            this.text = text;
        }
    }

    /**
     * Decorator to open file when displayed
     */
    private static class FileChat extends Chat {
        final File file;
        public FileChat(String text, File file) {
            super(text);
            this.file = file;
        }
    }
}
