/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.component;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

/**
 * Created by Mark on 9/2/2014.
 */
public class ButtonSpacer extends Button
{
    public ButtonSpacer()
    {
        super("");
    }

    public ButtonSpacer(int size)
    {
        super(size, size, "");
    }

    public void drawPartialScrollable(Minecraft minecraft, int x, int y, int width, int height)
    {
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float f)
    {
    }

    @Override
    public void drawUnderline()
    {
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        return false;
    }

    @Override
    public ArrayList<String> getTooltip()
    {
        return null;
    }

    @Override
    public boolean mouseOver(int mouseX, int mouseY)
    {
        return false;
    }
}
