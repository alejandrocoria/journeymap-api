/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.dialog;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.forge.event.KeyEventHandler;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.JmUI;

import java.awt.*;

public class FullscreenHotkeysHelp extends JmUI
{
    private int lastWidth = 0;
    private int lastHeight = 0;
    private Button buttonClose;

    private KeyEventHandler keyEventHandler;

    public FullscreenHotkeysHelp(JmUI returnDisplay)
    {
        super(Constants.getString("jm.fullscreen.hotkeys_title"), returnDisplay);
        keyEventHandler = new KeyEventHandler();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        buttonClose = new Button(Constants.getString("jm.common.close")); //$NON-NLS-1$
        buttonClose.setWidth(150);

        buttonList.add(buttonClose);

    }

    /**
     * Center buttons in UI.
     */
    @Override
    protected void layoutButtons()
    {
        // Buttons

        if (buttonList.isEmpty())
        {
            initGui();
        }

        if (lastWidth != width || lastHeight != height)
        {

            lastWidth = width;
            lastHeight = height;
            final int by = (this.height / 4) + 60;
            buttonClose.centerHorizontalOn(this.width / 2).setY(by);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton == buttonClose)
        {
            closeAndReturn();
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1, par2, par3);

        // Title
        int y = this.height / 4 - 18;

        // Hotkey help
        y += 12;
        final int x = (this.width) / 2;
        drawHelpStrings(Constants.getString("key.journeymap.map_toggle_alt"), Constants.getKeyName(Constants.KB_MAP), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.zoom_in"), Constants.getKeyName(Constants.KB_MAP_ZOOMIN), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.zoom_out"), Constants.getKeyName(Constants.KB_MAP_ZOOMOUT), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.day"), Constants.getKeyName(Constants.KB_MAP_DAY), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.night"), Constants.getKeyName(Constants.KB_MAP_NIGHT), x, y += 12);
        drawHelpStrings(Constants.getString("jm.fullscreen.hotkeys_north"), Constants.getKeyName(mc.gameSettings.keyBindForward), x, y += 12);
        drawHelpStrings(Constants.getString("jm.fullscreen.hotkeys_west"), Constants.getKeyName(mc.gameSettings.keyBindLeft), x, y += 12);
        drawHelpStrings(Constants.getString("jm.fullscreen.hotkeys_south"), Constants.getKeyName(mc.gameSettings.keyBindBack), x, y += 12);
        drawHelpStrings(Constants.getString("jm.fullscreen.hotkeys_east"), Constants.getKeyName(mc.gameSettings.keyBindRight), x, y += 12);
        drawHelpStrings(Constants.getString("jm.fullscreen.hotkeys_waypoint"), Constants.getString("jm.fullscreen.hotkeys_doubleclick"), x, y += 12);
        buttonClose.setY(y + 16);
    }

    protected void drawHelpStrings(String title, String key, int x, int y)
    {
        int hgap = 8;
        int tWidth = getFontRenderer().getStringWidth(title);
        drawString(getFontRenderer(), title, x - tWidth - hgap, y, 16777215);

        drawString(getFontRenderer(), key, x + hgap, y, Color.YELLOW.getRGB());
    }
}
