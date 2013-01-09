package net.techbrew.mcjm.server;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import se.rupy.http.Event;
import se.rupy.http.Query;
import se.rupy.http.Reply;
import se.rupy.http.Service;

/**
 * Provides common functionality for Rupy service implementations.
 * @author mwoodman
 *
 */
public abstract class BaseService extends Service {
	
	/**
	 * Enum to encapsulate knowledge of the
	 * MIME types for given file extensions.
	 * 
	 * @author mwoodman
	 *
	 */
	enum ContentType {
		
		css("text/css"), //$NON-NLS-1$
		gif("image/gif"), //$NON-NLS-1$
		ico("image/x-icon"), //$NON-NLS-1$
		htm("text/html"), //$NON-NLS-1$
		html("text/html"), //$NON-NLS-1$
		js("application/javascript"), //$NON-NLS-1$
		json("application/json"), //$NON-NLS-1$
		png("image/png"), //$NON-NLS-1$
		jpeg("image/jpeg"), //$NON-NLS-1$
		jpg("image/jpeg"), //$NON-NLS-1$
		txt("text/plain"); //$NON-NLS-1$
		
		private String mime;
		
		private ContentType(String mime) {
			this.mime = mime;
		}
		
		static ContentType fromFileName(String fileName) {
			String name = fileName.toLowerCase(Locale.ENGLISH);
			String ext = name.substring(name.lastIndexOf('.')+1); //$NON-NLS-1$
			try {
				return ContentType.valueOf(ext);
			} catch(Exception e) {
				JourneyMap.getLogger().warning("No ContentType match for file: " + name); //$NON-NLS-1$
				return null;
			}
		}
		
		String getMime() {
			return mime;
		}
	}
	
	/**
	 * Log and throw a Rupy Event exception.
	 * @param code
	 * @param message
	 * @param event
	 */
	protected void throwEventException(int code, String message, Event event, boolean isError) throws Event {
		
		// Log the issue depending on severity
		String out = code + " " + message; //$NON-NLS-1$
		if(isError) {
			JourneyMap.announce(message);
			JourneyMap.getLogger().warning(out);
		} else {
			JourneyMap.getLogger().info(out);
		}
		
		// Set the error code on the response
		try {
			event.reply().code(out);
		} catch (IOException e) {
			JourneyMap.getLogger().warning("Can't set response code: " + out); //$NON-NLS-1$
		}
		throw event;
	}
	
