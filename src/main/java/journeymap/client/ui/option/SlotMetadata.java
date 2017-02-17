/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
 * Created by Mark on 9/29/2014.
 */
public class SlotMetadata<T> implements Comparable<SlotMetadata>
{
    protected final Button button;
    protected final String range;
    protected final T defaultValue;
    protected final ValueType valueType;
    protected String name;
    protected String tooltip;
    protected boolean advanced;
    protected String[] tooltipLines;
    protected List valueList;
    protected boolean master;
    protected int order;

    public SlotMetadata(Button button)
    {
        this(button, false);
    }

    public SlotMetadata(Button button, int order)
    {
        this(button, false);
        this.order = order;
    }

    public SlotMetadata(Button button, boolean advanced)
    {
        this(button, button.displayString, button.getUnformattedTooltip(), null, null, advanced);
    }

    public SlotMetadata(Button button, String name, String tooltip, boolean advanced)
    {
        this(button, name, tooltip, null, null, advanced);
    }

    public SlotMetadata(Button button, String name, String tooltip)
    {
        this(button, name, tooltip, null, null, false);
    }

    public SlotMetadata(Button button, String name, String tooltip, int order)
    {
        this(button, name, tooltip, null, null, false);
        this.order = order;
    }

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

    public boolean isMasterPropertyForCategory()
    {
        return this.master;
    }

    public void setMasterPropertyForCategory(boolean master)
    {
        this.master = master;
    }

    public Button getButton()
    {
        return button;
    }

    public String getName()
    {
        return name;
    }

    public String getRange()
    {
        return range;
    }

    public boolean isAdvanced()
    {
        return advanced;
    }

    public void setAdvanced(boolean advanced)
    {
        this.advanced = advanced;
    }

    public ValueType getValueType()
    {
        return valueType;
    }

    public String[] getTooltipLines()
    {
        return tooltipLines;
    }

    public boolean isMaster()
    {
        return master;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public boolean isToolbar()
    {
        return valueType == ValueType.Toolbar;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public List getValueList()
    {
        return valueList;
    }

    public void setValueList(List valueList)
    {
        this.valueList = valueList;
    }

    public void updateFromButton()
    {
        if (button != null)
        {
            name = button.displayString;
            tooltip = button.getUnformattedTooltip();
            tooltipLines = null;
        }
    }

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

    public boolean hasConfigField()
    {
        return button != null && button instanceof IConfigFieldHolder && ((IConfigFieldHolder) button).getConfigField() != null;
    }

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

    public enum ValueType
    {
        Boolean, Set, Integer, Toolbar
    }
}
