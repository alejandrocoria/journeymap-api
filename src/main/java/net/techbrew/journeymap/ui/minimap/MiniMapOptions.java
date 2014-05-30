package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.Button;

import java.awt.*;

public class MiniMapOptions extends JmUI
{

    private enum ButtonEnum
    {
        MiniMap, Position, Shape, Font, Texture, Unicode, Keyboard, KeyboardHelp, Close, Showfps, ShowSelf, GeneralDisplay, CloseAll
    }

    private Button buttonPosition, buttonShape, buttonFont, buttonTexture, buttonUnicode, buttonMiniMap, buttonKeyboard;
    private Button buttonKeyboardHelp, buttonShowSelf, buttonShowfps, buttonGeneralDisplay, buttonClose, buttonCloseAll;

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
        buttonMiniMap = new Button(ButtonEnum.MiniMap,
                Constants.getString("MiniMap.enable_minimap", on),
                Constants.getString("MiniMap.enable_minimap", off),
                minimapOn); //$NON-NLS-1$  //$NON-NLS-2$
        buttonMiniMap.setToggled(minimapOn);

        DisplayVars.Position position = DisplayVars.Position.getPreferred();
        buttonPosition = new Button(ButtonEnum.Position, "");
        setPosition(position);
        buttonPosition.setEnabled(minimapOn);

        DisplayVars.Shape shape = DisplayVars.Shape.getPreferred();
        buttonShape = new Button(ButtonEnum.Shape, "");
        setShape(shape);
        buttonShape.setEnabled(minimapOn);

        buttonFont = new Button(ButtonEnum.Font,
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_large")),
                (miniMapProperties.fontSmall.get()));
        buttonFont.setEnabled(minimapOn);

        buttonTexture = new BooleanPropertyButton(ButtonEnum.Texture.ordinal(),
                Constants.getString("MiniMap.texture_size", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.texture_size", Constants.getString("MiniMap.font_large")),
                miniMapProperties, miniMapProperties.textureSmall);
        buttonTexture.setEnabled(minimapOn);

        buttonShowSelf= new BooleanPropertyButton(ButtonEnum.ShowSelf.ordinal(),
                Constants.getString("MapOverlay.show_self", Constants.getString("MapOverlay.on")),
                Constants.getString("MapOverlay.show_self", Constants.getString("MapOverlay.off")),
                miniMapProperties, miniMapProperties.showSelf);
        buttonShowSelf.setEnabled(minimapOn);

        boolean showHotKeys = miniMapProperties.enableHotkeys.get();
        buttonKeyboard = new Button(ButtonEnum.Keyboard,
                Constants.getString("MiniMap.hotkeys", on),
                Constants.getString("MiniMap.hotkeys", off), showHotKeys);
        buttonKeyboard.setEnabled(minimapOn);

        buttonKeyboardHelp = new Button(ButtonEnum.KeyboardHelp,
                Constants.getString("MapOverlay.hotkeys_button"));

        boolean isShowFps = miniMapProperties.showFps.get();
        buttonShowfps = new Button(ButtonEnum.Showfps,
                Constants.getString("MiniMap.show_fps", on),
                Constants.getString("MiniMap.show_fps", off), isShowFps);
        buttonShowfps.setEnabled(minimapOn);

        boolean forceUnicode = miniMapProperties.forceUnicode.get();
        buttonUnicode = new Button(ButtonEnum.Unicode,
                Constants.getString("MiniMap.force_unicode", on),
                Constants.getString("MiniMap.force_unicode", off), forceUnicode);

        buttonGeneralDisplay = new Button(ButtonEnum.GeneralDisplay,
                Constants.getString("MapOverlay.general_display_button"));

        leftButtons = new ButtonList(buttonShape, buttonShowfps, buttonShowSelf, buttonKeyboard, buttonKeyboardHelp);
        leftButtons.setNoDisableText(true);

        rightButtons = new ButtonList(buttonPosition, buttonTexture, buttonFont, buttonUnicode, buttonGeneralDisplay);
        rightButtons.setNoDisableText(true);

        buttonList.add(buttonMiniMap);
        buttonList.addAll(leftButtons);
        buttonList.addAll(rightButtons);

        new ButtonList(buttonList).equalizeWidths(getFontRenderer());

        buttonClose = new Button(ButtonEnum.Close, Constants.getString("MapOverlay.close")); //$NON-NLS-1$
        buttonClose.setWidth(150);

        buttonCloseAll = new Button(ButtonEnum.CloseAll, Constants.getString("MiniMap.return_to_game")); //$NON-NLS-1$
        buttonCloseAll.setWidth(150);

        buttonList.add(buttonClose);
        buttonList.add(buttonCloseAll);
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
        final int by = Math.max(40, (this.height - (8*24)) / 2);

        buttonMiniMap.centerHorizontalOn(bx).setY(by);

        leftButtons.layoutVertical(bx - hgap, buttonMiniMap.getBottomY() + vgap, false, vgap);
        rightButtons.layoutVertical(bx + hgap, buttonMiniMap.getBottomY() + vgap, true, vgap);

        for (Button button : leftButtons)
        {
            button.setEnabled(buttonMiniMap.getToggled());
        }

        for (Button button : rightButtons)
        {
            button.setEnabled(buttonMiniMap.getToggled());
        }

        buttonMiniMap.setEnabled(true);

        buttonCloseAll.below(leftButtons, vgap + vgap + vgap).centerHorizontalOn(bx);
        buttonClose.below(buttonCloseAll, vgap).centerHorizontalOn(bx);


        buttonKeyboardHelp.setEnabled(buttonMiniMap.getToggled() && buttonKeyboard.getToggled());
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

            case ShowSelf:
            {
                buttonShowSelf.toggle();
                break;
            }

            case Texture:
            {
                buttonTexture.toggle();
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

            case GeneralDisplay:
            {
                UIManager.getInstance().openGeneralDisplayOptions(getClass());
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

        // Show FPS
        String fps = mc.debug;
        final int idx = fps != null ? fps.indexOf(',') : -1;
        if (idx > 0)
        {
            fps = fps.substring(0, idx);
            if(currentPosition== DisplayVars.Position.TopRight)
            {
                DrawUtil.drawLabel(fps, width - 5, height-5, DrawUtil.HAlign.Left, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, true);
            }
            else
            {
                DrawUtil.drawLabel(fps, width - 5, 5, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.BLACK, 0, Color.cyan, 255, 1, true);
            }
        }
    }
}
