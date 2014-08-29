/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

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
    private Button buttonPosition, buttonShape, buttonFont, buttonTexture, buttonUnicode, buttonMiniMap, buttonKeyboard;
    private Button buttonKeyboardHelp, buttonShowSelf, buttonShowfps, buttonGeneralDisplay, buttonClose, buttonCloseAll;
    private IconSetButton buttonIconSet;
    private EnumPropertyButton<DisplayVars.Orientation> buttonOrientation;
    private SliderButton buttonTerrainAlpha, buttonFrameAlpha, buttonCustomSize;
    private DisplayVars.Shape currentShape;
    private DisplayVars.Position currentPosition;
    private ButtonList leftButtons;
    private ButtonList rightButtons;
    private ButtonList bottomButtons;
    private MiniMap miniMap = UIManager.getInstance().getMiniMap();
    private MiniMapProperties miniMapProperties = JourneyMap.getMiniMapProperties();

    public MiniMapOptions()
    {
        this(MasterOptions.class);
    }

    public MiniMapOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("jm.minimap.options"), returnClass);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();

        String on = Constants.getString("jm.common.on");
        String off = Constants.getString("jm.common.off");

        boolean minimapOn = miniMapProperties.enabled.get();
        buttonMiniMap = new Button(ButtonEnum.MiniMap,
                Constants.getString("jm.minimap.enable_minimap", on),
                Constants.getString("jm.minimap.enable_minimap", off),
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
                Constants.getString("jm.common.font", Constants.getString("jm.common.font_small")),
                Constants.getString("jm.common.font", Constants.getString("jm.common.font_large")),
                (miniMapProperties.fontSmall.get()));
        buttonFont.setEnabled(minimapOn);

        buttonTexture = BooleanPropertyButton.create(ButtonEnum.Texture.ordinal(), BooleanPropertyButton.Type.SmallLarge,
                "jm.minimap.texture_size", miniMapProperties, miniMapProperties.textureSmall);
        buttonTexture.setEnabled(minimapOn);

        buttonShowSelf = BooleanPropertyButton.create(ButtonEnum.ShowSelf.ordinal(), BooleanPropertyButton.Type.SmallLarge,
                "jm.common.show_self", miniMapProperties, miniMapProperties.showSelf);
        buttonShowSelf.setEnabled(minimapOn);

        boolean showHotKeys = miniMapProperties.enableHotkeys.get();
        buttonKeyboard = new Button(ButtonEnum.Keyboard,
                Constants.getString("jm.minimap.hotkeys", on),
                Constants.getString("jm.minimap.hotkeys", off), showHotKeys);
        buttonKeyboard.setEnabled(minimapOn);

        buttonKeyboardHelp = new Button(ButtonEnum.KeyboardHelp,
                Constants.getString("jm.common.hotkeys_button"));

        boolean isShowFps = miniMapProperties.showFps.get();
        buttonShowfps = new Button(ButtonEnum.Showfps,
                Constants.getString("jm.minimap.show_fps", on),
                Constants.getString("jm.minimap.show_fps", off), isShowFps);
        buttonShowfps.setEnabled(minimapOn);

        boolean forceUnicode = miniMapProperties.forceUnicode.get();
        buttonUnicode = new Button(ButtonEnum.Unicode,
                Constants.getString("jm.minimap.force_unicode", on),
                Constants.getString("jm.minimap.force_unicode", off), forceUnicode);

        buttonGeneralDisplay = new Button(ButtonEnum.GeneralDisplay,
                Constants.getString("jm.common.general_display_button"));

        buttonIconSet = new IconSetButton(ButtonEnum.IconSet.ordinal(), miniMapProperties, "jm.common.mob_icon_set");

        buttonCustomSize = SliderButton.create(ButtonEnum.CustomSize.ordinal(), miniMapProperties.customSize, 64, 1024, "jm.minimap.custom_size", false);
        buttonTerrainAlpha = SliderButton.create(ButtonEnum.TerrainAlpha.ordinal(), miniMapProperties.terrainAlpha, 1, 255, "jm.minimap.terrain_alpha", true);
        buttonFrameAlpha = SliderButton.create(ButtonEnum.FrameAlpha.ordinal(), miniMapProperties.frameAlpha, 1, 255, "jm.minimap.frame_alpha", true);

        buttonOrientation = new EnumPropertyButton<DisplayVars.Orientation>(ButtonEnum.Orientation.ordinal(), DisplayVars.Orientation.values(),
                "jm.minimap.orientation.button", miniMapProperties, miniMapProperties.orientation);

        leftButtons = new ButtonList(buttonShape, buttonCustomSize, buttonShowfps, buttonShowSelf, buttonKeyboard, buttonKeyboardHelp, buttonGeneralDisplay);
        leftButtons.setNoDisableText(true);

        rightButtons = new ButtonList(buttonPosition, buttonFont, buttonUnicode, buttonTexture, buttonOrientation, buttonIconSet, buttonTerrainAlpha, buttonFrameAlpha);
        rightButtons.setNoDisableText(true);

        buttonClose = new Button(ButtonEnum.Close, Constants.getString("jm.common.close")); //$NON-NLS-1$
        buttonCloseAll = new Button(ButtonEnum.CloseAll, Constants.getString("jm.minimap.return_to_game")); //$NON-NLS-1$

        bottomButtons = new ButtonList(buttonClose, buttonCloseAll);

        buttonList.add(buttonMiniMap);
        buttonList.addAll(leftButtons);
        buttonList.addAll(rightButtons);
        buttonList.addAll(bottomButtons);

        new ButtonList(buttonList).equalizeWidths(getFontRenderer());
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
        final int by = Math.max(30, (this.height - (8 * 24)) / 2);

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
        buttonGeneralDisplay.setEnabled(true);
        buttonKeyboardHelp.setEnabled(buttonMiniMap.getToggled() && buttonKeyboard.getToggled());

        buttonCustomSize.setEnabled(miniMapProperties.shape.get() == DisplayVars.Shape.CustomSquare);

        bottomButtons.layoutCenteredHorizontal(bx, rightButtons.getBottomY() + (3 * vgap), true, hgap);
    }

    /**
     * Called when the mouse is moved or a mouse button is released.  Signature: (mouseX, mouseY, which) which==-1 is
     * mouseMove, which==0 or which==1 is mouseUp
     */
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which)
    {
        // See if either slider was dragging before state is updated
        boolean sliderWasInUse = buttonCustomSize.dragging || buttonTerrainAlpha.dragging || buttonFrameAlpha.dragging;

        super.mouseMovedOrUp(mouseX, mouseY, which);

        // See if sliders no longer in use.
        boolean sliderNotInUse = !buttonCustomSize.dragging && !buttonTerrainAlpha.dragging && !buttonFrameAlpha.dragging;

        if (sliderWasInUse && sliderNotInUse)
        {
            miniMap.updateDisplayVars(true);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {

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
                miniMap.updateDisplayVars(true);
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
                miniMap.updateDisplayVars(true);
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
                miniMap.updateDisplayVars(true);
                break;
            }

            case Unicode:
            {
                buttonUnicode.setToggled(miniMapProperties.toggle(miniMapProperties.forceUnicode));
                miniMap.updateDisplayVars(true);
                break;
            }

            case GeneralDisplay:
            {
                UIManager.getInstance().openGeneralDisplayOptions(getClass());
                break;
            }

            case IconSet:
            {
                buttonIconSet.toggle();
                miniMap.forceRefreshState();
                break;
            }

            case TerrainAlpha:
            {
                miniMap.forceRefreshState();
                break;
            }

            case CustomSize:
            {
                miniMap.forceRefreshState();
                miniMap.updateDisplayVars(true);
                break;
            }

            case Orientation:
            {
                miniMap.updateDisplayVars(true);
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
        buttonShape.displayString = Constants.getString("jm.minimap.shape", Constants.getString(currentShape.label));
        miniMap.setShape(shape);
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
        buttonPosition.displayString = Constants.getString("jm.minimap.position", Constants.getString(currentPosition.label));
        miniMap.setPosition(position);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        drawTitle();
        drawLogo();

        if (JourneyMap.getMiniMapProperties().enabled.get())
        {
            MiniMap miniMap = this.miniMap;
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
            if (currentPosition == DisplayVars.Position.TopRight)
            {
                DrawUtil.drawLabel(fps, width - 5, height - 5, DrawUtil.HAlign.Left, DrawUtil.VAlign.Above, Color.BLACK, 0, Color.cyan, 255, 1, true);
            }
            else
            {
                DrawUtil.drawLabel(fps, width - 5, 5, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.BLACK, 0, Color.cyan, 255, 1, true);
            }
        }
    }

    @Override
    public void drawDefaultBackground()
    {
        // Reinforce header since normal mask isn't used
        DrawUtil.drawRectangle(0, 0, this.width, headerHeight, Color.black, 100);

        // Add footer to mask the bottom toolbar
        DrawUtil.drawRectangle(0, this.height - headerHeight, this.width, headerHeight, Color.black, 150);

        // Draw miniMap
        if (JourneyMap.getMiniMapProperties().enabled.get())
        {
            miniMap.drawMap();
        }
    }

    private enum ButtonEnum
    {
        MiniMap, Position, Shape, Font, Texture, IconSet, Unicode, Keyboard, KeyboardHelp, Close, Showfps, ShowSelf, GeneralDisplay, Orientation, TerrainAlpha, FrameAlpha, CustomSize, CloseAll
    }
}
