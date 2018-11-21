/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.io;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkLoadBehaviour;
import journeymap.common.Journeymap;

import java.io.File;
import java.util.Arrays;

/**
 * Encapsulates knowledge of PngJ 2.1.0
 *
 * @author mwoodman
 */
public class PngjHelper
{
    /**
     * @param tiles       Filenames of PNG files to tile
     * @param destFile    Destination PNG filename
     * @param tileColumns How many tiles per row?
     *                    <p/>
     *                    Original: https://code.google.com/p/pngj/wiki/Snippets
     */
    public static void mergeFiles(final File tiles[], final File destFile, final int tileColumns, final int tileSize)
    {
        final int ntiles = tiles.length;
        final int tileRows = (ntiles + tileColumns - 1) / tileColumns; // integer ceil
        final PngReader[] readers = new PngReader[tileColumns];
        final ImageInfo destImgInfo = new ImageInfo(tileSize * tileColumns, tileSize * tileRows, 8, true); // bitdepth, alpha
        final PngWriter pngw = new PngWriter(destFile, destImgInfo, true);

        pngw.getMetadata().setText("Author", "JourneyMap" + Journeymap.JM_VERSION);
        pngw.getMetadata().setText("Comment", Journeymap.WEBSITE_URL);

        final ImageLineInt destLine = new ImageLineInt(destImgInfo);
        final int lineLen = tileSize * 4; // 4=bytesPixel
        final int gridColor = 135;
        final boolean showGrid = Journeymap.getClient().getFullMapProperties().showGrid.get();

        int destRow = 0;

        for (int ty = 0; ty < tileRows; ty++)
        {
            int nTilesXcur = ty < tileRows - 1 ? tileColumns : ntiles - (tileRows - 1) * tileColumns;
            Arrays.fill(destLine.getScanline(), 0);

            for (int tx = 0; tx < nTilesXcur; tx++)
            { // open several readers
                readers[tx] = new PngReader(tiles[tx + ty * tileColumns]);
                readers[tx].setChunkLoadBehaviour(ChunkLoadBehaviour.LOAD_CHUNK_NEVER);
                //readers[tx].setUnpackedMode(false); TODO: Still need this to be set somehow?
            }

            rowcopy:
            for (int srcRow = 0; srcRow < tileSize; srcRow++, destRow++)
            {
                for (int tx = 0; tx < nTilesXcur; tx++)
                {
                    ImageLineInt srcLine = (ImageLineInt) readers[tx].readRow(srcRow); // read line
                    int[] src = srcLine.getScanline();

                    // Overlay chunk grid
                    if (showGrid)
                    {
                        int skip = (srcRow % 16 == 0) ? 4 : 64;
                        for (int i = 0; i <= src.length - skip; i += skip)
                        {
                            src[i] = (src[i] + src[i] + gridColor) / 3;
                            src[i + 1] = (src[i + 1] + src[i + 1] + gridColor) / 3;
                            src[i + 2] = (src[i + 2] + src[i + 2] + gridColor) / 3;
                            src[i + 3] = 255;
                        }
                    }

                    int[] dest = destLine.getScanline();
                    int destPos = (lineLen * tx);
                    try
                    {
                        System.arraycopy(src, 0, dest, destPos, lineLen);
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        Journeymap.getLogger().error("Bad image data. Src len=" + src.length + ", dest len=" + dest.length + ", destPos=" + destPos);
                        break rowcopy;
                    }
                }
                pngw.writeRow(destLine, destRow); // write to full image
            }

            for (int tx = 0; tx < nTilesXcur; tx++)
            {
                readers[tx].end(); // close readers
            }
        }

        pngw.end(); // close writer
    }
}
