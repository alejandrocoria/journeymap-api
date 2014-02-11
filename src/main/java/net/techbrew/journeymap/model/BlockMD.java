package net.techbrew.journeymap.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.log.LogFormatter;

import java.awt.*;
import java.io.Serializable;
import java.util.EnumSet;


public class BlockMD implements Serializable {

	private static final long serialVersionUID = 1L;

    public final static class CacheKey implements Serializable
    {
        public final BlockUtils.UniqueIdentifierProxy uid;
        public final int meta;

        public CacheKey(BlockUtils.UniqueIdentifierProxy uid, int meta) {
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

    private static final LoadingCache<CacheKey, BlockMD> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(256)
            .build(new CacheLoader<CacheKey, BlockMD>() {
                @Override
                public BlockMD load(CacheKey key) throws Exception {
                try {
                    return createBlockMD(key);
                } catch (Exception e) {
                    throw e;
                }
                }
            });

    public final CacheKey key;
    private transient Block block;
	private Color color;
	private float alpha;
    private final EnumSet<BlockUtils.Flag> flags;
    private final String name;

    /**
     * Produces a BlockMD instance.
     * @param chunkMd
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static BlockMD getBlockMD(ChunkMD chunkMd, int x, int y, int z) {
        try {
            Block block;
            int meta;
            boolean isAir = false;
            if(y>=0) {
                block = chunkMd.getBlock(x, y, z);
                isAir = block == null || block.isAirBlock(chunkMd.worldObj, x, y, z);
                meta = (isAir) ? 0 : chunkMd.stub.getBlockMetadata(x, y, z);
            } else {
                block = Block.bedrock;
                meta = 0;
            }

            if(isAir)
            {
                return cache.get(new CacheKey(BlockUtils.AIRPROXY, 0));
            } else {
                CacheKey key = new CacheKey(new BlockUtils.UniqueIdentifierProxy(block), meta);
                return cache.get(key);
            }


        } catch (Exception e) {
            JourneyMap.getLogger().severe("Can't get blockId/meta for chunk " + chunkMd.stub.xPosition + "," + chunkMd.stub.zPosition + " block " + x + "," + y + "," + z); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            JourneyMap.getLogger().severe(LogFormatter.toString(e));
            return null;
        }
    }

    public static BlockMD getBlockMD(BlockUtils.UniqueIdentifierProxy uid, int meta) {
        try {
            return cache.get(new CacheKey(uid, meta));
        } catch (Exception e) {
            JourneyMap.getLogger().severe("Can't get BlockMD for block " + uid + " meta " + meta);
            JourneyMap.getLogger().severe(LogFormatter.toString(e));
            return null;
        }
    }

    private static final BlockMD createBlockMD(CacheKey key) {

        BlockMD blockMD;

        if(key.uid.blockId==0 || key.uid.blockId -1 > Block.blocksList.length) {
            blockMD = new BlockMD(key, null, "air");
        } else {
            Block block = Block.blocksList[key.uid.blockId];
            blockMD = new BlockMD(key, block, key.uid.toString() + ":" + key.meta);
        }

        if(blockMD.isAir()) {
            blockMD.color = Color.CYAN; // Should be obvious if it gets displayed somehow.
            blockMD.setAlpha(0f);
        } else {
            if(BlockUtils.hasAlpha(blockMD.getBlock())) {
                blockMD.setAlpha(BlockUtils.getAlpha(blockMD.getBlock()));
            } else {
                blockMD.setAlpha(1F);
            }
        }

        //JourneyMap.getLogger().info("Created " + blockMD);

        return blockMD;
    }

	private BlockMD(CacheKey key, Block block, String name) {
        //if(block==null) throw new IllegalArgumentException("Block can't be null");
        this.key = key;
		this.block = block;
        this.name = name;
        this.flags = BlockUtils.getFlags(this.key.uid);
	}

    public boolean hasFlag(BlockUtils.Flag flag)
    {
        return flags.contains(flag);
    }

    public void addFlags(BlockUtils.Flag... addFlags)
    {
        for(BlockUtils.Flag flag : addFlags) {
            this.flags.add(flag);
        }
    }
	
	public RGB getColor(ChunkMD chunkMd, int x, int y, int z) {
		if(this.color!=null) {
            return new RGB(this.color);
        } else {

			Color color = ColorCache.getInstance().getBlockColor(chunkMd, this, x, y, z);
            if(color==null) {
                return new RGB(Color.BLACK);
            }

            if(isBiomeColored()) {
                return new RGB(color);
            } else {
                this.color = color;
                return new RGB(color);
            }
		}
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
        if(alpha<1f) {
            this.flags.add(BlockUtils.Flag.Transparency);
        } else {
            this.flags.remove(BlockUtils.Flag.Transparency);
        }
	}

    public float getAlpha() {
        return alpha;
    }
	
	public Block getBlock() {
        if(block==null && key.uid.blockId>0){
            if(key.uid.blockId==0) {
                block = Block.blocksList[key.uid.blockId];
            }
        }
        return block;
	}

    public boolean isTransparent() {
        return block.blockMaterial == Material.air;
    }

    public boolean isAir() {
        return hasFlag(BlockUtils.Flag.HasAir);
    }

    public boolean isTorch() {
        getBlock();
        return block== Block.torchWood||block==Block.torchRedstoneActive||block==Block.torchRedstoneIdle;
    }

    public boolean isWater() {
        getBlock();
        return block== Block.waterMoving||block==Block.waterStill;
    }

    public boolean isLava() {
        getBlock();
        return block== Block.lavaMoving||block==Block.lavaStill;
    }

    public boolean isFoliage() {
        return getBlock() instanceof BlockLeaves;
    }

    public boolean isBiomeColored() {
        return flags.contains(BlockUtils.Flag.BiomeColor) || flags.contains(BlockUtils.Flag.CustomBiomeColor);
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
		if (!(obj instanceof BlockMD))
			return false;
		BlockMD other = (BlockMD) obj;
		return key.equals(other.key);
	}

	@Override
	public String toString() {
		return "BlockMD [" + key.uid + " meta " + key.meta + "]";
	}

    /**
     * Use with care, since this creates an itemstack just to get the name.
     * @return
     */
    public String getName() {
        return name;
    }

    public static void clearCache() {
        cache.invalidateAll();
    }


}
