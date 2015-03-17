/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io;


import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.render.map.Tile;
import net.techbrew.journeymap.render.map.TileDrawStep;
import net.techbrew.journeymap.render.map.TileDrawStepCache;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegionImageHandler
{
    private RegionImageHandler()
    {
    }

    // Get singleton instance.  Concurrency-safe.
    public static RegionImageHandler getInstance()
    {
        return Holder.INSTANCE;
    }

    public static File getImageDir(RegionCoord rCoord, MapType mapType)
    {
        File dimDir = rCoord.dimDir.toFile();
        File subDir = null;
        if (rCoord.isUnderground())
        {
            subDir = new File(dimDir, Integer.toString(rCoord.getVerticalSlice()));
        }
        else
        {
            subDir = new File(dimDir, mapType.name());
        }
        if (!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }

    @Deprecated
    public static File getDimensionDir(File worldDir, int dimension)
    {
        File dimDir = new File(worldDir, "DIM" + dimension); //$NON-NLS-1$
        if (!dimDir.exists())
        {
            dimDir.mkdirs();
        }
        return dimDir;
    }

    public static File getRegionImageFile(RegionCoord rCoord, Constants.MapType mapType, boolean allowLegacy)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(rCoord.regionX).append(",").append(rCoord.regionZ).append(".png"); //$NON-NLS-1$ //$NON-NLS-2$
        File regionFile = new File(getImageDir(rCoord, mapType), sb.toString());

        return regionFile;
    }

    public static BufferedImage createBlankImage(int width, int height)
    {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = img.createGraphics();
        graphics2D.setFont(new Font("Arial", Font.BOLD, 18));
        graphics2D.setBackground(Color.black);
        graphics2D.setColor(Color.yellow);
        graphics2D.drawString("BLANK", 0, 0);
        return img;
    }

    public static BufferedImage readRegionImage(File regionFile, boolean returnNull)
    {
        FileInputStream fis = null;
        BufferedImage image = null;

        if (regionFile.exists() && regionFile.canRead())
        {
            try
            {
                image = ImageIO.read(regionFile);
            }
            catch (Exception e)
            {
                String error = "Region file produced error: " + regionFile + ": " + LogFormatter.toString(e);
                JourneyMap.getLogger().error(error);
            }
        }

        if (image == null)
        {
            if (!returnNull)
            {
                image = createBlankImage(512, 512);
            }
        }
        return image;
    }

    public static BufferedImage getImage(File file)
    {
        try
        {
            return ImageIO.read(file);
        }
        catch (IOException e)
        {
            String error = "Could not get image from file: " + file + ": " + (e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    /**
     * Used by MapOverlay to let the image dimensions be directly specified (as a power of 2).
     */
    public static synchronized BufferedImage getMergedChunks(final File worldDir, final ChunkCoordIntPair startCoord, final ChunkCoordIntPair endCoord, final Constants.MapType mapType, Integer vSlice, final int dimension, final Boolean useCache, BufferedImage image, final Integer imageWidth, final Integer imageHeight, final boolean allowNullImage, boolean showGrid)
    {

        long start = 0, stop = 0;
        start = System.currentTimeMillis();

        boolean isUnderground = mapType.equals(Constants.MapType.underground);
        if (!isUnderground)
        {
            vSlice = null;
        }

        final int initialWidth = (endCoord.chunkXPos - startCoord.chunkXPos + 1) * 16;
        final int initialHeight = (endCoord.chunkZPos - startCoord.chunkZPos + 1) * 16;

        if (image == null || image.getWidth() != initialWidth || imageHeight != initialHeight)
        {
            image = new BufferedImage(initialWidth, initialHeight, BufferedImage.TYPE_INT_ARGB);
        }
        final Graphics2D g2D = initRenderingHints(image.createGraphics());
        g2D.clearRect(0, 0, imageWidth, imageHeight);

        final RegionImageCache cache = DataCache.instance().getRegionImageCache();

        RegionCoord rc = null;
        BufferedImage regionImage = null;

        final int rx1 = RegionCoord.getRegionPos(startCoord.chunkXPos);
        final int rx2 = RegionCoord.getRegionPos(endCoord.chunkXPos);
        final int rz1 = RegionCoord.getRegionPos(startCoord.chunkZPos);
        final int rz2 = RegionCoord.getRegionPos(endCoord.chunkZPos);

        int rminCx, rminCz, rmaxCx, rmaxCz, sx1, sy1, sx2, sy2, dx1, dx2, dy1, dy2;

        boolean imageDrawn = false;
        for (int rx = rx1; rx <= rx2; rx++)
        {
            for (int rz = rz1; rz <= rz2; rz++)
            {
                rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
                if (cache.contains(rc))
                {
                    regionImage = cache.getGuaranteedImage(rc, mapType);
                }
                else
                {
                    regionImage = RegionImageHandler.readRegionImage(RegionImageHandler.getRegionImageFile(rc, mapType, false), false);
                }

                if (regionImage == null)
                {
                    continue;
                }

                rminCx = Math.max(rc.getMinChunkX(), startCoord.chunkXPos);
                rminCz = Math.max(rc.getMinChunkZ(), startCoord.chunkZPos);
                rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.chunkXPos);
                rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.chunkZPos);

                int xoffset = rc.getMinChunkX() * 16;
                int yoffset = rc.getMinChunkZ() * 16;
                sx1 = (rminCx * 16) - xoffset;
                sy1 = (rminCz * 16) - yoffset;
                sx2 = sx1 + ((rmaxCx - rminCx + 1) * 16);
                sy2 = sy1 + ((rmaxCz - rminCz + 1) * 16);

                xoffset = startCoord.chunkXPos * 16;
                yoffset = startCoord.chunkZPos * 16;
                dx1 = (startCoord.chunkXPos * 16) - xoffset;
                dy1 = (startCoord.chunkZPos * 16) - yoffset;
                dx2 = dx1 + ((endCoord.chunkXPos - startCoord.chunkXPos + 1) * 16);
                dy2 = dy1 + ((endCoord.chunkZPos - startCoord.chunkZPos + 1) * 16);

                g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                imageDrawn = true;
            }
        }

        // Show chunk grid
        if (imageDrawn)
        {
            if (showGrid)
            {

                if (mapType == MapType.day)
                {
                    g2D.setColor(Color.black);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.25F));
                }
                else
                {
                    g2D.setColor(Color.gray);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.1F));
                }

                for (int x = 0; x <= initialWidth; x += 16)
                {
                    g2D.drawLine(x, 0, x, initialHeight);
                }

                for (int z = 0; z <= initialHeight; z += 16)
                {
                    g2D.drawLine(0, z, initialWidth, z);
                }
            }
        }

        g2D.dispose();

        if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
        {
            stop = System.currentTimeMillis();
            JourneyMap.getLogger().debug("getMergedChunks time: " + (stop - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (allowNullImage && !imageDrawn)
        {
            return null;
        }

        // Scale if needed
        if (imageHeight != null && imageWidth != null && (initialHeight != imageHeight || initialWidth != imageWidth))
        {
            final BufferedImage scaledImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = initRenderingHints(scaledImage.createGraphics());
            g.drawImage(image, 0, 0, imageWidth, imageHeight, null);
            g.dispose();
            return scaledImage;
        }
        else
        {
            return image;
        }

    }

    public static BufferedImage getScaledRegionArea(final RegionCoord rCoord, final MapType mapType, final int zoom, boolean highQuality, int x1, int y1)
    {
        RegionImageCache cache = DataCache.instance().getRegionImageCache();
        BufferedImage regionImage = cache.getGuaranteedImage(rCoord, mapType);

        if (regionImage == null)
        {
            return null;
        }

        int scale = (int) Math.pow(2, zoom);
        BufferedImage scaledImage = null;
        int scaledSize = Tile.TILESIZE / scale;

        try
        {
            BufferedImage subImage = regionImage.getSubimage(x1, y1, scaledSize, scaledSize);
            scaledImage = new BufferedImage(Tile.TILESIZE, Tile.TILESIZE, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = initRenderingHints(scaledImage.createGraphics());
            g.drawImage(subImage, 0, 0, Tile.TILESIZE, Tile.TILESIZE, null);
            g.dispose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            boolean good = false;
        }
        return scaledImage;

        /**
         BufferedImage image = new BufferedImage(regionImage.getWidth(), regionImage.getHeight(), regionImage.getType());
         final Graphics2D g2D = initRenderingHints(image.createGraphics());


         g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
         //g2D.drawImage(regionImage, 0, 0, 512, 512, sx1, sy1, sx2, sy2, Color.yellow, null);
         g2D.drawImage(regionImage, sx1, sy1, sx2-sx1, sy2-sy1,null);
         g2D.dispose();

         return image;
         */
    }


    /**
     * Used by MapOverlay to let the image dimensions be directly specified (as a power of 2).
     */
    public static synchronized List<TileDrawStep> getTileDrawSteps(final File worldDir, final ChunkCoordIntPair startCoord,
                                                                   final ChunkCoordIntPair endCoord, final Constants.MapType mapType,
                                                                   Integer zoom, boolean highQuality, Integer vSlice, final int dimension)
    {
        boolean isUnderground = mapType.equals(Constants.MapType.underground);
        if (!isUnderground)
        {
            vSlice = null;
        }

        final int rx1 = RegionCoord.getRegionPos(startCoord.chunkXPos);
        final int rx2 = RegionCoord.getRegionPos(endCoord.chunkXPos);
        final int rz1 = RegionCoord.getRegionPos(startCoord.chunkZPos);
        final int rz2 = RegionCoord.getRegionPos(endCoord.chunkZPos);

        List<TileDrawStep> drawSteps = new ArrayList<TileDrawStep>();

        RegionCoord rc;
        int rminCx, rminCz, rmaxCx, rmaxCz, sx1, sy1, sx2, sy2, dx1, dx2, dy1, dy2;

        for (int rx = rx1; rx <= rx2; rx++)
        {
            for (int rz = rz1; rz <= rz2; rz++)
            {
                rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
                rminCx = Math.max(rc.getMinChunkX(), startCoord.chunkXPos);
                rminCz = Math.max(rc.getMinChunkZ(), startCoord.chunkZPos);
                rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.chunkXPos);
                rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.chunkZPos);

                int xoffset = rc.getMinChunkX() * 16;
                int yoffset = rc.getMinChunkZ() * 16;
                sx1 = (rminCx * 16) - xoffset;
                sy1 = (rminCz * 16) - yoffset;
                sx2 = sx1 + ((rmaxCx - rminCx + 1) * 16);
                sy2 = sy1 + ((rmaxCz - rminCz + 1) * 16);

                drawSteps.add(TileDrawStepCache.getOrCreate(mapType, rc, zoom, sx1, sy1, sx2, sy2));
            }
        }

        return drawSteps;
    }

    public static File getBlank512x512ImageFile()
    {
        final File dataDir = new File(FileHandler.MinecraftDirectory, Constants.DATA_DIR);
        final File tmpFile = new File(dataDir, "blank512x512.png");
        if (!tmpFile.canRead())
        {
            BufferedImage image;
            image = createBlankImage(512, 512);
            try
            {
                dataDir.mkdirs();
                ImageIO.write(image, "png", tmpFile);
                tmpFile.setReadOnly();
                tmpFile.deleteOnExit();
            }
            catch (IOException e)
            {
                JourneyMap.getLogger().error("Could not create blank temp file " + tmpFile + ": " + LogFormatter.toString(e));
            }
        }
        return tmpFile;
    }

    public static Graphics2D initRenderingHints(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    public BufferedImage getCachedRegionImage(RegionCoord rCoord, MapType mapType)
    {
        return DataCache.instance().getRegionImageCache().getGuaranteedImage(rCoord, mapType);
    }

    // On-demand-holder for instance
    private static class Holder
    {
        private static final RegionImageHandler INSTANCE = new RegionImageHandler();
    }

}
