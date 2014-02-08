package net.techbrew.journeymap.server;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import se.rupy.http.Event;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * Serve files from disk.  Works for zip-archived files
 * when the mod is in normal use, also works for standard 
 * file-system access when the mod is unzipped or when
 * running from Eclipse during development.
 * 
 * @author mwoodman
 *
 */
public class FileService extends BaseService {
	
	private static final long serialVersionUID = 2L;

	private String resourcePath;
	private boolean useZipEntry;
    private File zipFile;
	
	/**
	 * Default constructor
	 */
	public FileService() {
		
		URL resourceDir = JourneyMap.class.getResource("/assets/journeymap/web"); //$NON-NLS-1$
				
		if(resourceDir==null) {
			JourneyMap.getLogger().severe("Can't determine path to webroot!");
		} else {

            // Format reusable resourcePath
            resourcePath = resourceDir.getPath();
            if(resourcePath.endsWith("/")) { //$NON-NLS-1$
                resourcePath = resourcePath.substring(0, resourcePath.length()-1);
            }

            // Check whether operating out of a zip/jar
            useZipEntry = (resourceDir.getProtocol().equals("file") || resourceDir.getProtocol().equals("jar")) && resourcePath.contains("!/"); //$NON-NLS-1$	//$NON-NLS-2$
        }
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
			String requestPath = null;
			path = event.query().path(); //$NON-NLS-1$
			
			if(path.startsWith("/skin/")) {
				serveSkin(path.split("/skin/")[1], event);
				return;
			}
			
			if("/".equals(path)) { //$NON-NLS-1$
				// Default to index
				requestPath = resourcePath + "/index.html"; //$NON-NLS-1$
			} else {
				requestPath = resourcePath + path;
			}

			if(useZipEntry) { 				
				// Running out of a Zip archive or jar
				String[] tokens = requestPath.split("file:")[1].split("!/"); //$NON-NLS-1$ //$NON-NLS-2$

                // Lazy load the file
                if(zipFile==null) {
                    zipFile = new File( URI.create(tokens[0]).getPath() );
                    if(!zipFile.canRead()) {
                        throw new RuntimeException("Can't read Zip file: " + zipFile + " (originally: " + tokens[0] + ")");
                    }
                }
                String innerName = tokens[1];
				
				BufferedOutputStream dest = null;
		        FileInputStream fis = new FileInputStream(zipFile);
		        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		        ZipEntry zipEntry;
		        File destFile;
		        boolean found = false;
		        while((zipEntry = zis.getNextEntry()) != null) {
		            if(innerName.equals(zipEntry.getName())) {
		            	// Set inputstream
		            	in = new ZipFile(zipFile).getInputStream(zipEntry);
		            	ResponseHeader.on(event).content(zipEntry);
						serveStream(in, event);
						found = true;
						break;
		            }
		        }
		        zis.close();
		        fis.close();

		        // Didn't find it		    
		        if(!found) {
					JourneyMap.getLogger().severe("zipEntry not found: " + zipEntry + " in " + zipFile);	
					throwEventException(404, Constants.getMessageJMERR13(requestPath), event, true);
		        }
				
			} else {
				// Running out of a directory
				File file = new File(requestPath); 				
				if(file.exists()) {					
					serveFile(file, event);
				} else {
					JourneyMap.getLogger().severe("Directory not found: " + requestPath);	
					throwEventException(404, Constants.getMessageJMERR13(requestPath), event, true);
				}
			}
			

		} catch(Event eventEx) {
			throw eventEx;
		} catch(Throwable t) {				
			JourneyMap.getLogger().severe(LogFormatter.toString(t));			
			throwEventException(500, Constants.getMessageJMERR12(path), event, true);
		} 
	}
	
	public void serveSkin(String username, Event event) throws Exception {
		
		ResponseHeader.on(event).contentType(ContentType.png);
		
		TextureImpl tex = TextureCache.instance().getPlayerSkin(username);
		BufferedImage img = tex.getImage();
		if(img!=null) {			
			serveImage(event, img);
		} else {
			event.reply().code("404 Not Found");
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
	public void serveFile(File sourceFile, Event event) throws Event, IOException {
		
		// Set content headers
		ResponseHeader.on(event).content(sourceFile);
		
		// Stream file
		serveStream(new FileInputStream(sourceFile), event);
	}

    /**
     * Respond with the contents of a file input stream.
     * @param input
     * @param event
     * @throws Event
     * @throws IOException
     */
	public void serveStream(final InputStream input, Event event) throws Event, IOException {
		
		// Transfer inputstream to event outputstream
		ReadableByteChannel inputChannel = null;
		WritableByteChannel outputChannel = null;
		try {
			inputChannel = Channels.newChannel(input);
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
            GZIPOutputStream output = new GZIPOutputStream(bout);
            
			outputChannel = Channels.newChannel(output);			
			ByteBuffer buffer = ByteBuffer.allocate(65536); 
			while (inputChannel.read(buffer) != -1) { 
				buffer.flip( ); 
				outputChannel.write(buffer); 
				buffer.clear( ); 
			}		
			
			output.flush();
            output.close();
            bout.close();
            
            byte[] gzbytes = bout.toByteArray();
            
            ResponseHeader.on(event).contentLength(gzbytes.length).setHeader("Content-encoding", "gzip");	//$NON-NLS-1$ //$NON-NLS-2$
            event.output().write(gzbytes);
            
		} catch (IOException e) {
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			throw event;
		} finally {
			if (input != null) {
				input.close();
			}
		}

	}

	/**
	 * Gzip encode a string and return the byte array.  
	 * 
	 * @param data
	 * @return
	 */
	@Override
	protected byte[] gzip(String data) {
        ByteArrayOutputStream bout = null;
        try {
            bout = new ByteArrayOutputStream();
            GZIPOutputStream output = new GZIPOutputStream(bout);
            output.write(data.getBytes());
            output.flush();
            output.close();
            bout.close();
            return bout.toByteArray();
        } catch (Exception ex) {
        	JourneyMap.getLogger().warning("Failed to gzip encode: " + data);
        	return null;
        }
    }

}