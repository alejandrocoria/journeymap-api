package net.techbrew.journeymap.model;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.IPlantable;
import net.techbrew.journeymap.JourneyMap;

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

    public enum Flag
    {
        HasAir, BiomeColor, CustomBiomeColor, MetaBasedColor, NotHideSky, NoShadow, Side2Texture, Transparency, Error, TransparentRoof, Crop, TileEntity
    }

    private final static HashMap<GameRegistry.UniqueIdentifier, EnumSet<Flag>> blockFlags = new HashMap<GameRegistry.UniqueIdentifier, EnumSet<Flag>>(64);
    private final static HashMap<GameRegistry.UniqueIdentifier, Float> blockAlphas = new HashMap<GameRegistry.UniqueIdentifier, Float>(8);

	/**
	 * Constructor
	 */
	public static void initialize() {

        BlockMD.blockUids.clear();
        blockAlphas.clear();

        setAlpha(Blocks.air, 0F);
        setAlpha(Blocks.fence, .4F);
        setAlpha(Blocks.fence_gate, .4F);
        setAlpha(Blocks.flowing_water, .3F);
        setAlpha(Blocks.glass, .3F);
        setAlpha(Blocks.glass_pane, .3F);
        setAlpha(Blocks.ice, .8F);
        setAlpha(Blocks.iron_bars, .4F);
        setAlpha(Blocks.nether_brick_fence, .4F);
        setAlpha(Blocks.stained_glass, .5F);
        setAlpha(Blocks.stained_glass_pane, .5F);
        setAlpha(Blocks.torch, .5F);
        setAlpha(Blocks.vine, .2F);
        setAlpha(Blocks.water, .3F);

        blockFlags.clear();

        if(JourneyMap.getInstance().coreProperties.caveIgnoreGlass.get())
        {
            setFlags(Blocks.glass, Flag.NotHideSky);
            setFlags(Blocks.glass_pane, Flag.NotHideSky);
            setFlags(Blocks.stained_glass, Flag.NotHideSky);
            setFlags(Blocks.stained_glass, Flag.NotHideSky);
        }

        setFlags(Blocks.air, Flag.HasAir, Flag.NotHideSky, Flag.NoShadow, Flag.NotHideSky);
        setFlags(Blocks.fence, Flag.TransparentRoof);
        setFlags(Blocks.fire, Flag.NoShadow, Flag.Side2Texture);
        setFlags(Blocks.flowing_water, Flag.BiomeColor);
        setFlags(Blocks.glass, Flag.TransparentRoof);
        setFlags(Blocks.glass_pane, Flag.TransparentRoof);
        setFlags(Blocks.grass, Flag.BiomeColor);
        setFlags(Blocks.iron_bars, Flag.TransparentRoof);
        setFlags(Blocks.ladder, Flag.NotHideSky);
        setFlags(Blocks.lava, Flag.NoShadow);
        setFlags(Blocks.leaves, Flag.NotHideSky, Flag.BiomeColor);
        setFlags(Blocks.leaves2, Flag.NotHideSky, Flag.BiomeColor);
        setFlags(Blocks.redstone_torch, Flag.HasAir);
        setFlags(Blocks.stained_glass, Flag.TransparentRoof, Flag.Transparency);
        setFlags(Blocks.stained_glass_pane, Flag.TransparentRoof, Flag.Transparency);
        setFlags(Blocks.tallgrass, Flag.BiomeColor);
        setFlags(Blocks.torch, Flag.HasAir, Flag.NoShadow);
        setFlags(Blocks.tripwire, Flag.NoShadow);
        setFlags(Blocks.tripwire_hook, Flag.NoShadow);
        setFlags(Blocks.unlit_redstone_torch, Flag.HasAir, Flag.NoShadow);
        setFlags(Blocks.vine, Flag.NotHideSky, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Blocks.water, Flag.NoShadow, Flag.BiomeColor);
        setFlags(Blocks.web, Flag.NotHideSky, Flag.Side2Texture);

        FMLControlledNamespacedRegistry<Block> blockRegistry = GameData.getBlockRegistry();
        Iterator<Block> fmlBlockIter = blockRegistry.iterator();
        while(fmlBlockIter.hasNext())
        {
            Block block = fmlBlockIter.next();
            BlockMD.blockUids.put(block, new GameRegistry.UniqueIdentifier(blockRegistry.getNameForObject(block)));

            if(block.getMaterial() == Material.air) {
                setFlags(block, Flag.HasAir, Flag.NotHideSky, Flag.NoShadow);
                JourneyMap.getLogger().fine(BlockMD.findUniqueIdentifierFor(block) + " flags set to hide block");
                continue;
            }

            if(block instanceof BlockLeavesBase || block instanceof BlockGrass || block instanceof BlockTallGrass ||block instanceof BlockVine || block instanceof BlockLilyPad) {
                setFlags(block, Flag.BiomeColor);
                JourneyMap.getLogger().fine(BlockMD.findUniqueIdentifierFor(block) + " flag set: Flag.BiomeColor");
            }

            if(block instanceof IPlantable) {
                setFlags(block, Flag.Side2Texture, Flag.NoShadow);
                JourneyMap.getLogger().fine(BlockMD.findUniqueIdentifierFor(block) + " flags set: Flag.Side2Texture, Flag.NoShadow");
            }

            if(block instanceof BlockStem) {
                setFlags(block, Flag.MetaBasedColor, Flag.NoShadow);
                JourneyMap.getLogger().fine(BlockMD.findUniqueIdentifierFor(block) + " flags set: Flag.MetaBasedColor, Flag.NoShadow");
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

        int checkY = y;
        final int maxY = chunkMd.stub.getHeightValue(x, z);
        if(checkY>maxY)
        {
            return true;
        }

        boolean seeSky = chunkMd.stub.canBlockSeeTheSky(x, checkY, z);
        if(!seeSky) {
            seeSky = true;
            Block block;


            while(seeSky && checkY<=maxY) {
                block = chunkMd.getBlock(x, checkY, z);
                if(block==null || hasFlag(block, Flag.HasAir))
                {
                    checkY++;
                }
                else if(hasFlag(block, Flag.NotHideSky))
                {
                    checkY++;
                }
                else
                {
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
        int y = topY;

        try
        {
            Block block;
            while(y>=0) {
                block = chunkMd.getBlock(x, y, z);
                if(block==null) {
                    y--;
                } else if(chunkMd.stub.canBlockSeeTheSky(x, y, z)) {
                    y--;
                } else if(hasFlag(block, Flag.NotHideSky)) {
                    y--;
                } else {
                    break;
                }
            }

            return Math.max(0,y);
        } catch(Exception e) {
            JourneyMap.getLogger().fine(e + " at " + x + "," + y + "," + z);
            return Math.max(0, topY);
        }
    }

    public static EnumSet<Flag> getFlags(GameRegistry.UniqueIdentifier uid)
    {
        EnumSet<Flag> flags = blockFlags.get(uid);
        return flags==null ? EnumSet.noneOf(Flag.class) : flags;
    }

    public static void setFlags(Block block, Flag... flags)
    {
        GameRegistry.UniqueIdentifier uid = BlockMD.findUniqueIdentifierFor(block);
        EnumSet<Flag> eset = getFlags(uid);
        eset.addAll(Arrays.asList(flags));
        blockFlags.put(uid, eset);
    }

    public static boolean hasFlag(Block block, Flag flag)
    {
        EnumSet<Flag> flags = blockFlags.get(BlockMD.findUniqueIdentifierFor(block));
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasAnyFlags(Block block, Flag... flags)
    {
        EnumSet<Flag> flagSet = blockFlags.get(BlockMD.findUniqueIdentifierFor(block));
        if(flagSet==null) return false;
        for(Flag flag : flags) {
            if(flagSet.contains(flag)){
                return true;
            }
        }
        return false;
    }

    public static boolean hasFlag(GameRegistry.UniqueIdentifier uid, Flag flag)
    {
        EnumSet<Flag> flags = blockFlags.get(uid);
        return flags!=null && flags.contains(flag);
    }

    public static boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(BlockMD.findUniqueIdentifierFor(block));
    }

    public static float getAlpha(Block block)
    {
        Float alpha = blockAlphas.get(BlockMD.findUniqueIdentifierFor(block));
        return alpha==null ? 1F : alpha;
    }

    public static void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(BlockMD.findUniqueIdentifierFor(block), alpha);
    }

    public static HashMap getFlagsMap()
    {
        return blockFlags;
    }

    public static HashMap getAlphaMap()
    {
        return blockAlphas;
    }
}
