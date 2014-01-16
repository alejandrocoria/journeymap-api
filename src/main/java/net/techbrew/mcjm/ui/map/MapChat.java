package net.techbrew.mcjm.ui.map;

import net.minecraft.client.gui.GuiChat;
import org.lwjgl.opengl.GL11;


public class MapChat extends GuiChat
{
    protected boolean hidden = false;
    protected int bottomMargin = 8;
    private int cursorCounter;

    public MapChat(String defaultText, boolean hidden)
    {
    	super(defaultText);
        this.hidden = hidden;
    }

    @Override
	public void func_146281_b()
    {
        super.func_146281_b();
        hidden = true;
    }

	public void close() {
        func_146281_b();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
	public void updateScreen()
    {
        if(hidden) return;
        super.updateScreen();
        cursorCounter++;
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
	public void keyTyped(char par1, int par2)
    {
    	if(hidden) return;
        super.keyTyped(par1, par2);
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void func_146274_d()
    {
    	if(hidden) return;
        super.func_146274_d();
    }

    /**
     * Called when the mouse is clicked.
     */
    //@Override
	public void mouseClicked(int par1, int par2, int par3)
    {
    	if(hidden) return;
        super.mouseClicked(par1, par2, par3);
    }

    @Override
	public void confirmClicked(boolean par1, int par2)
    {
        if(hidden) return;
        super.confirmClicked(par1, par2);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int par1, int par2, float par3)
    {    	    
    	GL11.glPushMatrix();
        GL11.glTranslatef(0, this.field_146295_m - 39.5f - bottomMargin, 0.0F);
        if(this.field_146297_k!=null) {
            if(this.field_146297_k.ingameGUI!=null && this.field_146297_k.ingameGUI.func_146158_b()!=null) {
                this.field_146297_k.ingameGUI.func_146158_b().func_146230_a(hidden ? this.field_146297_k.ingameGUI.getUpdateCounter() : this.cursorCounter);
            }
        }
    	GL11.glPopMatrix();
    	
    	if(hidden) return;
    	
        super.drawScreen(par1, par2, par3);
    }

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

    public void setText(String defaultText) {
        this.field_146415_a.func_146180_a(defaultText);
    }
}
