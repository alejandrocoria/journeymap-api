/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameData;
import modinfo.ModInfo;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.io.IconLoader;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Cache of block baseColors derived from the current texture pack.
 *
 * @author mwoodman
 */
public class ColorCache implements IResourceManagerReloadListener
{

    private final HashMap<BlockMD, Color> baseColors = new HashMap<BlockMD, Color>(256);
    private final HashMap<String, HashMap<BlockMD, Color>> biomeColors = new HashMap<String, HashMap<BlockMD, Color>>(32);
    private volatile IconLoader iconLoader;
    private String lastResourcePack;

    private ColorCache()
    {
        IResourceManager rm = FMLClientHandler.instance().getClient().getResourceManager();
        if (rm instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) rm).registerReloadListener(this);
        }
        else
        {
            JourneyMap.getLogger().warning("Could not register ResourcePack ReloadListener.  Changing resource packs will require restart");
        }

        this.onResourceManagerReload(rm);
    }

    public static ColorCache getInstance()
    {
        return Holder.INSTANCE;
    }

    @Override
    public void onResourceManagerReload(IResourceManager mgr)
    {
        if (JourneyMap.getInstance().isMapping() || iconLoader == null)
        {
            String currentPack = getResourcePackNames();
            if (currentPack.equals(lastResourcePack))
            {
                JourneyMap.getLogger().fine("ResourcePack unchanged: " + currentPack);
            }
            else
            {
                JourneyMap.getLogger().info("ResourcePack: " + lastResourcePack + " --> " + currentPack);
                ModInfo modInfo = JourneyMap.getInstance().getModInfo();
                if (modInfo != null)
                {
                    modInfo.reportEvent("Resource Pack", "Load", currentPack);
                }
                reset();
                lastResourcePack = currentPack;
                iconLoader = new IconLoader();

                if(ColorPalette.paletteFileFound())
                {
                    loadColorPalette();
                }
                else
                {
                    prefetchResourcePackColors();
                }
            }

        }
    }

    private void loadColorPalette()
    {
        try
        {
            long start = System.currentTimeMillis();
            ColorPalette colorPalette = ColorPalette.loadFromFile();
            if (colorPalette != null)
            {
                this.baseColors.putAll(colorPalette.getBasicColorMap());
                this.biomeColors.putAll(colorPalette.getBiomeColorMap());
                long elapsed = System.currentTimeMillis()-start;
                JourneyMap.getLogger().info(String.format("Color palette loaded %d colors from '%s' in %dms", colorPalette.size(), colorPalette.getOrigin(), elapsed));
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning("Could not load color palette: " + e);
        }
    }

    private String getResourcePackNames()
    {
        ResourcePackRepository resourcepackrepository = FMLClientHandler.instance().getClient().getResourcePackRepository();
        String packs = Joiner.on(",").join(Lists.reverse(resourcepackrepository.getRepositoryEntries()));
        if(Strings.isNullOrEmpty(packs))
        {
            packs = "Default";
        }
        return packs;
    }

    public ColorPalette generateColorPalette(boolean global)
    {
        DataCache.instance().resetBlockMetadata();
        prefetchResourcePackColors();
        ColorPalette palette = null;
        try
        {
            String resourcePackNames = getResourcePackNames();
            palette = new ColorPalette(resourcePackNames, baseColors, biomeColors);
            if(palette.writeToFile(global))
            {
                return palette;
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe("Couldn't create ColorPalette: " + LogFormatter.toString(e));
        }
        return null;
    }

    // Force load all block colors
    public void prefetchResourcePackColors()
    {
        StatTimer timer = StatTimer.get("prefetchResourcePackColors", -1).start();

        int count = 0;
        Iterator<Block> fmlBlockIter = GameData.getBlockRegistry().iterator();
        while(fmlBlockIter.hasNext())
        {
            Block block = fmlBlockIter.next();
            if(block.getMaterial().equals(Material.air))
            {
                continue;
            }

            ArrayList<ItemStack> subBlocks = new ArrayList<ItemStack>();
            try
            {
                Item item = Item.getItemFromBlock(block);
                if(item==null)
                {
                    count += prefetchColors(DataCache.instance().getBlockMD(block, 0));
                    continue;
                }

                block.getSubBlocks(item, null, subBlocks);
                for(ItemStack subBlockStack : subBlocks)
                {
                    int meta = subBlockStack.getItemDamage();
                    count += prefetchColors(DataCache.instance().getBlockMD(block, meta));
                }
            }
            catch(Exception e)
            {
                JourneyMap.getLogger().severe("Couldn't get subblocks for block " + block + ": " + e);
                count += prefetchColors(DataCache.instance().getBlockMD(block, 0));
            }
        }

        timer.stop();
    }

    private int prefetchColors(BlockMD blockMD)
    {
        int count = 0;
        if(blockMD.isBiomeColored())
        {
            for(BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
            {
                if(biome!=null)
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

    public Integer getBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        if (iconLoader == null)
        {
            return null;
        }
        Color color = null;
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
        return color.getRGB();
    }

    private Color getBiomeBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        BiomeGenBase biome = chunkMd.stub.getBiomeGenForWorldCoords(x, z, chunkMd.worldObj.getWorldChunkManager());
        return getBiomeBlockColor(biome, blockMD, x, y, z);
    }

    public Color getBiomeBlockColor(BiomeGenBase biome, BlockMD blockMD, int x, int y, int z) {
        Block block = blockMD.getBlock();

        if(block instanceof BlockGrass || block instanceof BlockTallGrass) {
            return getGrassColor(blockMD, biome, x, y, z);
        }

        if(blockMD.isWater()) {
            return getWaterColor(blockMD, biome, x, y, z);
        }

        if(blockMD.isFoliage() || block instanceof BlockVine) {
            return getFoliageColor(blockMD, biome, x, y, z);
        }

        // Anything else, including those with CustomBiomeColor
        return getCustomBiomeColor(blockMD, biome, x, y, z);
    }

    private Color getCustomBiomeColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        Color color = getBiomeColor(blockMD, biome);
        if (color == null)
        {
            color = getBaseColor(blockMD, x, y, z);
            int tint = getBiomeColorMultiplier(blockMD, x, y, z);
            if ((tint != 0xFFFFFF) && (tint != 0xFFFFFFFF))
            { // white without alpha, white with alpha
                color = colorMultiplier(color, tint);
                JourneyMap.getLogger().fine("Custom biome tint set for " + blockMD + " in " + biome.biomeName);
            }
            else
            {
                JourneyMap.getLogger().fine("Custom biome tint not found for " + blockMD + " in " + biome.biomeName);
            }
            putBiomeColor(blockMD, biome, color);
        }
        return color;
    }

    private Color getFoliageColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        Color color = getBiomeColor(blockMD, biome);
        if (color == null)
        {
            int leafColor = blockMD.getBlock().getRenderColor(blockMD.meta); // getRenderColor()
            int biomeColor = BiomeGenBase.plains.getBiomeFoliageColor(x, y, z); // Default
            try
            {
                biomeColor = biome.getBiomeFoliageColor(x, y, z);
            }
            catch (Throwable t)
            {
                blockMD.addFlags(BlockMD.Flag.Error);
                JourneyMap.getLogger().severe("Couldn't get biome foliage color: " + LogFormatter.toString(t));
            }
            int leafTimesBiome = colorMultiplier(biomeColor, leafColor);
            int darker = colorMultiplier(leafTimesBiome, 0xFFAAAAAA); // I added this, I'm sure it'll break with some custom leaf mod somewhere.
            color = new Color(darker);
            putBiomeColor(blockMD, biome, color);
        }
        return color;
    }

    private Color getGrassColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        Color color = getBiomeColor(blockMD, biome);
        if (color == null)
        {
            Color baseColor = getBaseColor(blockMD, x, y, z);
            int biomeColor = BiomeGenBase.plains.getBiomeGrassColor(x, y, z); // Default
            try
            {
                biomeColor = biome.getBiomeGrassColor(x, y, z);
            }
            catch (Throwable t)
            {
                blockMD.addFlags(BlockMD.Flag.Error);
                JourneyMap.getLogger().severe("Couldn't get biome grass color: " + LogFormatter.toString(t));
            }
            color = colorMultiplier(baseColor, biomeColor);
            putBiomeColor(blockMD, biome, color);
        }
        return color;
    }

    private Color getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        Color color = getBiomeColor(blockMD, biome);
        if (color == null)
        {
            color = colorMultiplier(getBaseColor(blockMD, x, y, z), biome.waterColorMultiplier);
            putBiomeColor(blockMD, biome, color);
        }
        return color;
    }

    private int getBiomeColorMultiplier(BlockMD blockMD, int x, int y, int z)
    {
        WorldClient world = FMLClientHandler.instance().getClient().theWorld;
        try
        {
            return blockMD.getBlock().colorMultiplier(world, x, 78, z) | 0xFF000000;
        }
        catch (NullPointerException e)
        {
            // Bugfix for NPE thrown by uk.co.shadeddimensions.ep3.block.BlockFrame.func_71920_b
            JourneyMap.getLogger().warning("Block throws NullPointerException when calling colorMultiplier(): " + blockMD.getBlock().getUnlocalizedName());
            return 16777215;
        }
    }

    private HashMap<BlockMD, Color> getBiomeColorMap(BiomeGenBase biome)
    {
        synchronized (biomeColors)
        {
            HashMap<BlockMD, Color> biomeColorMap = biomeColors.get(biome.biomeName);
            if (biomeColorMap == null)
            {
                biomeColorMap = new HashMap<BlockMD, Color>(16);
                biomeColors.put(biome.biomeName, biomeColorMap);
            }
            return biomeColorMap;
        }
    }

    private Color getBiomeColor(BlockMD blockMD, BiomeGenBase biome)
    {
        return getBiomeColorMap(biome).get(blockMD);
    }

    private void putBiomeColor(BlockMD blockMD, BiomeGenBase biome, Color color)
    {
        getBiomeColorMap(biome).put(blockMD, color);
    }

    /**
     * Gets the color for the block from the cache, or
     * gets it from the icon loader.
     *
     * @param blockMD
     * @return
     */
    private Color getBaseColor(BlockMD blockMD, int x, int y, int z)
    {
        Color color = baseColors.get(blockMD);
        if (color == null)
        {
            if (blockMD.isAir())
            {
                color = Color.white;
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
    private Color loadBaseColor(BlockMD blockMD, int x, int y, int z)
    {

        Color baseColor = null;

        // Get the color from the texture
        synchronized (iconLoader)
        {
            baseColor = iconLoader.loadBlockColor(blockMD);
        }

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
                    DataCache.instance().getBlockMetadata().setFlags(blockMD.getBlock(), BlockMD.Flag.BiomeColor);
                    JourneyMap.getLogger().fine("Custom biome tint discovered for " + blockMD);
                }
                else
                {
                    // Check for render color
                    int renderColor = blockMD.getBlock().getRenderColor(blockMD.meta & 0xf); // getRenderColor()
                    if (renderColor != 0xffffff && renderColor != 0xffffffff)
                    { // white without alpha or white with alpha
                        baseColor = colorMultiplier(baseColor, 0xff000000 | renderColor); // Force opaque render color
                        JourneyMap.getLogger().fine("Applied render color for " + blockMD);
                    }
                }
            }
        }

        if (baseColor == null)
        {
            baseColor = Color.BLACK;
            if (iconLoader.failedFor(blockMD))
            {
                JourneyMap.getLogger().warning("Iconloader failed to get base color for " + blockMD);
            }
            else
            {
                JourneyMap.getLogger().warning("Unknown failure, could not get base color for " + blockMD);
            }
        }
        return baseColor;
    }

    public void reset()
    {
        biomeColors.clear();
        baseColors.clear();
    }

    public String getCacheDebugHtml()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LogFormatter.LINEBREAK).append("<!-- color cache --><div>");
        sb.append(LogFormatter.LINEBREAK).append("<b>Current Resource Packs: </b>").append(lastResourcePack);

        sb.append(LogFormatter.LINEBREAK).append("<table><tr valign='top'><td width='50%'>");
        sb.append(debugCache(DataCache.instance().getBlockMetadata().getAlphaMap(), "Block Transparency"));
        sb.append(LogFormatter.LINEBREAK).append("</td><td>");
        sb.append(LogFormatter.LINEBREAK).append(debugCache(DataCache.instance().getBlockMetadata().getFlagsMap(), "Block Flags"));
        sb.append(LogFormatter.LINEBREAK).append("</td></tr></table>");

        sb.append(LogFormatter.LINEBREAK).append(debugCache(baseColors, "Base Colors"));

        List<String> biomeNames = new ArrayList<String>(biomeColors.keySet());
        Collections.sort(biomeNames);

        for (String biome : biomeNames)
        {
            HashMap<BlockMD, Color> colorsForBiome = biomeColors.get(biome);
            sb.append(LogFormatter.LINEBREAK).append(debugCache(colorsForBiome, "Biome Colors: " + biome));
        }

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

            if (value instanceof Color)
            {
                Color color = (Color) value;
                String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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

    Color colorMultiplier(Color color, int mult)
    {
        return new Color(colorMultiplier(color.getRGB(), mult));
    }

    int colorMultiplier(int rgb, int mult)
    {

        int alpha1 = rgb >> 24 & 0xFF;
        int red1 = rgb >> 16 & 0xFF;
        int green1 = rgb >> 8 & 0xFF;
        int blue1 = rgb >> 0 & 0xFF;

        int alpha2 = mult >> 24 & 0xFF;
        int red2 = mult >> 16 & 0xFF;
        int green2 = mult >> 8 & 0xFF;
        int blue2 = mult >> 0 & 0xFF;

        int alpha = alpha1 * alpha2 / 255;
        int red = red1 * red2 / 255;
        int green = green1 * green2 / 255;
        int blue = blue1 * blue2 / 255;

        int result = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;

        return result | -16777216;
    }

    private static class Holder
    {
        private static final ColorCache INSTANCE = new ColorCache();
    }

}
