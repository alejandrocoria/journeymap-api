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

            if (!resourcePackSame || !modPackSame)
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

    /**
     * Get the current palette.
     * @return
     */
    public ColorPalette getCurrentPalette()
    {
        return currentPalette;
    }

    /**
     * Initialize color cache from a palette.
     * @param colorPalette
     */
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

    /**
     * Load a color palette.
     */
    private void loadColorPalette()
    {
        ColorPalette palette = ColorPalette.getActiveColorPalette();
        boolean standard = true;
        boolean permanent = false;
        if (palette != null)
        {
            standard = palette.isStandard();
            permanent = palette.isPermanent();
            initCacheFromPalette(palette);
        }
        this.currentPalette = generateColorPalette(standard, permanent);
    }

    /**
     * Generate a color palette.
     * @param standard
     * @param permanent
     * @return
     */
    private ColorPalette generateColorPalette(boolean standard, boolean permanent)
    {
        long start = System.currentTimeMillis();
        prefetchResourcePackColors();
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

    /**
     * Force load all block colors
     */
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

    /**
     * Prefetch colors for each block.
     * TODO:  Determine whether the fake coords are going to be a problem
     */
    private int prefetchColors(BlockMD blockMD)
    {
        int count = 0;
        getBaseColor(blockMD, 0, 78, 0);
        count++;
        return count;
    }

    /**
     * Get the color of the block at the world coordinates.
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

        // Last resort
        if (color == null)
        {
            color = colorHelper.getMapColor(blockMD);
        }
        return color;
    }

    /**
     * Get the biome-based block color at the world coordinates
     */
    private Integer getBiomeBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        BiomeGenBase biome = forgeHelper.getBiome(chunkMd.getWorld(), x, y, z);
        if(biome!=null)
        {
            return getBiomeBlockColor(biome, blockMD, x, y, z);
        }
        else
        {
            return null;
        }
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
            return blockMD.getBlockColorHandler().getGrassColor(blockMD, biome, x, y, z);
        }

        if (blockMD.isFoliage())
        {
            return blockMD.getBlockColorHandler().getFoliageColor(blockMD, biome, x, y, z);
        }

        if (blockMD.isWater())
        {
            return blockMD.getBlockColorHandler().getWaterColor(blockMD, biome, x, y, z);
        }

        // Anything else, including those with CustomBiomeColor
        return blockMD.getBlockColorHandler().getCustomBiomeColor(blockMD, biome, x, y, z);
    }

    /**
     * Gets the cached base color, if any.
     */
    public Integer getBaseColor(BlockMD blockMD)
    {
        return baseColors.get(blockMD);
    }

    /**
     * Gets the color for the block from the cache, or
     * gets it from the icon loader.
     *
     * @param blockMD
     * @return
     */
    public int getBaseColor(BlockMD blockMD, int x, int y, int z)
    {
        Integer color = getBaseColor(blockMD);
        if (color == null)
        {
            if (blockMD.isAir())
            {
                color = RGB.WHITE_ARGB;
                blockMD.setAlpha(0f);
                blockMD.addFlags(BlockMD.Flag.HasAir, BlockMD.Flag.OpenToSky, BlockMD.Flag.NoShadow);
            }
            else
            {
                color = loadBaseColor(blockMD, x, y, z);
            }
            setBaseColor(blockMD, color);
        }
        return color;
    }

    /**
     * Set the base color for a BlockMD.
     */
    public void setBaseColor(BlockMD blockMD, Integer color)
    {
        baseColors.put(blockMD, color);
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
                int tint = blockMD.getBlockColorHandler().getTint(blockMD, x, y, z);
                if (!RGB.isWhite(tint) && !RGB.isBlack(tint))
                {
                    blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                    DataCache.instance().getBlockMetadata().setFlags(blockMD.getBlock(), BlockMD.Flag.CustomBiomeColor);
                    Journeymap.getLogger().info("Custom biome color will be used with " + blockMD);
                }
                else
                {
                    // Check for render color
                    int renderColor = colorHelper.getRenderColor(blockMD);
                    if (!RGB.isWhite(renderColor))
                    {
                        baseColor = RGB.multiply(baseColor, RGB.ALPHA_OPAQUE | renderColor); // Force opaque render color
                        Journeymap.getLogger().info("Applied render color for " + blockMD);
                    }
                }
            }
        }

        if (baseColor == null)
        {
            baseColor = RGB.BLACK_ARGB;
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
