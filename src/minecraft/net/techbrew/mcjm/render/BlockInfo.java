package net.techbrew.mcjm.render;

public class BlockInfo {

	public final int id;
	public final int meta;
	
	public BlockInfo(int id, int meta) {
		this.id = id;
		this.meta = meta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + meta;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BlockInfo))
			return false;
		BlockInfo other = (BlockInfo) obj;
		if (id != other.id)
			return false;
		if (meta != other.meta)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BlockInfo [" + id + ":" + meta + "]";
	}
	
}
