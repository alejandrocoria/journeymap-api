/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client;


import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.*;

/**
 * Constants and Keybindings... and other stuff that are squatting here for some reason.
 * TODO: The localization stuff should probably be moved, or possibly removed altogether.
 */
public class Constants
{
    /**
     * The constant CASE_INSENSITIVE_NULL_SAFE_ORDER.
     */
    public static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast(); // or nullsFirst()
    /**
     * The constant GMT.
     */
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Joiner path = Joiner.on(File.separator).useForNull("");
    private static final String END = null;
    /**
     * The constant JOURNEYMAP_DIR.
     */
    public static String JOURNEYMAP_DIR = "journeymap";
    /**
     * The constant CONFIG_DIR_LEGACY.
     */
    public static String CONFIG_DIR_LEGACY = path.join(JOURNEYMAP_DIR, "config");
    /**
     * The constant CONFIG_DIR.
     */
    public static String CONFIG_DIR = path.join(JOURNEYMAP_DIR, "config", Journeymap.JM_VERSION.toMajorMinorString(), END);
    /**
     * The constant DATA_DIR.
     */
    public static String DATA_DIR = path.join(JOURNEYMAP_DIR, "data");
    /**
     * The constant SP_DATA_DIR.
     */
    public static String SP_DATA_DIR = path.join(DATA_DIR, WorldType.sp, END);
    /**
     * The constant MP_DATA_DIR.
     */
    public static String MP_DATA_DIR = path.join(DATA_DIR, WorldType.mp, END);
    /**
     * The constant RESOURCE_PACKS_DEFAULT.
     */
    public static String RESOURCE_PACKS_DEFAULT = "Default";

    public static String CATEGORY_ALL;
    public static String CATEGORY_FULLMAP;

    public static KeyBinding KB_FULLSCREEN;
    public static KeyBinding KB_MINIMAP_ZOOMIN;
    public static KeyBinding KB_MINIMAP_ZOOMOUT;
    public static KeyBinding KB_MINIMAP_TYPE;
    public static KeyBinding KB_MINIMAP_PRESET;
    public static KeyBinding KB_MINIMAP_TOGGLE;
    public static KeyBinding KB_CREATE_WAYPOINT;
    public static KeyBinding KB_WAYPOINT_MANAGER;
    public static KeyBinding KB_FULLMAP_OPTIONS_MANAGER;
    public static KeyBinding KB_FULLMAP_ACTIONS_MANAGER;
    public static KeyBinding KB_FULLMAP_PAN_NORTH;
    public static KeyBinding KB_FULLMAP_PAN_SOUTH;
    public static KeyBinding KB_FULLMAP_PAN_EAST;
    public static KeyBinding KB_FULLMAP_PAN_WEST;

    
    private static String ICON_DIR = path.join(JOURNEYMAP_DIR, "icon");
    /**
     * The constant ENTITY_ICON_DIR.
     */
    public static String ENTITY_ICON_DIR = path.join(ICON_DIR, "entity", END);
    /**
     * The constant WAYPOINT_ICON_DIR.
     */
    public static String WAYPOINT_ICON_DIR = path.join(ICON_DIR, "waypoint", END);
    /**
     * The constant THEME_ICON_DIR.
     */
    public static String THEME_ICON_DIR = path.join(ICON_DIR, "theme", END);

    // Network Channel IDs

    /**
     * Initialize the keybindings, return them as a list.
     *
     * @return list
     */
    public static List<KeyBinding> initKeybindings()
    {
        CATEGORY_ALL = Constants.getString("jm.common.hotkeys_keybinding_category");
        CATEGORY_FULLMAP = Constants.getString("jm.common.hotkeys_keybinding_fullscreen_category");

        // Active in-game and Fullscreen
        KB_MINIMAP_ZOOMIN = new KeyBinding("key.journeymap.zoom_in", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_EQUALS, CATEGORY_ALL);
        KB_MINIMAP_ZOOMOUT = new KeyBinding("key.journeymap.zoom_out", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_MINUS, CATEGORY_ALL);
        KB_MINIMAP_TYPE = new KeyBinding("key.journeymap.minimap_type", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_LBRACKET, CATEGORY_ALL);
        KB_MINIMAP_PRESET = new KeyBinding("key.journeymap.minimap_preset", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_BACKSLASH, CATEGORY_ALL);
        KB_CREATE_WAYPOINT = new KeyBinding("key.journeymap.create_waypoint", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_B, CATEGORY_ALL);
        KB_FULLSCREEN = new KeyBinding("key.journeymap.map_toggle_alt", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_J, CATEGORY_ALL);

        // Active in-game or Fullscreen, but shouldn't be treated as conflicts because they have modifiers.
        KB_MINIMAP_TOGGLE = new KeyBinding("key.journeymap.minimap_toggle_alt", KeyConflictContext.GUI, KeyModifier.CONTROL, Keyboard.KEY_J, CATEGORY_ALL);
        KB_WAYPOINT_MANAGER = new KeyBinding("key.journeymap.fullscreen_waypoints", KeyConflictContext.GUI, KeyModifier.CONTROL, Keyboard.KEY_B, CATEGORY_ALL);

        // Active only in Fullscreen
        KB_FULLMAP_PAN_NORTH = new KeyBinding("key.journeymap.fullscreen.north", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_UP, CATEGORY_FULLMAP);
        KB_FULLMAP_PAN_SOUTH = new KeyBinding("key.journeymap.fullscreen.south", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_DOWN, CATEGORY_FULLMAP);
        KB_FULLMAP_PAN_EAST = new KeyBinding("key.journeymap.fullscreen.east", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_RIGHT, CATEGORY_FULLMAP);
        KB_FULLMAP_PAN_WEST = new KeyBinding("key.journeymap.fullscreen.west", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_LEFT, CATEGORY_FULLMAP);
        KB_FULLMAP_OPTIONS_MANAGER = new KeyBinding("key.journeymap.fullscreen_options", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_O, CATEGORY_FULLMAP);
        KB_FULLMAP_ACTIONS_MANAGER = new KeyBinding("key.journeymap.fullscreen_actions", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_A, CATEGORY_FULLMAP);

        return Arrays.asList(
                KB_FULLSCREEN, KB_MINIMAP_ZOOMIN, KB_MINIMAP_ZOOMOUT, KB_MINIMAP_TYPE, KB_MINIMAP_PRESET, KB_MINIMAP_TOGGLE, KB_CREATE_WAYPOINT,
                KB_WAYPOINT_MANAGER, KB_FULLMAP_OPTIONS_MANAGER, KB_FULLMAP_ACTIONS_MANAGER, KB_FULLMAP_PAN_NORTH, KB_FULLMAP_PAN_SOUTH,
                KB_FULLMAP_PAN_EAST, KB_FULLMAP_PAN_WEST
        );
    }

