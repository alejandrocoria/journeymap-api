package net.techbrew.mcjm.server;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.Constants.WorldType;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.MapSaver;
import net.techbrew.mcjm.io.PlayerDataFileHandler;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.ChunkRenderer;
import net.techbrew.mcjm.ui.ZoomLevel;
import Acme.Utils;

// Referenced classes of package Acme.Serve:
//            ThrottleItem, ThrottledOutputStream

public class ChunkServlet extends HttpServlet {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String JSON_CONTENT_TYPE = "application/json"; //$NON-NLS-1$

	/**
	 * Serves chunk data and player info.
	 */
	public ChunkServlet() {
		super();
	}

	@Override
	public String getServletInfo() {
		return "Serves chunk data for JourneyMap"; //$NON-NLS-1$
	}

	/**
	 * Handle all HTTP request method types.
	 * Only GET and HEAD are used.
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {		
		
		try {
			String method = request.getMethod().toLowerCase();
			if(method.equals("head")) { //$NON-NLS-1$
				serveData(request, response, true);
			} else if(method.equals("get")) { //$NON-NLS-1$
				serveData(request, response, false);
			} else {
				String error = Constants.getMessageJMERR14(method); //$NON-NLS-1$
				JourneyMap.announce(error);
				JourneyMap.getLogger().warning(error);
				response.addHeader("Allow", "GET,HEAD"); //$NON-NLS-1$ //$NON-NLS-2$
				response.sendError(405, error);
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe(LogFormatter.toString(t));
		}
	}

	/**
	 * Serves chunk data and/or player data.
	 * 
	 * @param request
	 * @param response
	 * @param onlyHeaders
	 * @throws IOException
	 */
	private void serveData(HttpServletRequest request,
			HttpServletResponse response, boolean onlyHeaders)
			throws IOException {

		response.setCharacterEncoding(CHARACTER_ENCODING);
		Map<String, String[]> pMap = request.getParameterMap();

		Minecraft minecraft = Minecraft.getMinecraft();
		World theWorld = minecraft.theWorld;
		if (theWorld == null) {
			response.sendError(503, Constants.getMessageJMERR09());
			JourneyMap.getLogger().finer(Constants.getMessageJMERR09());
			return;
		}
		
		// Ensure world is loaded
		if(!JourneyMap.isRunning()) {
			response.sendError(503, Constants.getMessageJMERR02());
			JourneyMap.getLogger().finer(Constants.getMessageJMERR02());
			return;
		}
		
		
		File worldDir = FileHandler.getWorldDir(minecraft);

		// Check world requested
		String worldName = getParameter(pMap, "worldName", null); //$NON-NLS-1$
		if (worldName == null) {
			servePlayerData(request, response, worldDir, onlyHeaders);
			if(JourneyMap.getLogger().isLoggable(Level.FINER)) {
				JourneyMap.getLogger().finer("Request: " + request.getRequestURL()); //$NON-NLS-1$
			}
			return;
		}
		worldName = URLDecoder.decode(worldName, "UTF-8"); //$NON-NLS-1$
		
		// Check world type
		String worldTypeString = getParameter(pMap, "worldType", Constants.WorldType.sp.name()); //$NON-NLS-1$
		Constants.WorldType worldType;
		try {
			worldType = Constants.WorldType.valueOf(worldTypeString);
		} catch (Exception e) {			
			String error = Constants.getMessageJMERR05("worldType=" + worldTypeString); //$NON-NLS-1$
			JourneyMap.announce(error);
			response.sendError(400, error);
			JourneyMap.getLogger().warning(error);
			return;
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(pMap, "depth", 4); //$NON-NLS-1$
		
		if(!worldName.equals(URLDecoder.decode(FileHandler.getSafeName(ModLoader.getMinecraftInstance())))) {
			String error = Constants.getMessageJMERR10("worldType=" + worldType); //$NON-NLS-1$
			response.sendError(503, error);
			JourneyMap.announce(error);
			JourneyMap.getLogger().warning(error);
			return;

		}  else if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR06("worldDir=" + worldDir.getAbsolutePath()); //$NON-NLS-1$
			response.sendError(400, error);
			JourneyMap.announce(error);
			JourneyMap.getLogger().warning(error);
			return;
		}
		
		// Read coordinate pairs
		try {
			int x1 = getParameter(pMap, "x1", 0); //$NON-NLS-1$
			int z1 = getParameter(pMap, "z1", 0); //$NON-NLS-1$
			int x2 = getParameter(pMap, "x2", x1); //$NON-NLS-1$
			int z2 = getParameter(pMap, "z2", z1); //$NON-NLS-1$
			int width = getParameter(pMap, "width", 100); //$NON-NLS-1$
			int height = getParameter(pMap, "height", 100); //$NON-NLS-1$
			int worldProviderType = getParameter(pMap, "worldProviderType", 0);  //$NON-NLS-1$
			String mapTypeString = getParameter(pMap, "mapType", Constants.MapType.day.name()); //$NON-NLS-1$
			Constants.MapType mapType = null;
			try {
				mapType = Constants.MapType.valueOf(mapTypeString);
			} catch (Exception e) {
				String error = Constants.getMessageJMERR05("mapType=" + mapType); //$NON-NLS-1$
				JourneyMap.announce(error);
				response.sendError(400, error);
				JourneyMap.getLogger().warning(error);
				return;
			}
			
			if (x1 >= x2 || z1 >= z2) {
				String error = Constants.getMessageJMERR05("coordinates=" + x1 + "," + z1 + "," + x2 + "," + z2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				JourneyMap.announce(error);
				response.sendError(400, error);
				JourneyMap.getLogger().warning(error);
				return;
			} else {
				response.setHeader("Cache-Control","no-cache"); //HTTP 1.1 //$NON-NLS-1$ //$NON-NLS-2$
				response.setHeader("Pragma","no-cache"); //HTTP 1.0 //$NON-NLS-1$ //$NON-NLS-2$
				response.setDateHeader ("Expires", 0); //prevents caching at the proxy server				 //$NON-NLS-1$
				response.setContentType("image/png"); //$NON-NLS-1$
				mergeImageChunks(request, response, worldDir, x1, z1, x2, z2, mapType, depth, worldProviderType, width, height);
			}
		} catch (NumberFormatException e) {
			reportMalformedRequest(request, response);
		} finally {

		}

		

	}

	private void reportMalformedRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String error = Constants.getMessageJMERR05("queryString=" + request.getQueryString()); //$NON-NLS-1$
		JourneyMap.announce(error);
		JourneyMap.getLogger().severe(error);
		response.sendError(400, error);
	}

