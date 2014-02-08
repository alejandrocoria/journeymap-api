package net.techbrew.journeymap.server;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.StatTimer;
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
        ResponseHeader.on(event).contentType(ContentType.html).noCache();

        // TODO:  JSON this stuff and don't be a html-generating loser.

        StringBuilder sb = new StringBuilder();
        sb.append('\n').append("<html><head><title>JourneyMap Debug</title><style>");
        sb.append('\n').append("h1{background-color:#ccc; width:100%;text-align:center}");
        sb.append('\n').append("span{vertical-align:middle; margin:2px}");
        sb.append('\n').append(".entry{width:300px;display:inline-block;}");
        sb.append('\n').append(".rgb{display:inline-block;height:32px;width:32px}");
        sb.append('\n').append("</style></head><body><div>");
        sb.append('\n').append("<h1>").append(JourneyMap.MOD_NAME).append("</h1>");
        sb.append('\n').append(PropertyManager.getInstance().toString()).append("</div>");

        sb.append('\n').append("<div><h1>Stat Timers</h1>");
        sb.append('\n').append("<pre>").append(StatTimer.getReport()).append("</pre></div>");

        sb.append('\n').append(ColorCache.getInstance().getCacheDebugHtml());

        sb.append('\n').append("</div></body></html>");
        gzipResponse(event, sb.toString());
	}
}
