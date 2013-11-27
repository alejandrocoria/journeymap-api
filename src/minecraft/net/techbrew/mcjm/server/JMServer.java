package net.techbrew.mcjm.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.thread.JMThreadFactory;
import se.rupy.http.Daemon;

/**
 * Wraps Rupy Daemon and provides thread management.  Tests webserver_port
 * before starting daemon.
 * 
 * @author mwoodman
 *
 */
public class JMServer {
		
	private final static int MAXPORT = 9990;
	private final static int MAXFAILS = 5;
	
	private final Logger logger = JourneyMap.getLogger();
	
	private Daemon rupy;
	private int port;
	private boolean ready = false;
	
	public JMServer() {		
		port = PropertyManager.getInstance().getInteger(PropertyManager.Key.WEBSERVER_PORT);
		validatePort();
	}
	
	/**
	 * Verify port can be bound, try to find another one if not.
	 */
	private void validatePort() {
		
		int hardFails = 0;
		int testPort = port;
		final int maxPort = Math.max(MAXPORT, port + 1000);
		boolean validPort = false;
		
		while(!validPort && hardFails<=MAXFAILS && testPort<=maxPort) {
			ServerSocketChannel server = null;
			try {
				server = ServerSocketChannel.open();
				server.socket().bind(new InetSocketAddress(testPort));								
				validPort = true;
			} catch(java.net.BindException e) {
				logger.warning("Port " + testPort + " already in use");
				testPort+=10;
			} catch (Throwable t) {
				logger.severe("Error when testing port " + testPort + ": " + t);
				hardFails++;
			} finally {
				if(server != null) {
					try {
						server.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		ready = validPort;
		
		if(ready && port!=testPort) {
			logger.info("Webserver will use port " + testPort + " for this session");
			port = testPort;
		}	
		
		if(!ready && hardFails>MAXFAILS) {
			logger.severe("Gave up finding a port for webserver after " + hardFails + " failures to test ports!");
		}
		
		if(!ready && testPort>MAXPORT) {
			logger.severe("Gave up finding a port for webserver after testing ports " + port + " - " + maxPort + " without finding one open!");
		}
						
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() throws Exception {		
		
		if(!ready) throw new IllegalStateException("Initialization failed");
		
		// Init properties for daemon
		Properties props = new Properties();
		
		// Use port from journeymap properties

		props.put("port", Integer.toString(port)); //$NON-NLS-1$
		props.put("delay", Integer.toString(5000)); //$NON-NLS-1$ // socket timeout in ms
		props.put("timeout", Integer.toString(0)); //$NON-NLS-1$ // session timeout, 0 to disable sessions
		props.put("threads", Integer.toString(15)); //$NON-NLS-1$
		
		// Rupy logging is spammy.  Only enable it if you really need to.
		Level logLevel = Level.parse(PropertyManager.getInstance().getString(PropertyManager.Key.LOGGING_LEVEL));
		if(logLevel.intValue()<=(Level.FINEST.intValue())) {
			props.put("debug", Boolean.TRUE.toString()); //$NON-NLS-1$
		} 	
		if(logLevel.intValue()<=(Level.FINER.intValue())) {
			props.put("verbose", Boolean.TRUE.toString()); //$NON-NLS-1$
		} 
				
		rupy = new Daemon(props);		
		rupy.add(new DataService());
		rupy.add(new LogService());
		rupy.add(new TileService());
		rupy.add(new ActionService()); 
		rupy.add(new FileService());
		rupy.add(new PropertyService());
		
		// Initialize daemon
		rupy.init();
		
		// Init thread factory
		JMThreadFactory tf = new JMThreadFactory("svr");
		
		// Run server in own thread
		ExecutorService es = Executors.newSingleThreadExecutor(tf);
		es.execute(rupy);
		
		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable() {
			@Override
			public void run() {
				stop();
			}
		}));
		
		logger.info("Started webserver on port " + port); //$NON-NLS-1$
			
	}
	
	public void stop() {
		try {
			rupy.stop();
			logger.info("Stopped webserver without errors"); //$NON-NLS-1$
		} catch (Throwable t) {
			logger.info("Stopped webserver with error: " + t); //$NON-NLS-1$
		}
	}		

}
