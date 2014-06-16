package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MasterOptions;
import net.techbrew.journeymap.waypoint.ReiReader;
import net.techbrew.journeymap.waypoint.VoxelReader;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.awt.*;

public class WaypointHelp extends JmUI
{
    private enum ButtonEnum
    {
        ImportRei, ImportVoxel, Close
    }

    private Button buttonRei, buttonVoxel, buttonClose;

    private KeyEventHandler keyEventHandler;

    String importReiText;
    String importVoxelText;
    int importReiTextWidth;
    int importVoxelTextWidth;

    public WaypointHelp()
    {
        this(MasterOptions.class);
    }

    public WaypointHelp(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("jm.waypoint.help_title"), returnClass);
        keyEventHandler = new KeyEventHandler();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        String jmWaypointDir = FileHandler.getWaypointDir().toString();
        FontRenderer fr = getFontRenderer();

        // Rei
        String reiFileName = ReiReader.getPointsFilename();
        importReiText = Constants.getString("jm.waypoint.help_import_rei", reiFileName, jmWaypointDir);
        importReiTextWidth = fr.getStringWidth(importReiText);
        buttonRei = new Button(ButtonEnum.ImportRei, Constants.getString("jm.waypoint.help_import_rei_title"));
        buttonRei.setEnabled(WaypointsData.isReiMinimapEnabled());
        buttonList.add(buttonRei);

        // Voxel
        String voxFileName = VoxelReader.getPointsFilename();
        importVoxelText = Constants.getString("jm.waypoint.help_import_voxel", voxFileName, jmWaypointDir);
        importVoxelTextWidth = fr.getStringWidth(importVoxelText);
        buttonVoxel = new Button(ButtonEnum.ImportVoxel, Constants.getString("jm.waypoint.help_import_voxel_title"));
        buttonVoxel.setEnabled(WaypointsData.isVoxelMapEnabled());
        buttonList.add(buttonVoxel);

        // Close
        buttonClose = new Button(ButtonEnum.Close.ordinal(), 0, 0, Constants.getString("jm.common.close")); //$NON-NLS-1$
        buttonList.add(buttonClose);

        new ButtonList(buttonRei, buttonVoxel).equalizeWidths(getFontRenderer());
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

    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
        switch (id)
        {

            case ImportRei:
            {
                WaypointStore.instance().load(ReiReader.loadWaypoints(), true);
                closeAndReturn();
                break;
            }

            case ImportVoxel:
            {
                WaypointStore.instance().load(VoxelReader.loadWaypoints(), true);
                closeAndReturn();
                break;
            }

            case Close:
            {
                closeAndReturn();
                break;
            }
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1, par2, par3);

        // Title
        int y = Math.max(30, this.height / 8);

        // Hotkey help
        final int x = (this.width) / 2 + (getFontRenderer().getStringWidth(Constants.getString("jm.waypoint.help_create_ingame")) / 3);

        String waypointKey = Constants.getKeyName(Constants.KB_WAYPOINT);
        drawHelpStrings(Constants.getString("jm.waypoint.help_create_ingame"), waypointKey, x, y += 12);
        drawHelpStrings(Constants.getString("jm.waypoint.help_create_inmap"), waypointKey, x, y += 12);
        drawHelpStrings(Constants.getString("jm.waypoint.help_manage_ingame"), Constants.CONTROL_KEYNAME_COMBO + waypointKey, x, y += 12);

        FontRenderer fr = getFontRenderer();
        int indentX = this.width / 20;
        int indentWidth = this.width - (indentX);

        int importReiTextWidth = fr.getStringWidth(importReiText);
        int importVoxelTextWidth = fr.getStringWidth(importVoxelText);

        if (importVoxelTextWidth < indentWidth && importReiTextWidth < indentWidth)
        {
            indentWidth = Math.max(importReiTextWidth, importVoxelTextWidth);
            indentX = (this.width - indentWidth) / 2;
        }

        // Show Rei Import
        int reiHeight = fr.listFormattedStringToWidth(importReiText, indentWidth).size() * getFontRenderer().FONT_HEIGHT;
        y += 24;
        buttonRei.centerHorizontalOn(width/2).setY(y);
        y += buttonRei.getHeight() + 5;
        fr.drawSplitString(importReiText, indentX, y, indentWidth, Color.white.getRGB());
        y += reiHeight + 16;

        // Show Voxel Import
        int voxelHeight = fr.listFormattedStringToWidth(importVoxelText, indentWidth).size() * getFontRenderer().FONT_HEIGHT;
        buttonVoxel.centerHorizontalOn(width/2).setY(y);
        if (!buttonVoxel.isDrawButton())
        {
            fr.drawStringWithShadow("§n" + buttonVoxel.displayString, indentX, y, Color.lightGray.getRGB());
        }
        y += buttonVoxel.getHeight() + 5;
        fr.drawSplitString(importVoxelText, indentX, y, indentWidth, Color.white.getRGB());
        y += voxelHeight + 16;

        buttonClose.centerHorizontalOn(width / 2);
        buttonClose.setY(Math.min(y, height - buttonClose.getHeight()));
    }

    protected void drawHelpStrings(String title, String key, int x, int y)
    {
        int hgap = 8;
        int tWidth = getFontRenderer().getStringWidth(title);
        drawString(getFontRenderer(), title, x - tWidth - hgap, y, 16777215);

        drawString(getFontRenderer(), key, x + hgap, y, Color.YELLOW.getRGB());
    }
}
