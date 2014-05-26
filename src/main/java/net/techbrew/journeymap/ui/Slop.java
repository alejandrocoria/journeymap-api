package net.techbrew.journeymap.ui;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.properties.*;
import net.techbrew.journeymap.render.draw.DrawUtil;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mwoodman on 5/22/2014.
 */
public class Slop extends JmUI
{
    String labelOn = Constants.getString("MapOverlay.on");
    String labelOff = Constants.getString("MapOverlay.off");
    String labelSmall = Constants.getString("MiniMap.font_small");
    String labelLarge = Constants.getString("MiniMap.font_large");
    String labelFullMap = Constants.getString("MapOverlay.title");
    String labelMiniMap = Constants.getString("MiniMap.title");
    String labelWebMap = Constants.getString("WebMap.title");
    int lastWidth = 0;
    int lastHeight = 0;

    ArrayList<ButtonRow> commonRows = new ArrayList<ButtonRow>();
    ButtonRow rowMobs, rowAnimals, rowVillagers, rowPets, rowPlayers, rowWaypoints, rowFontSize, rowForceUnicode;

    Button buttonGrid, buttonWebserver, buttonMiniMap, buttonMiniMapHotKeys, buttonHelpFullmap, buttonHelpMinimap, buttonHelpWaypoints, buttonWaypointManager, buttonClose;

    TextField maxDistanceField;

    ButtonList leftButtons;
    ButtonList rightButtons;
    ButtonList wpLeftButtons, wpRightButtons;

    public Slop()
    {
        super(Constants.getString("MapOverlay.options"));
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        int id = 0;

        FullMapProperties fullMap = JourneyMap.getInstance().fullMapProperties;
        MiniMapProperties miniMap = JourneyMap.getInstance().miniMapProperties;
        WebMapProperties webMap = JourneyMap.getInstance().webMapProperties;
        WaypointProperties waypointProps = JourneyMap.getInstance().waypointProperties;
        //CoreProperties coreProps = JourneyMap.getInstance().coreProperties;

        rowMobs = new ButtonRow(Constants.getString("MapOverlay.show_monsters", ""));
        rowMobs.add(new PropertyButton(id++, fullMap, fullMap.showMobs));
        rowMobs.add(new PropertyButton(id++, miniMap, miniMap.showMobs));
        rowMobs.add(new PropertyButton(id++, webMap, webMap.showMobs));
        rowMobs.setEnabled(FeatureManager.isAllowed(Feature.RadarMobs));
        commonRows.add(rowMobs);

        rowAnimals = new ButtonRow(Constants.getString("MapOverlay.show_animals", ""));
        rowAnimals.add(new PropertyButton(id++, fullMap, fullMap.showAnimals));
        rowAnimals.add(new PropertyButton(id++, miniMap, miniMap.showAnimals));
        rowAnimals.add(new PropertyButton(id++, webMap, webMap.showAnimals));
        rowAnimals.setEnabled(FeatureManager.isAllowed(Feature.RadarAnimals));
        commonRows.add(rowAnimals);

        rowVillagers = new ButtonRow(Constants.getString("MapOverlay.show_villagers", ""));
        rowVillagers.add(new PropertyButton(id++, fullMap, fullMap.showVillagers));
        rowVillagers.add(new PropertyButton(id++, miniMap, miniMap.showVillagers));
        rowVillagers.add(new PropertyButton(id++, webMap, webMap.showVillagers));
        rowVillagers.setEnabled(FeatureManager.isAllowed(Feature.RadarVillagers));
        commonRows.add(rowVillagers);

        rowPets = new ButtonRow(Constants.getString("MapOverlay.show_pets", ""));
        rowPets.add(new PropertyButton(id++, fullMap, fullMap.showPets));
        rowPets.add(new PropertyButton(id++, miniMap, miniMap.showPets));
        rowPets.add(new PropertyButton(id++, webMap, webMap.showPets));
        rowPets.setEnabled(FeatureManager.isAllowed(Feature.RadarAnimals));
        commonRows.add(rowPets);

        rowPlayers = new ButtonRow(Constants.getString("MapOverlay.show_players", ""));
        rowPlayers.add(new PropertyButton(id++, fullMap, fullMap.showPlayers));
        rowPlayers.add(new PropertyButton(id++, miniMap, miniMap.showPlayers));
        rowPlayers.add(new PropertyButton(id++, webMap, webMap.showPlayers));
        rowPlayers.setEnabled(FeatureManager.isAllowed(Feature.RadarPlayers));
        commonRows.add(rowPlayers);

        rowWaypoints = new ButtonRow(Constants.getString("MapOverlay.show_waypoints", ""));
        rowWaypoints.add(new PropertyButton(id++, fullMap, fullMap.showWaypoints));
        rowWaypoints.add(new PropertyButton(id++, miniMap, miniMap.showWaypoints));
        rowWaypoints.add(new PropertyButton(id++, webMap, webMap.showWaypoints));
        rowWaypoints.setEnabled(WaypointsData.isAnyEnabled());
        commonRows.add(rowWaypoints);

        rowForceUnicode = new ButtonRow(Constants.getString("MiniMap.force_unicode", ""));
        rowForceUnicode.add(new PropertyButton(id++, fullMap, fullMap.forceUnicode));
        rowForceUnicode.add(new PropertyButton(id++, miniMap, miniMap.forceUnicode));
        rowForceUnicode.add(new PropertyButton(id++, null, null));
        commonRows.add(rowForceUnicode);

        rowFontSize = new ButtonRow(Constants.getString("MiniMap.font", ""));
        rowFontSize.add(new PropertyButton(id++, labelSmall, labelLarge, fullMap, fullMap.fontSmall));
        rowFontSize.add(new PropertyButton(id++, labelSmall, labelLarge, miniMap, miniMap.fontSmall));
        rowFontSize.add(new PropertyButton(id++, labelSmall, labelLarge, null, null));
        commonRows.add(rowFontSize);

        int commonWidth = getFontRenderer().getStringWidth(labelOn);
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelOff));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelFullMap));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelMiniMap));
        commonWidth = Math.max(commonWidth, getFontRenderer().getStringWidth(labelWebMap));
        commonWidth += 4;

        for (ButtonRow buttonRow : commonRows)
        {
            ButtonList.setWidths(commonWidth, buttonRow);
            buttonList.addAll(buttonRow);
        }
