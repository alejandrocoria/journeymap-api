package net.techbrew.mcjm.render;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.cartography.MapBlocks;

import java.awt.*;
import java.io.Serializable;


public class BlockInfo implements Serializable {

	private static final long serialVersionUID = 2L;

	public final int meta;
    public final String uid_name;
    public final String uid_modId;
    public final int hash;

    private transient Block block;
	private Color color;
	private Float alpha;

    public BlockInfo(Block block) {
        this(block, 0);
    }

	public BlockInfo(Block block, int meta) {
        if(block==null) throw new IllegalArgumentException("Block can't be null");
		this.block = block;
		this.meta = meta;
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);
        this.uid_name = uid.name;
        this.uid_modId = uid.modId;
        hash = (uid_modId + uid_name + meta).hashCode();
	}

    public BlockInfo(BlockInfo blockInfo, int meta) {
        this.block = blockInfo.block;
        this.meta = meta;
        this.uid_name = blockInfo.uid_name;
        this.uid_modId = blockInfo.uid_modId;
        this.hash = (uid_modId + uid_name + meta).hashCode();
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

    public float getAlpha() {
        if(alpha==null){
            alpha = isTransparent() ? 0f : 1f;
        }
        return alpha;
    }
	
	public Block getBlock() {
        if(block==null){
            block = GameRegistry.findBlock(uid_modId, uid_name);
            if(block==null){
                JourneyMap.getLogger().warning("Block not found: " + uid_modId + ":" + uid_name);
                block = Blocks.air; // TODO  This is probably a bad idea.
            }
        }
        return block;
	}

    public boolean isTransparent() {
        return block.func_149688_o() == Material.field_151579_a;
    }

    public boolean isAir() {
        return MapBlocks.hasFlag(getBlock(), MapBlocks.Flag.HasAir);
    }

    public boolean isTorch() {
        getBlock();
        return block== Blocks.torch||block==Blocks.redstone_torch||block==Blocks.unlit_redstone_torch;
    }

    public boolean isWater() {
        getBlock();
        return block== Blocks.water||block==Blocks.flowing_water;
    }

    public boolean isLava() {
        getBlock();
        return block== Blocks.lava||block==Blocks.flowing_lava;
    }

    public boolean isFoliage() {
        return getBlock() instanceof BlockLeaves;
    }

	@Override
	public int hashCode() {
		return hash;
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
		return hash == other.hash;
	}

	@Override
	public String toString() {
		return "BlockInfo [" + GameRegistry.findUniqueIdentifierFor(block) + ":" + meta + "]";
	}

}
