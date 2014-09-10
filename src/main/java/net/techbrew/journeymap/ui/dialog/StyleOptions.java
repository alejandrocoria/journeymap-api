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
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.properties.CoreProperties;
import net.techbrew.journeymap.task.MapPlayerTask;
import net.techbrew.journeymap.ui.component.*;

public class StyleOptions extends JmUI
{
    boolean refreshChunks = false;
    boolean refreshBlockMetadata = false;
    Button buttonClose;
    ButtonList leftButtons = new ButtonList();
    ButtonList rightButtons = new ButtonList();

    public StyleOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("jm.common.map_style_title"), returnClass);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.leftButtons.clear();
        this.rightButtons.clear();

        int id = 0;

        CoreProperties core = JourneyMap.getCoreProperties();

        leftButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_bathymetry", core, core.mapBathymetry));
        leftButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_transparency", core, core.mapTransparency));
        leftButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_cavelighting", core, core.mapCaveLighting));
        leftButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_caveshowsurface", core, core.mapSurfaceAboveCaves));

        buttonList.addAll(leftButtons);

        rightButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_antialiasing", core, core.mapAntialiasing));
        rightButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_crops", core, core.mapCrops));
        rightButtons.add(BooleanPropertyButton.create(id++, "jm.common.map_style_plants", core, core.mapPlants));

        BooleanPropertyButton buttonMapPlantShadows = BooleanPropertyButton.create(id++, "jm.common.map_style_plantshadows", core, core.mapPlantShadows);
        buttonMapPlantShadows.addToggleListener(new Button.ToggleListener()
        {
            @Override
            public boolean onToggle(Button button, boolean toggled)
            {
                refreshBlockMetadata = true;
                return true;
            }
        });

        rightButtons.add(buttonMapPlantShadows);
        buttonList.addAll(rightButtons);

        ButtonList all = new ButtonList(leftButtons);
        all.addAll(rightButtons);
        all.equalizeWidths(getFontRenderer());

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
        int by = (this.height - (leftButtons.getHeight(vgap) + buttonClose.getHeight() + (4*vgap)))/2;

        leftButtons.layoutVertical((this.width/2)-hgap, by, false, vgap);
        rightButtons.layoutVertical((this.width/2)+hgap, by, true, vgap);

        int closeY = Math.min(height - vgap - buttonClose.getHeight(), leftButtons.getBottomY() + (4 * vgap));
        buttonClose.centerHorizontalOn(width / 2).setY(closeY);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button instanceof BooleanPropertyButton || button instanceof IconSetButton)
        {
            ((Button) button).toggle();
            refreshChunks = true;
            return;
        }

        if (button == buttonClose)
        {
            closeAndReturn();
        }
    }

    @Override
    protected void closeAndReturn()
    {
        if (refreshBlockMetadata)
        {
            JourneyMap.getLogger().debug("Refreshing block metadata after map style changes.");
            DataCache.instance().resetBlockMetadata();
        }

        if (refreshChunks)
        {
            JourneyMap.getLogger().debug("Chunks need to remap after map style changes.");
            MapPlayerTask.forceNearbyRemap();
        }

        super.closeAndReturn();
    }

    private enum ButtonEnum
    {
        Close
    }

}
