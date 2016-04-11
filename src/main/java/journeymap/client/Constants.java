/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client;


import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.settings.KeyBinding;
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
    public static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast(); // or nullsFirst()
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Joiner path = Joiner.on(File.separator).useForNull("");
    private static final String END = null;
    public static String JOURNEYMAP_DIR = "journeymap";
    public static String CONFIG_DIR_LEGACY = path.join(JOURNEYMAP_DIR, "config");
    public static String CONFIG_DIR = path.join(JOURNEYMAP_DIR, "config", Journeymap.JM_VERSION.toMajorMinorString(), END);
    public static String DATA_DIR = path.join(JOURNEYMAP_DIR, "data");
    public static String SP_DATA_DIR = path.join(DATA_DIR, WorldType.sp, END);
    public static String MP_DATA_DIR = path.join(DATA_DIR, WorldType.mp, END);
    public static String RESOURCE_PACKS_DEFAULT = "Default";
    public static String CONTROL_KEYNAME_COMBO;
    public static String KEYBINDING_CATEGORY;
    public static KeyBinding KB_MAP;
    public static KeyBinding KB_MAP_ZOOMIN;
    public static KeyBinding KB_MAP_ZOOMOUT;
    public static KeyBinding KB_MAP_DAY;
    public static KeyBinding KB_MAP_NIGHT;
    public static KeyBinding KB_MINIMAP_PRESET;
    public static KeyBinding KB_WAYPOINT;
    private static String ICON_DIR = path.join(JOURNEYMAP_DIR, "icon");
    public static String ENTITY_ICON_DIR = path.join(ICON_DIR, "entity", END);
    public static String WAYPOINT_ICON_DIR = path.join(ICON_DIR, "waypoint", END);
    public static String THEME_ICON_DIR = path.join(ICON_DIR, "theme", END);

    // Network Channel IDs

    /**
     * Initialize the keybindings, return them as a list.
     *
     * @return
     */
    public static List<KeyBinding> initKeybindings()
    {
        CONTROL_KEYNAME_COMBO = "Ctrl,";
        KEYBINDING_CATEGORY = Constants.getString("jm.common.hotkeys_keybinding_category", CONTROL_KEYNAME_COMBO);
        KB_MAP = new KeyBinding("key.journeymap.map_toggle", Keyboard.KEY_J, KEYBINDING_CATEGORY);
        KB_MAP_ZOOMIN = new KeyBinding("key.journeymap.zoom_in", Keyboard.KEY_EQUALS, KEYBINDING_CATEGORY);
        KB_MAP_ZOOMOUT = new KeyBinding("key.journeymap.zoom_out", Keyboard.KEY_MINUS, KEYBINDING_CATEGORY);
        KB_MAP_DAY = new KeyBinding("key.journeymap.minimap_type", Keyboard.KEY_LBRACKET, KEYBINDING_CATEGORY);
        KB_MAP_NIGHT = new KeyBinding("key.journeymap.minimap_type", Keyboard.KEY_RBRACKET, KEYBINDING_CATEGORY);
        KB_MINIMAP_PRESET = new KeyBinding("key.journeymap.minimap_preset", Keyboard.KEY_BACKSLASH, KEYBINDING_CATEGORY);
        KB_WAYPOINT = new KeyBinding("key.journeymap.create_waypoint", Keyboard.KEY_B, KEYBINDING_CATEGORY);
        return Arrays.asList(KB_MAP, KB_MAP_ZOOMIN, KB_MAP_ZOOMOUT, KB_MAP_DAY, KB_MAP_NIGHT, KB_MINIMAP_PRESET, KB_WAYPOINT);
    }

    /**
     * Get the current locale
     *
     * @return
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
     * @param key
     * @return
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
     * @param key
     * @param params
     * @return
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
     * @param keyBinding
     * @return
     */
    public static String getKeyName(KeyBinding keyBinding)
    {
        return Keyboard.getKeyName(keyBinding.getKeyCode());
    }

    /**
     * Safely check two strings for case-insensitive equality.
     *
     * @param first
     * @param second
     * @return
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
     * TODO:  Why did this end up here?
     *
     * @return
     */
    public static String getResourcePackNames()
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

        String packs;
        if (entries.isEmpty())
        {
            packs = RESOURCE_PACKS_DEFAULT;
        }
        else
        {
            ArrayList<String> entryStrings = new ArrayList<String>(entries.size());
            for (ResourcePackRepository.Entry entry : entries)
            {
                entryStrings.add(entry.toString());
            }
            Collections.sort(entryStrings);
            packs = Joiner.on(", ").join(entryStrings);
        }
        return packs;
    }

    /**
     * Get a list of all loaded mod names.
     * TODO:  Why did this end up here?
     *
     * @return
     */
    public static String getModNames()
    {
        ArrayList<String> list = new ArrayList<String>();
        for (ModContainer mod : Loader.instance().getModList())
        {
            if (Loader.isModLoaded(mod.getModId()))
            {
                list.add(String.format("%s:%s", mod.getName(), mod.getVersion()));
            }
        }
        Collections.sort(list);
        return Joiner.on(", ").join(list);
    }

    public enum WorldType
    {
        mp, sp
    }


}
