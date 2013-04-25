package net.techbrew.mcjm.model;

import java.util.List;

import net.minecraft.src.Texture;
import net.minecraft.src.TextureStitched;

public class TextureStitchedStub extends TextureStitched {

    /** width of this icon in pixels */
    public int width;

    /** height of this icon in pixels */
    private int height;
    
	public TextureStitchedStub(TextureStitched original) {
		super(original.getIconName());
		this.copyFrom(original);
	}
	
	public Texture getTextureSheet() {
		return super.textureSheet;
	}
	
	public void init(Texture par1Texture, List par2List, int par3, int par4, int par5, int par6, boolean par7)
    {
		super.init(par1Texture, par2List, par3, par4, par5, par6, par7);
//        this.textureSheet = par1Texture;
//        this.textureList = par2List;
//        this.originX = par3;
//        this.originY = par4;
        this.width = par5;
        this.height = par6;
//        this.rotated = par7;
//        float var8 = 0.01F / (float)par1Texture.getWidth();
//        float var9 = 0.01F / (float)par1Texture.getHeight();
//        this.minU = (float)par3 / (float)par1Texture.getWidth() + var8;
//        this.maxU = (float)(par3 + par5) / (float)par1Texture.getWidth() - var8;
//        this.minV = (float)par4 / (float)par1Texture.getHeight() + var9;
//        this.maxV = (float)(par4 + par6) / (float)par1Texture.getHeight() - var9;
//        this.widthNorm = (float)par5 / 16.0F;
//        this.heightNorm = (float)par6 / 16.0F;
    }
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
