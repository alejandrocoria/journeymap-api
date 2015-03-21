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
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.task.multi.MapRegionTask;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.JmUI;
import org.lwjgl.input.Keyboard;

public class AutoMapConfirmation extends JmUI
{

    Button buttonAll, buttonMissing, buttonNone, buttonClose;

    public AutoMapConfirmation()
    {
        super(Constants.getString("jm.common.automap_dialog"));
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        buttonList.clear();

        buttonAll = new Button(Constants.getString("jm.common.automap_dialog_all"));
        buttonMissing = new Button(Constants.getString("jm.common.automap_dialog_missing"));
        buttonNone = new Button(Constants.getString("jm.common.automap_dialog_none"));
        buttonClose = new Button(Constants.getString("jm.common.close"));

        boolean enable = !JourneyMap.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class);
        buttonAll.setEnabled(enable);
        buttonMissing.setEnabled(enable);
        buttonNone.setEnabled(!enable);

        buttonList.add(buttonAll);
        buttonList.add(buttonMissing);
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

        this.drawCenteredString(getFontRenderer(), Constants.getString("jm.common.automap_dialog_text"), x, y, 16777215);

        buttonAll.centerHorizontalOn(x).setY(y + 18);
        buttonMissing.centerHorizontalOn(x).below(buttonAll, vgap);
        buttonNone.centerHorizontalOn(x).below(buttonMissing, vgap);
        buttonClose.centerHorizontalOn(x).below(buttonNone, vgap * 4);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton == buttonAll)
        {
            JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.TRUE);
        }
        if (guibutton == buttonMissing)
        {
            JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.FALSE);
        }
        if (guibutton == buttonNone)
        {
            JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, false, null);
        }
        if (guibutton == buttonClose)
        {
        }

        UIManager.getInstance().openFullscreenMap();
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
