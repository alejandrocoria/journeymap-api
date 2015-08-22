/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import journeymap.client.Constants;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.forge.helper.IForgeHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.*;

/**
 * Cache of block baseColors derived from the current texture pack.
 *
 * @author mwoodman
 */
public class ColorCache
{
    private final HashMap<BlockMD, Integer> baseColors = new HashMap<BlockMD, Integer>(256);
    private final IForgeHelper forgeHelper = ForgeHelper.INSTANCE;
    private volatile IColorHelper colorHelper;
    private volatile ColorPalette currentPalette;
    private String lastResourcePackNames;
    private String lastModNames;

    private ColorCache()
    {
    }

    public static ColorCache instance()
    {
        return Holder.INSTANCE;
    }

    /**
     * Ensure the colors in the cache match the current resource packs.
     */
    public void ensureCurrent()
    {
        if (forgeHelper.getClient().theWorld == null)
        {
            // Can happen when resource packs are changed after quitting out of world.
            // This ensures on next world load
            reset();
        }
        else
        {
            String currentResourcePackNames = Constants.getResourcePackNames();
            String currentModNames = Constants.getModNames();

            boolean resourcePackSame = false;
            boolean modPackSame = false;

            if (currentResourcePackNames.equals(lastResourcePackNames) && colorHelper != null)
            {
                Journeymap.getLogger().debug("Resource Pack(s) unchanged: " + currentResourcePackNames);
                resourcePackSame = true;
            }

            if (currentModNames.equals(lastModNames))
            {
                Journeymap.getLogger().debug("Mod Pack(s) unchanged: " + currentModNames);
                modPackSame = true;
            }

            // TODO: Don't check in
            //if (!resourcePackSame || !modPackSame)
            {
                reset();

                lastResourcePackNames = currentResourcePackNames;
                lastModNames = currentModNames;

                // Make sure block metadata is reset
                DataCache.instance().resetBlockMetadata();

                // Load the cache from a color palette
                Journeymap.getLogger().info(String.format("Getting color palette for Resource Pack(s): %s", currentResourcePackNames));
                loadColorPalette();

                // Remap around player
                MapPlayerTask.forceNearbyRemap();
            }
        }
    }

    public ColorPalette getCurrentPalette()
    {
        return currentPalette;
    }

    private void initCacheFromPalette(ColorPalette colorPalette)
    {
        try
        {
            long start = System.currentTimeMillis();
            if (colorPalette != null)
            {
                this.baseColors.putAll(colorPalette.getBasicColorMap());
                long elapsed = System.currentTimeMillis() - start;
                Journeymap.getLogger().info(String.format("Existing color palette loaded %d colors in %dms from file: %s", colorPalette.size(), elapsed, colorPalette.getOrigin()));
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Could not load color palette: " + e);
        }
    }

    private void loadColorPalette()
    {
        ColorPalette palette = ColorPalette.getActiveColorPalette();
        if (palette != null)
        {
            initCacheFromPalette(palette);
        }
        else
        {
            palette = generateColorPalette(true, false);
        }

        this.currentPalette = palette;
    }

    private ColorPalette generateColorPalette(boolean standard, boolean permanent)
    {
        long start = System.currentTimeMillis();
        //prefetchResourcePackColors();
        ColorPalette palette = null;
        try
        {
            String resourcePackNames = Constants.getResourcePackNames();
            String modPackNames = Constants.getModNames();
            palette = new ColorPalette(resourcePackNames, modPackNames, baseColors);
            palette.setPermanent(permanent);
            palette.writeToFile(standard);
            long elapsed = System.currentTimeMillis() - start;
            Journeymap.getLogger().info(String.format("New color palette generated with %d colors in %dms for: %s", palette.size(), elapsed, palette.getOrigin()));
            return palette;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Couldn't create ColorPalette: " + LogFormatter.toString(e));
        }
        return null;
    }

    // Force load all block colors
    private void prefetchResourcePackColors()
    {
        StatTimer timer = StatTimer.get("prefetchResourcePackColors", -1).start();

        int count = 0;
        Iterator<Block> fmlBlockIter = GameData.getBlockRegistry().iterator();
        while (fmlBlockIter.hasNext())
        {
            Block block = fmlBlockIter.next();
            if (block.getMaterial().equals(Material.air))
            {
                continue;
            }

            ArrayList<ItemStack> subBlocks = new ArrayList<ItemStack>();
            try
            {
                Item item = Item.getItemFromBlock(block);
                if (item == null)
                {
                    count += prefetchColors(DataCache.instance().getBlockMD(block, 0));
                    continue;
                }

                block.getSubBlocks(item, null, subBlocks);
                for (ItemStack subBlockStack : subBlocks)
                {
                    int meta = subBlockStack.getMetadata();
                    count += prefetchColors(DataCache.instance().getBlockMD(block, meta));
                }
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error("Couldn't get subblocks for block " + block + ": " + e);
                count += prefetchColors(DataCache.instance().getBlockMD(block, 0));
            }
        }

        timer.stop();
    }

    private int prefetchColors(BlockMD blockMD)
    {
        int count = 0;
        if (blockMD.isBiomeColored())
        {
            for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
            {
                if (biome != null)
                {
                    getBiomeBlockColor(biome, blockMD, 0, 64, 0);
                    count++;
                }
                //JourneyMap.getLogger().info("Prefectched color for " + blockMD.getBlock().getUnlocalizedName() + " in " + biome.biomeName);
            }
        }
        else
        {
            getBaseColor(blockMD, 0, 78, 0);
            count++;
            //JourneyMap.getLogger().info("Prefectched color for " + blockMD.getBlock().getUnlocalizedName());
        }
        return count;
    }

    /**
     * Get the color of the block at the world coordinates.
     * @param chunkMd
     * @param blockMD
     * @param x     world x (not blockX)
     * @param y     y
     * @param z     world z (not blockZ)
     * @return
     */
    public Integer getBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        if (colorHelper == null)
        {
            return null;
        }
        Integer color = null;
        if (!blockMD.isBiomeColored())
        {
            // This may load a custom biome color and update
            // the flags on blockMD accordingly.
            color = getBaseColor(blockMD, x, y, z);
        }
        if (blockMD.isBiomeColored())
        {
            color = getBiomeBlockColor(chunkMd, blockMD, x, y, z);
        }
        return color;
    }

