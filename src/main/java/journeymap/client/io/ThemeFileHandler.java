/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.common.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.common.log.JMLogger;
import journeymap.common.log.LogFormatter;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.ui.option.StringListProvider;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemePresets;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Mark on 8/29/2014.
 */
public class ThemeFileHandler
{
    public static final String ASSETS_JOURNEYMAP_ICON_THEME = "/assets/journeymap/icon/theme";
    public static final String THEME_FILE_SUFFIX = ".theme.json";
    public static final String DEFAULT_THEME_FILE = "default.theme.config";

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setVersion(Theme.VERSION).create();

    private static transient Theme currentTheme = null;

    public static void initialize()
    {
        JourneymapClient.getLogger().trace("Initializing themes ...");

        // Theme dirs
        Set<String> themeDirNames = new HashSet<String>();
        for (Theme theme : ThemePresets.getPresets())
        {
            themeDirNames.add(theme.directory);
        }

        // Copy theme dirs from assets
        for (String dirName : themeDirNames)
        {
            FileHandler.copyResources(getThemeIconDir(), ASSETS_JOURNEYMAP_ICON_THEME, dirName, true);
        }

        // Save theme files
        for (Theme theme : ThemePresets.getPresets())
        {
            save(theme);
        }

        // Create a default.theme.json file only if it doesn't already exist
        ensureDefaultThemeFile();
    }

    public static File getThemeIconDir()
    {
        File dir = new File(ForgeHelper.INSTANCE.getClient().mcDataDir, Constants.THEME_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    public static File[] getThemeDirectories()
    {
        File parentDir = getThemeIconDir();
        File[] themeDirs = parentDir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        });
        return themeDirs;
    }

