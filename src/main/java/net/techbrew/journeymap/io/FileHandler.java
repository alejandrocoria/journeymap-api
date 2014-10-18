/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io;


import com.google.common.base.Joiner;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.log.LogFormatter;
import org.apache.logging.log4j.Level;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileHandler
{
    public static final String ASSETS_JOURNEYMAP = "/assets/journeymap";
    public static final String ASSETS_JOURNEYMAP_WEB = "/assets/journeymap/web";

    private static final File MinecraftDirectory = FMLClientHandler.instance().getClient().mcDataDir;
    private static final File JourneyMapDirectory = new File(MinecraftDirectory, Constants.JOURNEYMAP_DIR);

    public static File getMCWorldDir(Minecraft minecraft)
    {
        if (minecraft.isIntegratedServerRunning())
        {
            String lastMCFolderName = minecraft.getIntegratedServer().getFolderName();
            File lastMCWorldDir = new File(minecraft.mcDataDir, "saves" + File.separator + lastMCFolderName);
            return lastMCWorldDir;
        }
        return null;
    }

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
                dimDir = new File(worldDir, "DIM" + dimString); //$NON-NLS-1$
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

    public static void migrateJourneyMapDir()
    {
        try
        {

            File[] files = MinecraftDirectory.listFiles();
            if (files != null)
            {
                for (File legacyDir : files)
                {
                    if (legacyDir.isDirectory() && legacyDir.getName().equals(Constants.JOURNEYMAP_DIR_LEGACY))
                    {

                        try
                        {
                            JourneyMap.getLogger().info(String.format("Renaming \"%s\" to \"%s\".",
                                    legacyDir, JourneyMapDirectory));
                            File backupDir = new File(MinecraftDirectory, Constants.JOURNEYMAP_DIR_BACKUP);
                            legacyDir.renameTo(backupDir);
                            backupDir.renameTo(JourneyMapDirectory);
                        }
                        catch (Throwable t)
                        {
                            JMLogger.logOnce(String.format("Could not rename \"%s\" to \"%s\" ! Please shut down and rename it manually.",
                                    legacyDir, JourneyMapDirectory), t);
                        }
                        break;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JMLogger.logOnce(String.format("Could not rename old directory to \"%s\" ! Please shut down and rename it manually.",
                    JourneyMapDirectory), t);
        }
    }

    public static File getJourneyMapDir()
    {
        return JourneyMapDirectory;
    }


    public static File getJMWorldDir(Minecraft minecraft)
    {
        if (minecraft.theWorld == null)
        {
            return null;
        }

        if (!minecraft.isSingleplayer())
        {
            return getJMWorldDir(minecraft, JourneyMap.getInstance().getCurrentWorldId());
        }
        else
        {
            return getJMWorldDir(minecraft, null);
        }
    }

    public static File getJMWorldDir(Minecraft minecraft, String worldUid)
    {
        if (minecraft.theWorld == null)
        {
            return null;
        }

        File worldDir = null;

        try
        {
            if (!minecraft.isSingleplayer())
            {
                String legacyWorldName = WorldData.getWorldName(minecraft, true);
                File legacyWorldDir = new File(MinecraftDirectory, Constants.MP_DATA_DIR + legacyWorldName + "_0"); //$NON-NLS-1$

                String suffix = "";
                if (worldUid != null)
                {
                    suffix = "_" + worldUid;
                }
                worldDir = new File(MinecraftDirectory, Constants.MP_DATA_DIR + WorldData.getWorldName(minecraft, false) + suffix); //$NON-NLS-1$

                if (legacyWorldDir.exists())
                {
                    migrateLegacyFolderName(legacyWorldDir, worldDir);
                }
            }
            else
            {
                File legacyWorldDir = new File(MinecraftDirectory, Constants.SP_DATA_DIR + WorldData.getWorldName(minecraft, true));
                worldDir = new File(MinecraftDirectory, Constants.SP_DATA_DIR + WorldData.getWorldName(minecraft, false));

                if (!legacyWorldDir.getName().equals(worldDir.getName()))
                {
                    if (legacyWorldDir.exists() && worldDir.exists())
                    {
                        JMLogger.logOnce(String.format("Found two directories that might be in conflict. Using:  %s , Ignoring: %s", worldDir, legacyWorldDir), null);
                    }
                }

                if (legacyWorldDir.exists() && !worldDir.exists())
                {
                    migrateLegacyFolderName(legacyWorldDir, worldDir);
                }
            }

            worldDir.mkdirs();
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
            throw new RuntimeException(e);
        }

        return worldDir;
    }

    private static void migrateLegacyFolderName(File legacyWorldDir, File worldDir)
    {
        boolean success = false;
        try
        {
            success = legacyWorldDir.renameTo(worldDir);
            if (!success)
            {
                throw new IllegalStateException("Need to rename legacy folder, but not able to");
            }
            JourneyMap.getLogger().info(String.format("Migrated legacy folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()));
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn(String.format("Failed to migrate legacy folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()));

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
                JourneyMap.getLogger().warn(String.format("Failed to even rename legacy folder from %s to %s", legacyWorldDir.getName(), tempName));
            }
        }
    }

    public static File getWaypointDir()
    {
        return getWaypointDir(getJMWorldDir(FMLClientHandler.instance().getClient()));
    }

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


    public static File getAnvilRegionDirectory(File worldDirectory, int dimension)
    {
        if (dimension == 0)
        {
            return new File(worldDirectory, "region"); //$NON-NLS-1$
        }
        else
        {
            return new File(worldDirectory, "DIM" + dimension); //$NON-NLS-1$
        }
    }

    public static BufferedImage getWebImage(String fileName)
    {
        try
        {
            String png = FileHandler.ASSETS_JOURNEYMAP_WEB + "/img/" + fileName;//$NON-NLS-1$
            InputStream is = JourneyMap.class.getResourceAsStream(png);
            if (is == null)
            {
                JourneyMap.getLogger().warn("Image not found: " + png);
                return null;
            }
            BufferedImage img = ImageIO.read(is);
            is.close();
            return img;
        }
        catch (IOException e)
        {
            String error = "Could not get web image " + fileName + ": " + e.getMessage();
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static Properties getLangFile(String fileName)
    {
        try
        {
            InputStream is = JourneyMap.class.getResourceAsStream("/assets/journeymap/lang/" + fileName);
            if (is == null)
            {
                JourneyMap.getLogger().warn("Language file not found: " + fileName);
                return null;
            }
            Properties properties = new Properties();
            properties.load(is);
            is.close();
            return properties;
        }
        catch (IOException e)
        {
            String error = "Could not get language file " + fileName + ": " + (e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static File getWorldConfigDir(boolean fallbackToStandardConfigDir)
    {
        File worldDir = getJMWorldDir(FMLClientHandler.instance().getClient(), null); // always use the "base" folder for multiplayer
        if (worldDir != null)
        {
            File worldConfigDir = new File(worldDir, "config");
            if (worldConfigDir.exists())
            {
                return worldConfigDir;
            }
        }

        return fallbackToStandardConfigDir ? getStandardConfigDir() : null;
    }

    public static File getStandardConfigDir()
    {
        return new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.CONFIG_DIR);
    }

//    public static File getCacheDir()
//    {
//        return new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.CACHE_DIR);
//    }


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
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static boolean isInJar()
    {
        URL location = JourneyMap.class.getProtectionDomain().getCodeSource().getLocation();
        return "jar".equals(location.getProtocol());
    }

    public static File copyColorPaletteHtmlFile(File toDir, String fileName)
    {
        try
        {
            final File outFile = new File(toDir, fileName);
            String htmlPath = FileHandler.ASSETS_JOURNEYMAP_WEB + "/" + fileName;
            InputStream inputStream = JourneyMap.class.getResource(htmlPath).openStream();

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
            JourneyMap.getLogger().warn("Couldn't copy color palette html: " + t);
            return null;
        }
    }

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
                JourneyMap.getLogger().error("Could not open path with /usr/bin/open: " + path + " : " + LogFormatter.toString(e));
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
                    JourneyMap.getLogger().error("Could not open path with cmd.exe: " + path + " : " + LogFormatter.toString(e));
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
            JourneyMap.getLogger().error("Could not open path with Desktop: " + path + " : " + LogFormatter.toString(e));
            Sys.openURL("file://" + path);
        }
    }

    public static void copyResources(File targetDirectory, String assetsPath, String setName, boolean overwrite)
    {
        String fromPath = null;
        File toDir = null;
        try
        {
            URL resourceDir = JourneyMap.class.getResource(assetsPath);
            String toPath = String.format("%s/%s", assetsPath, setName);
            toDir = new File(targetDirectory, setName);
            boolean inJar = FileHandler.isInJar();
            if (inJar)
            {
                fromPath = URLDecoder.decode(resourceDir.getPath(), "utf-8").split("file:")[1].split("!/")[0];
                FileHandler.copyFromZip(fromPath, toPath, toDir, overwrite);
            }
            else
            {
                File fromDir = new File(JourneyMap.class.getResource(toPath).getFile());
                fromPath = fromDir.getPath();
                FileHandler.copyFromDirectory(fromDir, toDir, overwrite);
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error(String.format("Couldn't unzip resource set from %s to %s: %s", fromPath, toDir, t));
        }
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @throws IOException
     */
    static void copyFromZip(String zipFilePath, String zipEntryName, File destDir, boolean overWrite) throws Throwable
    {

        if (zipEntryName.startsWith("/"))
        {
            zipEntryName = zipEntryName.substring(1);
        }
        final ZipFile zipFile = new ZipFile(zipFilePath);
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();

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
    }

    /**
     * Copies contents of one directory to another
     */
    static void copyFromDirectory(File fromDir, File toDir, boolean overWrite) throws IOException
    {
        toDir.mkdir();
        File[] files = fromDir.listFiles();

        if (files == null)
        {
            throw new IOException(fromDir + " nas no files");
        }

        for (File from : files)
        {
            File to = new File(toDir, from.getName());
            if (from.isDirectory())
            {
                copyFromDirectory(from, to, overWrite);
            }
            else if (overWrite || !to.exists())
            {
                Files.copy(from, to);
            }
        }
    }

    public static BufferedImage getIconFromFile(File parentdir, String assetsPath, String setName, String iconPath, BufferedImage defaultImg)
    {
        BufferedImage img = null;

        try
        {
            String filePath = Joiner.on(File.separatorChar).join(setName, iconPath.replace('/', File.separatorChar));
            File iconFile = new File(parentdir, filePath);


            if (iconFile.exists())
            {
                img = FileHandler.getImage(iconFile);
            }

            if (img == null)
            {
                img = FileHandler.getIconFromResource(assetsPath, setName, iconPath);
                if (img == null && defaultImg != null)
                {
                    img = defaultImg;
                    try
                    {
                        iconFile.getParentFile().mkdirs();
                        ImageIO.write(img, "png", iconFile);
                    }
                    catch (Exception e)
                    {
                        String error = "FileHandler can't write image: " + iconFile + ": " + e;
                        JourneyMap.getLogger().error(error);
                    }

                    JourneyMap.getLogger().debug("Created image: " + iconFile);
                }
                else
                {
                    String pngPath = Joiner.on('/').join(assetsPath, setName, iconPath);
                    JourneyMap.getLogger().error(String.format("Can't get image from file (%s) nor resource (%s) ", iconFile, pngPath));
                }
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().error("Couldn't load iconset file: " + LogFormatter.toString(e));
        }

        return img;
    }

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
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static InputStream getIconStream(String assetsPath, String setName, String iconPath)
    {
        try
        {
            String pngPath = Joiner.on('/').join(assetsPath, setName, iconPath);
            InputStream is = JourneyMap.class.getResourceAsStream(pngPath);
            if (is == null)
            {
                JourneyMap.getLogger().warn(String.format("Icon Set asset not found: " + pngPath));
                return null;
            }
            return is;
        }
        catch (Throwable e)
        {
            String error = String.format("Could not get icon stream: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    private static class ZipEntryByteSource extends ByteSource
    {
        final ZipFile file;
        final ZipEntry entry;

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
