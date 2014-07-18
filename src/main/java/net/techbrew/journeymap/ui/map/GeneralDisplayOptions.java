/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.Button;

import java.awt.*;
import java.util.ArrayList;

public class GeneralDisplayOptions extends JmUI
{

    Button buttonCaves, buttonGrid, buttonClose;
    String labelOn = Constants.getString("jm.common.on");
    String labelOff = Constants.getString("jm.common.off");
    String labelFullMap = Constants.getString("jm.fullscreen.title");
    String labelMiniMap = Constants.getString("jm.minimap.title");
    ArrayList<ButtonList> leftRows = new ArrayList<ButtonList>();
    ArrayList<ButtonList> rightRows = new ArrayList<ButtonList>();
    ButtonList rowMobs, rowAnimals, rowVillagers, rowPets, rowGrid, rowCaves, rowSelf, rowPlayers, rowWaypoints, rowFontSize, rowForceUnicode, rowTextureSize;
    ButtonList rowIconSets;
    public GeneralDisplayOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("jm.common.general_display_title"), returnClass);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.leftRows.clear();
        this.rightRows.clear();

        int id = 0;

        String on = Constants.getString("jm.common.on");
        String off = Constants.getString("jm.common.off");

        FullMapProperties fullMap = JourneyMap.getInstance().fullMapProperties;
        MiniMapProperties miniMap = JourneyMap.getInstance().miniMapProperties;

        rowMobs = new ButtonList(Constants.getString("jm.common.show_monsters", ""));
        rowMobs.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showMobs));
        rowMobs.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showMobs));
        rowMobs.setEnabled(FeatureManager.isAllowed(Feature.RadarMobs));
        leftRows.add(rowMobs);

        rowAnimals = new ButtonList(Constants.getString("jm.common.show_animals", ""));
        rowAnimals.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showAnimals));
        rowAnimals.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showAnimals));
        rowAnimals.setEnabled(FeatureManager.isAllowed(Feature.RadarAnimals));
        leftRows.add(rowAnimals);

        rowVillagers = new ButtonList(Constants.getString("jm.common.show_villagers", ""));
        rowVillagers.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showVillagers));
        rowVillagers.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showVillagers));
        rowVillagers.setEnabled(FeatureManager.isAllowed(Feature.RadarVillagers));
        leftRows.add(rowVillagers);

        rowPets = new ButtonList(Constants.getString("jm.common.show_pets", ""));
        rowPets.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showPets));
        rowPets.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showPets));
        rowPets.setEnabled(FeatureManager.isAllowed(Feature.RadarAnimals));
        leftRows.add(rowPets);

        buttonCaves = BooleanPropertyButton.create(id++, fullMap, fullMap.showCaves);
        buttonCaves.setEnabled(FeatureManager.isAllowed(Feature.RadarPlayers));

        rowCaves = new ButtonList(Constants.getString("jm.common.show_caves", ""));
        rowCaves.add(buttonCaves);
        leftRows.add(rowCaves);

        buttonGrid = BooleanPropertyButton.create(id++, fullMap, fullMap.showGrid);
        rowGrid = new ButtonList(Constants.getString("jm.common.show_grid", ""));
        rowGrid.add(buttonGrid);
        leftRows.add(rowGrid);

        rowSelf = new ButtonList(Constants.getString("jm.common.show_self", ""));
        rowSelf.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showSelf));
        rowSelf.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showSelf));
        rightRows.add(rowSelf);

        rowPlayers = new ButtonList(Constants.getString("jm.common.show_players", ""));
        rowPlayers.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showPlayers));
        rowPlayers.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showPlayers));
        rowPlayers.setEnabled(FeatureManager.isAllowed(Feature.RadarPlayers));
        rightRows.add(rowPlayers);

        rowWaypoints = new ButtonList(Constants.getString("jm.common.show_waypoints", ""));
        rowWaypoints.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showWaypoints));
        rowWaypoints.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showWaypoints));
        rowWaypoints.setEnabled(JourneyMap.getInstance().waypointProperties.managerEnabled.get());
        rightRows.add(rowWaypoints);

        rowForceUnicode = new ButtonList(Constants.getString("jm.minimap.force_unicode", ""));
        rowForceUnicode.add(BooleanPropertyButton.create(id++, fullMap, fullMap.forceUnicode));
        rowForceUnicode.add(BooleanPropertyButton.create(id++, miniMap, miniMap.forceUnicode));
        rightRows.add(rowForceUnicode);

        rowFontSize = new ButtonList(Constants.getString("jm.common.font", ""));
        rowFontSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, fullMap, fullMap.fontSmall));
        rowFontSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, miniMap, miniMap.fontSmall));
        rightRows.add(rowFontSize);

        rowTextureSize = new ButtonList(Constants.getString("jm.minimap.texture_size", ""));
        rowTextureSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, fullMap, fullMap.textureSmall));
        rowTextureSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, miniMap, miniMap.textureSmall));
        rightRows.add(rowTextureSize);

        rowIconSets = new ButtonList(Constants.getString("jm.common.mob_icon_set", ""));
        rowIconSets.add(new IconSetButton(id++, fullMap, "%s"));
        rowIconSets.add(new IconSetButton(id++, miniMap, "%s"));
        rightRows.add(rowIconSets);

        int commonWidth = getFontRenderer().getStringWidth(labelOn);
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelOff));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelFullMap));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelMiniMap));
        commonWidth += 4;

        for (ButtonList ButtonList : leftRows)
        {
            ButtonList.setWidths(commonWidth);
            buttonList.addAll(ButtonList);
        }

        for (ButtonList ButtonList : rightRows)
        {
            ButtonList.setWidths(commonWidth);
            buttonList.addAll(ButtonList);
        }

        rowCaves.setWidths(rowAnimals.getWidth(4));
        rowGrid.setWidths(rowAnimals.getWidth(4));

        buttonClose = new Button(ButtonEnum.Close.ordinal(), 0, 0, Constants.getString("jm.common.close")); //$NON-NLS-1$
        buttonClose.fitWidth(getFontRenderer());
        if (buttonClose.getWidth() < 150)
        {
            buttonClose.setWidth(150);
        }
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

        final int hgap = 4;
        final int vgap = 3;
        int rowWidth = 0;
        int rowLabelWidth = 0;
        int spacer = 20;

        for (ButtonList row : leftRows)
        {
            rowLabelWidth = Math.max(rowLabelWidth, getFontRenderer().getStringWidth(row.getLabel()));
            rowWidth = Math.max(rowWidth, row.getWidth(hgap));

        }

        for (ButtonList row : rightRows)
        {
            rowLabelWidth = Math.max(rowLabelWidth, getFontRenderer().getStringWidth(row.getLabel()));
            rowWidth = Math.max(rowWidth, row.getWidth(hgap));
        }

        int bx = ((this.width - ((rowWidth * 2) + (rowLabelWidth * 2)) - spacer) / 2) + rowLabelWidth;
        final int by = Math.max(50, (this.height - (140)) / 2);

        int leftX, rightX, topY, bottomY;
        leftX = width;
        rightX = 0;
        topY = by - 20;
        bottomY = 0;

        ButtonList lastRow = null;
        for (ButtonList row : leftRows)
        {
            if (lastRow == null)
            {
                row.layoutHorizontal(bx, by, true, hgap);
            }
            else
            {
                row.layoutHorizontal(bx, lastRow.getBottomY() + vgap, true, hgap);
            }
            lastRow = row;
            DrawUtil.drawLabel(row.getLabel(), row.getLeftX() - hgap, lastRow.getTopY() + vgap, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.black, 0, Color.cyan, 255, 1, true);
            leftX = Math.min(leftX, row.getLeftX() - hgap - getFontRenderer().getStringWidth(row.getLabel()));
            bottomY = Math.max(bottomY, row.getBottomY());
        }

        DrawUtil.drawCenteredLabel(labelFullMap, leftRows.get(0).get(0).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);
        DrawUtil.drawCenteredLabel(labelMiniMap, leftRows.get(0).get(1).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);

        lastRow = null;
        bx = leftRows.get(0).getRightX() + spacer + rowLabelWidth;
        for (ButtonList row : rightRows)
        {
            if (lastRow == null)
            {
                row.layoutHorizontal(bx, by, true, hgap);
            }
            else
            {
                row.layoutHorizontal(bx, lastRow.getBottomY() + vgap, true, hgap);
            }
            lastRow = row;
            DrawUtil.drawLabel(row.getLabel(), row.getLeftX() - hgap, lastRow.getTopY() + vgap, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.black, 0, Color.cyan, 255, 1, true);
            rightX = Math.max(rightX, row.getRightX());
            bottomY = Math.max(bottomY, row.getBottomY());
        }

        DrawUtil.drawCenteredLabel(labelFullMap, rightRows.get(0).get(0).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);
        DrawUtil.drawCenteredLabel(labelMiniMap, rightRows.get(0).get(1).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);

        topY -= 5;
        bottomY += 10;
        leftX -= 5;
        rightX += 5;
        DrawUtil.drawRectangle(leftX, topY, rightX - leftX, 1, Color.lightGray, 150);
        DrawUtil.drawRectangle(leftX, bottomY, rightX - leftX, 1, Color.lightGray, 150);

        if (rightX - leftX > width)
        {
            int commonWidth = leftRows.get(0).get(0).getWidth() - 4;
            for (ButtonList ButtonList : leftRows)
            {
                ButtonList.setWidths(commonWidth);
            }

            for (ButtonList ButtonList : rightRows)
            {
                ButtonList.setWidths(commonWidth);
            }

            rowCaves.setWidths(rowAnimals.getWidth(4));
            rowGrid.setWidths(rowAnimals.getWidth(4));
        }

        int closeY = Math.min(height - vgap - buttonClose.getHeight(), bottomY + (4 * vgap));
        buttonClose.centerHorizontalOn(width / 2).setY(closeY);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button instanceof BooleanPropertyButton || button instanceof IconSetButton)
        {
            ((Button) button).toggle();
            return;
        }

        if (button == buttonClose)
        {
            closeAndReturn();
        }
    }

    private enum ButtonEnum
    {
        Caves, Grid, Close
    }

}
