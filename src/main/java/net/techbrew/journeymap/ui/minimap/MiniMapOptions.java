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
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.*;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.dialog.MasterOptions;

import java.awt.*;
import java.util.ArrayList;

public class MiniMapOptions extends JmUI
{
    private Button buttonPosition, buttonShape, buttonFontSize, buttonTextureSize, buttonUnicode, buttonMiniMap, buttonKeyboard;
    private Button buttonKeyboardHelp, buttonShowSelf, buttonShowfps, buttonGeneralDisplay, buttonClose, buttonCloseAll;
    private Button buttonShowLocation, buttonShowBiome, buttonCompass, buttonCompassFont, buttonReticle;
    private IconSetButton buttonIconSet;
    private EnumPropertyButton<Orientation> buttonOrientation;
    private EnumPropertyButton<ReticleOrientation> buttonReticleOrientation;
    private SliderButton buttonTerrainAlpha, buttonFrameAlpha, buttonSize;
    private ArrayList<ButtonList> buttonRows;

    private Shape currentShape;
    private Position currentPosition;
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

        Position position = Position.getPreferred();
        buttonPosition = new Button(ButtonEnum.Position, "");
        setPosition(position);
        buttonPosition.setEnabled(minimapOn);

        Shape shape = Shape.getPreferred();
        buttonShape = new Button(ButtonEnum.Shape, "");
        setShape(shape);
        buttonShape.setEnabled(false);

        buttonFontSize = new Button(ButtonEnum.Font,
                Constants.getString("jm.common.font", Constants.getString("jm.common.font_small")),
                Constants.getString("jm.common.font", Constants.getString("jm.common.font_large")),
                (miniMapProperties.fontSmall.get()));
        buttonFontSize.setEnabled(minimapOn);

        buttonTextureSize = BooleanPropertyButton.create(ButtonEnum.Texture.ordinal(), BooleanPropertyButton.Type.SmallLarge,
                "jm.minimap.texture_size", miniMapProperties, miniMapProperties.textureSmall);
        buttonTextureSize.setEnabled(minimapOn);

        buttonShowSelf = BooleanPropertyButton.create(ButtonEnum.ShowSelf.ordinal(),
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

        buttonShowLocation = BooleanPropertyButton.create(ButtonEnum.Location.ordinal(),
                "jm.minimap.show_location", miniMapProperties, miniMapProperties.showLocation);

        buttonShowBiome = BooleanPropertyButton.create(ButtonEnum.Biome.ordinal(),
                "jm.minimap.show_biome", miniMapProperties, miniMapProperties.showBiome);

        buttonCompass = BooleanPropertyButton.create(ButtonEnum.Compass.ordinal(),
                "jm.minimap.show_compass", miniMapProperties, miniMapProperties.showCompass);

        buttonCompassFont = BooleanPropertyButton.create(ButtonEnum.CompassFont.ordinal(), BooleanPropertyButton.Type.SmallLarge,
                "jm.minimap.compass_font", miniMapProperties, miniMapProperties.compassFontSmall);

        buttonReticle = BooleanPropertyButton.create(ButtonEnum.Reticle.ordinal(),
                "jm.minimap.show_reticle", miniMapProperties, miniMapProperties.showReticle);

        buttonGeneralDisplay = new Button(ButtonEnum.GeneralDisplay,
                Constants.getString("jm.common.general_display_button"));

        buttonIconSet = new IconSetButton(ButtonEnum.IconSet.ordinal(), miniMapProperties, miniMapProperties.entityIconSetName, IconSetFileHandler.getEntityIconSetNames(), "jm.common.mob_icon_set");

        buttonSize = SliderButton.create(ButtonEnum.CustomSize.ordinal(), miniMapProperties.customSize, 128, 758, "jm.minimap.size", false);
        buttonTerrainAlpha = SliderButton.create(ButtonEnum.TerrainAlpha.ordinal(), miniMapProperties.terrainAlpha, 1, 255, "jm.minimap.terrain_alpha", true);
        buttonFrameAlpha = SliderButton.create(ButtonEnum.FrameAlpha.ordinal(), miniMapProperties.frameAlpha, 1, 255, "jm.minimap.frame_alpha", true);

        buttonOrientation = new EnumPropertyButton<Orientation>(ButtonEnum.Orientation.ordinal(), Orientation.values(),
                "jm.minimap.orientation.button", miniMapProperties, miniMapProperties.orientation);

        buttonReticleOrientation = new EnumPropertyButton<ReticleOrientation>(ButtonEnum.ReticleOrientation.ordinal(), ReticleOrientation.values(),
                "jm.minimap.reticle_orientation", miniMapProperties, miniMapProperties.reticleOrientation);

        /** Button lists **/

        //buttonMiniMap
        buttonRows = new ArrayList<ButtonList>();

        buttonRows.add(new ButtonList(buttonPosition, buttonShape, buttonOrientation));
        ButtonList reticleRow = new ButtonList(buttonReticle, buttonReticleOrientation, buttonCompass);
        buttonReticleOrientation.setDrawButton(false);
        buttonRows.add(reticleRow);
        buttonRows.add(new ButtonList(buttonShowfps, buttonShowLocation, buttonShowBiome));
        buttonRows.add(new ButtonList(buttonIconSet, buttonShowSelf, buttonTextureSize));
        buttonRows.add(new ButtonList(buttonUnicode, buttonFontSize, buttonCompassFont));

        buttonList.add(buttonMiniMap);
        for (ButtonList row : buttonRows)
        {
            buttonList.addAll(row);
        }
        new ButtonList(buttonList).equalizeWidths(getFontRenderer());
        buttonReticleOrientation.setDrawButton(true);
        reticleRow.equalizeWidths(getFontRenderer(), 3, buttonRows.get(0).getWidth(3));

        ButtonList keyboardRow = new ButtonList(buttonKeyboard, buttonKeyboardHelp);
        keyboardRow.setWidths(buttonMiniMap.getWidth());
        buttonRows.add(keyboardRow);
        buttonList.addAll(keyboardRow);

        ButtonList slidersRow = new ButtonList(buttonSize, buttonTerrainAlpha, buttonFrameAlpha);
        slidersRow.setWidths(buttonMiniMap.getWidth());
        buttonRows.add(0, slidersRow);
        buttonList.addAll(slidersRow);

        buttonClose = new Button(ButtonEnum.Close, Constants.getString("jm.common.close")); //$NON-NLS-1$
        buttonCloseAll = new Button(ButtonEnum.CloseAll, Constants.getString("jm.minimap.return_to_game")); //$NON-NLS-1$

        bottomButtons = new ButtonList(buttonGeneralDisplay, buttonClose, buttonCloseAll);
        bottomButtons.equalizeWidths(getFontRenderer());
        buttonList.addAll(bottomButtons);


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

        final int hgap = 3;
        final int vgap = 3;
        final int bx = this.width / 2;
        int by = Math.max(25, (this.height - (8 * 24)) / 2);

        buttonMiniMap.centerHorizontalOn(bx).setY(by);
        by = buttonMiniMap.getBottomY() + vgap;

        boolean minimapOn = miniMapProperties.enabled.get();
        for (ButtonList row : buttonRows)
        {
            row.layoutCenteredHorizontal(bx, by, true, hgap);
            for (Button button : row)
            {
                button.setEnabled(minimapOn);
            }
            by = row.getBottomY() + vgap;
        }

        buttonMiniMap.setEnabled(true);
        buttonGeneralDisplay.setEnabled(true);
        buttonKeyboardHelp.setEnabled(buttonMiniMap.getToggled() && buttonKeyboard.getToggled());

        bottomButtons.layoutCenteredHorizontal(bx, by + (3 * vgap), true, hgap);
    }

