/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.option;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.ui.component.CheckBox;
import journeymap.client.ui.component.IntSliderButton;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.client.ui.component.ScrollListPane;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static journeymap.client.properties.ClientCategory.*;

/**
 * Generates the UI slots in the Options Manager.
 */
public class OptionSlotFactory
{
    protected static final Charset UTF8 = Charset.forName("UTF-8");
    protected static BufferedWriter docWriter;
    protected static File docFile;
    protected static boolean generateDocs = false;

    public static List<CategorySlot> getSlots(Map<Category, List<SlotMetadata>> toolbars)
    {
        HashMap<Category, List<SlotMetadata>> mergedMap = new HashMap<Category, List<SlotMetadata>>();

        addSlots(mergedMap, MiniMap1, Journeymap.getClient().getMiniMapProperties1());
        addSlots(mergedMap, MiniMap2, Journeymap.getClient().getMiniMapProperties2());
        addSlots(mergedMap, FullMap, Journeymap.getClient().getFullMapProperties());
        addSlots(mergedMap, WebMap, Journeymap.getClient().getWebMapProperties());
        addSlots(mergedMap, Waypoint, Journeymap.getClient().getWaypointProperties());
        addSlots(mergedMap, Advanced, Journeymap.getClient().getCoreProperties());

        List<CategorySlot> categories = new ArrayList<CategorySlot>();
        for (Map.Entry<Category, List<SlotMetadata>> entry : mergedMap.entrySet())
        {
            Category category = entry.getKey();
            CategorySlot categorySlot = new CategorySlot(category);
            for (SlotMetadata val : entry.getValue())
            {
                categorySlot.add(new ButtonListSlot(categorySlot).add(val));
            }

            if (toolbars.containsKey(category))
            {
                ButtonListSlot toolbarSlot = new ButtonListSlot(categorySlot);
                for (SlotMetadata toolbar : toolbars.get(category))
                {
                    toolbarSlot.add(toolbar);
                }
                categorySlot.add(toolbarSlot);
            }

            categories.add(categorySlot);
        }

        Collections.sort(categories);

        int count = 0;
        for (CategorySlot categorySlot : categories)
        {
            count += categorySlot.size();
        }

        if (generateDocs)
        {
            ensureDocFile();

            for (ScrollListPane.ISlot rootSlot : categories)
            {
                CategorySlot categorySlot = (CategorySlot) rootSlot;

                if (categorySlot.category == MiniMap2)
                {
                    continue;
                }
                doc(categorySlot);
                docTable(true);

                categorySlot.sort();
                for (SlotMetadata childSlot : categorySlot.getAllChildMetadata())
                {
                    doc(childSlot, categorySlot.getCategory() == Advanced);
                }
                docTable(false);
            }

            endDoc();
        }

        return categories;
    }

    protected static void addSlots(HashMap<Category, List<SlotMetadata>> mergedMap, Category inheritedCategory, PropertiesBase properties)
    {
        Class<? extends PropertiesBase> propertiesClass = properties.getClass();
        HashMap<Category, List<SlotMetadata>> slots = buildSlots(null, inheritedCategory, propertiesClass, properties);
        for (Map.Entry<Category, List<SlotMetadata>> entry : slots.entrySet())
        {
            Category category = entry.getKey();
            if (category == Category.Inherit)
            {
                category = inheritedCategory;
            }

            List<SlotMetadata> slotMetadataList = null;
            if (mergedMap.containsKey(category))
            {
                slotMetadataList = mergedMap.get(category);
            }
            else
            {
                slotMetadataList = new ArrayList<SlotMetadata>();
                mergedMap.put(category, slotMetadataList);
            }

            slotMetadataList.addAll(entry.getValue());
        }
    }

