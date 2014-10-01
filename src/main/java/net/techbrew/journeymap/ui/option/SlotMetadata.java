package net.techbrew.journeymap.ui.option;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.ui.component.Button;

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
    protected boolean master;

    public SlotMetadata(Button button, String name, String tooltip, boolean advanced)
    {
        this(button, name, tooltip, null, null, advanced);
    }

    public SlotMetadata(Button button, String name, String tooltip, String range, T defaultValue)
    {
        this(button, name, tooltip, range, defaultValue, false);
    }

    public SlotMetadata(Button button, String name, String tooltip, String range, T defaultValue, boolean advanced)
    {
        this.button = button;
        this.name = name;
        this.tooltip = tooltip;
        this.range = range;
        this.defaultValue = defaultValue;
        this.advanced = advanced;
        if (defaultValue instanceof Boolean)
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

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public String[] getTooltip()
    {
        ArrayList<String> lines = new ArrayList<String>(4);
        if (this.tooltip != null || this.range != null || this.defaultValue != null || advanced)
        {
            lines.add((advanced ? EnumChatFormatting.RED : EnumChatFormatting.AQUA) + this.name);
            if (this.tooltip != null)
            {
                FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
                int lineWidth = 200;
                if (this.range != null)
                {
                    lineWidth = Math.max(lineWidth, fontRenderer.getStringWidth(this.range));
                }

                List<String> tooltipList = fontRenderer.listFormattedStringToWidth(tooltip, lineWidth);
                for (String tooltipLine : tooltipList)
                {
                    lines.add(EnumChatFormatting.YELLOW + tooltipLine);
                }
            }
//            if (this.defaultValue != null)
//            {
//                lines.add(EnumChatFormatting.GRAY + this.defaultValue.toString());
//            }
            if (this.range != null)
            {
                lines.add(EnumChatFormatting.WHITE + this.range);
            }
        }

        return lines.toArray(new String[lines.size()]);
    }

    @Override
    public int compareTo(SlotMetadata other)
    {
        int result = Boolean.compare(other.master, this.master);

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
        Boolean, Set, Integer
    }
}