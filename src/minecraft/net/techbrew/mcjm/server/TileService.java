package net.techbrew.mcjm.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.model.RegionImageSet;
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
			int rx = getParameter(query, "rx", 0); //$NON-NLS-1$
			Integer ry = getParameter(query, "ry", (Integer) null); //$NON-NLS-1$
			int rz = getParameter(query, "rz", 0); //$NON-NLS-1$			
			int dimension = getParameter(query, "dimension", 0);  //$NON-NLS-1$
			
			// Map type
			String mapTypeString = getParameter(query, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				throwEventException(400, error, event, true);
			}

			RegionCoord rCoord = new RegionCoord(worldDir,rx,ry,rz,dimension);	
			RegionImageCache cache = RegionImageCache.getInstance();
			if(cache.contains(rCoord)) {
				serveImage(event, cache.getGuaranteedImage(rCoord, mapType));
			} else {
				RegionImageSet ris = new RegionImageSet(rCoord);
				serveImage(event, ris.getImage(mapType));
			}
						
			long stop=System.currentTimeMillis();
			if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine((stop-start) + "ms to serve map.png");
			}
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(event);
		} 

	}
	
	private void serveImage(Event event, BufferedImage img) throws Exception {
		if(img!=null) {
			gzipResponse(event, blankImage);
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
		gzipResponse(event, blankImage);
	}
	
}