    protected static HashMap<Category, List<SlotMetadata>> buildSlots(HashMap<Category, List<SlotMetadata>> map, Category inheritedCategory, Class<? extends PropertiesBase> propertiesClass, PropertiesBase properties)
    {
        if (map == null)
        {
            map = new HashMap<Category, List<SlotMetadata>>();
        }

        for (ConfigField configField : properties.getConfigFields().values())
        {
            if (configField.getCategory() == Category.Hidden)
            {
                continue;
            }

            SlotMetadata slotMetadata = null;

            if (configField instanceof BooleanField)
            {
                slotMetadata = getBooleanSlotMetadata((BooleanField) configField);
            }
            else if (configField instanceof IntegerField)
            {
                slotMetadata = getIntegerSlotMetadata((IntegerField) configField);
            }
            else if (configField instanceof StringField)
            {
                slotMetadata = getStringSlotMetadata((StringField) configField);
            }
            else if (configField instanceof EnumField)
            {
                slotMetadata = getEnumSlotMetadata((EnumField) configField);
            }

            if (slotMetadata != null)
            {
                // Set sort order
                slotMetadata.setOrder(configField.getSortOrder());

                // Determine category
                Category category = configField.getCategory();
                if (category == Category.Inherit)
                {
                    category = inheritedCategory;
                }

                List<SlotMetadata> list = map.get(category);
                if (list == null)
                {
                    list = new ArrayList<SlotMetadata>();
                    map.put(category, list);
                }
                list.add(slotMetadata);
            }
            else
            {
                Journeymap.getLogger().warn(String.format("Unable to create config gui for %s in %s", properties.getClass().getSimpleName(), configField));
            }
        }

//        // Check for parent class
//        Class parentClass = propertiesClass.getSuperclass();
//        if (PropertiesBase.class.isAssignableFrom(parentClass))
//        {
//            map = buildSlots(map, inheritedCategory, (Class<? extends PropertiesBase>) parentClass, properties);
//        }

        return map;
    }

    static String getTooltip(ConfigField configField)
    {
        String tooltipKey = configField.getKey() + ".tooltip";
        String tooltip = Constants.getString(tooltipKey);
        if (tooltipKey.equals(tooltip))
        {
            tooltip = null;
        }
        return tooltip;
    }

    /**
     * Create a slot for a boolean property
     * @param field
     * @return
     */
    static SlotMetadata<Boolean> getBooleanSlotMetadata(BooleanField field)
    {
        String name = Constants.getString(field.getKey());
        String tooltip = getTooltip(field);
        String defaultTip = Constants.getString("jm.config.default", field.getDefaultValue());
        boolean advanced = field.getCategory() == Advanced;

        CheckBox button = new CheckBox(name, field);
        SlotMetadata<Boolean> slotMetadata = new SlotMetadata<Boolean>(button, name, tooltip, defaultTip, field.getDefaultValue(), advanced);
        slotMetadata.setMasterPropertyForCategory(field.isCategoryMaster());
        if (field.isCategoryMaster())
            {
                button.setLabelColors(RGB.CYAN_RGB, null, null);
            }
            return slotMetadata;
    }

    /**
     * Create a slot for an Integer property
     *
     * @param field
     * @return
     */
    static SlotMetadata<Integer> getIntegerSlotMetadata(IntegerField field)
    {
        String name = Constants.getString(field.getKey());
        String tooltip = getTooltip(field);
        String defaultTip = Constants.getString("jm.config.default_numeric", (int) field.getMinValue(), (int) field.getMaxValue(), (int) field.getDefaultValue());
        boolean advanced = field.getCategory() == Advanced;

        IntSliderButton button = new IntSliderButton(field, name + " : ", "", (int) field.getMinValue(), (int) field.getMaxValue(), true);
        button.setDefaultStyle(false);
        button.setDrawBackground(false);
        SlotMetadata<Integer> slotMetadata = new SlotMetadata<Integer>(button, name, tooltip, defaultTip, (int) field.getDefaultValue(), advanced);
        return slotMetadata;
    }

