package net.techbrew.journeymap.ui.option;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.IntSliderButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 9/29/2014.
 */
public class SlotMetadata<T> implements Comparable<SlotMetadata>
{
    protected final Button button;
    protected final String name;
    protected final String tooltip;
    protected final String range;
    protected final T defaultValue;
    protected final boolean advanced;
    protected final ValueType valueType;
    protected String[] tooltipLines;
    protected boolean master;

    public SlotMetadata(Button button, String name, String tooltip, boolean advanced)
    {
        this(button, name, tooltip, null, null, advanced);
    }

    public SlotMetadata(Button button, String name, String tooltip)
    {
        this(button, name, tooltip, null, null, false);
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

    public String[] getTooltip()
    {
        if (tooltipLines == null)
        {
            ArrayList<String> lines = new ArrayList<String>(4);
            if (this.tooltip != null || this.range != null || this.defaultValue != null || advanced)
            {
                EnumChatFormatting nameColor = isToolbar() ? EnumChatFormatting.GREEN : (advanced ? EnumChatFormatting.RED : EnumChatFormatting.AQUA);
                lines.add(nameColor + this.name);
                if (this.tooltip != null)
                {
                    lines.addAll(getWordWrappedLines(EnumChatFormatting.YELLOW.toString(), this.tooltip));
                }

                if (button instanceof IntSliderButton)
                {
                    lines.addAll(getWordWrappedLines(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC,
                            Constants.getString("jm.config.control_arrowkeys")));
                }

                if (this.range != null)
                {
                    lines.add(EnumChatFormatting.WHITE + this.range);
                }
            }

            if (!lines.isEmpty())
            {
                tooltipLines = lines.toArray(new String[lines.size()]);
            }
        }
        return tooltipLines;
    }

    protected List<String> getWordWrappedLines(String color, String original)
    {
        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        List<String> list = new ArrayList<String>();

        for (Object line : fontRenderer.listFormattedStringToWidth(original, 250))
        {
            list.add(color + line);
        }
        return list;
    }

    @Override
    public int compareTo(SlotMetadata other)
    {
        int result = 0;//Boolean.compare(this.isToolbar(), other.isToolbar());

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