	/**
	 * Get request headers and remote address for a request Event.
	 * @param event
	 * @return
	 */
	protected String debugRequestHeaders(Event event) {
		StringBuffer sb = new StringBuffer("HTTP Request headers:"); //$NON-NLS-1$		
		HashMap headers = event.query().header();
	    for(Object name : headers.keySet()) {
	      Object value = headers.get(name);
	      sb.append("\n\t").append(name).append("=").append(value); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    sb.append("\n\tRemote Address:").append(event.remote());
	    return sb.toString();
	}
	
	/**
	 * Log a bad request coming from the browser.
	 * @param event
	 * @throws Event
	 */
	protected void reportMalformedRequest(Event event) throws Event {
		String error = Constants.getMessageJMERR05("queryString=" + event.query().path()); //$NON-NLS-1$
		throwEventException(400, Constants.getMessageJMERR02(), event, false);
	}

	/**
	 * Get a request parameter String value or return the default provided.
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected String getParameter(Query map, String key, String defaultValue) {
		Object val = map.get(key);
		return (val!=null) ? val.toString() : defaultValue;
	}

	/**
	 * Get a request parameter int value or return the default provided.
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected int getParameter(Map<String, String[]> map, String key, int defaultValue) {
		Object val = map.get(key);
		Integer intVal = null;
		if(val!=null) {
			try {
				intVal = Integer.parseInt((String) val);
			} catch(NumberFormatException e) {
				JourneyMap.getLogger().warning("Didn't get numeric query parameter for '" + key + "': " + val);
			}
		}
		return (intVal!=null) ? intVal : defaultValue;
	}
	
	/**
	 * Encapsulate knowledge about setting HTTP headers
	 * on the Event response. Builder pattern allows for
	 * convenient method chaining.
	 * 
	 * @author mwoodman
	 *
	 */
	static class ResponseHeader {
		
		/**
	     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
	     */
	    protected static SimpleDateFormat dateFormat;
	    static {
	    	dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Constants.getLocale()); //$NON-NLS-1$
	    	dateFormat.setTimeZone(Constants.GMT);
	    }
		
		static ResponseHeader on(Event event) {
			return new ResponseHeader(event);
		}
		
		private Reply reply;
		private ResponseHeader(Event event) {
			this.reply = event.reply();
		}
		
		ResponseHeader setHeader(String name, String value) {
			if(reply==null) {
				throw new IllegalStateException("ResponseHeader builder already cleared."); //$NON-NLS-1$
			}			
			reply.header(name, value); 
			return this;
		}
		
		/**
		 * Set headers to prevent browser caching of the response.
		 * @return
		 */
		ResponseHeader noCache() {
			setHeader("Cache-Control","no-cache"); //HTTP 1.1 //$NON-NLS-1$ //$NON-NLS-2$
			setHeader("Pragma","no-cache"); //HTTP 1.0 //$NON-NLS-1$ //$NON-NLS-2$
			setHeader("Expires", "0"); //prevents caching by a proxy server  //$NON-NLS-1$ //$NON-NLS-2$
			return this;
		}
		
		/**
		 * Set content headers for the file to be returned.
		 * @param file
		 * @return
		 */
		ResponseHeader content(File file) {
			contentType(ContentType.fromFileName(file.getName()));	
			contentLength(file.length());
			return contentModified(file.lastModified());	
		}
		
		/**
		 * Set content headers for the ZipEntry-based file to be returned.
		 * @param file
		 * @return
		 */
		ResponseHeader content(ZipEntry zipEntry) {
			contentType(ContentType.fromFileName(zipEntry.getName()));
			long size = zipEntry.getSize();
			long time = zipEntry.getTime();
			if(size>-1) contentLength(size);
			if(time>-1) contentModified(time);
			return this;
		}
		
		/**
		 * Set content length for the file to be returned.
		 * @param input
		 * @return
		 */
		ResponseHeader contentLength(FileInputStream input) {
			try {
				contentLength(input.getChannel().size()); //$NON-NLS-1$
			} catch(IOException e) {
				JourneyMap.getLogger().warning("Couldn't get content length for FileInputStream"); //$NON-NLS-1$
			}
			return this;
		}
		
		/**
		 * Set content last=modified timestamp.
		 * @param file
		 * @return
		 */
		ResponseHeader contentModified(long timestamp) {
			return setHeader("Last-Modified", dateFormat.format(new Date(timestamp))); //$NON-NLS-1$
		}
		
		/**
		 * Set content length for the file to be returned.
		 * @param file
		 * @return
		 */
		ResponseHeader contentLength(long fileSize) {
			return setHeader("Content-Length", Long.toString(fileSize)); //$NON-NLS-1$
		}
		
		/**
		 * Set MIME content type for the file to be returned.
		 * @param file
		 * @return
		 */
		ResponseHeader contentType(ContentType type) {
			if(type!=null) {
				setHeader("Content-Type", type.getMime()); //$NON-NLS-1$
			} 
			return this;
		}
		
		/**
		 * Set the inline content filename of the data being returned.
		 * @param name
		 * @return
		 */
		ResponseHeader inlineFilename(String name) {
			return setHeader("Content-Disposition", "inline; filename=\"" + name + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		/**
		 * Clear the object reference to the reply. 
		 * This really shouldn't be necessary if GC is doing its job.
		 */
		void done() {
			this.reply = null;
		}
		
	}

}
