/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

/**
 * @author techbrew 9/2/2014.
 */
public class ButtonSpacer extends Button
{
    /**
     * Instantiates a new Button spacer.
     */
    public ButtonSpacer()
    {
        super("");
    }

    /**
     * Instantiates a new Button spacer.
     *
     * @param size the size
     */
    public ButtonSpacer(int size)
    {
        super(size, size, "");
    }

    public void drawPartialScrollable(Minecraft minecraft, int x, int y, int width, int height)
    {
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
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
