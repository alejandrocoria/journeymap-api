package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.BooleanPropertyButton;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;

import java.awt.*;
import java.util.ArrayList;

public class MapOverlayOptions extends JmUI
{

    private enum ButtonEnum
    {
        Caves, Grid, Close
    }

    Button buttonCaves, buttonGrid, buttonClose;

    String labelOn = Constants.getString("MapOverlay.on");
    String labelOff = Constants.getString("MapOverlay.off");
    String labelFullMap = Constants.getString("MapOverlay.title");
    String labelMiniMap = Constants.getString("MiniMap.title");

    ArrayList<ButtonRow> leftRows = new ArrayList<ButtonRow>();
    ArrayList<ButtonRow> rightRows = new ArrayList<ButtonRow>();
    ButtonRow rowMobs, rowAnimals, rowVillagers, rowPets, rowGrid, rowCaves, rowSelf, rowPlayers, rowWaypoints, rowFontSize, rowForceUnicode, rowTextureSize;

    public MapOverlayOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("MapOverlay.general_display_title"), returnClass);
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

        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");

        FullMapProperties fullMap = JourneyMap.getInstance().fullMapProperties;
        MiniMapProperties miniMap = JourneyMap.getInstance().miniMapProperties;

        rowMobs = new ButtonRow(Constants.getString("MapOverlay.show_monsters", ""));
        rowMobs.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showMobs));
        rowMobs.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showMobs));
        rowMobs.setEnabled(FeatureManager.isAllowed(Feature.RadarMobs));
        leftRows.add(rowMobs);

        rowAnimals = new ButtonRow(Constants.getString("MapOverlay.show_animals", ""));
        rowAnimals.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showAnimals));
        rowAnimals.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showAnimals));
        rowAnimals.setEnabled(FeatureManager.isAllowed(Feature.RadarAnimals));
        leftRows.add(rowAnimals);

        rowVillagers = new ButtonRow(Constants.getString("MapOverlay.show_villagers", ""));
        rowVillagers.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showVillagers));
        rowVillagers.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showVillagers));
        rowVillagers.setEnabled(FeatureManager.isAllowed(Feature.RadarVillagers));
        leftRows.add(rowVillagers);

        rowPets = new ButtonRow(Constants.getString("MapOverlay.show_pets", ""));
        rowPets.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showPets));
        rowPets.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showPets));
        rowPets.setEnabled(FeatureManager.isAllowed(Feature.RadarAnimals));
        leftRows.add(rowPets);

        buttonCaves = BooleanPropertyButton.create(id++, fullMap, fullMap.showCaves);
        buttonCaves.setEnabled(FeatureManager.isAllowed(Feature.RadarPlayers));

        rowCaves = new ButtonRow(Constants.getString("MapOverlay.show_caves", ""));
        rowCaves.add(buttonCaves);
        leftRows.add(rowCaves);

        buttonGrid = BooleanPropertyButton.create(id++, fullMap, fullMap.showGrid);
        rowGrid = new ButtonRow(Constants.getString("MapOverlay.show_grid", ""));
        rowGrid.add(buttonGrid);
        leftRows.add(rowGrid);
        
        rowSelf = new ButtonRow(Constants.getString("MapOverlay.show_self", ""));
        rowSelf.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showSelf));
        rowSelf.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showSelf));
        rightRows.add(rowSelf);

        rowPlayers = new ButtonRow(Constants.getString("MapOverlay.show_players", ""));
        rowPlayers.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showPlayers));
        rowPlayers.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showPlayers));
        rowPlayers.setEnabled(FeatureManager.isAllowed(Feature.RadarPlayers));
        rightRows.add(rowPlayers);

        rowWaypoints = new ButtonRow(Constants.getString("MapOverlay.show_waypoints", ""));
        rowWaypoints.add(BooleanPropertyButton.create(id++, fullMap, fullMap.showWaypoints));
        rowWaypoints.add(BooleanPropertyButton.create(id++, miniMap, miniMap.showWaypoints));
        rowWaypoints.setEnabled(WaypointsData.isAnyEnabled());
        rightRows.add(rowWaypoints);

        rowForceUnicode = new ButtonRow(Constants.getString("MiniMap.force_unicode", ""));
        rowForceUnicode.add(BooleanPropertyButton.create(id++, fullMap, fullMap.forceUnicode));
        rowForceUnicode.add(BooleanPropertyButton.create(id++, miniMap, miniMap.forceUnicode));
        rightRows.add(rowForceUnicode);

        rowFontSize = new ButtonRow(Constants.getString("MiniMap.font", ""));
        rowFontSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, fullMap, fullMap.fontSmall));
        rowFontSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, miniMap, miniMap.fontSmall));
        rightRows.add(rowFontSize);

        rowTextureSize = new ButtonRow(Constants.getString("MiniMap.texture_size", ""));
        rowTextureSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, fullMap, fullMap.textureSmall));
        rowTextureSize.add(BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, miniMap, miniMap.textureSmall));
        rightRows.add(rowTextureSize);

        int commonWidth = getFontRenderer().getStringWidth(labelOn);
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelOff));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelFullMap));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelMiniMap));
        commonWidth += 4;

        for (ButtonRow buttonRow : leftRows)
        {
            buttonRow.setWidths(commonWidth);
            buttonList.addAll(buttonRow);
        }

        for (ButtonRow buttonRow : rightRows)
        {
            buttonRow.setWidths(commonWidth);
            buttonList.addAll(buttonRow);
        }

        rowCaves.setWidths(rowAnimals.getWidth(4));
        rowGrid.setWidths(rowAnimals.getWidth(4));

        buttonClose = new Button(ButtonEnum.Close.ordinal(), 0, 0, Constants.getString("MapOverlay.close")); //$NON-NLS-1$
        buttonClose.fitWidth(getFontRenderer());
        if(buttonClose.getWidth() < 150)
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
        int rowWidth = 10 + ((leftRows.get(0).getWidth(hgap)) * 2);
        int bx = (this.width - rowWidth) / 2;
        final int by = Math.max(30, this.height / 4);

        int leftX, rightX, topY, bottomY;
        leftX = width;
        rightX = 0;
        topY = by - 20;
        bottomY = 0;

        ButtonRow lastRow = null;
        for (ButtonRow row : leftRows)
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
            DrawUtil.drawLabel(row.label, row.getLeftX() - hgap, lastRow.getTopY() + vgap, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.black, 0, Color.cyan, 255, 1, true);
            leftX = Math.min(leftX, row.getLeftX() - hgap - getFontRenderer().getStringWidth(row.label));
            bottomY = Math.max(bottomY, row.getBottomY());
        }

        DrawUtil.drawCenteredLabel(labelFullMap, leftRows.get(0).get(0).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);
        DrawUtil.drawCenteredLabel(labelMiniMap, leftRows.get(0).get(1).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);

        lastRow = null;
        bx = (this.width + rowWidth) / 2;
        for (ButtonRow row : rightRows)
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
            DrawUtil.drawLabel(row.label, row.getLeftX() - hgap, lastRow.getTopY() + vgap, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.black, 0, Color.cyan, 255, 1, true);
            rightX = Math.max(rightX, row.getRightX());
            bottomY = Math.max(bottomY, row.getBottomY());
        }

        DrawUtil.drawCenteredLabel(labelFullMap, rightRows.get(0).get(0).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);
        DrawUtil.drawCenteredLabel(labelMiniMap, rightRows.get(0).get(1).getCenterX(), by - 10, Color.black, 0, Color.white, 255, 1);

        topY-=5;
        bottomY+=10;
        leftX-=5;
        rightX+=5;
        DrawUtil.drawRectangle(leftX, topY, rightX-leftX, 1, Color.lightGray, 150);
        DrawUtil.drawRectangle(leftX, bottomY, rightX-leftX, 1, Color.lightGray, 150);

        if(rightX-leftX>width)
        {
            int commonWidth = leftRows.get(0).get(0).getWidth()-4;
            for (ButtonRow buttonRow : leftRows)
            {
                buttonRow.setWidths(commonWidth);
            }

            for (ButtonRow buttonRow : rightRows)
            {
                buttonRow.setWidths(commonWidth);
            }

            rowCaves.setWidths(rowAnimals.getWidth(4));
            rowGrid.setWidths(rowAnimals.getWidth(4));
        }

        int closeY = Math.min(height-vgap-buttonClose.getHeight(), bottomY + (4*vgap));
        buttonClose.centerHorizontalOn(width / 2).setY(closeY);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    { // actionPerformed

        if (button instanceof BooleanPropertyButton)
        {
            ((BooleanPropertyButton) button).toggle();
            return;
        }

        if (button == buttonClose)
        {
            closeAndReturn();
        }
    }

    class ButtonRow extends ButtonList
    {
        String label;

        ButtonRow(String label)
        {
            this.label = label;
        }

        void setEnabled(boolean enable)
        {
            for (Button button : this)
            {
                button.setEnabled(enable);
            }
        }
    }
}
