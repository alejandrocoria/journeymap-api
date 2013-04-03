package net.techbrew.mcjm.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Generic waypoint data holder
 */
public class Waypoint {
	
	public enum Key {
		name, x, y, z, enable, r,g,b,
		dimension,
		reiType, // Normal = 0, Deathpoint = 1
		;
	}
	
	private final String display;
	
	LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();

    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, Integer reiType, String display)
    {
        map.put(Key.name.name(), name == null ? "" : name);
        map.put(Key.x.name(), x);
        map.put(Key.y.name(), y);
        map.put(Key.z.name(), z);
        map.put(Key.enable.name(), enable);
        map.put(Key.r.name(), red);
        map.put(Key.g.name(), green);
        map.put(Key.b.name(), blue);
        if(reiType!=null) {
        	map.put(Key.reiType.name(), reiType);
        }
        this.display = display;
    }
    
    public Integer getInteger(Key p) {
    	Object val = map.get(p.name());
    	return val==null ? null : (Integer) val; 
    }

    public Boolean getBoolean(Key p) {
    	Object val = map.get(p.name());
    	return val==null ? null : (Boolean) val; 
    }
    
    public String getString(Key p) {
    	Object val = map.get(p.name());
    	return val==null ? null : (String) val; 
    }
    
    public Object getObject(Key p) {
    	return map.get(p.name());
    }
    
    public HashMap<String, Object> getProperties() {
    	return map;
    }
    
    public String toString() {
    	return this.display;
    }
    
}
