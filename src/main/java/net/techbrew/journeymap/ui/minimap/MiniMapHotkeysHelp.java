package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.MasterOptions;
import net.techbrew.journeymap.ui.UIManager;

import java.awt.*;

public class MiniMapHotkeysHelp extends JmUI
{

    private int lastWidth = 0;
    private int lastHeight = 0;

    private enum ButtonEnum
    {
        Close
    }

    private Button buttonClose;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;

    private KeyEventHandler keyEventHandler;

    public MiniMapHotkeysHelp()
    {
        this(MasterOptions.class);
    }

    public MiniMapHotkeysHelp(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("jm.minimap.hotkeys_title"), returnClass);
        keyEventHandler = new KeyEventHandler();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        buttonClose = new Button(ButtonEnum.Close, Constants.getString("jm.common.close")); //$NON-NLS-1$

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
        drawHelpStrings(Constants.getString("jm.minimap.hotkeys_toggle"), Constants.CONTROL_KEYNAME_COMBO + Constants.getKeyName(Constants.KB_MAP), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.zoom_in"), Constants.getKeyName(Constants.KB_MAP_ZOOMIN), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.zoom_out"), Constants.getKeyName(Constants.KB_MAP_ZOOMOUT), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.day"), Constants.getKeyName(Constants.KB_MAP_DAY), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.night"), Constants.getKeyName(Constants.KB_MAP_NIGHT), x, y += 12);
        drawHelpStrings(Constants.getString("key.journeymap.minimap_position"), Constants.getKeyName(Constants.KB_MINIMAP_POS), x, y += 12);
        buttonClose.setY(y + 16);
    }

    protected void drawHelpStrings(String title, String key, int x, int y)
    {
        int hgap = 8;
        int tWidth = getFontRenderer().getStringWidth(title);
        drawString(getFontRenderer(), title, x - tWidth - hgap, y, 16777215);

        drawString(getFontRenderer(), key, x + hgap, y, Color.YELLOW.getRGB());
    }

    @Override
    public void drawBackground(int layer)
    {
        super.drawBackground(layer);

        if(JourneyMap.getInstance().miniMapProperties.enabled.get())
        {
            MiniMap miniMap = UIManager.getInstance().getMiniMap();
            miniMap.drawMap();
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        keyEventHandler.onKeypress(true);
    }

}
