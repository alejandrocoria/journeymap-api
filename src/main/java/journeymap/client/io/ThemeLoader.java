/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.io;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
import journeymap.client.log.JMLogger;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemePresets;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.StringField;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author techbrew 8/29/2014.
 */
public class ThemeLoader
{
    /**
     * The constant THEME_FILE_SUFFIX.
     */
    public static final String THEME_FILE_SUFFIX = ".theme2.json";

    /**
     * The constant DEFAULT_THEME_FILE.
     */
    public static final String DEFAULT_THEME_FILE = "default.theme.config";

    /**
     * The constant GSON.
     */
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setVersion(Theme.VERSION).create();

    private static transient Theme currentTheme = null;

    /**
     * Initialize.
     *
     * @param preLoadCurrentTheme the pre load current theme
     */
    public static void initialize(boolean preLoadCurrentTheme)
    {
        Journeymap.getLogger().trace("Initializing themes ...");

        // Theme dirs
        Set<String> themeDirNames = ThemePresets.getPresetDirs().stream().collect(Collectors.toSet());

        // Copy theme dirs from assets
        for (String dirName : themeDirNames)
        {
            FileHandler.copyResources(getThemeIconDir(), new ResourceLocation(Journeymap.MOD_ID, "theme/" + dirName), dirName, true);
        }

        // Save theme files
        ThemePresets.getPresets().forEach(ThemeLoader::save);

        // Create a default.theme.json file only if it doesn't already exist
        ensureDefaultThemeFile();

        // Preload the current theme
        if (preLoadCurrentTheme)
        {
            preloadCurrentTheme();
        }
    }

