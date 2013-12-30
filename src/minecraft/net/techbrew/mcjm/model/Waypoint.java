package net.techbrew.mcjm.model;

import net.minecraft.util.ChunkCoordinates;

import java.awt.*;
import java.util.LinkedHashMap;

/**
 * Generic waypoint data holder
 */
public class Waypoint extends LinkedHashMap<String, Object> {
	
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_DEATH = 1;
	
	public enum Key {
		id,
		name, 
		x, y, z, 
		enable,
		r,g,b,
		dimension,
		type, // Normal = 0, Deathpoint = 1
		origin,
		display
		;
	}

    ChunkCoordinates location;
	
    public Waypoint(String name, int x, int y, int z, boolean enable, int red, int green, int blue, int type, String origin, String display)
    {
    	if(name==null) name = "";
    	this.put(Key.id.name(), name + "_" + x + "_" + y + "_" + z);
        this.put(Key.name.name(), name);
        this.put(Key.x.name(), x);
        this.put(Key.y.name(), y);
        this.put(Key.z.name(), z);
        this.put(Key.enable.name(), enable);
        this.put(Key.r.name(), red);
        this.put(Key.g.name(), green);
        this.put(Key.b.name(), blue);
        this.put(Key.type.name(), type);
        this.put(Key.origin.name(), origin);
        this.put(Key.display.name(), display);
        location = new ChunkCoordinates(x, y, z);
    }
    
    public String getId() {
    	return getString(Key.id);
    }
    
    public Object getObject(Key p) {
    	return this.get(p.name());
    }
    
    public String getName() {
    	return getString(Key.name);
    }
    
    public String getOrigin() {
    	return getString(Key.origin);
    }
    
    public int getX() {
    	return getInteger(Key.x);
    }

    public int getY() {
        return getInteger(Key.y);
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
    
    public String getDisplay() {
    	return getString(Key.display);
    }

    public ChunkCoordinates getLocation() {
        return location;
    }
    
    @Override
	public String toString() {
    	return getDisplay();
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
