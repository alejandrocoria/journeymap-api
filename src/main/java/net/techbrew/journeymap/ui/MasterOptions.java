/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.map.MapOverlayActions;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.awt.*;

/**
 * Master options UI
 */
public class MasterOptions extends JmUI
{
    String titleGeneral = Constants.getString("jm.common.general_display");
    String titleMiniMap = Constants.getString("jm.minimap.title");
    String titleWaypoints = Constants.getString("jm.waypoint.display_management");
    String titleWebmap = Constants.getString("jm.webmap.title");
    String labelOptions = Constants.getString("jm.common.options_button");
    String labelHelp = Constants.getString("jm.common.help");
    String labelStyle = Constants.getString("jm.common.map_style_button");
    Button buttonGeneralDisplayOptions, buttonFullMapHelp;
    Button buttonMiniMapEnable, buttonMiniMapOptions, buttonMiniMapHelp;
    Button buttonWaypointOptions, buttonWaypointHelp, buttonWaypointManagerEnable;
    Button buttonWebMapEnable, buttonWebMapOpen;
    Button buttonStyleOptions;
    Button buttonClose;
    ButtonList listGeneral, listMiniMap, listWaypoints, listWebMap;

    public MasterOptions()
    {
        super(Constants.getString("jm.common.options"));
    }

