/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography.color;

import com.google.common.collect.HashBasedTable;
import com.google.common.io.Files;
import com.google.gson.*;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.client.log.ChatLog;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameData;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Provides serialization of cache colors to/from file.
 */
public class ColorPalette
{
    /**
     * The constant HELP_PAGE.
     */
    public static final String HELP_PAGE = "http://journeymap.info/Color_Palette";
    /**
     * The constant SAMPLE_STANDARD_PATH.
     */
    public static final String SAMPLE_STANDARD_PATH = ".minecraft/journeymap/";
    /**
     * The constant SAMPLE_WORLD_PATH.
     */
    public static final String SAMPLE_WORLD_PATH = SAMPLE_STANDARD_PATH + "data/*/worldname/";
    /**
     * The constant JSON_FILENAME.
     */
    public static final String JSON_FILENAME = "colorpalette.json";
    /**
     * The constant HTML_FILENAME.
     */
    public static final String HTML_FILENAME = "colorpalette.html";
    /**
     * The constant VARIABLE.
     */
    public static final String VARIABLE = "var colorpalette=";
    /**
     * The constant UTF8.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * The constant VERSION.
     */
    public static final double VERSION = 5.49;

    private static final Logger logger = Journeymap.getLogger();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(HashBasedTable.class, new Serializer())
            .registerTypeAdapter(HashBasedTable.class, new Deserializer())
            //.setPrettyPrinting()
            .create();

    /**
     * The Version.
     */
    @Since(3)
    double version;

    /**
     * The Name.
     */
    @Since(1)
    String name;

    /**
     * The Generated.
     */
    @Since(1)
    String generated;

    /**
     * The Description.
     */
    @Since(1)
    String[] description;

    /**
     * The Permanent.
     */
    @Since(1)
    boolean permanent;

    /**
     * The Resource packs.
     */
    @Since(1)
    String resourcePacks;

    /**
     * The Mod names.
     */
    @Since(2)
    String modNames;

    /**
     * The Block colors.
     */
    @Since(5.49)
    HashBasedTable<String, String, BlockStateColor> table;

    /**
     * Source file
     */
    private transient File origin;

    /**
     * Whether changes have occurred but have not been persisted.
     */
    private transient boolean dirty;

    /**
     * Default constructor for GSON.
     */
    ColorPalette()
    {
        table = HashBasedTable.create(GameData.getBlockStateIDMap().size(), 16);
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
        table = HashBasedTable.create(GameData.getBlockStateIDMap().size(), 16);
    }

    /**
     * Returns the active pallete.
     *
     * @return active color palette
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
                    logger.warn(String.format("Existing world color palette is obsolete. Required version: %s.  Found version: %s", VERSION, palette.version));
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
                logger.warn(String.format("Existing color palette is unusable. Required version: %s.  Found version: %s", VERSION, palette.version));
                standardPaletteFile.renameTo(new File(standardPaletteFile.getParentFile(), standardPaletteFile.getName() + ".v" + palette.version));
                palette = null;
            }

            if (palette != null)
            {
                if (palette.isPermanent())
                {
                    logger.info("Existing color palette is set to be permanent.");
                    return palette;
                }

                if (resourcePacks.equals(palette.resourcePacks))
                {
                    if (modNames.equals(palette.modNames))
                    {
                        logger.debug("Existing color palette's resource packs and mod names match current loadout.");
                        return palette;
                    }
                    else
                    {
                        logger.warn("Existing color palette's mods no longer match current loadout.");
                        logger.info(String.format("WAS: %s\nNOW: %s", palette.modNames, modNames));
                    }
                }
                else
                {
                    logger.warn("Existing color palette's resource packs no longer match current loadout.");
                    logger.info(String.format("WAS: %s\nNOW: %s", palette.resourcePacks, resourcePacks));
                }
            }
        }

        return null;
    }

    /**
     * Create a color palette based on current block colors and write it to file.
     *
     * @param standard  the standard
     * @param permanent the permanent
     * @return the color palette
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
            logger.info(String.format("Color palette file generated for %d blockstates in %dms for: %s", palette.size(), elapsed, palette.getOrigin()));
            return palette;
        }
        catch (Exception e)
        {
            logger.error("Couldn't create ColorPalette: " + LogFormatter.toString(e));
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
                logger.error("Couldn't rename bad palette file: " + e2);
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
     * Whether BlockStateColor is in the palette for the BlockMD.
     * @return true if exists.
     */
    boolean hasBlockStateColor(final BlockMD blockMD)
    {
        BlockMD canonical = blockMD.getCanonicalState();
        return table.contains(BlockMD.getBlockId(canonical), BlockMD.getBlockStateId(canonical));
    }


    /**
     * Get the BlockStateColor from the palette, create one if missing.
     *
     * @param blockMD         block
     * @param createIfMissing true to create
     * @return null if not created, including if no color was derived during creation attempt
     */
    @Nullable
    private BlockStateColor getBlockStateColor(final BlockMD blockMD, boolean createIfMissing)
    {
        BlockMD canonical = blockMD.getCanonicalState();

        BlockStateColor blockStateColor = table.get(BlockMD.getBlockId(canonical), BlockMD.getBlockStateId(canonical));

        if (blockStateColor == null && createIfMissing)
        {
            if (canonical.hasColor())
            {
                blockStateColor = new BlockStateColor(canonical);
                table.put(BlockMD.getBlockId(canonical), BlockMD.getBlockStateId(canonical), blockStateColor);
                dirty = true;
            }
        }
        return blockStateColor;
    }


