package net.techbrew.mcjm.server;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionImageHandler;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;

/**
 * Provides a map image by combining region files.
 * @author mwoodman
 *
 */
public class TileService extends FileService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CALLBACK_PARAM = "callback";  //$NON-NLS-1$	
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
		
		try {
			int zoom = getParameter(query, "zoom", 0); //$NON-NLS-1$

			// Region coords
			final int x = getParameter(query, "x", 0); //$NON-NLS-1$
			Integer vSlice = getParameter(query, "depth", (Integer) null); //$NON-NLS-1$
			final int z = getParameter(query, "z", 0); //$NON-NLS-1$			
			final int dimension = getParameter(query, "dim", 0);  //$NON-NLS-1$
			
			// Map type
			final String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
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
			
			if(mapType==MapType.underground && WorldData.isHardcoreAndMultiplayer()) {
				ResponseHeader.on(event).contentType(ContentType.png).noCache();
				serveFile(RegionImageHandler.getBlank512x512ImageFile(), event);
			} else {
			
				// Determine chunks for coordinates at zoom level
				final int scale = (int) Math.pow(2, zoom);
				final int distance = 32/scale;
				final int minChunkX = x * distance;
				final int minChunkZ = z * distance;
				final int maxChunkX = minChunkX + distance - 1;
				final int maxChunkZ = minChunkZ + distance - 1;
				
				//System.out.println("zoom " + zoom + ", scale=" + scale + ", distance=" + distance + ": " + minChunkX + "," + minChunkZ + " - " + maxChunkX + "," + maxChunkZ);			
				
				final ChunkCoordIntPair startCoord = new ChunkCoordIntPair(minChunkX,minChunkZ);
				final ChunkCoordIntPair endCoord = new ChunkCoordIntPair(maxChunkX,maxChunkZ);
				
				final BufferedImage img = RegionImageHandler.getMergedChunks(worldDir, startCoord, endCoord, mapType, vSlice, dimension, true, 512, 512, false);
	
				ResponseHeader.on(event).contentType(ContentType.png).noCache();
				serveImage(event, img);
			}
						
			final long stop=System.currentTimeMillis();
			if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine((stop-start) + "ms to serve tile");
			}
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} 

	}
	
}
