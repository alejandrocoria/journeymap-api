package net.techbrew.mcjm.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;

public class JMLogger extends Logger {

	public static final String LOG_FILE = "journeyMap.log"; //$NON-NLS-1$
	java.util.logging.ConsoleHandler consoleHandler;
	java.util.logging.FileHandler logHandler;
	
	public JMLogger() {
		super("JourneyMap", null); //$NON-NLS-1$
		setLevel(Level.INFO);
		
		// Console logging
		consoleHandler = new java.util.logging.ConsoleHandler();
		consoleHandler.setFormatter(new LogFormatter());
		this.addHandler(consoleHandler);
		
		// File logging
		try {
			File logFile = getLogFile();
			if(logFile.exists()) {
				logFile.delete();
			} else {
				FileHandler.getJourneyMapDir().mkdirs();
			}
			logHandler = new java.util.logging.FileHandler(logFile.getAbsolutePath());
			logHandler.setFormatter(new LogFormatter());
			this.addHandler(logHandler);					
			
		} catch (SecurityException e) {
			this.severe(LogFormatter.toString(e));
		} catch (IOException e) {
			this.severe(LogFormatter.toString(e));
		}
	}
	
	/**
	 * Show system properties and those from the PropertyManager.
	 */
	public void environment() {
		
		info("os.name = " + System.getProperty("os.name") + //$NON-NLS-1$ //$NON-NLS-2$
			", os.arch = " + System.getProperty("os.arch") +  //$NON-NLS-1$ //$NON-NLS-2$
			", user.country = " + System.getProperty("user.country") + //$NON-NLS-1$ //$NON-NLS-2$
			", user.language = " + System.getProperty("user.language") + //$NON-NLS-1$ //$NON-NLS-2$
			", java.version = " + System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
		
		info("Game settings language: " + Minecraft.getMinecraft().gameSettings.language + " / Locale: " + Constants.getLocale());
	}
	
	
	/**
	 * Set the logging level from the value in the properties file.
	 */
	public void setLevelFromProps() {

		String propLevel = null;
		Level level = Level.INFO;
		try {
			propLevel = PropertyManager.getInstance().getString(PropertyManager.Key.LOGGING_LEVEL);			
			if(!propLevel.equals(Level.INFO)) {
				level = Level.parse(propLevel);
				info("Setting log level from journeyMap.properties: " + level); //$NON-NLS-1$
			}
		} catch(IllegalArgumentException e) {
			warning("Could not read " + PropertyManager.Key.LOGGING_LEVEL + " in " + PropertyManager.FILE_NAME + ": " + propLevel);			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch(Throwable t) {
			severe(LogFormatter.toString(t));
		} finally {
			setLevel(level);
			consoleHandler.setLevel(level);
			logHandler.setLevel(level);
		}
	}
	
	/**
	 * Return a handle to the log file used.
	 * @return
	 */
	public File getLogFile() {
		return new File(FileHandler.getJourneyMapDir(), LOG_FILE);
	}
	
	
}