	private String getParameter(Map<String, String[]> map, String key,
			String defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key)[0];
		} else {
			return defaultValue;
		}
	}

	private int getParameter(Map<String, String[]> map, String key,
			int defaultValue) {
		if (map.containsKey(key)) {
			return Integer.parseInt(map.get(key)[0]);
		} else {
			return defaultValue;
		}
	}

	private void servePlayerData(HttpServletRequest request,
			HttpServletResponse response, File worldDir, boolean onlyHeaders) throws IOException {

		File playerFile = PlayerDataFileHandler.getPlayerFile(ModLoader.getMinecraftInstance());
		if (playerFile!=null && playerFile.exists() && playerFile.canRead()) {
			response.setStatus(200);
			response.setContentType(JSON_CONTENT_TYPE);
			response.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
			response.addDateHeader("Last-Modified", playerFile.lastModified()); //$NON-NLS-1$
			response.setHeader("Content-Length", Long.toString(playerFile.length())); //$NON-NLS-1$
			if(!onlyHeaders) {
				FileInputStream fs = new FileInputStream(playerFile);
				Utils.copyStream(fs, response.getOutputStream(), 0);
				fs.close();
			}
		} else {
			String error = Constants.getMessageJMERR11(playerFile);
			//JourneyMap.announce(error);
			JourneyMap.getLogger().log(Level.WARNING, error);
			response.sendError(400, error);
			return;
		}

		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	/**
	 * Used by ChunkServlet to put chunk images together into what the browser needs.
	 * @param request
	 * @param response
	 * @param worldDir
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param mapType
	 * @param depth
	 * @throws IOException
	 */
	private static synchronized void mergeImageChunks(HttpServletRequest request,
			HttpServletResponse response, File worldDir, int x1, int z1,
			int x2, int z2, Constants.MapType mapType, int depth, int worldProviderType, int canvasWidth, int canvasHeight)
			throws IOException {
		
		long start = 0, stop = 0;
		final Constants.CoordType cType = Constants.CoordType.convert(mapType, worldProviderType);
		
		if(JourneyMap.getLogger().isLoggable(Level.FINEST)) {
			start = System.currentTimeMillis();
		}
		
		// Headers
		response.setStatus(200);
		response.setContentType("image/png"); //$NON-NLS-1$
		response.setHeader("Content-Disposition", "inline; filename=\"jm.png\"");	 //$NON-NLS-1$ //$NON-NLS-2$

		OutputStream out = response.getOutputStream();
		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
		BufferedImage mergedImg = RegionFileHandler.getMergedChunks(worldDir, x1, z1, x2, z2, mapType, depth, cType, false, 
				new ZoomLevel(1F, 1, false, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
		
//		if(mergedImg.getWidth()!=canvasWidth) {
//			Image scaled = mergedImg.getScaledInstance(canvasWidth, -1, Image.SCALE_SMOOTH);					
//			BufferedImage temp = new BufferedImage(mergedImg.getWidth(null), mergedImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//			Graphics2D g = temp.createGraphics();
//			g.drawImage(scaled, 0, 0, null);
//			mergedImg = temp;
//		} 
		
		ImageIO.write(mergedImg, "png", ios); //$NON-NLS-1$
		
		ios.flush();
		ios.close();
		
		if(JourneyMap.getLogger().isLoggable(Level.FINEST)) {
			stop = System.currentTimeMillis();
			JourneyMap.getLogger().finest("mergeImageChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
