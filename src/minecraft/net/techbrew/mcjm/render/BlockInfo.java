package net.techbrew.mcjm.render;

import java.awt.Color;

import net.minecraft.src.Block;


public class BlockInfo {

	public final int id;
	public final int meta;
	private Color color;
	public float alpha;
	
	public BlockInfo(int id, int meta) {
		this.id = id;
		this.meta = meta;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		if(this.color==null) {
			return null;
		} else {
			return this.color;
		}
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public Block getBlock() {
		return Block.blocksList[id];
	}
	
	public int getRenderColor() {
		return getBlock().getRenderColor(meta);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * (id | meta << 12);
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
	
	public String debugString() {
		StringBuffer sb = new StringBuffer();
		Block block = getBlock();
		if(block!=null) {
			sb.append("Block ").append(block.getUnlocalizedName()).append(" ");
		} else {
			sb.append("Non-Block ");
		}
		sb.append(id).append(":").append(meta);
		if(block!=null) {
			int bcolor = block.getBlockColor();
			if(bcolor!=16777215) {
				sb.append(", blockColor=").append(Integer.toHexString(bcolor));
			}
			int rcolor = block.getBlockColor();
			if(rcolor!=16777215) {
				sb.append(", renderColor=").append(Integer.toHexString(rcolor));
			}
		}
		return sb.toString();
	}
	
}
