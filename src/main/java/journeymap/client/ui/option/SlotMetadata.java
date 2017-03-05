/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.option;

import journeymap.client.Constants;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.IConfigFieldHolder;
import journeymap.client.ui.component.IntSliderButton;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.ConfigField;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author techbrew 9/29/2014.
 *
 * @param <T> the type parameter
 */
public class SlotMetadata<T> implements Comparable<SlotMetadata>
{
    /**
     * The Button.
     */
    protected final Button button;
    /**
     * The Range.
     */
    protected final String range;
    /**
     * The Default value.
     */
    protected final T defaultValue;
    /**
     * The Value type.
     */
    protected final ValueType valueType;
    /**
     * The Name.
     */
    protected String name;
    /**
     * The Tooltip.
     */
    protected String tooltip;
    /**
     * The Advanced.
     */
    protected boolean advanced;
    /**
     * The Tooltip lines.
     */
    protected String[] tooltipLines;
    /**
     * The Value list.
     */
    protected List valueList;
    /**
     * The Master.
     */
    protected boolean master;
    /**
     * The Order.
     */
    protected int order;

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button the button
     */
    public SlotMetadata(Button button)
    {
        this(button, false);
    }

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button the button
     * @param order  the order
     */
    public SlotMetadata(Button button, int order)
    {
        this(button, false);
        this.order = order;
    }

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button   the button
     * @param advanced the advanced
     */
    public SlotMetadata(Button button, boolean advanced)
    {
        this(button, button.displayString, button.getUnformattedTooltip(), null, null, advanced);
    }

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button   the button
     * @param name     the name
     * @param tooltip  the tooltip
     * @param advanced the advanced
     */
    public SlotMetadata(Button button, String name, String tooltip, boolean advanced)
    {
        this(button, name, tooltip, null, null, advanced);
    }

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button  the button
     * @param name    the name
     * @param tooltip the tooltip
     */
    public SlotMetadata(Button button, String name, String tooltip)
    {
        this(button, name, tooltip, null, null, false);
    }

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button  the button
     * @param name    the name
     * @param tooltip the tooltip
     * @param order   the order
     */
    public SlotMetadata(Button button, String name, String tooltip, int order)
    {
        this(button, name, tooltip, null, null, false);
        this.order = order;
    }

    /**
     * Instantiates a new Slot metadata.
     *
     * @param button       the button
     * @param name         the name
     * @param tooltip      the tooltip
     * @param range        the range
     * @param defaultValue the default value
     * @param advanced     the advanced
     */
    public SlotMetadata(Button button, String name, String tooltip, String range, T defaultValue, boolean advanced)
    {
        this.button = button;
        this.name = name;
        this.tooltip = tooltip;
        this.range = range;
        this.defaultValue = defaultValue;
        this.advanced = advanced;

        if (defaultValue == null && range == null && !advanced)
        {
            valueType = ValueType.Toolbar;
        }
        else if (defaultValue instanceof Boolean)
        {
            valueType = ValueType.Boolean;
        }
        else if (defaultValue instanceof Integer)
        {
            valueType = ValueType.Integer;
        }
        else
        {
            valueType = ValueType.Set;
        }
    }

    /**
     * Is master property for category boolean.
     *
     * @return the boolean
     */
    public boolean isMasterPropertyForCategory()
    {
        return this.master;
    }

    /**
     * Sets master property for category.
     *
     * @param master the master
     */
    public void setMasterPropertyForCategory(boolean master)
    {
        this.master = master;
    }

