package net.techbrew.mcjm.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import se.rupy.http.Event;
import se.rupy.http.Service;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.server.BaseService.ResponseHeader;


/**
 * Serve files from disk.  Works for zip-archived files
 * when the mod is in normal use, also works for standard 
 * file-system access when the mod is unzipped or when
 * running from Eclipse during development.
 * 
 * @author mwoodman
 *
 */
public class FileServlet extends BaseService {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor
	 */
	public FileServlet() {
	}
	
	@Override
	public String path() {
		return null; // Default handler
	}
	
	@Override
	public void filter(Event event) throws Event, Exception {
		
		String path = null;
		InputStream in = null;
		
		try {
			
			// Determine request path
			path = "web" + event.query().path(); //$NON-NLS-1$
			if("web/".equals(path)) { //$NON-NLS-1$
				path = "web/index.html"; //$NON-NLS-1$
			} 
			
			// Determine file system context for running application
			URL pathRes = JourneyMap.class.getResource(path);			
			if(pathRes==null) {
				throwEventException(500, "Could not get resource for " + path + " from location " + JourneyMap.class.getResource("web"), event, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$				
			}
			String pathURL = pathRes.getPath();
			boolean useZipEntry = pathRes.getProtocol().equals("file") && pathURL.contains("!/"); //$NON-NLS-1$	//$NON-NLS-2$
			
			if(useZipEntry) { 				
				// Running out of a Zip archive or jar
				String[] tokens = pathURL.split("file:/")[1].split("!/"); //$NON-NLS-1$ //$NON-NLS-2$
				ZipFile zipFile = new ZipFile(new File( URLDecoder.decode(tokens[0], "utf-8") )); //$NON-NLS-1$
				ZipEntry zipEntry = zipFile.getEntry(tokens[1]);
				
				// Set inputstream
				in = zipFile.getInputStream(zipEntry);
				
				if(in!=null) {					
					// Set content headers
					ResponseHeader.on(event).content(zipEntry);
					serveStream(in, event);
				}
				
			} else {
				// Running out of a directory
				in = JourneyMap.class.getResourceAsStream(path);
				
				if(in!=null) {					
					File file = new File(pathRes.toURI()); 
					serveFile(file, event);
				}
			}
			
			// Return 404 if not found
			if(in == null) {
				throwEventException(404, Constants.getMessageJMERR13(pathURL), event, false);
			}

		} catch(Event eventEx) {
			throw eventEx;
		} catch(Throwable t) {			
			JourneyMap.getLogger().info(debugRequestHeaders(event));
			throwEventException(500, Constants.getMessageJMERR12(path), event, true);
		} 
	}
	
	/**
	 * Respond with the contents of a file.
	 * 
	 * @param sourceFile
	 * @param event
	 * @throws Event
	 * @throws IOException
	 */
	public static void serveFile(File sourceFile, Event event) throws Event, IOException {
		
		// Set content headers
		ResponseHeader.on(event).content(sourceFile);
		
		// Stream file
		serveStream(new FileInputStream(sourceFile), event);
	}
	
	/**
	 * Respond with the contents of a file input stream.
	 * 
	 * @param sourceFile
	 * @param event
	 * @throws Event
	 * @throws IOException
	 */
	public static void serveStream(final InputStream input, Event event) throws Event, IOException {
		
		// Transfer inputstream to event outputstream
		ReadableByteChannel inputChannel = null;
		WritableByteChannel outputChannel = null;
		try {
			inputChannel = Channels.newChannel(input);
			outputChannel = Channels.newChannel(event.output());			
			ByteBuffer buffer = ByteBuffer.allocate(65536); 
			while (inputChannel.read(buffer) != -1) { 
				buffer.flip( ); 
				outputChannel.write(buffer); 
				buffer.clear( ); 
			}		
		} finally {
			if (input != null) {
				input.close();
			}
			event.output().flush();
		}
	}



}
