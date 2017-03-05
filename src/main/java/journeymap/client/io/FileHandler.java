/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.io;


import com.google.common.base.Joiner;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.data.WorldData;
import journeymap.client.log.JMLogger;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Level;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * The type File handler.
 */
public class FileHandler
{
    /**
     * The constant DEV_MINECRAFT_DIR.
     */
    public static final String DEV_MINECRAFT_DIR = "run/";
    /**
     * The constant ASSETS_JOURNEYMAP.
     */
    public static final String ASSETS_JOURNEYMAP = "/assets/journeymap";
    /**
     * The constant ASSETS_JOURNEYMAP_UI.
     */
    public static final String ASSETS_JOURNEYMAP_UI = "/assets/journeymap/ui";

    /**
     * The constant MinecraftDirectory.
     */
    public static final File MinecraftDirectory = getMinecraftDirectory();
    /**
     * The constant JourneyMapDirectory.
     */
    public static final File JourneyMapDirectory = new File(MinecraftDirectory, Constants.JOURNEYMAP_DIR);
    /**
     * The constant StandardConfigDirectory.
     */
    public static final File StandardConfigDirectory = new File(MinecraftDirectory, Constants.CONFIG_DIR);

    private static WorldClient theLastWorld;

    /**
     * Gets minecraft directory.
     *
     * @return the minecraft directory
     */
    public static File getMinecraftDirectory()
    {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if(minecraft!=null)
        {
            return minecraft.mcDataDir;
        }
        else
        {
            return new File(DEV_MINECRAFT_DIR);
        }
    }

    /**
     * Gets mc world dir.
     *
     * @param minecraft the minecraft
     * @return the mc world dir
     */
    public static File getMCWorldDir(Minecraft minecraft)
    {
        if (minecraft.isIntegratedServerRunning())
        {
            String lastMCFolderName = minecraft.getIntegratedServer().getFolderName();
            File lastMCWorldDir = new File(getMinecraftDirectory(), "saves" + File.separator + lastMCFolderName);
            return lastMCWorldDir;
        }
        return null;
    }

    /**
     * Gets world save dir.
     *
     * @param minecraft the minecraft
     * @return the world save dir
     */
    public static File getWorldSaveDir(Minecraft minecraft)
    {
        if (minecraft.isSingleplayer())
        {
            try
            {
                File savesDir = new File(getMinecraftDirectory(), "saves");
                File worldSaveDir = new File(savesDir, minecraft.getIntegratedServer().getFolderName());
                if (minecraft.theWorld.provider.getSaveFolder() != null)
                {
                    File dir = new File(worldSaveDir, minecraft.theWorld.provider.getSaveFolder());
                    dir.mkdirs();
                    return dir;
                }
                else
                {
                    return worldSaveDir;
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Error getting world save dir: %s", t);
            }
        }
        return null;
    }

    /**
     * Gets mc world dir.
     *
     * @param minecraft the minecraft
     * @param dimension the dimension
     * @return the mc world dir
     */
    public static File getMCWorldDir(Minecraft minecraft, final int dimension)
    {
        File worldDir = getMCWorldDir(minecraft);
        if (worldDir == null)
        {
            return null;
        }
        if (dimension == 0)
        {
            return worldDir;
        }
        else
        {
            final String dimString = Integer.toString(dimension);
            File dimDir = null;

            // Normal dimensions handled this way
            if (dimension == -1 || dimension == 1)
            {
                dimDir = new File(worldDir, "DIM" + dimString); 
            }

            // Custom dimensions handled this way
            // TODO: Use Forge dimensionhandler to get directory name.  This is a brittle workaround.
            if (dimDir == null || !dimDir.exists())
            {
                File[] dims = worldDir.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return name.startsWith("DIM") && name.endsWith(dimString) && !name.endsWith("-" + dimString); // this last part prevents negative matches, but may nerf a dumb naming scheme
                    }
                });

                if (dims.length == 0)
                {
                    return new File(worldDir, "DIM" + dimString); 
                }
                else if (dims.length == 1)
                {
                    dimDir = dims[0];
                }
                else
                {
                    // 7 might match DIM7 and DIM17.  Sort and return shortest filename.
                    List<File> list = Arrays.asList(dims);
                    Collections.sort(list, new Comparator<File>()
                    {
                        @Override
                        public int compare(File o1, File o2)
                        {
                            return new Integer(o1.getName().length()).compareTo(o2.getName().length());
                        }
                    });
                    return list.get(0);
                }
            }

