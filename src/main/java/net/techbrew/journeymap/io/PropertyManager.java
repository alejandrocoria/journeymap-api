package net.techbrew.journeymap.io;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.properties.*;
import net.techbrew.journeymap.ui.minimap.DisplayVars;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Deprecated
public class PropertyManager
{

    @Deprecated
    public static final String FILE_NAME = "journeyMap.properties"; //$NON-NLS-1$

    private static class Holder
    {
        private static final PropertyManager INSTANCE = new PropertyManager();
    }

    public static PropertyManager getInstance()
    {
        return Holder.INSTANCE;
    }

    private final SortedProperties properties;

    private Boolean writeNeeded = false;

    @Deprecated
    public enum Key
    {

        // MAPGUI_ENABLED(Boolean.class, "mapgui_enabled", true), //$NON-NLS-1$

        WEBSERVER_ENABLED(Boolean.class, "webserver_enabled", true), //$NON-NLS-1$
        WEBSERVER_PORT(Integer.class, "webserver_port", 8080), //$NON-NLS-1$
        CHUNK_OFFSET(Integer.class, "chunk_offset", 5), //$NON-NLS-1$
        BROWSER_POLL(Integer.class, "browser_poll", 1900), //$NON-NLS-1$
        //UPDATETIMER_PLAYER(Integer.class,"update_timer_entities", 1000), //$NON-NLS-1$
        UPDATETIMER_CHUNKS(Integer.class, "update_timer_chunks", 2000), //$NON-NLS-1$
        LOGGING_LEVEL(String.class, "logging_level", "INFO"), //$NON-NLS-1$  //$NON-NLS-2$
        CAVE_LIGHTING(Boolean.class, "render_cavelighting_enabled", true), //$NON-NLS-1$
        ANNOUNCE_MODLOADED(Boolean.class, "announce_modloaded", true), //$NON-NLS-1$
        UPDATE_CHECK_ENABLED(Boolean.class, "update_check_enabled", true), //$NON-NLS-1$
        NATIVE_WAYPOINTS_ENABLED(Boolean.class, "native_waypoints_enabled", true), //$NON-NLS-1$

        PREF_SHOW_CAVES(Boolean.class, "preference_show_caves", true), //$NON-NLS-1$
        PREF_SHOW_MOBS(Boolean.class, "preference_show_mobs", true), //$NON-NLS-1$
        PREF_SHOW_ANIMALS(Boolean.class, "preference_show_animals", true), //$NON-NLS-1$
        PREF_SHOW_VILLAGERS(Boolean.class, "preference_show_villagers", true), //$NON-NLS-1$
        PREF_SHOW_PETS(Boolean.class, "preference_show_pets", true), //$NON-NLS-1$
        PREF_SHOW_PLAYERS(Boolean.class, "preference_show_players", true), //$NON-NLS-1$
        PREF_SHOW_WAYPOINTS(Boolean.class, "preference_show_waypoints", true), //$NON-NLS-1$
        PREF_SHOW_GRID(Boolean.class, "preference_show_grid", true), //$NON-NLS-1$

        PREF_FONTSCALE(Double.class, "preference_fontscale", 1.0), //$NON-NLS-1$
        PREF_FORCEUNICODE(Boolean.class, "preference_forceunicode", false), //$NON-NLS-1$

        PREF_SHOW_MINIMAP(Boolean.class, "preference_show_minimap", true), //$NON-NLS-1$

        PREF_MINIMAP_SHAPE(String.class, "preference_minimap_shape", DisplayVars.Shape.SmallSquare.name()), //$NON-NLS-1$
        PREF_MINIMAP_POSITION(String.class, "preference_minimap_position", DisplayVars.Position.TopRight.name()), //$NON-NLS-1$
        PREF_MINIMAP_FONTSCALE(Double.class, "preference_minimap_fontscale", 1.0), //$NON-NLS-1$
        PREF_MINIMAP_SHOWFPS(Boolean.class, "preference_minimap_showfps", false), //$NON-NLS-1$
        PREF_MINIMAP_FORCEUNICODE(Boolean.class, "preference_minimap_forceunicode", false), //$NON-NLS-1$
        PREF_MINIMAP_HOTKEYS(Boolean.class, "preference_minimap_hotkeys", true), //$NON-NLS-1$

        ;
        private final String property;
        private final String defaultValue;
        private final Class type;

        private Key(Class type, String property, Object defaultValue)
        {
            this.type = type;
            this.property = property;
            this.defaultValue = defaultValue.toString();
        }

        @Deprecated
        public String getProperty()
        {
            return property;
        }

        @Deprecated
        String getDefault()
        {
            return defaultValue;
        }

        @Deprecated
        public static Key lookup(String propName)
        {
            for (Key key : Key.values())
            {
                if (key.getProperty().equals(propName))
                {
                    return key;
                }
            }
            return null;
        }
    }


