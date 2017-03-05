/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.waypoint;

import journeymap.client.ui.component.OnOffButton;
import net.minecraft.client.Minecraft;

/**
 * @author techbrew 10/12/2014.
 */
class SortButton extends OnOffButton
{
    /**
     * The Sort.
     */
    final WaypointManagerItem.Sort sort;
    /**
     * The Label inactive.
     */
    final String labelInactive;

    /**
     * Instantiates a new Sort button.
     *
     * @param label the label
     * @param sort  the sort
     */
    public SortButton(String label, WaypointManagerItem.Sort sort)
    {
        super(String.format("%s %s", label, WaypointManager.ASCEND), String.format("%s %s", label, WaypointManager.DESCEND), sort.ascending);
        this.labelInactive = label;
        this.sort = sort;
    }

    @Override
    public void toggle()
    {
        sort.ascending = !sort.ascending;
        setActive(true);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        super.drawButton(minecraft, mouseX, mouseY);
        super.drawUnderline();
    }

    /**
     * Sets active.
     *
     * @param active the active
     */
    public void setActive(boolean active)
    {
        if (active)
        {
            setToggled(sort.ascending);
        }
        else
        {
            displayString = String.format("%s %s", labelInactive, " ");
        }
    }
}
