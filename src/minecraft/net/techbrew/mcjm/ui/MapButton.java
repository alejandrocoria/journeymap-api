package net.techbrew.mcjm.ui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.Constants;

public class MapButton extends GuiSmallButton {
	
	private Boolean toggled = true;
	String icon;
	String[] multiline;
	String labelOn;
	String labelOff;

	public MapButton(int id, int x, int y, String label) {
		super(id, x, y, label);
	}
	
	public MapButton(int id, int x, int y, String labelOn, String labelOff, Boolean toggled) {
		super(id, x, y, toggled ? labelOn : labelOff);
		this.labelOn = labelOn;
		this.labelOff = labelOff;
		this.toggled = toggled;
	}	
	
	public MapButton(int id, int x, int y, int width, int height, String hoverText, String icon) {
		super(id, x, y, width, height, "");
		this.icon = (icon==null) ? "/gui/gui.png" : icon; //$NON-NLS-1$
		setHoverText(hoverText);
	}
	
	public void setHoverText(String label) {
		multiline = displayString.split(" "); //$NON-NLS-1$
	}
	
	private void updateLabel() {
		if(labelOn!=null && labelOff!=null) {
			this.displayString = toggled ? labelOn : labelOff;
		}		
	}
	
	public void toggle() {
		setToggled(!toggled);
	}
	
	public void setToggled(Boolean toggled) {
		this.toggled = toggled;
		updateLabel();
	}
	
	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
		if(!drawButton)
        {
            return;
        }
		
		if(this.icon==null) {
			super.drawButton(minecraft, mouseX, mouseY);
			return;
		}
		
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		if(enabled) {
			GL11.glColor4f(1F, 1F, 1F, 1F);
		} else {
			GL11.glColor4f(.5F, .5F, .5F, 1F);
		}
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, minecraft.renderEngine.getTexture(icon));
		
		// Check hover
		boolean hover = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

		// Preserve aspect ratio of source image		
		int w = toggled ? this.width : (int) Math.ceil(this.width*.6);
		int h = toggled ? this.height :  (int) Math.ceil(this.height*.6);
		int widthOffset = (this.width-w)/2;
		int heightOffset = (this.height-h);

		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(xPosition + widthOffset, h + yPosition + heightOffset, 0.0D, 0, 1);
		tessellator.addVertexWithUV(xPosition + w + widthOffset, h + yPosition + heightOffset, 0.0D, 1, 1);
		tessellator.addVertexWithUV(xPosition + w + widthOffset, yPosition + heightOffset, 0.0D, 1, 0);
		tessellator.addVertexWithUV(xPosition + widthOffset, yPosition + heightOffset, 0.0D, 0, 0);
		tessellator.draw();
		
        if(hover)
        {
        	int color = enabled ? 0xffffa0 : 0xcccccc;
        	if(multiline!=null && multiline.length==2) {
        		drawCenteredString(minecraft.fontRenderer, multiline[0], xPosition + width / 2, yPosition + (height - 16) / 2, color);
        		drawCenteredString(minecraft.fontRenderer, multiline[1], xPosition + width / 2, yPosition + (height - 0) / 2, color);
        	} else {
        		drawCenteredString(minecraft.fontRenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, color);
        	}
        } 
    }

	//@Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        return enabled && drawButton && i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
    }
}
