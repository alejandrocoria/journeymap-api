package net.techbrew.mcjm.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

import Acme.Utils;

public class FileServlet extends HttpServlet {

	final long expireCache;
	public FileServlet() {
		expireCache = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days
	}
	
	private static final long serialVersionUID = 1L;
	private static boolean debug = true;

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {	
		
		
		try {

			String method = request.getMethod().toLowerCase();
			if(method.equals("head")) { //$NON-NLS-1$
				serveFile(request, response, true);
			} else if(method.equals("get")) { //$NON-NLS-1$
				serveFile(request, response, false);
			} else {
				String error = Constants.getMessageJMERR14(method);
				JourneyMap.announce(error);
				response.addHeader("Allow", "GET,HEAD"); //$NON-NLS-1$ //$NON-NLS-2$
				response.sendError(405, error);
				JourneyMap.getLogger().warning(error);
			}
		} finally {
			
		}
	}
	
	private void serveFile(HttpServletRequest request, HttpServletResponse response, boolean onlyHeaders)
			throws ServletException, IOException {	
		
		String agent = null;
		String path = null;
		try {
			agent = request.getHeader("user-agent"); //$NON-NLS-1$
			path = "web" + request.getRequestURI(); //$NON-NLS-1$
			if("web/".equals(path)) { //$NON-NLS-1$
				path = "web/index.html"; //$NON-NLS-1$
			} 
			InputStream in = null;
			URL pathRes = JourneyMap.class.getResource(path);
			
			if(pathRes==null) throw new IOException("Could not get resource for " + path + " from location " + JourneyMap.class.getResource("web")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String pathURL = pathRes.getPath();
			
			if(pathRes.getProtocol().equals("file")) { //$NON-NLS-1$
				if(pathURL.contains("!/")) { //$NON-NLS-1$
					String[] tokens = pathURL.split("file:/")[1].split("!/"); //$NON-NLS-1$ //$NON-NLS-2$
					ZipFile zipFile = new ZipFile(new File( URLDecoder.decode(tokens[0], "utf-8") )); //$NON-NLS-1$
					ZipEntry zipEntry = zipFile.getEntry(tokens[1]);
					in = zipFile.getInputStream(zipEntry);
				} else {
					in = JourneyMap.class.getResourceAsStream(path);
				}
			} else {
				in = JourneyMap.class.getResourceAsStream(path);
			}
			
			if(in == null) {
				String error = Constants.getMessageJMERR13(pathURL);
				JourneyMap.announce(error);
				response.sendError(404, error);
				JourneyMap.getLogger().warning(error);
			} else {
				response.setStatus(200);
				response.setContentType(getServletContext().getMimeType(path));
				//response.addDateHeader("Last-Modified", file.lastModified());
				//response.addDateHeader("Expires", expireCache);
				//response.setHeader("Content-Length", Long.toString(file.length()));
				//response.addHeader("Cache-Control", "public");				
				if(!onlyHeaders) {
					Utils.copyStream(in, response.getOutputStream(), 0);
				}
			}
			if(in!=null) {
				in.close();
			}
		} catch(Throwable t) {			
			String error = Constants.getMessageJMERR12(path);
			JourneyMap.announce(error);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));	
			JourneyMap.getLogger().info(getRequestHeaders(request));
		} finally {
			response.getOutputStream().flush();
		}
		
	}
	
	private String getRequestHeaders(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer("HTTP Request headers:"); //$NON-NLS-1$
		Enumeration<String> headerNames = request.getHeaderNames();
	    while(headerNames.hasMoreElements()) {
	      String headerName = (String)headerNames.nextElement();
	      String headerValue = request.getHeader(headerName);
	      sb.append("\n\t").append(headerName).append("=").append(headerValue); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    sb.append("\n\tRemote Address:").append(request.getRemoteAddr());
	    sb.append("\n\tRemote Host:").append(request.getRemoteHost());
	    sb.append("\n\tRemote Port:").append(request.getRemotePort());
	    sb.append("\n\tRemote User:").append(request.getRemoteUser());
	    return sb.toString();
	}

}
