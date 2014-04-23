package net.techbrew.journeymap.server;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.PropertyManager;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.net.URLEncoder;

/**
 * Provide player data
 * 
 * @author mwoodman
 *
 */
public class PropertyService extends BaseService {

	private static final long serialVersionUID = 4412225358529161454L;

	public static final String CALLBACK_PARAM = "callback";  //$NON-NLS-1$	
	
	/**
	 * Serves / saves property info
	 */
	public PropertyService() {
		super();
	}
	
	@Override
	public String path() {
		return "/properties";
	}
	
	@Override
	public void filter(Event event) throws Event, Exception {

		// Parse query for parameters
		Query query = event.query();	
		query.parse();
		String path = query.path();
		
		if(query.method()==Query.POST) {
			post(event);
			return;
		} else if(query.method()!=Query.GET) {
			throw new Exception("HTTP method not allowed");
		}
		
		// Build the response string
		StringBuffer jsonData = new StringBuffer();
				
		// Check for callback to determine Json or JsonP
		boolean useJsonP = query.containsKey(CALLBACK_PARAM);
		if(useJsonP) {
			jsonData.append(URLEncoder.encode(query.get(CALLBACK_PARAM).toString(), UTF8.name()));
			jsonData.append("("); //$NON-NLS-1$	
		} else {
			jsonData.append("data="); //$NON-NLS-1$	
		}	
		
		// Put map into json form
		jsonData.append(GSON.toJson(PropertyManager.getInstance().getProperties()));
		
		// Finish function call for JsonP if needed
		if(useJsonP) {
			jsonData.append(")"); //$NON-NLS-1$
			
			// Optimize headers for JSONP
			ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
		}

		// Gzip response
		gzipResponse(event, jsonData.toString());
	}
	
	public void post(Event event) throws Exception {
		
		try {
			Query query = event.query();
			String[] param = query.parameters().split("=");
			if(param.length!=2) throw new Exception("Expected single key-value pair");
			PropertyManager pm = PropertyManager.getInstance();
			PropertyManager.Key key = PropertyManager.Key.lookup(param[0]);
			if(key!=null) {
				// todo: type check param value
				pm.setProperty(key, param[1]);
				JourneyMap.getLogger().finer("Updated property: " + param[0] + "=" + param[01]);
			} else {
				throw new Exception("Unknown property key: " + param[0]);
			}
		} catch(Exception e) {
			throw e;
		}
	}
	
}
