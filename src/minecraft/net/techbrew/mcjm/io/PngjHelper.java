package net.techbrew.mcjm.io;

import java.io.File;
import java.util.Arrays;

import net.techbrew.mcjm.JourneyMap;
import ar.com.hjg.pngj.FileHelper;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.ChunkLoadBehaviour;

/**
 * Encapsulates knowledge of PngJ
 * 
 * @author mwoodman
 *
 */
public class PngjHelper {

	/**
	 * @param tiles  Filenames of PNG files to tile
	 * @param destFile   Destination PNG filename
	 * @param tileColumns How many tiles per row?
	 * 
	 * Original: https://code.google.com/p/pngj/wiki/Snippets
	 */
	public static void mergeFiles(File tiles[], File destFile, int tileColumns, int xWidth) {
	    int ntiles = tiles.length;
	    int tileRows = (ntiles + tileColumns - 1) / tileColumns; // integer ceil
	    
	    // 1:small tile   2:big image
	    ImageInfo imi1, imi2; 
	    PngReader pngr = FileHelper.createPngReader(tiles[0]);
	    imi1 = pngr.imgInfo;
	    PngReader[] readers = new PngReader[tileColumns];
	    imi2 = new ImageInfo(xWidth * tileColumns, imi1.rows * tileRows, imi1.bitDepth, imi1.alpha, imi1.greyscale, imi1.indexed);
	    PngWriter pngw = FileHelper.createPngWriter(destFile, imi2, true);
	    
	    // copy palette and transparency if necessary (more chunks?)
	    pngw.copyChunksFirst(pngr, ChunkCopyBehaviour.COPY_PALETTE | ChunkCopyBehaviour.COPY_TRANSPARENCY);
	    pngr.end(); // close, we'll reopen it again soon
	    
	    ImageLine line2 = new ImageLine(imi2, ImageLine.SampleType.INT, false);
	    int row2 = 0;
	    
	    for( int ty = 0; ty < tileRows; ty++ ) {
	        int nTilesXcur = ty < tileRows - 1 ? tileColumns : ntiles - (tileRows - 1) * tileColumns;
	        Arrays.fill(line2.scanline, 0);
	        
	        for( int tx = 0; tx < nTilesXcur; tx++ ) { // open several readers
	            readers[tx] = FileHelper.createPngReader(tiles[tx + ty * tileColumns]);
	            readers[tx].setChunkLoadBehaviour(ChunkLoadBehaviour.LOAD_CHUNK_NEVER);
	            readers[tx].setUnpackedMode(false);
	            if(!readers[tx].imgInfo.equals(imi1)) 
	                throw new RuntimeException("different tile ? "  + readers[tx].imgInfo);
	        }
	        
	        rowcopy: for(int row1 = 0; row1 < imi1.rows; row1++, row2++ ) {
	            for( int tx = 0; tx < nTilesXcur; tx++ ) {
	                ImageLine line1 = readers[tx].readRowInt(row1); // read line
	                
	                int len = xWidth * imi1.bytesPixel;
	                
	                int[] src = line1.scanline;
                	int srcPos = 0; // xOffset * imi1.bytesPixel;	                	
                	int[] dest = line2.scanline;
                	int destPos = (len*tx);
                	int length = len;
	                try {	                	
	                	System.arraycopy(src, srcPos, dest, destPos, length);
	                } catch(ArrayIndexOutOfBoundsException e) {
	                	JourneyMap.getLogger().severe("Bad numbers: ");
	                	System.out.println(src.length + ", " + srcPos + ", " + dest.length + ", " + destPos + ", " + length);
	                	break rowcopy;
	                }
	            }
	            pngw.writeRow(line2, row2); // write to full image
	        }
	        
	        for( int tx = 0; tx < nTilesXcur; tx++ ) {
	            readers[tx].end(); // close readers
	        }
	    }
	    pngw.end(); // close writer
	}
}
