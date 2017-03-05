/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.properties;

import com.google.common.base.Objects;

/**
 * Category for organizing ConfigFields in the Options Manager.
 */
public class Category implements Comparable<Category>
{
    /**
     * The constant Inherit.
     */
// Category used to group a field within a category specified elsewhere
    public static final Category Inherit = new Category("Inherit", 0, "", "");

    /**
     * The constant Hidden.
     */
// Category used to indicate the field shouldn't be visible in a UI
    public static final Category Hidden = new Category("Hidden", 0, "", "");

    /**
     * The Name.
     */
    String name;
    /**
     * The Label.
     */
    String label;
    /**
     * The Tooltip.
     */
    String tooltip;
    /**
     * The Order.
     */
    int order;

    /**
     * Instantiates a new Category.
     *
     * @param name    the name
     * @param order   the order
     * @param label   the label
     * @param tooltip the tooltip
     */
    public Category(String name, int order, String label, String tooltip)
    {
        this.name = name;
        this.order = order;
        this.label = label;
        this.tooltip = tooltip;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return label == null ? getName() : label;
    }

    /**
     * Gets tooltip.
     *
     * @return the tooltip
     */
    public String getTooltip()
    {
        return tooltip == null ? getLabel() : tooltip;
    }

    /**
     * Gets order.
     *
     * @return the order
     */
    public int getOrder()
    {
        return order;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Category))
        {
            return false;
        }
        Category category = (Category) o;
        return Objects.equal(getName(), category.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(getName());
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int compareTo(Category o)
    {
        int result = Integer.compare(order, o.order);
        if (result == 0)
        {
            result = name.compareTo(o.name);
        }
        return result;
    }
}
