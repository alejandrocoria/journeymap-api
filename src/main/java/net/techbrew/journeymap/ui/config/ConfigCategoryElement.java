package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.IConfigElement;
import net.techbrew.journeymap.properties.Config;

import java.util.List;

/**
 * Created by Mark on 9/25/2014.
 */
class ConfigCategoryElement<T> extends DummyConfigElement.DummyCategoryElement<T> implements Comparable<ConfigCategoryElement<T>>
{
    final Config.Category category;

    public ConfigCategoryElement(Config.Category category, List<IConfigElement> childElements)
    {
        super(category.name(), category.key, childElements);
        this.category = category;
    }

    @Override
    public int compareTo(ConfigCategoryElement<T> o)
    {
        return this.category.compareTo(o.category);
    }
}
