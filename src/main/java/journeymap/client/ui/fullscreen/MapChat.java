/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;


/**
 * The type Map chat.
 */
public class MapChat extends GuiChat
{
    /**
     * Whether shown on map
     */
    protected boolean hidden = false;

    /**
     * Used by chat to make old chat lines fade out
     */
    protected int cursorCounter;

    /**
     * Instantiates a new Map chat.
     *
     * @param defaultText the default text
     * @param hidden      the hidden
     */
    public MapChat(String defaultText, boolean hidden) {
        super(defaultText);
        this.hidden = hidden;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        hidden = true;
    }

    /**
     * Close.
     */
    public void close() {
        onGuiClosed();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        if (hidden) {
            return;
        }
        super.updateScreen();
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (hidden) {
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
        } else if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
            super.keyTyped(typedChar, keyCode);
        } else {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty()) {
                this.sendChatMessage(s);
            }

            this.inputField.setText("");
            this.mc.ingameGUI.getChatGUI().resetScroll();
        }
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException {
        if (hidden) {
            return;
        }
        super.handleMouseInput();
    }

    /**
     * Called when the mouse is clicked.
     */
    //@Override
    public void mouseClicked(int par1, int par2, int par3) throws IOException {
        if (hidden) {
            return;
        }
        super.mouseClicked(par1, par2, par3);
    }

    @Override
    public void confirmClicked(boolean par1, int par2) {
        if (hidden) {
            return;
        }
        super.confirmClicked(par1, par2);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GL11.glTranslatef(0, this.height - 47.5f, 0.0F);
        if (this.mc != null) {
            if (this.mc.ingameGUI != null && this.mc.ingameGUI.getChatGUI() != null) {
                this.mc.ingameGUI.getChatGUI().drawChat(hidden ? this.mc.ingameGUI.getUpdateCounter() : this.cursorCounter++);
            }
        }
        GlStateManager.popMatrix();

        if (hidden) {
            return;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Is hidden boolean.
     *
     * @return the boolean
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets hidden.
     *
     * @param hidden the hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Sets text.
     *
     * @param defaultText the default text
     */
    public void setText(String defaultText) {
        this.inputField.setText(defaultText);
    }
}
