package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.render.draw.DrawUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MapButton extends GuiButton {

	private Boolean toggled = true;
	String icon;
	DynamicTexture iconTexture;
	String labelOn;
	String labelOff;

    public boolean enabled;
    public boolean drawButton;
    public boolean noDisableText;

    private void tempInit(){
        this.enabled = true;
        this.drawButton = true;
    }

	public MapButton(int id, int x, int y, String label) {
		super(id, x, y, label);
        tempInit();
	}
	
	public MapButton(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, label);
        tempInit();
	}
	
	public MapButton(int id, int x, int y, int width, int height, String labelOn, String labelOff, boolean toggled) {
		super(id, x, y, width, height, toggled ? labelOn : labelOff);
		this.labelOn = labelOn;
		this.labelOff = labelOff;
		this.setToggled(toggled);
        tempInit();
	}	
	
	public MapButton(int id, int x, int y, String labelOn, String labelOff, boolean toggled) {
		super(id, x, y, toggled ? labelOn : labelOff);
		this.labelOn = labelOn;
		this.labelOff = labelOff;
		this.setToggled(toggled);
        tempInit();
	}	

	private void updateLabel() {
		if(labelOn!=null && labelOff!=null) {
			super.displayString = getToggled() ? labelOn : labelOff;
		}		
	}
	
	public void toggle() {
		setToggled(!getToggled());
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

        super.drawButton(minecraft, mouseX, mouseY);

        if(!this.enabled) {

            if(mouseOver(mouseX, mouseY)) {

                if(noDisableText) {
                    DrawUtil.drawRectangle(this.getX(), this.getY(), width, height, Color.darkGray, 185);
                } else {
                    DrawUtil.drawRectangle(this.getX()+1, this.getY()+1, width-2, height-2, Color.darkGray, 255);
                    this.drawCenteredString(minecraft.fontRenderer, Constants.getString("MapOverlay.disabled_feature"), this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, -6250336);
                }
            } else {
                //this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, -6250336);
                DrawUtil.drawRectangle(this.getX(), this.getY(), width, height, Color.darkGray, 185);
            }
        }

		if(this.icon!=null) {
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
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, iconTexture.getGlTextureId());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			
			// Preserve aspect ratio of source image
            int height = getHeight();
            int width = getWidth();
			int w = getToggled() ? width : (int) Math.ceil(width*.6);
			int h = getToggled() ? height :  (int) Math.ceil(height*.6);
			int widthOffset = (width-w)/2;
			int heightOffset = (height-h);
	
			tessellator.startDrawingQuads();
            int xPosition = getX();
            int yPosition = getY();
			tessellator.addVertexWithUV(xPosition + widthOffset, h + yPosition + heightOffset, 0.0D, 0, 1);
			tessellator.addVertexWithUV(xPosition + w + widthOffset, h + yPosition + heightOffset, 0.0D, 1, 1);
			tessellator.addVertexWithUV(xPosition + w + widthOffset, yPosition + heightOffset, 0.0D, 1, 0);
			tessellator.addVertexWithUV(xPosition + widthOffset, yPosition + heightOffset, 0.0D, 0, 0);
			tessellator.draw();
		}
		
    }

    @Override
	public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        return enabled && drawButton && i >= getX() && j >= getY() && i < getX() + getWidth() && j < getY() + getHeight();
    }

    public boolean mouseOver(int mouseX, int mouseY) {
        return mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX <= (this.xPosition + this.width)
                && mouseY <= (this.yPosition + this.height);
    }

	public Boolean getToggled() {
		return toggled;
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

    public int getX() {
        return this.xPosition;
    }

    public int getY() {
        return this.yPosition;
    }

    public void setX(int x) {
        this.xPosition = x;
    }

    public void setY(int y) {
        this.yPosition = y;
    }
	
	public void setPosition(int x, int y) {
		setX(x);
        setY(y);
	}
	
	public MapButton leftOf(int x) {
		this.setX(x - getWidth());
		return this;
	}

    public MapButton rightOf(int x) {
        this.setX(x);
        return this;
    }
	
	public MapButton centerHorizontalOn(int x) {
		this.setX(x - (getWidth()/2));
		return this;
	}
	
	public MapButton leftOf(MapButton other, int margin) {
        this.setX(other.getX() - getWidth() - margin);
		return this;
	}
	
	public MapButton rightOf(MapButton other, int margin) {
        this.setX(other.getX() + other.getWidth() + margin);
		return this;
	}
	
	public MapButton below(MapButton other, int margin) {
        this.setY(other.getY() + this.getHeight() + margin);
		return this;
	}
	
}
