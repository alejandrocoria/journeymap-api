package net.techbrew.mcjm;


import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

public class Constants {

    public static String JOURNEYMAP_DIR = "journeyMap" + File.separator; //$NON-NLS-1$
    public static String CUSTOM_DIR = JOURNEYMAP_DIR + "custom" + File.separator; //$NON-NLS-1$
    public static String CACHE_DIR = JOURNEYMAP_DIR + "cache" + File.separator; //$NON-NLS-1$
    public static String DATA_DIR = JOURNEYMAP_DIR + "data" + File.separator; //$NON-NLS-1$
    public static String SP_DATA_DIR = DATA_DIR + WorldType.sp + File.separator;
    public static String MP_DATA_DIR = DATA_DIR + WorldType.mp + File.separator;
    public static String CHUNK_FILE_EXT = "chunk"; //$NON-NLS-1$
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static Locale getLocale() {
        Locale locale = Locale.getDefault();
        try {
            String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
            locale = Locale.forLanguageTag(lang);
        } catch (Exception e) {
            JourneyMap.getLogger().warning("Couldn't determine locale from game settings, defaulting to " + locale);
        }
        return locale;
    }

    public static String getString(String key) {
        return I18n.getStringParams(key);
    }

    public static String getString(String key, Object... params) {
        return I18n.getStringParams(key, params);
    }

    public enum MapType {
        day(0), night(16), underground(0),

        @Deprecated
        OBSOLETE(-1);

        MapType(int offset) {
            _offset = offset;
        }

        private final int _offset;

        public int offset() {
            return _offset;
        }
    }

    ;

    @Deprecated
    public enum CoordType {
        Normal, Cave, Nether, End, Other, OtherCave;

        public static CoordType convert(int dimension) {
            return convert(false, dimension);
        }

        public static CoordType convert(MapType mapType, int dimension) {
            return convert(mapType.equals(MapType.underground), dimension);
        }

        public static CoordType convert(Boolean underground, int dimension) {
            switch (dimension) {
                case 0: {
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

    public enum WorldType {mp, sp}

    ;

    public static String getMessageJMERR00(Object... params) {
        return getString("JMERR00", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR01(Object... params) {
        return getString("JMERR01", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR02(Object... params) {
        return getString("JMERR02", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR03(Object... params) {
        return getString("JMERR03", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR04(Object... params) {
        return getString("JMERR04", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR05(Object... params) {
        return getString("JMERR05", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR06(Object... params) {
        return getString("JMERR06", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR07(Object... params) {
        return getString("JMERR07", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR08(Object... params) {
        return getString("JMERR08", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR09(Object... params) {
        return getString("JMERR09", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR10(Object... params) {
        return getString("JMERR10", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR11(Object... params) {
        return getString("JMERR11", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR12(Object... params) {
        return getString("JMERR12", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR13(Object... params) {
        return getString("JMERR13", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR14(Object... params) {
        return getString("JMERR13", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR15(Object... params) {
        return getString("JMERR15", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR16(Object... params) {
        return getString("JMERR16", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR17(Object... params) {
        return getString("JMERR17", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR18(Object... params) {
        return getString("JMERR18", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR19(Object... params) {
        return getString("JMERR19", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR20(Object... params) {
        return getString("JMERR20", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR21(Object... params) {
        return getString("JMERR21", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR22(Object... params) {
        return getString("JMERR22", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR23(Object... params) {
        return getString("JMERR23", params); //$NON-NLS-1$
    }

    public static String getMessageJMERR24() {
        return getString("JMERR24"); //$NON-NLS-1$
    }

}
