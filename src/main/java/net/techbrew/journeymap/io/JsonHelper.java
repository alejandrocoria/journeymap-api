package net.techbrew.journeymap.io;

import java.util.List;
import java.util.Map;

/**
 * Conversion of convenience objects to JSON strings.
 * 
 * @author mwoodman
 *
 */
public class JsonHelper {
	
	public static String toJson(Map props) {
		final StringBuffer sb = new StringBuffer("{");

		Object key,value;
		final Object[] keys = props.keySet().toArray();
		final int size = keys.length;
		
		for(int i=0;i<size;i++) {
			key = keys[i];
			value = props.get(key);
			wrapAsString(sb, key);
			sb.append(":");			
			wrap(sb, value);			
			if(i+1<size) {
				sb.append(",");
			}
		}		
		sb.append("}");
		return sb.toString();
	}
	
	public static String toJson(List list) {
		return toJson(list.toArray());
	}
	
	public static String toJson(Object obj) {
		final StringBuffer sb = new StringBuffer();
		wrap(sb, obj);
		return sb.toString();
	}
	
	public static String toJson(Object[] arr) {
		final StringBuffer sb = new StringBuffer("[");
		final int size = arr.length;
		
		Object value;
		for(int i=0;i<size;i++) {
			value = arr[i];
			wrap(sb, value);			
			if(i+1<size) {
				sb.append(",\n");
			}
		}		
		sb.append("]");
		return sb.toString();
	}
	
	
	static void wrap(final StringBuffer sb, final Object val) {
	
		if(val == null) {
			sb.append("null");			
		} else if(val instanceof String) {
			wrapAsString(sb, val);
		} else if(val instanceof Number) {
			sb.append(val);
		} else if(val instanceof Boolean) {
			sb.append(val);
		} else if(val instanceof Map) {
			sb.append(toJson((Map) val));
		} else if(val instanceof List) {
			sb.append(toJson((List) val));
		} else if(val.getClass().isArray()) {
			sb.append(toJson((Object[]) val));
		} else {
			wrapAsString(sb, val);
		}
	}
	
	static void wrapAsString(final StringBuffer sb, final Object val) {
		String str = val.toString().replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
		sb.append("'").append(str).append("'");	
	}

}
