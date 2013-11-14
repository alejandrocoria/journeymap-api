package net.techbrew.mcjm.render.overlay;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraft.src.AbstractTexture;
import net.minecraft.src.ResourceManager;
import net.minecraft.src.TextureUtil;

import org.lwjgl.opengl.GL11;

public class MapTexture extends AbstractTexture
{
    private final int[] dynamicTextureData;

    /** width of this icon in pixels */
    public final int width;

    /** height of this icon in pixels */
    public final int height;

    public MapTexture(BufferedImage image)
    {    	
    	this.width = image.getWidth();
        this.height = image.getHeight();
        this.dynamicTextureData = new int[width * height];
        TextureUtil.allocateTexture(this.getGlTextureId(), width, height);
        updateTexture(image);
    }    

    public void updateTexture(BufferedImage image)
    {
    	if(image.getWidth()!=width || image.getHeight()!=height) {
    		throw new IllegalArgumentException("Image dimensions don't match");
    	}
    	image.getRGB(0, 0, width, height, this.dynamicTextureData, 0, width);
        TextureUtil.uploadTexture(getGlTextureId(), dynamicTextureData, width, height);
    }

	public void clear() {
		if(this.glTextureId!=-1) {
			GL11.glDeleteTextures(this.getGlTextureId());
		}
	}

	@Override
	public void loadTexture(ResourceManager par1ResourceManager) throws IOException {}
}
