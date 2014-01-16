package net.techbrew.mcjm.cartography;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.render.BlockInfo;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;

public class MapBlocks extends HashMap {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);

    public enum Flag {HasAir, BiomeColor, IgnoreOverhead, NotTopBlock, NoShadow, Side2Texture}

    private final static HashMap<GameRegistry.UniqueIdentifier, EnumSet<Flag>> blockFlags = new HashMap<GameRegistry.UniqueIdentifier, EnumSet<Flag>>(64);
    private final static HashMap<GameRegistry.UniqueIdentifier, Float> blockAlphas = new HashMap<GameRegistry.UniqueIdentifier, Float>(8);

	final ColorCache colorCache = ColorCache.getInstance();
	
	/**
	 * Constructor
	 */
	public MapBlocks() {

        blockAlphas.clear();
        setAlpha(Blocks.ice, .8F);
        setAlpha(Blocks.glass, .3F);
        setAlpha(Blocks.glass_pane, .3F);
        setAlpha(Blocks.vine, .2F);
        setAlpha(Blocks.torch, .5F);

        blockFlags.clear();
        setFlags(Blocks.air, Flag.HasAir, Flag.IgnoreOverhead, Flag.NotTopBlock, Flag.NoShadow);
        setFlags(Blocks.brown_mushroom, Flag.Side2Texture);
        setFlags(Blocks.carrots, Flag.Side2Texture);
        setFlags(Blocks.deadbush, Flag.NotTopBlock, Flag.Side2Texture);
        setFlags(Blocks.fire, Flag.NoShadow, Flag.Side2Texture);
        setFlags(Blocks.flowing_water, Flag.BiomeColor);
        setFlags(Blocks.grass, Flag.BiomeColor);
        setFlags(Blocks.glass, Flag.NotTopBlock, Flag.NoShadow);
        setFlags(Blocks.glass_pane, Flag.NotTopBlock, Flag.NoShadow);
        setFlags(Blocks.ladder, Flag.IgnoreOverhead);
        setFlags(Blocks.lava, Flag.NoShadow);
        setFlags(Blocks.leaves, Flag.IgnoreOverhead, Flag.BiomeColor);
        setFlags(Blocks.leaves2, Flag.IgnoreOverhead, Flag.BiomeColor);
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
        setFlags(Blocks.vine, Flag.IgnoreOverhead, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Blocks.water, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Blocks.wheat, Flag.Side2Texture);
        setFlags(Blocks.web, Flag.IgnoreOverhead, Flag.Side2Texture);
        setFlags(Blocks.yellow_flower, Flag.Side2Texture);
    }
	
	/**
	 * Returns a simple wrapper object of the blockId and the block meta values.
	 * @param chunkMd
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	BlockInfo getBlockInfo(ChunkMD chunkMd, int x, int y, int z) {
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
			BlockInfo info = new BlockInfo(block, meta);
			if(!isAir) {
				info.setColor(colorCache.getBlockColor(chunkMd, info, x, y, z));
				Float alpha = getAlpha(block);
				info.setAlpha(alpha==null ? 1F : alpha);
			} else {
				info.setColor(Color.black);
				info.setAlpha(0);
			}
			
			return info;
			
		} catch (Exception e) {
			JourneyMap.getLogger().severe("Can't get blockId/meta for chunk " + chunkMd.stub.xPosition + "," + chunkMd.stub.zPosition + " block " + x + "," + y + "," + z); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			JourneyMap.getLogger().severe(LogFormatter.toString(e));
			return null;
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
                block = chunkMd.stub.func_150810_a(x, y, z);
				if(hasFlag(block, Flag.IgnoreOverhead)) {
					checkY++;
				} else {
					seeSky = false;
					break;
				}
			}
			if(seeSky==true) {
				//JourneyMap.getLogger().info("Can see sky at " + x + "," + y + "," + z);
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
			} else if(hasFlag(block, Flag.NotTopBlock)) {
				y--;
			} else {
				break;
			}
		}
		
		return y;
	}

    public static void setFlags(Block block, Flag... flags)
    {
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);
        EnumSet<Flag> eset = blockFlags.get(uid);
        if(eset==null) {
            eset = EnumSet.noneOf(Flag.class);
        }
        eset.addAll(Arrays.asList(flags));
        blockFlags.put(uid, eset);
    }

    public static boolean hasFlag(Block block, Flag flag)
    {
        EnumSet<Flag> flags = blockFlags.get(GameRegistry.findUniqueIdentifierFor(block));
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(GameRegistry.findUniqueIdentifierFor(block));
    }

    public static Float getAlpha(Block block)
    {
        return blockAlphas.get(GameRegistry.findUniqueIdentifierFor(block));
    }

    public static void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(GameRegistry.findUniqueIdentifierFor(block), alpha);
    }

	
}
