/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io;


import journeymap.client.Constants;
import journeymap.client.model.GridSpec;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.client.render.map.Tile;
import journeymap.client.render.map.TileDrawStep;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.ChunkPos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegionImageHandler
{

    public static File getImageDir(RegionCoord rCoord, MapType mapType)
    {
        File dimDir = rCoord.dimDir.toFile();
        File subDir = null;
        if (mapType.isUnderground())
        {
            subDir = new File(dimDir, Integer.toString(mapType.vSlice));
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

    public static File getRegionImageFile(RegionCoord rCoord, MapType mapType, boolean allowLegacy)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(rCoord.regionX).append(",").append(rCoord.regionZ).append(".png"); //$NON-NLS-1$ //$NON-NLS-2$
        File regionFile = new File(getImageDir(rCoord, mapType), sb.toString());

        return regionFile;
    }

    public static BufferedImage createBlankImage(int width, int height)
    {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = initRenderingHints(img.createGraphics());
        g.setColor(Color.black);
        g.setComposite(AlphaComposite.Clear);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        return img;
    }

    public static BufferedImage readRegionImage(File regionFile, boolean returnNull)
    {
        BufferedImage image = null;
        if (regionFile.canRead())
        {
            try
            {
                image = ImageIO.read(regionFile);
            }
            catch (Exception e)
            {
                String error = "Region file produced error: " + regionFile + ": " + LogFormatter.toPartialString(e);
                Journeymap.getLogger().error(error);
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
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    /**
     * Used by MapOverlay to let the image dimensions be directly specified (as a power of 2).
     */
    public static synchronized BufferedImage getMergedChunks(final File worldDir, final ChunkPos startCoord, final ChunkPos endCoord, final MapType mapType,
                                                             int scale, boolean showGrid)
    {
        scale = Math.max(scale, 1);
        final int initialWidth = Math.min(Tile.TILESIZE, ((endCoord.chunkXPos - startCoord.chunkXPos + 1) * 16) / scale);
        final int initialHeight = Math.min(Tile.TILESIZE, ((endCoord.chunkZPos - startCoord.chunkZPos + 1) * 16) / scale);

        BufferedImage blank = null;

        BufferedImage image = createBlankImage(initialWidth, initialHeight);
        final Graphics2D g2D = image.createGraphics();

        RegionCoord rc;
        BufferedImage regionImage;

        final int rx1 = RegionCoord.getRegionPos(startCoord.chunkXPos);
        final int rx2 = RegionCoord.getRegionPos(endCoord.chunkXPos);
        final int rz1 = RegionCoord.getRegionPos(startCoord.chunkZPos);
        final int rz2 = RegionCoord.getRegionPos(endCoord.chunkZPos);

        int rminCx, rminCz, rmaxCx, rmaxCz, sx1, sy1, sx2, sy2, dx1, dx2, dy1, dy2;

        for (int rx = rx1; rx <= rx2; rx++)
        {
            for (int rz = rz1; rz <= rz2; rz++)
            {
                rc = new RegionCoord(worldDir, rx, rz, mapType.dimension);
                regionImage = RegionImageCache.INSTANCE.getRegionImageSet(rc).getImage(mapType);

                if (regionImage == null)
                {
                    if (blank == null)
                    {
                        blank = createBlankImage(Tile.TILESIZE, Tile.TILESIZE);
                    }
                    regionImage = blank;
                }

                rminCx = Math.max(rc.getMinChunkX(), startCoord.chunkXPos);
                rminCz = Math.max(rc.getMinChunkZ(), startCoord.chunkZPos);
                rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.chunkXPos);
                rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.chunkZPos);

                // Get pixel coords of source image
                sx1 = (rminCx - rc.getMinChunkX()) * 16;
                sy1 = (rminCz - rc.getMinChunkZ()) * 16;
                sx2 = sx1 + ((rmaxCx - rminCx + 1) * 16);
                sy2 = sy1 + ((rmaxCz - rminCz + 1) * 16);

                // Get pixel coords of destination image
                dx1 = (rminCx - startCoord.chunkXPos) * 16;
                dy1 = (rminCz - startCoord.chunkZPos) * 16;
                dx2 = dx1 + (sx2 - sx1);
                dy2 = dy1 + (sy2 - sy1);

                g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
            }
        }

        // Show chunk grid
        if (showGrid)
        {
            GridSpec gridSpec = Journeymap.getClient().getCoreProperties().gridSpecs.getSpec(mapType);
            if (gridSpec != null)
            {
                BufferedImage gridImage = gridSpec.getTexture().getImage();
                g2D.setXORMode(new Color(gridSpec.getColor()));
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, gridSpec.alpha));
                g2D.drawImage(gridImage, 0, 0, initialWidth, initialHeight, null);
            }
        }

        g2D.dispose();

        // Scale if needed
        if (scale > 1)
        {
            int scaledWidth = Math.min(Tile.TILESIZE, initialWidth * scale);
            int scaledHeight = Math.min(Tile.TILESIZE, initialHeight * scale);
            final BufferedImage scaledImage = createBlankImage(scaledWidth, scaledHeight);
            final Graphics2D g = initRenderingHints(scaledImage.createGraphics());
            g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
            g.dispose();
            return scaledImage;
        }
        else
        {
            return image;
        }
    }

    /**
     * Used by MapOverlay to let the image dimensions be directly specified (as a power of 2).
     */
    public static synchronized List<TileDrawStep> getTileDrawSteps(final File worldDir, final ChunkPos startCoord,
                                                                   final ChunkPos endCoord, final MapType mapType,
                                                                   Integer zoom, boolean highQuality)
    {
        boolean isUnderground = mapType.isUnderground();

        final int rx1 = RegionCoord.getRegionPos(startCoord.chunkXPos);
        final int rx2 = RegionCoord.getRegionPos(endCoord.chunkXPos);
        final int rz1 = RegionCoord.getRegionPos(startCoord.chunkZPos);
        final int rz2 = RegionCoord.getRegionPos(endCoord.chunkZPos);

        List<TileDrawStep> drawSteps = new ArrayList<TileDrawStep>();

        RegionCoord rc;
        int rminCx, rminCz, rmaxCx, rmaxCz, sx1, sy1, sx2, sy2;

        for (int rx = rx1; rx <= rx2; rx++)
        {
            for (int rz = rz1; rz <= rz2; rz++)
            {
                rc = new RegionCoord(worldDir, rx, rz, mapType.dimension);
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

                drawSteps.add(TileDrawStepCache.getOrCreate(mapType, rc, zoom, highQuality, sx1, sy1, sx2, sy2));
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
                Journeymap.getLogger().error("Could not create blank temp file " + tmpFile + ": " + LogFormatter.toString(e));
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
}
