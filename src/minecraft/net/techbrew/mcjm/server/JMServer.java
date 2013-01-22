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
		
		// Instantiate daemon
		rupy = new Daemon(props);
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() throws Exception {		
		
		// Alias /player to PlayerService
		rupy.add(new DataService());
				
		// Alias /jm to ChunkServlet
		rupy.add(new ChunkServlet());
		
		// Alias /save to MapServlet
		rupy.add(new SaveMapServlet()); 
		
		// Alias /msg to MessagesServlet
		rupy.add(new MessagesServlet()); 
		
		// Default everything else to FileServlet
		rupy.add(new FileServlet());
		
		// Initialize daemon
		rupy.init();
		
		// Init thread factory
		JMThreadFactory tf = JMThreadFactory.getInstance();
		
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
