package net.techbrew.journeymap.ui;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.map.MapOverlayActions;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.awt.*;

/**
 * Master options UI
 */
public class MasterOptions extends JmUI
{
    enum ButtonEnum
    {
        FullMapOptions, FullMapHelp, MiniMapOptions, MiniMapHelp, WaypointOptions, WaypointHelp, MiniMapEnable, WaypointEnable, WebMapEnable, WebMapOpen, Close;

        Button create(String label)
        {
            return new Button(this, label);
        }
    }

    String titleGeneral = Constants.getString("MapOverlay.general_display");
    String titleMiniMap = Constants.getString("MiniMap.title");
    String titleWaypoints = Constants.getString("Waypoint.management");
    String titleWebmap = Constants.getString("WebMap.title");

    String labelOptions = Constants.getString("MapOverlay.options_button");
    String labelHelp = Constants.getString("MapOverlay.help");

    Button buttonGeneralDisplayOptions, buttonFullMapHelp;
    Button buttonMiniMapEnable, buttonMiniMapOptions, buttonMiniMapHelp;
    Button buttonWaypointOptions, buttonWaypointHelp, buttonWaypointEnable;
    Button buttonWebMapEnable, buttonWebMapOpen;

    Button buttonClose;

    ButtonList listGeneral, listMiniMap, listWaypoints, listWebMap;

    public MasterOptions()
    {
        super(Constants.getString("MapOverlay.options"));
    }

    @Override
    public void initGui()
    {
        buttonList.clear();

        buttonGeneralDisplayOptions = ButtonEnum.FullMapOptions.create(labelOptions);
        buttonFullMapHelp = ButtonEnum.FullMapHelp.create(labelHelp);
        listGeneral = new ButtonList(buttonGeneralDisplayOptions, buttonFullMapHelp);
        buttonList.addAll(listGeneral);

        buttonMiniMapOptions = ButtonEnum.MiniMapOptions.create(labelOptions);
        buttonMiniMapHelp = ButtonEnum.MiniMapHelp.create(labelHelp);
        buttonMiniMapEnable = BooleanPropertyButton.create(ButtonEnum.MiniMapEnable.ordinal(), BooleanPropertyButton.Type.OnOff, "Waypoint.enable",
                JourneyMap.getInstance().miniMapProperties, JourneyMap.getInstance().miniMapProperties.enabled);
        listMiniMap = new ButtonList(buttonMiniMapOptions, buttonMiniMapEnable, buttonMiniMapHelp);
        buttonList.addAll(listMiniMap);

        buttonWaypointOptions = ButtonEnum.WaypointOptions.create(labelOptions);
        buttonWaypointHelp = ButtonEnum.WaypointHelp.create(labelHelp);
        buttonWaypointEnable = BooleanPropertyButton.create(ButtonEnum.WaypointEnable.ordinal(), BooleanPropertyButton.Type.OnOff, "Waypoint.enable",
                JourneyMap.getInstance().waypointProperties, JourneyMap.getInstance().waypointProperties.enabled);
        listWaypoints = new ButtonList(buttonWaypointOptions, buttonWaypointEnable, buttonWaypointHelp);
        buttonList.addAll(listWaypoints);

        buttonWebMapEnable = BooleanPropertyButton.create(ButtonEnum.WebMapEnable.ordinal(), BooleanPropertyButton.Type.OnOff, "Waypoint.enable",
                JourneyMap.getInstance().webMapProperties, JourneyMap.getInstance().webMapProperties.enabled);
        buttonWebMapOpen = ButtonEnum.WebMapOpen.create(Constants.getString("MapOverlay.use_browser"));
        listWebMap = new ButtonList(buttonWebMapOpen, buttonWebMapEnable);
        buttonList.addAll(listWebMap);

        new ButtonList(buttonList).equalizeWidths(getFontRenderer());

        buttonClose = ButtonEnum.Close.create(Constants.getString("MapOverlay.close"));
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
        int by = Math.max(50, this.height / 6);

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
        buttonWaypointOptions.setEnabled(buttonWaypointEnable.getToggled());

        // Web Map
        DrawUtil.drawLabel(titleWebmap, bx, by, DrawUtil.HAlign.Right, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, false);
        listWebMap.layoutHorizontal(bx, by, true, hgap);
        by = listWebMap.getBottomY() + 10;
        buttonWebMapOpen.setEnabled(buttonWebMapEnable.getToggled());

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
                UIManager.getInstance().openFullMapOptions(getClass());
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
            case WaypointOptions:
            {
                UIManager.getInstance().openWaypointOptions(getClass());
                break;
            }
            case WaypointEnable:
            {
                if (WaypointsData.isAnyEnabled())
                {
                    WaypointStore.instance().load();
                }
                else
                {
                    WaypointStore.instance().clear();
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
}
