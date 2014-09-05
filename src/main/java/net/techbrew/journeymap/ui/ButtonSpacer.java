package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

/**
 * Created by Mark on 9/2/2014.
 */
public class ButtonSpacer extends Button
{
    public ButtonSpacer()
    {
        super(Integer.MIN_VALUE, "");
    }

    public ButtonSpacer(int size)
    {
        super(Integer.MIN_VALUE, size, size, "");
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