    /**
     * Create a slot for a bound list of strings property
     *
     * @param field
     * @return
     */
    static SlotMetadata<String> getStringSlotMetadata(StringField field)
    {
        try
        {
            String name = Constants.getString(field.getKey());
            String tooltip = getTooltip(field);
            boolean advanced = field.getCategory() == Advanced;

            ListPropertyButton<String> button = null;
            String defaultTip = null;

            // Exception: LocationProperty gets its own button
            if (LocationFormat.IdProvider.class.isAssignableFrom(field.getValuesProviderClass()))
            {
                button = new LocationFormat.Button(field);
                defaultTip = Constants.getString("jm.config.default", ((LocationFormat.Button) button).getLabel(field.getDefaultValue()));
            }
            else
            {
                button = new ListPropertyButton<String>(field.getValidValues(), name, field);
                defaultTip = Constants.getString("jm.config.default", field.getDefaultValue());
            }
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            SlotMetadata<String> slotMetadata = new SlotMetadata<String>(button, name, tooltip, defaultTip, field.getDefaultValue(), advanced);
            slotMetadata.setValueList(field.getValidValues());
            return slotMetadata;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a slot for a bound list of strings property
     *
     * @param field
     * @return
     */
    static SlotMetadata<Enum> getEnumSlotMetadata(EnumField field)
    {
        try
        {
            String name = Constants.getString(field.getKey());
            String tooltip = getTooltip(field);
            boolean advanced = field.getCategory() == Advanced;


            ListPropertyButton<Enum> button = new ListPropertyButton<Enum>(field.getValidValues(), name, field);
            String defaultTip = Constants.getString("jm.config.default", field.getDefaultValue());

            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            SlotMetadata<Enum> slotMetadata = new SlotMetadata<Enum>(button, name, tooltip, defaultTip, field.getDefaultValue(), advanced);
            slotMetadata.setValueList(Arrays.asList(field.getValidValues()));
            return slotMetadata;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static void ensureDocFile()
    {
        if (docFile == null)
        {
            docFile = new File(Constants.JOURNEYMAP_DIR, "journeymap-options-wiki.txt");

            try
            {
                if (docFile.exists())
                {
                    docFile.delete();
                }
                Files.createParentDirs(docFile);

                docWriter = Files.newWriter(docFile, UTF8);
                docWriter.append(String.format("<!-- Generated %s -->", new Date()));
                docWriter.newLine();
                docWriter.append("=== Overview ===");
                docWriter.newLine();
                docWriter.append("{{version|5.0.0|page}}");
                docWriter.newLine();
                docWriter.append("This page lists all of the available options which can be configured in-game using the JourneyMap [[Options Manager]].");
                docWriter.append("(Note: All of this information can also be obtained from the tooltips within the [[Options Manager]] itself.) <br clear/> <br clear/>");
                docWriter.newLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    static void doc(CategorySlot categorySlot)
    {
        try
        {
            docWriter.newLine();
            docWriter.append(String.format("==%s==", categorySlot.getCategory().getName().replace("Preset 1", "Preset (1 and 2)")));
            docWriter.newLine();
            docWriter.append(String.format("''%s''", categorySlot.getMetadata().iterator().next().tooltip.replace("Preset 1", "Preset (1 and 2)")));
            docWriter.newLine();
            docWriter.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void docTable(boolean start)
    {
        try
        {
            if (start)
            {
                docWriter.append("{| class=\"wikitable\" style=\"cellpadding=\"4\"");
                docWriter.newLine();
                docWriter.append("! scope=\"col\" | Option");
                docWriter.newLine();
                docWriter.append("! scope=\"col\" | Purpose");
                docWriter.newLine();
                docWriter.append("! scope=\"col\" | Range / Default Value");
                docWriter.newLine();
                docWriter.append("|-");
                docWriter.newLine();
            }
            else
            {
                docWriter.append("|}");
                docWriter.newLine();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void doc(SlotMetadata slotMetadata, boolean advanced)
    {
        try
        {
            String color = advanced ? "red" : "black";
            docWriter.append(String.format("| style=\"text-align:right; white-space: nowrap; font-weight:bold; padding:6px; color:%s\" | %s", color, slotMetadata.getName()));
            docWriter.newLine();
            docWriter.append(String.format("| %s ", slotMetadata.tooltip));
            if (slotMetadata.getValueList() != null)
            {
                docWriter.append(String.format("<br/><em>Choices available:</em> <code>%s</code>", Joiner.on(", ").join(slotMetadata.getValueList())));
            }
            docWriter.newLine();
            docWriter.append(String.format("| <code>%s</code>", slotMetadata.range.replace("[", "").replace("]", "").trim()));
            docWriter.newLine();
            docWriter.append("|-");
            docWriter.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void endDoc()
    {
        try
        {
            docFile = null;
            docWriter.flush();
            docWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
