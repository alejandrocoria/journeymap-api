package net.techbrew.journeymap.ui.option;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.ui.component.CheckBox;
import net.techbrew.journeymap.ui.component.ScrollListPane;
import net.techbrew.journeymap.ui.component.SliderButton2;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 9/29/2014.
 */
public class OptionSlotFactory
{
    public static List<ScrollListPane.ISlot> getSlots()
    {
        CategorySlot root = new CategorySlot(Config.Category.General);
        List<CategorySlot> categories = new ArrayList<CategorySlot>();

        addSlots(root, categories, Config.Category.MiniMap, JourneyMap.getMiniMapProperties());
        //categories.get(0).getChildElements().add(0, new ConfigCategoryElement(Config.Category.MiniMap, null).setConfigEntryClass(new IConfigEt))

        addSlots(root, categories, Config.Category.FullMap, JourneyMap.getFullMapProperties());
        addSlots(root, categories, Config.Category.WebMap, JourneyMap.getWebMapProperties());
        addSlots(root, categories, Config.Category.Waypoint, JourneyMap.getWaypointProperties());
        addSlots(root, categories, Config.Category.Advanced, JourneyMap.getCoreProperties());

        Collections.sort(categories);

        int count = root.size();
        root.sort();

        for (CategorySlot categorySlot : categories)
        {
            count += categorySlot.size();
            categorySlot.sort();
        }

        List<ScrollListPane.ISlot> slots = new ArrayList<ScrollListPane.ISlot>(categories.size() + root.size());

        slots.addAll(categories);
        slots.addAll(root.getChildSlots());

        JourneyMap.getLogger().info("Configurable properties: " + count);


        return slots;
    }

    protected static void addSlots(CategorySlot root, List<CategorySlot> categories, Config.Category inheritedCategory, PropertiesBase properties)
    {
        Class<? extends PropertiesBase> propertiesClass = properties.getClass();
        for (Map.Entry<Config.Category, List<SlotMetadata>> entry : buildSlots(null, inheritedCategory, propertiesClass, properties).entrySet())
        {
            Config.Category category = entry.getKey();
            if (category == Config.Category.General)
            {
                for (SlotMetadata val : entry.getValue())
                {
                    root.add(new ButtonListSlot().add(val));
                }
            }
            else
            {
                if (category == Config.Category.Inherit)
                {
                    category = inheritedCategory;
                }
                CategorySlot categorySlot = new CategorySlot(category);
                for (SlotMetadata val : entry.getValue())
                {
                    categorySlot.add(new ButtonListSlot().add(val));
                }
                categories.add(categorySlot);
            }
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
                else if (field.getType().equals(AtomicReference.class))
                {
                    if (!config.stringListProvider().equals(Config.NoStringProvider.class))
                    {
                        // TODO
                        //slotMetadata = StringConfigElement.create(properties, field);
                    }
                    else
                    {
                        // TODO
                        //slotMetadata = EnumConfigElement.create(properties, field);
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
            return slotMetadata;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static SlotMetadata<Integer> getIntegerSlotMetadata(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicInteger property = (AtomicInteger) field.get(properties);
            String name = getName(annotation);
            String tooltip = getTooltip(annotation);
            String defaultTip = Constants.getString("jm.config.default_numeric", annotation.minInt(), annotation.maxInt(), annotation.defaultInt());
            boolean advanced = annotation.category() == Config.Category.Advanced;

            SliderButton2 button = new SliderButton2(0, properties, property, name + ": ", "", annotation.minInt(), annotation.maxInt(), true);
            SlotMetadata<Integer> slotMetadata = new SlotMetadata<Integer>(button, name, tooltip, defaultTip, annotation.defaultInt(), advanced);
            return slotMetadata;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
