package net.techbrew.journeymap.model;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IPlantable;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.properties.CoreProperties;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

import static net.techbrew.journeymap.model.BlockMD.Flag.*;

/**
 * Created by Mark on 7/14/2014.
 */
public class BlockMDCache extends CacheLoader<String, BlockMD>
{
    CoreProperties coreProperties = JourneyMap.getInstance().coreProperties;

    public final static BlockMD AIRBLOCK = new BlockMD("Air", Blocks.air, 0, 0f, BlockMD.Flag.HasAir);
    public final static BlockMD VOIDBLOCK = new BlockMD("Void", null, 0, 1f);

    private final static HashMap<GameRegistry.UniqueIdentifier, EnumSet<BlockMD.Flag>> blockFlags = new HashMap<GameRegistry.UniqueIdentifier, EnumSet<BlockMD.Flag>>(64);
    private final static HashMap<GameRegistry.UniqueIdentifier, Float> blockAlphas = new HashMap<GameRegistry.UniqueIdentifier, Float>(8);

    public BlockMDCache()
    {
        initialize();
    }

    /**
     * Constructor
     */
    public void initialize()
    {
        StatTimer timer = StatTimer.get("BlockMDCache.initialize").start();

        blockAlphas.clear();
        blockFlags.clear();

        // Set alphas
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

        // Set optional flags
        if (JourneyMap.getInstance().coreProperties.caveIgnoreGlass.get())
        {
            setFlags(Blocks.glass, OpenToSky);
            setFlags(Blocks.glass_pane, OpenToSky);
            setFlags(Blocks.stained_glass, OpenToSky);
            setFlags(Blocks.stained_glass, OpenToSky);
        }

        // Set manual flags
        setFlags(Blocks.air, HasAir, OpenToSky, NoShadow, OpenToSky);
        setFlags(Blocks.double_plant, BiomeColor);
        setFlags(Blocks.fence, TransparentRoof);
        setFlags(Blocks.fire, NoShadow, Side2Texture);
        setFlags(Blocks.flowing_water, BiomeColor);
        setFlags(Blocks.glass, TransparentRoof);
        setFlags(Blocks.glass_pane, TransparentRoof);
        setFlags(Blocks.grass, BiomeColor);
        setFlags(Blocks.iron_bars, TransparentRoof);
        setFlags(Blocks.ladder, OpenToSky);
        setFlags(Blocks.lava, NoShadow);
        setFlags(Blocks.leaves, OpenToSky, BiomeColor);
        setFlags(Blocks.leaves2, OpenToSky, BiomeColor);
        setFlags(Blocks.redstone_torch, HasAir);
        setFlags(Blocks.stained_glass, TransparentRoof, Transparency);
        setFlags(Blocks.stained_glass_pane, TransparentRoof, Transparency);
        setFlags(Blocks.tallgrass, BiomeColor);
        setFlags(Blocks.torch, HasAir, NoShadow);
        setFlags(Blocks.tripwire, NoShadow);
        setFlags(Blocks.tripwire_hook, NoShadow);
        setFlags(Blocks.unlit_redstone_torch, HasAir, NoShadow);
        setFlags(Blocks.vine, OpenToSky, NoShadow, BiomeColor);
        setFlags(Blocks.water, NoShadow, BiomeColor);
        setFlags(Blocks.web, OpenToSky, Side2Texture);

        // Set flags based on inheritance
        for (Block block : GameData.getBlockRegistry().typeSafeIterable())
        {
            if (block.getMaterial() == Material.air)
            {
                setFlags(block, HasAir, OpenToSky, NoShadow);
                continue;
            }

            if (block instanceof BlockLeavesBase || block instanceof BlockGrass || block instanceof BlockVine || block instanceof BlockLilyPad)
            {
                setFlags(block, BiomeColor);
            }

            if (block instanceof IPlantable)
            {

                setFlags(block, Side2Texture);
                if (!coreProperties.mapPlantShadows.get())
                {
                    setFlags(block, NoShadow);
                }
            }

            if (block instanceof BlockRailBase)
            {
                setFlags(block, NoShadow);
            }
        }

        timer.stop();
    }


