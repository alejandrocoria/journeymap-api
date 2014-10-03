package net.techbrew.journeymap.ui.option;

import com.google.common.util.concurrent.AtomicDouble;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.ui.component.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 9/29/2014.
 */
public class OptionSlotFactory
{
    public static List<ScrollListPane.ISlot> getSlots(Map<Config.Category, List<SlotMetadata>> toolbars)
    {
        HashMap<Config.Category, List<SlotMetadata>> mergedMap = new HashMap<Config.Category, List<SlotMetadata>>();

        addSlots(mergedMap, Config.Category.MiniMap, JourneyMap.getMiniMapProperties());
        addSlots(mergedMap, Config.Category.FullMap, JourneyMap.getFullMapProperties());
        addSlots(mergedMap, Config.Category.WebMap, JourneyMap.getWebMapProperties());
        addSlots(mergedMap, Config.Category.Waypoint, JourneyMap.getWaypointProperties());
        addSlots(mergedMap, Config.Category.Advanced, JourneyMap.getCoreProperties());

        List<CategorySlot> categories = new ArrayList<CategorySlot>();
        for (Map.Entry<Config.Category, List<SlotMetadata>> entry : mergedMap.entrySet())
        {
            Config.Category category = entry.getKey();
            CategorySlot categorySlot = new CategorySlot(category);
            for (SlotMetadata val : entry.getValue())
            {
                categorySlot.add(new ButtonListSlot().add(val));
            }

            if (toolbars.containsKey(category))
            {
                ButtonListSlot toolbarSlot = new ButtonListSlot();
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

        JourneyMap.getLogger().info("Configurable properties: " + count);
        return new ArrayList<ScrollListPane.ISlot>(categories);
    }

    protected static void addSlots(HashMap<Config.Category, List<SlotMetadata>> mergedMap, Config.Category inheritedCategory, PropertiesBase properties)
    {
        Class<? extends PropertiesBase> propertiesClass = properties.getClass();
        for (Map.Entry<Config.Category, List<SlotMetadata>> entry : buildSlots(null, inheritedCategory, propertiesClass, properties).entrySet())
        {
            Config.Category category = entry.getKey();
            if (category == Config.Category.Inherit)
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

    protected static HashMap<Config.Category, List<SlotMetadata>> buildSlots(HashMap<Config.Category, List<SlotMetadata>> map, Config.Category inheritedCategory, Class<? extends PropertiesBase> propertiesClass, PropertiesBase properties)
    {
        if (map == null)
        {
            map = new HashMap<Config.Category, List<SlotMetadata>>();
        }
        for (Field field : propertiesClass.getDeclaredFields())
        {
            if (field.isAnnotationPresent(Config.class))
            {
                Config config = field.getAnnotation(Config.class);
                SlotMetadata slotMetadata = null;

                if (field.getType().equals(AtomicBoolean.class))
                {
                    slotMetadata = getBooleanSlotMetadata(properties, field);
                }
                else if (field.getType().equals(AtomicInteger.class))
                {
                    slotMetadata = getIntegerSlotMetadata(properties, field);
                }
                else if (field.getType().equals(AtomicDouble.class))
                {
                    slotMetadata = getDoubleSlotMetadata(properties, field);
                }
                else if (field.getType().equals(AtomicReference.class))
                {
                    if (!config.stringListProvider().equals(Config.NoStringProvider.class))
                    {
                        slotMetadata = getStringSlotMetadata(properties, field);
                    }
                    else
                    {
                        // TODO
                        slotMetadata = getEnumSlotMetadata(properties, field);
                    }
                }

                if (slotMetadata != null)
                {
                    Config.Category category = config.category();
                    if (category == Config.Category.Inherit)
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
                    JourneyMap.getLogger().warn(String.format("Unable to create config gui for %s.%s using %s", properties.getClass().getSimpleName(), field.getName(), config));
                }
            }
        }

        // Check for parent class
        Class parentClass = propertiesClass.getSuperclass();
        if (PropertiesBase.class.isAssignableFrom(parentClass))
        {
            map = buildSlots(map, inheritedCategory, (Class<? extends PropertiesBase>) parentClass, properties);
        }

        return map;
    }

    static String getName(Config annotation)
    {
        return Constants.getString(annotation.key());
    }

    static String getTooltip(Config annotation)
    {
        String tooltipKey = annotation.key() + ".tooltip";
        String tooltip = Constants.getString(tooltipKey);
        if (tooltipKey.equals(tooltip))
        {
            tooltip = null;
        }
        return tooltip;
    }

    /**
     * Create a slot for a boolean property
     *
     * @param properties
     * @param field
     * @return
     */
    static SlotMetadata<Boolean> getBooleanSlotMetadata(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicBoolean property = (AtomicBoolean) field.get(properties);
            String name = getName(annotation);
            String tooltip = getTooltip(annotation);
            String defaultTip = Constants.getString("jm.config.default", annotation.defaultBoolean());
            boolean advanced = annotation.category() == Config.Category.Advanced;

            CheckBox button = new CheckBox(0, name, property, properties);
            SlotMetadata<Boolean> slotMetadata = new SlotMetadata<Boolean>(button, name, tooltip, defaultTip, annotation.defaultBoolean(), advanced);
            slotMetadata.setMasterPropertyForCategory(annotation.master());
            if (annotation.master())
            {
                button.packedFGColour = Color.cyan.getRGB();
            }
            return slotMetadata;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a slot for an Integer property
     *
     * @param properties
     * @param field
     * @return
     */
    static SlotMetadata<Integer> getIntegerSlotMetadata(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicInteger property = (AtomicInteger) field.get(properties);
            String name = getName(annotation);
            String tooltip = getTooltip(annotation);
            String defaultTip = Constants.getString("jm.config.default_numeric", (int) annotation.minValue(), (int) annotation.maxValue(), (int) annotation.defaultValue());
            boolean advanced = annotation.category() == Config.Category.Advanced;

            IntSliderButton button = new IntSliderButton(0, properties, property, name + ": ", "", (int) annotation.minValue(), (int) annotation.maxValue(), true);
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            SlotMetadata<Integer> slotMetadata = new SlotMetadata<Integer>(button, name, tooltip, defaultTip, (int) annotation.defaultValue(), advanced);
            return slotMetadata;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a slot for an Integer property
     *
     * @param properties
     * @param field
     * @return
     */
    static SlotMetadata<Double> getDoubleSlotMetadata(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicDouble property = (AtomicDouble) field.get(properties);
            String name = getName(annotation);
            String tooltip = getTooltip(annotation);
            String defaultTip = Constants.getString("jm.config.default_numeric", annotation.minValue(), annotation.maxValue(), annotation.defaultValue());
            boolean advanced = annotation.category() == Config.Category.Advanced;

            DoubleSliderButton button = new DoubleSliderButton(0, properties, property, name + ": ", "", (double) annotation.minValue(), (double) annotation.maxValue(), true);
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            SlotMetadata<Double> slotMetadata = new SlotMetadata<Double>(button, name, tooltip, defaultTip, (double) annotation.defaultValue(), advanced);
            return slotMetadata;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a slot for a bound list of strings property
     *
     * @param properties
     * @param field
     * @return
     */
    static SlotMetadata<String> getStringSlotMetadata(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicReference<String> property = (AtomicReference<String>) field.get(properties);
            String name = getName(annotation);
            String tooltip = getTooltip(annotation);
            StringListProvider slp = annotation.stringListProvider().newInstance();
            String defaultTip = Constants.getString("jm.config.default", slp.getDefaultString());
            boolean advanced = annotation.category() == Config.Category.Advanced;

            StringPropertyButton button = new StringPropertyButton(0, slp.getStrings(), name, properties, property);
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            SlotMetadata<String> slotMetadata = new SlotMetadata<String>(button, name, tooltip, defaultTip, slp.getDefaultString(), advanced);
            return slotMetadata;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static SlotMetadata<Enum> getEnumSlotMetadata(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicReference<Enum> property = (AtomicReference<Enum>) field.get(properties);
            String name = getName(annotation);
            String tooltip = getTooltip(annotation);
            Class<? extends Enum> enumClass = property.get().getClass();
            EnumSet<?> enumSet = EnumSet.allOf(enumClass);
            String defaultTip = Constants.getString("jm.config.default", Enum.valueOf(enumClass, annotation.defaultEnum()));
            boolean advanced = annotation.category() == Config.Category.Advanced;

            EnumPropertyButton button = new EnumPropertyButton(0, enumSet.toArray(new Enum[enumSet.size()]), name, properties, property);
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            SlotMetadata<Enum> slotMetadata = new SlotMetadata<Enum>(button, name, tooltip, defaultTip, property.get(), advanced);
            return slotMetadata;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
