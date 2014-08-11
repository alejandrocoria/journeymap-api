/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;

import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Provides serialization of cache colors to/from file.
 */
public class ColorPalette
{
    public static final String SAMPLE_GLOBAL_PATH = Joiner.on(File.separator).join(".minecraft", "journeyMap");
    public static final String SAMPLE_WORLD_PATH = Joiner.on(File.separator).join(SAMPLE_GLOBAL_PATH, "data", "*", "(worldname)");
    public static final String JSON_FILENAME = "colorpalette.json";
    public static final String HTML_FILENAME = "colorpalette.html";
    public static final String VARIABLE = "var colorpalette=";
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final int VERSION = 1;
    public static final Gson GSON = new GsonBuilder().setVersion(VERSION).setPrettyPrinting().create();
    @Since(1)
    String name;
    @Since(1)
    String[] description;
    @Since(1)
    String generated;
    @Since(1)
    String resourcePacks;
    @Since(1)
    ArrayList<BlockColor> basicColors = new ArrayList<BlockColor>(0);
    @Since(1)
    LinkedHashMap<String, ArrayList<BlockColor>> biomeColors = new LinkedHashMap<String, ArrayList<BlockColor>>(60);
    private transient File origin;

    ColorPalette()
    {
    }

    ColorPalette(String resourcePacks, HashMap<BlockMD, Color> basicColorMap, HashMap<String, HashMap<BlockMD, Color>> biomeColorMap)
    {
        this.name = Constants.getString("jm.colorpalette.file_title");
        this.generated = String.format("Generated using %s for %s on %s", JourneyMap.MOD_NAME, Loader.MC_VERSION, new Date());
        this.resourcePacks = resourcePacks;

        ArrayList<String> lines = new ArrayList<String>();
        lines.add(Constants.getString("jm.colorpalette.file_header_1"));
        lines.add(Constants.getString("jm.colorpalette.file_header_2", HTML_FILENAME));
        lines.add(Constants.getString("jm.colorpalette.file_header_3", JSON_FILENAME, SAMPLE_WORLD_PATH));
        lines.add(Constants.getString("jm.colorpalette.file_header_4", JSON_FILENAME, SAMPLE_GLOBAL_PATH));
        this.description = lines.toArray(new String[4]);

        this.basicColors = toList(basicColorMap);
        ArrayList<String> biomeNames = new ArrayList<String>(biomeColorMap.keySet());
        Collections.sort(biomeNames);

        for (String biomeName : biomeNames)
        {
            ArrayList<BlockColor> list = toList(biomeColorMap.get(biomeName));
            this.biomeColors.put(biomeName, list);
        }
    }

    public static boolean paletteFileFound()
    {
        return getWorldPaletteFile().canRead() || getGlobalPaletteFile().canRead();
    }