    /**
     * Gets button.
     *
     * @return the button
     */
    public Button getButton()
    {
        return button;
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
     * Gets range.
     *
     * @return the range
     */
    public String getRange()
    {
        return range;
    }

    /**
     * Is advanced boolean.
     *
     * @return the boolean
     */
    public boolean isAdvanced()
    {
        return advanced;
    }

    /**
     * Sets advanced.
     *
     * @param advanced the advanced
     */
    public void setAdvanced(boolean advanced)
    {
        this.advanced = advanced;
    }

    /**
     * Gets value type.
     *
     * @return the value type
     */
    public ValueType getValueType()
    {
        return valueType;
    }

    /**
     * Get tooltip lines string [ ].
     *
     * @return the string [ ]
     */
    public String[] getTooltipLines()
    {
        return tooltipLines;
    }

    /**
     * Is master boolean.
     *
     * @return the boolean
     */
    public boolean isMaster()
    {
        return master;
    }

    /**
     * Gets default value.
     *
     * @return the default value
     */
    public T getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Is toolbar boolean.
     *
     * @return the boolean
     */
    public boolean isToolbar()
    {
        return valueType == ValueType.Toolbar;
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

    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    /**
     * Gets value list.
     *
     * @return the value list
     */
    public List getValueList()
    {
        return valueList;
    }

    /**
     * Sets value list.
     *
     * @param valueList the value list
     */
    public void setValueList(List valueList)
    {
        this.valueList = valueList;
    }

    /**
     * Update from button.
     */
    public void updateFromButton()
    {
        if (button != null)
        {
            name = button.displayString;
            tooltip = button.getUnformattedTooltip();
            tooltipLines = null;
        }
    }

    /**
     * Get tooltip string [ ].
     *
     * @return the string [ ]
     */
    public String[] getTooltip()
    {
        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        String bidiColor = fontRenderer.getBidiFlag() ? "%2$s%1$s" : "%1$s%2$s";

        if (tooltipLines == null)
        {
            ArrayList<TextComponentTranslation> lines = new ArrayList<TextComponentTranslation>(4);
            if (this.tooltip != null || this.range != null || this.defaultValue != null || advanced)
            {
                TextFormatting nameColor = isToolbar() ? TextFormatting.GREEN : (advanced ? TextFormatting.RED : TextFormatting.AQUA);
                lines.add(new TextComponentTranslation("jm.config.tooltip_format", nameColor, this.name));
                if (this.tooltip != null)
                {
                    lines.addAll(getWordWrappedLines(TextFormatting.YELLOW.toString(), this.tooltip));
                }

                if (button != null && button instanceof IntSliderButton)
                {
                    lines.addAll(getWordWrappedLines(TextFormatting.GRAY.toString() + TextFormatting.ITALIC.toString(),
                            Constants.getString("jm.config.control_arrowkeys")));
                }

                if (this.range != null)
                {
                    lines.add(new TextComponentTranslation("jm.config.tooltip_format", TextFormatting.WHITE, this.range));
                }
            }

            if (!lines.isEmpty())
            {
                ArrayList<String> stringLines = new ArrayList<String>();
                for (TextComponentTranslation line : lines)
                {
                    stringLines.add(line.getUnformattedText().trim());
                }
                tooltipLines = stringLines.toArray(new String[stringLines.size()]);
            }
        }
        return tooltipLines;
    }

    /**
     * Gets word wrapped lines.
     *
     * @param color    the color
     * @param original the original
     * @return the word wrapped lines
     */
    protected List<TextComponentTranslation> getWordWrappedLines(String color, String original)
    {
        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        List<TextComponentTranslation> list = new ArrayList<TextComponentTranslation>();

        int max = fontRenderer.getBidiFlag() ? 170 : 250;
        for (Object line : fontRenderer.listFormattedStringToWidth(original, max))
        {
            list.add(new TextComponentTranslation("jm.config.tooltip_format", color, line));
        }
        return list;
    }

    /**
     * Reset to default value.
     */
    public void resetToDefaultValue()
    {
        if (button != null)
        {
            if (button instanceof IConfigFieldHolder)
            {
                try
                {
                    ConfigField configField = ((IConfigFieldHolder) button).getConfigField();
                    if (configField != null)
                    {
                        configField.setToDefault();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            button.refresh();
        }
    }

    /**
     * Has config field boolean.
     *
     * @return the boolean
     */
    public boolean hasConfigField()
    {
        return button != null && button instanceof IConfigFieldHolder && ((IConfigFieldHolder) button).getConfigField() != null;
    }

    /**
     * Gets properties.
     *
     * @return the properties
     */
    public PropertiesBase getProperties()
    {
        if (hasConfigField())
        {
            return ((IConfigFieldHolder) button).getConfigField().getOwner();
        }
        return null;
    }

    @Override
    public int compareTo(SlotMetadata other)
    {
        int result = Boolean.compare(this.isToolbar(), other.isToolbar());

        if (result == 0)
        {
            result = Integer.compare(this.order, other.order);
        }

        if (result == 0)
        {
            result = Boolean.compare(other.master, this.master);
        }

        if (result == 0)
        {
            result = this.valueType.compareTo(other.valueType);
        }

        if (result == 0)
        {
            result = this.name.compareTo(other.name);
        }

        return result;
    }

    /**
     * The enum Value type.
     */
    public enum ValueType
    {
        /**
         * Boolean value type.
         */
        Boolean, /**
     * Set value type.
     */
    Set, /**
     * Integer value type.
     */
    Integer, /**
     * Toolbar value type.
     */
    Toolbar
    }
}