    private String getString(Key key)
    {
        return properties.getProperty(key.getProperty());
    }

    private Integer getInteger(Key key)
    {
        return Integer.parseInt(properties.getProperty(key.getProperty()));
    }

    private Double getDouble(Key key)
    {
        return Double.parseDouble(properties.getProperty(key.getProperty()));
    }

    private Boolean getBoolean(Key key)
    {
        return Boolean.parseBoolean(properties.getProperty(key.getProperty()));
    }

    private void setProperty(Key key, Object value)
    {
        Object old = properties.getProperty(key.getProperty());
        if (old == null || !old.equals(value))
        {
            properties.setProperty(key.getProperty(), value.toString());
            writeToFile();
            JourneyMap.getLogger().fine("Property changed: " + key.getProperty() + "=" + value);
        }
        else
        {
            JourneyMap.getLogger().fine("Property unchanged: " + key.getProperty() + "=" + value);
        }
    }

    private static String getStringProp(Key key)
    {
        return getInstance().getString(key);
    }

    private static Integer getIntegerProp(Key key)
    {
        return getInstance().getInteger(key);
    }

    private static Boolean getBooleanProp(Key key)
    {
        return getInstance().getBoolean(key);
    }

    private static Double getDoubleProp(Key key)
    {
        return getInstance().getDouble(key);
    }

    private static Boolean toggle(Key key)
    {
        boolean flip = !getInstance().getBoolean(key);
        set(key, flip);
        return flip;
    }

    private static void set(Key key, Boolean value)
    {
        getInstance().setProperty(key, value);
    }

    private static void set(Key key, Integer value)
    {
        getInstance().setProperty(key, value);
    }

    private static void set(Key key, Double value)
    {
        getInstance().setProperty(key, value);
    }

    private static void set(Key key, String value)
    {
        getInstance().setProperty(key, value);
    }

    /**
     * Get a normalized, type-safe view of the properties.
     *
     * @return
     */
    private Map<String, Object> getProperties()
    {
        HashMap<String, Object> map = new HashMap<String, Object>(properties.size());
        for (Key key : Key.values())
        {
            if (key.type.equals(Boolean.class))
            {
                map.put(key.getProperty(), getBoolean(key));
            }
            else
            {
                if (key.type.equals(Integer.class))
                {
                    map.put(key.getProperty(), getInteger(key));
                }
                else
                {
                    if (key.type.equals(Double.class))
                    {
                        map.put(key.getProperty(), getDouble(key));
                    }
                    else
                    {
                        map.put(key.getProperty(), getString(key));
                    }
                }
            }
        }
        return map;
    }

    private SortedProperties getDefaultProperties()
    {
        SortedProperties defaults = new SortedProperties();
        for (Key key : Key.values())
        {
            defaults.put(key.getProperty(), key.getDefault());
        }
        return defaults;
    }

    private PropertyManager()
    {
        properties = new SortedProperties();
        readFromFile();
        Properties defaults = getDefaultProperties();
        for (Object key : defaults.keySet())
        {
            if (!properties.containsKey(key))
            {
                properties.put(key, defaults.get(key));
                writeNeeded = true;
            }
        }
        if (writeNeeded)
        {
            writeToFile();
        }
    }

    private File getFile()
    {
        File propFile = new File(FileHandler.getJourneyMapDir(), FILE_NAME);
        return propFile;
    }

    private void readFromFile()
    {
        synchronized (properties)
        {
            File propFile = getFile();
            if (!propFile.exists())
            {
                JourneyMap.getLogger().fine("Legacy property file doesn't exist: " + propFile.getAbsolutePath()); //$NON-NLS-1$
                return;
            }

            try
            {
                FileReader in = new FileReader(propFile);
                properties.load(in);
                in.close();
            }
            catch (IOException e)
            {
                String error = Constants.getMessageJMERR19(propFile.getAbsolutePath());
                JourneyMap.getLogger().severe(error + ": " + LogFormatter.toString(e));
                return;
            }

            // Convert older files if needed
            HashMap<String, String> temp = new HashMap(properties);
            for (Map.Entry<String, String> entry : temp.entrySet())
            {
                if (entry.getKey().contains("."))
                {
                    writeNeeded = true;
                    properties.put(entry.getKey().replaceAll("\\.", "_"), entry.getValue());
                    properties.remove(entry.getKey());
                }
                if (entry.getKey().equals("use_custom_texturepack"))
                {
                    writeNeeded = true;
                    properties.remove(entry.getKey());
                }
                if (entry.getKey().equals("automap_enabled"))
                {
                    writeNeeded = true;
                    properties.remove(entry.getKey());
                }
                if (entry.getKey().equals("mapgui_keycode"))
                {
                    writeNeeded = true;
                    properties.remove(entry.getKey());
                }
            }
        }
    }

