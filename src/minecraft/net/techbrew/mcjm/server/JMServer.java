package net.techbrew.mcjm.server;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.thread.JMThreadFactory;
import se.rupy.http.Daemon;

public class JMServer {
		
	private final Daemon rupy;
	private int port;
	
	public JMServer() {
		
		// Init properties for daemon
		Properties props = new Properties();
		
		// Use port from journeymap properties
		port = PropertyManager.getInstance().getInteger(PropertyManager.WEBSERVER_PORT_PROP);
		props.put("port", Integer.toString(port)); //$NON-NLS-1$
		props.put("delay", Integer.toString(10000)); //$NON-NLS-1$
		props.put("timeout", Integer.toString(10000)); //$NON-NLS-1$
		props.put("cookie", Integer.toString(0)); //$NON-NLS-1$
		
		// Instantiate daemon
		rupy = new Daemon(props);
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() throws Exception {		
		
		rupy.add(new DataService());
		rupy.add(new LogService());
		rupy.add(new MapService());
		rupy.add(new SaveMapService()); 
		rupy.add(new FileService());
		
		// Initialize daemon
		rupy.init();
		
		// Init thread factory
		JMThreadFactory tf = new JMThreadFactory("JMServer");
		
		// Run server in own thread
		ExecutorService es = Executors.newSingleThreadExecutor(tf);
		es.execute(rupy);
		
		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable() {
			public void run() {
				stop();
			}
		}));
		
		JourneyMap.getLogger().info("Started webserver on port " + port); //$NON-NLS-1$
				
	}
	
	public void stop() {
		try {
			rupy.stop();
			JourneyMap.getLogger().info("Stopped webserver without errors"); //$NON-NLS-1$
		} catch (Throwable t) {
			JourneyMap.getLogger().info("Stopped webserver with error: " + t); //$NON-NLS-1$
		}
	}		

}