    @Override
    public void initGui()
    {
        buttonList.clear();

        buttonGeneralDisplayOptions = ButtonEnum.FullMapOptions.create(labelOptions);
        buttonFullMapHelp = ButtonEnum.FullMapHelp.create(labelHelp);
        buttonStyleOptions = ButtonEnum.StyleOptions.create(labelStyle);
        listGeneral = new ButtonList(buttonGeneralDisplayOptions, buttonStyleOptions, buttonFullMapHelp);
        buttonList.addAll(listGeneral);

        buttonMiniMapOptions = ButtonEnum.MiniMapOptions.create(labelOptions);
        buttonMiniMapHelp = ButtonEnum.MiniMapHelp.create(labelHelp);
        buttonMiniMapEnable = BooleanPropertyButton.create(ButtonEnum.MiniMapEnable.ordinal(), BooleanPropertyButton.Type.OnOff, "jm.common.enable",
                JourneyMap.getInstance().miniMapProperties, JourneyMap.getInstance().miniMapProperties.enabled);
        listMiniMap = new ButtonList(buttonMiniMapOptions, buttonMiniMapEnable, buttonMiniMapHelp);
        buttonList.addAll(listMiniMap);

        buttonWaypointOptions = ButtonEnum.WaypointOptions.create(labelOptions);
        buttonWaypointHelp = ButtonEnum.WaypointHelp.create(labelHelp);
        buttonWaypointManagerEnable = BooleanPropertyButton.create(ButtonEnum.WaypointManagerEnable.ordinal(), BooleanPropertyButton.Type.OnOff, "jm.waypoint.enable_manager",
                JourneyMap.getInstance().waypointProperties, JourneyMap.getInstance().waypointProperties.managerEnabled);
        listWaypoints = new ButtonList(buttonWaypointOptions, buttonWaypointManagerEnable, buttonWaypointHelp);
        buttonList.addAll(listWaypoints);

        buttonWebMapEnable = BooleanPropertyButton.create(ButtonEnum.WebMapEnable.ordinal(), BooleanPropertyButton.Type.OnOff, "jm.webmap.enable",
                JourneyMap.getInstance().webMapProperties, JourneyMap.getInstance().webMapProperties.enabled);
        buttonWebMapOpen = ButtonEnum.WebMapOpen.create(Constants.getString("jm.common.use_browser"));
        listWebMap = new ButtonList(buttonWebMapOpen, buttonWebMapEnable);
        buttonList.addAll(listWebMap);

        new ButtonList(buttonList).equalizeWidths(getFontRenderer());

        buttonClose = ButtonEnum.Close.create(Constants.getString("jm.common.close"));
        buttonClose.setWidth(150);
        buttonList.add(buttonClose);
    }

    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initGui();
        }

        final int hgap = 4;
        final int vgap = (getFontRenderer().FONT_HEIGHT * 2) + 4;

        int listWidth = listGeneral.getWidth(hgap);
        listWidth = Math.max(listWidth, listMiniMap.getWidth(hgap));
        listWidth = Math.max(listWidth, listWaypoints.getWidth(hgap));
        listWidth = Math.max(listWidth, listWebMap.getWidth(hgap));

        final int bx = (this.width - listWidth) / 2;
        int by = Math.max(40, ((this.height - (5 * 30)) / 2));

        DrawUtil.drawRectangle(bx - 5, by - 20, listWidth + 10, 1, Color.lightGray, 150);

        // Full Map
        DrawUtil.drawLabel(titleGeneral, bx, by, DrawUtil.HAlign.Right, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, false);
        listGeneral.layoutHorizontal(bx, by, true, hgap);
        by = listGeneral.getBottomY() + vgap;

        // Mini Map
        DrawUtil.drawLabel(titleMiniMap, bx, by, DrawUtil.HAlign.Right, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, false);
        listMiniMap.layoutHorizontal(bx, by, true, hgap);
        by = listMiniMap.getBottomY() + vgap;
        buttonMiniMapOptions.setEnabled(buttonMiniMapEnable.getToggled());

        // Waypoints
        DrawUtil.drawLabel(titleWaypoints, bx, by, DrawUtil.HAlign.Right, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, false);
        listWaypoints.layoutHorizontal(bx, by, true, hgap);
        by = listWaypoints.getBottomY() + vgap;
        buttonWaypointOptions.setEnabled(buttonWaypointManagerEnable.getToggled());

        // Web Map
        DrawUtil.drawLabel(titleWebmap, bx, by, DrawUtil.HAlign.Right, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, false);
        listWebMap.layoutHorizontal(bx, by, true, hgap);
        by = listWebMap.getBottomY() + 20;
        buttonWebMapOpen.setEnabled(buttonWebMapEnable.getToggled());

        DrawUtil.drawRectangle(bx - 5, by - 10, listWidth + 10, 1, Color.lightGray, 150);

        // Close
        buttonClose.centerHorizontalOn(width / 2).setY(by);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        super.drawScreen(x, y, par3);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button instanceof BooleanPropertyButton)
        {
            ((BooleanPropertyButton) button).toggle();
        }

        final ButtonEnum id = ButtonEnum.values()[button.id];
        switch (id)
        {
            case FullMapOptions:
            {
                UIManager.getInstance().openGeneralDisplayOptions(getClass());
                break;
            }
            case FullMapHelp:
            {
                UIManager.getInstance().openMapHotkeyHelp(getClass());
                break;
            }
            case MiniMapOptions:
            {
                UIManager.getInstance().openMiniMapOptions(getClass());
                break;
            }
            case MiniMapHelp:
            {
                UIManager.getInstance().openMiniMapHotkeyHelp(getClass());
                break;
            }
            case StyleOptions:
            {
                UIManager.getInstance().openStyleOptions(getClass());
                break;
            }
            case WaypointOptions:
            {
                UIManager.getInstance().openWaypointOptions(getClass());
                break;
            }
            case WaypointManagerEnable:
            {
                if (JourneyMap.getInstance().waypointProperties.managerEnabled.get())
                {
                    WaypointStore.instance().load();
                }
                else
                {
                    WaypointStore.instance().reset();
                }
                break;
            }
            case WaypointHelp:
            {
                UIManager.getInstance().openWaypointHelp(getClass());
                break;
            }
            case WebMapOpen:
            {
                MapOverlayActions.launchLocalhost();
                break;
            }
            case WebMapEnable:
            {
                JourneyMap.getInstance().toggleWebserver(buttonWebMapEnable.getToggled(), true);
                break;
            }
            case Close:
            {
                UIManager.getInstance().openMap();
                break;
            }
        }
    }

    @Override
    protected void closeAndReturn()
    {
        if (returnClass == null)
        {
            UIManager.getInstance().openMap();
        }
        else
        {
            UIManager.getInstance().open(returnClass);
        }
    }

    enum ButtonEnum
    {
        FullMapOptions, FullMapHelp, MiniMapOptions, MiniMapHelp, StyleOptions, WaypointOptions, WaypointHelp, MiniMapEnable, WaypointManagerEnable, WebMapEnable, WebMapOpen, Close;

        Button create(String label)
        {
            return new Button(this, label);
        }
    }
}
