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

import se.rupy.http.Event;
import se.rupy.http.Service;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.server.BaseService.ContentType;
import net.techbrew.mcjm.server.BaseService.ResponseHeader;

/**
 * Provides L10N string values for the user's locale in JSON format.
 * 
 * @author mwoodman
 *
 */
public class MessagesServlet extends BaseService {
	
	private static final long serialVersionUID = 1L;
	private static boolean debug = true;

	private static final String KEY_PREFIX = "WebMap."; //$NON-NLS-1$
	private final byte[] jsonBytes;
	final long expireCache;
	
	public MessagesServlet() {
		expireCache = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days
		jsonBytes = buildJson();
	}
	
	@Override
	public String path() {
		return "/msg"; //$NON-NLS-1$
	}
	
	@Override
	public void filter(Event event) throws Event, Exception {
		try {	
			ResponseHeader.on(event).contentType(ContentType.js).contentLength(jsonBytes.length);
			event.output().write(jsonBytes); 
		} catch(Throwable t) {			
			String error = Constants.getMessageJMERR12(t.getMessage());
			throwEventException(500, error, event, true);
		} 
	}
	
	/**
	 * Builds a JSON string of the L10N messages for the user locale.
	 * @return
	 */
	private byte[] buildJson() {
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
		return sb.toString().getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$
	}
	

}