/*
        Waypoint.in_game_title=Waypoints In-Game
    */

        wpLeftButtons = new ButtonList(
                new PropertyButton(id++, "Waypoint.show_texture", waypointProps, waypointProps.showTexture),
                new PropertyButton(id++, Constants.getString("Waypoint.texture_size", labelSmall), Constants.getString("Waypoint.texture_size", labelLarge), waypointProps, waypointProps.textureSmall),
                new PropertyButton(id++, "Waypoint.show_static_beam", waypointProps, waypointProps.showStaticBeam),
                new PropertyButton(id++, "Waypoint.show_rotating_beam", waypointProps, waypointProps.showRotatingBeam),
                new PropertyButton(id++, "Waypoint.auto_hide_label", waypointProps, waypointProps.autoHideLabel)
        );

        wpRightButtons = new ButtonList(
                new PropertyButton(id++, "Waypoint.show_name", waypointProps, waypointProps.showName),
                new PropertyButton(id++, "Waypoint.show_distance", waypointProps, waypointProps.showDistance),
                new PropertyButton(id++, "Waypoint.bold_label", waypointProps, waypointProps.boldLabel),
                new PropertyButton(id++, "Waypoint.force_unicode", waypointProps, waypointProps.forceUnicode),
                new PropertyButton(id++, Constants.getString("Waypoint.font_size", labelSmall), Constants.getString("Waypoint.font_size", labelLarge), waypointProps, waypointProps.fontSmall)
        );

        maxDistanceField = new TextField(Constants.getString("Waypoint.max_distance"), getFontRenderer(), 50, 20, true, true);


        buttonWebserver = new PropertyButton(id++, "MapOverlay.enable_webserver", webMap, webMap.enabled);
        buttonMiniMap = new PropertyButton(id++, "MiniMap.enable_minimap", miniMap, miniMap.enabled);
        buttonMiniMapHotKeys = new PropertyButton(id++, "MiniMap.hotkeys", miniMap, miniMap.enableHotkeys);
        buttonWaypointManager = new PropertyButton(id++, "MapOverlay.waypoint_manager", waypointProps, waypointProps.enabled);
        buttonGrid = new PropertyButton(id++, "MapOverlay.show_grid", fullMap, fullMap.showGrid);
        buttonHelpFullmap = new Button(id++, 0, 0, Constants.getString("MapOverlay.hotkeys_button"));
        buttonHelpMinimap = new Button(id++, 0, 0, Constants.getString("MapOverlay.hotkeys_button"));
        buttonHelpWaypoints = new Button(id++, 0, 0, Constants.getString("MapOverlay.hotkeys_button"));

        leftButtons = new ButtonList(buttonWebserver, buttonMiniMap, buttonGrid, buttonHelpMinimap);
        rightButtons = new ButtonList(buttonWaypointManager, buttonMiniMapHotKeys, buttonHelpFullmap, buttonHelpWaypoints);

        buttonList.addAll(leftButtons);
        buttonList.addAll(rightButtons);

        buttonClose = new Button(id++, 0, 0, Constants.getString("MapOverlay.close")); //$NON-NLS-1$
        buttonList.add(buttonClose);
    }

    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initGui();
        }

        if (lastWidth != width || lastHeight != height)
        {

            lastWidth = width;
            lastHeight = height;

            final int hgap = 4;
            final int vgap = 3;
            final int bx = (this.width) / 2;
            int by = Math.max(30, this.height / 6) + 12;

            for (ButtonRow row : commonRows)
            {
                row.layoutCenteredHorizontal(bx, by, true, hgap);
                DrawUtil.drawLabel(row.label, row.getLeftX() - hgap, by, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.black, 0, Color.cyan, 255, 1, true);
                by += vgap;
            }

            // Common row labels
            DrawUtil.drawCenteredLabel(labelFullMap, rowMobs.get(0).getCenterX(), rowMobs.getTopY() - 12, Color.black, 0, Color.cyan, 255, 2);


            leftButtons.layoutVertical(bx - (hgap / 2), by, false, vgap);
            rightButtons.layoutVertical(bx + (hgap / 2), by, true, vgap);

            buttonClose.below(leftButtons, vgap).centerHorizontalOn(bx);

        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton instanceof PropertyButton)
        {
            ((PropertyButton) guibutton).toggle();
            return;
        }

        if (guibutton == buttonHelpFullmap)
        {

        }

        if (guibutton == buttonHelpMinimap)
        {

        }

        if (guibutton == buttonHelpWaypoints)
        {

        }

        if (guibutton == buttonHelpWaypoints)
        {
            UIManager.getInstance().openMap();
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
                UIManager.getInstance().openMap();
                break;
            }
        }
    }

    class PropertyButton extends Button
    {
        final PropertiesBase properties;
        final AtomicBoolean valueHolder;

        public PropertyButton(int id, String rawLabel, PropertiesBase properties, AtomicBoolean valueHolder)
        {
            super(id, 0, 0, Constants.getString(rawLabel, Slop.this.labelOn), Constants.getString(rawLabel, Slop.this.labelOff), valueHolder.get());
            this.valueHolder = valueHolder;
            this.properties = properties;
        }

        public PropertyButton(int id, String labelOn, String labelOff, PropertiesBase properties, AtomicBoolean valueHolder)
        {
            super(id, 0, 0, labelOn, labelOff, valueHolder.get());
            this.valueHolder = valueHolder;
            this.properties = properties;
            if (properties == null || valueHolder == null)
            {
                this.enabled = false;
                this.noDisableText = true;
            }
        }

        public PropertyButton(int id, PropertiesBase properties, AtomicBoolean valueHolder)
        {
            super(id, 0, 0, 0, 0, Slop.this.labelOn, Slop.this.labelOff, valueHolder.get());
            this.valueHolder = valueHolder;
            this.properties = properties;
            this.noDisableText = true;
        }


        @Override
        public void toggle()
        {
            if (valueHolder != null)
            {
                setToggled(properties.toggle(valueHolder));
            }
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
                button.enabled = enable;
            }
        }
    }

}
