/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.component;

import journeymap.client.JourneymapClient;
import journeymap.client.log.LogFormatter;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;

/**
 * Created by Mark on 10/2/2014.
 */
public class OnOffButton extends Button
{
    protected Boolean toggled = true;
    protected String labelOn;
    protected String labelOff;
    protected ArrayList<ToggleListener> toggleListeners = new ArrayList<ToggleListener>(0);

    public OnOffButton(String labelOn, String labelOff, boolean toggled)
    {
        this(0, labelOn, labelOff, toggled);
    }

    public OnOffButton(int id, String labelOn, String labelOff, boolean toggled)
    {
        super(toggled ? labelOn : labelOff);
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
        return isEnabled() && toggled;
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
        if (this.toggled == toggled || !this.isEnabled() || !this.visible)
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
            JourneymapClient.getLogger().error("Error trying to toggle button '" + displayString + "': " + LogFormatter.toString(t));
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
        public boolean onToggle(OnOffButton button, boolean toggled);
    }
}

