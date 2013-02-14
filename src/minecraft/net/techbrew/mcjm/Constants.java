package net.techbrew.mcjm;

import java.io.File;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import net.minecraft.client.Minecraft;


public class Constants {
	
	public static String JOURNEYMAP_DIR = "journeyMap" + File.separator; //$NON-NLS-1$
	public static String LIB_DIR = JOURNEYMAP_DIR + "lib" + File.separator; //$NON-NLS-1$
	public static String DATA_DIR = JOURNEYMAP_DIR + "data" + File.separator; //$NON-NLS-1$
	public static String SP_DATA_DIR = DATA_DIR + WorldType.sp + File.separator;
	public static String MP_DATA_DIR = DATA_DIR + WorldType.mp + File.separator;
	public static String CHUNK_FILE_EXT = "chunk"; //$NON-NLS-1$
	
	private static final String BUNDLE_NAME = "net.techbrew.mcjm.messages"; //$NON-NLS-1$
	
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	
	public static Locale getLocale() {
		String language = Minecraft.getMinecraft().gameSettings.language;
		Locale locale = null;
		if(language==null) {
			locale = Locale.getDefault();
		} else if(language.equals("en_PT")) {
			locale = new Locale("en", "PT");
		} else {
			locale = new Locale(language);
		}
		return locale;
	}

	public static ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle(BUNDLE_NAME, getLocale());
	}
	
	public static Set<String> getBundleKeys() {
		return getResourceBundle().keySet();
	}
	
	public static void refreshBundle() {
		JourneyMap.getLogger().info("Clearing resource bundle cache");
		getResourceBundle().clearCache();
	}
	
	public static String getString(String key) {
		return getString(key, getResourceBundle());
	}
	
	public static String getString(String key, ResourceBundle bundle) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(getResourceBundle().getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
	
	public enum MapType {
		day(0), night(16), nightAndDay(0), underground(0);
		MapType(int offset) {
			_offset = offset;
		}
		private int _offset;
		public int offset() {
			return _offset;
		}
		
		public static final EnumSet<MapType> DayOrNight = EnumSet.of(MapType.day, MapType.night, MapType.nightAndDay);
	};
	
	public enum CoordType {
		Normal, Cave, Nether, End, Other, OtherCave;
		
		public static CoordType convert(int wp) {
			return convert(false, wp);
		}
		
		public static CoordType convert(MapType mapType, int wp) {
			return convert(mapType.equals(MapType.underground), wp);
		}
		
		public static CoordType convert(Boolean underground, int wp) {
			switch(wp) {
				case 0  : {
					return underground ? Cave : Normal;
				}
				case -1 : return Nether;
				case 1 : return End;
			}
			return underground ? Cave : Normal;
		}
	}
	
	public enum WorldType {mp, sp};
	
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
