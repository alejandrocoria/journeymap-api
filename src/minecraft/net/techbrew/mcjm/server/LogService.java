package net.techbrew.mcjm.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
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
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import se.rupy.http.Event;
import se.rupy.http.Service;

import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.JMLogger;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.server.BaseService.ResponseHeader;


/**
 * Serve the journeyMap.log file from disk
 * 
 * @author mwoodman
 *
 */
public class LogService extends FileService {
	
	private static final long serialVersionUID = 1L;
	
	private static final String CLASSPATH_ROOT = "/";
	private static final String CLASSPATH_WEBROOT = "web";
	private static final String IDE_TEST = "eclipse/Client/bin/";	
	private static final String IDE_SOURCEPATH = "../../../src/minecraft/net/techbrew/mcjm/web";
	
	private File logFile;
	
	/**
	 * Default constructor
	 */
	public LogService() {				
	}
	
	@Override
	public String path() {
		return "/log"; //$NON-NLS-1$
	}
	
	/**
	 * Serve the journeyMap.log file
	 */
	@Override
	public void filter(Event event) throws Event, Exception {
		
		// Lazy lookup on first use
		if(logFile==null) {
			logFile = ((JMLogger) JourneyMap.getLogger()).getLogFile();
		}
		
		if(logFile.exists()) {		
			ResponseHeader.on(event).contentType(ContentType.txt);
			serveFile(logFile, event);
		} else {
			throwEventException(404, Constants.getMessageJMERR13(logFile.getPath()), event, true);
		}

	}
}
