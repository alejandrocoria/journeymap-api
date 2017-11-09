/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.io;

import com.google.common.base.Joiner;
import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.log.ChatLog;
import journeymap.client.log.StatTimer;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merges all region files into a single image
 *
 * @author techbrew
 */
public class MapSaver
{
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    /**
     * The World dir.
     */
    final File worldDir;
    /**
     * The Map type.
     */
    final MapType mapType;
    /**
     * The Save file.
     */
    File saveFile;
    /**
     * The Output columns.
     */
    int outputColumns;
    /**
     * The Output rows.
     */
    int outputRows;
    /**
     * The Files.
     */
    ArrayList<File> files;

    /**
     * Instantiates a new Map saver.
     *
     * @param worldDir the world dir
     * @param mapType  the map type
     */
    public MapSaver(File worldDir, MapType mapType)
    {
        super();
        this.worldDir = worldDir;
        this.mapType = mapType;

        prepareFiles();
    }

    /**
     * Use pngj to assemble region files.
     *
     * @return the file
     */
    public File saveMap()
    {

        StatTimer timer = StatTimer.get("MapSaver.saveMap");

        try
        {

            if (!isValid())
            {
                Journeymap.getLogger().warn("No images found in " + getImageDir());
                return null;
            }

            // Ensure latest regions are flushed to disk synchronously before continuing
            RegionImageCache.INSTANCE.flushToDisk(false);

            timer.start();

            // Merge image files
            File[] fileArray = files.toArray(new File[files.size()]);
            PngjHelper.mergeFiles(fileArray, saveFile, outputColumns, 512);

            timer.stop();
            Journeymap.getLogger().info("Map filesize:" + saveFile.length()); //$NON-NLS-1$ //$NON-NLS-2$

            String message = Constants.getString("jm.common.map_saved", saveFile);
            ChatLog.announceFile(message, saveFile);
            //FileHandler.open(saveFile);

        }
        catch (java.lang.OutOfMemoryError e)
        {
            String error = "Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.";
            Journeymap.getLogger().error(error);
            ChatLog.announceError(error);
            timer.cancel();
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            timer.cancel();
            return null;
        }

        return saveFile;
    }

    /**
     * Gets save file name.
     *
     * @return the save file name
     */
    public String getSaveFileName()
    {
        return saveFile.getName();
    }

    /**
     * Is valid boolean.
     *
     * @return the boolean
     */
    public boolean isValid()
    {
        return files != null && files.size() > 0;
    }

    private File getImageDir()
    {
        // Fake coord gets us to the image directory
        RegionCoord fakeRc = new RegionCoord(worldDir, 0, 0, mapType.dimension);
        return RegionImageHandler.getImageDir(fakeRc, mapType);
    }

    /**
     * Prepares files to be merged, returns estimatedBytes of the result.
     */
    private void prepareFiles()
    {
        try
        {
            // Build save file name
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final String date = dateFormat.format(new Date());
            final String worldName = WorldData.getWorldName(mc, false);
            final String dimName = WorldData.getSafeDimensionName(new WorldData.WrappedProvider(mc.world.provider));
            final String fileName = Joiner.on("_").skipNulls().join(date, worldName, dimName, mapType.name, mapType.vSlice) + ".png";

            // Ensure screenshots directory
            File screenshotsDir = new File(FileHandler.getMinecraftDirectory(), "screenshots");
            if (!screenshotsDir.exists())
            {
                screenshotsDir.mkdir();
            }

            // Create result file
            saveFile = new File(screenshotsDir, fileName);

            // Ensure latest regions are flushed to disk synchronously before continuing
            RegionImageCache.INSTANCE.flushToDisk(false);

            // Look for pngs
            File imageDir = getImageDir();
            File[] pngFiles = imageDir.listFiles();

            final Pattern tilePattern = Pattern.compile("([^\\.]+)\\,([^\\.]+)\\.png");
            Integer minX = null, minZ = null, maxX = null, maxZ = null;

            for (File file : pngFiles)
            {
                Matcher matcher = tilePattern.matcher(file.getName());
                if (matcher.matches())
                {
                    Integer x = Integer.parseInt(matcher.group(1));
                    Integer z = Integer.parseInt(matcher.group(2));
                    if (minX == null || x < minX)
                    {
                        minX = x;
                    }
                    if (minZ == null || z < minZ)
                    {
                        minZ = z;
                    }
                    if (maxX == null || x > maxX)
                    {
                        maxX = x;
                    }
                    if (maxZ == null || z > maxZ)
                    {
                        maxZ = z;
                    }
                }
            }

            if (minX == null || maxX == null || minZ == null || maxZ == null)
            {
                Journeymap.getLogger().warn("No region files to save in " + imageDir);
                return;
            }
            // Create blank
            final long blankSize = RegionImageHandler.getBlank512x512ImageFile().length();

            outputColumns = (maxX - minX) + 1;
            outputRows = (maxZ - minZ) + 1;
            files = new ArrayList<File>(outputColumns * outputRows);
            File rfile;
            RegionCoord rc;

            // Sum the sizes of the files
            for (int rz = minZ; rz <= maxZ; rz++)
            {
                for (int rx = minX; rx <= maxX; rx++)
                {
                    rc = new RegionCoord(worldDir, rx, rz, mapType.dimension);
                    rfile = RegionImageHandler.getRegionImageFile(rc, mapType, true);
                    if (rfile.canRead())
                    {
                        files.add(rfile);
                    }
                    else
                    {
                        files.add(RegionImageHandler.getBlank512x512ImageFile());
                    }
                }
            }

        }
        catch (Throwable t)
        {
            Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(t));
        }

    }

}
