package journeymap.server.legacyserver.config;


import com.google.gson.Gson;
import journeymap.server.legacyserver.util.FileManager;

import java.io.File;


/**
 * Created by Mysticdrew on 10/8/2014.
 */
public class ConfigHandler
{
    private static final float CONFIG_VERSION = 1.12F;
    private static File configPath;

    public static void init(File configPath)
    {
        ConfigHandler.configPath = configPath;
    }

    public static File getConfigPath()
    {
        return configPath;
    }

    /**
     * returns a config based on the world file, if the config file does not exists
     * this method starts the process of generating a new one.
     * If a new config is generated, it will return a new config with default values.
     * If the config exists, it will return it.
     *
     * @param worldName
     * @return
     */
    public static Configuration getConfigByWorldName(String worldName)
    {
        worldName = parseWorldName(worldName);
        Configuration config = loadConfig(worldName);
        if (config != null)
        {
            return config;
        }
        return null;
    }

    private static Configuration loadConfig(String worldName)
    {
        worldName = parseWorldName(worldName);
        Configuration config;
        File configFile = new File(configPath, String.format("%s.cfg", worldName));

        try
        {
            Gson gson = new Gson();
            config = gson.fromJson(FileManager.readFile(configFile), Configuration.class);
        }
        catch (NoClassDefFoundError nce)
        {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            config = gson.fromJson(FileManager.readFile(configFile), Configuration.class);
        }
        return config;
    }

    private static String parseWorldName(String worldName)
    {
        String[] name = worldName.split("/");
        return name[name.length - 1];
    }

    public static void delete(String world)
    {
        File configFile = new File(configPath, String.format("%s.cfg", world));
        configFile.delete();
    }
}
