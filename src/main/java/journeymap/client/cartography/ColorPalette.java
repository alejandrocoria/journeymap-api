/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.client.log.ChatLog;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Provides serialization of cache colors to/from file.
 */
public class ColorPalette
{
    public static final String HELP_PAGE = "http://journeymap.info/Color_Palette";
    public static final String SAMPLE_STANDARD_PATH = ".minecraft/journeymap/";
    public static final String SAMPLE_WORLD_PATH = SAMPLE_STANDARD_PATH + "data/*/worldname/";
    public static final String JSON_FILENAME = "colorpalette.json";
    public static final String HTML_FILENAME = "colorpalette.html";
    public static final String VARIABLE = "var colorpalette=";
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final double VERSION = 5.3;
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).setPrettyPrinting().create();

    @Since(3)
    double version;

    @Since(1)
    String name;

    @Since(1)
    String generated;

    @Since(1)
    String[] description;

    @Since(1)
    boolean permanent;

    @Since(1)
    String resourcePacks;

    @Since(2)
    String modNames;

    @Since(5.2)
    ArrayList<BlockColor> blockColors = new ArrayList<BlockColor>(0);

    private transient File origin;

    /**
     * Default constructor for GSON.
     */
    ColorPalette()
    {
    }

    /**
     * Constructor invoked by static create() method
     */
    private ColorPalette(String resourcePacks, String modNames)
    {
        this.version = VERSION;
        this.name = Constants.getString("jm.colorpalette.file_title");
        this.generated = String.format("Generated using %s for %s on %s", JourneymapClient.MOD_NAME, Loader.MC_VERSION, new Date());
        this.resourcePacks = resourcePacks;
        this.modNames = modNames;

        ArrayList<String> lines = new ArrayList<String>();
        lines.add(Constants.getString("jm.colorpalette.file_header_1"));
        lines.add(Constants.getString("jm.colorpalette.file_header_2", HTML_FILENAME));
        lines.add(Constants.getString("jm.colorpalette.file_header_3", JSON_FILENAME, SAMPLE_WORLD_PATH));
        lines.add(Constants.getString("jm.colorpalette.file_header_4", JSON_FILENAME, SAMPLE_STANDARD_PATH));
        lines.add(Constants.getString("jm.config.file_header_5", HELP_PAGE));
        this.description = lines.toArray(new String[4]);

        this.blockColors = deriveBlockColors();
    }

    /**
     * Returns the active pallete.
     *
     * @return
     */
    public static ColorPalette getActiveColorPalette()
    {
        String resourcePacks = ColorManager.getResourcePackNames();
        String modNames = Constants.getModNames();

        File worldPaletteFile = ColorPalette.getWorldPaletteFile();
        if (worldPaletteFile.canRead())
        {
            ColorPalette palette = ColorPalette.loadFromFile(worldPaletteFile);
            if (palette != null)
            {
                if (palette.version < VERSION)
                {
                    Journeymap.getLogger().warn(String.format("Existing world color palette is obsolete. Required version: %s.  Found version: %s", VERSION, palette.version));
                }
                else
                {
                    return palette;
                }
            }
        }

        File standardPaletteFile = ColorPalette.getStandardPaletteFile();
        if (standardPaletteFile.canRead())
        {
            ColorPalette palette = ColorPalette.loadFromFile(standardPaletteFile);
            if (palette != null && palette.version != VERSION)
            {
                Journeymap.getLogger().warn(String.format("Existing color palette is unusable. Required version: %s.  Found version: %s", VERSION, palette.version));
                standardPaletteFile.renameTo(new File(standardPaletteFile.getParentFile(), standardPaletteFile.getName() + ".v" + palette.version));
                palette = null;
            }

            if (palette != null)
            {
                if (palette.isPermanent())
                {
                    Journeymap.getLogger().info("Existing color palette is set to be permanent.");
                    return palette;
                }

                if (resourcePacks.equals(palette.resourcePacks))
                {
                    if (modNames.equals(palette.modNames))
                    {
                        Journeymap.getLogger().info("Existing color palette's resource packs and mod names match current loadout.");
                        return palette;
                    }
                    else
                    {
                        Journeymap.getLogger().warn("Existing color palette's mods no longer match current loadout.");
                        Journeymap.getLogger().info(String.format("WAS: %s\nNOW: %s", palette.modNames, modNames));
                    }
                }
                else
                {
                    Journeymap.getLogger().warn("Existing color palette's resource packs no longer match current loadout.");
                    Journeymap.getLogger().info(String.format("WAS: %s\nNOW: %s", palette.resourcePacks, resourcePacks));
                }
            }
        }

        return null;
    }

    /**
     * Create a color palette based on current block colors and write it to file.
     */
    public static ColorPalette create(boolean standard, boolean permanent)
    {
        long start = System.currentTimeMillis();

        ColorPalette palette = null;
        try
        {
            String resourcePackNames = ColorManager.getResourcePackNames();
            String modPackNames = Constants.getModNames();

            palette = new ColorPalette(resourcePackNames, modPackNames);
            palette.setPermanent(permanent);
            palette.writeToFile(standard);
            long elapsed = System.currentTimeMillis() - start;
            Journeymap.getLogger().info(String.format("Color palette file generated with %d colors in %dms for: %s", palette.size(), elapsed, palette.getOrigin()));
            return palette;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Couldn't create ColorPalette: " + LogFormatter.toString(e));
        }
        return null;
    }

    /**
     * Gets the palette file for the specific world, if any.
     *
     * @return
     */
    private static File getWorldPaletteFile()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        return new File(FileHandler.getJMWorldDir(mc), JSON_FILENAME);
    }

    /**
     * Gets the standard palette file used with any world.
     *
     * @return
     */
    private static File getStandardPaletteFile()
    {
        return new File(FileHandler.getJourneyMapDir(), JSON_FILENAME);
    }

    /**
     * Builds the object from the file.
     * @param file
     * @return
     */
    private static ColorPalette loadFromFile(File file)
    {
        String jsonString = null;
        try
        {
            jsonString = Files.toString(file, UTF8).replaceFirst(VARIABLE, "");
            ColorPalette palette = GSON.fromJson(jsonString, ColorPalette.class);
            palette.origin = file;

            // Ensure current HTML file accompanies the data
            palette.getOriginHtml(true, true);
            return palette;
        }
        catch (Throwable e)
        {
            ChatLog.announceError(Constants.getString("jm.colorpalette.file_error", file.getPath()));
            try
            {
                file.renameTo(new File(file.getParentFile(), file.getName() + ".bad"));
            }
            catch (Exception e2)
            {
                Journeymap.getLogger().error("Couldn't rename bad palette file: " + e2);
            }
            return null;
        }
    }

    /**
     * String manipulation
     */
    private String substituteValueInContents(String contents, String key, Object... params)
    {
        String token = String.format("\\$%s\\$", key);
        return contents.replaceAll(token, Matcher.quoteReplacement(Constants.getString(key, params)));
    }

    /**
     * Generate a list of BlockColors and their nested variants.
     */
    private ArrayList<BlockColor> deriveBlockColors()
    {
        Table<Block, Integer, BlockMD> blockColorTable = HashBasedTable.create(512, 16);

        // Use table to sort/organize by Block, then color
        for (BlockMD blockMD : BlockMD.getAll())
        {
            if (blockMD.isAir())
            {
                continue;
            }

            if (blockMD.hasFlag(BlockMD.Flag.Error))
            {
                Journeymap.getLogger().debug("Block with Error flag won't be saved to color palette: " + blockMD);
                continue;
            }

            if (Strings.isEmpty(blockMD.getUid()))
            {
                Journeymap.getLogger().debug("Block without uid won't be saved to color palette: " + blockMD);
                continue;
            }

            Integer color = blockMD.getColor();
            if (color == null)
            {
                Journeymap.getLogger().debug("Block without color won't be saved to color palette: " + blockMD);
                continue;
            }

            Block block = blockMD.getBlock();
            if (block == null)
            {
                Journeymap.getLogger().debug("Bad block won't be saved to color palette: " + blockMD);
                continue;
            }

            blockColorTable.put(blockMD.getBlock(), color, blockMD);
        }

        // Build a list of BlockColor objects with variants nested by color
        ArrayList<BlockColor> list = new ArrayList<BlockColor>(512);
        for (Block block : blockColorTable.rowKeySet())
        {
            BlockColor blockColors = null;
            for (Map.Entry<Integer, BlockMD> entry : blockColorTable.row(block).entrySet())
            {
                if (blockColors == null)
                {
                    blockColors = new BlockColor(entry.getValue(), entry.getKey());
                    list.add(blockColors);
                }
                else
                {
                    blockColors.addVariant(entry.getValue(), entry.getKey());
                }
            }
            if (blockColors != null)
            {
                blockColors.sort();
            }
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Writes this instance to file.
     */
    private boolean writeToFile(boolean standard)
    {
        File palleteFile = null;
        try
        {
            // Write JSON
            palleteFile = standard ? getStandardPaletteFile() : getWorldPaletteFile();
            Files.write(VARIABLE + GSON.toJson(this), palleteFile, UTF8);
            this.origin = palleteFile;

            // Write HTML
            getOriginHtml(true, true);
            return true;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't save color palette file %s: %s", palleteFile, LogFormatter.toString(e)));
            return false;
        }
    }

    /**
     * Update BlockMD colors from the current list of blockColors
     */
    void updateColors()
    {
        for (BlockColor blockColor : blockColors)
        {
            blockColor.inflate().updateBlockMD();
        }
    }

    /**
     * Get the file this instance came from.
     */
    public File getOrigin() throws IOException
    {
        return origin.getCanonicalFile();
    }

    /**
     * Get/create the companion html file for viewing the palette.
     */
    public File getOriginHtml(boolean createIfMissing, boolean overwriteExisting)
    {
        try
        {
            if (origin == null)
            {
                return null;
            }

            File htmlFile = new File(origin.getParentFile(), HTML_FILENAME);
            if ((!htmlFile.exists() && createIfMissing) || overwriteExisting)
            {
                // Copy HTML file
                htmlFile = FileHandler.copyColorPaletteHtmlFile(origin.getParentFile(), HTML_FILENAME);
                String htmlString = Files.toString(htmlFile, UTF8);

                // Substitutions in HTML file
                htmlString = substituteValueInContents(htmlString, "jm.colorpalette.file_title");
                htmlString = substituteValueInContents(htmlString, "jm.colorpalette.file_missing_data", JSON_FILENAME);
                htmlString = substituteValueInContents(htmlString, "jm.colorpalette.resource_packs");
                htmlString = substituteValueInContents(htmlString, "jm.colorpalette.mods");
                htmlString = substituteValueInContents(htmlString, "jm.colorpalette.basic_colors");
                htmlString = substituteValueInContents(htmlString, "jm.colorpalette.biome_colors");
                Files.write(htmlString, htmlFile, UTF8);

            }
            return htmlFile;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Can't get colorpalette.html: " + t);
        }
        return null;
    }

    public boolean isPermanent()
    {
        return permanent;
    }

    public void setPermanent(boolean permanent)
    {
        this.permanent = permanent;
    }

    public boolean isStandard()
    {
        return origin != null && origin.getParentFile().getAbsoluteFile().equals(FileHandler.getJourneyMapDir().getAbsoluteFile());
    }

    public int size()
    {
        int size = 0;
        for (BlockColor blockColor : blockColors)
        {
            size += blockColor.size();
        }
        return size;
    }

    @Override
    public String toString()
    {
        return "ColorPalette[" + resourcePacks + "]";
    }

    class BlockColor implements Comparable<BlockColor>
    {
        @Since(5.2)
        String uid;

        @Since(5.2)
        String name;

        @Since(5.2)
        String color;

        @Since(5.2)
        Float alpha;

        @Since(5.2)
        Integer meta;

        @Since(5.2)
        ArrayList<BlockColor> variants;

        BlockColor()
        {
        }

        BlockColor(BlockMD blockMD, Integer color)
        {
            this.uid = blockMD.getUid();
            this.name = blockMD.getName();
            this.color = RGB.toHexString(color);
            this.alpha = blockMD.getAlpha();
            this.meta = blockMD.getMeta();
        }

        /**
         * Add a variant of this BlockColor, ideally with as few deltas as possible.
         */
        void addVariant(BlockMD blockMD, Integer color)
        {
            if (variants==null)
            {
                variants = new ArrayList<BlockColor>();
            }

            BlockColor variant = new BlockColor(blockMD, color);
            deflate(variant);
            variants.add(variant);
        }

        /**
         * Nulls any properties the variants have in common with this one.
         * Makes for more efficient serialization.
         */
        void deflate()
        {
            if (variants != null)
            {
                for (BlockColor variant : variants)
                {
                    deflate(variant);
                }
            }
        }

        /**
         * Nulls any properties the variant has in common with this one.
         * Makes for more efficient serialization.
         */
        void deflate(BlockColor variant)
        {
            if (Objects.equals(this.uid, variant.uid))
            {
                variant.uid = null;
            }
            if (Objects.equals(this.name, variant.name))
            {
                variant.name = null;
            }
            if (Objects.equals(this.color, variant.color))
            {
                variant.color = null;
            }
            if (Objects.equals(this.alpha, variant.alpha))
            {
                variant.alpha = null;
            }
            variant.deflate();
        }

        /**
         * Replaces nulls on variants with properties set on this one.
         */
        BlockColor inflate()
        {
            if (variants != null)
            {
                for (BlockColor variant : variants)
                {
                    inflate(variant);
                }
            }
            return this;
        }

        /**
         * Replaces nulls on a variant with properties set on this one.
         */
        void inflate(BlockColor variant)
        {
            if (variant.uid == null)
            {
                variant.uid = this.uid;
            }
            if (variant.name == null)
            {
                variant.name = this.name;
            }
            if (variant.color == null)
            {
                variant.color = this.color;
            }
            if (variant.alpha == null)
            {
                variant.alpha = this.alpha;
            }
            variant.inflate();
        }

        /**
         * Lookup a BlockMD and update the color and alpha.
         */
        void updateBlockMD()
        {
            if (!isInflated())
            {
                throw new IllegalArgumentException("BlockColor object must be inflated before use.");
            }

            BlockMD blockMD = BlockMD.get(this.uid, this.meta);
            if (blockMD == null)
            {
                Journeymap.getLogger().warn(String.format("Block referenced in Color Palette is not registered: %s:%s ", uid, meta));
                return;
            }

            if (blockMD.hasTransparency())
            {
                blockMD.setAlpha((alpha != null) ? alpha : 1f);
            }
            int color = RGB.hexToInt(this.color);
            blockMD.setColor(color);

            if (variants != null)
            {
                for (BlockColor variant : variants)
                {
                    variant.updateBlockMD();
                }
            }
        }

        boolean isInflated()
        {
            return (this.uid != null && this.meta != null && this.alpha != null && this.color != null);
        }

        @Override
        public int compareTo(BlockColor that)
        {
            Ordering ordering = Ordering.natural().nullsLast();
            return ComparisonChain.start()
                    .compare(this.uid, that.uid, ordering)
                    .compare(this.meta, that.meta, ordering)
                    .compare(this.name, that.name, ordering)
                    .compare(this.color, that.color, ordering)
                    .compare(this.alpha, that.alpha, ordering)
                    .result();
        }

        public int size()
        {
            int size = 1;
            if (variants!=null)
            {
                for (BlockColor variant : variants)
                {
                    size += variant.size();
                }
            }
            return size;
        }

        public void sort()
        {
            if (variants!=null)
            {
                Collections.sort(variants);
            }
        }
    }
}