    /**
     * Gets theme icon dir.
     *
     * @return the theme icon dir
     */
    public static File getThemeIconDir()
    {
        File dir = new File(FileHandler.getMinecraftDirectory(), Constants.THEME_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Get theme directories file [ ].
     *
     * @return the file [ ]
     */
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

    /**
     * Gets themes.
     *
     * @return the themes
     */
    public static List<Theme> getThemes()
    {
        File[] themeDirs = getThemeDirectories();
        if (themeDirs == null || themeDirs.length == 0)
        {
            // This shouldn't happen unless somebody deleted a directory.
            initialize(false);
            themeDirs = getThemeDirectories();
            if (themeDirs == null || themeDirs.length == 0)
            {
                Journeymap.getLogger().error("Couldn't find theme directories.");
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

        if (themes.isEmpty())
        {
            themes.addAll(ThemePresets.getPresets());
        }

        Collections.sort(themes, Comparator.comparing(theme -> theme.name));
        return themes;
    }

    /**
     * Gets theme names.
     *
     * @return the theme names
     */
    public static List<String> getThemeNames()
    {
        List<Theme> themes = null;
        try
        {
            themes = getThemes();
        }
        catch (Exception e)
        {
            themes = ThemePresets.getPresets();
        }

        return themes.stream().map(theme -> theme.name).collect(Collectors.toList());
    }

    /**
     * Gets current theme.
     *
     * @return the current theme
     */
    public static Theme getCurrentTheme()
    {
        return getCurrentTheme(false);
    }

    /**
     * Set the current theme.
     *
     * @param theme the theme
     */
    public synchronized static void setCurrentTheme(Theme theme)
    {
        if (currentTheme == theme)
        {
            return;
        }
        Journeymap.getClient().getCoreProperties().themeName.set(theme.name);
        getCurrentTheme(true);
        UIManager.INSTANCE.getMiniMap().reset();
    }

    /**
     * Gets current theme.
     *
     * @param forceReload the force reload
     * @return the current theme
     */
    public synchronized static Theme getCurrentTheme(boolean forceReload)
    {
        if (forceReload)
        {
            TextureCache.purgeThemeImages(TextureCache.themeImages);
        }

        String themeName = Journeymap.getClient().getCoreProperties().themeName.get();
        if (forceReload || currentTheme == null || !themeName.equals(currentTheme.name))
        {
            currentTheme = getThemeByName(themeName);
            Journeymap.getClient().getCoreProperties().themeName.set(currentTheme.name);
        }
        return currentTheme;
    }

    /**
     * Gets theme by name.
     *
     * @param themeName the theme name
     * @return the theme by name
     */
    public static Theme getThemeByName(String themeName)
    {
        for (Theme theme : getThemes())
        {
            if (theme.name.equals(themeName))
            {
                return theme;
            }
        }
        Journeymap.getLogger().warn(String.format("Theme '%s' not found, reverting to default", themeName));
        return ThemePresets.getDefault();
    }

    /**
     * Load theme from file theme.
     *
     * @param themeFile       the theme file
     * @param createIfMissing the create if missing
     * @return the theme
     */
    public static Theme loadThemeFromFile(File themeFile, boolean createIfMissing)
    {
        try
        {
            if (themeFile != null && themeFile.exists())
            {
                Charset UTF8 = Charset.forName("UTF-8");
                String json = Files.toString(themeFile, UTF8);
                Theme theme = GSON.fromJson(json, Theme.class);
                if (theme.schema < Theme.VERSION)
                {
                    Journeymap.getLogger().error("Theme file schema is obsolete, cannot be used: " + themeFile);
                    return null;
                }
                return theme;
            }
            else if (createIfMissing)
            {
                Journeymap.getLogger().info("Generating Theme json file: " + themeFile);
                Theme theme = new Theme();
                theme.name = themeFile.getName();
                save(theme);
                return theme;
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Could not load Theme json file: " + LogFormatter.toString(t));
        }
        return null;
    }

    private static File getThemeFile(String themeDirName, String themeFileName)
    {
        File themeDir = new File(getThemeIconDir(), themeDirName);
        String fileName = String.format("%s%s", themeFileName.replaceAll("[\\\\/:\"*?<>|]", "_"), THEME_FILE_SUFFIX);
        return new File(themeDir, fileName);
    }

    /**
     * Save.
     *
     * @param theme the theme
     */
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
            Journeymap.getLogger().error("Could not save Theme json file: " + t);
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
                Theme.DefaultPointer defaultPointer = new Theme.DefaultPointer(ThemePresets.getDefault());
                Charset UTF8 = Charset.forName("UTF-8");
                Files.write(GSON.toJson(defaultPointer), defaultThemeFile, UTF8);
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Could not save DefaultTheme json file: " + t);
            }
        }
    }

    /**
     * Gets default theme.
     *
     * @return the default theme
     */
    public static Theme getDefaultTheme()
    {
        if (FMLClientHandler.instance().getClient() == null)
        {
            return ThemePresets.getDefault();
        }

        Theme theme = null;
        File themeFile = null;
        Theme.DefaultPointer pointer = null;
        try
        {
            pointer = loadDefaultPointer();
            pointer.filename = pointer.filename.replace(THEME_FILE_SUFFIX, "");

            themeFile = getThemeFile(pointer.directory, pointer.filename);
            theme = loadThemeFromFile(themeFile, false);
        }
        catch (Exception e)
        {
            JMLogger.logOnce("Default theme not found in files", e);
        }

        if (theme == null)
        {
            // Is this needed anymore.
//            if (pointer != null)
//            {
//                JMLogger.logOnce(String.format("Default theme not found in %s: %s", themeFile, pointer.name), null);
//            }
            theme = ThemePresets.getDefault();
        }
        return theme;
    }

    /**
     * Load next theme.
     */
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

        setCurrentTheme(getThemes().get(index));
    }

    /**
     * Get the DefaultPointer needed to load a theme.
     */
    private static Theme.DefaultPointer loadDefaultPointer()
    {
        try
        {
            ensureDefaultThemeFile();
            File defaultThemeFile = new File(getThemeIconDir(), DEFAULT_THEME_FILE);
            if (defaultThemeFile.exists())
            {
                Charset UTF8 = Charset.forName("UTF-8");
                String json = Files.toString(defaultThemeFile, UTF8);
                return GSON.fromJson(json, Theme.DefaultPointer.class);
            }
            else
            {
                return new Theme.DefaultPointer(ThemePresets.getDefault());
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Could not load Theme.DefaultTheme json file: " + LogFormatter.toString(t));
        }
        return null;
    }

    /**
     * Load the theme textures ahead of time to avoid the initial
     * lag when opening the Fullscreen map.
     */
    public static void preloadCurrentTheme()
    {
        int count = 0;
        try
        {
            Theme theme = getCurrentTheme();
            File themeDir = new File(getThemeIconDir(), theme.directory).getCanonicalFile();
            Path themePath = themeDir.toPath();
            for (File file : Files.fileTreeTraverser().breadthFirstTraversal(themeDir))
            {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png"))
                {
                    String relativePath = themePath.relativize(file.toPath()).toString().replaceAll("\\\\", "/");
                    TextureCache.getThemeTexture(theme, relativePath);
                    count++;
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error preloading theme textures: " + LogFormatter.toString(t));
        }
        Journeymap.getLogger().info("Preloaded theme textures: " + count);
    }

    /**
     * The type Theme values provider.
     */
    public static class ThemeValuesProvider implements StringField.ValuesProvider
    {
        @Override
        public List<String> getStrings()
        {
            return ThemeLoader.getThemeNames();
        }

        @Override
        public String getDefaultString()
        {
            return getDefaultTheme().name;
        }
    }
}
