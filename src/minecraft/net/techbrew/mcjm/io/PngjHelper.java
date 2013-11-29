package net.techbrew.mcjm.io;

import java.io.File;
import java.util.Arrays;

import net.techbrew.mcjm.JourneyMap;
import ar.com.hjg.pngj.FileHelper;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
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
	public static void mergeFiles(final File tiles[], final File destFile, final int tileColumns, final int tileSize) {
	    final int ntiles = tiles.length;
	    final int tileRows = (ntiles + tileColumns - 1) / tileColumns; // integer ceil
	    final PngReader[] readers = new PngReader[tileColumns];
	    final ImageInfo destImgInfo = new ImageInfo(tileSize * tileColumns, tileSize * tileRows, 8, true); // bitdepth, alpha
	    final PngWriter pngw = FileHelper.createPngWriter(destFile, destImgInfo, true);	  
	    pngw.getMetadata().setText("Author", "JourneyMap" + JourneyMap.JM_VERSION);
	    pngw.getMetadata().setText("Comment", JourneyMap.WEBSITE_URL);
	    
	    final ImageLine destLine = new ImageLine(destImgInfo, ImageLine.SampleType.INT, false);
	    final int lineLen = tileSize * 4; // 4=bytesPixel
	    final int gridColor = 135;
	    final boolean showGrid = PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID);
	    
	    int destRow = 0;
	    
	    for( int ty = 0; ty < tileRows; ty++ ) {
	        int nTilesXcur = ty < tileRows - 1 ? tileColumns : ntiles - (tileRows - 1) * tileColumns;
	        Arrays.fill(destLine.scanline, 0);
	        
	        for( int tx = 0; tx < nTilesXcur; tx++ ) { // open several readers
	            readers[tx] = FileHelper.createPngReader(tiles[tx + ty * tileColumns]);
	            readers[tx].setChunkLoadBehaviour(ChunkLoadBehaviour.LOAD_CHUNK_NEVER);
	            readers[tx].setUnpackedMode(false);	            
	        }
	        
	        rowcopy: for(int srcRow = 0; srcRow < tileSize; srcRow++, destRow++ ) {
	            for( int tx = 0; tx < nTilesXcur; tx++ ) {
	                ImageLine srcLine = readers[tx].readRowInt(srcRow); // read line
	                int[] src = srcLine.scanline;	                	
	                
	                // Overlay chunk grid
	                if(showGrid) {
		                int skip = (srcRow%16==0) ? 4 : 64;
		                for(int i=0;i<=src.length-skip;i+=skip) {
		                	src[i]=(src[i]+src[i]+gridColor)/3;
	                		src[i+1]=(src[i+1]+src[i+1]+gridColor)/3;
	                		src[i+2]=(src[i+2]+src[i+2]+gridColor)/3;
	                		src[i+3]=255;
		                }
	                }
	                
                	int[] dest = destLine.scanline;
                	int destPos = (lineLen*tx);
	                try {	                	
	                	System.arraycopy(src, 0, dest, destPos, lineLen);
	                } catch(ArrayIndexOutOfBoundsException e) {
	                	JourneyMap.getLogger().severe("Bad image data. Src len=" +src.length + ", dest len=" + dest.length + ", destPos=" + destPos);
	                	break rowcopy;
	                }
	            }
	            pngw.writeRow(destLine, destRow); // write to full image
	        }
	        
	        for( int tx = 0; tx < nTilesXcur; tx++ ) {
	            readers[tx].end(); // close readers
	        }
	    }
	    
	    pngw.end(); // close writer
	}
}
