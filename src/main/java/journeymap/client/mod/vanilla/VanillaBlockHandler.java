/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod.vanilla;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import journeymap.client.mod.IModBlockHandler;
import journeymap.client.model.BlockFlag;
import journeymap.client.model.BlockMD;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static journeymap.client.model.BlockFlag.*;

/**
 * Common handler works with vanilla blocks and mod blocks that inherit from them in a normal way.
 */
public final class VanillaBlockHandler implements IModBlockHandler {
    /**
     * The Material flags.
     */
    ListMultimap<Material, BlockFlag> materialFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
    /**
     * The Block class flags.
     */
    ListMultimap<Class<?>, BlockFlag> blockClassFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
    /**
     * The Block flags.
     */
    ListMultimap<Block, BlockFlag> blockFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
    /**
     * The Material alphas.
     */
    HashMap<Material, Float> materialAlphas = new HashMap<Material, Float>();
    /**
     * The Block alphas.
     */
    HashMap<Block, Float> blockAlphas = new HashMap<>();
    /**
     * The Block class alphas.
     */
    HashMap<Class<?>, Float> blockClassAlphas = new HashMap<>();

    private boolean mapPlants;
    private boolean mapPlantShadows;
    private boolean mapCrops;

    /**
     * Instantiates a new Vanilla block handler.
     */
    public VanillaBlockHandler() {
        preInitialize();
    }

    private void preInitialize() {
        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        mapPlants = coreProperties.mapPlants.get();
        mapCrops = coreProperties.mapCrops.get();
        mapPlantShadows = coreProperties.mapPlantShadows.get();

        // Init flags and alphas to be set according to a Block's material.
        setFlags(Material.BARRIER, Ignore);
        setFlags(Material.AIR, Ignore);
        setFlags(Material.GLASS, .4F, Transparency);
        setFlags(Material.GRASS, Grass);
        if (coreProperties.caveIgnoreGlass.get()) {
            setFlags(Material.GLASS, OpenToSky);
        }
        setFlags(Material.LAVA, 1.0F, NoShadow);
        setFlags(Material.WATER, .25F, Water, NoShadow);
        materialAlphas.put(Material.ICE, .8F);
        materialAlphas.put(Material.PACKED_ICE, .8F);

        // Init flags and alphas on specific Block instances
        setFlags(Blocks.IRON_BARS, .4F, Transparency);
        setFlags(Blocks.FIRE, NoShadow);
        setFlags(Blocks.LADDER, OpenToSky);
        setFlags(Blocks.SNOW_LAYER, NoTopo, NoShadow);
        setFlags(Blocks.TRIPWIRE, Ignore);
        setFlags(Blocks.TRIPWIRE_HOOK, Ignore);
        setFlags(Blocks.WEB, OpenToSky, NoShadow);

        // Init flags and alphas to be set according to a Block's parent class
        setFlags(BlockBush.class, Plant);
        setFlags(BlockFence.class, .4F, Transparency);
        setFlags(BlockFenceGate.class, .4F, Transparency);
        setFlags(BlockGrass.class, Grass);
        setFlags(BlockLeaves.class, OpenToSky, Foliage, NoTopo);
        setFlags(BlockLog.class, OpenToSky, NoTopo);
        setFlags(BlockRailBase.class, NoShadow, NoTopo);
        setFlags(BlockRedstoneWire.class, Ignore);
        setFlags(BlockTorch.class, Ignore);
        setFlags(BlockVine.class, .2F, OpenToSky, Foliage, NoShadow);
        setFlags(IPlantable.class, Plant, NoTopo);
    }

    /**
     * Set flags, alpha, etc. for a BlockMD
     */
    @Override
    public void initialize(BlockMD blockMD) {
        Block block = blockMD.getBlockState().getBlock();
        Material material = blockMD.getBlockState().getMaterial();
        IBlockState blockState = blockMD.getBlockState();

        // Ignore invisible blocks
        if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
            blockMD.addFlags(Ignore);
            return;
        }

