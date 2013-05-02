package net.techbrew.mcjm.server;

import java.io.File;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.JMLogger;
import se.rupy.http.Event;


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
