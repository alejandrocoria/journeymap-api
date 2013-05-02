package net.techbrew.mcjm.model;

import java.awt.Color;
import java.util.LinkedHashMap;

/**
 * Generic waypoint data holder
 */
public class Waypoint extends LinkedHashMap<String, Object> {
	
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_DEATH = 1;
	
	public enum Key {
		name, x, y, z, enable, r,g,b,
		dimension,
		type, // Normal = 0, Deathpoint = 1
		origin,
		;
	}
	
	private final String display;

    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, int type, String origin, String display)
    {
        this.put(Key.name.name(), name == null ? "" : name);
        this.put(Key.x.name(), x);
        this.put(Key.y.name(), y);
        this.put(Key.z.name(), z);
        this.put(Key.enable.name(), enable);
        this.put(Key.r.name(), red);
        this.put(Key.g.name(), green);
        this.put(Key.b.name(), blue);
        this.put(Key.type.name(), type);
        this.put(Key.origin.name(), origin);
        this.display = display;
    }
    
    public Object getObject(Key p) {
    	return this.get(p.name());
    }
    
    public String getName() {
    	return getString(Key.name);
    }
    
    public String getOrigin() {
    	return getString(Key.origin.name);
    }
    
    public int getX() {
    	return getInteger(Key.x);
    }
    
    public int getZ() {
    	return getInteger(Key.z);
    }
    
    public int getType() {
    	return getInteger(Key.type);
    }
    
    public boolean getEnable() {
    	return getBoolean(Key.enable);
    }
    
    public Color getColor() {
    	int r = getInteger(Key.r);
    	int g = getInteger(Key.g);
    	int b = getInteger(Key.b);
    	return new Color(r,g,b);
    }
    
    public String toString() {
    	return this.display;
    }
    
    /** Internal to class **/
    
    Integer getInteger(Key p) {
    	Object val = this.get(p.name());
    	return val==null ? null : (Integer) val; 
    }

    Boolean getBoolean(Key p) {
    	Object val = this.get(p.name());
    	return val==null ? null : (Boolean) val; 
    }
    
    String getString(Key p) {
    	Object val = this.get(p.name());
    	return val==null ? null : (String) val; 
    }
    
}
