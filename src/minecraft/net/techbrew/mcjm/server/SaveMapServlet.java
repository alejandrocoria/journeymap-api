package net.techbrew.mcjm.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
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
import net.techbrew.mcjm.render.ChunkRenderer;
import Acme.Utils;

// Referenced classes of package Acme.Serve:
//            ThrottleItem, ThrottledOutputStream

public class SaveMapServlet extends HttpServlet {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String PNG_CONTENT_TYPE = "image/png"; //$NON-NLS-1$
	private static boolean debug = true;

	/**
	 * Serves chunk data and player info.
	 */
	public SaveMapServlet() {
		super();
	}

	@Override
	public String getServletInfo() {
		return "Serves map image for JourneyMap"; //$NON-NLS-1$
	}

	/**
	 * Handle all HTTP request method types.
	 * Only GET and HEAD are used.
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {		
		String method = request.getMethod().toLowerCase();
		if(method.equals("head")) { //$NON-NLS-1$
			serveData(request, response, true);
		} else if(method.equals("get")) { //$NON-NLS-1$
			serveData(request, response, false);
		} else {
			String error = Constants.getMessageJMERR02(method);
			JourneyMap.announce(error);
			response.addHeader("Allow", "GET,HEAD"); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendError(405, error);
			JourneyMap.getLogger().warning(error);
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

		// Check world
		Minecraft minecraft = ModLoader.getMinecraftInstance();
		World theWorld = minecraft.theWorld; 
		if (theWorld == null) {
			response.sendError(503, Constants.getMessageJMERR09());
			JourneyMap.getLogger().warning(Constants.getMessageJMERR09());
			return;
		}
		
		// Ensure world is loaded
		if(!JourneyMap.isRunning()) {
			response.sendError(503, Constants.getMessageJMERR02());
			JourneyMap.getLogger().warning(Constants.getMessageJMERR02());
			return;
		}
		
		File worldDir = FileHandler.getWorldDir(minecraft);
		
		// Check for hardcore
		Boolean hardcore = !minecraft.isSingleplayer() && theWorld.getWorldInfo().isHardcoreModeEnabled();

		// Check world requested
		String worldName = getParameter(pMap, "worldName", null); //$NON-NLS-1$
		worldName = URLDecoder.decode(worldName, "UTF-8"); //$NON-NLS-1$
		
		// Check world type
		String worldTypeString = getParameter(pMap, "worldType", Constants.WorldType.sp.name()); //$NON-NLS-1$
		Constants.WorldType worldType;
		try {
			worldType = Constants.WorldType.valueOf(worldTypeString);
		} catch (Exception e) {
			String error = Constants.getMessageJMERR05("worldType=" + worldTypeString); //$NON-NLS-1$
			JourneyMap.announce(error);
			JourneyMap.getLogger().warning(error);
			response.sendError(400, error);
			return;
		}
		
		// Check depth (for underground maps)
		int depth = getParameter(pMap, "depth", 4); //$NON-NLS-1$
		
		if(!worldName.equals(URLDecoder.decode(FileHandler.getSafeName(ModLoader.getMinecraftInstance())))) {
			String error = Constants.getMessageJMERR10("worldType=" + worldTypeString); //$NON-NLS-1$
			response.sendError(503, error);
			JourneyMap.getLogger().warning(error);
			JourneyMap.announce(error);
			return;

		}  else if (!worldDir.exists() || !worldDir.isDirectory()) {
			String error = Constants.getMessageJMERR10(worldDir.getAbsolutePath());
			JourneyMap.announce(error);
			response.sendError(400, error);
			JourneyMap.getLogger().warning(error);
			return;
		}
		
		try {
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
			
			// Validate cave mapping allowed
			if(mapType.equals(Constants.MapType.underground) && hardcore) {
				throw new IllegalArgumentException("Cave mapping on hardcore servers is not allowed"); //$NON-NLS-1$
			}
			
			final Constants.CoordType cType = Constants.CoordType.convert(mapType, worldProviderType);
			
			response.setHeader("Cache-Control","no-cache"); //HTTP 1.1 //$NON-NLS-1$ //$NON-NLS-2$
			response.setHeader("Pragma","no-cache"); //HTTP 1.0 //$NON-NLS-1$ //$NON-NLS-2$
			response.setDateHeader ("Expires", 0); //prevents caching at the proxy server				 //$NON-NLS-1$
			response.setContentType("image/png"); //$NON-NLS-1$
			OutputStream out = response.getOutputStream();
			BufferedImage mapImg = MapSaver.saveMap(worldDir, mapType, depth, cType);
			ImageIO.write(mapImg, "png", out); //$NON-NLS-1$
			out.flush();
			out.close();
			
		} catch (NumberFormatException e) {
			reportMalformedRequest(request, response);
		} finally {

		}
	}

	private void reportMalformedRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String error = Constants.getMessageJMERR05("query=" + request.getQueryString()); //$NON-NLS-1$
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
}
