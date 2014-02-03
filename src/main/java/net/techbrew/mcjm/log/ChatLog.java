package net.techbrew.mcjm.log;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by mwoodman on 2/2/14.
 */
public class ChatLog {

    // Announcements
    static final List<IChatComponent> announcements = Collections.synchronizedList(new LinkedList<IChatComponent>());

    public static int pending() {
        return announcements.size();
    }

    /**
     * Announce webserver status with link.
     * @param pos
     * @param message
     * @param portStr
     */
    public static void announceWebserver(int pos, String message, String portStr) {
        ChatComponentText chatcomponenttext = new ChatComponentText(message);
        chatcomponenttext.func_150256_b().func_150241_a(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://localhost" + portStr));
        announcements.add(pos, chatcomponenttext);
        JourneyMap.getLogger().info(message);
    }

    /**
     * Queue an announcement to be shown in the UI.
     * @param key i18n key
     * @param parms message parms (optional)
     */
    public static void announceI18N(String key, Object... parms) {
        String text = Constants.getString(key, parms);
        announceText(text, announcements.size() - 1, Level.INFO);
    }

    /**
     * Queue an announcement to be shown in the UI.
     * @param pos position to insert announcement.
     * @param key i18n key
     * @param parms message parms (optional)
     */
    public static void announceI18N(int pos, String key, Object... parms) {
        String text = Constants.getString(key, parms);
        announceText(text, pos, Level.INFO);
    }

    /**
     * Queue an announcement to be shown in the UI.
     * @param message
     */
    public static void announceError(String message) {
        announceText(message, announcements.size()-1, Level.SEVERE);
    }

    /**
     * Queue an announcement to be shown in the UI.
     * @param message
     */
    public static void announceText(String message, int pos, Level logLevel) {
        announcements.add(pos, new ChatComponentText(message));
        JourneyMap.getLogger().log(logLevel, message.toString());
    }

    public static void showChatAnnouncements(Minecraft mc) {
        while(!announcements.isEmpty()) {
            IChatComponent message = announcements.remove(0);
            try {
                mc.ingameGUI.func_146158_b().func_146227_a(message);
            } catch(Exception e){
                JourneyMap.getLogger().severe("Could not display announcement in chat: " + LogFormatter.toString(e));
            }
        }
    }

}
