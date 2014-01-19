package net.techbrew.mcjm.cartography;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;

import java.awt.*;
import java.io.Serializable;


public class BlockInfo implements Serializable {

	private static final long serialVersionUID = 2L;

    public final static class CacheKey implements Serializable
    {
        public final GameRegistry.UniqueIdentifier uid;
        public final int meta;

        public CacheKey(GameRegistry.UniqueIdentifier uid, int meta) {
            this.uid = uid;
            this.meta = meta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey cacheKey = (CacheKey) o;
            if (meta != cacheKey.meta) return false;
            if (!uid.equals(cacheKey.uid)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = uid.hashCode();
            result = 31 * result + meta;
            return result;
        }

        @Override
        public String toString() {
            return uid + ":" + meta;
        }
    }

    private static final LoadingCache<CacheKey, BlockInfo> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(256)
            .build(new CacheLoader<CacheKey, BlockInfo>() {
                @Override
                public BlockInfo load(CacheKey key) throws Exception {
                    return createBlockInfo(key);
                }
            });

    public final CacheKey key;
    private transient Block block;
	private Color color;
	private Float alpha;

    /**
     * Produces a BlockInfo instance.
     * @param chunkMd
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static BlockInfo getBlockInfo(ChunkMD chunkMd, int x, int y, int z) {
        try {
            Block block;
            int meta;
            boolean isAir = false;
            if(y>=0) {
                block = chunkMd.stub.func_150810_a(x, y, z);
                isAir = block.isAir(chunkMd.worldObj, x, y, z);
                meta = (isAir) ? 0 : chunkMd.stub.getBlockMetadata(x, y, z);
            } else {
                block = Blocks.bedrock;
                meta = 0;
            }

            CacheKey key = new CacheKey(GameRegistry.findUniqueIdentifierFor(block), meta);
            return cache.get(key);

        } catch (Exception e) {
            JourneyMap.getLogger().severe("Can't get blockId/meta for chunk " + chunkMd.stub.xPosition + "," + chunkMd.stub.zPosition + " block " + x + "," + y + "," + z); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            JourneyMap.getLogger().severe(LogFormatter.toString(e));
            return null;
        }
    }

    public static BlockInfo getBlockInfo(GameRegistry.UniqueIdentifier uid, int meta) {
        try {
            return cache.get(new CacheKey(uid, meta));
        } catch (Exception e) {
            JourneyMap.getLogger().severe("Can't get BlockInfo for block " + uid + " meta " + meta);
            JourneyMap.getLogger().severe(LogFormatter.toString(e));
            return null;
        }
    }

    private static final BlockInfo createBlockInfo(CacheKey key) {

        Block block = GameRegistry.findBlock(key.uid.modId, key.uid.name);
        if(block==null)
        {
            JourneyMap.getLogger().severe("Block not found for " + key.uid);
            return new BlockInfo(key, Blocks.air);
        }

        BlockInfo info = new BlockInfo(key, block);
        if(info.isAir()) {
            info.color = Color.CYAN; // Should be obvious if it gets displayed somehow.
            info.setAlpha(0f);
        } else {
            Float alpha = MapBlocks.getAlpha(block);
            info.setAlpha(alpha==null ? 1F : alpha);
        }

        JourneyMap.getLogger().info("Created " + info);

        return info;
    }

	private BlockInfo(CacheKey key, Block block) {
        if(block==null) throw new IllegalArgumentException("Block can't be null");
        this.key = key;
		this.block = block;
	}
	
	public Color getColor(ChunkMD chunkMd, int x, int y, int z) {
		if(this.color!=null) {
            return this.color;
        } else {
            boolean biomeColored = MapBlocks.hasFlag(getBlock(), MapBlocks.Flag.BiomeColor);
			Color color = ColorCache.getInstance().getBlockColor(chunkMd, this, biomeColored, x, y, z);
            if(color==null) {
                return Color.BLACK;
            }

            if(biomeColored) {
                return color;
            } else {
                this.color = color;
                return color;
            }
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
            block = GameRegistry.findBlock(key.uid.modId, key.uid.name);
            if(block==null){
                block = Blocks.air;
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
		return key.hashCode();
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
		return key.equals(other.key);
	}

	@Override
	public String toString() {
		return "BlockInfo [" + key.uid + " meta " + key.meta + "]";
	}

}
