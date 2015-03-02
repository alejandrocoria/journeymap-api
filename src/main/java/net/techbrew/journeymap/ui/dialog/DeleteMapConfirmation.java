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
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.task.MapPlayerTask;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.JmUI;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import org.lwjgl.input.Keyboard;

public class DeleteMapConfirmation extends JmUI
{

    Button buttonAll, buttonCurrent, buttonNone, buttonClose;

    public DeleteMapConfirmation()
    {
        super(Constants.getString("jm.common.deletemap_dialog"));
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        buttonList.clear();

        buttonAll = new Button(Constants.getString("jm.common.deletemap_dialog_all"));
        buttonCurrent = new Button(Constants.getString("jm.common.deletemap_dialog_this"));
        buttonNone = new Button(Constants.getString("jm.common.deletemap_dialog_none"));
        buttonClose = new Button(Constants.getString("jm.common.close"));

        buttonList.add(buttonAll);
        buttonList.add(buttonCurrent);
        buttonList.add(buttonNone);
        buttonList.add(buttonClose);
    }

    @Override
    protected void layoutButtons()
    {

        if (this.buttonList.isEmpty())
        {
            this.initGui();
        }

        final int x = this.width / 2;
        final int y = this.height / 4;
        final int vgap = 3;

        this.drawCenteredString(getFontRenderer(), Constants.getString("jm.common.deletemap_dialog_text"), x, y, 16777215);

        buttonAll.centerHorizontalOn(x).setY(y + 18);
        buttonCurrent.centerHorizontalOn(x).below(buttonAll, vgap);
        buttonNone.centerHorizontalOn(x).below(buttonCurrent, vgap);
        buttonClose.centerHorizontalOn(x).below(buttonNone, vgap * 4);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton == buttonAll || guibutton == buttonCurrent)
        {
            RegionImageCache.instance().deleteMap(Fullscreen.state(), guibutton == buttonAll);
            MapPlayerTask.forceNearbyRemap();
            Fullscreen.reset();
            UIManager.getInstance().openFullscreenMap();
            return;
        }

        if (guibutton == buttonNone || guibutton == buttonClose)
        {
            UIManager.getInstance().openMapActions();
            return;
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                UIManager.getInstance().openMapActions();
                break;
            }
        }
    }
}