    /**
     * Get the biome-based block color at the world coordinates
     * @param chunkMd
     * @param blockMD
     * @param x
     * @param y
     * @param z
     * @return
     */
    private Integer getBiomeBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        BiomeGenBase biome = forgeHelper.getBiome(chunkMd.getWorld(), x, y, z);
        return getBiomeBlockColor(biome, blockMD, x, y, z);
    }

    /**
     * Get the biome-based block color at the world coordinates with the given biome.
     * @param biome
     * @param blockMD
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Integer getBiomeBlockColor(BiomeGenBase biome, BlockMD blockMD, int x, int y, int z)
    {
        if (blockMD.isGrass())
        {
            return getGrassColor(blockMD, biome, x, y, z);
        }

        if (blockMD.isFoliage())
        {
            return getFoliageColor(blockMD, biome, x, y, z);
        }

        if (blockMD.isWater())
        {
            return getWaterColor(blockMD, biome, x, y, z);
        }

        // Anything else, including those with CustomBiomeColor
        return getCustomBiomeColor(blockMD, biome, x, y, z);
    }

    private Integer getCustomBiomeColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        Integer color = getBaseColor(blockMD, x, y, z);
        int tint = getBiomeColorMultiplier(blockMD, x, y, z);
        if ((tint != 0xFFFFFF) && (tint != 0xFFFFFFFF) && tint!=0)
        { // white without alpha, white with alpha
            color = RGB.multiply(color, tint);
            Journeymap.getLogger().debug("Custom biome tint set for " + blockMD + " in " + biome.biomeName);
        }
        else
        {
            Journeymap.getLogger().debug("Custom biome tint not found for " + blockMD + " in " + biome.biomeName);
        }
        return color;
    }

    private Integer getFoliageColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        int leafColor = forgeHelper.getRenderColor(blockMD);
        int biomeColor = forgeHelper.getFoliageColor(biome, x, y, z);
        return RGB.multiply(RGB.multiply(0x999999,leafColor), biomeColor);
    }

    private Integer getGrassColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        return RGB.multiply(0x999999, forgeHelper.getGrassColor(biome, x, y, z));
    }

    private Integer getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        return RGB.multiply(getBaseColor(blockMD, x, y, z), forgeHelper.getWaterColor(biome, x, y, z));
    }

    private int getBiomeColorMultiplier(BlockMD blockMD, int x, int y, int z)
    {
        WorldClient world = forgeHelper.getClient().theWorld;
        try
        {
            // TODO: Defer calls to colorMultiplier to main thread only,
            // since some mods like Forestry mess up TileEntities when this is called
            // from a worker thread
            return forgeHelper.getColorMultiplier(world, blockMD.getBlock(), x, y, z);
        }
        catch (Exception e)
        {
            // Bugfix for NPE thrown by uk.co.shadeddimensions.ep3.block.BlockFrame.func_71920_b
            Journeymap.getLogger().warn("Block throws NullPointerException when calling colorMultiplier(): " + blockMD.getBlock().getUnlocalizedName());
            return 0xffffff;
        }
    }


    /**
     * Gets the color for the block from the cache, or
     * gets it from the icon loader.
     *
     * @param blockMD
     * @return
     */
    private int getBaseColor(BlockMD blockMD, int x, int y, int z)
    {
        Integer color = baseColors.get(blockMD);
        if (color == null)
        {
            if (blockMD.isAir())
            {
                color = 0xFFFFFF;
                blockMD.setAlpha(0f);
                blockMD.addFlags(BlockMD.Flag.HasAir, BlockMD.Flag.OpenToSky, BlockMD.Flag.NoShadow);
            }
            else
            {
                color = loadBaseColor(blockMD, x, y, z);
            }
            baseColors.put(blockMD, color);
        }
        return color;
    }

    /**
     * Provides a color using the icon loader.
     * For non-biome blocks, the base color is multiplied against the block's render color.
     *
     * @return
     */
    private Integer loadBaseColor(BlockMD blockMD, int x, int y, int z)
    {

        Integer baseColor = null;

        // Get the color from the texture
        baseColor = colorHelper.loadBlockColor(blockMD);

        // Non-biome block colors get multiplied by their render color.
        // Some blocks may have custom biome-based tints as well.
        if (baseColor != null)
        {
            if (!blockMD.isBiomeColored())
            {
                // Check for custom biome-based color multiplier
                int tint = getBiomeColorMultiplier(blockMD, x, y, z);
                if ((tint != 0xFFFFFF) && (tint != 0xFFFFFFFF))
                { // white without alpha, white with alpha
                    blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                    DataCache.instance().getBlockMetadata().setFlags(blockMD.getBlock(), BlockMD.Flag.CustomBiomeColor);
                    Journeymap.getLogger().info("Custom biome tint discovered for " + blockMD);
                }
                else
                {
                    // Check for render color
                    int renderColor = forgeHelper.getRenderColor(blockMD);
                    if (renderColor != 0xffffff && renderColor != 0xffffffff)
                    { // white without alpha or white with alpha
                        baseColor = RGB.multiply(baseColor, 0xff000000 | renderColor); // Force opaque render color
                        Journeymap.getLogger().debug("Applied render color for " + blockMD);
                    }
                }
            }
        }

        if (baseColor == null)
        {
            baseColor = 0x000000;
            if (blockMD.hasFlag(BlockMD.Flag.TileEntity))
            {
                // TODO: What to do about this?
                Journeymap.getLogger().info("Iconloader ignoring tile entity: " + blockMD);
            }
            else if (colorHelper.failedFor(blockMD))
            {
                Journeymap.getLogger().warn("Iconloader failed to get base color for " + blockMD);
            }
            else
            {
                Journeymap.getLogger().warn("Unknown failure, could not get base color for " + blockMD);
            }
        }
        return baseColor;
    }

    public void reset()
    {
        colorHelper = ForgeHelper.INSTANCE.getColorHelper();
        baseColors.clear();
    }

    public String getCacheDebugHtml()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LogFormatter.LINEBREAK).append("<!-- color cache --><div>");
        sb.append(LogFormatter.LINEBREAK).append("<b>Current Resource Packs: </b>").append(lastResourcePackNames);

        sb.append(LogFormatter.LINEBREAK).append("<table><tr valign='top'><td width='50%'>");
        sb.append(debugCache(DataCache.instance().getBlockMetadata().getAlphaMap(), "Block Transparency"));
        sb.append(LogFormatter.LINEBREAK).append("</td><td>");
        sb.append(LogFormatter.LINEBREAK).append(debugCache(DataCache.instance().getBlockMetadata().getFlagsMap(), "Block Flags"));
        sb.append(LogFormatter.LINEBREAK).append("</td></tr></table>");
        sb.append(LogFormatter.LINEBREAK).append("</div><!-- /color cache -->");

        return sb.toString();
    }

    private String debugCache(HashMap cache, String name)
    {
        if (cache.isEmpty())
        {
            return "";
        }

        List keyList = new ArrayList(cache.keySet());
        Collections.sort(keyList, new Comparator<Object>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });

        String biome = null;
        StringBuilder sb = new StringBuilder().append(LogFormatter.LINEBREAK).append("<h2>").append(name).append("</h2><div>");
        for (Object key : keyList)
        {
            Object value = cache.get(key);
            String info;
            if (key instanceof BlockMD)
            {
                info = ((BlockMD) key).getName();
            }
            else
            {
                info = key.toString();
                if (info.indexOf("|") >= 0)
                {
                    String[] infoSplit = info.split("\\|");
                    if (!infoSplit[0].equals(biome))
                    {
                        biome = infoSplit[0];
                        sb.append("<h3>").append(biome).append("</h3>");
                    }
                    info = infoSplit[1];
                }
            }

            if (value instanceof Integer)
            {
                Integer color = (Integer) value;
                String hex = RGB.toHexString(color);
                sb.append(LogFormatter.LINEBREAK).append("<span class='entry' title='").append(hex).append("'>");
                sb.append("<span class='rgb' style='background-color:").append(hex).append("'></span>");
                sb.append(info).append("</span>");
            }
            else if (value instanceof Number)
            {
                if (Double.parseDouble(value.toString()) != 1)
                {
                    sb.append(LogFormatter.LINEBREAK).append("<div class='other'><b>").append(info).append("</b>: ");
                    sb.append(value).append("</div>");
                }
            }
            else
            {
                sb.append(LogFormatter.LINEBREAK).append("<div class='other'><b>").append(info).append("</b>: ");
                sb.append(value).append("</div>");
            }

        }
        sb.append(LogFormatter.LINEBREAK).append("</div><!-- /").append(name).append(" -->");
        return sb.toString();
    }

    private static class Holder
    {
        private static final ColorCache INSTANCE = new ColorCache();
    }

}
