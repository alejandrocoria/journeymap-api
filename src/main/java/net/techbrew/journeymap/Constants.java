/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Util;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Constants
{
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    public static String JOURNEYMAP_DIR = "journeyMap" + File.separator; //$NON-NLS-1$
    public static String CONFIG_DIR = JOURNEYMAP_DIR + "config" + File.separator; //$NON-NLS-1$
    @Deprecated
    public static String CUSTOM_DIR = JOURNEYMAP_DIR + "custom" + File.separator; //$NON-NLS-1$
    public static String ICON_DIR = JOURNEYMAP_DIR + "icon" + File.separator; //$NON-NLS-1$
    public static String ENTITY_ICON_DIR = ICON_DIR + File.separator + "entity" + File.separator; //$NON-NLS-1$
    public static String WAYPOINT_ICON_DIR = ICON_DIR + File.separator + "waypoint" + File.separator; //$NON-NLS-1$
    public static String CACHE_DIR = JOURNEYMAP_DIR + "cache" + File.separator; //$NON-NLS-1$
    public static String DATA_DIR = JOURNEYMAP_DIR + "data" + File.separator; //$NON-NLS-1$
    public static String SP_DATA_DIR = DATA_DIR + WorldType.sp + File.separator;
    public static String MP_DATA_DIR = DATA_DIR + WorldType.mp + File.separator;
    public static String CONTROL_KEYNAME_COMBO;
    public static String KEYBINDING_CATEGORY;
    public static KeyBinding KB_MAP;
    public static KeyBinding KB_MAP_ZOOMIN;
    public static KeyBinding KB_MAP_ZOOMOUT;
    public static KeyBinding KB_MAP_DAY;
    public static KeyBinding KB_MAP_NIGHT;
    public static KeyBinding KB_MINIMAP_POS;
    public static KeyBinding KB_WAYPOINT;

    public static List<KeyBinding> initKeybindings()
    {
        CONTROL_KEYNAME_COMBO = "Ctrl+";
        KEYBINDING_CATEGORY = Constants.getString("jm.common.hotkeys_keybinding_category", CONTROL_KEYNAME_COMBO);
        KB_MAP = new KeyBinding("key.journeymap.hotkeys_toggle", Keyboard.KEY_J, KEYBINDING_CATEGORY);
        KB_MAP_ZOOMIN = new KeyBinding("key.journeymap.zoom_in", Keyboard.KEY_EQUALS, KEYBINDING_CATEGORY);
        KB_MAP_ZOOMOUT = new KeyBinding("key.journeymap.zoom_out", Keyboard.KEY_MINUS, KEYBINDING_CATEGORY);
        KB_MAP_DAY = new KeyBinding("key.journeymap.day", Keyboard.KEY_LBRACKET, KEYBINDING_CATEGORY);
        KB_MAP_NIGHT = new KeyBinding("key.journeymap.night", Keyboard.KEY_RBRACKET, KEYBINDING_CATEGORY);
        KB_MINIMAP_POS = new KeyBinding("key.journeymap.minimap_position", Keyboard.KEY_BACKSLASH, KEYBINDING_CATEGORY);
        KB_WAYPOINT = new KeyBinding("key.journeymap.create_waypoint", Keyboard.KEY_B, KEYBINDING_CATEGORY);
        return Arrays.asList(KB_MAP, KB_MAP_ZOOMIN, KB_MAP_ZOOMOUT, KB_MAP_DAY, KB_MAP_NIGHT, KB_MINIMAP_POS, KB_WAYPOINT);
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
            JourneyMap.getLogger().warning("Couldn't determine locale from game settings, defaulting to " + locale);
        }
        return locale;
    }

    public static String getString(String key)
    {
        String result = I18n.format(key);
        if (result.equals(key))
        {
            JourneyMap.getLogger().warning("Message key not found: " + key);
        }
        return result;
    }

    public static String getString(String key, Object... params)
    {
        String result = I18n.format(key, params);
        if (result.equals(key))
        {
            JourneyMap.getLogger().warning("Message key not found: " + key);
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

    public static String getMessageJMERR00(Object... params)
    {
        return getString("jm.error.00", params); //$NON-NLS-1$
    }

    ;

    public static String getMessageJMERR01(Object... params)
    {
        return getString("jm.error.01", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR02(Object... params)
    {
        return getString("jm.error.02", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR03(Object... params)
    {
        return getString("jm.error.03", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR04(Object... params)
    {
        return getString("jm.error.04", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR05(Object... params)
    {
        return getString("jm.error.05", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR06(Object... params)
    {
        return getString("jm.error.06", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR07(Object... params)
    {
        return getString("jm.error.07", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR08(Object... params)
    {
        return getString("jm.error.08", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR09(Object... params)
    {
        return getString("jm.error.09", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR10(Object... params)
    {
        return getString("jm.error.10", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR11(Object... params)
    {
        return getString("jm.error.11", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR12(Object... params)
    {
        return getString("jm.error.12", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR13(Object... params)
    {
        return getString("jm.error.13", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR14(Object... params)
    {
        return getString("jm.error.13", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR15(Object... params)
    {
        return getString("jm.error.15", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR16(Object... params)
    {
        return getString("jm.error.16", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR17(Object... params)
    {
        return getString("jm.error.17", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR18(Object... params)
    {
        return getString("jm.error.18", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR19(Object... params)
    {
        return getString("jm.error.19", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR20(Object... params)
    {
        return getString("jm.error.20", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR21(Object... params)
    {
        return getString("jm.error.21", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR22(Object... params)
    {
        return getString("jm.error.22", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR23(Object... params)
    {
        return getString("jm.error.23", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR24()
    {
        return getString("jm.error.24"); //$NON-NLS-1$
    }

    public enum MapType
    {
        day(0), night(16), underground(0),

        @Deprecated
        OBSOLETE(-1);
        private final int _offset;

        MapType(int offset)
        {
            _offset = offset;
        }

        public int offset()
        {
            return _offset;
        }
    }

    @Deprecated
    public enum CoordType
    {
        Normal, Cave, Nether, End, Other, OtherCave;

        public static CoordType convert(int dimension)
        {
            return convert(false, dimension);
        }

        public static CoordType convert(MapType mapType, int dimension)
        {
            return convert(mapType.equals(MapType.underground), dimension);
        }

        public static CoordType convert(Boolean underground, int dimension)
        {
            switch (dimension)
            {
                case 0:
                {
                    return underground ? Cave : Normal;
                }
                case -1:
                    return Nether;
                case 1:
                    return End;
            }
            return underground ? Cave : Normal;
        }
    }

    public enum WorldType
    {
        mp, sp
    }

}