    @Override
    public BlockMD load(String key) throws Exception
    {
        String modId = null;
        String name = null;
        Integer meta = null;

        try
        {
            // Instead of split, defensively use first/last positions
            // in case some schlub puts a colon in the block name
            int firstColon = key.indexOf(':');
            int lastColon = key.lastIndexOf(':');
            modId = key.substring(0, firstColon);
            name = key.substring(firstColon+1, lastColon);
            meta = Integer.valueOf(key.substring(lastColon+1));
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning(String.format("Bad key %s because %s, returning AIR", key, e));
            return AIRBLOCK;
        }

        // Find the block
        Block block = GameRegistry.findBlock(modId, name);
        if(block==null)
        {
            block = GameRegistry.findBlock(null, name);
            if(block==null)
            {
                if(name.equals("Air")) {
                    return AIRBLOCK;
                }
                else
                {
                    JourneyMap.getLogger().warning(String.format("Block not found for key %s, returning Error Block", key));
                    return new BlockMD("ERROR-" + block.getClass().getName(), Blocks.air, meta, 0f, BlockMD.Flag.Error, BlockMD.Flag.HasAir);
                }
            }
        }

        // Get the UID
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);

        // Find the display name
        try {
            // Gotta love this.  TODO: Is there a better way?
            Item item = Item.getItemFromBlock(block);
            ItemStack stack = new ItemStack(item, 1, block.damageDropped(meta));
            name = stack.getDisplayName();
        } catch(Throwable t) {
            JourneyMap.getLogger().fine("Displayname not available for " + name);
        }

        String prefix = "minecraft".equals(modId) ? "" : (modId + ":");
        String displayName = String.format("%s%s:%s", prefix, name, meta);

        BlockMD blockMD = new BlockMD(displayName, block, meta, getAlpha(block), getFlags(uid));

        return blockMD;
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     */
    public BlockMD getBlockMD(LoadingCache<String, BlockMD> cache, ChunkMD chunkMd, int x, int y, int z)
    {
        Block block;
        int meta;
        boolean isAir;

        try
        {
            if (y >= 0)
            {
                block = chunkMd.getBlock(x, y, z);

                if(block instanceof BlockAir)
                {
                    return BlockMDCache.AIRBLOCK;
                }
                else
                {
                    meta = chunkMd.stub.getBlockMetadata(x, y, z);
                    String key = toCacheKeyString(block, meta);
                    return cache.get(key);
                }
            }
            else
            {
                return BlockMDCache.VOIDBLOCK;
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't get blockId/meta for chunk %s,%s block %s,%s,%s : %s", chunkMd.stub.xPosition, chunkMd.stub.zPosition, x ,y ,z, LogFormatter.toString(e)));
            return BlockMDCache.AIRBLOCK;
        }
    }

    public static String toCacheKeyString(Block block, int meta)
    {
        return toCacheKeyString(GameRegistry.findUniqueIdentifierFor(block), meta);
    }

    public static String toCacheKeyString(GameRegistry.UniqueIdentifier uid, int meta)
    {
        return String.format("%s:%s:%s", uid.modId, uid.name, meta);
    }

    public EnumSet<BlockMD.Flag> getFlags(GameRegistry.UniqueIdentifier uid)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(uid);
        return flags==null ? EnumSet.noneOf(BlockMD.Flag.class) : flags;
    }

    public void setFlags(Block block, BlockMD.Flag... flags)
    {
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);
        EnumSet<BlockMD.Flag> eset = getFlags(uid);
        eset.addAll(Arrays.asList(flags));
        blockFlags.put(uid, eset);
        JourneyMap.getLogger().fine(uid + " flags set: " + eset);
    }

    public boolean hasFlag(Block block, BlockMD.Flag flag)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(GameRegistry.findUniqueIdentifierFor(block));
        return flags!=null && flags.contains(flag);
    }

    public boolean hasAnyFlags(Block block, BlockMD.Flag... flags)
    {
        EnumSet<BlockMD.Flag> flagSet = blockFlags.get(GameRegistry.findUniqueIdentifierFor(block));
        if(flagSet==null) return false;
        for(BlockMD.Flag flag : flags) {
            if(flagSet.contains(flag)){
                return true;
            }
        }
        return false;
    }

    public boolean hasFlag(GameRegistry.UniqueIdentifier uid, BlockMD.Flag flag)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(uid);
        return flags!=null && flags.contains(flag);
    }

    public boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(GameRegistry.findUniqueIdentifierFor(block));
    }

    public float getAlpha(Block block)
    {
        Float alpha = blockAlphas.get(GameRegistry.findUniqueIdentifierFor(block));
        return alpha==null ? 1F : alpha;
    }

    public void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(GameRegistry.findUniqueIdentifierFor(block), alpha);
    }

    public HashMap getFlagsMap()
    {
        return blockFlags;
    }

    public HashMap getAlphaMap()
    {
        return blockAlphas;
    }
}
