/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.dialog;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.io.MapSaver;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.MapState;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.task.multi.MapRegionTask;
import net.techbrew.journeymap.task.multi.SaveMapTask;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.BooleanPropertyButton;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.ButtonList;
import net.techbrew.journeymap.ui.component.JmUI;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import java.io.IOException;


public class FullscreenActions extends JmUI
{
    protected TextureImpl patreonLogo = TextureCache.instance().getPatreonLogo();

    Button buttonAutomap, buttonSave, buttonClose, buttonBrowser, buttonCheck, buttonDonate, buttonDeleteMap;
    BooleanPropertyButton buttonEnableMapping;

    public FullscreenActions()
    {
        super(Constants.getString("jm.common.actions"));
    }

    public static void launchLocalhost()
    {
        String url = "http://localhost:" + JourneyMap.getWebMapProperties().port.get();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (IOException e)
        {
            JourneyMap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
        }
    }

    public static void launchPatreon()
    {
        String url = "http://patreon.com/techbrew";
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (IOException e)
        {
            JourneyMap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
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

        buttonSave = new Button(Constants.getString("jm.common.save_map"));
        buttonClose = new Button(Constants.getString("jm.common.close"));
        buttonBrowser = new Button(Constants.getString("jm.common.use_browser"));
        buttonBrowser.setEnabled(JourneyMap.getWebMapProperties().enabled.get());

        buttonAutomap = new Button(Constants.getString("jm.common.automap_title"));
        buttonAutomap.setTooltip(Constants.getString("jm.common.automap_text"));
        buttonAutomap.setEnabled(FMLClientHandler.instance().getClient().isSingleplayer() && JourneyMap.getCoreProperties().mappingEnabled.get());

        buttonDeleteMap = new Button(Constants.getString("jm.common.deletemap_title"));
        buttonDeleteMap.setTooltip(Constants.getString("jm.common.deletemap_text"));

        buttonDonate = new Button(Constants.getString("jm.webmap.donate_text"));
        buttonDonate.setDefaultStyle(false);
        buttonDonate.setDrawBackground(false);
        buttonDonate.setDrawFrame(false);

        buttonCheck = new Button(Constants.getString("jm.common.update_check"));

        buttonEnableMapping = new BooleanPropertyButton(Constants.getString("jm.common.enable_mapping_false"),
                Constants.getString("jm.common.enable_mapping_true"),
                JourneyMap.getCoreProperties(),
                JourneyMap.getCoreProperties().mappingEnabled);

        buttonList.add(buttonAutomap);
        buttonList.add(buttonSave);
        buttonList.add(buttonCheck);
        buttonList.add(buttonDonate);
        buttonList.add(buttonBrowser);
        buttonList.add(buttonDeleteMap);
        buttonList.add(buttonEnableMapping);

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

        ButtonList row1 = new ButtonList(buttonAutomap, buttonEnableMapping);
        ButtonList row2 = new ButtonList(buttonSave, buttonDeleteMap);
        ButtonList row3 = new ButtonList(buttonBrowser, buttonCheck);

        row1.layoutCenteredHorizontal(bx, by, true, hgap);
        row2.layoutCenteredHorizontal(bx, row1.getBottomY() + vgap, true, hgap);
        row3.layoutCenteredHorizontal(bx, row2.getBottomY() + vgap, true, hgap);

        int patreonX = bx - 8;
        int patreonY = row2.getBottomY() + 32;
        DrawUtil.drawImage(patreonLogo, patreonX, patreonY, false, .5f, 0);

        buttonDonate.centerHorizontalOn(bx).setY(patreonY + 16);
        buttonClose.below(buttonDonate, vgap * 4).centerHorizontalOn(bx);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        if (guibutton == buttonSave)
        {
            save();
            UIManager.getInstance().openFullscreenMap();
            return;
        }
        if (guibutton == buttonClose)
        {
            UIManager.getInstance().openFullscreenMap();
            return;
        }
        if (guibutton == buttonBrowser)
        {
            launchLocalhost();
            UIManager.getInstance().openFullscreenMap();
            return;
        }
        if (guibutton == buttonDonate)
        {
            launchPatreon();
            UIManager.getInstance().openFullscreenMap();
            return;
        }
        if (guibutton == buttonAutomap)
        {
            UIManager.getInstance().open(AutoMapConfirmation.class);
            return;
        }
        if (guibutton == buttonDeleteMap)
        {
            UIManager.getInstance().open(DeleteMapConfirmation.class);
            return;
        }
        if (guibutton == buttonCheck)
        {
            VersionCheck.launchWebsite();
            UIManager.getInstance().openFullscreenMap();
            return;
        }
        if (guibutton == buttonEnableMapping)
        {
            buttonEnableMapping.toggle();
            if (JourneyMap.getCoreProperties().mappingEnabled.get())
            {
                UIManager.getInstance().openFullscreenMap();
                ChatLog.announceI18N("jm.common.enable_mapping_true_text");
                return;
            }
            else
            {
                JourneyMap.getInstance().stopMapping();
                ChatLog.announceI18N("jm.common.enable_mapping_false_text");
                UIManager.getInstance().openFullscreenMap();
                return;
            }
        }

    }

    void save()
    {
        final MapState state = Fullscreen.state();
        boolean showCaves = JourneyMap.getFullMapProperties().showCaves.get();
        final MapType mapType = state.getMapType(showCaves);
        final Integer vSlice = state.getMapType(showCaves) == MapType.underground ? state.getVSlice() : null;
        final MapSaver mapSaver = new MapSaver(state.getWorldDir(), mapType, vSlice, state.getDimension());
        if (mapSaver.isValid())
        {
            JourneyMap.getInstance().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
            ChatLog.announceI18N("jm.common.save_filename", mapSaver.getSaveFileName());
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
                UIManager.getInstance().openFullscreenMap();
                break;
            }
        }
    }
}
