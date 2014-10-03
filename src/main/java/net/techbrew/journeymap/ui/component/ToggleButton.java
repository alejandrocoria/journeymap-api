package net.techbrew.journeymap.ui.component;

import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.ArrayList;

/**
 * Created by Mark on 10/2/2014.
 */
public class ToggleButton extends Button
{
    protected Boolean toggled = true;
    protected String labelOn;
    protected String labelOff;
    protected ArrayList<ToggleListener> toggleListeners = new ArrayList<ToggleListener>(0);

    public ToggleButton(Enum enumValue, String labelOn, String labelOff, boolean toggled)
    {
        this(enumValue.ordinal(), labelOn, labelOff, toggled);
    }

    public ToggleButton(int id, String labelOn, String labelOff, boolean toggled)
    {
        super(id, toggled ? labelOn : labelOff);
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        this.setToggled(toggled);
        finishInit();
    }

    public void setLabels(String labelOn, String labelOff)
    {
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        updateLabel();
    }

    protected void updateLabel()
    {
        if (labelOn != null && labelOff != null)
        {
            super.displayString = getToggled() ? labelOn : labelOff;
        }
    }

    public void toggle()
    {
        setToggled(!getToggled());
    }

    public int getFitWidth(FontRenderer fr)
    {
        int max = fr.getStringWidth(displayString);
        if (this.labelOn != null)
        {
            max = Math.max(max, fr.getStringWidth(labelOn));
        }
        if (this.labelOff != null)
        {
            max = Math.max(max, fr.getStringWidth(labelOff));
        }
        return max + WIDTH_PAD;
    }

    @Override
    public boolean isActive()
    {
        return enabled && toggled;
    }

    public Boolean getToggled()
    {
        return toggled;
    }

    public void setToggled(Boolean toggled)
    {
        setToggled(toggled, true);
    }

    public void setToggled(Boolean toggled, boolean notifyToggleListener)
    {
        if (this.toggled == toggled || !this.enabled || !this.visible)
        {
            return;
        }

        boolean allowChange = true;
        try
        {
            if (notifyToggleListener && !toggleListeners.isEmpty())
            {
                for (ToggleListener listener : toggleListeners)
                {
                    allowChange = listener.onToggle(this, toggled);
                    if (!allowChange)
                    {
                        break;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Error trying to toggle button '" + displayString + "': " + LogFormatter.toString(t));
            allowChange = false;
        }

        if (allowChange)
        {
            this.toggled = toggled;
            updateLabel();
        }
    }

    public void addToggleListener(ToggleListener toggleListener)
    {
        this.toggleListeners.add(toggleListener);
    }

    public static interface ToggleListener
    {
        public boolean onToggle(ToggleButton button, boolean toggled);
    }
}

