package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.waypoint.ReiReader;
import net.techbrew.journeymap.waypoint.VoxelReader;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class WaypointHelp extends JmUI {

    private int lastWidth = 0;
    private int lastHeight = 0;

	private enum ButtonEnum {ImportRei, ImportVoxel, Close};
	private Button buttonRei, buttonVoxel, buttonClose;

    private KeyEventHandler keyEventHandler;

    String importReiText;
    String importVoxelText;
    int importReiTextWidth;
    int importVoxelTextWidth;

    public WaypointHelp() {
		super(Constants.getString("MapOverlay.waypoint_help_title"));
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
        importReiText = Constants.getString("MapOverlay.waypoint_help_import_rei", reiFileName, jmWaypointDir);
        importReiTextWidth = fr.getStringWidth(importReiText);
        buttonRei = new Button(ButtonEnum.ImportRei, Constants.getString("MapOverlay.waypoint_help_import_rei_title"));
        buttonRei.enabled = WaypointsData.isReiMinimapEnabled();
        buttonList.add(buttonRei);

        // Voxel
        String voxFileName = VoxelReader.getPointsFilename();
        importVoxelText = Constants.getString("MapOverlay.waypoint_help_import_voxel", voxFileName, jmWaypointDir);
        importVoxelTextWidth = fr.getStringWidth(importVoxelText);
        buttonVoxel = new Button(ButtonEnum.ImportVoxel, Constants.getString("MapOverlay.waypoint_help_import_voxel_title"));
        buttonVoxel.enabled = WaypointsData.isVoxelMapEnabled();
        buttonList.add(buttonVoxel);

        // Close
		buttonClose = new Button(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$
        buttonList.add(buttonClose);

        ButtonList.equalizeWidths(mc.fontRenderer, new ButtonList(buttonRei, buttonVoxel));
    }
    
    /**
	 * Center buttons in UI.
	 */
    @Override
    protected void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}

	}

    @Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {

            case ImportRei: {
                WaypointStore.instance().load(ReiReader.loadWaypoints(), true);
                UIManager.getInstance().openWaypointManager();
                break;
            }

            case Close: {
                UIManager.getInstance().openWaypointManager();
                break;
            }
		}
	}
    
    @Override
	public void updateScreen() {
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
        int y = Math.max(30, this.height/8);

        // Hotkey help
        final int x = (this.width)/2;

        String waypointKey = Constants.getKeyName(Constants.KB_WAYPOINT);
        drawHelpStrings(Constants.getString("MapOverlay.waypoint_help_create_ingame"), waypointKey, x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.waypoint_help_create_inmap"), waypointKey, x, y+=12);
        drawHelpStrings(Constants.getString("MapOverlay.waypoint_help_manage_ingame"), Constants.CONTROL_KEYNAME_COMBO + waypointKey, x, y+=12);

        FontRenderer fr = getFontRenderer();
        int indentX = this.width/20;
        int indentWidth = this.width-(indentX*2);

        int importReiTextWidth = fr.getStringWidth(importReiText);
        int importVoxelTextWidth = fr.getStringWidth(importVoxelText);

        if(importVoxelTextWidth < indentWidth && importReiTextWidth < indentWidth)
        {
            indentWidth = Math.max(importReiTextWidth, importVoxelTextWidth);
            indentX = (this.width-indentWidth)/2;
        }

        // Show Rei Import
        int reiHeight = fr.listFormattedStringToWidth(importReiText, indentWidth).size() * fr.FONT_HEIGHT;
        y += 24;
        buttonRei.setPosition(indentX-4, y);
        y+=buttonRei.getHeight() + 5;
        fr.drawSplitString(importReiText, indentX, y, indentWidth, Color.white.getRGB());
        y+=reiHeight + 24;

        // Show Voxel Import
        int voxelHeight = fr.listFormattedStringToWidth(importVoxelText, indentWidth).size() * fr.FONT_HEIGHT;
        buttonVoxel.setPosition(indentX-4, y);
        if(!buttonVoxel.drawButton)
        {
            fr.drawStringWithShadow("§n" + buttonVoxel.displayString, indentX, y, Color.lightGray.getRGB());
        }
        y+=buttonVoxel.getHeight() + 5;
        fr.drawSplitString(importVoxelText, indentX, y, indentWidth, Color.white.getRGB());
        y+=voxelHeight + 24;

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
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
            case Keyboard.KEY_ESCAPE : {
                UIManager.getInstance().openWaypointManager();
                return;
            }
		}
	}
}
