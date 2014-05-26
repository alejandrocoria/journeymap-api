package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.JmUI;

import java.awt.*;

public class MapOverlayHotkeysHelp extends JmUI
{

    private int lastWidth = 0;
    private int lastHeight = 0;

    private enum ButtonEnum
    {
        Close
    }

    ;
    private Button buttonClose;

    private KeyEventHandler keyEventHandler;

    public MapOverlayHotkeysHelp(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("MapOverlay.hotkeys_title"), returnClass);
        keyEventHandler = new KeyEventHandler();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        buttonClose = new net.techbrew.journeymap.ui.Button(ButtonEnum.Close.ordinal(), 0, 0, Constants.getString("MapOverlay.close")); //$NON-NLS-1$

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

        if (lastWidth != width || lastHeight != height)
        {

            lastWidth = width;
            lastHeight = height;
            final int by = (this.height / 4) + 60;
            buttonClose.centerHorizontalOn(this.width / 2).setY(by);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
        switch (id)
        {

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
        int y = this.height / 4 - 18;

        // Hotkey help
        y += 12;
        final int x = (this.width) / 2;
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_toggle"), Constants.getKeyName(Constants.KB_MAP), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_zoom_in"), Constants.getKeyName(Constants.KB_MAP_ZOOMIN), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_zoom_out"), Constants.getKeyName(Constants.KB_MAP_ZOOMOUT), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_day"), Constants.getKeyName(Constants.KB_MAP_DAY), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_night"), Constants.getKeyName(Constants.KB_MAP_NIGHT), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_north"), Constants.getKeyName(mc.gameSettings.keyBindForward), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_west"), Constants.getKeyName(mc.gameSettings.keyBindLeft), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_south"), Constants.getKeyName(mc.gameSettings.keyBindBack), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_east"), Constants.getKeyName(mc.gameSettings.keyBindRight), x, y += 12);
        drawHelpStrings(Constants.getString("MapOverlay.hotkeys_waypoint"), Constants.getString("MapOverlay.hotkeys_doubleclick"), x, y += 12);
        buttonClose.setY(y + 16);
    }

    protected void drawHelpStrings(String title, String key, int x, int y)
    {
        int hgap = 8;
        int tWidth = getFontRenderer().getStringWidth(title);
        drawString(getFontRenderer(), title, x - tWidth - hgap, y, 16777215);

        drawString(getFontRenderer(), key, x + hgap, y, Color.YELLOW.getRGB());
    }
}
