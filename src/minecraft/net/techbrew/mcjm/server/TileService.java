package net.techbrew.mcjm.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.ui.ZoomLevel;
import se.rupy.http.Event;
import se.rupy.http.Query;

/**
 * Provides a map image by combining region files.
 * @author mwoodman
 *
 */
public class TileService extends FileService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$	
	
	private byte[] blankImage;

	/**
	 * Serves chunk data and player info.
	 */
	public TileService() {
		super();
	}
	
	@Override
	public String path() {
		return "/tile"; //$NON-NLS-1$
	}
		
	@Override
	public void filter(Event event) throws Event, Exception {
		
		long start=System.currentTimeMillis();

		// Parse query for parameters
		Query query = event.query();
		query.parse();
		
		Minecraft minecraft = Minecraft.getMinecraft();
		World theWorld = minecraft.theWorld;
		if (theWorld == null) {
			throwEventException(503, Constants.getMessageJMERR09(), event, false);
		}
		
		// Ensure world is loaded
		if(!JourneyMap.getInstance().isMapping()) {
			throwEventException(503, Constants.getMessageJMERR02(), event, false);
		}
		
		// Ensure world dir is found
		File worldDir = FileHandler.getJMWorldDir(minecraft);
		if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR06("worldDir=" + worldDir.getAbsolutePath()); //$NON-NLS-1$
			throwEventException(400, error, event, true);
		}
		
		// Region coords
		try {
			int zoom = getParameter(query, "zoom", 0); //$NON-NLS-1$
			int x = getParameter(query, "x", 0); //$NON-NLS-1$
			Integer vSlice = getParameter(query, "depth", (Integer) null); //$NON-NLS-1$
			int z = getParameter(query, "z", 0); //$NON-NLS-1$			
			int dimension = getParameter(query, "dim", 0);  //$NON-NLS-1$
			
			// Map type
			String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				throwEventException(400, error, event, true);
			}
			if(mapType!=MapType.underground) {
				vSlice = null;
			}
			
			// Determine chunks for coordinates at zoom level
			int scale = (int) Math.pow(2, zoom);
			int distance = 32/scale;
			int minChunkX = x * distance;
			int minChunkZ = z * distance;
			int maxChunkX = minChunkX + distance - 1;
			int maxChunkZ = minChunkZ + distance - 1;
			
			//System.out.println("zoom " + zoom + ", scale=" + scale + ", distance=" + distance + ": " + minChunkX + "," + minChunkZ + " - " + maxChunkX + "," + maxChunkZ);			
			
			BufferedImage img = RegionImageHandler.getMergedChunks(worldDir, minChunkX, minChunkZ, maxChunkX, maxChunkZ, mapType, vSlice, dimension, true, ZoomLevel.getDefault(), 512, 512);

			serveImage(event, img);
						
			long stop=System.currentTimeMillis();
			if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine((stop-start) + "ms to serve tile");
			}
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} 

	}
	
	private void serveImage(Event event, BufferedImage img) throws Exception {
		if(img!=null) {
			ResponseHeader.on(event).contentType(ContentType.png).noCache();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			baos.flush();
			byte[] bytes = baos.toByteArray();
			baos.close();
			event.output().write(bytes); 
			//gzipResponse(event, bytes);
			return;
		}
		if(blankImage==null) {
			img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			ResponseHeader.on(event).contentType(ContentType.png).noCache();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			baos.flush();
			blankImage = baos.toByteArray();
			baos.close();
		}
		event.output().write(blankImage); 
	}
	
}