    /**
     * Called when the mouse is moved or a mouse button is released.  Signature: (mouseX, mouseY, which) which==-1 is
     * mouseMove, which==0 or which==1 is mouseUp
     */
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which)
    {
        // See if either slider was dragging before state is updated
        boolean sliderWasInUse = buttonSize.dragging || buttonTerrainAlpha.dragging || buttonFrameAlpha.dragging;

        super.mouseMovedOrUp(mouseX, mouseY, which);

        // See if sliders no longer in use.
        boolean sliderNotInUse = !buttonSize.dragging && !buttonTerrainAlpha.dragging && !buttonFrameAlpha.dragging;

        if (sliderWasInUse)
        {
            buttonSize.updateValue();
            buttonTerrainAlpha.updateValue();
            buttonFrameAlpha.updateValue();
        }
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
                buttonFontSize.setToggled(miniMapProperties.toggle(miniMapProperties.fontSmall));
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
                buttonTextureSize.toggle();
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
                // TODO Show warning if shape is a square and orientation is my heading
                miniMap.updateDisplayVars(true);
                break;
            }

            case Location:
            {
                buttonShowLocation.toggle();
                miniMap.updateDisplayVars(true);
                break;
            }

            case Biome:
            {
                buttonShowBiome.toggle();
                miniMap.updateDisplayVars(true);
                break;
            }

            case Compass:
            {
                buttonCompass.toggle();
                miniMap.updateDisplayVars(true);
                break;
            }

            case CompassFont:
            {
                buttonCompassFont.toggle();
                miniMap.updateDisplayVars(true);
                break;
            }

            case Reticle:
            {
                buttonReticle.toggle();
                miniMap.updateDisplayVars(true);
                break;
            }

            case ReticleOrientation:
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
        if (nextIndex == Shape.Enabled.length)
        {
            nextIndex = 0;
        }
        setShape(Shape.Enabled[nextIndex]);
    }

    private void setShape(Shape shape)
    {
        if (!shape.isEnabled())
        {
            shape = Shape.Enabled[0];
        }
        currentShape = shape;
        buttonShape.displayString = Constants.getString("jm.minimap.shape", Constants.getString(currentShape.key));
        miniMap.setShape(shape);
    }

    private void nextPosition()
    {
        int nextIndex = currentPosition.ordinal() + 1;
        if (nextIndex == Position.values().length)
        {
            nextIndex = 0;
        }
        setPosition(Position.values()[nextIndex]);
    }

    private void setPosition(Position position)
    {
        currentPosition = position;
        buttonPosition.displayString = Constants.getString("jm.minimap.position", Constants.getString(currentPosition.key));
        miniMap.setPosition(position);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();


        if (miniMapProperties.enabled.get())
        {
            MiniMap miniMap = this.miniMap;
            miniMap.drawMap();
        }

        drawTitle();
        drawLogo();

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
            if (currentPosition == Position.TopRight)
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
        MiniMap, Position, Shape, Font, Texture, IconSet, Unicode, Keyboard, KeyboardHelp, Close, Showfps, ShowSelf,
        Location, Biome, Compass, CompassFont, Reticle, ReticleOrientation,
        GeneralDisplay, Orientation, TerrainAlpha, FrameAlpha, CustomSize, CloseAll
    }
}
