/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;

/**
 * @author techbrew 10/2/2014.
 */
public class OnOffButton extends Button
{
    /**
     * The Toggled.
     */
    protected Boolean toggled = true;
    /**
     * The Label on.
     */
    protected String labelOn;
    /**
     * The Label off.
     */
    protected String labelOff;
    /**
     * The Toggle listeners.
     */
    protected ArrayList<ToggleListener> toggleListeners = new ArrayList<ToggleListener>(0);

    /**
     * Instantiates a new On off button.
     *
     * @param labelOn  the label on
     * @param labelOff the label off
     * @param toggled  the toggled
     */
    public OnOffButton(String labelOn, String labelOff, boolean toggled)
    {
        this(0, labelOn, labelOff, toggled);
    }

    /**
     * Instantiates a new On off button.
     *
     * @param id       the id
     * @param labelOn  the label on
     * @param labelOff the label off
     * @param toggled  the toggled
     */
    public OnOffButton(int id, String labelOn, String labelOff, boolean toggled)
    {
        super(toggled ? labelOn : labelOff);
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        this.setToggled(toggled);
        finishInit();
    }

    /**
     * Sets labels.
     *
     * @param labelOn  the label on
     * @param labelOff the label off
     */
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

    /**
     * Toggle.
     */
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

    /**
     * Gets toggled.
     *
     * @return the toggled
     */
    public Boolean getToggled()
    {
        return toggled;
    }

    /**
     * Sets toggled.
     *
     * @param toggled the toggled
     */
    public void setToggled(Boolean toggled)
    {
        setToggled(toggled, true);
    }

    /**
     * Sets toggled.
     *
     * @param toggled              the toggled
     * @param notifyToggleListener the notify toggle listener
     */
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
            Journeymap.getLogger().error("Error trying to toggle button '" + displayString + "': " + LogFormatter.toString(t));
            allowChange = false;
        }

        if (allowChange)
        {
            this.toggled = toggled;
            updateLabel();
        }
    }

    /**
     * Add toggle listener.
     *
     * @param toggleListener the toggle listener
     */
    public void addToggleListener(ToggleListener toggleListener)
    {
        this.toggleListeners.add(toggleListener);
    }

    /**
     * The interface Toggle listener.
     */
    public static interface ToggleListener
    {
        /**
         * On toggle boolean.
         *
         * @param button  the button
         * @param toggled the toggled
         * @return the boolean
         */
        public boolean onToggle(OnOffButton button, boolean toggled);
    }
}

