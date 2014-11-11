/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;
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
 * @author Mark
 */
public class MapSaver
{

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    final File worldDir;
    final Constants.MapType mapType;
    final Integer vSlice;
    final int dimension;
    File saveFile;
    int outputColumns;
    int outputRows;
    ArrayList<File> files;

    public MapSaver(File worldDir, MapType mapType, Integer vSlice, int dimension)
    {
        super();
        this.worldDir = worldDir;
        this.mapType = mapType;
        this.vSlice = vSlice;
        this.dimension = dimension;

        prepareFiles();
    }

    /**
     * Use pngj to assemble region files.
     */
    public File saveMap()
    {

        StatTimer timer = StatTimer.get("MapSaver.saveMap");

        try
        {

            if (!isValid())
            {
                JourneyMap.getLogger().warn("No images found in " + getImageDir());
                return null;
            }

            // Ensure latest regions are flushed to disk
            RegionImageCache.getInstance().flushToDisk();

            timer.start();

            // Merge image files
            File[] fileArray = files.toArray(new File[files.size()]);
            PngjHelper.mergeFiles(fileArray, saveFile, outputColumns, 512);

            timer.stop();
            JourneyMap.getLogger().info("Map filesize:" + saveFile.length()); //$NON-NLS-1$ //$NON-NLS-2$

            String message = Constants.getString("jm.common.map_saved", saveFile);
            ChatLog.announceFile(message, saveFile);
            //FileHandler.open(saveFile);

        }
        catch (java.lang.OutOfMemoryError e)
        {
            String error = "Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.";
            JourneyMap.getLogger().error(error);
            ChatLog.announceError(error);
            timer.cancel();
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error(LogFormatter.toString(t));
            timer.cancel();
            return null;
        }

        return saveFile;
    }

    public String getSaveFileName()
    {
        return saveFile.getName();
    }

    public boolean isValid()
    {
        return files != null && files.size() > 0;
    }

    private File getImageDir()
    {
        // Fake coord gets us to the image directory
        RegionCoord fakeRc = new RegionCoord(worldDir, 0, vSlice, 0, dimension);
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
            final boolean isUnderground = mapType.equals(Constants.MapType.underground);
            final StringBuilder sb = new StringBuilder(date).append("_");
            sb.append(WorldData.getWorldName(mc, false)).append("_");
            sb.append(WorldData.getSafeDimensionName(mc.theWorld.provider)).append("_");
            if (isUnderground)
            {
                sb.append("slice").append(vSlice);
            }
            else
            {
                sb.append(mapType);
            }
            sb.append(".png");

            // Ensure screenshots directory
            File screenshotsDir = new File(FMLClientHandler.instance().getClient().mcDataDir, "screenshots");
            if (!screenshotsDir.exists())
            {
                screenshotsDir.mkdir();
            }

            // Create result file
            saveFile = new File(screenshotsDir, sb.toString());

            // Ensure latest regions are flushed to disk
            RegionImageCache.getInstance().flushToDisk();

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
                JourneyMap.getLogger().warn("No region files to save in " + imageDir);
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
                    rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
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
            JourneyMap.getLogger().log(Level.ERROR, LogFormatter.toString(t));
        }

    }

}