    /**
     * Update BlockMD colors from the palette
     *
     * @param blockMD
     * @return true if color applied.
     */
    public boolean applyColor(BlockMD blockMD, boolean createIfMissing)
    {
        boolean preExisting = hasBlockStateColor(blockMD);
        BlockStateColor blockStateColor = getBlockStateColor(blockMD, createIfMissing);
        if (blockStateColor == null)
        {
            return false;
        }
        else if (preExisting)
        {
            if (blockMD.hasTransparency())
            {
                blockMD.setAlpha((blockStateColor.alpha != null) ? blockStateColor.alpha : 1f);
            }
            int color = RGB.hexToInt(blockStateColor.color);
            blockMD.setColor(color);
        }
        return true;
    }

    /**
     * Update BlockMD colors from the palette
     *
     * @param blockMDs
     */
    public int applyColors(Collection<BlockMD> blockMDs, boolean createIfMissing)
    {
        int hit = 0;
        int miss = 0;

        for (BlockMD blockMD : blockMDs)
        {
            if (applyColor(blockMD, createIfMissing))
            {
                hit++;
            }
            else
            {
                miss++;
            }
        }

        if (miss> 0)
        {
            logger.debug(miss + " blockstates didn't have a color in the palette");
        }

        return hit;
    }

    public void writeToFile()
    {
        writeToFile(isStandard());
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
            this.dirty = false;

            // Write HTML
            getOriginHtml(true, true);
            return true;
        }
        catch (Exception e)
        {
            logger.error(String.format("Can't save color palette file %s: %s", palleteFile, LogFormatter.toString(e)));
            return false;
        }
    }


    /**
     * Get the file this instance came from.
     *
     * @return the origin
     * @throws IOException the io exception
     */
    public File getOrigin() throws IOException
    {
        return origin.getCanonicalFile();
    }

    /**
     * Get/create the companion html file for viewing the palette.
     *
     * @param createIfMissing   the create if missing
     * @param overwriteExisting the overwrite existing
     * @return the origin html
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
            logger.error("Can't write colorpalette.html: " + t);
        }
        return null;
    }

    /**
     * Is permanent boolean.
     *
     * @return the boolean
     */
    public boolean isPermanent()
    {
        return permanent;
    }

    /**
     * Sets permanent.
     *
     * @param permanent the permanent
     */
    public void setPermanent(boolean permanent)
    {
        this.permanent = permanent;
    }

    /**
     * Is standard boolean.
     *
     * @return the boolean
     */
    public boolean isStandard()
    {
        return origin != null && origin.getParentFile().getAbsoluteFile().equals(FileHandler.getJourneyMapDir().getAbsoluteFile());
    }

    /**
     * Palette version
     *
     * @return version
     */
    public double getVersion()
    {
        return version;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size()
    {
        return table.size();
    }

    @Override
    public String toString()
    {
        return "ColorPalette[" + resourcePacks + "]";
    }

    /**
     * Custom serializer for the palette file.
     */
    private static class Serializer implements JsonSerializer<HashBasedTable<String, String, BlockStateColor>>
    {
        @Override
        public JsonElement serialize(HashBasedTable<String, String, BlockStateColor> src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonTable = new JsonObject();

            for (String blockId : src.rowKeySet().stream().sorted().collect(Collectors.toList()))
            {
                String[] resource = blockId.split(":");
                String mod = resource[0];
                String block = resource[1];
                JsonObject jsonMod = null;
                if (!jsonTable.has(mod))
                {
                    jsonMod = new JsonObject();
                    jsonTable.add(mod, jsonMod);
                }
                else
                {
                    jsonMod = jsonTable.getAsJsonObject(mod);
                }

                JsonObject jsonBlock = null;
                if (!jsonMod.has(block))
                {
                    jsonBlock = new JsonObject();
                    jsonMod.add(block, jsonBlock);
                }
                else
                {
                    jsonBlock = jsonMod.getAsJsonObject(block);
                }

                for (String stateId : src.row(blockId).keySet().stream().sorted().collect(Collectors.toList()))
                {
                    BlockStateColor blockStateColor = src.get(blockId, stateId);
                    JsonArray bscArray = new JsonArray();
                    bscArray.add(new JsonPrimitive(blockStateColor.color));
                    if (blockStateColor.alpha != null && blockStateColor.alpha != 1F)
                    {
                        bscArray.add(new JsonPrimitive(blockStateColor.alpha));
                    }
                    jsonBlock.add(stateId, bscArray);
                }
            }
            return jsonTable;
        }
    }

    /**
     * Custom deserializer to produce a smaller palette file.
     */
    private static class Deserializer implements JsonDeserializer<HashBasedTable<String, String, BlockStateColor>>
    {
        @Override
        public HashBasedTable<String, String, BlockStateColor> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            HashBasedTable<String, String, BlockStateColor> result = HashBasedTable.create(GameData.getBlockStateIDMap().size(), 16);
            JsonObject jsonTable = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> jsonMod : jsonTable.entrySet())
            {
                String modId = jsonMod.getKey();
                for (Map.Entry<String, JsonElement> jsonBlock : jsonMod.getValue().getAsJsonObject().entrySet())
                {
                    String blockId = jsonBlock.getKey();

                    for (Map.Entry<String, JsonElement> jsonState : jsonBlock.getValue().getAsJsonObject().entrySet())
                    {
                        String blockStateId = jsonState.getKey();
                        JsonArray bscArray = jsonState.getValue().getAsJsonArray();
                        String color = bscArray.get(0).getAsString();
                        float alpha = 1F;
                        if (bscArray.size() > 1)
                        {
                            alpha = bscArray.get(1).getAsFloat();
                        }
                        BlockStateColor blockStateColor = new BlockStateColor(color, alpha);
                        result.put(modId + ":" + blockId, blockStateId, blockStateColor);
                    }
                }
            }

            return result;
        }
    }
}
