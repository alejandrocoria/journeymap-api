package journeymap.client.model.mod.vanilla;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import journeymap.client.JourneymapClient;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.common.Journeymap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Common handler works with vanilla blocks and mod blocks that inherit from them in a normal way.
 */
public final class VanillaBlockHandler implements ModBlockDelegate.IModBlockHandler
{
    ListMultimap<Material, BlockMD.Flag> materialFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
    ListMultimap<Class<? extends Block>, BlockMD.Flag> blockClassFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
    ListMultimap<Block, BlockMD.Flag> blockFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
    HashMap<Material, Float> materialAlphas = new HashMap<Material, Float>();
    HashMap<Block, Float> blockAlphas = new HashMap<Block, Float>();
    HashMap<Class<? extends Block>, Float> blockClassAlphas = new HashMap<Class<? extends Block>, Float>();
    HashMap<Block, Integer> blockTextureSides = new HashMap<Block, Integer>();
    HashMap<Class<? extends Block>, Integer> blockClassTextureSides = new HashMap<Class<? extends Block>, Integer>();

    public VanillaBlockHandler()
    {
        preInitialize();
    }

    private void preInitialize()
    {
        // Init flags and alphas to be set according to a Block's material.
        setFlags(Material.BARRIER, HasAir, OpenToSky, NoShadow);
        setFlags(Material.AIR, HasAir, OpenToSky, NoShadow);
        setFlags(Material.GLASS, .4F, TransparentRoof);
        if (JourneymapClient.getCoreProperties().caveIgnoreGlass.get())
        {
            setFlags(Material.GLASS, OpenToSky);
        }
        setFlags(Material.WATER, .3F, NoShadow, Water);
        setFlags(Material.LAVA, NoShadow);
        materialAlphas.put(Material.ICE, .8F);
        materialAlphas.put(Material.PACKED_ICE, .8F);

        // Init flags and alphas on specific Block instances
        setFlags(Blocks.IRON_BARS, .4F);
        setFlags(Blocks.FIRE, NoShadow);
        setTextureSide(Blocks.FIRE, 2);
        setFlags(Blocks.IRON_BARS, TransparentRoof);
        setFlags(Blocks.LADDER, OpenToSky);
        setFlags(Blocks.SNOW_LAYER, NoTopo);
        setFlags(Blocks.TRIPWIRE, NoShadow);
        setFlags(Blocks.TRIPWIRE_HOOK, NoShadow);
        setFlags(Blocks.WEB, OpenToSky);
        setTextureSide(Blocks.WEB, 2);

        // Init flags and alphas to be set according to a Block's parent class
        setFlags(BlockLog.class, OpenToSky, CustomBiomeColor, NoTopo);
        setFlags(BlockFence.class, .4F, TransparentRoof);
        setFlags(BlockFenceGate.class, .4F, TransparentRoof);
        setFlags(BlockGrass.class, Grass);
        setFlags(BlockTallGrass.class, HasAir, NoTopo);
        setFlags(BlockDoublePlant.class, Plant, NoTopo);
        setTextureSide(BlockDoublePlant.class, 2);
        setFlags(BlockLeaves.class, OpenToSky, Foliage, NoTopo);
        setFlags(BlockVine.class, .2F, OpenToSky, CustomBiomeColor, Foliage, NoTopo, NoShadow);
        setFlags(BlockLilyPad.class, CustomBiomeColor, NoTopo);
        setFlags(BlockCrops.class, Crop, NoTopo);
        setTextureSide(BlockCrops.class, 2);
        setFlags(BlockFlower.class, Plant, NoTopo);
        setFlags(BlockBush.class, Plant, NoTopo);
        setTextureSide(BlockBush.class, 2);
        setFlags(BlockCactus.class, Plant, NoTopo);
        setTextureSide(BlockCactus.class, 2);
        setFlags(BlockRailBase.class, NoShadow, NoTopo);
        setFlags(BlockTorch.class, HasAir, NoShadow, NoTopo);
    }

