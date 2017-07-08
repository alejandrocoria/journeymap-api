/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.dialog;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.properties.ClientCategory;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.task.multi.MapRegionTask;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

/**
 * The type Auto map confirmation.
 */
public class AutoMapConfirmation extends JmUI
{

    Button buttonOptions, buttonAll, buttonMissing, buttonClose;

    /**
     * Instantiates a new Auto map confirmation.
     */
    public AutoMapConfirmation()
    {
        super(Constants.getString("jm.common.automap_dialog"));
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        buttonList.clear();

        buttonOptions = new Button(Constants.getString("jm.common.options_button"));
        buttonAll = new Button(Constants.getString("jm.common.automap_dialog_all"));
        buttonMissing = new Button(Constants.getString("jm.common.automap_dialog_missing"));
        buttonClose = new Button(Constants.getString("jm.common.close"));

        buttonList.add(buttonOptions);
        buttonList.add(buttonAll);
        buttonList.add(buttonMissing);
        buttonList.add(buttonClose);
    }

    @Override
    protected void layoutButtons()
    {

        if (this.buttonList.isEmpty())
        {
            this.initGui();
        }

        final int x = this.width / 2;
        int lineHeight = (fontRenderer.FONT_HEIGHT + 3);
        int y = headerHeight + (lineHeight)*2;

        this.drawCenteredString(getFontRenderer(), Constants.getString("jm.common.automap_dialog_summary_1"), x, y, 16777215);
        y += lineHeight;

        this.drawCenteredString(getFontRenderer(), Constants.getString("jm.common.automap_dialog_summary_2"), x, y, 16777215);
        y += (lineHeight)*2;

        buttonOptions.centerHorizontalOn(x).centerVerticalOn(y);
        y+= (lineHeight)*3;

        this.drawCenteredString(getFontRenderer(), Constants.getString("jm.common.automap_dialog_text"), x, y, 0xffff00);
        y += (lineHeight)*2;

        ButtonList buttons = new ButtonList(buttonAll, buttonMissing);
        buttons.equalizeWidths(fontRenderer, 4, 200);
        buttons.layoutCenteredHorizontal(x, y, true, 4);

        buttonClose.centerHorizontalOn(x).below(buttonMissing, lineHeight);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if(guibutton == buttonOptions)
        {
            UIManager.INSTANCE.openOptionsManager(this, ClientCategory.Cartography);
            return;
        }
        else if (guibutton != buttonClose)
        {
            final boolean enable;
            final Object arg;
            if (guibutton == buttonAll)
            {
                enable = true;
                arg = Boolean.TRUE;
            }
            else if (guibutton == buttonMissing)
            {
                enable = true;
                arg = Boolean.FALSE;
            }
            else
            {
                enable = false;
                arg = null;
            }

            Journeymap.getClient().queueMainThreadTask(new IMainThreadTask()
            {
                @Override
                public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
                {
                    Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, enable, arg);
                    return null;
                }

                @Override
                public String getName()
                {
                    return "Automap";
                }
            });
        }

        UIManager.INSTANCE.openFullscreenMap();
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                UIManager.INSTANCE.openMapActions();
                break;
            }
        }
    }
}
