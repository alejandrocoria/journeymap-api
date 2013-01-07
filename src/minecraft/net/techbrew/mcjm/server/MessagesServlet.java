package net.techbrew.mcjm.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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

public class MessagesServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static boolean debug = true;

	private static final String KEY_PREFIX = "WebMap."; //$NON-NLS-1$
	private String json;
	private byte[] jsonBytes;
	final long expireCache;
	
	public MessagesServlet() {
		expireCache = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days
		buildJson();
	}
	
	private void buildJson() {
		Set<String> allKeys = Constants.getBundleKeys();
		StringBuffer sb = new StringBuffer("var JML10N = {\n"); //$NON-NLS-1$
		for(String key : allKeys) {
			if(key.startsWith(KEY_PREFIX)) {
				String name = key.split(KEY_PREFIX)[1];
				String value = Constants.getString(key);
				sb.append(name).append(" : \"").append(value).append("\",\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		sb.append("time : \"").append(new Date()).append("\" };"); //$NON-NLS-1$ //$NON-NLS-2$
		json = sb.toString();
		jsonBytes = json.getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {	
		try {	
			response.setStatus(200);
			response.setContentType("text/javascript"); //$NON-NLS-1$
			OutputStream out = response.getOutputStream();
			out.write(jsonBytes); 
		} catch(Throwable t) {			
			String error = Constants.getMessageJMERR12(t.getMessage());
			JourneyMap.announce(error);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));			
			response.sendError(500, error);
		} finally {
			response.getOutputStream().flush();
		}
	}
}