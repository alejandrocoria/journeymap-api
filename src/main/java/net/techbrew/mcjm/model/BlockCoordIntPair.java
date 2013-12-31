package net.techbrew.mcjm.model;

public class BlockCoordIntPair {
	
	public int x;
	public int z;
	
	public BlockCoordIntPair() {
		setLocation(0,0);
	}

	public BlockCoordIntPair(int x, int z) {
		setLocation(x,z);
	}
	
	public void setLocation(int x, int z) {
		this.x = x;
		this.z = z;
	}

}