    public void migrateLegacyProperties()
    {
        File propFile = getFile();
        if (!propFile.exists())
        {
            JourneyMap.getLogger().fine("Legacy property file doesn't exist: " + propFile.getAbsolutePath()); //$NON-NLS-1$
            return;
        }

        // Update new property files
        CoreProperties coreProperties = JourneyMap.getInstance().coreProperties;
        coreProperties.announceMod.set(getBoolean(Key.ANNOUNCE_MODLOADED));
        coreProperties.mapCaveLighting.set(getBoolean(Key.CAVE_LIGHTING));
        coreProperties.checkUpdates.set(getBoolean(Key.UPDATE_CHECK_ENABLED));
        // coreProperties.chunkOffset.set(getInteger(Key.CHUNK_OFFSET));  Lets put everybody back at the default.
        coreProperties.chunkPoll.set(getInteger(Key.UPDATETIMER_CHUNKS));
        coreProperties.entityPoll.set(getInteger(Key.BROWSER_POLL));
        coreProperties.playerPoll.set(getInteger(Key.BROWSER_POLL));
        coreProperties.save();
        JourneyMap.getLogger().info(String.format("Migrated legacy property file values from %s to %s .", propFile.getName(), coreProperties.getFile().getName())); //$NON-NLS-1$

        WebMapProperties webMapProperties = JourneyMap.getInstance().webMapProperties.enableSave(false);
        webMapProperties.enabled.set(getBoolean(Key.WEBSERVER_ENABLED));
        webMapProperties.port.set(getInteger(Key.WEBSERVER_PORT));

        MiniMapProperties miniMapProperties = JourneyMap.getInstance().miniMapProperties.enableSave(false);
        miniMapProperties.enabled.set(getBoolean(Key.PREF_SHOW_MINIMAP));
        miniMapProperties.enableHotkeys.set(getBoolean(Key.PREF_MINIMAP_HOTKEYS));
        miniMapProperties.fontSmall.set(getDouble(Key.PREF_MINIMAP_FONTSCALE).intValue() == 1);
        miniMapProperties.forceUnicode.set(getBoolean(Key.PREF_MINIMAP_FORCEUNICODE));
        miniMapProperties.position.set(DisplayVars.Position.safeValueOf(getString(Key.PREF_MINIMAP_POSITION)));
        miniMapProperties.shape.set(DisplayVars.Shape.safeValueOf(getString(Key.PREF_MINIMAP_SHAPE)));
        miniMapProperties.showFps.set(getBoolean(Key.PREF_MINIMAP_SHOWFPS));

        FullMapProperties fullMapProperties = JourneyMap.getInstance().fullMapProperties.enableSave(false);
        fullMapProperties.fontSmall.set(getDouble(Key.PREF_FONTSCALE).intValue() == 1);
        fullMapProperties.forceUnicode.set(getBoolean(Key.PREF_FORCEUNICODE));
        fullMapProperties.showCaves.set(getBoolean(Key.PREF_SHOW_CAVES));
        fullMapProperties.showGrid.set(getBoolean(Key.PREF_SHOW_GRID));

        List<MapProperties> propsList = Arrays.asList(fullMapProperties, miniMapProperties, webMapProperties);
        for (MapProperties props : propsList)
        {
            props.showAnimals.set(getBoolean(Key.PREF_SHOW_ANIMALS));
            props.showMobs.set(getBoolean(Key.PREF_SHOW_MOBS));
            props.showPets.set(getBoolean(Key.PREF_SHOW_PETS));
            props.showPlayers.set(getBoolean(Key.PREF_SHOW_PLAYERS));
            props.showVillagers.set(getBoolean(Key.PREF_SHOW_VILLAGERS));
            props.showWaypoints.set(getBoolean(Key.PREF_SHOW_WAYPOINTS));
            props.enableSave(true).save();
            JourneyMap.getLogger().info(String.format("Migrated legacy property file values from %s to %s .", propFile.getName(), props.getFile().getName())); //$NON-NLS-1$
        }

        // Delete legacy properties file.  Farewell, old friend.  You were with me from the beginning.
        propFile.delete();
    }


    @Deprecated
    private void writeToFile()
    {
//		synchronized(properties) {
//			File propFile = getFile();
//			try {
//				FileHandler.getJourneyMapDir().mkdirs();
//				FileWriter out = new FileWriter(propFile);
//				properties.store(out, "Properties for JourneyMap " + JourneyMap.JM_VERSION); //$NON-NLS-1$
//				out.close();
//			} catch(IOException e) {
//				String error = Constants.getMessageJMERR20(propFile.getAbsolutePath());
//				JourneyMap.getLogger().severe(error);
//				JourneyMap.getLogger().severe(LogFormatter.toString(e));
//				throw new RuntimeException(error);
//			}
//		}
    }

    @Override
    public String toString()
    {
        return properties.toString();
    }


}
