package net.techbrew.journeymap.model;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.IPlantable;
import net.techbrew.journeymap.JourneyMap;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;

public class BlockUtils {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);

    public enum Flag {HasAir, BiomeColor, CustomBiomeColor, NotHideSky, NotCeiling, NoShadow, Side2Texture, Transparency}

    private final static HashMap<UniqueIdentifierProxy, EnumSet<Flag>> blockFlags = new HashMap<UniqueIdentifierProxy, EnumSet<Flag>>(64);
    private final static HashMap<UniqueIdentifierProxy, Float> blockAlphas = new HashMap<UniqueIdentifierProxy, Float>(8);
    public final static UniqueIdentifierProxy AIRPROXY = new UniqueIdentifierProxy(0, "air");

	/**
	 * Constructor
	 */
	public static void initialize() {

        blockAlphas.clear();
        setAlpha(AIRPROXY, 0F); // air
        setAlpha(Block.ice, .8F);
        setAlpha(Block.glass, .3F);
        setAlpha(Block.thinGlass, .3F);
        setAlpha(Block.vine, .2F);
        setAlpha(Block.torchWood, .5F);

        blockFlags.clear();
        setFlags(AIRPROXY, Flag.HasAir, Flag.NotHideSky, Flag.NoShadow, Flag.NotCeiling);
        setFlags(Block.fire, Flag.NoShadow, Flag.Side2Texture);
        setFlags(Block.waterStill, Flag.BiomeColor);
        setFlags(Block.grass, Flag.BiomeColor);
        setFlags(Block.glass, Flag.NotCeiling, Flag.NoShadow);
        setFlags(Block.thinGlass, Flag.NotCeiling, Flag.NoShadow);
        setFlags(Block.ladder, Flag.NotHideSky);
        setFlags(Block.lavaStill, Flag.NoShadow);
        setFlags(Block.leaves, Flag.NotHideSky, Flag.BiomeColor);
        setFlags(Block.torchRedstoneActive, Flag.HasAir);
        setFlags(Block.torchWood, Flag.HasAir);
        setFlags(Block.tallGrass, Flag.BiomeColor);
        setFlags(Block.tripWireSource, Flag.NoShadow);
        setFlags(Block.tripWire, Flag.NoShadow);
        setFlags(Block.torchRedstoneIdle, Flag.HasAir);
        setFlags(Block.vine, Flag.NotHideSky, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Block.waterMoving, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Block.web, Flag.NotHideSky, Flag.Side2Texture);

        for(Block block : Block.blocksList) {

            if(block==null) continue;
            if(block.getUnlocalizedName().equals("tile.ForgeFiller")) {
                break;
            }

            UniqueIdentifierProxy uidProxy = new UniqueIdentifierProxy(block);

            if(block.blockMaterial == Material.air) {
                setFlags(uidProxy, Flag.HasAir, Flag.NotHideSky, Flag.NoShadow, Flag.NotCeiling);
                JourneyMap.getLogger().fine(uidProxy + " flags set to hide block");
                continue;
            }

            if(block instanceof BlockLeavesBase || block instanceof BlockGrass || block instanceof BlockVine || block instanceof BlockLilyPad) {
                setFlags(uidProxy, Flag.BiomeColor);
                JourneyMap.getLogger().fine(uidProxy + " flag set: Flag.BiomeColor");
            }

            if(block instanceof IPlantable) {
                setFlags(uidProxy, Flag.Side2Texture, Flag.NoShadow);
                JourneyMap.getLogger().fine(uidProxy + " flags set: Flag.Side2Texture, Flag.NoShadow");
            }
        }
    }

    /**
     * Attempt at faster way to figure out if there is sky above
     * @param chunkMd
     * @param x
     * @param y
     * @param z
     * @return
     */
	public static boolean skyAbove(ChunkMD chunkMd, final int x, final int y, final int z) {
		
		boolean seeSky = chunkMd.stub.canBlockSeeTheSky(x, y, z);
		if(!seeSky) {
			seeSky = true;
			Block block;
			int checkY = y;
			final int maxY = chunkMd.stub.getHeightValue(x, z);
			while(seeSky && checkY<=maxY) {
                block = chunkMd.getBlock(x, checkY, z);
				if(hasFlag(block, Flag.NotHideSky)) {
					checkY++;
				} else {
					seeSky = false;
					break;
				}
			}
		}
		return seeSky;
	}

    /**
     * Attempt at faster way to figure out if there is sky above
     * @param chunkMd
     * @param x
     * @param maxY
     * @param z
     * @return
     */
	public static int ceiling(ChunkMD chunkMd, final int x, final int maxY, final int z) {
		
		final int chunkHeight = chunkMd.stub.getHeightValue(x, z);
		final int topY = Math.min(maxY, chunkHeight);

		Block block;
		int y = topY;
		
		while(y>=0) {
            block = chunkMd.getBlock(x, y, z);
			if(chunkMd.stub.canBlockSeeTheSky(x, y, z)) {
				y--;
			} else if(hasFlag(block, Flag.NotCeiling)) {
				y--;
			} else if(hasFlag(block, Flag.NotHideSky)) {
                y--;
            } else {
				break;
			}
		}
		
		return Math.max(0,y);
	}

    public static EnumSet<Flag> getFlags(Block block)
    {
        return getFlags(new UniqueIdentifierProxy(block));
    }

    public static EnumSet<Flag> getFlags(UniqueIdentifierProxy uidProxy)
    {
        EnumSet<Flag> flags = blockFlags.get(uidProxy);
        return flags==null ? EnumSet.noneOf(Flag.class) : flags;
    }

    public static void setFlags(Block block, Flag... flags)
    {
        UniqueIdentifierProxy uidProxy = new UniqueIdentifierProxy(block);
        setFlags(uidProxy, flags);
    }

    public static void setFlags(UniqueIdentifierProxy uidProxy, Flag... flags)
    {
        EnumSet<Flag> eset = getFlags(uidProxy);
        eset.addAll(Arrays.asList(flags));
        blockFlags.put(uidProxy, eset);
    }

    public static boolean hasFlag(Block block, Flag flag)
    {
        EnumSet<Flag> flags = blockFlags.get(new UniqueIdentifierProxy(block));
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasAnyFlags(Block block, Flag... flags)
    {
        EnumSet<Flag> flagSet = blockFlags.get(GameRegistry.findUniqueIdentifierFor(block));
        if(flagSet==null) return false;
        for(Flag flag : flags) {
            if(flagSet.contains(flag)){
                return true;
            }
        }
        return false;
    }

    public static boolean hasFlag(UniqueIdentifierProxy uid, Flag flag)
    {
        EnumSet<Flag> flags = getFlags(uid);
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(new UniqueIdentifierProxy(block));
    }

    public static float getAlpha(Block block)
    {
        Float alpha = blockAlphas.get(new UniqueIdentifierProxy(block));
        return alpha==null ? 1F : alpha;
    }

    public static void setAlpha(UniqueIdentifierProxy uidProxy, Float alpha)
    {
        blockAlphas.put(uidProxy, alpha);
    }

    public static void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(new UniqueIdentifierProxy(block), alpha);
    }

    public static HashMap getFlagsMap()
    {
        return blockFlags;
    }

    public static class UniqueIdentifierProxy
    {
        public final int blockId;
        public final String name;

        UniqueIdentifierProxy(int blockId, String name)
        {
            this.blockId = blockId;
            this.name = name;
        }

        UniqueIdentifierProxy(Block block)
        {
            this.blockId = block.blockID;
            this.name = block.getUnlocalizedName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UniqueIdentifierProxy)) return false;

            UniqueIdentifierProxy that = (UniqueIdentifierProxy) o;

            if (blockId!=that.blockId) return false;
            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = blockId;
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return blockId + ":" + name;
        }
    }
	
}
