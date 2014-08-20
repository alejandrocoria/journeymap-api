/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.map;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.io.MapSaver;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.task.MapRegionTask;
import net.techbrew.journeymap.task.SaveMapTask;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import org.apache.logging.log4j.Level;


public class MapOverlayActions extends JmUI
{

    Button buttonAutomap, buttonSave, buttonClose, buttonBrowser, buttonCheck;

    ;

    public MapOverlayActions()
    {
        super(Constants.getString("jm.common.actions"));
    }

    public static void launchLocalhost()
    {
        String url = "http://localhost:" + JourneyMap.getInstance().webMapProperties.port.get(); //$NON-NLS-1$
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (IOException e)
        {
            JourneyMap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e)); //$NON-NLS-1$
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        String on = Constants.getString("jm.common.on");
        String off = Constants.getString("jm.common.off");

        buttonSave = new Button(ButtonEnum.Save, Constants.getString("jm.common.save_map")); //$NON-NLS-1$
        buttonClose = new Button(ButtonEnum.Close, Constants.getString("jm.common.close")); //$NON-NLS-1$
        buttonBrowser = new Button(ButtonEnum.Browser, Constants.getString("jm.common.use_browser")); //$NON-NLS-1$
        buttonBrowser.setEnabled(JourneyMap.getInstance().webMapProperties.enabled.get());

        buttonAutomap = new Button(ButtonEnum.Automap,
                Constants.getString("jm.common.automap_title", on),
                Constants.getString("jm.common.automap_title", off),
                true); //$NON-NLS-1$ //$NON-NLS-2$
        buttonAutomap.setEnabled(FMLClientHandler.instance().getClient().isSingleplayer());

        buttonCheck = new Button(ButtonEnum.Check, Constants.getString("jm.common.update_check")); //$NON-NLS-1$

        buttonList.add(buttonAutomap);
        buttonList.add(buttonSave);
        buttonList.add(buttonCheck);
        buttonList.add(buttonBrowser);

        new ButtonList(buttonList).equalizeWidths(getFontRenderer());

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

        buttonSave.setEnabled(!JourneyMap.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class));

        final int hgap = 4;
        final int vgap = 3;
        final int bx = (this.width) / 2;
        final int by = this.height / 4;

        buttonAutomap.leftOf(bx - 2).setY(by);
        buttonBrowser.rightOf(buttonAutomap, hgap).setY(by);
        buttonSave.below(buttonAutomap, vgap).leftOf(bx - 2);
        buttonCheck.below(buttonBrowser, vgap).rightOf(buttonSave, hgap);
        buttonClose.below(buttonSave, vgap * 4).centerHorizontalOn(bx);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
        switch (id)
        {
            case Save:
            {
                save();
                UIManager.getInstance().openMap();
                break;
            }
            case Close:
            {
                UIManager.getInstance().openMap();
                break;
            }
            case Browser:
            {
                launchLocalhost();
                UIManager.getInstance().openMap();
                break;
            }
            case Automap:
            {
                UIManager.getInstance().open(AutoMapConfirmation.class);
                break;
            }
            case Check:
            {
                VersionCheck.launchWebsite();
                UIManager.getInstance().openMap();
                break;
            }
        }
    }

    void save()
    {
        MapProperties mapProperties = JourneyMap.getInstance().fullMapProperties;
        final MapOverlayState state = MapOverlay.state();
        boolean showCaves = JourneyMap.getInstance().fullMapProperties.showCaves.get();
        final MapType mapType = state.getMapType(showCaves);
        final Integer vSlice = state.getMapType(showCaves) == MapType.underground ? state.getVSlice() : null;
        final MapSaver mapSaver = new MapSaver(state.getWorldDir(), mapType, vSlice, state.getDimension());
        if (mapSaver.isValid())
        {
            JourneyMap.getInstance().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
            ChatLog.announceI18N("jm.common.save_filename", mapSaver.getSaveFileName()); //$NON-NLS-1$
        }
        close();
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                UIManager.getInstance().openMap();
                break;
            }
        }
    }


    private enum ButtonEnum
    {
        Automap, Check, Save, Browser, Close
    }

}