    /**
     * Get the current locale
     *
     * @return locale
     */
    public static Locale getLocale()
    {
        Locale locale = Locale.getDefault();
        try
        {
            String lang = FMLClientHandler.instance().getClient().getLanguageManager().getCurrentLanguage().getLanguageCode();
            locale = new Locale(lang);
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Couldn't determine locale from game settings, defaulting to " + locale);
        }
        return locale;
    }

    /**
     * Get the localized string for a given key.
     *
     * @param key the key
     * @return string
     */
    public static String getString(String key)
    {
        if (FMLClientHandler.instance().getClient() == null)
        {
            return key;
        }

        try
        {
            String result = I18n.format(key);
            if (result.equals(key))
            {
                Journeymap.getLogger().warn("Message key not found: " + key);
            }
            return result;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().warn(String.format("Message key '%s' threw exception: %s", key, t.getMessage()));
            return key;
        }
    }

    /**
     * Get the localized string for a key and parameters.
     *
     * @param key    the key
     * @param params the params
     * @return string
     */
    public static String getString(String key, Object... params)
    {
        if (FMLClientHandler.instance().getClient() == null)
        {
            return String.format("%s (%s)", key, Joiner.on(",").join(params));
        }

        try
        {
            String result = I18n.format(key, params);
            if (result.equals(key))
            {
                Journeymap.getLogger().warn("Message key not found: " + key);
            }
            return result;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().warn(String.format("Message key '%s' threw exception: %s", key, t.getMessage()));
            return key;
        }
    }

    /**
     * Get the key name for a binding.
     *
     * @param keyBinding the key binding
     * @return key name
     */
    public static String getKeyName(KeyBinding keyBinding)
    {
        return Keyboard.getKeyName(keyBinding.getKeyCode());
    }

    /**
     * Safely check two strings for case-insensitive equality.
     *
     * @param first  the first
     * @param second the second
     * @return boolean
     */
    public static boolean safeEqual(String first, String second)
    {
        int result = CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(first, second);
        if (result != 0)
        {
            return false;
        }
        return CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(first, second) == 0;
    }

    /**
     * Get a list of all resource pack names.
     *
     * @return resource packs
     */
    public static List<ResourcePackRepository.Entry> getResourcePacks()
    {
        ArrayList<ResourcePackRepository.Entry> entries = new ArrayList<ResourcePackRepository.Entry>();

        try
        {
            ResourcePackRepository resourcepackrepository = FMLClientHandler.instance().getClient().getResourcePackRepository();
            entries.addAll(resourcepackrepository.getRepositoryEntries());
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("Can't get resource pack names: %s", LogFormatter.toString(t)));
        }
        return entries;
    }

    /**
     * Get a list of all loaded mod names.
     * TODO:  Why did this end up here?
     *
     * @return mod names
     */
    public static String getModNames()
    {
        ArrayList<String> list = new ArrayList<String>();
        for (ModContainer mod : Loader.instance().getActiveModList())
        {
            if (Loader.isModLoaded(mod.getModId()))
            {
                list.add(String.format("%s:%s", mod.getName(), mod.getVersion()));
            }
        }
        Collections.sort(list);
        return Joiner.on(", ").join(list);
    }

    /**
     * Birthday message string.
     *
     * @return the string
     */
    public static String birthdayMessage() {
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int date = today.get(Calendar.DATE);
        if (month == Calendar.JULY && date == 2) {
            return getString("jm.common.birthday", "techbrew");
        }
        if (month == Calendar.SEPTEMBER && date == 21) {
            return getString("jm.common.birthday", "mysticdrew");
        }

        return null;
    }

    /**
     * The enum World type.
     */
    public enum WorldType {
        /**
         * Mp world type.
         */
        mp, /**
         * Sp world type.
         */
        sp
    }


}
