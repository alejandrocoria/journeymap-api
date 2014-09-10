/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.dialog;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.properties.CoreProperties;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.*;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.theme.Theme;
import net.techbrew.journeymap.ui.theme.ThemeButton;
import net.techbrew.journeymap.ui.theme.ThemeToolbar;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GeneralDisplayOptions extends JmUI
{
    int commonButtonHeight = 20;
    int bottomLineY = 0;
    ThemeToolbar themeToolbar;
    ThemeButton themeExampleButton;
    IconSetButton buttonTheme;
    ButtonSpacer themeSpacer;
    Button buttonCaves, buttonGrid, buttonClose;
    String labelOn = Constants.getString("jm.common.on");
    String labelOff = Constants.getString("jm.common.off");
    String labelFullMap = Constants.getString("jm.fullscreen.title");
    String labelMiniMap = Constants.getString("jm.minimap.title");
    ArrayList<ButtonList> leftRows = new ArrayList<ButtonList>();
    ArrayList<ButtonList> rightRows = new ArrayList<ButtonList>();
    ButtonList rowMobs, rowAnimals, rowVillagers, rowPets, rowGrid, rowTheme, rowCaves, rowSelf, rowPlayers, rowWaypoints, rowFontSize, rowForceUnicode, rowTextureSize;
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

        commonButtonHeight = 20;
        bottomLineY = 0;

        CoreProperties core = JourneyMap.getCoreProperties();
        FullMapProperties fullMap = JourneyMap.getFullMapProperties();
        MiniMapProperties miniMap = JourneyMap.getMiniMapProperties();

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
        buttonCaves.setEnabled(FeatureManager.isAllowed(Feature.MapCaves));

        rowCaves = new ButtonList(Constants.getString("jm.common.show_caves", ""));
        rowCaves.add(buttonCaves);
        leftRows.add(rowCaves);

        buttonGrid = BooleanPropertyButton.create(id++, fullMap, fullMap.showGrid);
        rowGrid = new ButtonList(Constants.getString("jm.common.show_grid", ""));
        rowGrid.add(buttonGrid);
        leftRows.add(rowGrid);

        buttonTheme = (new IconSetButton(id++, core, core.themeName, ThemeFileHandler.getThemeNames(), "%s"));
        rowTheme = new ButtonList(Constants.getString("jm.common.ui_theme", ""));
        //rowTheme.add(buttonTheme);
        themeSpacer = new ButtonSpacer();
        rowTheme.add(themeSpacer);
        leftRows.add(rowTheme);

        Theme theme = ThemeFileHandler.getCurrentTheme();
        String[] tooltips = new String[]{EnumChatFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author)};
        themeExampleButton = new ThemeButton(id, theme, theme.name, "options");
        themeExampleButton.setAdditionalTooltips(Arrays.asList(tooltips));

        themeToolbar = new ThemeToolbar(id++, ThemeFileHandler.getCurrentTheme(), themeExampleButton);
        themeToolbar.addAllButtons(this);
        //themeToolbar.add(buttonTheme);

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
        rowWaypoints.setEnabled(JourneyMap.getWaypointProperties().managerEnabled.get());
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
        rowIconSets.add(new IconSetButton(id++, fullMap, fullMap.entityIconSetName, IconSetFileHandler.getEntityIconSetNames(), "%s"));
        rowIconSets.add(new IconSetButton(id++, miniMap, miniMap.entityIconSetName, IconSetFileHandler.getEntityIconSetNames(), "%s"));
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
        if (buttonClose.getWidth() < 100)
        {
            buttonClose.setWidth(100);
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
        final int by = Math.max(50, (this.height - (150)) / 2);

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
            int labelMiddle = lastRow.getTopY() + ((lastRow.getBottomY()-lastRow.getTopY())/2);
            DrawUtil.drawLabel(row.getLabel(), row.getLeftX() - hgap, labelMiddle, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, Color.black, 0, Color.cyan, 255, 1, true, 0);
            leftX = Math.min(leftX, row.getLeftX() - hgap - getFontRenderer().getStringWidth(row.getLabel()));
            bottomY = Math.max(bottomY, row.getBottomY());
        }

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

            int labelMiddle = lastRow.getTopY() + ((lastRow.getBottomY()-lastRow.getTopY())/2);
            DrawUtil.drawLabel(row.getLabel(), row.getLeftX() - hgap, labelMiddle, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, Color.black, 0, Color.cyan, 255, 1, true, 0);
            rightX = Math.max(rightX, row.getRightX());
            bottomY = Math.max(bottomY, row.getBottomY());
        }

        topY -= 5;
        bottomY += 10;
        leftX -= 5;
        rightX += 5;

        if (rightX - leftX > width)
        {
            commonButtonHeight = 13;
            int commonWidth = leftRows.get(0).get(0).getWidth() - 4;
            for (ButtonList buttonList : leftRows)
            {
                buttonList.setWidths(commonWidth);
                buttonList.setHeights(commonButtonHeight);
            }

            for (ButtonList buttonList : rightRows)
            {
                buttonList.setWidths(commonWidth);
                buttonList.setHeights(commonButtonHeight);
            }

            rowCaves.setWidths(rowAnimals.getWidth(4));
            rowGrid.setWidths(rowAnimals.getWidth(4));

            layoutButtons();
            return;
        }

        themeSpacer.setWidth(rowAnimals.getWidth(4));
        themeToolbar.layoutCenteredHorizontal(themeSpacer.getCenterX(), rowGrid.getBottomY() + themeToolbar.getVMargin(), true, themeToolbar.getToolbarSpec().padding);
        bottomY = Math.max(bottomY, themeToolbar.getBottomY() + themeToolbar.getVMargin());

        bottomLineY = Math.max(bottomY, bottomLineY);
        int closeY = Math.min(height - vgap - buttonClose.getHeight(), bottomLineY + (4 * vgap));

        if(closeY<bottomY && commonButtonHeight==20)
        {
            commonButtonHeight = 13;
            for (ButtonList buttonList : leftRows)
            {
                buttonList.setHeights(commonButtonHeight);
            }

            for (ButtonList buttonList : rightRows)
            {
                buttonList.setHeights(commonButtonHeight);
            }

            layoutButtons();
            return;
        }

        buttonClose.centerHorizontalOn(width/2).setY(closeY);

        DrawUtil.drawLabel(labelFullMap, leftRows.get(0).get(0).getRightX(), by - 10, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, Color.black, 0, Color.white, 255, 1, true);
        DrawUtil.drawCenteredLabel(labelMiniMap, leftRows.get(0).get(1).getCenterX(), by - 10, Color.black, 0, Color.lightGray, 255, 1);

        DrawUtil.drawLabel(labelFullMap, rightRows.get(0).get(0).getRightX(), by - 10, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, Color.black, 0, Color.white, 255, 1, true);
        DrawUtil.drawCenteredLabel(labelMiniMap, rightRows.get(0).get(1).getCenterX(), by - 10, Color.black, 0, Color.lightGray, 255, 1);

        DrawUtil.drawRectangle(leftX, topY, rightX - leftX, 1, Color.lightGray, 150);
        DrawUtil.drawRectangle(leftX, bottomLineY, rightX - leftX, 1, Color.lightGray, 150);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button instanceof BooleanPropertyButton || button instanceof IconSetButton || button instanceof ThemeButton)
        {
            ((Button) button).toggle();

            if(button == themeExampleButton)
            {
                buttonTheme.toggle();
            }

            if(button == buttonTheme || button == themeExampleButton)
            {
                Theme theme = ThemeFileHandler.getCurrentTheme();
                String[] tooltips = new String[]{EnumChatFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author)};
                themeExampleButton.displayString = theme.name;
                themeExampleButton.setAdditionalTooltips(Arrays.asList(tooltips));
                themeExampleButton.updateTheme(theme);
                themeToolbar.updateTheme(theme);

                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
            }
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
