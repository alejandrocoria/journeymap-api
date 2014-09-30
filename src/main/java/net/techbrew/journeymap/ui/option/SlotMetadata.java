package net.techbrew.journeymap.ui.option;

import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.ui.component.Button;

import java.util.ArrayList;

/**
 * Created by Mark on 9/29/2014.
 */
public class SlotMetadata<T>
{
    protected final Button button;
    protected final String name;
    protected final String tooltip;
    protected final String range;
    protected final T defaultValue;
    protected final boolean advanced;

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
                lines.add(EnumChatFormatting.YELLOW + this.tooltip);
            }
            if (this.tooltip != null)
            {
                lines.add(EnumChatFormatting.GRAY + this.defaultValue.toString());
            }
            if (this.range != null)
            {
                lines.add(EnumChatFormatting.WHITE + this.range);
            }
        }

        return lines.toArray(new String[lines.size()]);
    }
}
