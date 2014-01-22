package net.techbrew.mcjm.model;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.techbrew.mcjm.JourneyMap;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

public class BlockUtils {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);

    public enum Flag {HasAir, BiomeColor, NotHideSky, NotTopBlock, NotCeiling, NoShadow, Side2Texture}

    private final static HashMap<GameRegistry.UniqueIdentifier, EnumSet<Flag>> blockFlags = new HashMap<GameRegistry.UniqueIdentifier, EnumSet<Flag>>(64);
    private final static HashMap<GameRegistry.UniqueIdentifier, Float> blockAlphas = new HashMap<GameRegistry.UniqueIdentifier, Float>(8);

	/**
	 * Constructor
	 */
	public static void initialize() {

        blockAlphas.clear();
        setAlpha(Blocks.air, 0F);
        setAlpha(Blocks.ice, .8F);
        setAlpha(Blocks.glass, .3F);
        setAlpha(Blocks.glass_pane, .3F);
        setAlpha(Blocks.vine, .2F);
        setAlpha(Blocks.torch, .5F);

        blockFlags.clear();
        setFlags(Blocks.air, Flag.HasAir, Flag.NotHideSky, Flag.NotTopBlock, Flag.NoShadow, Flag.NotCeiling);
        setFlags(Blocks.brown_mushroom, Flag.Side2Texture);
        setFlags(Blocks.carrots, Flag.Side2Texture);
        setFlags(Blocks.deadbush, Flag.NotTopBlock, Flag.Side2Texture);
        setFlags(Blocks.fire, Flag.NoShadow, Flag.Side2Texture);
        setFlags(Blocks.flowing_water, Flag.BiomeColor);
        setFlags(Blocks.grass, Flag.BiomeColor);
        setFlags(Blocks.glass, Flag.NotCeiling, Flag.NoShadow);
        setFlags(Blocks.glass_pane, Flag.NotCeiling, Flag.NoShadow);
        setFlags(Blocks.ladder, Flag.NotHideSky);
        setFlags(Blocks.lava, Flag.NoShadow);
        setFlags(Blocks.leaves, Flag.NotHideSky, Flag.BiomeColor);
        setFlags(Blocks.leaves2, Flag.NotHideSky, Flag.BiomeColor);
        setFlags(Blocks.redstone_torch, Flag.HasAir);
        setFlags(Blocks.red_flower, Flag.Side2Texture);
        setFlags(Blocks.red_mushroom, Flag.Side2Texture);
        setFlags(Blocks.reeds, Flag.Side2Texture);
        setFlags(Blocks.sapling, Flag.Side2Texture);
        setFlags(Blocks.torch, Flag.HasAir);
        setFlags(Blocks.melon_stem, Flag.Side2Texture);
        setFlags(Blocks.nether_wart, Flag.Side2Texture);
        setFlags(Blocks.potatoes, Flag.Side2Texture);
        setFlags(Blocks.pumpkin_stem, Flag.Side2Texture);
        setFlags(Blocks.tallgrass, Flag.NotTopBlock, Flag.Side2Texture, Flag.BiomeColor);
        setFlags(Blocks.tripwire_hook, Flag.NotTopBlock);
        setFlags(Blocks.tripwire, Flag.NotTopBlock);
        setFlags(Blocks.unlit_redstone_torch, Flag.HasAir);
        setFlags(Blocks.vine, Flag.NotHideSky, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Blocks.water, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Blocks.wheat, Flag.Side2Texture);
        setFlags(Blocks.web, Flag.NotHideSky, Flag.Side2Texture);
        setFlags(Blocks.yellow_flower, Flag.Side2Texture);

        Iterator<Block> fmlBlockIter = GameData.blockRegistry.iterator();
        while(fmlBlockIter.hasNext()) {
            Block block = fmlBlockIter.next();

            if(block.func_149688_o() == Material.field_151579_a) {
                setFlags(block, Flag.HasAir, Flag.NotHideSky, Flag.NotTopBlock, Flag.NoShadow, Flag.NotCeiling);
                JourneyMap.getLogger().info(GameRegistry.findUniqueIdentifierFor(block) + " flags set to hide block");
                continue;
            }

            if(block instanceof BlockLeavesBase || block instanceof BlockGrass || block instanceof BlockVine || block instanceof BlockLilyPad) {
                setFlags(block, Flag.BiomeColor);
                JourneyMap.getLogger().info(GameRegistry.findUniqueIdentifierFor(block) + " flag set: Flag.BiomeColor");
            }

            if(block instanceof BlockFlower || block instanceof BlockStem || block instanceof BlockBush) {
                setFlags(block, Flag.Side2Texture);
                JourneyMap.getLogger().info(GameRegistry.findUniqueIdentifierFor(block) + " flag set: Flag.Side2Texture");
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
            block = chunkMd.stub.func_150810_a(x, y, z);
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
		
		return y;
	}

    public static EnumSet<Flag> getFlags(GameRegistry.UniqueIdentifier uid)
    {
        EnumSet<Flag> flags = blockFlags.get(uid);
        return flags==null ? EnumSet.noneOf(Flag.class) : flags;
    }

    public static void setFlags(Block block, Flag... flags)
    {
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);
        EnumSet<Flag> eset = getFlags(uid);
        eset.addAll(Arrays.asList(flags));
        blockFlags.put(uid, eset);
    }

    public static boolean hasFlag(Block block, Flag flag)
    {
        EnumSet<Flag> flags = blockFlags.get(GameRegistry.findUniqueIdentifierFor(block));
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasFlag(GameRegistry.UniqueIdentifier uid, Flag flag)
    {
        EnumSet<Flag> flags = blockFlags.get(uid);
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(GameRegistry.findUniqueIdentifierFor(block));
    }

    public static float getAlpha(Block block)
    {
        Float alpha = blockAlphas.get(GameRegistry.findUniqueIdentifierFor(block));
        return alpha==null ? 1F : alpha;
    }

    public static void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(GameRegistry.findUniqueIdentifierFor(block), alpha);
    }

	
}
