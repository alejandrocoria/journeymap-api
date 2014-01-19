package net.techbrew.mcjm.server;

import net.techbrew.mcjm.cartography.ColorCache;
import se.rupy.http.Event;


/**
 * Serve debug goodness
 * 
 * @author mwoodman
 *
 */
public class DebugService extends FileService {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public DebugService() {
	}
	
	@Override
	public String path() {
		return "/debug"; //$NON-NLS-1$
	}
	
	/**
	 * Serve it.
	 */
	@Override
	public void filter(Event event) throws Event, Exception {

        ResponseHeader.on(event).contentType(ContentType.html);
        gzipResponse(event, ColorCache.getInstance().getCacheDebugHtml());
	}
}