    /**
     * Set flags, alpha, etc. for a BlockMD
     */
    @Override
    public boolean initialize(BlockMD blockMD)
    {
        // Set vanilla color handler
        blockMD.setBlockColorHandler(VanillaColorHandler.INSTANCE);

        if (blockMD.isWater())
        {
            Journeymap.getLogger().info("Water! " + blockMD);
        }

        // Set flags based on material
        Block block = blockMD.getBlock();
        IBlockState blockState = block.getStateFromMeta(blockMD.getMeta());
        Material material = block.getMaterial(blockState);
        blockMD.addFlags(materialFlags.get(material));

        // Set alpha based on material
        Float alpha = materialAlphas.get(material);
        if (alpha != null)
        {
            blockMD.setAlpha(alpha);
        }

        // Set flags based on exact block
        if (blockFlags.containsKey(block))
        {
            blockMD.addFlags(blockFlags.get(block));
        }

        // Set alpha based on exact block
        alpha = blockAlphas.get(block);
        if (alpha != null)
        {
            blockMD.setAlpha(alpha);
        }

        // Add flags based on block class inheritance
        if (blockMD.getFlags().isEmpty())
        {
            for (Class<? extends Block> parentClass : blockClassFlags.keys())
            {
                if (parentClass.isAssignableFrom(block.getClass()))
                {
                    blockMD.addFlags(blockClassFlags.get(parentClass));
                    alpha = blockClassAlphas.get(parentClass);
                    if (alpha != null)
                    {
                        blockMD.setAlpha(alpha);
                    }

                    Integer textureSide = blockClassTextureSides.get(parentClass);
                    if (textureSide != null)
                    {
                        blockMD.setTextureSide(textureSide);
                    }

                    break;
                }
            }
        }

        // Below are the oddball blocks that need extra help

        // 1.7.10
        // TODO BlockHugeMushroom

        // 1.8
        if (block instanceof BlockHugeMushroom)
        {
            // 1.8 : 14 gets "all_outside" texture
            int overrideMeta = block.getMetaFromState(block.getDefaultState());
            if (blockMD.getMeta() != overrideMeta)
            {
                blockMD.setOverrideMeta(overrideMeta);
            }
        }

        // Double-tall grass should be treated like BlockTallGrass:  ignored
        if (block == Blocks.DOUBLE_PLANT && blockMD.getMeta() == 2)
        {
            blockMD.addFlags(HasAir, NoTopo);
        }

        // Ferns unlike other BlockTallGrass will be treated like plants
        if (block == Blocks.TALLGRASS && blockMD.getMeta() == 2)
        {
            blockMD.addFlags(Plant, CustomBiomeColor);
        }

        // Toggle plant shadows
        if (blockMD.hasAnyFlag(BlockMD.FlagsPlantAndCrop) && !JourneymapClient.getCoreProperties().mapPlantShadows.get())
        {
            blockMD.addFlags(NoShadow);
        }

        return false;
    }

    @Override
    public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
    {
        // Should never be called
        return blockMD;
    }

    private void setFlags(Material material, BlockMD.Flag... flags)
    {
        materialFlags.putAll(material, new ArrayList<BlockMD.Flag>(Arrays.asList(flags)));
    }

    private void setFlags(Material material, Float alpha, BlockMD.Flag... flags)
    {
        materialAlphas.put(material, alpha);
        setFlags(material, flags);
    }

    private void setFlags(Class<? extends Block> parentClass, BlockMD.Flag... flags)
    {
        blockClassFlags.putAll(parentClass, new ArrayList<BlockMD.Flag>(Arrays.asList(flags)));
    }

    private void setFlags(Class<? extends Block> parentClass, Float alpha, BlockMD.Flag... flags)
    {
        blockClassAlphas.put(parentClass, alpha);
        setFlags(parentClass, flags);
    }

    private void setFlags(Block block, BlockMD.Flag... flags)
    {
        blockFlags.putAll(block, new ArrayList<BlockMD.Flag>(Arrays.asList(flags)));
    }

    private void setFlags(Block block, Float alpha, BlockMD.Flag... flags)
    {
        blockAlphas.put(block, alpha);
        setFlags(block, flags);
    }

    private void setTextureSide(Class<? extends Block> parentClass, int side)
    {
        blockClassTextureSides.put(parentClass, side);
    }

    private void setTextureSide(Block block, int side)
    {
        blockTextureSides.put(block, side);
    }
}
