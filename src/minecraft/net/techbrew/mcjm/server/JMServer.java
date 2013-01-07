package net.techbrew.mcjm.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.thread.JMThreadFactory;

public class JMServer {
		
	private final JMServ srv;
	private int port;
	
	public JMServer() {
		srv = new JMServ();
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() {		
		
		port = PropertyManager.getInstance().getInteger(PropertyManager.WEBSERVER_PORT_PROP);
	
		//properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		
		Properties arguments = new Properties();
		arguments.put("port", port); //$NON-NLS-1$
		
		srv.arguments = arguments;
		
		// Alias / to FileServlet
		srv.addServlet("/", new FileServlet()); //$NON-NLS-1$
		
		// Alias /jm to ChunkServlet
		srv.addServlet("/jm", new ChunkServlet()); //$NON-NLS-1$
		
		// Alias /save to MapServlet
		srv.addServlet("/save", new SaveMapServlet()); //$NON-NLS-1$
		
		// Alias /msg t0 MessagesServlet
		srv.addServlet("/msg", new MessagesServlet()); //$NON-NLS-1$
		
		// Init thread factory
		JMThreadFactory tf = JMThreadFactory.getInstance();
		
		// Run server in own thread
		ExecutorService es = Executors.newSingleThreadExecutor(tf);
		es.execute(new Runner());
		
		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(tf.newThread(new Shutdown()));
				
	}
	
	public void stop() {
		try {
			srv.notifyStop();
			JourneyMap.getLogger().info("JourneyMap shut down JMServer"); //$NON-NLS-1$
		} catch (Throwable t) {

		}
		srv.destroyAllServlets();
	}
	
	class JMServ extends Acme.Serve.Serve {
		private static final long serialVersionUID = 1887331248990619384L;
		// Overriding method for public access
		public void setMappingTable(PathTreeDictionary mappingtable) {
			super.setMappingTable(mappingtable);
		}
	}
	
	class Runner implements Runnable {
		public void run() {
			Object port = srv.arguments.get("port"); //$NON-NLS-1$
			JourneyMap.getLogger().info("Starting JMServer on http://localhost:" + port +"/"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				srv.serve();			
			}
			catch(Throwable t) {
				String error = Constants.getMessageJMERR03();
				JourneyMap.announce(error);
				JourneyMap.getLogger().severe(error);
				JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
			}
		}
	}
	
	class Shutdown implements Runnable {		
		public void run() {
			JMServer.this.stop();
		}
	}

}
