/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io;


import com.google.common.base.Joiner;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.Utils;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import org.apache.logging.log4j.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileHandler
{
    public static final String ASSETS_JOURNEYMAP = "/assets/journeymap";
    public static final String ASSETS_JOURNEYMAP_WEB = "/assets/journeymap/web";
    public static final String ASSETS_JOURNEYMAP_ICON_ENTITY = "/assets/journeymap/icon/entity";

    public final static String MOB_ICON_SET_2D = "2D";
    public final static String MOB_ICON_SET_3D = "3D";

    public static volatile File lastJMWorldDir;

    public static volatile String lastMCFolderName = "";
    public static volatile File lastMCWorldDir = null;

    public static File getMCWorldDir(Minecraft minecraft)
    {
        if (minecraft.isIntegratedServerRunning())
        {
            if (lastMCWorldDir == null || !lastMCFolderName.equals(minecraft.getIntegratedServer().getFolderName()))
            {
                lastMCFolderName = minecraft.getIntegratedServer().getFolderName();
                lastMCWorldDir = new File(minecraft.mcDataDir, "saves" + File.separator + lastMCFolderName);
            }
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

    public static File getJourneyMapDir()
    {
        return new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.JOURNEYMAP_DIR);
    }


    public static File getJMWorldDir(Minecraft minecraft)
    {
        if(minecraft.theWorld==null)
        {
            return null;
        }

        if (!minecraft.isSingleplayer())
        {
            return getJMWorldDir(minecraft, Utils.getWorldHash(minecraft));
        }
        else
        {
            return getJMWorldDir(minecraft, -1L);
        }
    }

    public static File getJMWorldDir(Minecraft minecraft, long hash)
    {
        if(minecraft.theWorld==null)
        {
            return null;
        }

        File mcDir = minecraft.mcDataDir;

        if (lastJMWorldDir == null)
        {
            File worldDir = null;

            try
            {
                String worldName = WorldData.getWorldName(minecraft);
                if (!minecraft.isSingleplayer())
                {
                    String legacyWorldName = WorldData.getWorldName(minecraft, true);
                    File legacyWorldDir = new File(mcDir, Constants.MP_DATA_DIR + legacyWorldName + "_" + hash); //$NON-NLS-1$
                    worldDir = new File(mcDir, Constants.MP_DATA_DIR + worldName + "_" + hash); //$NON-NLS-1$

                    if(legacyWorldDir.exists())
                    {
                        migrateLegacyServerName(legacyWorldDir, worldDir);
                    }
                }
                else
                {
                    worldDir = new File(mcDir, Constants.SP_DATA_DIR + worldName);
                }

                worldDir.mkdirs();
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
                throw new RuntimeException(e);
            }

            lastJMWorldDir = worldDir;
        }
        return lastJMWorldDir;
    }

    private static void migrateLegacyServerName(File legacyWorldDir, File worldDir)
    {
        boolean success = false;
        try
        {
            success = legacyWorldDir.renameTo(worldDir);
            if(!success)
            {
                throw new IllegalStateException("Need to rename legacy folder, but not able to");
            }
            JourneyMap.getLogger().info(String.format("Migrated legacy server folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()));
        }
        catch(Exception e)
        {
            JourneyMap.getLogger().warn(String.format("Failed to migrate legacy server folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()));

            String tempName = worldDir.getName() + "__OLD";
            try
            {
                success = legacyWorldDir.renameTo(new File(legacyWorldDir.getParentFile(), tempName));
            }
            catch (Exception e2)
            {
                success = false;
            }
            if(!success)
            {
                JourneyMap.getLogger().warn(String.format("Failed to even rename legacy server folder from %s to %s", legacyWorldDir.getName(), tempName));
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
            String error = Constants.getMessageJMERR17(e.getMessage());
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
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static File getWorldConfigDir(boolean fallbackToStandardConfigDir)
    {
        File worldDir = getJMWorldDir(FMLClientHandler.instance().getClient());
        if(worldDir!=null)
        {
            File worldConfigDir = new File(worldDir, "config");
            boolean found = worldConfigDir.exists() ||  worldConfigDir.mkdirs();
            if(found)
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

    public static File getCacheDir()
    {
        return new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.CACHE_DIR);
    }

    public static File getEntityIconDir()
    {
        File dir = new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.ENTITY_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

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
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static boolean isInJar()
    {
        URL location = JourneyMap.class.getProtectionDomain().getCodeSource().getLocation();
        return "jar".equals(location.getProtocol());
    }

    public static void initMobIconSets()
    {
        JourneyMap.getLogger().info("Initializing mob icon sets...");

        StatTimer timer = StatTimer.getDisposable("initMobIconSets").start();

        final List<String> mobIconSetNames = Arrays.asList(MOB_ICON_SET_2D, MOB_ICON_SET_3D);
        boolean inJar = FileHandler.isInJar();

        for (String setName : mobIconSetNames)
        {
            try
            {
                URL resourceDir = JourneyMap.class.getResource(FileHandler.ASSETS_JOURNEYMAP_ICON_ENTITY);
                String toPath = String.format("%s/%s", ASSETS_JOURNEYMAP_ICON_ENTITY, setName);
                File toDir = new File(getEntityIconDir(), setName);
                if (inJar)
                {
                    String fromPath = resourceDir.getPath().split("file:")[1].split("!/")[0];
                    copyFromZip(fromPath, toPath, toDir, false);
                }
                else
                {
                    File fromDir = new File(JourneyMap.class.getResource(toPath).getFile());
                    copyFromDirectory(fromDir, toDir, false);
                }
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().warn("Couldn't unzip mob icon set for " + setName + ": " + t);
            }
        }

        JourneyMap.getLogger().info(timer.stopAndReport());
    }

    public static ArrayList<String> getMobIconSetNames()
    {
        String[] defaultIconSets = {MOB_ICON_SET_2D, MOB_ICON_SET_3D};

        File entityIconDir = getEntityIconDir();

        try
        {
            // Initialize entity iconset folders
            for (String iconSetName : defaultIconSets)
            {
                File iconSetDir = new File(entityIconDir, iconSetName);
                if (iconSetDir.exists() && !iconSetDir.isDirectory())
                {
                    iconSetDir.delete();
                }
                iconSetDir.mkdirs();
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Could not prepare entity iconset directories: " + LogFormatter.toString(t));
        }

        // Create list of icon set names
        ArrayList<String> names = new ArrayList<String>();
        for (File iconSetDir : entityIconDir.listFiles())
        {
            if (iconSetDir.isDirectory())
            {
                names.add(iconSetDir.getName());
            }
        }
        Collections.sort(names);

        return names;
    }

    public static BufferedImage getEntityIconFromFile(String setName, String iconPath, BufferedImage defaultImg)
    {
        String filePath = Joiner.on(File.separatorChar).join(setName, iconPath.replace('/', File.separatorChar));
        File iconFile = new File(getEntityIconDir(), filePath);

        BufferedImage img = null;
        if (iconFile.exists())
        {
            img = getImage(iconFile);
        }

        if (img == null)
        {
            img = FileHandler.getEntityIconFromResource(setName, iconPath);
            if (img == null)
            {
                img = defaultImg;
            }

            try
            {
                iconFile.getParentFile().mkdirs();
                ImageIO.write(img, "png", iconFile);
            }
            catch (Exception e)
            {
                String error = Constants.getMessageJMERR00("Can't write entity icon" + iconFile + ": " + e);
                JourneyMap.getLogger().error(error);
            }

            JourneyMap.getLogger().debug("Created entity icon: " + iconFile);
        }

        return img;
    }

    public static BufferedImage getEntityIconFromResource(String setName, String iconPath)
    {
        try
        {
            InputStream is = getEntityIconStream(setName, iconPath);
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
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static InputStream getEntityIconStream(String setName, String iconPath)
    {
        try
        {
            String pngPath = Joiner.on('/').join(ASSETS_JOURNEYMAP_ICON_ENTITY, setName, iconPath);
            InputStream is = JourneyMap.class.getResourceAsStream(pngPath);
            if (is == null)
            {
                JourneyMap.getLogger().warn(String.format("Entity Icon resource not found: " + pngPath));
                return null;
            }
            return is;
        }
        catch (Throwable e)
        {
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static File copyColorPaletteHtmlFile(File toDir, String fileName)
    {
        try
        {
            File outFile = new File(toDir, fileName);
            String htmlPath = FileHandler.ASSETS_JOURNEYMAP_WEB + "/" + fileName;
            File htmlFile = new File(JourneyMap.class.getResource(htmlPath).getFile());
            Files.copy(htmlFile, outFile);
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

    public static boolean serializeCache(String name, Serializable cache)
    {
        try
        {
            File cacheDir = getCacheDir();
            if (!cacheDir.exists())
            {
                cacheDir.mkdirs();
            }

            File cacheFile = new File(cacheDir, name);

            FileOutputStream fileOut = new FileOutputStream(cacheFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(cache);
            out.close();
            fileOut.close();
            return true;
        }
        catch (IOException e)
        {
            JourneyMap.getLogger().error("Could not serialize cache: " + name + " : " + LogFormatter.toString(e));
            return false;
        }
    }

    public static boolean writeDebugFile(String name, String contents)
    {
        try
        {
            File debugFile = new File(getJourneyMapDir(), "DEBUG-" + name);
            FileWriter writer = new FileWriter(debugFile, false);
            writer.write(contents);
            writer.flush();
            writer.close();
            return true;
        }
        catch (IOException e)
        {
            JourneyMap.getLogger().error("Could not write debug file: " + name + " : " + LogFormatter.toString(e));
            return false;
        }
    }

    public static <C extends Serializable> C deserializeCache(String name, Class<C> cacheClass)
    {

        File cacheFile = new File(getCacheDir(), name);
        if (!cacheFile.exists())
        {
            return null;
        }
        try
        {
            FileInputStream fileIn = new FileInputStream(cacheFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            C cache = (C) in.readObject();
            in.close();
            fileIn.close();
            if (cache.getClass() != cacheClass)
            {
                throw new ClassCastException(cache.getClass() + " can't be cast to " + cacheClass);
            }
            return cache;
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn("Could not deserialize cache: " + name + " : " + e);
            if (cacheFile.exists())
            {
                cacheFile.delete();
            }
            return null;
        }
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @throws IOException
     */
    private static void copyFromZip(String zipFilePath, String zipEntryName, File destDir, boolean overWrite) throws Throwable
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
                            toFile.getParentFile().mkdirs();
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
    private static void copyFromDirectory(File fromDir, File toDir, boolean overWrite) throws IOException
    {
        toDir.mkdir();
        for (File from : fromDir.listFiles())
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
