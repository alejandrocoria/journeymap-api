/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap;


import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Constants
{
    public static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast(); // or nullsFirst()
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Joiner path = Joiner.on(File.separator).useForNull("");
    private static final String END = null;
    public static String JOURNEYMAP_DIR_LEGACY = "journeyMap";
    public static String JOURNEYMAP_DIR_BACKUP = "journeymap_bak";
    public static String JOURNEYMAP_DIR = "journeymap";
    public static String CONFIG_DIR_LEGACY = path.join(JOURNEYMAP_DIR, "config");
    public static String CONFIG_DIR = path.join(JOURNEYMAP_DIR, "config", JourneyMap.JM_VERSION.toMajorMinorString(), END);
    private static String ICON_DIR = path.join(JOURNEYMAP_DIR, "icon");
    public static String ENTITY_ICON_DIR = path.join(ICON_DIR, "entity", END);
    public static String WAYPOINT_ICON_DIR = path.join(ICON_DIR, "waypoint", END);
    public static String THEME_ICON_DIR = path.join(ICON_DIR, "theme", END);
    public static String CACHE_DIR = path.join(JOURNEYMAP_DIR, "cache", END);
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

    public static List<KeyBinding> initKeybindings()
    {
        CONTROL_KEYNAME_COMBO = "Ctrl,";
        KEYBINDING_CATEGORY = Constants.getString("jm.common.hotkeys_keybinding_category", CONTROL_KEYNAME_COMBO);
        KB_MAP = new KeyBinding("key.journeymap.map_toggle", Keyboard.KEY_J, KEYBINDING_CATEGORY);
        KB_MAP_ZOOMIN = new KeyBinding("key.journeymap.zoom_in", Keyboard.KEY_EQUALS, KEYBINDING_CATEGORY);
        KB_MAP_ZOOMOUT = new KeyBinding("key.journeymap.zoom_out", Keyboard.KEY_MINUS, KEYBINDING_CATEGORY);
        KB_MAP_DAY = new KeyBinding("key.journeymap.day", Keyboard.KEY_LBRACKET, KEYBINDING_CATEGORY);
        KB_MAP_NIGHT = new KeyBinding("key.journeymap.night", Keyboard.KEY_RBRACKET, KEYBINDING_CATEGORY);
        KB_MINIMAP_PRESET = new KeyBinding("key.journeymap.minimap_preset", Keyboard.KEY_BACKSLASH, KEYBINDING_CATEGORY);
        KB_WAYPOINT = new KeyBinding("key.journeymap.create_waypoint", Keyboard.KEY_B, KEYBINDING_CATEGORY);
        return Arrays.asList(KB_MAP, KB_MAP_ZOOMIN, KB_MAP_ZOOMOUT, KB_MAP_DAY, KB_MAP_NIGHT, KB_MINIMAP_PRESET, KB_WAYPOINT);
    }

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
            JourneyMap.getLogger().warn("Couldn't determine locale from game settings, defaulting to " + locale);
        }
        return locale;
    }

    public static String getString(String key)
    {
        String result = I18n.format(key);
        if (result.equals(key))
        {
            JourneyMap.getLogger().warn("Message key not found: " + key);
        }
        return result;
    }

    public static String getString(String key, Object... params)
    {
        String result = I18n.format(key, params);
        if (result.equals(key))
        {
            JourneyMap.getLogger().warn("Message key not found: " + key);
        }
        return result;
    }

    public static String getKeyName(KeyBinding keyBinding)
    {
        return Keyboard.getKeyName(getKeyCode(keyBinding));
    }

    private static int getKeyCode(KeyBinding keyBinding)
    {
        // 1.7.2
        return keyBinding.getKeyCode();

        // 1.6.4
        //return keyBinding.keyCode;
    }

    public static boolean isPressed(KeyBinding keyBinding)
    {
        return keyBinding.isPressed() || Keyboard.isKeyDown(getKeyCode(keyBinding));
    }

    public static boolean safeEqual(String first, String second)
    {
        int result = CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(first, second);
        if (result != 0)
        {
            return false;
        }
        return CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(first, second) == 0;
    }

    public static String getResourcePackNames()
    {
        ResourcePackRepository resourcepackrepository = FMLClientHandler.instance().getClient().getResourcePackRepository();
        String packs = Joiner.on(", ").join(Lists.reverse(resourcepackrepository.getRepositoryEntries()));
        if (Strings.isNullOrEmpty(packs))
        {
            packs = RESOURCE_PACKS_DEFAULT;
        }
        return packs;
    }

    public enum MapType
    {
        day, night, underground
    }

    public enum WorldType
    {
        mp, sp
    }

}
