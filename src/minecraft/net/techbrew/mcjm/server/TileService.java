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
			
//			// Check for ping first
//			Long since = getParameter(query, "since", (Long) null);
//			if(since!=null) {
//				serveChangedBounds(event, zoom, since);
//				return;
//			}

			// Region coords
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
	
//	private void serveChangedBounds(Event event, int zoom, long time) throws Event, Exception {
//		
//		ImagesData data = new ImagesData(zoom, time);
//		DataCache.instance().getJson(data);
//		
//		List<RegionCoord> regions = RegionImageCache.getInstance().getDirtySince(time);
//		List<Double[]> bounds = new ArrayList<Double[]>(regions.size());		
//		for(RegionCoord rc : regions) {
//			bounds.add(toZoomedBounds(zoom, rc.regionX, rc.regionZ));
//		}
//		
//		ResponseHeader.on(event).contentType(ContentType.json).noCache();
//		try {
//			
//			// Build the response string
//			StringBuffer jsonData = new StringBuffer();
//					
//			// Check for callback to determine Json or JsonP
//			boolean useJsonP = event.query().containsKey(CALLBACK_PARAM);
//			if(useJsonP) {
//				jsonData.append(URLEncoder.encode(event.query().get(CALLBACK_PARAM).toString(), UTF8.name()));
//				jsonData.append("("); //$NON-NLS-1$	
//			} else {
//				jsonData.append("data="); //$NON-NLS-1$	
//			}	
//			
//			// JSON data
//			HashMap<String, Object> map = new HashMap<String, Object>(5);
//			map.put("zoom", zoom);
//			map.put("since", time);
//			map.put("bounds", bounds);
//			jsonData.append(JsonHelper.toJson(map));
//			
//			// Finish function call for JsonP if needed
//			if(useJsonP) {
//				jsonData.append(")"); //$NON-NLS-1$
//			}
//			
//			// Optimize headers for JSONP
//			ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
//					
//			// Gzip response
//			gzipResponse(event, jsonData.toString());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			throwEventException(400, e.getMessage(), event, true);
//		}
//	}
//	
//	private Double[] toZoomedBounds(final int zoom, final int rX, final int rZ) {
//		double scale = Math.pow(2, zoom);
//		double sX = rX/scale;
//		double sZ = rZ/scale;
//		double sX2 = (rX+1)/scale;
//		double sZ2 = (rZ+1)/scale;
//		return new Double[]{sX,sZ,sX2,sZ2};
//	}
	
}
