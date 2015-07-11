/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.waypoint;

import net.minecraft.client.Minecraft;
import journeymap.client.ui.component.OnOffButton;

/**
 * Created by Mark on 10/12/2014.
 */
class SortButton extends OnOffButton
{
    final WaypointManagerItem.Sort sort;
    final String labelInactive;

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
