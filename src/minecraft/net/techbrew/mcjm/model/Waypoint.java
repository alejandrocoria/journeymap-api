package net.techbrew.mcjm.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Generic waypoint data holder
 */
public class Waypoint extends LinkedHashMap<String, Object> {
	
	public static final int REITYPE_NORMAL = 0;
	public static final int REITYPE_DEATH = 1;
	
	public enum Key {
		name, x, y, z, enable, r,g,b,
		dimension,
		reiType, // Normal = 0, Deathpoint = 1
		;
	}
	
	private final String display;

    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, Integer reiType, String display)
    {
        this.put(Key.name.name(), name == null ? "" : name);
        this.put(Key.x.name(), x);
        this.put(Key.y.name(), y);
        this.put(Key.z.name(), z);
        this.put(Key.enable.name(), enable);
        this.put(Key.r.name(), red);
        this.put(Key.g.name(), green);
        this.put(Key.b.name(), blue);
        if(reiType!=null) {
        	this.put(Key.reiType.name(), reiType);
        }
        this.display = display;
    }
    
    public Object getObject(Key p) {
    	return this.get(p.name());
    }
    
    public String getName() {
    	return getString(Key.name);
    }
    
    public int getX() {
    	return getInteger(Key.x);
    }
    
    public int getZ() {
    	return getInteger(Key.z);
    }
    
    public int getReiType() {
    	return getInteger(Key.reiType);
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
