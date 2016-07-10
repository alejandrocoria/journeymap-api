/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.dialog;

import journeymap.client.Constants;
import journeymap.client.io.MapSaver;
import journeymap.client.log.ChatLog;
import journeymap.client.model.BlockMD;
import journeymap.client.model.MapState;
import journeymap.client.model.MapType;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.task.multi.MapRegionTask;
import journeymap.client.task.multi.SaveMapTask;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.BooleanPropertyButton;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.net.URLEncoder;


public class FullscreenActions extends JmUI
{
    protected TextureImpl patreonLogo = TextureCache.instance().getPatreonLogo();

    Button buttonAutomap, buttonSave, buttonAbout, buttonClose, buttonBrowser, buttonCheck, buttonDonate, buttonDeleteMap;
    BooleanPropertyButton buttonEnableMapping;

    public FullscreenActions()
    {
        super(Constants.getString("jm.common.actions"));
    }

    public FullscreenActions(JmUI returnDisplay)
    {
        super(Constants.getString("jm.common.actions"), returnDisplay);
    }


    public static void launchLocalhost()
    {
        String url = "http://localhost:" + Journeymap.getClient().getWebMapProperties().port.get();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (IOException e)
        {
            Journeymap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
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
            Journeymap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
        }
    }

    /**
     * Launch the JourneyMap website in the native OS.
     */
    public static void launchWebsite(String path)
    {
        String url = Journeymap.WEBSITE_URL + path;
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Could not launch browser with URL: " + url, LogFormatter.toString(e)); //$NON-NLS-1$
        }
    }

    /**
     * Suggest a tweet message
     */
    public static void tweet(String message)
    {
        String path = null;
        try
        {
            path = "http://twitter.com/home/?status=@JourneyMapMod+" + URLEncoder.encode(message, "UTF-8");
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(path));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Launch the download website in the native OS.
     */
    public static void launchDownloadWebsite()
    {
        String url = VersionCheck.getDownloadUrl();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Could not launch browser with URL: " + url, LogFormatter.toString(e)); //$NON-NLS-1$
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        buttonAbout = new Button(Constants.getString("jm.common.splash_about"));
        buttonSave = new Button(Constants.getString("jm.common.save_map"));
        buttonClose = new Button(Constants.getString("jm.common.close"));
        buttonBrowser = new Button(Constants.getString("jm.common.use_browser"));
        buttonBrowser.setEnabled(Journeymap.getClient().getWebMapProperties().enabled.get());

        buttonAutomap = new Button(Constants.getString("jm.common.automap_title"));
        buttonAutomap.setTooltip(Constants.getString("jm.common.automap_text"));
        buttonAutomap.setEnabled(FMLClientHandler.instance().getClient().isSingleplayer() && Journeymap.getClient().getCoreProperties().mappingEnabled.get());

        buttonDeleteMap = new Button(Constants.getString("jm.common.deletemap_title"));
        buttonDeleteMap.setTooltip(Constants.getString("jm.common.deletemap_text"));

        buttonDonate = new Button(Constants.getString("jm.webmap.donate_text"));
        buttonDonate.setDefaultStyle(false);
        buttonDonate.setDrawBackground(false);
        buttonDonate.setDrawFrame(false);

        buttonCheck = new Button(Constants.getString("jm.common.update_check"));

        buttonEnableMapping = new BooleanPropertyButton(Constants.getString("jm.common.enable_mapping_false"),
                Constants.getString("jm.common.enable_mapping_true"),
                Journeymap.getClient().getCoreProperties().mappingEnabled);

        buttonList.add(buttonAbout);
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

        buttonSave.setEnabled(!Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class));

        final int hgap = 4;
        final int vgap = 3;
        final int bx = (this.width) / 2;
        int by = this.height / 4;

        buttonAbout.centerHorizontalOn(bx).setY(by);
        by = buttonAbout.getBottomY() + vgap;

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
            UIManager.INSTANCE.openFullscreenMap();
            return;
        }
        if (guibutton == buttonClose)
        {
            UIManager.INSTANCE.openFullscreenMap();
            return;
        }
        if (guibutton == buttonBrowser)
        {
            launchLocalhost();
            UIManager.INSTANCE.openFullscreenMap();
            return;
        }
        if (guibutton == buttonDonate)
        {
            launchPatreon();
            UIManager.INSTANCE.openFullscreenMap();
            return;
        }
        if (guibutton == buttonAutomap)
        {
            UIManager.INSTANCE.open(AutoMapConfirmation.class);
            return;
        }
        if (guibutton == buttonDeleteMap)
        {
            UIManager.INSTANCE.open(DeleteMapConfirmation.class);
            return;
        }
        if (guibutton == buttonCheck)
        {
            launchWebsite("Download");
            UIManager.INSTANCE.openFullscreenMap();
            return;
        }
        if (guibutton == buttonAbout)
        {
            UIManager.INSTANCE.openSplash(this);
            return;
        }
        if (guibutton == buttonEnableMapping)
        {
            buttonEnableMapping.toggle();
            if (Journeymap.getClient().getCoreProperties().mappingEnabled.get())
            {
                UIManager.INSTANCE.openFullscreenMap();
                ChatLog.announceI18N("jm.common.enable_mapping_true_text");
                return;
            }
            else
            {
                Journeymap.getClient().stopMapping();
                BlockMD.reset();
                ChatLog.announceI18N("jm.common.enable_mapping_false_text");
                UIManager.INSTANCE.openFullscreenMap();
                return;
            }
        }

    }

    void save()
    {
        final MapState state = Fullscreen.state();
        boolean showCaves = Journeymap.getClient().getFullMapProperties().showCaves.get();
        final MapType mapType = state.getMapType(showCaves);
        final MapSaver mapSaver = new MapSaver(state.getWorldDir(), mapType);
        if (mapSaver.isValid())
        {
            Journeymap.getClient().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
            ChatLog.announceI18N("jm.common.save_filename", mapSaver.getSaveFileName());
        }
        closeAndReturn();
    }


    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                closeAndReturn();
                break;
            }
        }
    }
}
