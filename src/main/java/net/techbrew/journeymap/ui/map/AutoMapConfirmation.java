package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.task.MapRegionTask;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class AutoMapConfirmation extends JmUI
{

    private enum ButtonEnum
    {
        All, Missing, None, Close
    }

    Button buttonAll, buttonMissing, buttonNone, buttonClose;


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

        buttonAll = new Button(ButtonEnum.All.ordinal(), Constants.getString("jm.common.automap_dialog_all"));
        buttonAll.setNoDisableText(true);

        buttonMissing = new Button(ButtonEnum.Missing.ordinal(), Constants.getString("jm.common.automap_dialog_missing"));
        buttonMissing.setNoDisableText(true);

        buttonNone = new Button(ButtonEnum.None.ordinal(), Constants.getString("jm.common.automap_dialog_none"));
        buttonNone.setNoDisableText(true);

        buttonClose = new Button(ButtonEnum.None.ordinal(), Constants.getString("jm.common.close"));
        buttonClose.setNoDisableText(true);

        boolean enable = !JourneyMap.getInstance().isTaskManagerEnabled(MapRegionTask.Manager.class);
        buttonAll.setEnabled(enable);
        buttonMissing.setEnabled(enable);
        buttonNone.setEnabled(!enable);

        buttonList.add(buttonAll);
        buttonList.add(buttonMissing);
        buttonList.add(buttonNone);
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
        final int y = this.height / 4;
        final int vgap = 3;

        this.drawCenteredString(getFontRenderer(), Constants.getString("jm.common.automap_dialog_text"), x, y, 16777215);

        buttonAll.centerHorizontalOn(x).setY(y + 18);
        buttonMissing.centerHorizontalOn(x).below(buttonAll, vgap);
        buttonNone.centerHorizontalOn(x).below(buttonMissing, vgap);
        buttonClose.centerHorizontalOn(x).below(buttonNone, vgap * 4);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
        switch (id)
        {
            case All:
            {
                JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.TRUE);
                break;
            }
            case Missing:
            {
                JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, true, Boolean.FALSE);
                break;
            }
            case None:
            {
                JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, false, null);
                break;
            }
            case Close:
            {
                break;
            }
        }
        UIManager.getInstance().openMap();
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                UIManager.getInstance().openMapActions();
                break;
            }
        }
    }
}
