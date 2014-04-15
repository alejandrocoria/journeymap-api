package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.render.draw.DrawUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Button extends GuiSmallButton implements ScrollPane.Scrollable {

	private Boolean toggled = true;
	String icon;
	DynamicTexture iconTexture;
	protected String labelOn;
    protected String labelOff;

    protected static Color smallFrameColorLight = new Color(160, 160, 160);
    protected static Color smallFrameColorDark = new Color(120, 120, 120);
    protected static Color smallBgColor = new Color(100, 100, 100);
    protected static Color smallBgHoverColor = new Color(125,135,190);

    public boolean enabled;
    public boolean drawButton;
    public boolean noDisableText;
    public boolean drawFrame;
    public boolean drawBackground;

    private void tempInit(){
        this.enabled = true;
        this.drawButton = true;
        this.drawFrame = true;
        this.drawBackground = true;
    }

    public Button(Enum enumValue, String label) {
        super(enumValue.ordinal(), 0, 0, label);
        tempInit();
    }

	public Button(int id, int x, int y, String label) {
		super(id, x, y, label);
        tempInit();
	}
	
	public Button(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, label);
        tempInit();
	}
	
	public Button(int id, int x, int y, int width, int height, String labelOn, String labelOff, boolean toggled) {
		super(id, x, y, width, height, toggled ? labelOn : labelOff);
		this.labelOn = labelOn;
		this.labelOff = labelOff;
		this.setToggled(toggled);
        tempInit();
	}	
	
	public Button(int id, int x, int y, String labelOn, String labelOff, boolean toggled) {
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

    public void setWidth(int width) {
        this.width = width;
    }

    public int getFitWidth(FontRenderer fr) {
        int max = fr.getStringWidth(displayString);
        if(this.labelOn!=null) {
            max = Math.max(max, fr.getStringWidth(labelOn));
        }
        if(this.labelOff!=null) {
            max = Math.max(max, fr.getStringWidth(labelOff));
        }
        return max + 8;
    }

    public void fitWidth(FontRenderer fr) {
        this.width = getFitWidth(fr);
    }

    public void drawPartialScrollable(Minecraft minecraft, int x, int y, int width, int height)
    {
        minecraft.getTextureManager().bindTexture(buttonTextures);
        int k = 0;// this.getHoverState(this.field_82253_i);
        this.drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height);
        this.drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height);
    }

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
		if(!drawButton)
        {
            return;
        }

        if(this.height>=20)
        {
            // Use resource pack texture
            super.drawButton(minecraft, mouseX, mouseY);
        }
        else
        {
            // Use small button colors
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_82253_i = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;


            if(drawFrame)
            {
                DrawUtil.drawRectangle(xPosition, yPosition, width, 1, smallFrameColorLight, 255); // Top
                DrawUtil.drawRectangle(xPosition, yPosition, 1, height, smallFrameColorLight, 255); // Left

                DrawUtil.drawRectangle(xPosition, yPosition + height -1, width - 1, 1, smallFrameColorDark, 255); // Bottom
                DrawUtil.drawRectangle(xPosition + width -1, yPosition + 1, 1, height-1, smallFrameColorDark, 255); // Right
            }

            if(drawBackground)
            {
                int k = this.getHoverState(this.field_82253_i);
                DrawUtil.drawRectangle(xPosition + 1, yPosition + 1, width - 2, height - 2, k == 2 ? smallBgHoverColor : smallBgColor, 255);
            }

            this.mouseDragged(minecraft, mouseX, mouseY);
            int l = 14737632;

            if (!this.enabled)
            {
                l = -6250336;
            }
            else if (this.field_82253_i)
            {
                l = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        }

        if(!this.enabled)
        {
            boolean drawDisabledText = mouseOver(mouseX, mouseY) && !noDisableText;
            int alpha = drawDisabledText ? 255 : 185;
            if(this.height>=20)
            {
                DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, width - 3, height - 2, Color.darkGray, alpha);
                if(drawDisabledText)
                {
                    this.drawCenteredString(minecraft.fontRenderer, Constants.getString("MapOverlay.disabled_feature"), this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, -6250336);
                }
            }
            else
            {
                DrawUtil.drawRectangle(this.getX() + 1, this.getY() + 1, width - 2, height - 2, Color.darkGray, alpha);
                this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, Color.darkGray.brighter().getRGB());
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

    public void setHeight(int height) {
        this.height = height;
    }

    public void setTextOnly(FontRenderer fr) {
        setHeight(fr.FONT_HEIGHT+1);
        fitWidth(fr);
        drawBackground = false;
        drawFrame = false;
    }

    @Override
    public void drawScrollable(Minecraft mc, int mouseX, int mouseY) {
        drawButton(mc, mouseX, mouseY);
    }

    @Override
    public void clickScrollable(Minecraft mc, int mouseX, int mouseY) {
        // Do nothing - needs to be handled with Gui actionPerformed
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
	
	public Button leftOf(int x) {
		this.setX(x - getWidth());
		return this;
	}

    public Button rightOf(int x) {
        this.setX(x);
        return this;
    }
	
	public Button centerHorizontalOn(int x) {
		this.setX(x - (getWidth()/2));
		return this;
	}
	
	public Button leftOf(Button other, int margin) {
        this.setX(other.getX() - getWidth() - margin);
		return this;
	}
	
	public Button rightOf(Button other, int margin) {
        this.setX(other.getX() + other.getWidth() + margin);
		return this;
	}

    public Button above(Button other, int margin) {
        this.setY(other.getY() - this.getHeight() - margin);
        return this;
    }

    public Button above(int y) {
        this.setY(y - this.getHeight());
        return this;
    }
	
	public Button below(Button other, int margin) {
        this.setY(other.getY() + this.getHeight() + margin);
		return this;
	}

    public Button below(int y) {
        this.setY(y);
        return this;
    }
}