            return dimDir;
        }
    }

    /**
     * Gets journey map dir.
     *
     * @return the journey map dir
     */
    public static File getJourneyMapDir()
    {
        return JourneyMapDirectory;
    }

    /**
     * Gets jm world dir.
     *
     * @param minecraft the minecraft
     * @return the jm world dir
     */
    public static File getJMWorldDir(Minecraft minecraft)
    {
        if (minecraft.theWorld == null)
        {
            return null;
        }

        if (!minecraft.isSingleplayer())
        {
            return getJMWorldDir(minecraft, Journeymap.getClient().getCurrentWorldId());
        }
        else
        {
            return getJMWorldDir(minecraft, null);
        }
    }

    /**
     * Gets jm world dir.
     *
     * @param minecraft the minecraft
     * @param worldId   the world id
     * @return the jm world dir
     */
    public static synchronized File getJMWorldDir(Minecraft minecraft, String worldId)
    {
        if (minecraft.theWorld == null)
        {
            theLastWorld = null;

            return null;
        }

        File worldDirectory = null;

        try
        {

            worldDirectory = getJMWorldDirForWorldId(minecraft, worldId);

            if (worldDirectory!=null && worldDirectory.exists())
            {
                return worldDirectory;
            }

            File defaultWorldDirectory = FileHandler.getJMWorldDirForWorldId(minecraft, null);

            if (worldId != null && defaultWorldDirectory.exists() && !worldDirectory.exists())
            {
                Journeymap.getLogger().log(Level.INFO, "Moving default directory to " + worldDirectory);
                try
                {
                    migrateLegacyFolderName(defaultWorldDirectory, worldDirectory);
                    return worldDirectory;
                }
                catch (Exception e)
                {
                    Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
                }
            }

            if (!minecraft.isSingleplayer())
            {
                String legacyWorldName;
                File legacyWorldDir;

                boolean migrated = false;

                // Older use of MP server's socket IP/hostname
                legacyWorldName = WorldData.getLegacyServerName() + "_0";
                legacyWorldDir = new File(MinecraftDirectory, Constants.MP_DATA_DIR + legacyWorldName);
                if (legacyWorldDir.exists()
                        && !legacyWorldDir.getName().equals(defaultWorldDirectory.getName())
                        && !legacyWorldDir.getName().equals(worldDirectory.getName()))
                {
                    migrateLegacyFolderName(legacyWorldDir, worldDirectory);
                    migrated = true;
                }

                if (worldId != null)
                {
                    // Newer URL-encoded use of MP server entry provided by user, with world id
                    legacyWorldName = WorldData.getWorldName(minecraft, true) + "_" + worldId;
                    legacyWorldDir = new File(MinecraftDirectory, Constants.MP_DATA_DIR + legacyWorldName);
                    if (legacyWorldDir.exists()
                            && !legacyWorldDir.getName().equals(worldDirectory.getName()))
                    {
                        migrateLegacyFolderName(legacyWorldDir, worldDirectory);
                        migrated = true;
                    }
                }
            }
            else
            {
                File legacyWorldDir = new File(MinecraftDirectory, Constants.SP_DATA_DIR + WorldData.getWorldName(minecraft, true));
                if (!legacyWorldDir.getName().equals(worldDirectory.getName()))
                {
                    if (legacyWorldDir.exists() && worldDirectory.exists())
                    {
                        JMLogger.logOnce(String.format("Found two directories that might be in conflict. Using:  %s , Ignoring: %s", worldDirectory, legacyWorldDir), null);
                    }
                }

                if (legacyWorldDir.exists() && !worldDirectory.exists() && !legacyWorldDir.getName().equals(worldDirectory.getName()))
                {
                    migrateLegacyFolderName(legacyWorldDir, worldDirectory);
                }
            }

            if (!worldDirectory.exists())
            {
                if (!(worldId != null && worldDirectory.getName().equals(defaultWorldDirectory.getName())))
                {
                    worldDirectory.mkdirs();
                }
            }

        }
        catch (Exception e)
        {
            Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
            throw new RuntimeException(e);
        }

        theLastWorld = minecraft.theWorld;
        return worldDirectory;
    }

    /**
     * Gets jm world dir for world id.
     *
     * @param minecraft the minecraft
     * @param worldId   the world id
     * @return the jm world dir for world id
     */
    public static File getJMWorldDirForWorldId(Minecraft minecraft, String worldId)
    {
        if (minecraft==null || minecraft.theWorld == null)
        {
            return null;
        }

        File testWorldDirectory = null;
        try
        {
            if (!minecraft.isSingleplayer())
            {
                if (worldId != null)
                {
                    worldId = worldId.replaceAll("\\W+", "~");
                }
                String suffix = (worldId != null) ? ("_" + worldId) : "";
                testWorldDirectory = new File(MinecraftDirectory, Constants.MP_DATA_DIR + WorldData.getWorldName(minecraft, false) + suffix); 
            }
            else
            {
                testWorldDirectory = new File(MinecraftDirectory, Constants.SP_DATA_DIR + WorldData.getWorldName(minecraft, false));
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
        }

        return testWorldDirectory;
    }

    private static void migrateLegacyFolderName(File legacyWorldDir, File worldDir)
    {
        if (legacyWorldDir.getPath().equals(worldDir.getPath()))
        {
            return;
        }

        boolean success = false;
        try
        {
            success = legacyWorldDir.renameTo(worldDir);
            if (!success)
            {
                throw new IllegalStateException("Need to rename legacy folder, but not able to");
            }
            Journeymap.getLogger().info(String.format("Migrated legacy folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()));
        }
        catch (Exception e)
        {
            JMLogger.logOnce(String.format("Failed to migrate legacy folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()), e);

            String tempName = worldDir.getName() + "__OLD";
            try
            {
                success = legacyWorldDir.renameTo(new File(legacyWorldDir.getParentFile(), tempName));
            }
            catch (Exception e2)
            {
                success = false;
            }
            if (!success)
            {
                JMLogger.logOnce(String.format("Failed to even rename legacy folder from %s to %s", legacyWorldDir.getName(), tempName), e);
            }
        }
    }

    /**
     * Gets waypoint dir.
     *
     * @return the waypoint dir
     */
    public static File getWaypointDir()
    {
        return getWaypointDir(getJMWorldDir(FMLClientHandler.instance().getClient()));
    }

    /**
     * Gets waypoint dir.
     *
     * @param jmWorldDir the jm world dir
     * @return the waypoint dir
     */
    public static File getWaypointDir(File jmWorldDir)
    {
        File waypointDir = new File(jmWorldDir, "waypoints");
        if (!waypointDir.isDirectory())
        {
            waypointDir.delete();
        }
        if (!waypointDir.exists())
        {
            waypointDir.mkdirs();
        }
        return waypointDir;
    }

    /**
     * Gets lang file.
     *
     * @param fileName the file name
     * @return the lang file
     */
    public static Properties getLangFile(String fileName)
    {
        try
        {
            InputStream is = JourneymapClient.class.getResourceAsStream("/assets/journeymap/lang/" + fileName);
            if (is == null)
            {
                // Dev environment
                File file = new File("../src/main/resources/assets/journeymap/lang/" + fileName);
                if (file.exists())
                {
                    is = new FileInputStream(file);
                }
                else
                {
                    Journeymap.getLogger().warn("Language file not found: " + fileName);
                    return null;
                }
            }
            Properties properties = new Properties();
            properties.load(is);
            is.close();
            return properties;
        }
        catch (IOException e)
        {
            String error = "Could not get language file " + fileName + ": " + (e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    /**
     * Gets message model.
     *
     * @param <M>        the type parameter
     * @param model      the model
     * @param filePrefix the file prefix
     * @return the message model
     */
    public static <M> M getMessageModel(Class<M> model, String filePrefix)
    {
        try
        {
            String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
            InputStream is = getMessageModelInputStream(filePrefix, lang);
            if (is == null && !lang.equals("en_US"))
            {
                is = getMessageModelInputStream(filePrefix, "en_US");
            }
            if (is == null)
            {
                Journeymap.getLogger().warn("Message file not found: " + filePrefix);
                return null;
            }
            return new GsonBuilder().create().fromJson(new InputStreamReader(is), model);
        }
        catch (Throwable e)
        {
            String error = "Could not get Message model " + filePrefix + ": " + (e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    /**
     * Gets message model input stream.
     *
     * @param filePrefix the file prefix
     * @param lang       the lang
     * @return the message model input stream
     */
    public static InputStream getMessageModelInputStream(String filePrefix, String lang)
    {
        String file = String.format("/assets/journeymap/lang/message/%s-%s.json", filePrefix, lang);
        return JourneymapClient.class.getResourceAsStream(file);
    }

    /**
     * Gets world config dir.
     *
     * @param fallbackToStandardConfigDir the fallback to standard config dir
     * @return the world config dir
     */
    public static File getWorldConfigDir(boolean fallbackToStandardConfigDir)
    {
        File worldDir = getJMWorldDirForWorldId(FMLClientHandler.instance().getClient(), null); // always use the "base" folder for multiplayer
        if (worldDir != null && worldDir.exists())
        {
            File worldConfigDir = new File(worldDir, "config");
            if (worldConfigDir.exists())
            {
                return worldConfigDir;
            }
        }

        return fallbackToStandardConfigDir ? StandardConfigDirectory : null;
    }

    /**
     * Gets image.
     *
     * @param imageFile the image file
     * @return the image
     */
    public static BufferedImage getImage(File imageFile)
    {
        try
        {
            if (!imageFile.canRead())
            {
                return null;
            }
            return ImageIO.read(imageFile);
        }
        catch (IOException e)
        {
            String error = "Could not get imageFile " + imageFile + ": " + (e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    /**
     * Is in jar boolean.
     *
     * @return the boolean
     */
    public static boolean isInJar()
    {
        return isInJar(JourneymapClient.class.getProtectionDomain().getCodeSource().getLocation());
    }

    /**
     * Is in jar boolean.
     *
     * @param location the location
     * @return the boolean
     */
    public static boolean isInJar(URL location)
    {
        if ("jar".equals(location.getProtocol()))
        {
            return true;
        }
        ;

        if ("file".equals(location.getProtocol()))
        {
            File file = new File(location.getFile());
            if (file.exists())
            {
                if (file.getName().endsWith(".jar") || file.getName().endsWith(".jar"))
                {
                    return true;
                }
            }
        }
        ;

        return false;
    }

    /**
     * Copy color palette html file file.
     *
     * @param toDir    the to dir
     * @param fileName the file name
     * @return the file
     */
    public static File copyColorPaletteHtmlFile(File toDir, String fileName)
    {
        try
        {
            final File outFile = new File(toDir, fileName);
            String htmlPath = FileHandler.ASSETS_JOURNEYMAP_UI + "/" + fileName;
            InputStream inputStream = JourneymapClient.class.getResource(htmlPath).openStream();

            ByteSink out = new ByteSink()
            {
                @Override
                public OutputStream openStream() throws IOException
                {
                    return new FileOutputStream(outFile);
                }
            };
            out.writeFrom(inputStream);

            return outFile;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().warn("Couldn't copy color palette html: " + t);
            return null;
        }
    }

    /**
     * Open.
     *
     * @param file the file
     */
    public static void open(File file)
    {

        String path = file.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX)
        {
            try
            {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", path});
                return;
            }
            catch (IOException e)
            {
                Journeymap.getLogger().error("Could not open path with /usr/bin/open: " + path + " : " + LogFormatter.toString(e));
            }
        }
        else
        {
            if (Util.getOSType() == Util.EnumOS.WINDOWS)
            {

                String cmd = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[]{path});

                try
                {
                    Runtime.getRuntime().exec(cmd);
                    return;
                }
                catch (IOException e)
                {
                    Journeymap.getLogger().error("Could not open path with cmd.exe: " + path + " : " + LogFormatter.toString(e));
                }
            }
        }

        try
        {
            Class desktopClass = Class.forName("java.awt.Desktop");
            Object method = desktopClass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
            desktopClass.getMethod("browse", new Class[]{URI.class}).invoke(method, new Object[]{file.toURI()});
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Could not open path with Desktop: " + path + " : " + LogFormatter.toString(e));
            Sys.openURL("file://" + path);
        }
    }

    /**
     * Copy resources boolean.
     *
     * @param targetDirectory the target directory
     * @param location        the location
     * @param setName         the set name
     * @param overwrite       the overwrite
     * @return the boolean
     */
    public static boolean copyResources(File targetDirectory, ResourceLocation location, String setName, boolean overwrite)
    {
        String fromPath = null;
        File toDir = null;
        try
        {
            String domain = location.getResourceDomain();
            URL fileLocation = null;

            if (domain.equals("minecraft"))
            {
                fileLocation = Minecraft.class.getProtectionDomain().getCodeSource().getLocation();
            }
            else
            {
                ModContainer mod = Loader.instance().getIndexedModList().get(domain);
                if (mod == null)
                {
                    for (Map.Entry<String, ModContainer> modEntry : Loader.instance().getIndexedModList().entrySet())
                    {
                        if (modEntry.getValue().getModId().toLowerCase().equals(domain))
                        {
                            mod = modEntry.getValue();
                            break;
                        }
                    }
                }
                if (mod != null)
                {
                    fileLocation = mod.getSource().toURI().toURL();
                }
            }

            if (fileLocation != null)
            {
                String assetsPath;
                if (location.getResourcePath().startsWith("assets/"))
                {
                    assetsPath = location.getResourcePath();
                }
                else
                {
                    assetsPath = String.format("assets/%s/%s", domain, location.getResourcePath());
                }
                return copyResources(targetDirectory, fileLocation, assetsPath, setName, overwrite);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("Couldn't get resource set from %s to %s: %s", fromPath, toDir, t));
        }
        return false;
    }

    /**
     * Copy resources boolean.
     *
     * @param targetDirectory the target directory
     * @param assetsPath      the assets path
     * @param setName         the set name
     * @param overwrite       the overwrite
     * @return the boolean
     */
    public static boolean copyResources(File targetDirectory, String assetsPath, String setName, boolean overwrite)
    {
        ModContainer modContainer = Loader.instance().getIndexedModList().get(Journeymap.MOD_ID);
        if (modContainer != null)
        {
            try
            {
                URL resourceDir = modContainer.getSource().toURI().toURL();
                return copyResources(targetDirectory, resourceDir, assetsPath, setName, overwrite);
            }
            catch (MalformedURLException e)
            {
                Journeymap.getLogger().error(String.format("Couldn't find resource directory %s ", targetDirectory));
            }
        }
        return false;
    }

    /**
     * Copy resources boolean.
     *
     * @param targetDirectory the target directory
     * @param resourceDir     the resource dir
     * @param assetsPath      the assets path
     * @param setName         the set name
     * @param overwrite       the overwrite
     * @return the boolean
     */
    public static boolean copyResources(File targetDirectory, URL resourceDir, String assetsPath, String setName, boolean overwrite)
    {
        String fromPath = null;
        File toDir = null;
        try
        {
            toDir = new File(targetDirectory, setName);
            boolean inJar = FileHandler.isInJar(resourceDir);
            if (inJar)
            {
                if ("jar".equals(resourceDir.getProtocol()))
                {
                    fromPath = URLDecoder.decode(resourceDir.getPath(), "utf-8").split("file:")[1].split("!/")[0];
                }
                else
                {
                    fromPath = new File(resourceDir.getPath()).getPath();
                }
                return FileHandler.copyFromZip(fromPath, assetsPath, toDir, overwrite);
            }
            else
            {
                File fromDir = new File(resourceDir.getFile(), assetsPath);
                if (fromDir.exists())
                {
                    fromPath = fromDir.getPath();
                    return FileHandler.copyFromDirectory(fromDir, toDir, overwrite);
                }
                else
                {
                    Journeymap.getLogger().error(String.format("Couldn't locate icons for %s: %s", setName, fromDir));
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("Couldn't unzip resource set from %s to %s: %s", fromPath, toDir, t));
        }
        return false;
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param zipFilePath  the zip file path
     * @param zipEntryName the zip entry name
     * @param destDir      the dest dir
     * @param overWrite    the over write
     * @return the boolean
     * @throws Throwable the throwable
     */
    static boolean copyFromZip(String zipFilePath, String zipEntryName, File destDir, boolean overWrite) throws Throwable
    {
        if (zipEntryName.startsWith("/"))
        {
            zipEntryName = zipEntryName.substring(1);
        }
        final ZipFile zipFile = new ZipFile(zipFilePath);
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();

        boolean success = false;

        try
        {
            while (entry != null)
            {
                if (entry.getName().startsWith(zipEntryName))
                {
                    File toFile = new File(destDir, entry.getName().split(zipEntryName)[1]);
                    if (overWrite || !toFile.exists())
                    {
                        if (!entry.isDirectory())
                        {
                            Files.createParentDirs(toFile);
                            new ZipEntryByteSource(zipFile, entry).copyTo(Files.asByteSink(toFile));
                            success = true;
                        }
                    }
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        finally
        {
            zipIn.close();
        }

        return success;
    }

    /**
     * Copies contents of one directory to another
     *
     * @param fromDir   the from dir
     * @param toDir     the to dir
     * @param overWrite the over write
     * @return the boolean
     * @throws IOException the io exception
     */
    static boolean copyFromDirectory(File fromDir, File toDir, boolean overWrite) throws IOException
    {
        if (!toDir.exists())
        {
            if (!toDir.mkdirs())
            {
                throw new IOException("Couldn't create directory: " + toDir);
            }
        }
        File[] files = fromDir.listFiles();

        if (files == null)
        {
            throw new IOException(fromDir + " nas no files");
        }

        boolean success = true;
        for (File from : files)
        {
            File to = new File(toDir, from.getName());
            if (from.isDirectory())
            {
                if (!copyFromDirectory(from, to, overWrite))
                {
                    success = false;
                }
            }
            else if (overWrite || !to.exists())
            {
                Files.copy(from, to);
                if (!to.exists())
                {
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Delete boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean delete(File file)
    {
        if (!file.exists())
        {
            return true;
        }

        if (file.isFile())
        {
            return file.delete();
        }

        String[] cmd = null;
        String path = file.getAbsolutePath();
        Util.EnumOS os = Util.getOSType();

        switch (os)
        {
            case WINDOWS:
            {
                cmd = new String[]{String.format("cmd.exe /C RD /S /Q \"%s\"", path)};
                break;
            }
            case OSX:
            {
                cmd = new String[]{"rm", "-rf", path};
                break;
            }
            default:
            {
                cmd = new String[]{"rm", "-rf", path};
                break;
            }
        }

        try
        {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error(String.format("Could not delete using: %s : %s", Joiner.on(" ").join(cmd), LogFormatter.toString(e)));
        }
        return file.exists();
    }

    /**
     * Gets icon from file.
     *
     * @param parentdir the parentdir
     * @param setName   the set name
     * @param iconPath  the icon path
     * @return the icon from file
     */
    public static BufferedImage getIconFromFile(File parentdir, String setName, String iconPath)
    {
        BufferedImage img = null;
        if (iconPath == null)
        {
            // Will make error messages easier to interpret.
            iconPath = "null";
        }

        File iconFile = null;

        try
        {
            String filePath = Joiner.on(File.separatorChar).join(setName, iconPath.replace('/', File.separatorChar));
            iconFile = new File(parentdir, filePath);


            if (iconFile.exists())
            {
                img = FileHandler.getImage(iconFile);
            }


        }
        catch (Exception e)
        {
            JMLogger.logOnce("Couldn't load iconset file: " + iconFile, e);
        }

        return img;
    }

//    public void TODO(ResourceLocation assetsPath, String setName, String iconPath, BufferedImage defaultImg) {
//        if (img == null)
//        {
//            img = FileHandler.getIconFromResource(assetsPath, setName, iconPath);
//            if (img == null && defaultImg != null)
//            {
//                img = defaultImg;
//                try
//                {
//                    iconFile.getParentFile().mkdirs();
//                    ImageIO.write(img, "png", iconFile);
//                }
//                catch (Exception e)
//                {
//                    String error = "FileHandler can't write image: " + iconFile + ": " + e;
//                    Journeymap.getLogger().error(error);
//                }
//
//                Journeymap.getLogger().debug("Created image: " + iconFile);
//            }
//            else
//            {
//                String pngPath = Joiner.on('/').join(assetsPath, setName, iconPath);
//                Journeymap.getLogger().error(String.format("Can't get image from file (%s) nor resource (%s) ", iconFile, pngPath));
//            }
//        }
//    }

    /**
     * Gets icon from resource.
     *
     * @param assetsPath the assets path
     * @param setName    the set name
     * @param iconPath   the icon path
     * @return the icon from resource
     */
    public static BufferedImage getIconFromResource(String assetsPath, String setName, String iconPath)
    {
        try
        {
            InputStream is = getIconStream(assetsPath, setName, iconPath);
            if (is == null)
            {
                return null;
            }
            BufferedImage img = ImageIO.read(is);
            is.close();
            return img;
        }
        catch (IOException e)
        {
            String error = String.format("Could not get icon from resource: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    /**
     * Gets icon stream.
     *
     * @param assetsPath the assets path
     * @param setName    the set name
     * @param iconPath   the icon path
     * @return the icon stream
     */
    public static InputStream getIconStream(String assetsPath, String setName, String iconPath)
    {
        try
        {
            String pngPath = Joiner.on('/').join(assetsPath, setName, iconPath);
            InputStream is = JourneymapClient.class.getResourceAsStream(pngPath);
            if (is == null)
            {
                Journeymap.getLogger().warn(String.format("Icon Set asset not found: " + pngPath));
                return null;
            }
            return is;
        }
        catch (Throwable e)
        {
            String error = String.format("Could not get icon stream: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    private static class ZipEntryByteSource extends ByteSource
    {
        /**
         * The File.
         */
        final ZipFile file;
        /**
         * The Entry.
         */
        final ZipEntry entry;

        /**
         * Instantiates a new Zip entry byte source.
         *
         * @param file  the file
         * @param entry the entry
         */
        ZipEntryByteSource(ZipFile file, ZipEntry entry)
        {
            this.file = file;
            this.entry = entry;
        }

        @Override
        public InputStream openStream() throws IOException
        {
            return file.getInputStream(entry);
        }

        @Override
        public String toString()
        {
            return String.format("ZipEntryByteSource( %s / %s )", file, entry);
        }
    }
}
