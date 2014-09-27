package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 9/26/2014.
 */
public class ConfigManagerFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraftInstance)
    {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return ConfigManager.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }

    static List<IConfigElement> getConfigElements()
    {
        List<BaseConfigElement> root = new ArrayList<BaseConfigElement>();
        List<ConfigCategoryElement> categories = new ArrayList<ConfigCategoryElement>();

        addConfigElements(root, categories, Config.Category.MiniMap, JourneyMap.getMiniMapProperties());
        //categories.get(0).getChildElements().add(0, new ConfigCategoryElement(Config.Category.MiniMap, null).setConfigEntryClass(new IConfigEt))

        addConfigElements(root, categories, Config.Category.FullMap, JourneyMap.getFullMapProperties());
        addConfigElements(root, categories, Config.Category.WebMap, JourneyMap.getWebMapProperties());
        addConfigElements(root, categories, Config.Category.Waypoint, JourneyMap.getWaypointProperties());
        addConfigElements(root, categories, Config.Category.Advanced, JourneyMap.getCoreProperties());

        Collections.sort(categories);

        int count = root.size();

        for (DummyConfigElement.DummyCategoryElement categoryElement : categories)
        {
            count += categoryElement.getChildElements().size();
            Collections.sort(categoryElement.getChildElements());
            categoryElement.setCustomListEntryClass(JmConfigCategoryEntry.class);
        }

        List<IConfigElement> elements = new ArrayList<IConfigElement>(categories.size());
        elements.addAll(root);
        elements.addAll(categories);

        JourneyMap.getLogger().info("Configurable properties: " + count);


        return elements;
    }

    protected static void addConfigElements(List<BaseConfigElement> root, List<ConfigCategoryElement> categories, Config.Category inheritedCategory, PropertiesBase properties)
    {
        Class<? extends PropertiesBase> propertiesClass = properties.getClass();
        for (Map.Entry<Config.Category, List<BaseConfigElement>> entry : buildConfigElements(null, inheritedCategory, propertiesClass, properties).entrySet())
        {
            Config.Category category = entry.getKey();
            if (category == Config.Category.General)
            {
                for (BaseConfigElement val : entry.getValue())
                {
                    root.add(val);
                }
            }
            else
            {
                if (category == Config.Category.Inherit)
                {
                    category = inheritedCategory;
                }

                categories.add(new ConfigCategoryElement(category, entry.getValue()));
            }
        }
    }

    protected static HashMap<Config.Category, List<BaseConfigElement>> buildConfigElements(HashMap<Config.Category, List<BaseConfigElement>> map, Config.Category inheritedCategory, Class<? extends PropertiesBase> propertiesClass, PropertiesBase properties)
    {
        if (map == null)
        {
            map = new HashMap<Config.Category, List<BaseConfigElement>>();
        }
        for (Field field : propertiesClass.getDeclaredFields())
        {
            if (field.isAnnotationPresent(Config.class))
            {
                Config config = field.getAnnotation(Config.class);
                BaseConfigElement fieldConfig = null;

                if (field.getType().equals(AtomicBoolean.class))
                {
                    fieldConfig = BooleanConfigElement.create(properties, field);
                }
                else if (field.getType().equals(AtomicInteger.class))
                {
                    fieldConfig = IntegerConfigElement.create(properties, field);
                }
                else if (field.getType().equals(AtomicReference.class))
                {
                    if (!config.stringListProvider().equals(Config.NoStringProvider.class))
                    {
                        fieldConfig = StringConfigElement.create(properties, field);
                    }
                    else
                    {
                        fieldConfig = EnumConfigElement.create(properties, field);
                    }
                }

                if (fieldConfig != null)
                {
                    Config.Category category = config.category();
                    if (category == Config.Category.Inherit)
                    {
                        category = inheritedCategory;
                    }

                    List<BaseConfigElement> list = map.get(category);
                    if (list == null)
                    {
                        list = new ArrayList<BaseConfigElement>();
                        map.put(category, list);
                    }
                    list.add(fieldConfig);
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
            map = buildConfigElements(map, inheritedCategory, (Class<? extends PropertiesBase>) parentClass, properties);
        }

        return map;
    }

    /**
     * This custom list entry provides a button that will open to a screen that will allow a user to define a new mod override.
     */
    public static class JmConfigCategoryEntry extends GuiConfigEntries.CategoryEntry
    {
        public JmConfigCategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
        {
            super(owningScreen, owningEntryList, prop);
        }

        /**
         * This method is called in the constructor and is used to set the childScreen field.
         */
        protected GuiScreen buildChildScreen()
        {
            return new ConfigManager(this.owningScreen, this.configElement.getChildElements(), this.owningScreen.modID,
                    owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                    owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(), this.owningScreen.title,
                    ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name));
        }
    }
}