        // Set flags based on material
        blockMD.addFlags(materialFlags.get(material));

        // Set alpha based on material
        Float alpha = materialAlphas.get(material);
        if (alpha != null) {
            blockMD.setAlpha(alpha);
        }

        // Set flags based on exact block
        if (blockFlags.containsKey(block)) {
            blockMD.addFlags(blockFlags.get(block));
        }

        // Set alpha based on exact block
        alpha = blockAlphas.get(block);
        if (alpha != null) {
            blockMD.setAlpha(alpha);
        }

        // Add flags based on block class inheritance
        for (Class<?> parentClass : blockClassFlags.keys()) {
            if (parentClass.isAssignableFrom(block.getClass())) {
                blockMD.addFlags(blockClassFlags.get(parentClass));
                alpha = blockClassAlphas.get(parentClass);
                if (alpha != null) {
                    blockMD.setAlpha(alpha);
                }
                break;
            }
        }

        // Fluids
        if (block instanceof IFluidBlock) {
            blockMD.addFlags(Fluid, NoShadow);
            blockMD.setAlpha(.7F);
        }

        // If material is glass, but not actually transparent, then remove the flags used on glass.
        if (material == Material.GLASS)
        {
            if (block instanceof BlockGlowstone || block instanceof BlockSeaLantern || block instanceof BlockBeacon)
            {
                blockMD.removeFlags(OpenToSky, Transparency);
                blockMD.setAlpha(1F);
            }

        }

        // Double-tall plants will have the upper half ignored
        if (block instanceof BlockBush && blockMD.getBlockState().getProperties().get(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            blockMD.addFlags(Ignore);
        }

        // Crops are special
        if (block instanceof BlockCrops) {
            blockMD.addFlags(Crop);
        }

        if (block instanceof BlockFlower || block instanceof BlockFlowerPot) {
            blockMD.setBlockColorProxy(FlowerBlockProxy.INSTANCE);
        }

        if (blockMD.isVanillaBlock()) {
            return;
        }

        // Mod Torches
        String uid = blockMD.getBlockId();
        if (uid.toLowerCase().contains("torch")) {
            blockMD.addFlags(Ignore);
            return;
        }
    }

    /**
     * Clean up flags as needed.
     *
     * @param blockMD
     */
    public void postInitialize(BlockMD blockMD) {
        // Being both Plant and Crop isn't necessary
        if (blockMD.hasFlag(Crop)) {
            blockMD.removeFlags(Plant);
        }

        // Ignore plants and crops if necessary, set shadows.
        if (blockMD.hasAnyFlag(BlockMD.FlagsPlantAndCrop)) {
            if (!mapPlants && blockMD.hasFlag(Plant) || !mapCrops && blockMD.hasFlag(Crop)) {
                blockMD.addFlags(Ignore);
            } else if (!mapPlantShadows) {
                blockMD.addFlags(NoShadow);
            }
        }

        // If a block is Ignored, clear all other flags (except error)
        if (blockMD.isIgnore()) {
            blockMD.removeFlags(BlockMD.FlagsNormal);
        }
    }

    private void setFlags(Material material, BlockFlag... flags) {
        materialFlags.putAll(material, new ArrayList<>(Arrays.asList(flags)));
    }

    private void setFlags(Material material, Float alpha, BlockFlag... flags) {
        materialAlphas.put(material, alpha);
        setFlags(material, flags);
    }

    private void setFlags(Class parentClass, BlockFlag... flags) {
        blockClassFlags.putAll(parentClass, new ArrayList<>(Arrays.asList(flags)));
    }

    private void setFlags(Class parentClass, Float alpha, BlockFlag... flags) {
        blockClassAlphas.put(parentClass, alpha);
        setFlags(parentClass, flags);
    }

    private void setFlags(Block block, BlockFlag... flags) {
        blockFlags.putAll(block, new ArrayList<>(Arrays.asList(flags)));
    }

    private void setFlags(Block block, Float alpha, BlockFlag... flags) {
        blockAlphas.put(block, alpha);
        setFlags(block, flags);
    }
}