    static File getWorldPaletteFile()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        return new File(FileHandler.getJMWorldDir(mc), JSON_FILENAME);
    }

    static File getGlobalPaletteFile()
    {
        return new File(FileHandler.getJourneyMapDir(), JSON_FILENAME);
    }

    public static ColorPalette loadFromFile()
    {
        File worldPaletteFile = getWorldPaletteFile();
        if (worldPaletteFile.canRead())
        {
            ColorPalette palette = loadFromFile(worldPaletteFile);
            if (palette != null)
            {
                return palette;
            }
        }

        File globalPaletteFile = getGlobalPaletteFile();
        if (globalPaletteFile.canRead())
        {
            ColorPalette palette = loadFromFile(globalPaletteFile);
            if (palette != null)
            {
                return palette;
            }
        }

        return null;
    }

    protected static ColorPalette loadFromFile(File file)
    {
        String jsonString = null;
        try
        {
            jsonString = Files.toString(file, UTF8).replaceFirst(VARIABLE, "");
            ColorPalette palette = GSON.fromJson(jsonString, ColorPalette.class);
            palette.origin = file;
            return palette;
        }
        catch (Throwable e)
        {
            ChatLog.announceError(Constants.getString("jm.colorpalette.file_error", file.getPath()));
            return null;
        }
    }

    private String substituteValueInContents(String contents, String key, Object... params)
    {
        String token = String.format("\\$%s\\$", key);
        return contents.replaceAll(token, Matcher.quoteReplacement(Constants.getString(key, params)));
    }

    private ArrayList<BlockColor> toList(HashMap<BlockMD, Color> map)
    {
        ArrayList<BlockColor> list = new ArrayList<BlockColor>(map.size());
        for (Map.Entry<BlockMD, Color> entry : map.entrySet())
        {
            list.add(new BlockColor(entry.getKey(), entry.getValue()));
        }
        Collections.sort(list);
        return list;
    }

    public boolean writeToFile(boolean global)
    {
        File palleteFile = null;
        try
        {
            // Write JSON
            palleteFile = global ? getGlobalPaletteFile() : getWorldPaletteFile();
            Files.write(VARIABLE + GSON.toJson(this), palleteFile, UTF8);

            // Copy HTML file
            File htmlFile = FileHandler.copyColorPaletteHtmlFile(palleteFile.getParentFile(), HTML_FILENAME);
            String htmlString = Files.toString(htmlFile, UTF8);

            // Substitutions in HTML file
            htmlString = substituteValueInContents(htmlString, "jm.colorpalette.file_title");
            htmlString = substituteValueInContents(htmlString, "jm.colorpalette.file_missing_data", JSON_FILENAME);
            htmlString = substituteValueInContents(htmlString, "jm.colorpalette.resource_packs");
            htmlString = substituteValueInContents(htmlString, "jm.colorpalette.basic_colors");
            htmlString = substituteValueInContents(htmlString, "jm.colorpalette.biome_colors");
            Files.write(htmlString, htmlFile, UTF8);

            return true;
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't save color pallete file %s: %s", palleteFile, LogFormatter.toString(e)));
            return false;
        }
    }

    private HashMap<BlockMD, Color> listToMap(ArrayList<BlockColor> list)
    {
        HashMap<BlockMD, Color> map = new HashMap<BlockMD, Color>(list.size());
        for (BlockColor blockColor : list)
        {
            GameRegistry.UniqueIdentifier uid = new GameRegistry.UniqueIdentifier(blockColor.uid);
            BlockMD blockMD = BlockMD.getBlockMD(uid, blockColor.meta);
            if (blockMD.hasFlag(BlockUtils.Flag.Transparency))
            {
                Float alpha = blockColor.alpha;
                blockMD.setAlpha((alpha != null) ? alpha : 1f);
            }
            int color = Integer.parseInt(blockColor.color.replaceFirst("#", ""), 16);
            map.put(blockMD, new Color(color));
        }
        return map;
    }

    public HashMap<BlockMD, Color> getBasicColorMap()
    {
        return listToMap(this.basicColors);
    }

    public HashMap<String, HashMap<BlockMD, Color>> getBiomeColorMap()
    {
        HashMap<String, HashMap<BlockMD, Color>> map = new HashMap<String, HashMap<BlockMD, Color>>();
        for (String biome : biomeColors.keySet())
        {
            map.put(biome, listToMap(biomeColors.get(biome)));
        }
        return map;
    }

    public File getOrigin()
    {
        return origin;
    }

    public boolean isGlobal()
    {
        return origin != null && origin.getParentFile().getAbsoluteFile().equals(FileHandler.getJourneyMapDir().getAbsoluteFile());
    }

    public int size()
    {
        int count = basicColors.size();
        if(biomeColors.size()>0) {
            count += ((biomeColors.size() * biomeColors.entrySet().iterator().next().getValue().size()));
        }
        return count;
    }

    @Override
    public String toString()
    {
        return "ColorPalette[" + resourcePacks + "]";
    }


    class BlockColor implements Comparable<BlockColor>
    {
        @Since(1)
        String name;

        @Since(1)
        String uid;

        @Since(1)
        int meta;

        @Since(1)
        String color;

        @Since(1)
        Float alpha;

        BlockColor(BlockMD blockMD, Color awtColor)
        {
            this.name = blockMD.getName();
            this.uid = blockMD.key.uid.toString();
            this.meta = blockMD.key.meta;
            this.color = String.format("#%02x%02x%02x", awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
            if (blockMD.getAlpha() < 1f)
            {
                this.alpha = blockMD.getAlpha();
            }
        }

        @Override
        public int compareTo(BlockColor o)
        {
            int result = this.name.compareTo(o.name);
            if (result != 0)
            {
                return result;
            }
            result = this.uid.compareTo(o.uid);
            if (result != 0)
            {
                return result;
            }
            return Integer.compare(this.meta, o.meta);
        }
    }
}