    public static List<Theme> getThemes()
    {
        File[] themeDirs = getThemeDirectories();
        if (themeDirs == null || themeDirs.length == 0)
        {
            // This shouldn't happen unless somebody deleted a directory.
            initialize();
            themeDirs = getThemeDirectories();
            if (themeDirs == null || themeDirs.length == 0)
            {
                JourneymapClient.getLogger().error("Couldn't find theme directories.");
                return Collections.emptyList();
            }
        }

        // Find themes in files
        ArrayList<Theme> themes = new ArrayList<Theme>();
        for (File themeDir : themeDirs)
        {
            File[] themeFiles = themeDir.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(THEME_FILE_SUFFIX);
                }
            });

            if (themeFiles != null && themeFiles.length > 0)
            {
                for (File themeFile : themeFiles)
                {
                    Theme theme = loadThemeFromFile(themeFile, false);
                    if (theme != null)
                    {
                        themes.add(theme);
                    }
                }
            }
        }
        Collections.sort(themes);

        return themes;
    }

    public static List<String> getThemeNames()
    {
        List<Theme> themes = getThemes();
        ArrayList<String> names = new ArrayList<String>(themes.size());
        for (Theme theme : themes)
        {
            names.add(theme.name);
        }
        return names;
    }

    public static Theme getCurrentTheme()
    {
        return getCurrentTheme(false);
    }

    public synchronized static Theme getCurrentTheme(boolean forceReload)
    {
        if (forceReload)
        {
            TextureCache.instance().purgeThemeImages();
        }

        String themeName = JourneymapClient.getCoreProperties().themeName.get();
        if (forceReload || currentTheme == null || !themeName.equals(currentTheme.name))
        {
            currentTheme = getThemeByName(themeName);
        }
        return currentTheme;
    }

    public static Theme getThemeByName(String themeName)
    {
        for (Theme theme : getThemes())
        {
            if (theme.name.equals(themeName))
            {
                return theme;
            }
        }
        JourneymapClient.getLogger().warn(String.format("Theme '%s' not found, reverting to default", themeName));
        return ThemePresets.THEME_VICTORIAN;
    }

    public static Theme loadThemeFromFile(File themeFile, boolean createIfMissing)
    {
        try
        {
            if (themeFile != null && themeFile.exists())
            {
                Charset UTF8 = Charset.forName("UTF-8");
                String json = Files.toString(themeFile, UTF8);
                return GSON.fromJson(json, Theme.class);
            }
            else if (createIfMissing)
            {
                JourneymapClient.getLogger().info("Generating Theme json file: " + themeFile);
                Theme theme = new Theme();
                theme.name = themeFile.getName();
                save(theme);
                return theme;
            }
        }
        catch (Throwable t)
        {
            JourneymapClient.getLogger().error("Could not load Theme json file: " + LogFormatter.toString(t));
        }
        return null;
    }

    private static File getThemeFile(String themeDirName, String themeFileName)
    {
        File themeDir = new File(getThemeIconDir(), themeDirName);
        String fileName = String.format("%s%s", themeFileName.replaceAll("[\\\\/:\"*?<>|]", "_"), THEME_FILE_SUFFIX);
        return new File(themeDir, fileName);
    }

    public static void save(Theme theme)
    {
        try
        {
            File themeFile = getThemeFile(theme.directory, theme.name);
            Files.createParentDirs(themeFile);
            Charset UTF8 = Charset.forName("UTF-8");
            Files.write(GSON.toJson(theme), themeFile, UTF8);
        }
        catch (Throwable t)
        {
            JourneymapClient.getLogger().error("Could not save Theme json file: " + t);
        }
    }

    /**
     * Create default theme file if it doesn't exist
     */
    private static void ensureDefaultThemeFile()
    {
        File defaultThemeFile = new File(getThemeIconDir(), DEFAULT_THEME_FILE);
        if (!defaultThemeFile.exists())
        {
            try
            {
                Theme.DefaultPointer defaultPointer = new Theme.DefaultPointer(ThemePresets.THEME_VICTORIAN);
                Charset UTF8 = Charset.forName("UTF-8");
                Files.write(GSON.toJson(defaultPointer), defaultThemeFile, UTF8);
            }
            catch (Throwable t)
            {
                JourneymapClient.getLogger().error("Could not save DefaultTheme json file: " + t);
            }
        }
    }

    public static Theme getDefaultTheme()
    {
        Theme.DefaultPointer pointer = loadDefaultPointer();
        pointer.filename = pointer.filename.replace(THEME_FILE_SUFFIX, "");

        File themeFile = getThemeFile(pointer.directory, pointer.filename);
        Theme theme = loadThemeFromFile(themeFile, false);
        if (theme == null)
        {
            JMLogger.logOnce(String.format("Default theme not found in %s: %s", themeFile, pointer.name), null);
            theme = ThemePresets.THEME_VICTORIAN;
        }
        return theme;
    }

    public synchronized static void loadNextTheme()
    {
        List<String> themeNames = getThemeNames();
        int index = themeNames.indexOf(getCurrentTheme().name);
        if (index < 0 || index >= themeNames.size() - 1)
        {
            index = 0;
        }
        else
        {
            index++;
        }

        TextureCache.instance().purgeThemeImages();
        currentTheme = getThemes().get(index);
        JourneymapClient.getCoreProperties().themeName.set(currentTheme.name);
        getCurrentTheme(true);
    }

    /**
     * Get the DefaultPointer needed to load a theme.
     */
    private static Theme.DefaultPointer loadDefaultPointer()
    {
        ensureDefaultThemeFile();
        try
        {
            File defaultThemeFile = new File(getThemeIconDir(), DEFAULT_THEME_FILE);
            if (defaultThemeFile.exists())
            {
                Charset UTF8 = Charset.forName("UTF-8");
                String json = Files.toString(defaultThemeFile, UTF8);
                return GSON.fromJson(json, Theme.DefaultPointer.class);
            }
            else
            {
                return new Theme.DefaultPointer(ThemePresets.THEME_VICTORIAN);
            }
        }
        catch (Throwable t)
        {
            JourneymapClient.getLogger().error("Could not load Theme.DefaultTheme json file: " + LogFormatter.toString(t));
        }
        return null;
    }

    public static class ThemeStringListProvider implements StringListProvider
    {
        @Override
        public List<String> getStrings()
        {
            return ThemeFileHandler.getThemeNames();
        }

        @Override
        public String getDefaultString()
        {
            return getDefaultTheme().name;
        }
    }
}
