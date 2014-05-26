package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.ui.*;

public class MiniMapOptions extends JmUI
{

    private enum ButtonEnum
    {
        MiniMap, Position, Shape, Font, Unicode, Keyboard, KeyboardHelp, Close, Showfps, CloseAll
    }

    ;
    private Button buttonPosition, buttonShape, buttonFont, buttonUnicode, buttonMiniMap, buttonKeyboard, buttonKeyboardHelp, buttonShowfps, buttonClose, buttonCloseAll;
    private BooleanPropertyButton buttonShowWaypoints, buttonShowWaypointLabels, buttonShow;

    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;
    private ButtonList leftButtons;
    private ButtonList rightButtons;

    private MiniMapProperties miniMapProperties = JourneyMap.getInstance().miniMapProperties;

    public MiniMapOptions()
    {
        this(MasterOptions.class);
    }

    public MiniMapOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("MiniMap.options"), returnClass);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");


        boolean minimapOn = miniMapProperties.enabled.get();
        buttonMiniMap = new Button(ButtonEnum.MiniMap.ordinal(), 0, 0,
                Constants.getString("MiniMap.enable_minimap", on),
                Constants.getString("MiniMap.enable_minimap", off),
                minimapOn); //$NON-NLS-1$  //$NON-NLS-2$
        buttonMiniMap.setToggled(minimapOn);

        DisplayVars.Position position = DisplayVars.Position.getPreferred();
        buttonPosition = new Button(ButtonEnum.Position.ordinal(), 0, 0, "");
        setPosition(position);
        buttonPosition.enabled = minimapOn;

        DisplayVars.Shape shape = DisplayVars.Shape.getPreferred();
        buttonShape = new Button(ButtonEnum.Shape.ordinal(), 0, 0, "");
        setShape(shape);
        buttonShape.enabled = minimapOn;

        buttonFont = new Button(ButtonEnum.Font.ordinal(), 0, 0,
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_large")),
                (miniMapProperties.fontSmall.get()));
        buttonFont.enabled = minimapOn;

        boolean showHotKeys = miniMapProperties.enableHotkeys.get();
        buttonKeyboard = new Button(ButtonEnum.Keyboard.ordinal(), 0, 0,
                Constants.getString("MiniMap.hotkeys", on),
                Constants.getString("MiniMap.hotkeys", off), showHotKeys);
        buttonKeyboard.enabled = minimapOn;

        buttonKeyboardHelp = new Button(ButtonEnum.KeyboardHelp.ordinal(), 0, 0,
                Constants.getString("MapOverlay.hotkeys_button"));

        boolean isShowFps = miniMapProperties.showFps.get();
        buttonShowfps = new Button(ButtonEnum.Showfps.ordinal(), 0, 0,
                Constants.getString("MiniMap.show_fps", on),
                Constants.getString("MiniMap.show_fps", off), isShowFps);
        buttonShowfps.enabled = minimapOn;

        boolean forceUnicode = miniMapProperties.forceUnicode.get();
        buttonUnicode = new Button(ButtonEnum.Unicode.ordinal(), 0, 0,
                Constants.getString("MiniMap.force_unicode", on),
                Constants.getString("MiniMap.force_unicode", off), forceUnicode);

        buttonClose = new Button(ButtonEnum.Close.ordinal(), 0, 0, Constants.getString("MapOverlay.close")); //$NON-NLS-1$

        buttonCloseAll = new Button(ButtonEnum.CloseAll.ordinal(), 0, 0, Constants.getString("MiniMap.return_to_game")); //$NON-NLS-1$

        buttonList.add(buttonMiniMap);
        buttonList.add(buttonPosition);
        buttonList.add(buttonShape);
        buttonList.add(buttonFont);
        buttonList.add(buttonKeyboard);
        buttonList.add(buttonKeyboardHelp);
        buttonList.add(buttonShowfps);
        buttonList.add(buttonUnicode);

        buttonList.add(buttonClose);
        buttonList.add(buttonCloseAll);

        leftButtons = new ButtonList(buttonMiniMap, buttonPosition, buttonShowfps, buttonKeyboard);
        rightButtons = new ButtonList(buttonShape, buttonFont, buttonUnicode, buttonKeyboardHelp);
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

        final int hgap = 2;
        final int vgap = 3;
        final int bx = this.width / 2;
        final int by = this.height / 4;

        leftButtons.layoutVertical(bx - hgap, by, false, vgap);
        rightButtons.layoutVertical(bx + hgap, by, true, vgap);

        for (Button button : leftButtons)
        {
            button.enabled = buttonMiniMap.getToggled();
        }

        for (Button button : rightButtons)
        {
            button.enabled = buttonMiniMap.getToggled();
        }

        buttonMiniMap.enabled = true;

        buttonClose.below(leftButtons, vgap + vgap + vgap).centerHorizontalOn(bx);
        buttonCloseAll.below(buttonClose, vgap).centerHorizontalOn(bx);

        buttonKeyboardHelp.enabled = buttonMiniMap.getToggled() && buttonKeyboard.getToggled();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
        switch (id)
        {

            case MiniMap:
            {
                UIManager uim = UIManager.getInstance();
                final boolean enabled = !uim.isMiniMapEnabled();
                miniMapProperties.enabled.set(enabled);
                miniMapProperties.save();
                buttonMiniMap.setToggled(enabled);
                uim.setMiniMapEnabled(enabled);
                break;
            }

            case Shape:
            {
                nextShape();
                break;
            }

            case Position:
            {
                nextPosition();
                break;
            }

            case Font:
            {
                buttonFont.setToggled(miniMapProperties.toggle(miniMapProperties.fontSmall));
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                break;
            }

            case Keyboard:
            {
                buttonKeyboard.setToggled(miniMapProperties.toggle(miniMapProperties.enableHotkeys));
                break;
            }

            case KeyboardHelp:
            {
                UIManager.getInstance().openMiniMapHotkeyHelp(getClass());
                break;
            }

            case Showfps:
            {
                buttonShowfps.setToggled(miniMapProperties.toggle(miniMapProperties.showFps));
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                break;
            }

            case Unicode:
            {
                buttonUnicode.setToggled(miniMapProperties.toggle(miniMapProperties.forceUnicode));
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                break;
            }

            case Close:
            {
                closeAndReturn();
                break;
            }

            case CloseAll:
            {
                UIManager.getInstance().closeAll();
                break;
            }
        }
    }

    public void nextShape()
    {
        int nextIndex = currentShape.ordinal() + 1;
        if (nextIndex == DisplayVars.Shape.Enabled.length)
        {
            nextIndex = 0;
        }
        setShape(DisplayVars.Shape.Enabled[nextIndex]);
    }

    private void setShape(DisplayVars.Shape shape)
    {
        if (!shape.isEnabled())
        {
            shape = DisplayVars.Shape.Enabled[0];
        }
        currentShape = shape;
        buttonShape.displayString = Constants.getString("MiniMap.shape", Constants.getString(currentShape.label));
        UIManager.getInstance().getMiniMap().setShape(shape);
    }

    private void nextPosition()
    {
        int nextIndex = currentPosition.ordinal() + 1;
        if (nextIndex == DisplayVars.Position.values().length)
        {
            nextIndex = 0;
        }
        setPosition(DisplayVars.Position.values()[nextIndex]);
    }

    private void setPosition(DisplayVars.Position position)
    {
        currentPosition = position;
        buttonPosition.displayString = Constants.getString("MiniMap.position", Constants.getString(currentPosition.label));
        UIManager.getInstance().getMiniMap().setPosition(position);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        drawTitle();
        drawLogo();

        MiniMap miniMap = UIManager.getInstance().getMiniMap();
        if (miniMap.isEnabled())
        {
            miniMap.drawMap();
        }

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }
    }
}
